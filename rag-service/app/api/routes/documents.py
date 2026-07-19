"""文档路由 - 主流 RAG 模式：异步解析 + 预览 + 重新解析。

流程：
1. 上传文件 -> 立即返回 doc_id（状态=parsing）
2. 后台异步：解析文件 -> 存 parsed/{doc_id}.md -> LightRAG 索引
3. 前端轮询状态（parsing -> indexed/failed）
4. 预览：读 parsed/{doc_id}.md
5. 重新解析：清除旧 chunk -> 重新解析+索引
"""
from __future__ import annotations

import asyncio
import logging
import uuid
from datetime import datetime
from pathlib import Path
from typing import List, Optional

from fastapi import APIRouter, BackgroundTasks, Depends, File, Form, HTTPException, Query, UploadFile, status

from app.config import settings
from app.core.parser import (
    chunk_text,
    load_parsed,
    parse_file,
    save_parsed,
)
from app.core.rag_engine import RagEngine, get_engine
from app.models.schemas import (
    DocumentInfo,
    DocumentListResponse,
    DocumentUploadResponse,
    ErrorResponse,
)

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/v1/documents", tags=["documents"])


_ALLOWED_EXTS = {".pdf", ".docx", ".txt", ".md"}


def _parsed_dir() -> Path:
    p = settings.working_dir_path / "parsed"
    p.mkdir(parents=True, exist_ok=True)
    return p


def _recover_meta_from_disk(doc_id: str, engine: RagEngine) -> Optional[dict]:
    """从磁盘扫描恢复 meta（RAG 服务重启后 _indexed_files 内存丢失）。

    扫描 upload_dir 下所有 kb 子目录与根目录，匹配 {doc_id}{ext}。
    若 storage/parsed/{doc_id}.md 存在则状态为 indexed，否则为 parsing。
    """
    upload_dir = settings.upload_dir_path
    if not upload_dir.exists():
        return None
    targets = [upload_dir] + [d for d in upload_dir.iterdir() if d.is_dir()]
    for d in targets:
        for f in d.iterdir() if d.is_dir() else []:
            if f.is_file() and (f.name.startswith(doc_id) or f.stem == doc_id):
                kb_id = f.parent.name if f.parent != upload_dir else "default"
                parsed_md = _parsed_dir() / f"{doc_id}.md"
                status = "indexed" if parsed_md.exists() else "parsing"
                meta = {
                    "doc_id": doc_id,
                    "file_path": str(f),
                    "kb_id": kb_id,
                    "status": status,
                    "uploaded_at": datetime.fromtimestamp(f.stat().st_mtime),
                }
                engine._indexed_files[doc_id] = meta
                return meta
    return None


def _ensure_meta(doc_id: str, engine: RagEngine) -> Optional[dict]:
    """先查内存，丢失则从磁盘恢复。"""
    meta = engine._indexed_files.get(doc_id)
    if meta:
        return meta
    return _recover_meta_from_disk(doc_id, engine)


def _doc_info(doc_id: str, meta: dict) -> DocumentInfo:
    p = Path(meta["file_path"])
    return DocumentInfo(
        id=doc_id,
        filename=p.name,
        size_bytes=p.stat().st_size if p.exists() else 0,
        status=meta.get("status", "indexed"),
        kb_id=meta.get("kb_id", "default"),
        uploaded_at=datetime.fromtimestamp(p.stat().st_mtime) if p.exists() else datetime.utcnow(),
        page_count=meta.get("page_count"),
        chunk_count=meta.get("chunk_count"),
    )


async def _index_doc_async(engine: RagEngine, doc_id: str, file_path: str):
    """后台异步：解析 -> 存文本 -> LightRAG 索引。"""
    meta = engine._indexed_files.get(doc_id)
    if not meta:
        logger.error("async index: doc %s not found", doc_id)
        return
    try:
        meta["status"] = "parsing"
        result = parse_file(file_path, doc_id=doc_id)
        save_parsed(_parsed_dir(), doc_id, result["text"])
        meta["page_count"] = result["pages"]
        meta["chunk_count"] = result["meta"]["chunk_count"]
        meta["parser"] = result["meta"]["parser"]

        # LightRAG 索引（用解析后的纯文本）
        await engine.initialize()
        await engine.rag._ensure_lightrag_initialized()
        await engine.rag.lightrag.ainsert(result["text"], ids=[doc_id])
        meta["status"] = "indexed"
        logger.info("doc %s indexed: %d pages, %d chunks",
                    doc_id, result["pages"], result["meta"]["chunk_count"])
    except Exception as e:
        meta["status"] = "failed"
        meta["error"] = str(e)
        logger.exception("async index failed for %s", doc_id)


@router.post(
    "/upload",
    response_model=DocumentUploadResponse,
    responses={400: {"model": ErrorResponse}, 413: {"model": ErrorResponse}},
)
async def upload_document(
    background: BackgroundTasks,
    file: UploadFile = File(..., description="要索引的文档"),
    kb_id: str = Form("default", description="目标知识库 ID"),
    engine: RagEngine = Depends(get_engine),
) -> DocumentUploadResponse:
    """上传文档，立即返回 doc_id，后台异步解析+索引。"""
    if not file.filename:
        raise HTTPException(status_code=400, detail="filename is required")

    ext = Path(file.filename).suffix.lower()
    if ext not in _ALLOWED_EXTS:
        raise HTTPException(
            status_code=400,
            detail=f"unsupported file type: {ext}. allowed: {sorted(_ALLOWED_EXTS)}",
        )

    doc_id = uuid.uuid4().hex
    kb_dir = settings.upload_dir_path / (kb_id or "default")
    kb_dir.mkdir(parents=True, exist_ok=True)
    dest = kb_dir / f"{doc_id}{ext}"

    size = 0
    with dest.open("wb") as out:
        while chunk := await file.read(1024 * 1024):
            size += len(chunk)
            if size > settings.max_upload_size_mb * 1024 * 1024:
                out.close()
                dest.unlink(missing_ok=True)
                raise HTTPException(
                    status_code=413,
                    detail=f"file too large, max {settings.max_upload_size_mb} MB",
                )
            out.write(chunk)

    # 立即登记，状态=parsing，后台异步解析
    engine._indexed_files[doc_id] = {
        "doc_id": doc_id,
        "file_path": str(dest),
        "kb_id": kb_id or "default",
        "status": "parsing",
        "uploaded_at": datetime.utcnow(),
    }
    background.add_task(_index_doc_async, engine, doc_id, str(dest))

    return DocumentUploadResponse(
        id=doc_id,
        filename=file.filename,
        size_bytes=size,
        status="parsing",
        message="document uploaded, parsing in background",
        uploaded_at=datetime.utcnow(),
    )


@router.get("", response_model=DocumentListResponse)
async def list_documents(
    kb_id: str = Query(None, description="按知识库过滤"),
    engine: RagEngine = Depends(get_engine),
) -> DocumentListResponse:
    # 内存字典为空时，从磁盘扫描恢复所有已上传文档
    if not engine._indexed_files:
        upload_dir = settings.upload_dir_path
        if upload_dir.exists():
            scan_dirs = [d for d in upload_dir.iterdir() if d.is_dir()]
            for d in scan_dirs:
                for f in d.iterdir():
                    if f.is_file() and f.suffix.lower() in _ALLOWED_EXTS:
                        _ensure_meta(f.stem, engine)
    items: List[DocumentInfo] = []
    for doc_id, meta in list(engine._indexed_files.items()):
        doc_kb_id = meta.get("kb_id", "default")
        if kb_id and doc_kb_id != kb_id:
            continue
        items.append(_doc_info(doc_id, meta))
    return DocumentListResponse(total=len(items), items=items)


@router.get("/{doc_id}", response_model=DocumentInfo)
async def get_document(
    doc_id: str,
    engine: RagEngine = Depends(get_engine),
) -> DocumentInfo:
    meta = _ensure_meta(doc_id, engine)
    if not meta:
        raise HTTPException(status_code=404, detail="document not found")
    return _doc_info(doc_id, meta)


@router.get("/{doc_id}/preview")
async def preview_document(
    doc_id: str,
    engine: RagEngine = Depends(get_engine),
):
    """返回解析后的 Markdown 文本 + 分块列表。"""
    meta = _ensure_meta(doc_id, engine)
    if not meta:
        raise HTTPException(status_code=404, detail="document not found")

    text = load_parsed(_parsed_dir(), doc_id)
    if text is None:
        # 尚未解析完，尝试现场解析
        try:
            result = parse_file(meta["file_path"], doc_id=doc_id)
            text = result["text"]
            save_parsed(_parsed_dir(), doc_id, text)
            meta["page_count"] = result["pages"]
            meta["chunk_count"] = result["meta"]["chunk_count"]
            meta["parser"] = result["meta"]["parser"]
            meta["status"] = "indexed"
        except Exception as e:
            raise HTTPException(status_code=422, detail=f"preview failed: {e}")

    chunks = chunk_text(text, doc_id=doc_id)
    return {
        "id": doc_id,
        "filename": Path(meta["file_path"]).name,
        "status": meta.get("status", "unknown"),
        "page_count": meta.get("page_count"),
        "chunk_count": len(chunks),
        "parser": meta.get("parser", "unknown"),
        "text": text,
        "chunks": [{"id": c["id"], "text": c["text"][:200] + ("..." if len(c["text"]) > 200 else "")} for c in chunks],
    }


@router.post("/{doc_id}/reparse")
async def reparse_document(
    doc_id: str,
    background: BackgroundTasks,
    engine: RagEngine = Depends(get_engine),
):
    """重新解析文档：清除旧 chunk -> 重新解析+索引。"""
    meta = _ensure_meta(doc_id, engine)
    if not meta:
        raise HTTPException(status_code=404, detail="document not found")

    file_path = meta["file_path"]
    if not Path(file_path).exists():
        raise HTTPException(status_code=404, detail="source file not found")

    # 清除 LightRAG 中旧 chunk（非阻塞，失败仅记录）
    async def _clear_old():
        try:
            await engine.initialize()
            await engine.rag._ensure_lightrag_initialized()
            await engine.rag.lightrag.adelete([doc_id])
            logger.info("cleared old chunks for %s", doc_id)
        except Exception as e:
            logger.warning("clear old chunks failed for %s: %s", doc_id, e)

    await _clear_old()

    # 删除旧解析文本
    old_parsed = _parsed_dir() / f"{doc_id}.md"
    old_parsed.unlink(missing_ok=True)

    meta["status"] = "parsing"
    meta.pop("error", None)
    background.add_task(_index_doc_async, engine, doc_id, file_path)

    return {"id": doc_id, "status": "parsing", "message": "reparse started in background"}


@router.delete("/{doc_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_document(
    doc_id: str,
    engine: RagEngine = Depends(get_engine),
):
    meta = _ensure_meta(doc_id, engine)
    if not meta:
        raise HTTPException(status_code=404, detail="document not found")
    p = Path(meta["file_path"])
    p.unlink(missing_ok=True)
    # 清理解析文本和 LightRAG chunk
    old_parsed = _parsed_dir() / f"{doc_id}.md"
    old_parsed.unlink(missing_ok=True)
    try:
        await engine.initialize()
        await engine.rag._ensure_lightrag_initialized()
        await engine.rag.lightrag.adelete([doc_id])
    except Exception as e:
        logger.warning("delete lightrag chunks failed for %s: %s", doc_id, e)
    del engine._indexed_files[doc_id]
    logger.info("deleted document %s", doc_id)

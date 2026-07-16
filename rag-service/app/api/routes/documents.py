"""文档路由 - 上传、列表、详情、删除。"""
from __future__ import annotations

import logging
import uuid
from datetime import datetime
from pathlib import Path
from typing import List

from fastapi import APIRouter, Depends, File, HTTPException, UploadFile, status

from app.config import settings
from app.core.rag_engine import RagEngine, get_engine
from app.models.schemas import (
    DocumentInfo,
    DocumentListResponse,
    DocumentUploadResponse,
    ErrorResponse,
)

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/v1/documents", tags=["documents"])


_ALLOWED_EXTS = {
    ".pdf", ".doc", ".docx", ".ppt", ".pptx", ".xls", ".xlsx",
    ".jpg", ".jpeg", ".png", ".bmp", ".tiff", ".gif", ".webp",
    ".txt", ".md",
}


@router.post(
    "/upload",
    response_model=DocumentUploadResponse,
    responses={400: {"model": ErrorResponse}, 413: {"model": ErrorResponse}},
)
async def upload_document(
    file: UploadFile = File(..., description="要索引的文档"),
    engine: RagEngine = Depends(get_engine),
) -> DocumentUploadResponse:
    """上传并索引一个文档到默认知识库。"""
    if not file.filename:
        raise HTTPException(status_code=400, detail="filename is required")

    ext = Path(file.filename).suffix.lower()
    if ext not in _ALLOWED_EXTS:
        raise HTTPException(
            status_code=400,
            detail=f"unsupported file type: {ext}. allowed: {sorted(_ALLOWED_EXTS)}",
        )

    doc_id = uuid.uuid4().hex
    dest = settings.upload_dir_path / f"{doc_id}{ext}"

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

    # 解析 + 索引（同步等待；如要异步改成 BackgroundTasks）
    try:
        # .txt/.md 走 LightRAG 原生插库，绕过 MinerU 解析器
        if ext in {".txt", ".md"}:
            await engine.initialize()  # 先初始化 RAGAnything 实例
            await engine.rag._ensure_lightrag_initialized()  # 确保 lightrag 实例就绪
            content = dest.read_text(encoding="utf-8", errors="ignore")
            await engine.rag.lightrag.ainsert(content, ids=[doc_id])
            engine._indexed_files[doc_id] = {
                "doc_id": doc_id, "file_path": str(dest), "status": "indexed",
            }
        else:
            await engine.index_document(str(dest), doc_id=doc_id)
    except Exception as e:
        logger.exception("indexing failed for %s", dest)
        dest.unlink(missing_ok=True)
        raise HTTPException(status_code=500, detail=f"indexing failed: {e}")

    return DocumentUploadResponse(
        id=doc_id,
        filename=file.filename,
        size_bytes=size,
        status="indexed",
        message="document parsed and indexed successfully",
        uploaded_at=datetime.utcnow(),
    )


@router.get("", response_model=DocumentListResponse)
async def list_documents(
    engine: RagEngine = Depends(get_engine),
) -> DocumentListResponse:
    items: List[DocumentInfo] = []
    for doc_id, meta in engine._indexed_files.items():
        items.append(DocumentInfo(
            id=doc_id,
            filename=Path(meta["file_path"]).name,
            size_bytes=Path(meta["file_path"]).stat().st_size if Path(meta["file_path"]).exists() else 0,
            status=meta.get("status", "indexed"),
            kb_id="default",
            uploaded_at=datetime.fromtimestamp(Path(meta["file_path"]).stat().st_mtime)
                if Path(meta["file_path"]).exists() else datetime.utcnow(),
        ))
    return DocumentListResponse(total=len(items), items=items)


@router.get("/{doc_id}", response_model=DocumentInfo)
async def get_document(
    doc_id: str,
    engine: RagEngine = Depends(get_engine),
) -> DocumentInfo:
    meta = engine._indexed_files.get(doc_id)
    if not meta:
        raise HTTPException(status_code=404, detail="document not found")
    p = Path(meta["file_path"])
    return DocumentInfo(
        id=doc_id,
        filename=p.name,
        size_bytes=p.stat().st_size if p.exists() else 0,
        status=meta.get("status", "indexed"),
        kb_id="default",
        uploaded_at=datetime.fromtimestamp(p.stat().st_mtime) if p.exists() else datetime.utcnow(),
    )


@router.delete("/{doc_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_document(
    doc_id: str,
    engine: RagEngine = Depends(get_engine),
):
    meta = engine._indexed_files.get(doc_id)
    if not meta:
        raise HTTPException(status_code=404, detail="document not found")
    p = Path(meta["file_path"])
    p.unlink(missing_ok=True)
    del engine._indexed_files[doc_id]
    logger.info("deleted document %s", doc_id)

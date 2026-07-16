"""知识库路由 - 统计、列表、重建。"""
from __future__ import annotations

import logging

from fastapi import APIRouter, Depends, HTTPException

from app.core.rag_engine import RagEngine, get_engine
from app.models.schemas import KnowledgeBaseRebuildResponse, KnowledgeBaseStats

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/v1/kb", tags=["knowledge-base"])


@router.get("/{kb_id}/stats", response_model=KnowledgeBaseStats)
async def get_stats(
    kb_id: str,
    engine: RagEngine = Depends(get_engine),
) -> KnowledgeBaseStats:
    stats = await engine.get_stats()
    return KnowledgeBaseStats(
        kb_id=kb_id,
        document_count=stats.get("document_count", 0),
        chunk_count=stats.get("chunk_count", 0),
        entity_count=0,  # TODO 从 LightRAG 实体表读
        relation_count=0,
        total_size_bytes=0,
    )


@router.post("/{kb_id}/rebuild", response_model=KnowledgeBaseRebuildResponse)
async def rebuild(
    kb_id: str,
    engine: RagEngine = Depends(get_engine),
) -> KnowledgeBaseRebuildResponse:
    """重新解析所有已索引文档。"""
    try:
        for doc_id, meta in list(engine._indexed_files.items()):
            await engine.index_document(meta["file_path"], doc_id=doc_id)
    except Exception as e:
        logger.exception("rebuild failed")
        raise HTTPException(status_code=500, detail=str(e))
    return KnowledgeBaseRebuildResponse(
        kb_id=kb_id, status="completed", message="rebuilt successfully",
    )

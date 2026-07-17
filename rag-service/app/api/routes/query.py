"""查询路由 - 文本问答和多模态问答。"""
from __future__ import annotations

import logging
import time

from fastapi import APIRouter, Depends, HTTPException

from app.core.rag_engine import RagEngine, get_engine
from app.models.schemas import (
    MultimodalQueryRequest,
    QueryRequest,
    QueryResponse,
    QuerySource,
)

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/v1/query", tags=["query"])


@router.post("", response_model=QueryResponse)
async def query(
    body: QueryRequest,
    engine: RagEngine = Depends(get_engine),
) -> QueryResponse:
    """纯文本 / VLM 增强查询。"""
    t0 = time.time()
    try:
        kwargs = {}
        if body.vlm_enhanced is not None:
            kwargs["vlm_enhanced"] = body.vlm_enhanced

        # 生产级：多层动态召回 - 先按 doc_ids 做第一层过滤，再做语义检索
        query_kwargs = {
            "mode": body.mode,
            "top_k": body.top_k or 10,
        }
        if body.doc_ids:
            # 限定在指定文档集合内检索（RAG-Anything/LightRAG 支持 context 过滤）
            query_kwargs["doc_ids"] = body.doc_ids
        answer = await engine.query(
            body.question,
            **query_kwargs,
            **kwargs,
        )
    except Exception as e:
        logger.exception("query failed")
        raise HTTPException(status_code=500, detail=str(e))

    duration_ms = int((time.time() - t0) * 1000)
    return QueryResponse(
        answer=answer if isinstance(answer, str) else str(answer),
        sources=[],  # 简化版：未来从 context 抽 sources
        mode=body.mode,
        duration_ms=duration_ms,
    )


@router.post("/multimodal", response_model=QueryResponse)
async def query_multimodal(
    body: MultimodalQueryRequest,
    engine: RagEngine = Depends(get_engine),
) -> QueryResponse:
    """多模态查询 - 同时传文本问题 + 图片/表格/公式内容。"""
    t0 = time.time()
    try:
        answer = await engine.query_with_multimodal(
            body.question,
            body.multimodal_content,
            mode=body.mode,
        )
    except Exception as e:
        logger.exception("multimodal query failed")
        raise HTTPException(status_code=500, detail=str(e))

    duration_ms = int((time.time() - t0) * 1000)
    return QueryResponse(
        answer=answer if isinstance(answer, str) else str(answer),
        sources=[],
        mode=body.mode,
        duration_ms=duration_ms,
    )

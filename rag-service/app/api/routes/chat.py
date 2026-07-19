"""直连 LLM 测试端点 - 不需要上传文档，直接测试 AI 链路是否通。

POST /api/v1/chat/raw
{ "prompt": "你好" }
=> { "answer": "..." }

POST /api/v1/chat/embedding
{ "texts": ["句子1", "句子2"] }
=> { "dim": 1536, "count": 2 }

GET  /api/v1/chat/health - 综合健康检查（LLM + Vision + Embedding）
"""
from __future__ import annotations

import logging
import time

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, Field

from app.core.mock_llm import is_mock_mode
from app.core.rag_engine import RagEngine, get_engine

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/v1/chat", tags=["chat-test"])


class RawChatRequest(BaseModel):
    prompt: str = Field(..., min_length=1, max_length=4000)
    system: str | None = None


class RawChatResponse(BaseModel):
    answer: str
    mock_mode: bool
    duration_ms: int


class EmbeddingRequest(BaseModel):
    texts: list[str] = Field(..., min_length=1, max_length=64)


class EmbeddingResponse(BaseModel):
    dim: int
    count: int
    mock_mode: bool


@router.post("/raw", response_model=RawChatResponse)
async def raw_chat(
    body: RawChatRequest,
    engine: RagEngine = Depends(get_engine),
) -> RawChatResponse:
    """直接调用 LLM，不走 RAG 检索。

    用于：测 API 链路、压测 LLM、调试 prompt。
    """
    await engine.initialize()
    t0 = time.time()
    try:
        if engine.is_mock_mode():
            from app.core.mock_llm import mock_llm_func
            answer = await mock_llm_func(body.prompt, system_prompt=body.system)
        else:
            answer = await engine._rag.lightrag.llm_model_func(
                body.prompt, system_prompt=body.system or "你是 OA 系统的 AI 助手。",
            )
        if not isinstance(answer, str):
            answer = str(answer)
    except Exception as e:
        logger.exception("raw_chat failed")
        raise HTTPException(status_code=500, detail=str(e))
    return RawChatResponse(
        answer=answer,
        mock_mode=engine.is_mock_mode(),
        duration_ms=int((time.time() - t0) * 1000),
    )


@router.post("/embedding", response_model=EmbeddingResponse)
async def raw_embedding(
    body: EmbeddingRequest,
    engine: RagEngine = Depends(get_engine),
) -> EmbeddingResponse:
    """测 embedding 模型。"""
    await engine.initialize()
    try:
        if engine.is_mock_mode():
            from app.core.mock_llm import mock_embed_func
            vecs = await mock_embed_func(body.texts)
        else:
            vecs = await engine._rag.lightrag.embedding_func(body.texts)
        return EmbeddingResponse(
            dim=int(vecs.shape[1]) if hasattr(vecs, "shape") else (len(vecs[0]) if vecs else 0),
            count=len(body.texts),
            mock_mode=engine.is_mock_mode(),
        )
    except Exception as e:
        logger.exception("raw_embedding failed")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/health")
async def chat_health(engine: RagEngine = Depends(get_engine)):
    """综合健康检查 - 测三件套：LLM、Vision、Embedding。"""
    from app.core.mock_llm import is_mock_mode
    await engine.initialize()
    results = {"mock_mode": engine.is_mock_mode(), "checks": {}}
    try:
        if engine.is_mock_mode():
            from app.core.mock_llm import mock_llm_func
            ans = await mock_llm_func("ping")
            results["checks"]["llm"] = "ok" if "Mock" in ans else "fail"
            results["checks"]["vision"] = "ok (mock)"
            from app.core.mock_llm import mock_embed_func
            v = await mock_embed_func(["x"])
            results["checks"]["embedding"] = "ok" if len(v[0]) == 1536 else "fail"
        else:
            ans = await engine._rag.lightrag.llm_model_func("ping")
            results["checks"]["llm"] = "ok" if ans else "fail"
            v = await engine._rag.lightrag.embedding_func(["x"])
            results["checks"]["embedding"] = "ok" if v else "fail"
            results["checks"]["vision"] = "ok (configured)" if engine.rag.vision_model_func else "not configured"
    except Exception as e:
        results["error"] = str(e)
    return results

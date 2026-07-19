"""配置路由 - 接收 Spring Boot 推送的三组模型配置。"""
from __future__ import annotations

import logging
from typing import Any, Dict

from fastapi import APIRouter, Depends
from pydantic import BaseModel, Field

from app.core.rag_engine import RagEngine, get_engine

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/v1/config", tags=["config"])


class ModelConfig(BaseModel):
    embedding_provider: str | None = None
    embedding_base_url: str | None = None
    embedding_model: str | None = None
    embedding_api_key: str | None = None
    embedding_dim: int | None = None
    llm_provider: str | None = None
    llm_base_url: str | None = None
    llm_model: str | None = None
    llm_api_key: str | None = None
    llm_temperature: float | None = None
    llm_max_tokens: int | None = None
    reranker_provider: str | None = None
    reranker_base_url: str | None = None
    reranker_model: str | None = None
    reranker_api_key: str | None = None
    mock_mode: int | None = None


@router.get("")
async def get_config(engine: RagEngine = Depends(get_engine)) -> Dict[str, Any]:
    """返回当前 RAG 引擎配置（api_key 脱敏）。"""
    cfg = engine.get_config()
    for k in ("embedding_api_key", "llm_api_key", "reranker_api_key"):
        if cfg.get(k):
            cfg[k] = "***"
    cfg["initialized"] = engine._initialized
    cfg["mock"] = engine.is_mock_mode()
    return cfg


@router.post("")
async def update_config(
    body: ModelConfig,
    engine: RagEngine = Depends(get_engine),
) -> Dict[str, Any]:
    """Spring Boot 保存配置后调用此接口，推送新配置到 RAG 引擎。"""
    cfg = {k: v for k, v in body.model_dump().items() if v is not None}
    engine.update_config(cfg)
    logger.info("RAG 配置已更新: mock=%s, llm=%s, embedding=%s",
                engine.is_mock_mode(),
                cfg.get("llm_model"), cfg.get("embedding_model"))
    return {"ok": True, "message": "配置已更新，将在下次调用时重新初始化"}

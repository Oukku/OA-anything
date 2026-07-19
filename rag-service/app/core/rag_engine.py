"""RAG 引擎 - RAGAnything 单例封装。

支持动态配置：通过 update_config 接收 Spring Boot 推送的三组模型配置，
配置变更后自动重新初始化。
"""
from __future__ import annotations

import asyncio
import logging
import os
from pathlib import Path
from typing import Any, Dict, List, Optional

from app.config import settings

logger = logging.getLogger(__name__)
engine_logger = logger

_engine_lock = asyncio.Lock()
_engine: Optional["RagEngine"] = None


class RagEngine:
    """对 RAGAnything 的薄封装，支持动态模型配置。"""

    def __init__(self) -> None:
        self.config = settings
        self._rag = None
        self._initialized = False
        self._mock = True
        self._indexed_files: Dict[str, Dict[str, Any]] = {}

        # 动态模型配置（由 Spring Boot 推送）
        self._model_config: Dict[str, Any] = {
            "embedding_provider": "siliconflow",
            "embedding_base_url": "https://api.siliconflow.cn/v1",
            "embedding_model": "BAAI/bge-m3",
            "embedding_api_key": "",
            "embedding_dim": 1024,
            "llm_provider": "siliconflow",
            "llm_base_url": "https://api.siliconflow.cn/v1",
            "llm_model": "Qwen/Qwen2.5-7B-Instruct",
            "llm_api_key": "",
            "llm_temperature": 0.7,
            "llm_max_tokens": 2048,
            "reranker_provider": "siliconflow",
            "reranker_base_url": "https://api.siliconflow.cn/v1",
            "reranker_model": "BAAI/bge-reranker-v2-m3",
            "reranker_api_key": "",
            "mock_mode": 1,
        }

    def update_config(self, cfg: Dict[str, Any]) -> None:
        """Spring Boot 推送新配置时调用，触发重新初始化。"""
        changed = False
        for k, v in cfg.items():
            if k in self._model_config and self._model_config[k] != v:
                self._model_config[k] = v
                changed = True
        if changed:
            self._initialized = False
            self._rag = None
            logger.info("RAG 配置已更新，将在下次调用时重新初始化")

    def get_config(self) -> Dict[str, Any]:
        return dict(self._model_config)

    def is_mock_mode(self) -> bool:
        return bool(self._model_config.get("mock_mode", 1))

    async def initialize(self) -> None:
        if self._initialized:
            return

        cfg = self._model_config
        mock = self.is_mock_mode()

        # 判断是否有有效 API Key
        llm_key = cfg.get("llm_api_key", "") or ""
        emb_key = cfg.get("embedding_api_key", "") or ""

        if mock or not llm_key or not emb_key:
            logger.info("RAGService 启动在 [MOCK 模式] - 无有效 API Key")
            from app.core.mock_llm import mock_llm_func, mock_vision_func, mock_embed_func
            from lightrag.utils import EmbeddingFunc

            llm_model_func = mock_llm_func
            vision_model_func = mock_vision_func
            embedding_func = EmbeddingFunc(
                embedding_dim=1024,
                max_token_size=8192,
                func=mock_embed_func,
            )
            self._mock = True
        else:
            from lightrag.llm.openai import openai_complete_if_cache, openai_embed
            from lightrag.utils import EmbeddingFunc

            llm_base_url = cfg.get("llm_base_url", "https://api.siliconflow.cn/v1")
            llm_model = cfg.get("llm_model", "Qwen/Qwen2.5-7B-Instruct")
            emb_base_url = cfg.get("embedding_base_url", llm_base_url)
            emb_model = cfg.get("embedding_model", "BAAI/bge-m3")
            emb_dim = int(cfg.get("embedding_dim", 1024))

            logger.info("RAGService 使用真实模型: LLM=%s Embedding=%s", llm_model, emb_model)

            def llm_model_func(prompt, system_prompt=None, history_messages=[], **kwargs):
                return openai_complete_if_cache(
                    llm_model,
                    prompt,
                    system_prompt=system_prompt,
                    history_messages=history_messages,
                    api_key=llm_key,
                    base_url=llm_base_url,
                    **kwargs,
                )

            def vision_model_func(
                prompt, system_prompt=None, history_messages=[],
                image_data=None, messages=None, **kwargs,
            ):
                if messages:
                    return openai_complete_if_cache(
                        llm_model, "",
                        system_prompt=None, history_messages=[],
                        messages=messages,
                        api_key=llm_key,
                        base_url=llm_base_url,
                        **kwargs,
                    )
                if image_data:
                    return openai_complete_if_cache(
                        llm_model, "",
                        system_prompt=None, history_messages=[],
                        messages=[
                            {"role": "system", "content": system_prompt} if system_prompt else None,
                            {"role": "user", "content": [
                                {"type": "text", "text": prompt},
                                {"type": "image_url", "image_url": {"url": f"data:image/jpeg;base64,{image_data}"}},
                            ]},
                        ],
                        api_key=llm_key,
                        base_url=llm_base_url,
                        **kwargs,
                    )
                return llm_model_func(prompt, system_prompt, history_messages, **kwargs)

            async def _embed_func(texts: List[str], **kwargs):
                """调用硅基流动 embedding 接口。"""
                import httpx
                headers = {
                    "Authorization": f"Bearer {emb_key}",
                    "Content-Type": "application/json",
                }
                # 分批处理，每批最多 32 条（硅基流动限制）
                all_vecs = []
                batch_size = 32
                async with httpx.AsyncClient(timeout=60) as client:
                    for i in range(0, len(texts), batch_size):
                        batch = texts[i:i + batch_size]
                        resp = await client.post(
                            f"{emb_base_url.rstrip('/')}/embeddings",
                            headers=headers,
                            json={"model": emb_model, "input": batch},
                        )
                        resp.raise_for_status()
                        data = resp.json()
                        for item in data["data"]:
                            all_vecs.append(item["embedding"])

                import numpy as np
                return np.array(all_vecs, dtype=np.float32)

            embedding_func = EmbeddingFunc(
                embedding_dim=emb_dim,
                max_token_size=8192,
                func=_embed_func,
            )
            self._mock = False

        from raganything import RAGAnything, RAGAnythingConfig

        rag_config = RAGAnythingConfig(
            working_dir=str(self.config.working_dir_path),
            parser=self.config.parser,
            parse_method=self.config.parse_method,
            enable_image_processing=self.config.enable_image_processing,
            enable_table_processing=self.config.enable_table_processing,
            enable_equation_processing=self.config.enable_equation_processing,
        )

        self._rag = RAGAnything(
            config=rag_config,
            llm_model_func=llm_model_func,
            vision_model_func=vision_model_func,
            embedding_func=embedding_func,
        )
        self._initialized = True
        logger.info("RAGAnything 初始化完成 (parser=%s, mock=%s)",
                    self.config.parser, self._mock)

    @property
    def rag(self):
        if not self._initialized:
            raise RuntimeError("RagEngine 未初始化，请先调用 await initialize()")
        return self._rag

    async def index_document(self, file_path: str, doc_id: str, **kwargs) -> Dict[str, Any]:
        await self.initialize()
        await self.rag.process_document_complete(
            file_path=file_path,
            output_dir=str(Path(self.config.upload_dir_path) / "parsed"),
            parse_method=self.config.parse_method,
            parser=self.config.parser,
            doc_id=doc_id,
            **kwargs,
        )
        meta = {"doc_id": doc_id, "file_path": file_path, "status": "indexed"}
        self._indexed_files[doc_id] = meta
        return meta

    async def query(self, question: str, mode: str = "hybrid", **kwargs) -> str:
        await self.initialize()
        return await self.rag.aquery(question, mode=mode, **kwargs)

    async def query_with_multimodal(
        self, question: str, multimodal_content: List[Dict[str, Any]], mode: str = "hybrid", **kwargs
    ) -> str:
        await self.initialize()
        return await self.rag.aquery_with_multimodal(
            question, multimodal_content=multimodal_content, mode=mode, **kwargs,
        )

    async def get_stats(self) -> Dict[str, int]:
        await self.initialize()
        try:
            chunk_count = await self.rag.lightrag.chunk_entity_relation_graph.__len__()  # noqa
        except Exception:
            chunk_count = 0
        return {
            "document_count": len(self._indexed_files),
            "chunk_count": chunk_count,
        }


async def get_engine() -> RagEngine:
    global _engine
    async with _engine_lock:
        if _engine is None:
            _engine = RagEngine()
        return _engine

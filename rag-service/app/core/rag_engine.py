"""RAG 引擎 - RAGAnything 单例封装。

懒加载：首次调用时初始化，避免启动时阻塞。
"""
from __future__ import annotations

import asyncio
import logging
import os
from pathlib import Path
from typing import Any, Dict, List, Optional

from app.config import settings

logger = logging.getLogger(__name__)
# 暴露 self.logger 供 initialize 使用
engine_logger = logger

_engine_lock = asyncio.Lock()
_engine: Optional["RagEngine"] = None


class RagEngine:
    """对 RAGAnything 的薄封装。"""

    def __init__(self) -> None:
        self.config = settings
        self._rag = None
        self._initialized = False
        self._indexed_files: Dict[str, Dict[str, Any]] = {}  # doc_id -> meta

    async def initialize(self) -> None:
        if self._initialized:
            return
        # 重型依赖做懒加载，让 import 失败时只影响运行时而不影响启动
        from raganything import RAGAnything, RAGAnythingConfig

        # 根据环境决定用真实 LLM 还是 mock
        from app.core.mock_llm import is_mock_mode, mock_llm_func, mock_vision_func, mock_embed_func

        if is_mock_mode():
            from lightrag.utils import EmbeddingFunc
            logger.info("RAGService 启动在 [MOCK 模式] - 无 OpenAI Key，使用占位 LLM/Embedding。")
            llm_model_func = mock_llm_func
            vision_model_func = mock_vision_func
            embedding_func = EmbeddingFunc(
                embedding_dim=1536,
                max_token_size=8192,
                func=mock_embed_func,
            )
        else:
            from lightrag.llm.openai import openai_complete_if_cache, openai_embed
            from lightrag.utils import EmbeddingFunc

            def llm_model_func(prompt, system_prompt=None, history_messages=[], **kwargs):
                return openai_complete_if_cache(
                    self.config.llm_model,
                    prompt,
                    system_prompt=system_prompt,
                    history_messages=history_messages,
                    api_key=self.config.openai_api_key,
                    base_url=self.config.openai_base_url,
                    **kwargs,
                )

            def vision_model_func(
                prompt, system_prompt=None, history_messages=[],
                image_data=None, messages=None, **kwargs,
            ):
                if messages:
                    return openai_complete_if_cache(
                        self.config.vision_model, "",
                        system_prompt=None, history_messages=[],
                        messages=messages,
                        api_key=self.config.openai_api_key,
                        base_url=self.config.openai_base_url,
                        **kwargs,
                    )
                if image_data:
                    return openai_complete_if_cache(
                        self.config.vision_model, "",
                        system_prompt=None, history_messages=[],
                        messages=[
                            {"role": "system", "content": system_prompt} if system_prompt else None,
                            {"role": "user", "content": [
                                {"type": "text", "text": prompt},
                                {"type": "image_url", "image_url": {"url": f"data:image/jpeg;base64,{image_data}"}},
                            ]},
                        ],
                        api_key=self.config.openai_api_key,
                        base_url=self.config.openai_base_url,
                        **kwargs,
                    )
                return llm_model_func(prompt, system_prompt, history_messages, **kwargs)

            embedding_func = EmbeddingFunc(
                embedding_dim=self.config.embedding_dim,
                max_token_size=8192,
                func=openai_embed.func,
            )

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
        self._mock = is_mock_mode()
        logger.info("RAGAnything 初始化完成 (parser=%s, mock=%s)",
                    self.config.parser, self._mock)

    @property
    def rag(self):
        if not self._initialized:
            raise RuntimeError("RagEngine 未初始化，请先调用 await initialize()")
        return self._rag

    async def index_document(self, file_path: str, doc_id: str, **kwargs) -> Dict[str, Any]:
        """索引一个文档。"""
        await self.initialize()
        await self.rag.process_document_complete(
            file_path=file_path,
            output_dir=str(Path(self.config.upload_dir_path) / "parsed"),
            parse_method=self.config.parse_method,
            parser=self.config.parser,
            doc_id=doc_id,
            **kwargs,
        )
        meta = {
            "doc_id": doc_id,
            "file_path": file_path,
            "status": "indexed",
        }
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
        # LightRAG 内置统计
        try:
            chunk_count = await self.rag.lightrag.chunk_entity_relation_graph.__len__()  # noqa
        except Exception:  # pragma: no cover
            chunk_count = 0
        return {
            "document_count": len(self._indexed_files),
            "chunk_count": chunk_count,
        }


async def get_engine() -> RagEngine:
    """FastAPI 依赖。"""
    global _engine
    async with _engine_lock:
        if _engine is None:
            _engine = RagEngine()
        return _engine

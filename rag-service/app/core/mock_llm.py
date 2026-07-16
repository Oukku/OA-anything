"""Mock LLM - 离线测试模式，不调用外部 API。

替换 RAGAnything 里的 llm_model_func 和 vision_model_func，
让 rag-service 在没有 OpenAI key 时也能跑通完整流程。
"""
from __future__ import annotations

import hashlib
import json
from typing import Any, Dict, List, Optional


# 预设回复模板，让测试结果看起来合理
_MOCK_TEMPLATES = {
    "summarize": "【Mock 摘要】该文档主要涉及以下要点：1) 业务流程规范 2) 关键节点说明 3) 注意事项与责任人。",
    "qa": "【Mock 回答】根据知识库检索：{question} 涉及的内容可在文档第 {page} 页找到。相关条目已收录。",
    "default": "【Mock 回复】这是离线测试模式返回的占位回答。配置 OPENAI_API_KEY 后将使用真实 LLM。",
}

async def mock_llm_func(prompt: str, system_prompt: Optional[str] = None,
                          history_messages: List[Dict] = None, **kwargs) -> str:
    """Mock LLM 文本生成函数（async）。"""
    p = (prompt or "").lower()
    if "摘要" in p or "summarize" in p:
        return _MOCK_TEMPLATES["summarize"]
    if "?" in prompt or "？" in prompt or "什么" in p or "如何" in p or "怎么" in p:
        return _MOCK_TEMPLATES["qa"].format(
            question=(prompt[:30] + "...") if len(prompt) > 30 else prompt,
            page=hash(prompt) % 10 + 1,
        )
    return _MOCK_TEMPLATES["default"]


async def mock_vision_func(prompt: str, system_prompt: Optional[str] = None,
                           history_messages: List[Dict] = None,
                           image_data: Any = None, messages: Any = None, **kwargs) -> str:
    """Mock 视觉模型（async）。"""
    return "【Mock VLM】图片/表格内容描述（占位）"


async def mock_embed_func(texts: List[str], **kwargs) -> "np.ndarray":
    """Mock Embedding（async）- 返回 2D numpy 数组 (n_texts, dim)，兼容 LightRAG 的 .size 校验。"""
    import numpy as np
    dim = 1536
    out = np.zeros((len(texts), dim), dtype=np.float32)
    for i, t in enumerate(texts):
        h = hashlib.md5(t.encode("utf-8")).digest()
        full = (h * (dim * 4 // len(h) + 1))[: dim * 4]
        out[i] = np.array(
            [int.from_bytes(full[j*4:(j+1)*4], "big") / 2**32 for j in range(dim)],
            dtype=np.float32,
        )
    return out


def is_mock_mode() -> bool:
    """是否启用 mock 模式。"""
    import os
    return not os.getenv("OPENAI_API_KEY") or os.getenv("OPENAI_API_KEY", "").startswith("sk-placeholder")

"""Pydantic 数据模型 - REST API 请求/响应结构。"""
from datetime import datetime
from typing import Any, Dict, List, Literal, Optional
from pydantic import BaseModel, Field


# ===== 文档 =====

class DocumentUploadResponse(BaseModel):
    id: str = Field(..., description="文档 ID")
    filename: str
    size_bytes: int
    status: Literal["queued", "parsing", "indexed", "failed"]
    message: str = ""
    uploaded_at: datetime


class DocumentInfo(BaseModel):
    id: str
    filename: str
    size_bytes: int
    status: str
    kb_id: str
    uploaded_at: datetime
    page_count: Optional[int] = None
    chunk_count: Optional[int] = None
    entity_count: Optional[int] = None


class DocumentListResponse(BaseModel):
    total: int
    items: List[DocumentInfo]


# ===== 查询 =====

class QuerySource(BaseModel):
    """检索到的来源。"""
    type: Literal["text", "image", "table", "equation"]
    content: str
    doc_id: str
    page_idx: Optional[int] = None
    score: float = 0.0


class QueryRequest(BaseModel):
    question: str = Field(..., min_length=1, max_length=2000)
    mode: Literal["hybrid", "local", "global", "naive"] = "hybrid"
    kb_id: str = Field("default", description="知识库 ID")
    doc_ids: Optional[List[str]] = Field(default=None, description="限定检索的文档 ID 列表")
    vlm_enhanced: Optional[bool] = None
    top_k: int = 10
    temperature: float = 0.7
    max_tokens: int = 2048


class QueryResponse(BaseModel):
    answer: str
    sources: List[QuerySource] = []
    mode: str
    duration_ms: int


class MultimodalQueryRequest(QueryRequest):
    """多模态查询 - 带图片/表格/公式内容。"""
    multimodal_content: List[Dict[str, Any]] = Field(
        default_factory=list,
        description='[{"type": "table|equation|image", "table_data": "...", "latex": "..."}]',
    )


# ===== 知识库 =====

class KnowledgeBaseStats(BaseModel):
    kb_id: str
    document_count: int
    chunk_count: int
    entity_count: int
    relation_count: int
    total_size_bytes: int


class KnowledgeBaseRebuildResponse(BaseModel):
    kb_id: str
    status: Literal["rebuilding", "completed", "failed"]
    message: str = ""


# ===== 知识图谱 =====

class GraphNode(BaseModel):
    id: str
    label: str
    type: str = "entity"
    size: int = 20


class GraphEdge(BaseModel):
    source: str
    target: str
    label: str = "关联"


class GraphData(BaseModel):
    nodes: List[GraphNode]
    edges: List[GraphEdge]


# ===== 通用 =====

class HealthResponse(BaseModel):
    status: Literal["ok", "degraded", "down"]
    rag_initialized: bool
    parser: str
    llm_model: str
    working_dir: str
    version: str = "0.1.0"


class ErrorResponse(BaseModel):
    error: str
    detail: Optional[str] = None

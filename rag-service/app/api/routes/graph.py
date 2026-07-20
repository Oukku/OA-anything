"""知识图谱路由 - 解析时预生成缓存，进入界面直接读缓存。

流程：
1. 文档解析完成（documents.py:_index_doc_async）→ 立即调用 build_graph_data + save_graph_cache
2. /api/v1/graph/{doc_id} 优先读缓存（毫秒级返回），未命中再读 graphml 并生成缓存
3. 重新解析时清除旧缓存
"""
from __future__ import annotations

import json
import logging
from pathlib import Path
from typing import Dict, List, Optional
from xml.etree import ElementTree as ET

from fastapi import APIRouter, Depends, HTTPException

from app.config import settings
from app.core.rag_engine import RagEngine, get_engine
from app.models.schemas import GraphData, GraphEdge, GraphNode

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/v1/graph", tags=["knowledge-graph"])


# ------------------ 缓存目录 ------------------

def _cache_dir() -> Path:
    p = settings.working_dir_path / "graph_cache"
    p.mkdir(parents=True, exist_ok=True)
    return p


def _cache_path(doc_id: str) -> Path:
    return _cache_dir() / f"{doc_id}.json"


def _save_graph_cache(doc_id: str, data: GraphData) -> None:
    """将图谱快照写入缓存文件。"""
    try:
        payload = {
            "nodes": [n.model_dump() for n in data.nodes],
            "edges": [e.model_dump() for e in data.edges],
        }
        _cache_path(doc_id).write_text(
            json.dumps(payload, ensure_ascii=False), encoding="utf-8"
        )
        logger.info("graph cache saved for %s (%d nodes, %d edges)",
                    doc_id, len(data.nodes), len(data.edges))
    except Exception as e:
        logger.warning("save graph cache failed for %s: %s", doc_id, e)


def _load_graph_cache(doc_id: str) -> Optional[GraphData]:
    """从缓存文件读取图谱快照。命中返回 GraphData，未命中返回 None。"""
    p = _cache_path(doc_id)
    if not p.exists() or p.stat().st_size == 0:
        return None
    try:
        payload = json.loads(p.read_text(encoding="utf-8"))
        nodes = [GraphNode(**n) for n in payload.get("nodes", [])]
        edges = [GraphEdge(**e) for e in payload.get("edges", [])]
        return GraphData(nodes=nodes, edges=edges)
    except Exception as e:
        logger.warning("load graph cache failed for %s: %s", doc_id, e)
        return None


def _delete_graph_cache(doc_id: str) -> None:
    """清除指定文档的图谱缓存（重新解析/删除时调用）。"""
    try:
        _cache_path(doc_id).unlink(missing_ok=True)
    except Exception as e:
        logger.warning("delete graph cache failed for %s: %s", doc_id, e)


# ------------------ graphml 解析 ------------------

def _find_graph_file(working_dir: Path) -> Optional[Path]:
    """在 working_dir 中查找知识图谱文件，支持多种命名。"""
    candidates = [
        "graph_chunk_entity_relation.graphml",
        "chunk_entity_relation_graph.graphml",
        "chunk_entity_relation_graph.xml",
        "chunk_entity_relation_graph.json",
    ]
    for name in candidates:
        p = working_dir / name
        if p.exists() and p.stat().st_size > 0:
            return p
    return None


def _parse_graphml(text: str) -> tuple[List[GraphNode], List[GraphEdge]]:
    """解析 graphml，提取节点和边。"""
    nodes: Dict[str, GraphNode] = {}
    edges: List[GraphEdge] = []
    try:
        root = ET.fromstring(text)
        ns = "{http://graphml.graphdrawing.org/xmlns}"
        for node_el in root.iter(f"{ns}node"):
            nid = node_el.get("id")
            if not nid:
                continue
            label = nid
            for data_el in node_el.iter(f"{ns}data"):
                if data_el.text:
                    label = data_el.text.strip() or label
            nodes[nid] = GraphNode(id=nid, label=label, type="entity")
        for edge_el in root.iter(f"{ns}edge"):
            src = edge_el.get("source")
            tgt = edge_el.get("target")
            if not src or not tgt:
                continue
            label = "关联"
            for data_el in edge_el.iter(f"{ns}data"):
                if data_el.text:
                    label = data_el.text.strip()
            if src not in nodes:
                nodes[src] = GraphNode(id=src, label=src, type="entity")
            if tgt not in nodes:
                nodes[tgt] = GraphNode(id=tgt, label=tgt, type="entity")
            edges.append(GraphEdge(source=src, target=tgt, label=label))
    except ET.ParseError as e:
        logger.warning("graphml parse failed: %s", e)
    return list(nodes.values()), edges


def _parse_graph_json(text: str) -> tuple[List[GraphNode], List[GraphEdge]]:
    """解析 LightRAG JSON 格式图谱。"""
    nodes: Dict[str, GraphNode] = {}
    edges: List[GraphEdge] = []
    try:
        data = json.loads(text)
        for edge_raw in data.get("edges", []):
            src = str(edge_raw.get("src", edge_raw.get("source", ""))).strip()
            tgt = str(edge_raw.get("tgt", edge_raw.get("target", ""))).strip()
            rel = str(edge_raw.get("relation", "关联")).strip()
            if not src or not tgt:
                continue
            if src not in nodes:
                nodes[src] = GraphNode(id=src, label=src, type="entity")
            if tgt not in nodes:
                nodes[tgt] = GraphNode(id=tgt, label=tgt, type="entity")
            edges.append(GraphEdge(source=src, target=tgt, label=rel))
    except Exception as e:
        logger.warning("graph json parse failed: %s", e)
    return list(nodes.values()), edges


# ------------------ 核心构建函数（可被 documents.py 复用） ------------------

def build_graph_data(doc_id: str, file_path: str, working_dir: Path) -> GraphData:
    """根据 doc_id 构建 GraphData。优先读 graphml，兜底生成文档中心节点。

    此函数纯同步、不依赖 RAG 引擎初始化，可在后台任务中直接调用。
    """
    nodes: List[GraphNode] = []
    edges: List[GraphEdge] = []

    graph_file = _find_graph_file(working_dir)
    if graph_file:
        try:
            text = graph_file.read_text(encoding="utf-8", errors="ignore")
            if graph_file.suffix == ".json":
                nodes, edges = _parse_graph_json(text)
            else:
                nodes, edges = _parse_graphml(text)
        except Exception as e:
            logger.warning("read graph file %s failed: %s", graph_file, e)

    if not nodes:
        # 兜底：没有节点时生成文档中心节点
        filename = Path(file_path).stem if file_path else doc_id
        nodes = [
            GraphNode(id=filename, label=filename, type="document", size=30),
            GraphNode(id="RAG", label="RAG", type="system", size=20),
        ]
        edges = [GraphEdge(source=filename, target="RAG", label="已索引")]

    return GraphData(nodes=nodes, edges=edges)


def build_and_cache_graph(doc_id: str, file_path: str, working_dir: Path) -> GraphData:
    """构建图谱并写入缓存。供 documents.py 在解析完成后调用。"""
    data = build_graph_data(doc_id, file_path, working_dir)
    _save_graph_cache(doc_id, data)
    return data


# ------------------ 路由 ------------------

@router.get("/{doc_id}", response_model=GraphData)
async def get_document_graph(
    doc_id: str,
    engine: RagEngine = Depends(get_engine),
) -> GraphData:
    """返回文档知识图谱。优先读缓存（毫秒级），未命中再读 graphml 并生成缓存。"""
    # 1. 优先读缓存（解析时已预生成，进入界面瞬间返回）
    cached = _load_graph_cache(doc_id)
    if cached is not None:
        logger.info("graph cache hit for %s", doc_id)
        return cached

    # 2. 缓存未命中：恢复 meta → 读 graphml → 写缓存 → 返回
    meta = engine._indexed_files.get(doc_id)
    if not meta:
        upload_dir = Path(engine.config.upload_dir_path)
        for sub in upload_dir.iterdir() if upload_dir.exists() else []:
            if sub.is_dir():
                for f in sub.iterdir():
                    if f.name.startswith(doc_id) or f.stem == doc_id:
                        meta = {"doc_id": doc_id, "file_path": str(f), "status": "indexed"}
                        engine._indexed_files[doc_id] = meta
                        break
                if meta:
                    break
    file_path = meta.get("file_path", doc_id) if meta else doc_id

    working_dir = Path(engine.config.working_dir_path)
    data = build_graph_data(doc_id, file_path, working_dir)
    _save_graph_cache(doc_id, data)  # 顺带补缓存
    return data

"""知识图谱路由 - 直接读取 LightRAG 存储的 graphml 文件，避免触发引擎初始化。"""
from __future__ import annotations

import logging
import re
from pathlib import Path
from typing import Dict, List
from xml.etree import ElementTree as ET

from fastapi import APIRouter, Depends, HTTPException

from app.config import settings
from app.core.rag_engine import RagEngine, get_engine
from app.models.schemas import GraphData, GraphEdge, GraphNode

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/v1/graph", tags=["knowledge-graph"])


def _find_graph_file(working_dir: Path) -> Path | None:
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
        ns = {"g": "http://graphml.graphdrawing.org/xmlns"}
        for node_el in root.iter("{http://graphml.graphdrawing.org/xmlns}node"):
            nid = node_el.get("id")
            if not nid:
                continue
            label = nid
            for data_el in node_el.iter("{http://graphml.graphdrawing.org/xmlns}data"):
                if data_el.text:
                    label = data_el.text.strip() or label
            nodes[nid] = GraphNode(id=nid, label=label, type="entity")
        for edge_el in root.iter("{http://graphml.graphdrawing.org/xmlns}edge"):
            src = edge_el.get("source")
            tgt = edge_el.get("target")
            if not src or not tgt:
                continue
            label = "关联"
            for data_el in edge_el.iter("{http://graphml.graphdrawing.org/xmlns}data"):
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


@router.get("/{doc_id}", response_model=GraphData)
async def get_document_graph(
    doc_id: str,
    engine: RagEngine = Depends(get_engine),
) -> GraphData:
    """直接读取 LightRAG 存储的 graphml 文件构建图谱，不触发引擎初始化。"""
    # 引擎内存字典可能在重启后丢失，扫描 upload 目录恢复文件路径
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
    graph_file = _find_graph_file(working_dir)

    nodes: List[GraphNode] = []
    edges: List[GraphEdge] = []

    if graph_file:
        try:
            text = graph_file.read_text(encoding="utf-8", errors="ignore")
            if graph_file.suffix == ".json":
                import json
                data = json.loads(text)
                node_map: Dict[str, GraphNode] = {}
                for edge_raw in data.get("edges", []):
                    src = str(edge_raw.get("src", edge_raw.get("source", ""))).strip()
                    tgt = str(edge_raw.get("tgt", edge_raw.get("target", ""))).strip()
                    rel = str(edge_raw.get("relation", "关联")).strip()
                    if not src or not tgt:
                        continue
                    if src not in node_map:
                        node_map[src] = GraphNode(id=src, label=src, type="entity")
                    if tgt not in node_map:
                        node_map[tgt] = GraphNode(id=tgt, label=tgt, type="entity")
                    edges.append(GraphEdge(source=src, target=tgt, label=rel))
                nodes = list(node_map.values())
            else:
                nodes, edges = _parse_graphml(text)
        except Exception as e:
            logger.warning("read graph file %s failed: %s", graph_file, e)

    # 兜底：没有节点时生成文档中心节点
    if not nodes:
        filename = Path(file_path).stem if file_path else doc_id
        nodes = [
            GraphNode(id=filename, label=filename, type="document", size=30),
            GraphNode(id="RAG", label="RAG", type="system", size=20),
        ]
        edges = [GraphEdge(source=filename, target="RAG", label="已索引")]

    return GraphData(nodes=nodes, edges=edges)

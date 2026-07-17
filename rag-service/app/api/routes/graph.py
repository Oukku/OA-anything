"""知识图谱路由 - 基于 LightRAG 实体关系抽取生成图数据。"""
from __future__ import annotations

import json
import logging
from pathlib import Path
from typing import Dict, List, Tuple

from fastapi import APIRouter, Depends, HTTPException

from app.core.rag_engine import RagEngine, get_engine
from app.models.schemas import GraphData, GraphEdge, GraphNode

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/v1/graph", tags=["knowledge-graph"])


@router.get("/{doc_id}", response_model=GraphData)
async def get_document_graph(
    doc_id: str,
    engine: RagEngine = Depends(get_engine),
) -> GraphData:
    """基于 LightRAG 的 chunk_entity_relation_graph 抽取文档知识图谱。"""
    await engine.initialize()
    meta = engine._indexed_files.get(doc_id)
    if not meta:
        raise HTTPException(status_code=404, detail="document not found")

    nodes: Dict[str, GraphNode] = {}
    edges: List[GraphEdge] = []

    try:
        rag = engine.rag
        # 优先从 LightRAG 图存储读实体关系
        g = getattr(rag, "lightrag", None)
        if g is None:
            return GraphData(nodes=[], edges=[])

        graph_obj = getattr(g, "chunk_entity_relation_graph", None)
        if graph_obj is None:
            return GraphData(nodes=[], edges=[])

        # LightRAG 存储的是 json 文件，读取解析
        working_dir = Path(engine.config.working_dir_path)
        graph_file = working_dir / "chunk_entity_relation_graph.xml"
        if not graph_file.exists():
            graph_file = working_dir / "chunk_entity_relation_graph.json"

        if graph_file.exists():
            text = graph_file.read_text(encoding="utf-8", errors="ignore")
            try:
                data = json.loads(text) if graph_file.suffix == ".json" else _parse_xml_like(text)
            except Exception:
                data = _parse_xml_like(text)

            for edge_raw in data.get("edges", []):
                src = str(edge_raw.get("src", edge_raw.get("source", ""))).strip()
                tgt = str(edge_raw.get("tgt", edge_raw.get("target", ""))).strip()
                rel = str(edge_raw.get("relation", "关联")).strip()
                if not src or not tgt:
                    continue
                nodes[src] = GraphNode(id=src, label=src, type="entity")
                nodes[tgt] = GraphNode(id=tgt, label=tgt, type="entity")
                edges.append(GraphEdge(source=src, target=tgt, label=rel))

        # 如果没有任何边，兜底用文档名生成一个中心节点
        if not nodes:
            filename = Path(meta.get("file_path", doc_id)).stem
            nodes[filename] = GraphNode(id=filename, label=filename, type="document", size=30)
            nodes["RAG"] = GraphNode(id="RAG", label="RAG", type="system", size=20)
            edges.append(GraphEdge(source=filename, target="RAG", label="已索引"))

    except Exception as e:
        logger.exception("build graph failed for %s", doc_id)
        raise HTTPException(status_code=500, detail=f"graph build failed: {e}")

    return GraphData(nodes=list(nodes.values()), edges=edges)


def _parse_xml_like(text: str) -> Dict:
    """简单解析类 XML 的边数据，兜底用。"""
    edges = []
    import re
    for m in re.finditer(r'<edge[^>]*>(.*?)</edge>', text, re.S):
        inner = m.group(1)
        edge = {}
        for tag in ["source", "target", "src", "tgt", "relation"]:
            tm = re.search(rf'<{tag}[^>]*>(.*?)</{tag}>', inner, re.S)
            if tm:
                edge[tag] = tm.group(1).strip()
        edges.append(edge)
    return {"edges": edges}

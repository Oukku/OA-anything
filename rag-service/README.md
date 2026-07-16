# rag-service

把 [RAG-Anything](../RAG-Anything-main) 封装为 REST 服务，供 OA 后端调用。

## 架构

```
[Vue 前端] → [Spring Boot 后端] → [rag-service (FastAPI)] → [RAG-Anything / LightRAG / MinerU]
                                       ↑
                                   独立 Python 进程
```

## 启动

```bash
# 1. 创建 venv（一次性）
python -m venv rag-venv
.\rag-venv\Scripts\activate

# 2. 装依赖
pip install -r requirements.txt
# 注意：raganything 会拉 torch / lightrag / mineru，首次较慢

# 3. 配 LLM
cp .env.example .env
# 编辑 .env 填 OPENAI_API_KEY

# 4. 启服务
python -m app.main
# 或 uvicorn app.main:app --host 0.0.0.0 --port 8001 --reload
```

服务起来后访问 http://localhost:8001/docs 看 OpenAPI 文档。

## REST API 速查

| 方法 | 路径 | 用途 |
|---|---|---|
| GET  | `/health` | 健康检查 |
| POST | `/api/v1/documents/upload` | 上传 + 索引文档 |
| GET  | `/api/v1/documents` | 列出已索引文档 |
| GET  | `/api/v1/documents/{id}` | 文档详情 |
| DELETE | `/api/v1/documents/{id}` | 删除文档 |
| POST | `/api/v1/query` | 文本/VLM 增强查询 |
| POST | `/api/v1/query/multimodal` | 多模态查询 |
| GET  | `/api/v1/kb/{kb_id}/stats` | 知识库统计 |
| POST | `/api/v1/kb/{kb_id}/rebuild` | 重建知识库 |

## 嵌入到 OA 的功能映射

| OA 业务场景 | 用的 RAG 能力 |
|---|---|
| 企业知识库问答 | 上传 PDF/Word → 文本查询 |
| 多模态公告检索 | 上传带图表公告 → VLM 增强查询 |
| 智能文件管理 | 文件管理模块上传时同步索引 |
| 会议纪要助手 | 上传会议 PDF → 多模态查询 |
| 合同条款提取 | 上传合同 → 文本查询 |
| 工作日志 AI 总结 | 调用 RAG + LLM 生成周报 |

## Java 客户端

见 [`springboot/src/main/java/com/jlwl/rag/client/RagClient.java`](../springboot/src/main/java/com/jlwl/rag/client/RagClient.java)

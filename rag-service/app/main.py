"""FastAPI 入口。"""
import logging

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.routes import chat, config, documents, graph, health, kb, query
from app.config import settings

logging.basicConfig(
    level=settings.log_level,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)

app = FastAPI(
    title="OA RAG Service",
    description="把 RAG-Anything 封装成 REST 服务，供 OA 后端调用。",
    version="0.1.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

# CORS - OA 后端跨域调用
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生产环境收敛
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health.router)
app.include_router(documents.router)
app.include_router(query.router)
app.include_router(kb.router)
app.include_router(graph.router)
app.include_router(chat.router)
app.include_router(config.router)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host=settings.host,
        port=settings.port,
        reload=False,
    )

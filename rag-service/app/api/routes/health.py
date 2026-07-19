"""健康检查。"""
from fastapi import APIRouter, Depends

from app.config import settings
from app.core.rag_engine import RagEngine, get_engine
from app.models.schemas import HealthResponse

router = APIRouter(tags=["health"])


@router.get("/health", response_model=HealthResponse)
async def health(engine: RagEngine = Depends(get_engine)) -> HealthResponse:
    return HealthResponse(
        status="ok" if engine._initialized else "degraded",
        rag_initialized=engine._initialized,
        parser=settings.parser,
        llm_model=engine.get_config().get("llm_model", "unknown"),
        working_dir=str(settings.working_dir_path),
    )


@router.get("/")
async def root():
    return {
        "service": "rag-service",
        "version": "0.1.0",
        "docs": "/docs",
    }

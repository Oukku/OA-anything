"""应用配置 - 从 .env 读取。"""
from pathlib import Path
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # LLM
    openai_api_key: str = "sk-placeholder"
    openai_base_url: str = "https://api.openai.com/v1"
    llm_model: str = "gpt-4o-mini"
    vision_model: str = "gpt-4o"
    embedding_model: str = "text-embedding-3-large"
    embedding_dim: int = 3072

    # 解析
    parser: str = "mineru"
    parse_method: str = "auto"
    enable_image_processing: bool = True
    enable_table_processing: bool = True
    enable_equation_processing: bool = True

    # 存储
    working_dir: str = "./storage"
    upload_dir: str = "./data"
    max_upload_size_mb: int = 100

    # 服务
    host: str = "0.0.0.0"
    port: int = 8001
    log_level: str = "INFO"

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore",
    )

    @property
    def upload_dir_path(self) -> Path:
        p = Path(self.upload_dir)
        p.mkdir(parents=True, exist_ok=True)
        return p

    @property
    def working_dir_path(self) -> Path:
        p = Path(self.working_dir)
        p.mkdir(parents=True, exist_ok=True)
        return p


settings = Settings()

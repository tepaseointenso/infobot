from functools import lru_cache
from pathlib import Path
from typing import Literal

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    app_name: str = "Coodi RAG"
    # Comma separated list, e.g. "http://localhost:4200,https://coodi.cl"
    cors_origins: str = "http://localhost:4200,https://coodi.cl,https://www.coodi.cl"
    admin_api_key: str = Field(default="", description="Required in X-API-Key for document management")
    public_base_url: str = ""

    data_dir: Path = Path("./data")

    # "openai" works with any OpenAI-compatible API (OpenAI, Azure proxy, Ollama, vLLM, Groq...).
    # "fake" is deterministic and offline, used for tests and local development without keys.
    provider: Literal["openai", "fake"] = "openai"
    openai_api_key: str = ""
    openai_base_url: str | None = None
    chat_model: str = "gpt-4o-mini"
    vision_model: str = "gpt-4o-mini"
    embedding_model: str = "text-embedding-3-small"
    embedding_dim: int = 1536
    temperature: float = 0.2
    max_answer_tokens: int = 900

    # Empty -> embedded Qdrant persisted in data_dir/qdrant. Set to use a Qdrant server.
    qdrant_url: str | None = None
    qdrant_api_key: str | None = None
    collection: str = "coodi_knowledge"

    chunk_size: int = 1000
    chunk_overlap: int = 150
    top_k: int = 6
    min_score: float = 0.25

    describe_pdf_images: bool = True
    min_pdf_image_px: int = 120
    max_images_per_pdf: int = 60
    # Pages with less text than this are rendered and described by the vision model (scanned PDFs).
    ocr_min_chars: int = 40
    max_upload_mb: int = 50

    @property
    def media_dir(self) -> Path:
        return self.data_dir / "media"

    @property
    def cors_origin_list(self) -> list[str]:
        return [o.strip() for o in self.cors_origins.split(",") if o.strip()]


@lru_cache
def get_settings() -> Settings:
    return Settings()

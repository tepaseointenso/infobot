import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from .api import chat, documents
from .config import get_settings
from .llm import build_provider
from .rag import RagService
from .registry import DocumentRegistry
from .vectorstore import VectorStore

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")


def create_app() -> FastAPI:
    settings = get_settings()
    settings.media_dir.mkdir(parents=True, exist_ok=True)

    @asynccontextmanager
    async def lifespan(app: FastAPI):
        store = VectorStore(settings)
        registry = DocumentRegistry(settings.data_dir / "documents.db")
        app.state.rag = RagService(settings, build_provider(settings), store, registry)
        yield
        store.close()
        registry.close()

    app = FastAPI(
        title=settings.app_name,
        description="Backend RAG del asistente de robótica para profesores con el robot educativo Coodi.",
        version="1.0.0",
        lifespan=lifespan,
    )
    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.cors_origin_list,
        allow_methods=["GET", "POST", "DELETE", "OPTIONS"],
        allow_headers=["*"],
    )
    app.include_router(chat.router)
    app.include_router(documents.router)
    app.mount("/media", StaticFiles(directory=settings.media_dir), name="media")

    @app.get("/health", tags=["health"])
    def health() -> dict:
        rag: RagService = app.state.rag
        return {"status": "ok", "documents": len(rag.registry.list()), "chunks": rag.store.count()}

    return app


app = create_app()

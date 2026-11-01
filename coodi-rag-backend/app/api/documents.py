import hashlib
import mimetypes
import uuid
from pathlib import Path

from fastapi import APIRouter, BackgroundTasks, Depends, File, Form, HTTPException, UploadFile, status
from pydantic import BaseModel, Field

from ..config import Settings, get_settings
from ..deps import get_rag, require_admin
from ..ingestion import SUPPORTED_TYPES
from ..rag import RagService
from ..schemas import DocumentInfo, SearchResponse

router = APIRouter(prefix="/api/documents", tags=["documents"], dependencies=[Depends(require_admin)])

mimetypes.add_type("text/markdown", ".md")
mimetypes.add_type("image/webp", ".webp")


def _detect_mime(upload: UploadFile, head: bytes) -> str:
    if head.startswith(b"%PDF"):
        return "application/pdf"
    if head.startswith(b"\x89PNG"):
        return "image/png"
    if head.startswith(b"\xff\xd8\xff"):
        return "image/jpeg"
    if head[:4] == b"RIFF" and head[8:12] == b"WEBP":
        return "image/webp"
    if head[:4] == b"GIF8":
        return "image/gif"
    guessed, _ = mimetypes.guess_type(upload.filename or "")
    if guessed in ("text/plain", "text/markdown"):
        return guessed
    return upload.content_type or guessed or "application/octet-stream"


@router.post("", response_model=DocumentInfo, status_code=status.HTTP_202_ACCEPTED)
async def upload_document(
    background: BackgroundTasks,
    file: UploadFile = File(..., description="PDF, imagen (png/jpg/webp/gif), .txt o .md"),
    title: str | None = Form(default=None, max_length=200),
    tags: str | None = Form(default=None, description="Etiquetas separadas por coma, ej: 'basico,sensores'"),
    caption: str | None = Form(default=None, max_length=2000, description="Descripción opcional para imágenes"),
    rag: RagService = Depends(get_rag),
    settings: Settings = Depends(get_settings),
) -> DocumentInfo:
    max_bytes = settings.max_upload_mb * 1024 * 1024
    data = await file.read(max_bytes + 1)
    if len(data) > max_bytes:
        raise HTTPException(status.HTTP_413_REQUEST_ENTITY_TOO_LARGE, f"Máximo {settings.max_upload_mb} MB.")
    if not data:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "Archivo vacío.")

    mime = _detect_mime(file, data[:16])
    if mime not in SUPPORTED_TYPES:
        raise HTTPException(status.HTTP_415_UNSUPPORTED_MEDIA_TYPE, f"Tipo no soportado: {mime}")

    sha256 = hashlib.sha256(data).hexdigest()
    if existing := rag.registry.find_by_hash(sha256):
        raise HTTPException(
            status.HTTP_409_CONFLICT, {"message": "Este archivo ya fue cargado.", "document_id": existing.id}
        )

    filename = Path(file.filename or "archivo").name
    doc_id = uuid.uuid4().hex
    tag_list = sorted({t.strip().lower() for t in (tags or "").split(",") if t.strip()})
    doc = rag.registry.create(doc_id, title or Path(filename).stem, filename, mime, sha256, tag_list)
    background.add_task(rag.ingest, doc_id, data, filename, mime, doc.title, tag_list, caption)
    return doc


@router.get("", response_model=list[DocumentInfo])
def list_documents(rag: RagService = Depends(get_rag)) -> list[DocumentInfo]:
    return rag.registry.list()


@router.get("/{doc_id}", response_model=DocumentInfo)
def get_document(doc_id: str, rag: RagService = Depends(get_rag)) -> DocumentInfo:
    doc = rag.registry.get(doc_id)
    if not doc:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Documento no encontrado.")
    return doc


@router.delete("/{doc_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_document(doc_id: str, rag: RagService = Depends(get_rag)) -> None:
    if not rag.registry.get(doc_id):
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Documento no encontrado.")
    rag.delete_document(doc_id)


class SearchRequest(BaseModel):
    query: str = Field(min_length=1, max_length=2000)
    tags: list[str] | None = None


@router.post("/search", response_model=SearchResponse)
def search(req: SearchRequest, rag: RagService = Depends(get_rag)) -> SearchResponse:
    """Debug endpoint: shows which fragments the chat would retrieve for a query."""
    return SearchResponse(query=req.query, sources=rag.to_sources(rag.retrieve(req.query, req.tags)))

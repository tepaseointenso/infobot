import io
import logging
from pathlib import Path

import pymupdf
from PIL import Image

from ..config import Settings
from ..llm import AIProvider
from ..prompts import IMAGE_DESCRIPTION_PROMPT, PAGE_OCR_PROMPT
from .chunking import chunk_text, normalize_text
from .models import Chunk

log = logging.getLogger(__name__)

SUPPORTED_IMAGE_TYPES = {"image/png", "image/jpeg", "image/webp", "image/gif"}
SUPPORTED_TYPES = {"application/pdf", "text/plain", "text/markdown"} | SUPPORTED_IMAGE_TYPES
MAX_IMAGE_SIDE = 2000


def _save(media_dir: Path, doc_id: str, name: str, data: bytes) -> str:
    rel = f"{doc_id}/{name}"
    target = media_dir / rel
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(data)
    return rel


def normalize_image(data: bytes) -> tuple[bytes, str]:
    """Return (bytes, mime) downscaled and converted to a format every vision model accepts."""
    with Image.open(io.BytesIO(data)) as img:
        img.load()
        if max(img.size) > MAX_IMAGE_SIDE:
            img.thumbnail((MAX_IMAGE_SIDE, MAX_IMAGE_SIDE))
        if img.mode not in ("RGB", "RGBA", "L"):
            img = img.convert("RGBA" if "A" in img.getbands() else "RGB")
        buf = io.BytesIO()
        img.save(buf, format="PNG", optimize=True)
        return buf.getvalue(), "image/png"


def _describe(provider: AIProvider, image: bytes, mime: str, prompt: str) -> str:
    try:
        return normalize_text(provider.describe_image(image, mime, prompt))
    except Exception:
        log.exception("No se pudo describir la imagen")
        return ""


def _image_chunks(description: str, label: str, settings: Settings, page: int | None, path: str) -> list[Chunk]:
    return [
        Chunk(text=f"[{label}] {part}", kind="image", page=page, image_path=path)
        for part in chunk_text(description, settings.chunk_size, settings.chunk_overlap)
    ]


def _extract_pdf(
    data: bytes, doc_id: str, settings: Settings, provider: AIProvider
) -> tuple[list[Chunk], int]:
    chunks: list[Chunk] = []
    images_saved = 0
    seen_xrefs: set[int] = set()

    with pymupdf.open(stream=data, filetype="pdf") as doc:
        for page in doc:
            page_no = page.number + 1
            page_text = normalize_text(page.get_text("text"))

            if len(page_text) < settings.ocr_min_chars and settings.describe_pdf_images:
                png = page.get_pixmap(dpi=150).tobytes("png")
                path = _save(settings.media_dir, doc_id, f"page_{page_no}.png", png)
                images_saved += 1
                ocr = _describe(provider, png, "image/png", PAGE_OCR_PROMPT)
                page_text = normalize_text(f"{page_text}\n\n{ocr}")
                chunks += [
                    Chunk(text=t, kind="page_ocr", page=page_no, image_path=path)
                    for t in chunk_text(page_text, settings.chunk_size, settings.chunk_overlap)
                ]
                continue

            chunks += [
                Chunk(text=t, kind="text", page=page_no)
                for t in chunk_text(page_text, settings.chunk_size, settings.chunk_overlap)
            ]

            if not settings.describe_pdf_images:
                continue
            for img in page.get_images(full=True):
                xref, width, height = img[0], img[2], img[3]
                if xref in seen_xrefs or min(width, height) < settings.min_pdf_image_px:
                    continue
                if images_saved >= settings.max_images_per_pdf:
                    break
                seen_xrefs.add(xref)
                try:
                    pix = pymupdf.Pixmap(doc, xref)
                    if pix.n - pix.alpha >= 4:
                        pix = pymupdf.Pixmap(pymupdf.csRGB, pix)
                    png, mime = normalize_image(pix.tobytes("png"))
                except Exception:
                    log.exception("No se pudo extraer la imagen xref=%s", xref)
                    continue
                path = _save(settings.media_dir, doc_id, f"p{page_no}_x{xref}.png", png)
                images_saved += 1
                prompt = IMAGE_DESCRIPTION_PROMPT
                if page_text:
                    prompt += f"\n\nTexto de la página donde aparece la imagen (como contexto):\n{page_text[:800]}"
                description = _describe(provider, png, mime, prompt)
                if description:
                    chunks += _image_chunks(description, f"Imagen de la página {page_no}", settings, page_no, path)

    return chunks, images_saved


def _extract_image(
    data: bytes, filename: str, doc_id: str, settings: Settings, provider: AIProvider, caption: str | None
) -> tuple[list[Chunk], int]:
    png, mime = normalize_image(data)
    path = _save(settings.media_dir, doc_id, f"{Path(filename).stem[:60] or 'imagen'}.png", png)
    prompt = IMAGE_DESCRIPTION_PROMPT
    if caption:
        prompt += f"\n\nDescripción entregada por quien subió la imagen: {caption}"
    description = _describe(provider, png, mime, prompt)
    full = normalize_text(f"{caption or ''}\n\n{description}")
    if not full:
        raise ValueError("No se pudo obtener una descripción de la imagen.")
    return _image_chunks(full, f"Imagen: {filename}", settings, None, path), 1


def extract_chunks(
    data: bytes,
    filename: str,
    mime: str,
    doc_id: str,
    settings: Settings,
    provider: AIProvider,
    caption: str | None = None,
) -> tuple[list[Chunk], int]:
    if mime == "application/pdf":
        return _extract_pdf(data, doc_id, settings, provider)
    if mime in SUPPORTED_IMAGE_TYPES:
        return _extract_image(data, filename, doc_id, settings, provider, caption)
    if mime in ("text/plain", "text/markdown"):
        text = data.decode("utf-8", errors="replace")
        return [Chunk(text=t, kind="text") for t in chunk_text(text, settings.chunk_size, settings.chunk_overlap)], 0
    raise ValueError(f"Tipo de archivo no soportado: {mime}")

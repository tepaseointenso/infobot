import base64
import binascii
import json
import logging
import shutil
from collections.abc import Iterator

from .config import Settings
from .ingestion import extract_chunks
from .ingestion.pipeline import normalize_image
from .llm import AIProvider, Message, image_data_url
from .prompts import CHAT_IMAGE_PROMPT, CONDENSE_PROMPT, CONTEXT_TEMPLATE, NO_CONTEXT_NOTE, SYSTEM_PROMPT
from .registry import DocumentRegistry
from .schemas import ChatRequest, ChatResponse, Source
from .vectorstore import Hit, VectorStore

log = logging.getLogger(__name__)

MAX_HISTORY_TURNS = 10
KIND_LABELS = {"text": "texto", "image": "imagen", "page_ocr": "página escaneada"}


def decode_image(value: str) -> bytes:
    if value.startswith("data:"):
        value = value.split(",", 1)[-1]
    try:
        return base64.b64decode(value, validate=True)
    except (binascii.Error, ValueError) as exc:
        raise ValueError("image_base64 no es base64 válido") from exc


class RagService:
    def __init__(self, settings: Settings, provider: AIProvider, store: VectorStore, registry: DocumentRegistry):
        self.s = settings
        self.provider = provider
        self.store = store
        self.registry = registry

    # ---------- ingestion ----------

    def ingest(
        self,
        doc_id: str,
        data: bytes,
        filename: str,
        mime: str,
        title: str,
        tags: list[str],
        caption: str | None = None,
    ) -> None:
        try:
            chunks, images = extract_chunks(data, filename, mime, doc_id, self.s, self.provider, caption)
            if not chunks:
                raise ValueError("No se pudo extraer contenido del archivo.")
            texts = [self._embedding_text(title, c.page, c.text) for c in chunks]
            vectors = self.provider.embed(texts)
            self.store.add(doc_id, title, filename, tags, chunks, vectors)
            self.registry.update(doc_id, status="ready", chunks=len(chunks), images=images, error=None)
            log.info("Documento %s (%s) indexado: %d fragmentos, %d imágenes", doc_id, filename, len(chunks), images)
        except Exception as exc:
            log.exception("Error indexando %s", filename)
            self.store.delete_document(doc_id)
            self.registry.update(doc_id, status="error", error=str(exc)[:500])

    @staticmethod
    def _embedding_text(title: str, page: int | None, text: str) -> str:
        header = f"Documento: {title}" + (f" (página {page})" if page else "")
        return f"{header}\n{text}"

    def delete_document(self, doc_id: str) -> None:
        self.store.delete_document(doc_id)
        shutil.rmtree(self.s.media_dir / doc_id, ignore_errors=True)
        self.registry.delete(doc_id)

    # ---------- retrieval ----------

    def retrieve(self, query: str, tags: list[str] | None = None) -> list[Hit]:
        vector = self.provider.embed([query])[0]
        return self.store.search(vector, self.s.top_k, self.s.min_score, tags)

    def _media_url(self, path: str | None) -> str | None:
        if not path:
            return None
        return f"{self.s.public_base_url.rstrip('/')}/media/{path}"

    def to_sources(self, hits: list[Hit]) -> list[Source]:
        return [
            Source(
                id=i,
                doc_id=h.doc_id,
                title=h.title,
                filename=h.filename,
                page=h.page,
                kind=h.kind,
                score=round(h.score, 4),
                snippet=h.text[:400],
                image_url=self._media_url(h.image_path),
            )
            for i, h in enumerate(hits, start=1)
        ]

    # ---------- generation ----------

    def _condense(self, req: ChatRequest) -> str:
        if not req.history:
            return req.message
        history = "\n".join(
            f"{'Profesor' if t.role == 'user' else 'Asistente'}: {t.content[:1000]}"
            for t in req.history[-6:]
        )
        try:
            rewritten = self.provider.chat(
                [{"role": "user", "content": CONDENSE_PROMPT.format(history=history, question=req.message)}],
                max_tokens=200,
            )
            return rewritten.strip().strip('"') or req.message
        except Exception:
            log.exception("No se pudo reescribir la pregunta")
            return req.message

    def _prepare(self, req: ChatRequest) -> tuple[list[Message], list[Source], str]:
        query = self._condense(req)

        image_url = None
        if req.image_base64:
            try:
                image, mime = normalize_image(decode_image(req.image_base64))
            except OSError as exc:
                raise ValueError("La imagen adjunta no es válida.") from exc
            image_url = image_data_url(image, mime)
            description = self.provider.describe_image(image, mime, CHAT_IMAGE_PROMPT)
            query = f"{query}\n{description}"

        hits = self.retrieve(query, req.tags)
        sources = self.to_sources(hits)
        context = "\n\n".join(
            f"[{s.id}] ({s.title}{f', pág. {s.page}' if s.page else ''}, {KIND_LABELS.get(s.kind, s.kind)})\n{h.text}"
            for s, h in zip(sources, hits)
        ) or NO_CONTEXT_NOTE

        messages: list[Message] = [{"role": "system", "content": SYSTEM_PROMPT}]
        messages += [{"role": t.role, "content": t.content} for t in req.history[-MAX_HISTORY_TURNS:]]
        user_text = CONTEXT_TEMPLATE.format(context=context, question=req.message)
        if image_url:
            messages.append(
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": user_text},
                        {"type": "image_url", "image_url": {"url": image_url}},
                    ],
                }
            )
        else:
            messages.append({"role": "user", "content": user_text})
        return messages, sources, query

    def answer(self, req: ChatRequest) -> ChatResponse:
        messages, sources, query = self._prepare(req)
        return ChatResponse(answer=self.provider.chat(messages), sources=sources, query=query)

    def answer_stream(self, req: ChatRequest) -> Iterator[str]:
        """Server-Sent Events: one `sources` event, many `token` events, then `done` (or `error`)."""
        messages, sources, query = self._prepare(req)
        yield _sse("sources", {"query": query, "sources": [s.model_dump() for s in sources]})
        try:
            for token in self.provider.chat_stream(messages):
                yield _sse("token", {"text": token})
        except Exception:
            log.exception("Error durante el streaming")
            yield _sse("error", {"detail": "Ocurrió un error generando la respuesta."})
            return
        yield _sse("done", {})


def _sse(event: str, data: dict) -> str:
    return f"event: {event}\ndata: {json.dumps(data, ensure_ascii=False)}\n\n"

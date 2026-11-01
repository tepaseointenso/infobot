import uuid
from dataclasses import dataclass

from qdrant_client import QdrantClient, models

from .config import Settings
from .ingestion import Chunk


@dataclass
class Hit:
    score: float
    text: str
    doc_id: str
    title: str
    filename: str
    kind: str
    page: int | None
    image_path: str | None


class VectorStore:
    def __init__(self, settings: Settings):
        if settings.qdrant_url:
            self.client = QdrantClient(url=settings.qdrant_url, api_key=settings.qdrant_api_key)
        else:
            path = settings.data_dir / "qdrant"
            path.mkdir(parents=True, exist_ok=True)
            self.client = QdrantClient(path=str(path))
        self.collection = settings.collection
        self.dim = settings.embedding_dim
        self._ensure_collection(remote=bool(settings.qdrant_url))

    def _ensure_collection(self, remote: bool) -> None:
        if self.client.collection_exists(self.collection):
            return
        self.client.create_collection(
            self.collection,
            vectors_config=models.VectorParams(size=self.dim, distance=models.Distance.COSINE),
        )
        if not remote:
            return
        for field in ("doc_id", "tags"):
            self.client.create_payload_index(self.collection, field, models.PayloadSchemaType.KEYWORD)

    def add(
        self, doc_id: str, title: str, filename: str, tags: list[str], chunks: list[Chunk], vectors: list[list[float]]
    ) -> None:
        points = [
            models.PointStruct(
                id=str(uuid.uuid4()),
                vector=vec,
                payload={
                    "doc_id": doc_id,
                    "title": title,
                    "filename": filename,
                    "tags": tags,
                    "chunk_index": i,
                    "kind": c.kind,
                    "page": c.page,
                    "image_path": c.image_path,
                    "text": c.text,
                },
            )
            for i, (c, vec) in enumerate(zip(chunks, vectors, strict=True))
        ]
        for i in range(0, len(points), 128):
            self.client.upsert(self.collection, points=points[i : i + 128])

    def search(self, vector: list[float], limit: int, min_score: float, tags: list[str] | None = None) -> list[Hit]:
        query_filter = None
        if tags:
            query_filter = models.Filter(must=[models.FieldCondition(key="tags", match=models.MatchAny(any=tags))])
        result = self.client.query_points(
            self.collection,
            query=vector,
            limit=limit,
            query_filter=query_filter,
            score_threshold=min_score,
            with_payload=True,
        )
        return [
            Hit(
                score=p.score,
                text=p.payload["text"],
                doc_id=p.payload["doc_id"],
                title=p.payload.get("title") or p.payload["filename"],
                filename=p.payload["filename"],
                kind=p.payload["kind"],
                page=p.payload.get("page"),
                image_path=p.payload.get("image_path"),
            )
            for p in result.points
        ]

    def delete_document(self, doc_id: str) -> None:
        self.client.delete(
            self.collection,
            points_selector=models.FilterSelector(
                filter=models.Filter(must=[models.FieldCondition(key="doc_id", match=models.MatchValue(value=doc_id))])
            ),
        )

    def count(self) -> int:
        return self.client.count(self.collection).count

    def close(self) -> None:
        self.client.close()

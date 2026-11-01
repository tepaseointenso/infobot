import json
import sqlite3
import threading
from datetime import datetime, timezone
from pathlib import Path

from .schemas import DocumentInfo


class DocumentRegistry:
    """Small SQLite catalogue of ingested documents (the vectors live in Qdrant)."""

    def __init__(self, db_path: Path):
        db_path.parent.mkdir(parents=True, exist_ok=True)
        self._conn = sqlite3.connect(db_path, check_same_thread=False)
        self._conn.row_factory = sqlite3.Row
        self._lock = threading.Lock()
        with self._lock:
            self._conn.execute(
                """CREATE TABLE IF NOT EXISTS documents (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    filename TEXT NOT NULL,
                    mime TEXT NOT NULL,
                    sha256 TEXT NOT NULL,
                    tags TEXT NOT NULL DEFAULT '[]',
                    status TEXT NOT NULL,
                    error TEXT,
                    chunks INTEGER NOT NULL DEFAULT 0,
                    images INTEGER NOT NULL DEFAULT 0,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                )"""
            )
            self._conn.commit()

    @staticmethod
    def _now() -> str:
        return datetime.now(timezone.utc).isoformat()

    @staticmethod
    def _row(row: sqlite3.Row) -> DocumentInfo:
        data = dict(row)
        data["tags"] = json.loads(data["tags"])
        data.pop("sha256", None)
        return DocumentInfo(**data)

    def create(self, doc_id: str, title: str, filename: str, mime: str, sha256: str, tags: list[str]) -> DocumentInfo:
        now = self._now()
        with self._lock:
            self._conn.execute(
                "INSERT INTO documents (id, title, filename, mime, sha256, tags, status, created_at, updated_at)"
                " VALUES (?, ?, ?, ?, ?, ?, 'processing', ?, ?)",
                (doc_id, title, filename, mime, sha256, json.dumps(tags), now, now),
            )
            self._conn.commit()
        return self.get(doc_id)

    def update(self, doc_id: str, **fields) -> None:
        if not fields:
            return
        fields["updated_at"] = self._now()
        cols = ", ".join(f"{k} = ?" for k in fields)
        with self._lock:
            self._conn.execute(f"UPDATE documents SET {cols} WHERE id = ?", (*fields.values(), doc_id))
            self._conn.commit()

    def get(self, doc_id: str) -> DocumentInfo | None:
        with self._lock:
            row = self._conn.execute("SELECT * FROM documents WHERE id = ?", (doc_id,)).fetchone()
        return self._row(row) if row else None

    def find_by_hash(self, sha256: str) -> DocumentInfo | None:
        with self._lock:
            row = self._conn.execute(
                "SELECT * FROM documents WHERE sha256 = ? AND status != 'error'", (sha256,)
            ).fetchone()
        return self._row(row) if row else None

    def list(self) -> list[DocumentInfo]:
        with self._lock:
            rows = self._conn.execute("SELECT * FROM documents ORDER BY created_at DESC").fetchall()
        return [self._row(r) for r in rows]

    def delete(self, doc_id: str) -> None:
        with self._lock:
            self._conn.execute("DELETE FROM documents WHERE id = ?", (doc_id,))
            self._conn.commit()

    def close(self) -> None:
        self._conn.close()

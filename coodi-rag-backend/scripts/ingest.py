"""Bulk-load a folder of Coodi material (PDFs, images, .txt/.md) without going through the HTTP API.

Usage:
    python -m scripts.ingest ./material --tags manual,basico
    python -m scripts.ingest ./material/guia.pdf --title "Guía docente Coodi"

Stop the API server first when using the embedded Qdrant (it holds a lock on data/qdrant).
"""

import argparse
import hashlib
import mimetypes
import sys
import uuid
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from app.config import get_settings  # noqa: E402
from app.ingestion import SUPPORTED_TYPES  # noqa: E402
from app.llm import build_provider  # noqa: E402
from app.rag import RagService  # noqa: E402
from app.registry import DocumentRegistry  # noqa: E402
from app.vectorstore import VectorStore  # noqa: E402

mimetypes.add_type("text/markdown", ".md")
mimetypes.add_type("image/webp", ".webp")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("path", type=Path, help="Archivo o carpeta (se recorre recursivamente)")
    parser.add_argument("--tags", default="", help="Etiquetas separadas por coma")
    parser.add_argument("--title", default=None, help="Título (solo si path es un archivo)")
    args = parser.parse_args()

    settings = get_settings()
    settings.media_dir.mkdir(parents=True, exist_ok=True)
    store = VectorStore(settings)
    registry = DocumentRegistry(settings.data_dir / "documents.db")
    rag = RagService(settings, build_provider(settings), store, registry)
    tags = sorted({t.strip().lower() for t in args.tags.split(",") if t.strip()})

    files = [args.path] if args.path.is_file() else sorted(p for p in args.path.rglob("*") if p.is_file())
    failed = 0
    for path in files:
        mime, _ = mimetypes.guess_type(path.name)
        if mime not in SUPPORTED_TYPES:
            print(f"-  omitido (tipo no soportado): {path}")
            continue
        data = path.read_bytes()
        sha256 = hashlib.sha256(data).hexdigest()
        if existing := registry.find_by_hash(sha256):
            print(f"=  ya existe: {path} -> {existing.id}")
            continue
        doc_id = uuid.uuid4().hex
        title = args.title if args.title and args.path.is_file() else path.stem
        registry.create(doc_id, title, path.name, mime, sha256, tags)
        rag.ingest(doc_id, data, path.name, mime, title, tags)
        doc = registry.get(doc_id)
        if doc.status == "ready":
            print(f"+  {path}: {doc.chunks} fragmentos, {doc.images} imágenes -> {doc_id}")
        else:
            failed += 1
            print(f"!  {path}: {doc.error}")

    store.close()
    registry.close()
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())

import io
import sys
from pathlib import Path

import pymupdf
import pytest
from fastapi.testclient import TestClient
from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

ADMIN = {"X-API-Key": "test-key"}


def make_png(color=(200, 30, 30), size=(300, 300)) -> bytes:
    buf = io.BytesIO()
    Image.new("RGB", size, color).save(buf, format="PNG")
    return buf.getvalue()


@pytest.fixture
def sample_pdf() -> bytes:
    doc = pymupdf.open()
    p1 = doc.new_page()
    p1.insert_text(
        (72, 72),
        "Manual Coodi. El sensor ultrasonico de Coodi mide distancias entre 2 y 400 cm.\n"
        "Conecta el sensor ultrasonico al puerto D3 del robot Coodi.",
    )
    p1.insert_image(pymupdf.Rect(72, 200, 272, 400), stream=make_png())
    p2 = doc.new_page()
    p2.insert_text((72, 72), "Los motores de Coodi se controlan con el bloque avanzar y girar. Velocidad de 0 a 100.")
    doc.new_page()  # blank page: exercises the scanned-page (vision OCR) path
    data = doc.tobytes()
    doc.close()
    return data


@pytest.fixture
def client(tmp_path, monkeypatch):
    monkeypatch.setenv("PROVIDER", "fake")
    monkeypatch.setenv("DATA_DIR", str(tmp_path))
    monkeypatch.setenv("ADMIN_API_KEY", "test-key")
    monkeypatch.setenv("EMBEDDING_DIM", "256")
    monkeypatch.setenv("MIN_SCORE", "0.05")
    monkeypatch.setenv("PUBLIC_BASE_URL", "")

    from app.config import get_settings

    get_settings.cache_clear()
    from app.main import create_app

    with TestClient(create_app()) as c:
        yield c
    get_settings.cache_clear()

import base64
import json

from conftest import ADMIN, make_png


def upload(client, name, data, mime, **form):
    return client.post("/api/documents", headers=ADMIN, files={"file": (name, data, mime)}, data=form)


def test_pdf_ingestion_and_chat_with_sources(client, sample_pdf):
    r = upload(client, "manual.pdf", sample_pdf, "application/pdf", title="Manual Coodi", tags="Manual, basico")
    assert r.status_code == 202, r.text
    doc_id = r.json()["id"]

    doc = client.get(f"/api/documents/{doc_id}", headers=ADMIN).json()
    assert doc["status"] == "ready", doc
    assert doc["tags"] == ["basico", "manual"]
    assert doc["chunks"] >= 3
    assert doc["images"] == 2  # embedded image + rendered blank page

    r = client.post("/api/chat", json={"message": "¿En qué puerto conecto el sensor ultrasonico?"})
    assert r.status_code == 200, r.text
    body = r.json()
    assert "[1]" in body["answer"]
    top = body["sources"][0]
    assert top["title"] == "Manual Coodi"
    assert top["page"] == 1
    assert "D3" in top["snippet"]

    image_sources = [s for s in body["sources"] if s["image_url"]]
    assert image_sources
    assert client.get(image_sources[0]["image_url"]).status_code == 200

    r = client.post("/api/chat", json={"message": "sensor ultrasonico", "tags": ["otro"]})
    assert r.json()["sources"] == []
    assert "No encuentro" in r.json()["answer"]


def test_image_ingestion_with_caption(client):
    r = upload(client, "cableado.png", make_png((0, 0, 255)), "image/png", caption="Diagrama de cableado del buzzer")
    assert r.status_code == 202
    doc = client.get(f"/api/documents/{r.json()['id']}", headers=ADMIN).json()
    assert doc["status"] == "ready" and doc["images"] == 1

    r = client.post("/api/documents/search", headers=ADMIN, json={"query": "cableado del buzzer"})
    src = r.json()["sources"][0]
    assert src["kind"] == "image"
    assert src["image_url"].startswith("/media/")


def test_duplicate_and_unsupported(client, sample_pdf):
    assert upload(client, "a.pdf", sample_pdf, "application/pdf").status_code == 202
    r = upload(client, "b.pdf", sample_pdf, "application/pdf")
    assert r.status_code == 409
    assert upload(client, "x.zip", b"PK\x03\x04zipdata", "application/zip").status_code == 415


def test_admin_endpoints_require_key(client, sample_pdf):
    assert client.get("/api/documents").status_code == 401
    assert client.get("/api/documents", headers={"X-API-Key": "wrong"}).status_code == 401
    r = client.post("/api/documents", files={"file": ("a.pdf", sample_pdf, "application/pdf")})
    assert r.status_code == 401


def test_delete_document(client, sample_pdf):
    doc_id = upload(client, "manual.pdf", sample_pdf, "application/pdf").json()["id"]
    assert client.get("/health").json()["chunks"] > 0

    assert client.delete(f"/api/documents/{doc_id}", headers=ADMIN).status_code == 204
    assert client.get("/health").json() == {"status": "ok", "documents": 0, "chunks": 0}
    assert client.get(f"/api/documents/{doc_id}", headers=ADMIN).status_code == 404


def test_stream(client, sample_pdf):
    upload(client, "manual.pdf", sample_pdf, "application/pdf")
    r = client.post("/api/chat/stream", json={"message": "motores avanzar"})
    assert r.status_code == 200
    assert r.headers["content-type"].startswith("text/event-stream")
    events = [block.split("\n") for block in r.text.strip().split("\n\n")]
    names = [e[0].removeprefix("event: ") for e in events]
    assert names[0] == "sources" and names[-1] == "done" and "token" in names
    sources = json.loads(events[0][1].removeprefix("data: "))["sources"]
    assert sources[0]["page"] == 2


def test_chat_with_history_and_image(client, sample_pdf):
    upload(client, "manual.pdf", sample_pdf, "application/pdf")
    image = "data:image/png;base64," + base64.b64encode(make_png()).decode()
    r = client.post(
        "/api/chat",
        json={
            "message": "¿y cómo controlo los motores?",
            "history": [
                {"role": "user", "content": "Hola"},
                {"role": "assistant", "content": "Hola profe, ¿en qué te ayudo?"},
            ],
            "image_base64": image,
        },
    )
    assert r.status_code == 200, r.text
    assert r.json()["sources"]

    r = client.post("/api/chat", json={"message": "hola", "image_base64": "no-es-base64!!"})
    assert r.status_code == 400
    r = client.post("/api/chat/stream", json={"message": "hola", "image_base64": base64.b64encode(b"x").decode()})
    assert r.status_code == 400

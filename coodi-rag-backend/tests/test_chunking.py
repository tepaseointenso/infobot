from app.ingestion.chunking import chunk_text, normalize_text


def test_short_text_is_single_chunk():
    assert chunk_text("Hola  Coodi\n\n\n\nfin", 100, 10) == ["Hola Coodi\n\nfin"]


def test_chunks_respect_size_and_overlap():
    paragraphs = [f"Paso {i}: conecta el cable {i} al puerto del robot Coodi." for i in range(40)]
    chunks = chunk_text("\n\n".join(paragraphs), size=200, overlap=40)
    assert len(chunks) > 5
    assert all(len(c) <= 200 + 40 + 1 for c in chunks)
    joined = " ".join(chunks)
    assert all(p in joined for p in paragraphs)
    assert chunks[1].split(" ")[0] in chunks[0]


def test_normalize_joins_hyphenated_words():
    assert normalize_text("progra-\nmación") == "programación"


def test_empty():
    assert chunk_text("   \n ") == []

import re

_SEPARATORS = ["\n\n", "\n", ". ", " "]


def normalize_text(text: str) -> str:
    text = text.replace("\u00ad", "").replace("\r", "")
    text = re.sub(r"-\n(\w)", r"\1", text)
    text = re.sub(r"[ \t]+", " ", text)
    text = re.sub(r"\n{3,}", "\n\n", text)
    return text.strip()


def _split(text: str, size: int, seps: list[str]) -> list[str]:
    if len(text) <= size:
        return [text]
    if not seps:
        return [text[i : i + size] for i in range(0, len(text), size)]
    sep, rest = seps[0], seps[1:]
    pieces = text.split(sep)
    if len(pieces) == 1:
        return _split(text, size, rest)

    out: list[str] = []
    current = ""
    for piece in pieces:
        candidate = f"{current}{sep}{piece}" if current else piece
        if len(candidate) <= size:
            current = candidate
            continue
        if current:
            out.append(current)
        if len(piece) > size:
            out.extend(_split(piece, size, rest))
            current = ""
        else:
            current = piece
    if current:
        out.append(current)
    return out


def chunk_text(text: str, size: int = 1000, overlap: int = 150) -> list[str]:
    """Split on natural boundaries (paragraph > line > sentence > word) and add a tail overlap."""
    text = normalize_text(text)
    if not text:
        return []
    base = [p.strip() for p in _split(text, size, _SEPARATORS) if p.strip()]
    if overlap <= 0 or len(base) < 2:
        return base
    chunks = [base[0]]
    for prev, cur in zip(base, base[1:]):
        tail = prev[-overlap:]
        cut = tail.find(" ")
        if 0 <= cut < len(tail) - 1:
            tail = tail[cut + 1 :]
        chunks.append(f"{tail} {cur}".strip())
    return chunks

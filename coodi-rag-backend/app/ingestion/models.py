from dataclasses import dataclass
from typing import Literal

ChunkKind = Literal["text", "image", "page_ocr"]


@dataclass
class Chunk:
    text: str
    kind: ChunkKind
    page: int | None = None
    image_path: str | None = None

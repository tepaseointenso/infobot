from typing import Literal

from pydantic import BaseModel, Field


class ChatTurn(BaseModel):
    role: Literal["user", "assistant"]
    content: str = Field(max_length=8000)


class ChatRequest(BaseModel):
    message: str = Field(min_length=1, max_length=4000)
    history: list[ChatTurn] = Field(default_factory=list, max_length=40)
    # Optional photo from the teacher (e.g. their robot assembly or an error screen), base64 or data URL.
    image_base64: str | None = Field(default=None, max_length=12_000_000)
    tags: list[str] | None = Field(default=None, description="Restrict retrieval to documents with these tags")


class Source(BaseModel):
    id: int
    doc_id: str
    title: str
    filename: str
    page: int | None
    kind: str
    score: float
    snippet: str
    image_url: str | None


class ChatResponse(BaseModel):
    answer: str
    sources: list[Source]
    query: str


class DocumentInfo(BaseModel):
    id: str
    title: str
    filename: str
    mime: str
    tags: list[str]
    status: Literal["processing", "ready", "error"]
    error: str | None = None
    chunks: int = 0
    images: int = 0
    created_at: str
    updated_at: str


class SearchResponse(BaseModel):
    query: str
    sources: list[Source]

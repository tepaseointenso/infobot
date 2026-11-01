from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.responses import StreamingResponse

from ..deps import get_rag
from ..rag import RagService
from ..schemas import ChatRequest, ChatResponse

router = APIRouter(prefix="/api/chat", tags=["chat"])


def _bad_request(exc: Exception) -> HTTPException:
    return HTTPException(status.HTTP_400_BAD_REQUEST, str(exc))


@router.post("", response_model=ChatResponse)
def chat(req: ChatRequest, rag: RagService = Depends(get_rag)) -> ChatResponse:
    try:
        return rag.answer(req)
    except ValueError as exc:
        raise _bad_request(exc) from exc


@router.post("/stream")
def chat_stream(req: ChatRequest, rag: RagService = Depends(get_rag)) -> StreamingResponse:
    try:
        events = rag.answer_stream(req)
        first = next(events)
    except ValueError as exc:
        raise _bad_request(exc) from exc

    def body():
        yield first
        yield from events

    return StreamingResponse(
        body(),
        media_type="text/event-stream",
        headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no"},
    )

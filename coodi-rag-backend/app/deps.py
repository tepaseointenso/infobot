import secrets

from fastapi import Depends, Header, HTTPException, Request, status

from .config import Settings, get_settings
from .rag import RagService


def get_rag(request: Request) -> RagService:
    return request.app.state.rag


def require_admin(
    x_api_key: str | None = Header(default=None), settings: Settings = Depends(get_settings)
) -> None:
    if not settings.admin_api_key:
        raise HTTPException(status.HTTP_503_SERVICE_UNAVAILABLE, "ADMIN_API_KEY no está configurada en el servidor.")
    if not x_api_key or not secrets.compare_digest(x_api_key, settings.admin_api_key):
        raise HTTPException(status.HTTP_401_UNAUTHORIZED, "API key inválida.")

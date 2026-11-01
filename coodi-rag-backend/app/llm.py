import base64
import hashlib
import math
import re
from collections.abc import Iterator
from typing import Protocol

from .config import Settings

Message = dict


class AIProvider(Protocol):
    def embed(self, texts: list[str]) -> list[list[float]]: ...

    def describe_image(self, image: bytes, mime: str, prompt: str) -> str: ...

    def chat(self, messages: list[Message], max_tokens: int | None = None) -> str: ...

    def chat_stream(self, messages: list[Message]) -> Iterator[str]: ...


def image_data_url(image: bytes, mime: str) -> str:
    return f"data:{mime};base64,{base64.b64encode(image).decode()}"


class OpenAIProvider:
    def __init__(self, settings: Settings):
        from openai import OpenAI

        if not settings.openai_api_key and not settings.openai_base_url:
            raise RuntimeError("OPENAI_API_KEY no está configurada (o usa PROVIDER=fake para pruebas).")
        self.client = OpenAI(api_key=settings.openai_api_key or "not-needed", base_url=settings.openai_base_url)
        self.s = settings

    def embed(self, texts: list[str]) -> list[list[float]]:
        vectors: list[list[float]] = []
        for i in range(0, len(texts), 64):
            resp = self.client.embeddings.create(model=self.s.embedding_model, input=texts[i : i + 64])
            vectors.extend(d.embedding for d in sorted(resp.data, key=lambda d: d.index))
        return vectors

    def describe_image(self, image: bytes, mime: str, prompt: str) -> str:
        resp = self.client.chat.completions.create(
            model=self.s.vision_model,
            temperature=0,
            max_tokens=800,
            messages=[
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": prompt},
                        {"type": "image_url", "image_url": {"url": image_data_url(image, mime)}},
                    ],
                }
            ],
        )
        return (resp.choices[0].message.content or "").strip()

    def chat(self, messages: list[Message], max_tokens: int | None = None) -> str:
        resp = self.client.chat.completions.create(
            model=self.s.chat_model,
            temperature=self.s.temperature,
            max_tokens=max_tokens or self.s.max_answer_tokens,
            messages=messages,
        )
        return (resp.choices[0].message.content or "").strip()

    def chat_stream(self, messages: list[Message]) -> Iterator[str]:
        stream = self.client.chat.completions.create(
            model=self.s.chat_model,
            temperature=self.s.temperature,
            max_tokens=self.s.max_answer_tokens,
            messages=messages,
            stream=True,
        )
        for event in stream:
            if event.choices and event.choices[0].delta.content:
                yield event.choices[0].delta.content


class FakeProvider:
    """Offline, deterministic provider: hashed bag-of-words embeddings and template answers."""

    def __init__(self, settings: Settings):
        self.dim = settings.embedding_dim

    def _tokens(self, text: str) -> list[str]:
        return re.findall(r"\w+", text.lower())

    def embed(self, texts: list[str]) -> list[list[float]]:
        out = []
        for text in texts:
            vec = [0.0] * self.dim
            for tok in self._tokens(text):
                h = int(hashlib.md5(tok.encode()).hexdigest(), 16)
                vec[h % self.dim] += 1.0
            norm = math.sqrt(sum(v * v for v in vec)) or 1.0
            out.append([v / norm for v in vec])
        return out

    def describe_image(self, image: bytes, mime: str, prompt: str) -> str:
        return f"Imagen ({mime}, {len(image)} bytes) relacionada con el robot Coodi."

    def chat(self, messages: list[Message], max_tokens: int | None = None) -> str:
        last = messages[-1]["content"]
        if isinstance(last, list):
            last = " ".join(p.get("text", "") for p in last if p.get("type") == "text")
        if "Consulta autónoma:" in last:
            return last.split("Última pregunta:")[-1].split("Consulta autónoma:")[0].strip()
        refs = sorted(set(re.findall(r"^\[(\d+)\]", last, flags=re.M)), key=int)
        if not refs:
            return "No encuentro esa información en el material de Coodi."
        return "Respuesta de prueba basada en el material de Coodi " + " ".join(f"[{r}]" for r in refs) + "."

    def chat_stream(self, messages: list[Message]) -> Iterator[str]:
        for word in self.chat(messages).split(" "):
            yield word + " "


def build_provider(settings: Settings) -> AIProvider:
    if settings.provider == "fake":
        return FakeProvider(settings)
    return OpenAIProvider(settings)

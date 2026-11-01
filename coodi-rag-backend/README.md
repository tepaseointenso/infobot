# Coodi RAG Backend

Backend RAG (Retrieval-Augmented Generation) para el asistente de **coodi.cl**: un asistente de robótica
educativa para profesores que trabajan con el robot **Coodi**. Responde solo con el material que ustedes
carguen (manuales, guías docentes, planificaciones, fichas, diagramas, fotos), citando la fuente de cada
respuesta.

- **FastAPI** (Python 3.12), API REST + streaming (Server-Sent Events) para el frontend Angular.
- **PDF**: extrae el texto por página, las **imágenes incrustadas** (diagramas, fotos del robot, capturas
  de bloques) y las **páginas escaneadas** sin texto, que se transcriben con un modelo de visión.
- **Imágenes** (png/jpg/webp/gif): se describen con un modelo de visión (textos visibles, componentes,
  pines, colores de cables, bloques de código) y esa descripción se indexa. Se puede agregar una
  descripción manual (`caption`).
- **.txt / .md** también se aceptan.
- **Qdrant** como base vectorial: embebido (sin servidores extra) o servidor Qdrant en producción.
- Proveedor de IA **compatible con OpenAI** (OpenAI, Ollama, vLLM, Groq, etc.) configurable por variables
  de entorno. Proveedor `fake` sin conexión para pruebas.
- Las respuestas incluyen las **fuentes** (documento, página, fragmento y URL de la imagen) para que el
  frontend muestre referencias y miniaturas.
- El profesor puede **adjuntar una foto** a su pregunta (p.ej. cómo armó el robot o un error en pantalla).

```
coodi-rag-backend/
├── app/
│   ├── main.py            # App FastAPI, CORS, /media, /health
│   ├── config.py          # Configuración (variables de entorno / .env)
│   ├── prompts.py         # Prompt de sistema del asistente Coodi y prompts de visión
│   ├── rag.py             # Ingesta, recuperación y generación
│   ├── llm.py             # Proveedores de IA (OpenAI-compatible y fake)
│   ├── vectorstore.py     # Qdrant
│   ├── registry.py        # Catálogo SQLite de documentos
│   ├── ingestion/         # Extracción de PDF/imágenes y chunking
│   └── api/               # Endpoints /api/chat y /api/documents
├── scripts/ingest.py      # Carga masiva de una carpeta
├── examples/angular/      # Servicio Angular listo para copiar
└── tests/
```

## Puesta en marcha

```bash
cd coodi-rag-backend
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements-dev.txt
cp .env.example .env        # completa OPENAI_API_KEY y ADMIN_API_KEY
uvicorn app.main:app --reload --port 8000
```

Documentación interactiva (Swagger): http://localhost:8000/docs

Con Docker (incluye servidor Qdrant):

```bash
cp .env.example .env
docker compose up -d --build
```

## Cargar el contenido de Coodi

Por API (requiere `X-API-Key: <ADMIN_API_KEY>`). La ingesta corre en segundo plano: la respuesta es
`202` con `status: "processing"` y luego pasa a `ready` (o `error`, con el detalle).

```bash
# PDF con título y etiquetas
curl -H "X-API-Key: $ADMIN_API_KEY" \
  -F file=@"Manual Coodi.pdf" -F title="Manual del robot Coodi" -F tags="manual,hardware" \
  http://localhost:8000/api/documents

# Imagen con descripción opcional
curl -H "X-API-Key: $ADMIN_API_KEY" \
  -F file=@cableado-sensor.png -F caption="Conexión del sensor ultrasónico al puerto D3" -F tags=hardware \
  http://localhost:8000/api/documents

# Ver estado / listar / borrar
curl -H "X-API-Key: $ADMIN_API_KEY" http://localhost:8000/api/documents
curl -H "X-API-Key: $ADMIN_API_KEY" -X DELETE http://localhost:8000/api/documents/<id>

# Probar qué fragmentos recupera una pregunta (útil para afinar el contenido)
curl -H "X-API-Key: $ADMIN_API_KEY" -H "Content-Type: application/json" \
  -d '{"query":"¿cómo conecto el sensor de distancia?"}' http://localhost:8000/api/documents/search
```

Carga masiva de una carpeta (con el servidor detenido si usan Qdrant embebido):

```bash
python -m scripts.ingest ./material-coodi --tags curso-basico
```

Los archivos duplicados (mismo contenido) se detectan y no se vuelven a indexar.

**Recomendaciones de contenido**: PDFs con texto seleccionable dan mejores resultados que escaneos; usen
títulos descriptivos; separen por etiquetas (`basico`, `avanzado`, `planificaciones`, `hardware`...) si
quieren filtrar por nivel o curso desde el frontend.

## API para el frontend Angular

| Método | Ruta                    | Descripción                                   |
|--------|-------------------------|-----------------------------------------------|
| POST   | `/api/chat`             | Respuesta completa con fuentes                |
| POST   | `/api/chat/stream`      | Respuesta en streaming (SSE)                  |
| GET    | `/media/...`            | Imágenes de las fuentes                       |
| GET    | `/health`               | Estado y número de documentos/fragmentos      |

Request:

```json
{
  "message": "¿Cómo hago que Coodi avance y se detenga frente a un obstáculo?",
  "history": [
    {"role": "user", "content": "Hola"},
    {"role": "assistant", "content": "¡Hola profe! ¿En qué te ayudo?"}
  ],
  "image_base64": null,
  "tags": null
}
```

Response de `/api/chat`:

```json
{
  "answer": "Usa el sensor ultrasónico conectado al puerto D3 [1] ...",
  "query": "¿Cómo hago que Coodi avance y se detenga frente a un obstáculo?",
  "sources": [
    {
      "id": 1, "doc_id": "…", "title": "Manual del robot Coodi", "filename": "manual.pdf",
      "page": 12, "kind": "text", "score": 0.83, "snippet": "…", "image_url": null
    }
  ]
}
```

Las citas `[n]` del texto corresponden a `sources[n-1]`. `kind` puede ser `text`, `image` (descripción de
una imagen o diagrama) o `page_ocr` (página escaneada); en los dos últimos `image_url` apunta a la imagen.

`/api/chat/stream` emite eventos `sources` (una vez, al inicio), `token` (texto incremental), `done` o
`error`. El historial lo mantiene el frontend y se envía en cada request (el backend no guarda sesiones).

En [`examples/angular/coodi-chat.service.ts`](examples/angular/coodi-chat.service.ts) hay un servicio
Angular listo para copiar, con `ask()` (HTTP normal) y `askStream()` (streaming), más un ejemplo de uso.
Recuerden agregar el dominio del frontend a `CORS_ORIGINS` y configurar `PUBLIC_BASE_URL` con la URL
pública del backend para que las imágenes de las fuentes carguen.

## Personalizar el asistente

- `app/prompts.py`: personalidad, reglas y alcance del asistente (solo Coodi, robótica educativa y
  docencia; responde en español; no inventa; cita fuentes).
- `TOP_K` / `MIN_SCORE`: cuántos fragmentos se usan y el umbral de relevancia. Si responde "no encuentro"
  con contenido que sí existe, bajen `MIN_SCORE`; si mezcla temas, súbanlo.
- `CHUNK_SIZE` / `CHUNK_OVERLAP`: tamaño de los fragmentos. Si cambian `EMBEDDING_MODEL` a uno con otra
  dimensión, ajusten `EMBEDDING_DIM`, cambien `COLLECTION` y vuelvan a cargar el material.

## Producción

- `ADMIN_API_KEY` larga y secreta; los endpoints de documentos no funcionan sin ella. Nunca la incluyan
  en el frontend Angular (la carga de contenido debe hacerse desde un panel interno o por script).
- `/api/chat` es público para el frontend: pongan el backend detrás de un proxy (nginx, Cloud Run, etc.)
  con HTTPS y límite de solicitudes por IP para controlar costos del modelo.
- Respaldar el volumen de datos (`/data`: catálogo SQLite e imágenes) y el de Qdrant.

## Tests

```bash
pytest
```

Los tests usan el proveedor `fake`, por lo que no requieren API key ni conexión.

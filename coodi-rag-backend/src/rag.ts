import fs from "node:fs/promises";
import path from "node:path";
import type { Config } from "./config.js";
import { extractChunks, normalizeImage } from "./ingestion/pipeline.js";
import { type AIProvider, type Message, imageDataUrl } from "./llm.js";
import { CHAT_IMAGE_PROMPT, NO_CONTEXT_NOTE, SYSTEM_PROMPT, condensePrompt, contextMessage } from "./prompts.js";
import type { DocumentRegistry } from "./store/registry.js";
import type { Hit, VectorStore } from "./store/vectorstore.js";

const MAX_HISTORY_TURNS = 10;
const KIND_LABELS: Record<string, string> = { text: "texto", image: "imagen", page_ocr: "página escaneada" };

export class BadRequestError extends Error {}

export interface ChatTurn {
  role: "user" | "assistant";
  content: string;
}

export interface ChatRequest {
  message: string;
  history?: ChatTurn[];
  /** Optional photo from the teacher (e.g. their robot assembly or an error screen), base64 or data URL. */
  image_base64?: string | null;
  tags?: string[] | null;
}

export interface Source {
  id: number;
  doc_id: string;
  title: string;
  filename: string;
  page: number | null;
  kind: string;
  score: number;
  snippet: string;
  image_url: string | null;
}

export interface ChatResponse {
  answer: string;
  sources: Source[];
  query: string;
}

export type StreamEvent =
  | { event: "sources"; data: { query: string; sources: Source[] } }
  | { event: "token"; data: { text: string } }
  | { event: "done"; data: Record<string, never> }
  | { event: "error"; data: { detail: string } };

export interface IngestInput {
  docId: string;
  data: Buffer;
  filename: string;
  mime: string;
  title: string;
  tags: string[];
  caption?: string;
}

function decodeImage(value: string): Buffer {
  const b64 = value.startsWith("data:") ? value.slice(value.indexOf(",") + 1) : value;
  if (!/^[A-Za-z0-9+/]+={0,2}$/.test(b64.replace(/\s/g, ""))) {
    throw new BadRequestError("image_base64 no es base64 válido.");
  }
  return Buffer.from(b64, "base64");
}

export class RagService {
  private jobs = new Set<Promise<void>>();

  constructor(
    readonly cfg: Config,
    readonly provider: AIProvider,
    readonly store: VectorStore,
    readonly registry: DocumentRegistry,
  ) {}

  // ---------- ingestion ----------

  /** Runs ingestion in the background; the document status moves from "processing" to "ready"/"error". */
  enqueue(input: IngestInput): void {
    const job = this.ingest(input).finally(() => this.jobs.delete(job));
    this.jobs.add(job);
  }

  /** Resolves when every background ingestion has finished. */
  async idle(): Promise<void> {
    while (this.jobs.size) await Promise.all(this.jobs);
  }

  async ingest({ docId, data, filename, mime, title, tags, caption }: IngestInput): Promise<void> {
    try {
      const { chunks, images } = await extractChunks(data, filename, mime, docId, this.cfg, this.provider, caption);
      if (!chunks.length) throw new Error("No se pudo extraer contenido del archivo.");
      const vectors = await this.provider.embed(
        chunks.map((c) => `Documento: ${title}${c.page ? ` (página ${c.page})` : ""}\n${c.text}`),
      );
      await this.store.add({ docId, title, filename, tags }, chunks, vectors);
      this.registry.update(docId, { status: "ready", chunks: chunks.length, images, error: null });
      console.info(`Documento ${docId} (${filename}) indexado: ${chunks.length} fragmentos, ${images} imágenes`);
    } catch (err) {
      console.error(`Error indexando ${filename}:`, err);
      await this.store.deleteDocument(docId).catch(() => {});
      this.registry.update(docId, { status: "error", error: String((err as Error).message ?? err).slice(0, 500) });
    }
  }

  async deleteDocument(docId: string): Promise<void> {
    await this.store.deleteDocument(docId);
    await fs.rm(path.join(this.cfg.mediaDir, docId), { recursive: true, force: true });
    this.registry.delete(docId);
  }

  // ---------- retrieval ----------

  async retrieve(query: string, tags?: string[] | null): Promise<Hit[]> {
    const [vector] = await this.provider.embed([query]);
    return this.store.search(vector, this.cfg.TOP_K, this.cfg.MIN_SCORE, tags ?? undefined);
  }

  toSources(hits: Hit[]): Source[] {
    const base = this.cfg.PUBLIC_BASE_URL.replace(/\/+$/, "");
    return hits.map((h, i) => ({
      id: i + 1,
      doc_id: h.doc_id,
      title: h.title || h.filename,
      filename: h.filename,
      page: h.page,
      kind: h.kind,
      score: Math.round(h.score * 1e4) / 1e4,
      snippet: h.text.slice(0, 400),
      image_url: h.image_path ? `${base}/media/${encodeURI(h.image_path)}` : null,
    }));
  }

  // ---------- generation ----------

  private async condense(req: ChatRequest): Promise<string> {
    if (!req.history?.length) return req.message;
    const history = req.history
      .slice(-6)
      .map((t) => `${t.role === "user" ? "Profesor" : "Asistente"}: ${t.content.slice(0, 1000)}`)
      .join("\n");
    try {
      const rewritten = await this.provider.chat([{ role: "user", content: condensePrompt(history, req.message) }], 200);
      return rewritten.trim().replace(/^"|"$/g, "") || req.message;
    } catch (err) {
      console.error("No se pudo reescribir la pregunta:", err);
      return req.message;
    }
  }

  private async prepare(req: ChatRequest): Promise<{ messages: Message[]; sources: Source[]; query: string }> {
    let query = await this.condense(req);

    let imageUrl: string | null = null;
    if (req.image_base64) {
      let image: Buffer;
      try {
        image = await normalizeImage(decodeImage(req.image_base64));
      } catch (err) {
        if (err instanceof BadRequestError) throw err;
        throw new BadRequestError("La imagen adjunta no es válida.");
      }
      imageUrl = imageDataUrl(image, "image/png");
      query = `${query}\n${await this.provider.describeImage(image, "image/png", CHAT_IMAGE_PROMPT)}`;
    }

    const hits = await this.retrieve(query, req.tags);
    const sources = this.toSources(hits);
    const context =
      sources
        .map((s, i) => {
          const page = s.page ? `, pág. ${s.page}` : "";
          return `[${s.id}] (${s.title}${page}, ${KIND_LABELS[s.kind] ?? s.kind})\n${hits[i].text}`;
        })
        .join("\n\n") || NO_CONTEXT_NOTE;

    const userText = contextMessage(context, req.message);
    const messages: Message[] = [
      { role: "system", content: SYSTEM_PROMPT },
      ...(req.history ?? []).slice(-MAX_HISTORY_TURNS).map((t): Message => ({ role: t.role, content: t.content })),
      imageUrl
        ? {
            role: "user",
            content: [
              { type: "text", text: userText },
              { type: "image_url", image_url: { url: imageUrl } },
            ],
          }
        : { role: "user", content: userText },
    ];
    return { messages, sources, query };
  }

  async answer(req: ChatRequest): Promise<ChatResponse> {
    const { messages, sources, query } = await this.prepare(req);
    return { answer: await this.provider.chat(messages), sources, query };
  }

  /** Retrieval runs before the first event, so BadRequestError is thrown before any byte is sent. */
  async answerStream(req: ChatRequest): Promise<AsyncGenerator<StreamEvent>> {
    const { messages, sources, query } = await this.prepare(req);
    const provider = this.provider;
    return (async function* () {
      yield { event: "sources", data: { query, sources } } as StreamEvent;
      try {
        for await (const text of provider.chatStream(messages)) yield { event: "token", data: { text } };
      } catch (err) {
        console.error("Error durante el streaming:", err);
        yield { event: "error", data: { detail: "Ocurrió un error generando la respuesta." } };
        return;
      }
      yield { event: "done", data: {} };
    })();
  }
}

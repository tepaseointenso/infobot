import { randomUUID } from "node:crypto";
import { QdrantClient } from "@qdrant/js-client-rest";
import type Database from "better-sqlite3";
import type { Config } from "../config.js";
import type { Chunk, ChunkKind } from "../ingestion/pipeline.js";

export interface ChunkPayload {
  doc_id: string;
  title: string;
  filename: string;
  tags: string[];
  chunk_index: number;
  kind: ChunkKind;
  page: number | null;
  image_path: string | null;
  text: string;
}

export interface Hit extends ChunkPayload {
  score: number;
}

export interface DocMeta {
  docId: string;
  title: string;
  filename: string;
  tags: string[];
}

export interface VectorStore {
  add(meta: DocMeta, chunks: Chunk[], vectors: number[][]): Promise<void>;
  search(vector: number[], limit: number, minScore: number, tags?: string[]): Promise<Hit[]>;
  deleteDocument(docId: string): Promise<void>;
  count(): Promise<number>;
}

const toPayloads = (meta: DocMeta, chunks: Chunk[]): ChunkPayload[] =>
  chunks.map((c, i) => ({
    doc_id: meta.docId,
    title: meta.title,
    filename: meta.filename,
    tags: meta.tags,
    chunk_index: i,
    kind: c.kind,
    page: c.page,
    image_path: c.imagePath,
    text: c.text,
  }));

/**
 * Exact (brute-force) cosine search over vectors kept in SQLite and cached in memory.
 * Comfortable up to tens of thousands of chunks; beyond that use Qdrant (QDRANT_URL).
 */
export class LocalVectorStore implements VectorStore {
  private items: { payload: ChunkPayload; vec: Float32Array; norm: number }[] = [];

  constructor(
    private db: Database.Database,
    private dim: number,
  ) {
    db.exec(`CREATE TABLE IF NOT EXISTS chunks (
      id TEXT PRIMARY KEY,
      doc_id TEXT NOT NULL,
      payload TEXT NOT NULL,
      vector BLOB NOT NULL
    );
    CREATE INDEX IF NOT EXISTS chunks_doc ON chunks(doc_id);`);
    const rows = db.prepare("SELECT payload, vector FROM chunks").all() as { payload: string; vector: Buffer }[];
    for (const r of rows) {
      const vec = new Float32Array(r.vector.buffer, r.vector.byteOffset, r.vector.byteLength / 4);
      if (vec.length !== dim) {
        throw new Error(`EMBEDDING_DIM=${dim} no coincide con los vectores guardados (${vec.length}).`);
      }
      this.items.push({ payload: JSON.parse(r.payload), vec: Float32Array.from(vec), norm: norm(vec) });
    }
  }

  async add(meta: DocMeta, chunks: Chunk[], vectors: number[][]) {
    const payloads = toPayloads(meta, chunks);
    const insert = this.db.prepare("INSERT INTO chunks (id, doc_id, payload, vector) VALUES (?, ?, ?, ?)");
    const fresh = payloads.map((payload, i) => {
      if (vectors[i].length !== this.dim) throw new Error(`El embedding tiene ${vectors[i].length} dimensiones, se esperaban ${this.dim}.`);
      const vec = Float32Array.from(vectors[i]);
      return { payload, vec, norm: norm(vec) };
    });
    this.db.transaction(() => {
      for (const item of fresh) {
        insert.run(randomUUID(), meta.docId, JSON.stringify(item.payload), Buffer.from(item.vec.buffer));
      }
    })();
    this.items.push(...fresh);
  }

  async search(vector: number[], limit: number, minScore: number, tags?: string[]) {
    const q = Float32Array.from(vector);
    const qn = norm(q) || 1;
    const hits: Hit[] = [];
    for (const { payload, vec, norm: n } of this.items) {
      if (tags?.length && !payload.tags.some((t) => tags.includes(t))) continue;
      let dot = 0;
      for (let i = 0; i < vec.length; i++) dot += vec[i] * q[i];
      const score = dot / (qn * (n || 1));
      if (score >= minScore) hits.push({ ...payload, score });
    }
    return hits.sort((a, b) => b.score - a.score).slice(0, limit);
  }

  async deleteDocument(docId: string) {
    this.db.prepare("DELETE FROM chunks WHERE doc_id = ?").run(docId);
    this.items = this.items.filter((i) => i.payload.doc_id !== docId);
  }

  async count() {
    return this.items.length;
  }
}

function norm(v: Float32Array): number {
  let s = 0;
  for (let i = 0; i < v.length; i++) s += v[i] * v[i];
  return Math.sqrt(s);
}

export class QdrantVectorStore implements VectorStore {
  private client: QdrantClient;
  private ready?: Promise<void>;

  constructor(private cfg: Config) {
    this.client = new QdrantClient({ url: cfg.QDRANT_URL!, apiKey: cfg.QDRANT_API_KEY, checkCompatibility: false });
  }

  /** Lazily creates the collection; retried on the next call if Qdrant was unreachable. */
  private init(): Promise<void> {
    this.ready ??= this.ensureCollection().catch((err) => {
      this.ready = undefined;
      throw err;
    });
    return this.ready;
  }

  private async ensureCollection() {
    const { exists } = await this.client.collectionExists(this.cfg.COLLECTION);
    if (exists) return;
    await this.client.createCollection(this.cfg.COLLECTION, {
      vectors: { size: this.cfg.EMBEDDING_DIM, distance: "Cosine" },
    });
    for (const field_name of ["doc_id", "tags"]) {
      await this.client.createPayloadIndex(this.cfg.COLLECTION, { field_name, field_schema: "keyword", wait: true });
    }
  }

  async add(meta: DocMeta, chunks: Chunk[], vectors: number[][]) {
    await this.init();
    const points = toPayloads(meta, chunks).map((payload, i) => ({
      id: randomUUID(),
      vector: vectors[i],
      payload: { ...payload },
    }));
    for (let i = 0; i < points.length; i += 128) {
      await this.client.upsert(this.cfg.COLLECTION, { wait: true, points: points.slice(i, i + 128) });
    }
  }

  async search(vector: number[], limit: number, minScore: number, tags?: string[]) {
    await this.init();
    const res = await this.client.query(this.cfg.COLLECTION, {
      query: vector,
      limit,
      score_threshold: minScore,
      with_payload: true,
      filter: tags?.length ? { must: [{ key: "tags", match: { any: tags } }] } : undefined,
    });
    return res.points.map((p) => ({ ...(p.payload as unknown as ChunkPayload), score: p.score }));
  }

  async deleteDocument(docId: string) {
    await this.init();
    await this.client.delete(this.cfg.COLLECTION, {
      wait: true,
      filter: { must: [{ key: "doc_id", match: { value: docId } }] },
    });
  }

  async count() {
    await this.init();
    return (await this.client.count(this.cfg.COLLECTION, { exact: true })).count;
  }
}

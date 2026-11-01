import type Database from "better-sqlite3";

export type DocumentStatus = "processing" | "ready" | "error";

export interface DocumentInfo {
  id: string;
  title: string;
  filename: string;
  mime: string;
  tags: string[];
  status: DocumentStatus;
  error: string | null;
  chunks: number;
  images: number;
  created_at: string;
  updated_at: string;
}

type Row = Omit<DocumentInfo, "tags"> & { tags: string; sha256: string };

/** Catalogue of ingested documents (vectors live in the vector store). */
export class DocumentRegistry {
  constructor(private db: Database.Database) {
    db.exec(`CREATE TABLE IF NOT EXISTS documents (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      filename TEXT NOT NULL,
      mime TEXT NOT NULL,
      sha256 TEXT NOT NULL,
      tags TEXT NOT NULL DEFAULT '[]',
      status TEXT NOT NULL,
      error TEXT,
      chunks INTEGER NOT NULL DEFAULT 0,
      images INTEGER NOT NULL DEFAULT 0,
      created_at TEXT NOT NULL,
      updated_at TEXT NOT NULL
    )`);
    // Documents left half-indexed by a crash or restart can never finish.
    db.prepare(
      "UPDATE documents SET status = 'error', error = 'Procesamiento interrumpido; vuelve a subir el archivo.' WHERE status = 'processing'",
    ).run();
  }

  private static toInfo(row: Row | undefined): DocumentInfo | null {
    if (!row) return null;
    const { sha256: _sha, tags, ...rest } = row;
    return { ...rest, tags: JSON.parse(tags) };
  }

  create(doc: { id: string; title: string; filename: string; mime: string; sha256: string; tags: string[] }) {
    const now = new Date().toISOString();
    this.db
      .prepare(
        `INSERT INTO documents (id, title, filename, mime, sha256, tags, status, created_at, updated_at)
         VALUES (?, ?, ?, ?, ?, ?, 'processing', ?, ?)`,
      )
      .run(doc.id, doc.title, doc.filename, doc.mime, doc.sha256, JSON.stringify(doc.tags), now, now);
    return this.get(doc.id)!;
  }

  update(id: string, fields: Partial<Pick<DocumentInfo, "status" | "error" | "chunks" | "images">>) {
    const entries = Object.entries(fields);
    if (!entries.length) return;
    const cols = entries.map(([k]) => `${k} = ?`).join(", ");
    this.db
      .prepare(`UPDATE documents SET ${cols}, updated_at = ? WHERE id = ?`)
      .run(...entries.map(([, v]) => v), new Date().toISOString(), id);
  }

  get(id: string): DocumentInfo | null {
    return DocumentRegistry.toInfo(this.db.prepare("SELECT * FROM documents WHERE id = ?").get(id) as Row);
  }

  findByHash(sha256: string): DocumentInfo | null {
    return DocumentRegistry.toInfo(
      this.db.prepare("SELECT * FROM documents WHERE sha256 = ? AND status != 'error'").get(sha256) as Row,
    );
  }

  list(): DocumentInfo[] {
    const rows = this.db.prepare("SELECT * FROM documents ORDER BY created_at DESC").all() as Row[];
    return rows.map((r) => DocumentRegistry.toInfo(r)!);
  }

  delete(id: string) {
    this.db.prepare("DELETE FROM documents WHERE id = ?").run(id);
  }
}

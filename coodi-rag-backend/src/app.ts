import fs from "node:fs";
import Database from "better-sqlite3";
import cors from "cors";
import express from "express";
import type { Config } from "./config.js";
import { errorHandler } from "./errors.js";
import { type AIProvider, buildProvider } from "./llm.js";
import { RagService } from "./rag.js";
import { chatRouter } from "./routes/chat.js";
import { documentsRouter } from "./routes/documents.js";
import { DocumentRegistry } from "./store/registry.js";
import { LocalVectorStore, QdrantVectorStore } from "./store/vectorstore.js";

export function createRag(cfg: Config, provider: AIProvider = buildProvider(cfg)) {
  fs.mkdirSync(cfg.mediaDir, { recursive: true });
  const db = new Database(cfg.dbPath);
  db.pragma("journal_mode = WAL");
  const store = cfg.QDRANT_URL ? new QdrantVectorStore(cfg) : new LocalVectorStore(db, cfg.EMBEDDING_DIM);
  const rag = new RagService(cfg, provider, store, new DocumentRegistry(db));
  const close = async () => {
    await rag.idle();
    db.close();
  };
  return { rag, close };
}

export function createApp(cfg: Config, provider?: AIProvider) {
  const { rag, close } = createRag(cfg, provider);
  const app = express();

  app.disable("x-powered-by");
  app.use(cors({ origin: cfg.corsOrigins, methods: ["GET", "POST", "DELETE", "OPTIONS"] }));
  app.use(express.json({ limit: "16mb" }));

  app.use("/api/chat", chatRouter(rag));
  app.use("/api/documents", documentsRouter(rag));
  app.use("/media", express.static(cfg.mediaDir, { index: false, dotfiles: "deny", maxAge: "7d" }));

  app.get("/health", async (_req, res) => {
    res.json({ status: "ok", documents: rag.registry.list().length, chunks: await rag.store.count() });
  });

  app.use((_req, res) => {
    res.status(404).json({ detail: "No encontrado." });
  });
  app.use(errorHandler);

  return { app, rag, close };
}

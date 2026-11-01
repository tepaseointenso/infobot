import { createHash, randomUUID, timingSafeEqual } from "node:crypto";
import path from "node:path";
import { type RequestHandler, Router } from "express";
import multer from "multer";
import { z } from "zod";
import { HttpError } from "../errors.js";
import { SUPPORTED_TYPES, detectMime } from "../ingestion/pipeline.js";
import type { RagService } from "../rag.js";

export function requireAdmin(adminKey: string): RequestHandler {
  return (req, _res, next) => {
    if (!adminKey) throw new HttpError(503, "ADMIN_API_KEY no está configurada en el servidor.");
    const given = Buffer.from(req.header("x-api-key") ?? "");
    const expected = Buffer.from(adminKey);
    if (given.length !== expected.length || !timingSafeEqual(given, expected)) {
      throw new HttpError(401, "API key inválida.");
    }
    next();
  };
}

const uploadSchema = z.object({
  title: z.string().trim().max(200).optional(),
  tags: z.string().optional(),
  caption: z.string().trim().max(2000).optional(),
});

const searchSchema = z.object({
  query: z.string().trim().min(1).max(2000),
  tags: z.array(z.string()).nullish(),
});

export function documentsRouter(rag: RagService): Router {
  const router = Router();
  const upload = multer({ storage: multer.memoryStorage(), limits: { fileSize: rag.cfg.MAX_UPLOAD_MB * 1024 * 1024, files: 1 } });

  router.use(requireAdmin(rag.cfg.ADMIN_API_KEY));

  router.post("/", upload.single("file"), (req, res) => {
    const file = req.file;
    if (!file) throw new HttpError(400, "Falta el archivo (campo 'file').");
    if (!file.size) throw new HttpError(400, "Archivo vacío.");
    const form = uploadSchema.parse(req.body ?? {});

    // Multer decodes multipart filenames as latin1.
    const filename = path.basename(Buffer.from(file.originalname, "latin1").toString("utf8")) || "archivo";
    const mime = detectMime(file.buffer.subarray(0, 16), filename, file.mimetype);
    if (!SUPPORTED_TYPES.has(mime)) throw new HttpError(415, `Tipo no soportado: ${mime}`);

    const sha256 = createHash("sha256").update(file.buffer).digest("hex");
    const existing = rag.registry.findByHash(sha256);
    if (existing) throw new HttpError(409, "Este archivo ya fue cargado.", { document_id: existing.id });

    const tags = [...new Set((form.tags ?? "").split(",").map((t) => t.trim().toLowerCase()).filter(Boolean))].sort();
    const doc = rag.registry.create({
      id: randomUUID().replace(/-/g, ""),
      title: form.title || path.parse(filename).name,
      filename,
      mime,
      sha256,
      tags,
    });
    rag.enqueue({ docId: doc.id, data: file.buffer, filename, mime, title: doc.title, tags, caption: form.caption });
    res.status(202).json(doc);
  });

  router.get("/", (_req, res) => {
    res.json(rag.registry.list());
  });

  /** Debug endpoint: shows which fragments the chat would retrieve for a query. */
  router.post("/search", async (req, res) => {
    const { query, tags } = searchSchema.parse(req.body);
    res.json({ query, sources: rag.toSources(await rag.retrieve(query, tags)) });
  });

  router.get("/:id", (req, res) => {
    const doc = rag.registry.get(req.params.id);
    if (!doc) throw new HttpError(404, "Documento no encontrado.");
    res.json(doc);
  });

  router.delete("/:id", async (req, res) => {
    if (!rag.registry.get(req.params.id)) throw new HttpError(404, "Documento no encontrado.");
    await rag.deleteDocument(req.params.id);
    res.status(204).end();
  });

  return router;
}

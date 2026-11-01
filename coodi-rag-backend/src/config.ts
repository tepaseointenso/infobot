import path from "node:path";
import { z } from "zod";

const bool = z
  .string()
  .transform((v) => ["1", "true", "yes", "on"].includes(v.toLowerCase()))
  .or(z.boolean());

const optional = z
  .string()
  .optional()
  .transform((v) => (v ? v : undefined));

const schema = z.object({
  PORT: z.coerce.number().default(8000),
  CORS_ORIGINS: z.string().default("http://localhost:4200,https://coodi.cl,https://www.coodi.cl"),
  ADMIN_API_KEY: z.string().default(""),
  PUBLIC_BASE_URL: z.string().default(""),
  DATA_DIR: z.string().default("./data"),

  // "openai" works with any OpenAI-compatible API (OpenAI, Ollama, vLLM, Groq...).
  // "fake" is deterministic and offline, used for tests and local development without keys.
  PROVIDER: z.enum(["openai", "fake"]).default("openai"),
  OPENAI_API_KEY: z.string().default(""),
  OPENAI_BASE_URL: optional,
  CHAT_MODEL: z.string().default("gpt-4o-mini"),
  VISION_MODEL: z.string().default("gpt-4o-mini"),
  EMBEDDING_MODEL: z.string().default("text-embedding-3-small"),
  EMBEDDING_DIM: z.coerce.number().int().positive().default(1536),
  TEMPERATURE: z.coerce.number().default(0.2),
  MAX_ANSWER_TOKENS: z.coerce.number().int().default(900),

  // Empty -> vectors stored in the local SQLite database. Set to use a Qdrant server.
  QDRANT_URL: optional,
  QDRANT_API_KEY: optional,
  COLLECTION: z.string().default("coodi_knowledge"),

  CHUNK_SIZE: z.coerce.number().int().positive().default(1000),
  CHUNK_OVERLAP: z.coerce.number().int().nonnegative().default(150),
  TOP_K: z.coerce.number().int().positive().default(6),
  MIN_SCORE: z.coerce.number().default(0.25),

  DESCRIBE_PDF_IMAGES: bool.default(true),
  MIN_PDF_IMAGE_PX: z.coerce.number().int().default(120),
  MAX_IMAGES_PER_PDF: z.coerce.number().int().default(60),
  // Pages with less text than this are rendered and transcribed by the vision model (scanned PDFs).
  OCR_MIN_CHARS: z.coerce.number().int().default(40),
  MAX_UPLOAD_MB: z.coerce.number().int().positive().default(50),
});

export type Env = z.infer<typeof schema>;

export interface Config extends Env {
  mediaDir: string;
  dbPath: string;
  corsOrigins: string[];
}

export function loadConfig(env: Record<string, string | undefined> = process.env): Config {
  const parsed = schema.parse(env);
  const dataDir = path.resolve(parsed.DATA_DIR);
  return {
    ...parsed,
    DATA_DIR: dataDir,
    mediaDir: path.join(dataDir, "media"),
    dbPath: path.join(dataDir, "coodi.db"),
    corsOrigins: parsed.CORS_ORIGINS.split(",")
      .map((o) => o.trim())
      .filter(Boolean),
  };
}

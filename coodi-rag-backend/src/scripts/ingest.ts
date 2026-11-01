/**
 * Bulk-load a folder of Coodi material (PDFs, images, .txt/.md) without going through the HTTP API.
 *
 *   npm run ingest -- ./material --tags manual,basico
 *   npm run ingest -- ./material/guia.pdf --title "Guía docente Coodi"
 *
 * With the default local store, stop the API server first (it caches vectors in memory).
 */
import "dotenv/config";
import { createHash, randomUUID } from "node:crypto";
import fs from "node:fs/promises";
import path from "node:path";
import { parseArgs } from "node:util";
import { createRag } from "../app.js";
import { loadConfig } from "../config.js";
import { SUPPORTED_TYPES, detectMime } from "../ingestion/pipeline.js";

const { values, positionals } = parseArgs({
  allowPositionals: true,
  options: { tags: { type: "string", default: "" }, title: { type: "string" } },
});
if (!positionals[0]) {
  console.error("Uso: npm run ingest -- <archivo|carpeta> [--tags a,b] [--title 'Título']");
  process.exit(2);
}

const target = path.resolve(positionals[0]);
const isFile = (await fs.stat(target)).isFile();
const files = isFile
  ? [target]
  : (await fs.readdir(target, { recursive: true, withFileTypes: true }))
      .filter((e) => e.isFile())
      .map((e) => path.join(e.parentPath, e.name))
      .sort();

const tags = [...new Set(values.tags!.split(",").map((t) => t.trim().toLowerCase()).filter(Boolean))].sort();
const { rag, close } = createRag(loadConfig());
let failed = 0;

for (const file of files) {
  const data = await fs.readFile(file);
  const filename = path.basename(file);
  const mime = detectMime(data.subarray(0, 16), filename);
  if (!SUPPORTED_TYPES.has(mime)) {
    console.log(`-  omitido (tipo no soportado): ${file}`);
    continue;
  }
  const sha256 = createHash("sha256").update(data).digest("hex");
  const existing = rag.registry.findByHash(sha256);
  if (existing) {
    console.log(`=  ya existe: ${file} -> ${existing.id}`);
    continue;
  }
  const title = isFile && values.title ? values.title : path.parse(filename).name;
  const doc = rag.registry.create({ id: randomUUID().replace(/-/g, ""), title, filename, mime, sha256, tags });
  await rag.ingest({ docId: doc.id, data, filename, mime, title, tags });
  const result = rag.registry.get(doc.id)!;
  if (result.status === "ready") {
    console.log(`+  ${file}: ${result.chunks} fragmentos, ${result.images} imágenes -> ${doc.id}`);
  } else {
    failed++;
    console.log(`!  ${file}: ${result.error}`);
  }
}

await close();
process.exit(failed ? 1 : 0);

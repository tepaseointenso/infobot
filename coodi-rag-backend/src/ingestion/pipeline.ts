import { createHash } from "node:crypto";
import fs from "node:fs/promises";
import path from "node:path";
import * as mupdf from "mupdf";
import sharp from "sharp";
import type { Config } from "../config.js";
import type { AIProvider } from "../llm.js";
import { IMAGE_DESCRIPTION_PROMPT, PAGE_OCR_PROMPT } from "../prompts.js";
import { chunkText, normalizeText } from "./chunking.js";

export type ChunkKind = "text" | "image" | "page_ocr";

export interface Chunk {
  text: string;
  kind: ChunkKind;
  page: number | null;
  imagePath: string | null;
}

export const SUPPORTED_IMAGE_TYPES = new Set(["image/png", "image/jpeg", "image/webp", "image/gif"]);
export const SUPPORTED_TYPES = new Set(["application/pdf", "text/plain", "text/markdown", ...SUPPORTED_IMAGE_TYPES]);
const MAX_IMAGE_SIDE = 2000;

/** Downscale and convert to PNG, a format every vision model accepts. Throws on invalid images. */
export async function normalizeImage(data: Buffer): Promise<Buffer> {
  return sharp(data, { animated: false })
    .rotate()
    .resize({ width: MAX_IMAGE_SIDE, height: MAX_IMAGE_SIDE, fit: "inside", withoutEnlargement: true })
    .png()
    .toBuffer();
}

async function save(cfg: Config, docId: string, name: string, data: Uint8Array): Promise<string> {
  const rel = `${docId}/${name}`;
  await fs.mkdir(path.join(cfg.mediaDir, docId), { recursive: true });
  await fs.writeFile(path.join(cfg.mediaDir, rel), data);
  return rel;
}

async function describe(provider: AIProvider, image: Buffer, prompt: string): Promise<string> {
  try {
    return normalizeText(await provider.describeImage(image, "image/png", prompt));
  } catch (err) {
    console.error("No se pudo describir la imagen:", err);
    return "";
  }
}

function chunksOf(text: string, cfg: Config, kind: ChunkKind, page: number | null, imagePath: string | null, label = "") {
  return chunkText(text, cfg.CHUNK_SIZE, cfg.CHUNK_OVERLAP).map(
    (t): Chunk => ({ text: label ? `[${label}] ${t}` : t, kind, page, imagePath }),
  );
}

export interface Extraction {
  chunks: Chunk[];
  images: number;
}

async function extractPdf(data: Buffer, docId: string, cfg: Config, provider: AIProvider): Promise<Extraction> {
  const chunks: Chunk[] = [];
  const seen = new Set<string>();
  let images = 0;

  const doc = mupdf.Document.openDocument(data, "application/pdf");
  try {
    const pages = doc.countPages();
    for (let i = 0; i < pages; i++) {
      const pageNo = i + 1;
      const page = doc.loadPage(i);
      const stext = page.toStructuredText("preserve-images");
      const pageText = normalizeText(stext.asText());

      if (pageText.length < cfg.OCR_MIN_CHARS && cfg.DESCRIBE_PDF_IMAGES) {
        const scale = 150 / 72;
        const png = Buffer.from(
          page.toPixmap(mupdf.Matrix.scale(scale, scale), mupdf.ColorSpace.DeviceRGB, false).asPNG(),
        );
        const rel = await save(cfg, docId, `page_${pageNo}.png`, png);
        images++;
        const ocr = await describe(provider, png, PAGE_OCR_PROMPT);
        chunks.push(...chunksOf(`${pageText}\n\n${ocr}`, cfg, "page_ocr", pageNo, rel));
        continue;
      }

      chunks.push(...chunksOf(pageText, cfg, "text", pageNo, null));
      if (!cfg.DESCRIBE_PDF_IMAGES) continue;

      const pageImages: Buffer[] = [];
      stext.walk({
        onImageBlock(_bbox, _transform, image) {
          if (Math.min(image.getWidth(), image.getHeight()) < cfg.MIN_PDF_IMAGE_PX) return;
          try {
            let pix = image.toPixmap();
            const cs = pix.getColorSpace();
            if (cs && !cs.isRGB() && !cs.isGray()) pix = pix.convertToColorSpace(mupdf.ColorSpace.DeviceRGB);
            pageImages.push(Buffer.from(pix.asPNG()));
          } catch (err) {
            console.error(`No se pudo extraer una imagen de la página ${pageNo}:`, err);
          }
        },
      });

      for (const raw of pageImages) {
        if (images >= cfg.MAX_IMAGES_PER_PDF) break;
        let png: Buffer;
        try {
          png = await normalizeImage(raw);
        } catch (err) {
          console.error(`No se pudo extraer una imagen de la página ${pageNo}:`, err);
          continue;
        }
        const hash = createHash("sha1").update(png).digest("hex").slice(0, 12);
        if (seen.has(hash)) continue;
        seen.add(hash);

        const rel = await save(cfg, docId, `p${pageNo}_${hash}.png`, png);
        images++;
        let prompt = IMAGE_DESCRIPTION_PROMPT;
        if (pageText) prompt += `\n\nTexto de la página donde aparece la imagen (como contexto):\n${pageText.slice(0, 800)}`;
        const description = await describe(provider, png, prompt);
        if (description) chunks.push(...chunksOf(description, cfg, "image", pageNo, rel, `Imagen de la página ${pageNo}`));
      }
    }
  } finally {
    doc.destroy();
  }
  return { chunks, images };
}

async function extractImage(
  data: Buffer,
  filename: string,
  docId: string,
  cfg: Config,
  provider: AIProvider,
  caption?: string,
): Promise<Extraction> {
  const png = await normalizeImage(data);
  const stem = path.parse(filename).name.replace(/[^\p{L}\p{N}_-]+/gu, "_").slice(0, 60) || "imagen";
  const rel = await save(cfg, docId, `${stem}.png`, png);
  let prompt = IMAGE_DESCRIPTION_PROMPT;
  if (caption) prompt += `\n\nDescripción entregada por quien subió la imagen: ${caption}`;
  const description = await describe(provider, png, prompt);
  const full = normalizeText(`${caption ?? ""}\n\n${description}`);
  if (!full) throw new Error("No se pudo obtener una descripción de la imagen.");
  return { chunks: chunksOf(full, cfg, "image", null, rel, `Imagen: ${filename}`), images: 1 };
}

export async function extractChunks(
  data: Buffer,
  filename: string,
  mime: string,
  docId: string,
  cfg: Config,
  provider: AIProvider,
  caption?: string,
): Promise<Extraction> {
  if (mime === "application/pdf") return extractPdf(data, docId, cfg, provider);
  if (SUPPORTED_IMAGE_TYPES.has(mime)) return extractImage(data, filename, docId, cfg, provider, caption);
  if (mime === "text/plain" || mime === "text/markdown") {
    return { chunks: chunksOf(data.toString("utf8"), cfg, "text", null, null), images: 0 };
  }
  throw new Error(`Tipo de archivo no soportado: ${mime}`);
}

export function detectMime(head: Buffer, filename: string, declared?: string): string {
  if (head.subarray(0, 4).toString("latin1") === "%PDF") return "application/pdf";
  if (head.subarray(0, 4).equals(Buffer.from([0x89, 0x50, 0x4e, 0x47]))) return "image/png";
  if (head.subarray(0, 3).equals(Buffer.from([0xff, 0xd8, 0xff]))) return "image/jpeg";
  if (head.subarray(0, 4).toString("latin1") === "RIFF" && head.subarray(8, 12).toString("latin1") === "WEBP") {
    return "image/webp";
  }
  if (head.subarray(0, 4).toString("latin1") === "GIF8") return "image/gif";
  const ext = path.extname(filename).toLowerCase();
  if (ext === ".md" || ext === ".markdown") return "text/markdown";
  if (ext === ".txt") return "text/plain";
  return declared || "application/octet-stream";
}

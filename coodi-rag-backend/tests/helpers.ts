import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { PDFDocument, StandardFonts } from "pdf-lib";
import sharp from "sharp";
import { createApp } from "../src/app.js";
import { loadConfig } from "../src/config.js";

export const ADMIN = { "X-API-Key": "test-key" };

export function makePng(color = "#c81e1e", size = 300): Promise<Buffer> {
  return sharp({ create: { width: size, height: size, channels: 3, background: color } }).png().toBuffer();
}

export async function samplePdf(): Promise<Buffer> {
  const pdf = await PDFDocument.create();
  const font = await pdf.embedFont(StandardFonts.Helvetica);
  const p1 = pdf.addPage();
  p1.drawText("Manual Coodi. El sensor ultrasonico de Coodi mide distancias entre 2 y 400 cm.", { x: 50, y: 750, font, size: 11 });
  p1.drawText("Conecta el sensor ultrasonico al puerto D3 del robot Coodi.", { x: 50, y: 730, font, size: 11 });
  const img = await pdf.embedPng(await makePng());
  p1.drawImage(img, { x: 50, y: 300, width: 200, height: 200 });
  p1.drawImage(img, { x: 300, y: 300, width: 100, height: 100 }); // same image twice: must be deduplicated
  const p2 = pdf.addPage();
  p2.drawText("Los motores de Coodi se controlan con el bloque avanzar y girar. Velocidad de 0 a 100.", {
    x: 50,
    y: 750,
    font,
    size: 11,
  });
  pdf.addPage(); // blank page: exercises the scanned-page (vision OCR) path
  return Buffer.from(await pdf.save());
}

export function testApp(env: Record<string, string> = {}) {
  const dataDir = fs.mkdtempSync(path.join(os.tmpdir(), "coodi-rag-"));
  const cfg = loadConfig({
    PROVIDER: "fake",
    DATA_DIR: dataDir,
    ADMIN_API_KEY: "test-key",
    EMBEDDING_DIM: "256",
    MIN_SCORE: "0.05",
    // Set TEST_QDRANT_URL to run the same suite against a Qdrant server instead of the local store.
    ...(process.env.TEST_QDRANT_URL
      ? { QDRANT_URL: process.env.TEST_QDRANT_URL, COLLECTION: `test_${path.basename(dataDir).replace(/\W/g, "_")}` }
      : {}),
    ...env,
  });
  const ctx = createApp(cfg);
  return {
    ...ctx,
    cfg,
    async cleanup() {
      await ctx.close();
      fs.rmSync(dataDir, { recursive: true, force: true });
    },
  };
}

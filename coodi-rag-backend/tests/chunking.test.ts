import { describe, expect, it } from "vitest";
import { chunkText, normalizeText } from "../src/ingestion/chunking.js";

describe("chunking", () => {
  it("deja textos cortos en un solo fragmento", () => {
    expect(chunkText("Hola  Coodi\n\n\n\nfin", 100, 10)).toEqual(["Hola Coodi\n\nfin"]);
  });

  it("respeta el tamaño y agrega solapamiento", () => {
    const paragraphs = Array.from({ length: 40 }, (_, i) => `Paso ${i}: conecta el cable ${i} al puerto del robot Coodi.`);
    const chunks = chunkText(paragraphs.join("\n\n"), 200, 40);
    expect(chunks.length).toBeGreaterThan(5);
    expect(chunks.every((c) => c.length <= 200 + 40 + 1)).toBe(true);
    const joined = chunks.join(" ");
    expect(paragraphs.every((p) => joined.includes(p))).toBe(true);
    expect(chunks[0]).toContain(chunks[1].split(" ")[0]);
  });

  it("une palabras cortadas con guion", () => {
    expect(normalizeText("progra-\nmación")).toBe("programación");
  });

  it("ignora texto vacío", () => {
    expect(chunkText("   \n ")).toEqual([]);
  });
});

import request from "supertest";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { createApp } from "../src/app.js";
import { ADMIN, makePng, samplePdf, testApp } from "./helpers.js";

let ctx: ReturnType<typeof testApp>;
let pdf: Buffer;

beforeEach(async () => {
  ctx = testApp();
  pdf = await samplePdf();
});
afterEach(() => ctx.cleanup());

async function upload(name: string, data: Buffer, fields: Record<string, string> = {}) {
  let req = request(ctx.app).post("/api/documents").set(ADMIN).attach("file", data, name);
  for (const [k, v] of Object.entries(fields)) req = req.field(k, v);
  const res = await req;
  await ctx.rag.idle();
  return res;
}

const getDoc = async (id: string) => (await request(ctx.app).get(`/api/documents/${id}`).set(ADMIN)).body;

describe("ingesta de PDF y chat", () => {
  it("indexa texto, imágenes y páginas escaneadas, y responde con fuentes", async () => {
    const res = await upload("manual.pdf", pdf, { title: "Manual Coodi", tags: "Manual, basico" });
    expect(res.status).toBe(202);
    expect(res.body.status).toBe("processing");

    const doc = await getDoc(res.body.id);
    expect(doc).toMatchObject({ status: "ready", tags: ["basico", "manual"], images: 2 });
    expect(doc.chunks).toBeGreaterThanOrEqual(4);

    const chat = await request(ctx.app).post("/api/chat").send({ message: "¿En qué puerto conecto el sensor ultrasonico?" });
    expect(chat.status).toBe(200);
    expect(chat.body.answer).toContain("[1]");
    const top = chat.body.sources[0];
    expect(top).toMatchObject({ title: "Manual Coodi", page: 1, kind: "text" });
    expect(top.snippet).toContain("D3");

    const kinds = new Set(chat.body.sources.map((s: { kind: string }) => s.kind));
    expect(kinds).toContain("image");
    expect(kinds).toContain("page_ocr");
    const withImage = chat.body.sources.find((s: { image_url: string | null }) => s.image_url);
    const media = await request(ctx.app).get(withImage.image_url);
    expect(media.status).toBe(200);
    expect(media.headers["content-type"]).toBe("image/png");
  });

  it("filtra por etiquetas", async () => {
    await upload("manual.pdf", pdf, { tags: "basico" });
    const res = await request(ctx.app).post("/api/chat").send({ message: "sensor ultrasonico", tags: ["avanzado"] });
    expect(res.body.sources).toEqual([]);
    expect(res.body.answer).toContain("No encuentro");
  });

  it("usa historial y foto adjunta", async () => {
    await upload("manual.pdf", pdf);
    const image = `data:image/png;base64,${(await makePng()).toString("base64")}`;
    const res = await request(ctx.app)
      .post("/api/chat")
      .send({
        message: "¿y cómo controlo los motores?",
        history: [
          { role: "user", content: "Hola" },
          { role: "assistant", content: "Hola profe, ¿en qué te ayudo?" },
        ],
        image_base64: image,
      });
    expect(res.status).toBe(200);
    expect(res.body.sources.length).toBeGreaterThan(0);

    expect((await request(ctx.app).post("/api/chat").send({ message: "hola", image_base64: "no-es-base64!!" })).status).toBe(400);
    expect((await request(ctx.app).post("/api/chat").send({ message: "hola", image_base64: "eA==" })).status).toBe(400);
    expect((await request(ctx.app).post("/api/chat").send({ message: "" })).status).toBe(422);
  });

  it("transmite la respuesta por SSE", async () => {
    await upload("manual.pdf", pdf);
    const res = await request(ctx.app).post("/api/chat/stream").send({ message: "motores avanzar" });
    expect(res.status).toBe(200);
    expect(res.headers["content-type"]).toContain("text/event-stream");
    const events = res.text.trim().split("\n\n").map((b) => {
      const [ev, data] = b.split("\n");
      return { event: ev.replace("event: ", ""), data: JSON.parse(data.replace("data: ", "")) };
    });
    expect(events[0].event).toBe("sources");
    expect(events[0].data.sources[0].page).toBe(2);
    expect(events.at(-1)!.event).toBe("done");
    expect(events.filter((e) => e.event === "token").length).toBeGreaterThan(1);

    const bad = await request(ctx.app).post("/api/chat/stream").send({ message: "hola", image_base64: "eA==" });
    expect(bad.status).toBe(400);
  });
});

describe("gestión de documentos", () => {
  it("indexa imágenes con descripción", async () => {
    const res = await upload("cableado.png", await makePng("#0000ff"), { caption: "Diagrama de cableado del buzzer" });
    expect(await getDoc(res.body.id)).toMatchObject({ status: "ready", images: 1, mime: "image/png" });

    const search = await request(ctx.app).post("/api/documents/search").set(ADMIN).send({ query: "cableado del buzzer" });
    expect(search.body.sources[0].kind).toBe("image");
    expect(search.body.sources[0].image_url).toMatch(/^\/media\/.+\.png$/);
  });

  it("acepta markdown y rechaza duplicados y tipos no soportados", async () => {
    const md = await upload("clase1.md", Buffer.from("# Clase 1\nObjetivo: conocer el robot Coodi."));
    expect(await getDoc(md.body.id)).toMatchObject({ status: "ready", mime: "text/markdown" });

    expect((await upload("a.pdf", pdf)).status).toBe(202);
    const dup = await upload("b.pdf", pdf);
    expect(dup.status).toBe(409);
    expect(dup.body.document_id).toBeTruthy();

    expect((await upload("x.zip", Buffer.from("PK\x03\x04zipdata"))).status).toBe(415);
  });

  it("marca como error los archivos que no se pueden procesar", async () => {
    const res = await upload("roto.pdf", Buffer.from("%PDF-1.4 esto no es un pdf"));
    expect(res.status).toBe(202);
    const doc = await getDoc(res.body.id);
    expect(doc.status).toBe("error");
    expect(doc.error).toBeTruthy();
  });

  it("borra documentos con sus vectores e imágenes", async () => {
    const { body } = await upload("manual.pdf", pdf);
    expect((await request(ctx.app).get("/health")).body.chunks).toBeGreaterThan(0);

    expect((await request(ctx.app).delete(`/api/documents/${body.id}`).set(ADMIN)).status).toBe(204);
    expect((await request(ctx.app).get("/health")).body).toEqual({ status: "ok", documents: 0, chunks: 0 });
    expect((await request(ctx.app).get(`/api/documents/${body.id}`).set(ADMIN)).status).toBe(404);
  });

  it("exige API key en los endpoints de administración", async () => {
    expect((await request(ctx.app).get("/api/documents")).status).toBe(401);
    expect((await request(ctx.app).get("/api/documents").set("X-API-Key", "otra")).status).toBe(401);
    expect((await request(ctx.app).post("/api/documents").attach("file", pdf, "a.pdf")).status).toBe(401);
  });

  it("persiste el índice entre reinicios", async () => {
    await upload("manual.pdf", pdf);
    await ctx.close();
    const restarted = createApp(ctx.cfg);
    const res = await request(restarted.app).post("/api/chat").send({ message: "sensor ultrasonico D3" });
    expect(res.body.sources[0].page).toBe(1);
    await restarted.close();
    ctx.close = async () => {};
  });

  it("permite CORS desde el frontend Angular", async () => {
    const res = await request(ctx.app)
      .options("/api/chat")
      .set("Origin", "http://localhost:4200")
      .set("Access-Control-Request-Method", "POST");
    expect(res.status).toBe(204);
    expect(res.headers["access-control-allow-origin"]).toBe("http://localhost:4200");
  });
});

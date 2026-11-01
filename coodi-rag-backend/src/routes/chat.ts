import { Router } from "express";
import { z } from "zod";
import type { RagService } from "../rag.js";

export const chatSchema = z.object({
  message: z.string().trim().min(1).max(4000),
  history: z
    .array(z.object({ role: z.enum(["user", "assistant"]), content: z.string().max(8000) }))
    .max(40)
    .default([]),
  image_base64: z.string().max(12_000_000).nullish(),
  tags: z.array(z.string()).nullish(),
});

export function chatRouter(rag: RagService): Router {
  const router = Router();

  router.post("/", async (req, res) => {
    res.json(await rag.answer(chatSchema.parse(req.body)));
  });

  router.post("/stream", async (req, res) => {
    const events = await rag.answerStream(chatSchema.parse(req.body));
    res.writeHead(200, {
      "Content-Type": "text/event-stream; charset=utf-8",
      "Cache-Control": "no-cache",
      Connection: "keep-alive",
      "X-Accel-Buffering": "no",
    });
    let closed = false;
    res.on("close", () => (closed = true));
    for await (const { event, data } of events) {
      if (closed) break;
      res.write(`event: ${event}\ndata: ${JSON.stringify(data)}\n\n`);
    }
    res.end();
  });

  return router;
}

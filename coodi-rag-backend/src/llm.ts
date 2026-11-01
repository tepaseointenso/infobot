import { createHash } from "node:crypto";
import OpenAI from "openai";
import type { ChatCompletionMessageParam } from "openai/resources/chat/completions";
import type { Config } from "./config.js";
import { CONDENSE_MARKER } from "./prompts.js";

export type Message = ChatCompletionMessageParam;

export interface AIProvider {
  embed(texts: string[]): Promise<number[][]>;
  describeImage(image: Buffer, mime: string, prompt: string): Promise<string>;
  chat(messages: Message[], maxTokens?: number): Promise<string>;
  chatStream(messages: Message[]): AsyncIterable<string>;
}

export const imageDataUrl = (image: Buffer, mime: string) => `data:${mime};base64,${image.toString("base64")}`;

export class OpenAIProvider implements AIProvider {
  private client: OpenAI;

  constructor(private cfg: Config) {
    if (!cfg.OPENAI_API_KEY && !cfg.OPENAI_BASE_URL) {
      throw new Error("OPENAI_API_KEY no está configurada (o usa PROVIDER=fake para pruebas).");
    }
    this.client = new OpenAI({ apiKey: cfg.OPENAI_API_KEY || "not-needed", baseURL: cfg.OPENAI_BASE_URL });
  }

  async embed(texts: string[]): Promise<number[][]> {
    const out: number[][] = [];
    for (let i = 0; i < texts.length; i += 64) {
      const res = await this.client.embeddings.create({ model: this.cfg.EMBEDDING_MODEL, input: texts.slice(i, i + 64) });
      out.push(...[...res.data].sort((a, b) => a.index - b.index).map((d) => d.embedding));
    }
    return out;
  }

  async describeImage(image: Buffer, mime: string, prompt: string): Promise<string> {
    const res = await this.client.chat.completions.create({
      model: this.cfg.VISION_MODEL,
      temperature: 0,
      max_tokens: 800,
      messages: [
        {
          role: "user",
          content: [
            { type: "text", text: prompt },
            { type: "image_url", image_url: { url: imageDataUrl(image, mime) } },
          ],
        },
      ],
    });
    return (res.choices[0]?.message.content ?? "").trim();
  }

  async chat(messages: Message[], maxTokens?: number): Promise<string> {
    const res = await this.client.chat.completions.create({
      model: this.cfg.CHAT_MODEL,
      temperature: this.cfg.TEMPERATURE,
      max_tokens: maxTokens ?? this.cfg.MAX_ANSWER_TOKENS,
      messages,
    });
    return (res.choices[0]?.message.content ?? "").trim();
  }

  async *chatStream(messages: Message[]): AsyncIterable<string> {
    const stream = await this.client.chat.completions.create({
      model: this.cfg.CHAT_MODEL,
      temperature: this.cfg.TEMPERATURE,
      max_tokens: this.cfg.MAX_ANSWER_TOKENS,
      messages,
      stream: true,
    });
    for await (const event of stream) {
      const text = event.choices[0]?.delta?.content;
      if (text) yield text;
    }
  }
}

/** Offline, deterministic provider: hashed bag-of-words embeddings and template answers. */
export class FakeProvider implements AIProvider {
  constructor(private cfg: Config) {}

  async embed(texts: string[]): Promise<number[][]> {
    return texts.map((text) => {
      const vec = new Array<number>(this.cfg.EMBEDDING_DIM).fill(0);
      for (const tok of text.toLowerCase().match(/[\p{L}\p{N}_]+/gu) ?? []) {
        const h = createHash("md5").update(tok).digest().readUInt32BE(0);
        vec[h % vec.length] += 1;
      }
      const norm = Math.hypot(...vec) || 1;
      return vec.map((v) => v / norm);
    });
  }

  async describeImage(image: Buffer, mime: string): Promise<string> {
    return `Imagen (${mime}, ${image.length} bytes) relacionada con el robot Coodi.`;
  }

  async chat(messages: Message[]): Promise<string> {
    const last = messages.at(-1)!.content;
    const text = typeof last === "string" ? last : (last ?? []).map((p) => ("text" in p ? p.text : "")).join(" ");
    if (text.includes(CONDENSE_MARKER)) {
      return text.split("Última pregunta:").at(-1)!.split(CONDENSE_MARKER)[0].trim();
    }
    const refs = [...new Set([...text.matchAll(/^\[(\d+)\]/gm)].map((m) => m[1]))];
    if (!refs.length) return "No encuentro esa información en el material de Coodi.";
    return `Respuesta de prueba basada en el material de Coodi ${refs.map((r) => `[${r}]`).join(" ")}.`;
  }

  async *chatStream(messages: Message[]): AsyncIterable<string> {
    for (const word of (await this.chat(messages)).split(" ")) yield `${word} `;
  }
}

export function buildProvider(cfg: Config): AIProvider {
  return cfg.PROVIDER === "fake" ? new FakeProvider(cfg) : new OpenAIProvider(cfg);
}

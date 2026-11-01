import { Injectable, NgZone, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Ajusta a tu environment.ts, p.ej. environment.ragApiUrl
const RAG_API_URL = 'http://localhost:8000';

export interface ChatTurn {
  role: 'user' | 'assistant';
  content: string;
}

export interface ChatSource {
  id: number;
  doc_id: string;
  title: string;
  filename: string;
  page: number | null;
  kind: 'text' | 'image' | 'page_ocr';
  score: number;
  snippet: string;
  image_url: string | null;
}

export interface ChatResponse {
  answer: string;
  sources: ChatSource[];
  query: string;
}

export interface ChatRequest {
  message: string;
  history?: ChatTurn[];
  /** Foto opcional del profesor (data URL o base64). */
  image_base64?: string;
  tags?: string[];
}

export type ChatStreamEvent =
  | { type: 'sources'; sources: ChatSource[]; query: string }
  | { type: 'token'; text: string }
  | { type: 'done' }
  | { type: 'error'; detail: string };

@Injectable({ providedIn: 'root' })
export class CoodiChatService {
  private http = inject(HttpClient);
  private zone = inject(NgZone);

  /** Respuesta completa en una sola llamada. */
  ask(req: ChatRequest): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(`${RAG_API_URL}/api/chat`, req);
  }

  /**
   * Respuesta en streaming (Server-Sent Events sobre POST).
   * EventSource no soporta POST, por eso se usa fetch + ReadableStream.
   */
  askStream(req: ChatRequest): Observable<ChatStreamEvent> {
    return new Observable<ChatStreamEvent>((subscriber) => {
      const controller = new AbortController();

      (async () => {
        const res = await fetch(`${RAG_API_URL}/api/chat/stream`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(req),
          signal: controller.signal,
        });
        if (!res.ok || !res.body) {
          const body = await res.json().catch(() => ({}));
          throw new Error(body.detail ?? `Error ${res.status}`);
        }

        const reader = res.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';
        while (true) {
          const { value, done } = await reader.read();
          if (done) break;
          buffer += decoder.decode(value, { stream: true });
          let sep: number;
          while ((sep = buffer.indexOf('\n\n')) >= 0) {
            const block = buffer.slice(0, sep);
            buffer = buffer.slice(sep + 2);
            const type = block.match(/^event: (.*)$/m)?.[1];
            const data = JSON.parse(block.match(/^data: (.*)$/m)?.[1] ?? '{}');
            this.zone.run(() => subscriber.next({ type, ...data } as ChatStreamEvent));
          }
        }
        this.zone.run(() => subscriber.complete());
      })().catch((err) => {
        if (err?.name !== 'AbortError') this.zone.run(() => subscriber.error(err));
      });

      return () => controller.abort();
    });
  }

  /** Convierte un File (input type="file") a data URL para enviarlo como image_base64. */
  fileToDataUrl(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result as string);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }
}

/*
Ejemplo de uso en un componente:

  messages: ChatTurn[] = [];
  sources: ChatSource[] = [];
  answer = '';

  send(text: string) {
    const history = this.messages.slice(-10);
    this.messages.push({ role: 'user', content: text });
    this.answer = '';
    this.chat.askStream({ message: text, history }).subscribe({
      next: (e) => {
        if (e.type === 'sources') this.sources = e.sources;
        if (e.type === 'token') this.answer += e.text;
      },
      complete: () => this.messages.push({ role: 'assistant', content: this.answer }),
    });
  }

Las citas [n] del texto corresponden a sources[n-1]; si source.image_url existe, muéstrala como miniatura.
*/

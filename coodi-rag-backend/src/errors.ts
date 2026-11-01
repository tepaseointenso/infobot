import type { ErrorRequestHandler } from "express";
import multer from "multer";
import { ZodError } from "zod";
import { BadRequestError } from "./rag.js";

export class HttpError extends Error {
  constructor(
    readonly status: number,
    message: string,
    readonly extra?: Record<string, unknown>,
  ) {
    super(message);
  }
}

export const errorHandler: ErrorRequestHandler = (err, _req, res, _next) => {
  if (res.headersSent) {
    res.end();
    return;
  }
  if (err instanceof HttpError) {
    res.status(err.status).json({ detail: err.message, ...err.extra });
  } else if (err instanceof ZodError) {
    res.status(422).json({ detail: "Datos inválidos.", errors: err.issues });
  } else if (err instanceof BadRequestError) {
    res.status(400).json({ detail: err.message });
  } else if (err instanceof multer.MulterError) {
    res.status(err.code === "LIMIT_FILE_SIZE" ? 413 : 400).json({ detail: err.message });
  } else if (err?.type === "entity.too.large") {
    res.status(413).json({ detail: "Solicitud demasiado grande." });
  } else if (err?.type === "entity.parse.failed") {
    res.status(400).json({ detail: "JSON inválido." });
  } else {
    console.error(err);
    res.status(500).json({ detail: "Error interno del servidor." });
  }
};

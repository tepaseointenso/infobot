import "dotenv/config";
import { createApp } from "./app.js";
import { loadConfig } from "./config.js";

const cfg = loadConfig();
const { app, close } = createApp(cfg);

const server = app.listen(cfg.PORT, () => {
  console.info(`Coodi RAG escuchando en http://localhost:${cfg.PORT} (proveedor: ${cfg.PROVIDER})`);
});

const shutdown = () => {
  server.close(async () => {
    await close();
    process.exit(0);
  });
};
process.on("SIGINT", shutdown);
process.on("SIGTERM", shutdown);

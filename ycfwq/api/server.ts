/**
 * 本地开发入口 — 启动 HTTP 服务并挂载 WebSocket 终端网关
 */
import app from "./app.js";
import { setupTerminalGateway } from "./ws/terminalGateway.js";

const PORT = Number(process.env.PORT) || 3001;

const server = app.listen(PORT, () => {
  console.log(`[Remote Kernel] API server ready on port ${PORT}`);
});

// 挂载 WebSocket 终端网关
setupTerminalGateway(server);

process.on("SIGTERM", () => {
  console.log("SIGTERM signal received");
  server.close(() => {
    console.log("Server closed");
    process.exit(0);
  });
});

process.on("SIGINT", () => {
  console.log("SIGINT signal received");
  server.close(() => {
    console.log("Server closed");
    process.exit(0);
  });
});

export default app;

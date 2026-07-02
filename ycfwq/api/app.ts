/**
 * Express 应用 — 本地与 serverless 共用
 */
import express, {
  type Request,
  type Response,
  type NextFunction,
} from "express";
import cors from "cors";
import dotenv from "dotenv";
import connectionRoutes from "./routes/connections.js";

dotenv.config();

const app: express.Application = express();

app.use(cors());
app.use(express.json({ limit: "10mb" }));
app.use(express.urlencoded({ extended: true, limit: "10mb" }));

/**
 * 健康检查
 */
app.use("/api/health", (_req: Request, res: Response) => {
  res.status(200).json({ success: true, message: "ok" });
});

/**
 * 连接配置 + 监控 API
 */
app.use("/api/connections", connectionRoutes);

/**
 * 错误处理中间件
 */
app.use((error: Error, _req: Request, res: Response, _next: NextFunction) => {
  console.error("[api error]", error);
  res.status(500).json({ success: false, error: error.message || "Server internal error" });
});

/**
 * 404
 */
app.use((_req: Request, res: Response) => {
  res.status(404).json({ success: false, error: "API not found" });
});

export default app;

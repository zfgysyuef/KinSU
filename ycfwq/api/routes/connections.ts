/**
 * 连接配置 REST API
 */
import { Router, type Request, type Response } from "express";
import {
  listConnections,
  getConnection,
  getListItem,
  createConnection,
  updateConnection,
  deleteConnection,
  touchLastConnected,
} from "../services/connectionService.js";
import { testConnection, execOnce } from "../services/sshService.js";
import { collectMetrics } from "../services/monitorService.js";
import type { ConnectionInput } from "@shared/types";

const router = Router();

function parseInput(body: any): ConnectionInput {
  return {
    name: String(body?.name ?? "").trim(),
    host: String(body?.host ?? "").trim(),
    port: parseInt(body?.port, 10) || 22,
    username: String(body?.username ?? "").trim(),
    authType: body?.authType === "privateKey" ? "privateKey" : "password",
    password: body?.password,
    privateKey: body?.privateKey,
    passphrase: body?.passphrase,
  };
}

function validateInput(input: ConnectionInput): string | null {
  if (!input.name) return "连接名称不能为空";
  if (!input.host) return "主机地址不能为空";
  if (!input.username) return "用户名不能为空";
  if (input.authType === "password" && !input.password) {
    return "密码不能为空";
  }
  if (input.authType === "privateKey" && !input.privateKey) {
    return "私钥不能为空";
  }
  return null;
}

// 列表
router.get("/", (_req: Request, res: Response) => {
  res.json(listConnections());
});

// 详情
router.get("/:id", (req: Request, res: Response) => {
  const id = parseInt(req.params.id, 10);
  if (!Number.isFinite(id)) {
    res.status(400).json({ error: "无效的 id" });
    return;
  }
  const item = getListItem(id);
  if (!item) {
    res.status(404).json({ error: "连接不存在" });
    return;
  }
  res.json(item);
});

// 新建
router.post("/", (req: Request, res: Response) => {
  const input = parseInput(req.body);
  const err = validateInput(input);
  if (err) {
    res.status(400).json({ error: err });
    return;
  }
  const item = createConnection(input);
  res.status(201).json(item);
});

// 更新
router.put("/:id", (req: Request, res: Response) => {
  const id = parseInt(req.params.id, 10);
  if (!Number.isFinite(id)) {
    res.status(400).json({ error: "无效的 id" });
    return;
  }
  const input = parseInput(req.body);
  // 更新时若未提供敏感字段，允许跳过校验（保留原值）
  const existing = getConnection(id);
  if (!existing) {
    res.status(404).json({ error: "连接不存在" });
    return;
  }
  if (!input.name) input.name = existing.name;
  if (!input.host) input.host = existing.host;
  if (!input.username) input.username = existing.username;
  const err = validateInput({
    ...input,
    password: input.password ?? (existing.authType === "password" ? "____" : undefined),
    privateKey:
      input.privateKey ?? (existing.authType === "privateKey" ? "____" : undefined),
  });
  if (err) {
    res.status(400).json({ error: err });
    return;
  }
  const item = updateConnection(id, input);
  res.json(item);
});

// 删除
router.delete("/:id", (req: Request, res: Response) => {
  const id = parseInt(req.params.id, 10);
  if (!Number.isFinite(id)) {
    res.status(400).json({ error: "无效的 id" });
    return;
  }
  const ok = deleteConnection(id);
  if (!ok) {
    res.status(404).json({ error: "连接不存在" });
    return;
  }
  res.json({ ok: true });
});

// 测试连接
router.post("/:id/test", async (req: Request, res: Response) => {
  const id = parseInt(req.params.id, 10);
  const conn = getConnection(id);
  if (!conn) {
    res.status(404).json({ error: "连接不存在" });
    return;
  }
  const result = await testConnection(conn);
  res.json(result);
});

// 测试（不落库，前端表单直接提交凭据时使用）
router.post("/test/dry", async (req: Request, res: Response) => {
  const input = parseInput(req.body);
  const err = validateInput(input);
  if (err) {
    res.status(400).json({ error: err });
    return;
  }
  const result = await testConnection({ ...input, id: 0, createdAt: "", updatedAt: "" });
  res.json(result);
});

// 执行单次命令（用于监控采集等受控场景）
router.post("/:id/exec", async (req: Request, res: Response) => {
  const id = parseInt(req.params.id, 10);
  const conn = getConnection(id);
  if (!conn) {
    res.status(404).json({ error: "连接不存在" });
    return;
  }
  const command = String(req?.body?.command ?? "").trim();
  if (!command) {
    res.status(400).json({ error: "命令不能为空" });
    return;
  }
  const result = await execOnce(conn, command, 25000);
  res.json(result);
});

// 系统监控采集
router.get("/:id/monitor", async (req: Request, res: Response) => {
  const id = parseInt(req.params.id, 10);
  const conn = getConnection(id);
  if (!conn) {
    res.status(404).json({ error: "连接不存在" });
    return;
  }
  try {
    const metrics = await collectMetrics(conn);
    touchLastConnected(id);
    res.json(metrics);
  } catch (e: any) {
    res.status(500).json({ error: e?.message || "采集失败" });
  }
});

export default router;

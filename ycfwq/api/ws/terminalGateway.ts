/**
 * WebSocket 终端网关 — 将浏览器 ↔ Node ↔ 目标服务器的 SSH shell 串接
 * 协议见 shared/types.ts 的 ClientMessage / ServerMessage
 */
import type { Server } from "http";
import { WebSocketServer, WebSocket } from "ws";
import { getConnection } from "../services/connectionService.js";
import { openShell } from "../services/sshService.js";
import { touchLastConnected } from "../services/connectionService.js";
import type { ClientMessage, ServerMessage } from "@shared/types";

export function setupTerminalGateway(server: Server): WebSocketServer {
  const wss = new WebSocketServer({ noServer: true });

  server.on("upgrade", (req, socket, head) => {
    const url = req.url || "";
    // 仅处理 /ws/terminal/:connectionId
    const match = url.match(/^\/ws\/terminal\/(\d+)(?:\?.*)?$/);
    if (!match) {
      socket.destroy();
      return;
    }

    wss.handleUpgrade(req, socket, head, (ws) => {
      wss.emit("connection", ws, req, match[1]);
    });
  });

  wss.on("connection", (ws: WebSocket, _req: any, connectionIdStr: string) => {
    const connectionId = parseInt(connectionIdStr, 10);
    const conn = getConnection(connectionId);

    const send = (msg: ServerMessage) => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify(msg));
      }
    };

    if (!conn) {
      send({ type: "error", message: "连接配置不存在" });
      send({ type: "closed", reason: "no-config" });
      ws.close();
      return;
    }

    let initialCols = 80;
    let initialRows = 24;

    // 等待第一条 resize 消息后再开 shell，以拿到准确尺寸
    let started = false;
    const startShell = (cols: number, rows: number) => {
      if (started) return;
      started = true;

      const session = openShell(
        conn,
        cols,
        rows,
        (data) => send({ type: "data", data }),
        () => {
          send({ type: "closed" });
          try {
            ws.close();
          } catch {
            /* noop */
          }
        },
        (err) => {
          send({ type: "error", message: err.message });
          send({ type: "closed", reason: "ssh-error" });
          try {
            ws.close();
          } catch {
            /* noop */
          }
        },
      );

      touchLastConnected(connectionId);
      send({ type: "ready" });

      ws.on("message", (raw) => {
        let msg: ClientMessage;
        try {
          msg = JSON.parse(raw.toString());
        } catch {
          return;
        }
        if (msg.type === "input") {
          session.write(msg.data);
        } else if (msg.type === "resize") {
          session.resize(msg.cols, msg.rows);
        }
      });

      ws.on("close", () => {
        session.close();
      });
    };

    // 第一条消息若为 resize 则用其尺寸启动；否则立即启动
    ws.once("message", (raw) => {
      try {
        const msg = JSON.parse(raw.toString()) as ClientMessage;
        if (msg.type === "resize") {
          initialCols = msg.cols || 80;
          initialRows = msg.rows || 24;
        } else if (msg.type === "input") {
          // 第一条是 input，直接启动并写入
          startShell(initialCols, initialRows);
          // 重新派发
          ws.emit("message", Buffer.from(JSON.stringify(msg)));
          return;
        }
      } catch {
        /* ignore */
      }
      startShell(initialCols, initialRows);
    });

    // 超时保护：10 秒内无消息则按默认尺寸启动
    setTimeout(() => {
      if (!started) startShell(initialCols, initialRows);
    }, 2000);
  });

  return wss;
}

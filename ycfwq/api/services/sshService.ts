/**
 * SSH 服务 — 封装 ssh2 的连接、命令执行、交互 shell
 */
import { Client } from "ssh2";
import type { ConnectConfig, PseudoTtyOptions } from "ssh2";
import type { ConnectionConfig, ExecResult } from "@shared/types";

export interface SshClientWrapper {
  client: Client;
  close: () => void;
}

function buildConnectConfig(conn: ConnectionConfig): ConnectConfig {
  const base: ConnectConfig = {
    host: conn.host,
    port: conn.port,
    username: conn.username,
    readyTimeout: 15000,
    keepaliveInterval: 0,
  };

  if (conn.authType === "privateKey") {
    return {
      ...base,
      privateKey: conn.privateKey,
      passphrase: conn.passphrase,
    };
  }
  return { ...base, password: conn.password };
}

/**
 * 建立一次性 SSH 连接并执行命令，完成后关闭
 */
export function execOnce(
  conn: ConnectionConfig,
  command: string,
  timeoutMs = 20000,
): Promise<ExecResult> {
  return new Promise((resolve) => {
    const client = new Client();
    let settled = false;

    const timer = setTimeout(() => {
      if (settled) return;
      settled = true;
      try {
        client.end();
      } catch {
        /* noop */
      }
      resolve({
        stdout: "",
        stderr: "命令执行超时",
        exitCode: -1,
      });
    }, timeoutMs);

    client.on("ready", () => {
      client.exec(command, (err, stream) => {
        if (err) {
          if (settled) return;
          settled = true;
          clearTimeout(timer);
          client.end();
          resolve({ stdout: "", stderr: String(err), exitCode: -1 });
          return;
        }
        let stdout = "";
        let stderr = "";
        stream.on("data", (d: Buffer) => (stdout += d.toString("utf8")));
        stream.stderr.on("data", (d: Buffer) => (stderr += d.toString("utf8")));
        stream.on("close", (code: number) => {
          if (settled) return;
          settled = true;
          clearTimeout(timer);
          client.end();
          resolve({ stdout, stderr, exitCode: code ?? 0 });
        });
      });
    });

    client.on("error", (err) => {
      if (settled) return;
      settled = true;
      clearTimeout(timer);
      resolve({ stdout: "", stderr: String(err.message), exitCode: -1 });
    });

    client.on("close", () => {
      if (settled) return;
      settled = true;
      clearTimeout(timer);
      resolve({
        stdout: "",
        stderr: "连接已关闭",
        exitCode: -1,
      });
    });

    try {
      client.connect(buildConnectConfig(conn));
    } catch (e) {
      if (settled) return;
      settled = true;
      clearTimeout(timer);
      resolve({ stdout: "", stderr: String(e), exitCode: -1 });
    }
  });
}

/**
 * 测试连接是否可建立
 */
export async function testConnection(
  conn: ConnectionConfig,
): Promise<{ ok: boolean; message: string }> {
  const result = await execOnce(conn, "echo __RK_OK__", 15000);
  if (result.exitCode === 0 && result.stdout.includes("__RK_OK__")) {
    return { ok: true, message: "连接成功" };
  }
  return {
    ok: false,
    message: result.stderr || result.stdout || "连接失败",
  };
}

const TTY_OPTS: PseudoTtyOptions = {
  rows: 24,
  cols: 80,
  term: "xterm-256color",
};

/**
 * 打开交互式 shell，返回 client 与 stream
 */
export function openShell(
  conn: ConnectionConfig,
  cols: number,
  rows: number,
  onData: (data: string) => void,
  onClose: () => void,
  onError: (err: Error) => void,
): { client: Client; write: (data: string) => void; resize: (cols: number, rows: number) => void; close: () => void } {
  const client = new Client();
  let stream: any = null;
  let closed = false;

  const tty: PseudoTtyOptions = { ...TTY_OPTS, cols, rows };

  client.on("ready", () => {
    client.shell(tty, (err, s) => {
      if (err) {
        onError(err);
        return;
      }
      stream = s;
      s.on("data", (d: Buffer) => onData(d.toString("utf8")));
      s.on("close", () => {
        if (closed) return;
        closed = true;
        onClose();
      });
      s.stderr.on("data", (d: Buffer) => onData(d.toString("utf8")));
    });
  });

  client.on("error", (err) => {
    onError(err);
  });

  client.on("close", () => {
    if (closed) return;
    closed = true;
    onClose();
  });

  client.connect(buildConnectConfig(conn));

  return {
    client,
    write: (data: string) => {
      try {
        stream?.write(data);
      } catch {
        /* noop */
      }
    },
    resize: (c: number, r: number) => {
      try {
        stream?.setWindow(r, c, 480, 640);
      } catch {
        /* noop */
      }
    },
    close: () => {
      if (closed) return;
      closed = true;
      try {
        stream?.close();
      } catch {
        /* noop */
      }
      try {
        client.end();
      } catch {
        /* noop */
      }
    },
  };
}

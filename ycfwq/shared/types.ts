// 共享类型定义 — 前后端公用

export type AuthType = "password" | "privateKey";

export interface ConnectionConfig {
  id: number;
  name: string;
  host: string;
  port: number;
  username: string;
  authType: AuthType;
  password?: string;
  privateKey?: string;
  passphrase?: string;
  lastConnectedAt?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * 列表/详情接口返回的连接对象，不含敏感字段
 */
export type ConnectionListItem = Omit<
  ConnectionConfig,
  "password" | "privateKey" | "passphrase"
>;

export interface ConnectionInput {
  name: string;
  host: string;
  port: number;
  username: string;
  authType: AuthType;
  password?: string;
  privateKey?: string;
  passphrase?: string;
}

export interface TestConnectionResult {
  ok: boolean;
  message: string;
}

export interface ExecResult {
  stdout: string;
  stderr: string;
  exitCode: number;
}

// WebSocket 终端协议
export type ClientMessage =
  | { type: "resize"; cols: number; rows: number }
  | { type: "input"; data: string };

export type ServerMessage =
  | { type: "ready" }
  | { type: "data"; data: string }
  | { type: "closed"; reason?: string }
  | { type: "error"; message: string };

// 系统监控数据结构
export interface MonitorMetrics {
  hostname: string;
  uptime: string;
  os: string;
  kernel: string;
  cpu: {
    usage: number;
    cores: number;
    model: string;
    loadAvg: [number, number, number];
  };
  memory: {
    total: number;
    used: number;
    available: number;
    usage: number;
  };
  disk: {
    total: number;
    used: number;
    available: number;
    usage: number;
    mount: string;
  }[];
  topProcesses: {
    pid: number;
    user: string;
    cpu: number;
    mem: number;
    command: string;
  }[];
}

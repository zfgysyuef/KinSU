/**
 * 后端 API 客户端
 */
import type {
  ConnectionInput,
  ConnectionListItem,
  ExecResult,
  MonitorMetrics,
  TestConnectionResult,
} from "@shared/types";

const BASE = "/api";

async function request<T>(
  path: string,
  init?: RequestInit,
): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...init,
  });
  const text = await res.text();
  let data: any;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text;
  }
  if (!res.ok) {
    const msg =
      (data && typeof data === "object" && (data.error || data.message)) ||
      `请求失败 (${res.status})`;
    throw new Error(msg as string);
  }
  return data as T;
}

export const api = {
  listConnections: () => request<ConnectionListItem[]>("/connections"),

  getConnection: (id: number) =>
    request<ConnectionListItem>(`/connections/${id}`),

  createConnection: (input: ConnectionInput) =>
    request<ConnectionListItem>("/connections", {
      method: "POST",
      body: JSON.stringify(input),
    }),

  updateConnection: (id: number, input: Partial<ConnectionInput>) =>
    request<ConnectionListItem>(`/connections/${id}`, {
      method: "PUT",
      body: JSON.stringify(input),
    }),

  deleteConnection: (id: number) =>
    request<{ ok: true }>(`/connections/${id}`, { method: "DELETE" }),

  testConnection: (id: number) =>
    request<TestConnectionResult>(`/connections/${id}/test`, {
      method: "POST",
    }),

  testDry: (input: ConnectionInput) =>
    request<TestConnectionResult>("/connections/test/dry", {
      method: "POST",
      body: JSON.stringify(input),
    }),

  execCommand: (id: number, command: string) =>
    request<ExecResult>(`/connections/${id}/exec`, {
      method: "POST",
      body: JSON.stringify({ command }),
    }),

  getMonitor: (id: number) =>
    request<MonitorMetrics>(`/connections/${id}/monitor`),

  health: () => request<{ success: boolean }>("/health"),
};

/**
 * 建立终端 WebSocket
 */
export function openTerminalSocket(connectionId: number): WebSocket {
  const proto = window.location.protocol === "https:" ? "wss:" : "ws:";
  return new WebSocket(
    `${proto}//${window.location.host}/ws/terminal/${connectionId}`,
  );
}

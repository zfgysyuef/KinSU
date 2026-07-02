/**
 * 连接配置服务 — JSON 文件存储 + 凭据加解密
 */
import {
  readAll,
  findById,
  insert,
  update,
  remove as removeRow,
  touchLastConnected,
  type StoredConnection,
} from "../store/jsonStore.js";
import { encrypt, decrypt } from "./crypto.js";
import type {
  ConnectionConfig,
  ConnectionInput,
  ConnectionListItem,
} from "@shared/types";

function rowToConfig(row: StoredConnection): ConnectionConfig {
  return {
    id: row.id,
    name: row.name,
    host: row.host,
    port: row.port,
    username: row.username,
    authType: row.authType,
    password: decrypt(row.password_enc) ?? undefined,
    privateKey: decrypt(row.private_key_enc) ?? undefined,
    passphrase: decrypt(row.passphrase_enc) ?? undefined,
    lastConnectedAt: row.lastConnectedAt ?? undefined,
    createdAt: row.createdAt,
    updatedAt: row.updatedAt,
  };
}

function toListItem(c: ConnectionConfig): ConnectionListItem {
  const { password: _p, privateKey: _k, passphrase: _pp, ...rest } = c;
  return rest;
}

export function listConnections(): ConnectionListItem[] {
  return readAll()
    .map(rowToConfig)
    .map(toListItem)
    .sort((a, b) => {
      const ta = a.lastConnectedAt || a.createdAt;
      const tb = b.lastConnectedAt || b.createdAt;
      return tb.localeCompare(ta);
    });
}

export function getConnection(id: number): ConnectionConfig | undefined {
  const row = findById(id);
  return row ? rowToConfig(row) : undefined;
}

export function getListItem(id: number): ConnectionListItem | undefined {
  const c = getConnection(id);
  return c ? toListItem(c) : undefined;
}

export function createConnection(input: ConnectionInput): ConnectionListItem {
  const row = insert({
    name: input.name,
    host: input.host,
    port: input.port,
    username: input.username,
    authType: input.authType,
    password_enc: encrypt(input.password),
    private_key_enc: encrypt(input.privateKey),
    passphrase_enc: encrypt(input.passphrase),
    lastConnectedAt: null,
  });
  return toListItem(rowToConfig(row));
}

export function updateConnection(
  id: number,
  input: Partial<ConnectionInput>,
): ConnectionListItem | undefined {
  const existing = getConnection(id);
  if (!existing) return undefined;

  const patch: Partial<StoredConnection> = {
    name: input.name ?? existing.name,
    host: input.host ?? existing.host,
    port: input.port ?? existing.port,
    username: input.username ?? existing.username,
    authType: input.authType ?? existing.authType,
  };

  // 敏感字段：仅当显式提供时更新
  if (input.password !== undefined) {
    patch.password_enc = encrypt(input.password);
  }
  if (input.privateKey !== undefined) {
    patch.private_key_enc = encrypt(input.privateKey);
  }
  if (input.passphrase !== undefined) {
    patch.passphrase_enc = encrypt(input.passphrase);
  }

  const row = update(id, patch);
  return row ? toListItem(rowToConfig(row)) : undefined;
}

export function deleteConnection(id: number): boolean {
  return removeRow(id);
}

export { touchLastConnected };

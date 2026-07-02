/**
 * JSON 文件存储 — 纯 JS 实现，避免原生模块编译依赖
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const dataDir = process.env.RK_DATA_DIR
  ? path.resolve(process.env.RK_DATA_DIR)
  : path.resolve(__dirname, "../../data");

const dbPath = path.join(dataDir, "connections.json");

export interface StoredConnection {
  id: number;
  name: string;
  host: string;
  port: number;
  username: string;
  authType: "password" | "privateKey";
  password_enc: string | null;
  private_key_enc: string | null;
  passphrase_enc: string | null;
  lastConnectedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

interface StoreShape {
  nextId: number;
  connections: StoredConnection[];
}

function ensureStore(): StoreShape {
  if (!fs.existsSync(dataDir)) {
    fs.mkdirSync(dataDir, { recursive: true });
  }
  if (!fs.existsSync(dbPath)) {
    const empty: StoreShape = { nextId: 1, connections: [] };
    fs.writeFileSync(dbPath, JSON.stringify(empty, null, 2), "utf8");
    return empty;
  }
  try {
    const raw = fs.readFileSync(dbPath, "utf8");
    const parsed = JSON.parse(raw) as StoreShape;
    if (!parsed.connections) parsed.connections = [];
    if (!parsed.nextId) parsed.nextId = 1;
    return parsed;
  } catch {
    return { nextId: 1, connections: [] };
  }
}

function save(store: StoreShape): void {
  if (!fs.existsSync(dataDir)) {
    fs.mkdirSync(dataDir, { recursive: true });
  }
  // 原子写：先写临时文件再重命名
  const tmp = dbPath + ".tmp";
  fs.writeFileSync(tmp, JSON.stringify(store, null, 2), "utf8");
  fs.renameSync(tmp, dbPath);
}

export function readAll(): StoredConnection[] {
  return ensureStore().connections;
}

export function findById(id: number): StoredConnection | undefined {
  return ensureStore().connections.find((c) => c.id === id);
}

export function insert(
  row: Omit<StoredConnection, "id" | "createdAt" | "updatedAt">,
): StoredConnection {
  const store = ensureStore();
  const now = new Date().toISOString();
  const item: StoredConnection = {
    ...row,
    id: store.nextId,
    createdAt: now,
    updatedAt: now,
  };
  store.connections.push(item);
  store.nextId += 1;
  save(store);
  return item;
}

export function update(
  id: number,
  patch: Partial<Omit<StoredConnection, "id" | "createdAt">>,
): StoredConnection | undefined {
  const store = ensureStore();
  const idx = store.connections.findIndex((c) => c.id === id);
  if (idx === -1) return undefined;
  const now = new Date().toISOString();
  store.connections[idx] = {
    ...store.connections[idx],
    ...patch,
    id,
    updatedAt: now,
  };
  save(store);
  return store.connections[idx];
}

export function remove(id: number): boolean {
  const store = ensureStore();
  const before = store.connections.length;
  store.connections = store.connections.filter((c) => c.id !== id);
  if (store.connections.length === before) return false;
  save(store);
  return true;
}

export function touchLastConnected(id: number): void {
  update(id, { lastConnectedAt: new Date().toISOString() });
}

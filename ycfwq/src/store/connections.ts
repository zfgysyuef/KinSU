/**
 * 连接状态管理
 */
import { create } from "zustand";
import { api } from "@/lib/api";
import type {
  ConnectionInput,
  ConnectionListItem,
  TestConnectionResult,
} from "@shared/types";

interface ConnectionsState {
  list: ConnectionListItem[];
  loading: boolean;
  error: string | null;
  backendOnline: boolean;

  fetchAll: () => Promise<void>;
  checkBackend: () => Promise<void>;
  create: (input: ConnectionInput) => Promise<ConnectionListItem>;
  update: (
    id: number,
    input: Partial<ConnectionInput>,
  ) => Promise<ConnectionListItem>;
  remove: (id: number) => Promise<void>;
  test: (id: number) => Promise<TestConnectionResult>;
  testDry: (input: ConnectionInput) => Promise<TestConnectionResult>;
}

export const useConnectionsStore = create<ConnectionsState>((set, get) => ({
  list: [],
  loading: false,
  error: null,
  backendOnline: false,

  fetchAll: async () => {
    set({ loading: true, error: null });
    try {
      const list = await api.listConnections();
      set({ list, loading: false });
    } catch (e: any) {
      set({ loading: false, error: e?.message || "加载失败" });
    }
  },

  checkBackend: async () => {
    try {
      await api.health();
      set({ backendOnline: true });
    } catch {
      set({ backendOnline: false });
    }
  },

  create: async (input) => {
    const item = await api.createConnection(input);
    set({ list: [item, ...get().list] });
    return item;
  },

  update: async (id, input) => {
    const item = await api.updateConnection(id, input);
    set({
      list: get().list.map((c) => (c.id === id ? item : c)),
    });
    return item;
  },

  remove: async (id) => {
    await api.deleteConnection(id);
    set({ list: get().list.filter((c) => c.id !== id) });
  },

  test: async (id) => api.testConnection(id),
  testDry: async (input) => api.testDry(input),
}));

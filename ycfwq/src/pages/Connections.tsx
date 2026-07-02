import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Plus,
  Search,
  TerminalSquare,
  Activity,
  Pencil,
  Trash2,
  Plug,
  Server,
  Loader2,
  Clock,
  KeyRound,
  Lock,
} from "lucide-react";
import { AppLayout, useBackendCheck } from "@/components/layout/AppLayout";
import { ConnectionFormDrawer } from "@/components/ConnectionFormDrawer";
import { Button } from "@/components/ui/Button";
import { StatusDot } from "@/components/ui/StatusDot";
import { useConnectionsStore } from "@/store/connections";
import { cn } from "@/lib/utils";
import type { ConnectionListItem } from "@shared/types";

export default function Connections() {
  useBackendCheck();
  const navigate = useNavigate();
  const { list, loading, remove, test } = useConnectionsStore();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editing, setEditing] = useState<ConnectionListItem | null>(null);
  const [keyword, setKeyword] = useState("");
  const [testingId, setTestingId] = useState<number | null>(null);
  const [testResults, setTestResults] = useState<Record<number, boolean>>({});
  const [confirmDelete, setConfirmDelete] = useState<number | null>(null);

  const filtered = useMemo(() => {
    const k = keyword.trim().toLowerCase();
    if (!k) return list;
    return list.filter(
      (c) =>
        c.name.toLowerCase().includes(k) ||
        c.host.toLowerCase().includes(k) ||
        c.username.toLowerCase().includes(k),
    );
  }, [list, keyword]);

  const openNew = () => {
    setEditing(null);
    setDrawerOpen(true);
  };
  const openEdit = (c: ConnectionListItem) => {
    setEditing(c);
    setDrawerOpen(true);
  };

  const handleTest = async (id: number) => {
    setTestingId(id);
    try {
      const r = await test(id);
      setTestResults((m) => ({ ...m, [id]: r.ok }));
    } catch {
      setTestResults((m) => ({ ...m, [id]: false }));
    } finally {
      setTestingId(null);
      setTimeout(() => {
        setTestResults((m) => {
          const n = { ...m };
          delete n[id];
          return n;
        });
      }, 3000);
    }
  };

  const handleDelete = async (id: number) => {
    await remove(id);
    setConfirmDelete(null);
  };

  return (
    <AppLayout
      topbar={
        <div className="flex items-center justify-end gap-3 px-4">
          <div className="relative">
            <Search className="pointer-events-none absolute left-2.5 top-1/2 h-4 w-4 -translate-y-1/2 text-kernel-muted" />
            <input
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="搜索主机 / 名称"
              className="h-9 w-56 rounded-md border border-kernel-border bg-kernel-bg/60 pl-8 pr-3 text-sm text-kernel-ink placeholder:text-kernel-muted/50 focus:border-amber/50 focus:outline-none"
            />
          </div>
          <Button variant="primary" size="sm" onClick={openNew}>
            <Plus className="h-4 w-4" />
            新建连接
          </Button>
        </div>
      }
    >
      <div className="kernel-grid min-h-full">
        <div className="mx-auto max-w-7xl px-6 py-8">
          {/* 标题区 */}
          <header className="mb-8 animate-fade-up">
            <div className="flex items-center gap-2 font-mono text-[10px] uppercase tracking-[0.3em] text-amber">
              <span className="h-px w-8 bg-amber/50" />
              SSH · connections
            </div>
            <h1 className="mt-3 font-display text-4xl font-bold tracking-tight text-kernel-ink">
              连接管理
            </h1>
            <p className="mt-2 max-w-2xl text-sm text-kernel-muted">
              管理你的远程服务器连接。输入主机地址、端口与认证凭据，即可在浏览器中打开终端或查看系统监控。
            </p>
          </header>

          {/* 状态条 */}
          {loading && list.length === 0 ? (
            <div className="flex h-64 items-center justify-center text-kernel-muted">
              <Loader2 className="h-6 w-6 animate-spin" />
            </div>
          ) : filtered.length === 0 ? (
            <EmptyState keyword={keyword} onNew={openNew} />
          ) : (
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
              {filtered.map((c, i) => (
                <ConnectionCard
                  key={c.id}
                  connection={c}
                  index={i}
                  testing={testingId === c.id}
                  testOk={testResults[c.id]}
                  onTerminal={() => navigate(`/terminal/${c.id}`)}
                  onMonitor={() => navigate(`/monitor/${c.id}`)}
                  onEdit={() => openEdit(c)}
                  onDelete={() => setConfirmDelete(c.id)}
                  onTest={() => handleTest(c.id)}
                  confirmDelete={confirmDelete === c.id}
                  onConfirmDelete={() => handleDelete(c.id)}
                  onCancelDelete={() => setConfirmDelete(null)}
                />
              ))}
            </div>
          )}
        </div>
      </div>

      <ConnectionFormDrawer
        open={drawerOpen}
        connection={editing}
        onClose={() => setDrawerOpen(false)}
      />
    </AppLayout>
  );
}

interface ConnectionCardProps {
  connection: ConnectionListItem;
  index: number;
  testing: boolean;
  testOk?: boolean;
  onTerminal: () => void;
  onMonitor: () => void;
  onEdit: () => void;
  onDelete: () => void;
  onTest: () => void;
  confirmDelete: boolean;
  onConfirmDelete: () => void;
  onCancelDelete: () => void;
}

function ConnectionCard({
  connection: c,
  index,
  testing,
  testOk,
  onTerminal,
  onMonitor,
  onEdit,
  onDelete,
  onTest,
  confirmDelete,
  onConfirmDelete,
  onCancelDelete,
}: ConnectionCardProps) {
  const last = c.lastConnectedAt
    ? new Date(c.lastConnectedAt.replace(" ", "T") + "Z").toLocaleString("zh-CN", {
        hour12: false,
      })
    : "—";

  return (
    <div
      className="group relative flex flex-col rounded-lg border border-kernel-border bg-kernel-panel/70 p-4 shadow-panel backdrop-blur transition-all duration-200 hover:-translate-y-0.5 hover:border-amber/40 animate-fade-up"
      style={{ animationDelay: `${Math.min(index * 50, 400)}ms`, opacity: 0 }}
    >
      {/* 卡片顶部 */}
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-2">
          <StatusDot
            status={testOk === undefined ? "idle" : testOk ? "online" : "offline"}
            pulse={false}
          />
          <div className="flex h-9 w-9 items-center justify-center rounded-md border border-kernel-border bg-kernel-bg/60">
            <Server className="h-4 w-4 text-amber" />
          </div>
        </div>
        <span
          className={cn(
            "flex items-center gap-1 rounded border px-2 py-0.5 font-mono text-[9px] uppercase tracking-wider",
            c.authType === "privateKey"
              ? "border-neon/40 text-neon"
              : "border-kernel-border text-kernel-muted",
          )}
        >
          {c.authType === "privateKey" ? (
            <KeyRound className="h-2.5 w-2.5" />
          ) : (
            <Lock className="h-2.5 w-2.5" />
          )}
          {c.authType === "privateKey" ? "KEY" : "PWD"}
        </span>
      </div>

      {/* 主信息 */}
      <div className="mt-3">
        <h3 className="truncate font-display text-base font-bold text-kernel-ink">
          {c.name}
        </h3>
        <div className="mt-1 font-mono text-xs text-kernel-muted">
          <span className="text-amber/80">{c.username}</span>
          <span className="text-kernel-muted/60">@</span>
          <span className="text-kernel-ink">{c.host}</span>
          <span className="text-kernel-muted/60">:{c.port}</span>
        </div>
      </div>

      {/* 上次连接 */}
      <div className="mt-3 flex items-center gap-1.5 font-mono text-[10px] text-kernel-muted/70">
        <Clock className="h-3 w-3" />
        上次连接：{last}
      </div>

      {/* 操作 */}
      <div className="mt-4 flex items-center gap-2 border-t border-kernel-border pt-3">
        <Button size="sm" variant="primary" className="flex-1" onClick={onTerminal}>
          <TerminalSquare className="h-3.5 w-3.5" />
          终端
        </Button>
        <Button size="sm" variant="neon" className="flex-1" onClick={onMonitor}>
          <Activity className="h-3.5 w-3.5" />
          监控
        </Button>
        <button
          onClick={onTest}
          disabled={testing}
          className="flex h-8 w-8 items-center justify-center rounded-md border border-kernel-border text-kernel-muted transition-colors hover:border-amber/50 hover:text-amber disabled:opacity-50"
          title="测试连接"
        >
          {testing ? (
            <Loader2 className="h-3.5 w-3.5 animate-spin" />
          ) : (
            <Plug className="h-3.5 w-3.5" />
          )}
        </button>
        <button
          onClick={onEdit}
          className="flex h-8 w-8 items-center justify-center rounded-md border border-kernel-border text-kernel-muted transition-colors hover:border-amber/50 hover:text-amber"
          title="编辑"
        >
          <Pencil className="h-3.5 w-3.5" />
        </button>
        <button
          onClick={onDelete}
          className="flex h-8 w-8 items-center justify-center rounded-md border border-kernel-border text-kernel-muted transition-colors hover:border-danger hover:text-danger"
          title="删除"
        >
          <Trash2 className="h-3.5 w-3.5" />
        </button>
      </div>

      {/* 删除确认 */}
      {confirmDelete && (
        <div className="absolute inset-0 z-10 flex flex-col items-center justify-center gap-3 rounded-lg border border-danger/50 bg-kernel-panel/95 p-4 text-center backdrop-blur">
          <p className="text-sm text-kernel-ink">确认删除此连接？</p>
          <p className="font-mono text-xs text-kernel-muted">{c.name}</p>
          <div className="flex gap-2">
            <Button size="sm" variant="danger" onClick={onConfirmDelete}>
              删除
            </Button>
            <Button size="sm" variant="ghost" onClick={onCancelDelete}>
              取消
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}

function EmptyState({
  keyword,
  onNew,
}: {
  keyword: string;
  onNew: () => void;
}) {
  return (
    <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-kernel-border bg-kernel-panel/40 py-20">
      <div className="relative mb-4">
        <div className="flex h-16 w-16 items-center justify-center rounded-full border border-amber/30 bg-amber/5">
          <TerminalSquare className="h-7 w-7 text-amber" />
        </div>
        <span className="absolute -right-1 -top-1 h-3 w-3 rounded-full bg-amber animate-breathe" />
      </div>
      <h3 className="font-display text-lg font-bold text-kernel-ink">
        {keyword ? "未找到匹配的连接" : "尚未添加任何服务器"}
      </h3>
      <p className="mt-2 max-w-sm text-center text-sm text-kernel-muted">
        {keyword
          ? "试试调整搜索关键词"
          : "点击下方按钮，添加你的第一台远程服务器连接。"}
      </p>
      {!keyword && (
        <Button variant="primary" className="mt-5" onClick={onNew}>
          <Plus className="h-4 w-4" />
          新建连接
        </Button>
      )}
    </div>
  );
}

import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, Plus, X, TerminalSquare, Loader2 } from "lucide-react";
import { TerminalSession, type SessionStatus } from "@/components/TerminalSession";
import { api } from "@/lib/api";
import { cn } from "@/lib/utils";
import type { ConnectionListItem } from "@shared/types";

interface Tab {
  id: string;
  status: SessionStatus;
}

let tabSeq = 0;
const newTabId = () => `tab-${++tabSeq}`;

export default function Terminal() {
  const { connectionId } = useParams<{ connectionId: string }>();
  const navigate = useNavigate();
  const [conn, setConn] = useState<ConnectionListItem | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [tabs, setTabs] = useState<Tab[]>(() => [
    { id: newTabId(), status: "connecting" },
  ]);
  const [activeId, setActiveId] = useState<string>(() => tabs[0].id);

  useEffect(() => {
    const id = parseInt(connectionId || "0", 10);
    if (!id) {
      setLoadError("无效的连接 ID");
      return;
    }
    api
      .getConnection(id)
      .then(setConn)
      .catch((e) => setLoadError(e?.message || "加载连接失败"));
  }, [connectionId]);

  const addTab = () => {
    const t: Tab = { id: newTabId(), status: "connecting" };
    setTabs((prev) => [...prev, t]);
    setActiveId(t.id);
  };

  const closeTab = (id: string) => {
    setTabs((prev) => {
      const next = prev.filter((t) => t.id !== id);
      if (next.length === 0) {
        navigate("/connections");
        return next;
      }
      if (id === activeId) {
        setActiveId(next[next.length - 1].id);
      }
      return next;
    });
  };

  const updateTabStatus = (id: string, status: SessionStatus) => {
    setTabs((prev) => prev.map((t) => (t.id === id ? { ...t, status } : t)));
  };

  const id = parseInt(connectionId || "0", 10);

  return (
    <div className="flex h-full w-full flex-col bg-kernel-bg">
      {/* 顶栏 */}
      <header className="flex h-12 shrink-0 items-center justify-between border-b border-kernel-border bg-kernel-panel/80 px-3 backdrop-blur">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate("/connections")}
            className="flex h-8 w-8 items-center justify-center rounded-md border border-kernel-border text-kernel-muted transition-colors hover:border-amber/50 hover:text-amber"
            title="返回连接管理"
          >
            <ArrowLeft className="h-4 w-4" />
          </button>
          <div className="flex items-center gap-2">
            <TerminalSquare className="h-4 w-4 text-amber" />
            {conn ? (
              <div className="flex items-center gap-2 font-mono text-xs">
                <span className="font-display font-bold text-kernel-ink">
                  {conn.name}
                </span>
                <span className="text-kernel-muted/60">·</span>
                <span className="text-amber/80">{conn.username}</span>
                <span className="text-kernel-muted/60">@</span>
                <span className="text-kernel-ink">{conn.host}</span>
                <span className="text-kernel-muted/60">:{conn.port}</span>
              </div>
            ) : loadError ? (
              <span className="font-mono text-xs text-danger">{loadError}</span>
            ) : (
              <Loader2 className="h-3.5 w-3.5 animate-spin text-kernel-muted" />
            )}
          </div>
        </div>
      </header>

      {/* 标签栏 */}
      <div className="flex h-9 shrink-0 items-center gap-1 border-b border-kernel-border bg-kernel-panel/40 px-2">
        {tabs.map((t, i) => (
          <button
            key={t.id}
            onClick={() => setActiveId(t.id)}
            className={cn(
              "group flex h-7 items-center gap-2 rounded-t border-b-2 px-3 font-mono text-[11px] transition-colors",
              t.id === activeId
                ? "border-amber bg-kernel-bg text-amber"
                : "border-transparent text-kernel-muted hover:text-kernel-ink",
            )}
          >
            <span className="text-kernel-muted/60">{i + 1}.</span>
            <span>shell</span>
            <TabStatusDot status={t.status} />
            <span
              role="button"
              tabIndex={-1}
              onClick={(e) => {
                e.stopPropagation();
                closeTab(t.id);
              }}
              className="ml-1 rounded p-0.5 text-kernel-muted/60 hover:bg-danger/10 hover:text-danger"
            >
              <X className="h-3 w-3" />
            </span>
          </button>
        ))}
        <button
          onClick={addTab}
          className="flex h-7 w-7 items-center justify-center rounded text-kernel-muted transition-colors hover:bg-kernel-raised hover:text-amber"
          title="新建标签"
        >
          <Plus className="h-4 w-4" />
        </button>
      </div>

      {/* 终端区域 */}
      <div className="relative min-h-0 flex-1">
        {conn && id > 0 ? (
          tabs.map((t) => (
            <div
              key={t.id}
              className={cn(
                "absolute inset-0",
                t.id === activeId ? "z-10" : "pointer-events-none invisible z-0",
              )}
            >
              <TerminalSession
                connectionId={id}
                title={`shell #${tabs.indexOf(t) + 1}`}
                subtitle={`${conn.username}@${conn.host}`}
                active={t.id === activeId}
                onStatusChange={(s) => updateTabStatus(t.id, s)}
              />
            </div>
          ))
        ) : (
          <div className="flex h-full items-center justify-center text-kernel-muted">
            {loadError ? (
              <span className="font-mono text-sm text-danger">{loadError}</span>
            ) : (
              <Loader2 className="h-6 w-6 animate-spin" />
            )}
          </div>
        )}
      </div>
    </div>
  );
}

function TabStatusDot({ status }: { status: SessionStatus }) {
  const color =
    status === "connected"
      ? "bg-neon"
      : status === "connecting"
        ? "bg-amber animate-breathe"
        : status === "error"
          ? "bg-danger"
          : "bg-kernel-muted";
  return <span className={cn("h-1.5 w-1.5 rounded-full", color)} />;
}

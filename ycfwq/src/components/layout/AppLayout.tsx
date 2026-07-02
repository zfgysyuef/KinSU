import { useEffect, useState } from "react";
import { PanelLeftClose, PanelLeftOpen } from "lucide-react";
import { Sidebar } from "./Sidebar";
import { cn } from "@/lib/utils";
import { useConnectionsStore } from "@/store/connections";

interface AppLayoutProps {
  children: React.ReactNode;
  /** 是否默认折叠侧边栏 */
  defaultCollapsed?: boolean;
  /** 顶部操作条 */
  topbar?: React.ReactNode;
}

export function AppLayout({
  children,
  defaultCollapsed = false,
  topbar,
}: AppLayoutProps) {
  const [collapsed, setCollapsed] = useState(defaultCollapsed);

  return (
    <div className="flex h-full w-full overflow-hidden">
      <Sidebar collapsed={collapsed} />
      <div className="flex min-w-0 flex-1 flex-col">
        <div className="flex h-16 shrink-0 items-center justify-between border-b border-kernel-border bg-kernel-panel/40 px-4 backdrop-blur">
          <button
            onClick={() => setCollapsed((v) => !v)}
            className="flex h-8 w-8 items-center justify-center rounded-md border border-kernel-border text-kernel-muted transition-colors hover:border-amber/50 hover:text-amber"
            aria-label="切换侧边栏"
          >
            {collapsed ? (
              <PanelLeftOpen className="h-4 w-4" />
            ) : (
              <PanelLeftClose className="h-4 w-4" />
            )}
          </button>
          <div className="min-w-0 flex-1">{topbar}</div>
        </div>
        <main className={cn("min-h-0 flex-1 overflow-auto")}>{children}</main>
      </div>
    </div>
  );
}

/**
 * 启动时检查后端状态
 */
export function useBackendCheck() {
  const checkBackend = useConnectionsStore((s) => s.checkBackend);
  const fetchAll = useConnectionsStore((s) => s.fetchAll);
  useEffect(() => {
    checkBackend();
    fetchAll();
    const t = setInterval(checkBackend, 15000);
    return () => clearInterval(t);
  }, [checkBackend, fetchAll]);
}

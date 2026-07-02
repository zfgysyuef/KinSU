import { NavLink } from "react-router-dom";
import { TerminalSquare, Server, Activity } from "lucide-react";
import { cn } from "@/lib/utils";
import { useConnectionsStore } from "@/store/connections";
import { StatusDot } from "@/components/ui/StatusDot";

interface SidebarProps {
  collapsed?: boolean;
}

export function Sidebar({ collapsed = false }: SidebarProps) {
  const backendOnline = useConnectionsStore((s) => s.backendOnline);
  const connectionCount = useConnectionsStore((s) => s.list.length);

  return (
    <aside
      className={cn(
        "flex h-full flex-col border-r border-kernel-border bg-kernel-panel/60 backdrop-blur",
        collapsed ? "w-16" : "w-60",
      )}
    >
      {/* Logo */}
      <div className="flex h-16 items-center gap-3 border-b border-kernel-border px-4">
        <div className="relative flex h-9 w-9 shrink-0 items-center justify-center rounded-md border border-amber/40 bg-amber/5">
          <TerminalSquare className="h-5 w-5 text-amber" />
          <span className="absolute -right-0.5 -top-0.5 h-2 w-2 rounded-full bg-amber animate-breathe" />
        </div>
        {!collapsed && (
          <div className="flex flex-col leading-none">
            <span className="font-display text-sm font-bold tracking-wider text-kernel-ink">
              REMOTE KERNEL
            </span>
            <span className="mt-0.5 font-mono text-[9px] uppercase tracking-[0.2em] text-kernel-muted">
              SSH · console
            </span>
          </div>
        )}
      </div>

      {/* 导航 */}
      <nav className="flex-1 space-y-1 p-3">
        <NavItem
          to="/connections"
          icon={<Server className="h-4 w-4" />}
          label="连接管理"
          collapsed={collapsed}
        />
        <NavItem
          to="/connections"
          icon={<Activity className="h-4 w-4" />}
          label="系统监控"
          collapsed={collapsed}
          end
        />
      </nav>

      {/* 底部状态 */}
      <div className="border-t border-kernel-border p-3">
        {!collapsed ? (
          <div className="space-y-2 rounded-md border border-kernel-border bg-kernel-bg/50 p-3">
            <div className="flex items-center justify-between">
              <span className="font-mono text-[10px] uppercase tracking-widest text-kernel-muted">
                后端
              </span>
              <StatusDot status={backendOnline ? "online" : "offline"} />
            </div>
            <div className="flex items-center justify-between">
              <span className="font-mono text-[10px] uppercase tracking-widest text-kernel-muted">
                连接数
              </span>
              <span className="font-mono text-xs text-amber">
                {String(connectionCount).padStart(2, "0")}
              </span>
            </div>
          </div>
        ) : (
          <div className="flex justify-center">
            <StatusDot status={backendOnline ? "online" : "offline"} />
          </div>
        )}
      </div>
    </aside>
  );
}

interface NavItemProps {
  to: string;
  icon: React.ReactNode;
  label: string;
  collapsed: boolean;
  end?: boolean;
}

function NavItem({ to, icon, label, collapsed, end }: NavItemProps) {
  return (
    <NavLink
      to={to}
      end={end}
      className={({ isActive }) =>
        cn(
          "flex items-center gap-3 rounded-md px-3 py-2 font-mono text-xs uppercase tracking-wider transition-colors",
          collapsed && "justify-center",
          isActive
            ? "bg-amber/10 text-amber shadow-inner-glow"
            : "text-kernel-muted hover:bg-kernel-raised hover:text-kernel-ink",
        )
      }
    >
      {icon}
      {!collapsed && <span>{label}</span>}
    </NavLink>
  );
}

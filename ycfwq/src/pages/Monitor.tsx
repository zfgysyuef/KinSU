import { useCallback, useEffect, useRef, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  RefreshCw,
  Cpu,
  MemoryStick,
  HardDrive,
  Activity,
  Loader2,
  AlertCircle,
  Clock,
  TerminalSquare,
} from "lucide-react";
import { AppLayout, useBackendCheck } from "@/components/layout/AppLayout";
import { RingProgress } from "@/components/ui/RingProgress";
import { Button } from "@/components/ui/Button";
import { api } from "@/lib/api";
import { cn } from "@/lib/utils";
import type { ConnectionListItem, MonitorMetrics } from "@shared/types";

type Interval = 0 | 5 | 10 | 30;

export default function Monitor() {
  useBackendCheck();
  const { connectionId } = useParams<{ connectionId: string }>();
  const navigate = useNavigate();
  const id = parseInt(connectionId || "0", 10);

  const [conn, setConn] = useState<ConnectionListItem | null>(null);
  const [metrics, setMetrics] = useState<MonitorMetrics | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);
  const [interval, setIntervalSec] = useState<Interval>(0);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const fetchMetrics = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const [c, m] = await Promise.all([
        conn ? Promise.resolve(conn) : api.getConnection(id),
        api.getMonitor(id),
      ]);
      setConn(c);
      setMetrics(m);
      setLastUpdated(new Date());
    } catch (e: any) {
      setError(e?.message || "采集失败");
    } finally {
      setLoading(false);
    }
  }, [id, conn]);

  useEffect(() => {
    fetchMetrics();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  // 自动刷新
  useEffect(() => {
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }
    if (interval > 0) {
      timerRef.current = setInterval(fetchMetrics, interval * 1000);
    }
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [interval, fetchMetrics]);

  return (
    <AppLayout
      topbar={
        <div className="flex items-center justify-between gap-3 px-4">
          <div className="flex items-center gap-3">
            <button
              onClick={() => navigate("/connections")}
              className="flex h-8 w-8 items-center justify-center rounded-md border border-kernel-border text-kernel-muted transition-colors hover:border-amber/50 hover:text-amber"
              title="返回"
            >
              <ArrowLeft className="h-4 w-4" />
            </button>
            <div className="flex items-center gap-2 font-mono text-xs">
              {conn && (
                <>
                  <Activity className="h-4 w-4 text-neon" />
                  <span className="font-display font-bold text-kernel-ink">
                    {conn.name}
                  </span>
                  <span className="text-kernel-muted/60">·</span>
                  <span className="text-amber/80">{conn.host}</span>
                </>
              )}
            </div>
          </div>
          <div className="flex items-center gap-3">
            {lastUpdated && (
              <span className="flex items-center gap-1 font-mono text-[10px] text-kernel-muted">
                <Clock className="h-3 w-3" />
                {lastUpdated.toLocaleTimeString("zh-CN", { hour12: false })}
              </span>
            )}
            <select
              value={interval}
              onChange={(e) => setIntervalSec(Number(e.target.value) as Interval)}
              className="h-8 rounded-md border border-kernel-border bg-kernel-bg/60 px-2 font-mono text-xs text-kernel-ink focus:border-amber/50 focus:outline-none"
            >
              <option value={0}>手动</option>
              <option value={5}>5s</option>
              <option value={10}>10s</option>
              <option value={30}>30s</option>
            </select>
            <Button
              size="sm"
              variant="neon"
              onClick={fetchMetrics}
              disabled={loading}
            >
              <RefreshCw className={cn("h-3.5 w-3.5", loading && "animate-spin")} />
              刷新
            </Button>
            <Button
              size="sm"
              variant="secondary"
              onClick={() => navigate(`/terminal/${id}`)}
            >
              <TerminalSquare className="h-3.5 w-3.5" />
              终端
            </Button>
          </div>
        </div>
      }
    >
      <div className="mx-auto max-w-7xl px-6 py-6">
        {error ? (
          <div className="flex flex-col items-center justify-center gap-3 rounded-lg border border-danger/40 bg-danger/5 py-20 text-center">
            <AlertCircle className="h-8 w-8 text-danger" />
            <p className="font-mono text-sm text-danger">{error}</p>
            <Button size="sm" variant="secondary" onClick={fetchMetrics}>
              <RefreshCw className="h-3.5 w-3.5" />
              重试
            </Button>
          </div>
        ) : !metrics ? (
          <div className="flex h-64 items-center justify-center text-kernel-muted">
            <Loader2 className="h-6 w-6 animate-spin" />
          </div>
        ) : (
          <div className="space-y-6">
            {/* 主机信息条 */}
            <div className="flex flex-wrap items-center gap-x-6 gap-y-2 rounded-lg border border-kernel-border bg-kernel-panel/60 px-5 py-3 font-mono text-xs">
              <Info label="主机名" value={metrics.hostname || "—"} />
              <Info label="系统" value={metrics.os || "—"} />
              <Info label="内核" value={metrics.kernel || "—"} />
              <Info label="运行时长" value={metrics.uptime || "—"} />
              <Info
                label="负载"
                value={metrics.cpu.loadAvg
                  .map((n) => n.toFixed(2))
                  .join(" / ")}
              />
            </div>

            {/* 指标卡片网格 */}
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
              <MetricCard
                icon={<Cpu className="h-4 w-4" />}
                title="CPU 使用率"
                accent="amber"
              >
                <RingProgress
                  value={metrics.cpu.usage}
                  label="CPU"
                  sublabel={`${metrics.cpu.cores} 核`}
                  color="amber"
                />
                <div className="mt-2 text-center font-mono text-[10px] text-kernel-muted">
                  {metrics.cpu.model}
                </div>
              </MetricCard>

              <MetricCard
                icon={<MemoryStick className="h-4 w-4" />}
                title="内存使用率"
                accent="neon"
              >
                <RingProgress
                  value={metrics.memory.usage}
                  label="MEM"
                  sublabel={`${formatBytes(metrics.memory.used)} / ${formatBytes(
                    metrics.memory.total,
                  )}`}
                  color="neon"
                />
                <div className="mt-2 text-center font-mono text-[10px] text-kernel-muted">
                  可用 {formatBytes(metrics.memory.available)}
                </div>
              </MetricCard>

              <MetricCard
                icon={<HardDrive className="h-4 w-4" />}
                title="根分区使用率"
                accent="amber"
              >
                {(() => {
                  const root =
                    metrics.disk.find((d) => d.mount === "/") ||
                    metrics.disk[0];
                  return root ? (
                    <>
                      <RingProgress
                        value={root.usage}
                        label="DISK"
                        sublabel={`${formatBytes(root.used)} / ${formatBytes(
                          root.total,
                        )}`}
                        color={root.usage >= 90 ? "danger" : "amber"}
                      />
                      <div className="mt-2 text-center font-mono text-[10px] text-kernel-muted">
                        挂载 {root.mount} · 剩余 {formatBytes(root.available)}
                      </div>
                    </>
                  ) : (
                    <div className="text-kernel-muted">无数据</div>
                  );
                })()}
              </MetricCard>

              <MetricCard
                icon={<Activity className="h-4 w-4" />}
                title="系统负载（1/5/15min）"
                accent="neon"
              >
                <div className="flex h-[140px] flex-col items-center justify-center gap-2">
                  {metrics.cpu.loadAvg.map((v, i) => (
                    <div
                      key={i}
                      className="flex w-full items-center justify-between px-6"
                    >
                      <span className="font-mono text-[10px] uppercase tracking-widest text-kernel-muted">
                        {["1m", "5m", "15m"][i]}
                      </span>
                      <span
                        className={cn(
                          "font-mono text-2xl font-bold",
                          v > metrics.cpu.cores
                            ? "text-danger"
                            : v > metrics.cpu.cores * 0.7
                              ? "text-amber"
                              : "text-neon",
                        )}
                      >
                        {v.toFixed(2)}
                      </span>
                    </div>
                  ))}
                </div>
              </MetricCard>
            </div>

            {/* 所有磁盘挂载 */}
            {metrics.disk.length > 0 && (
              <Panel title="磁盘挂载">
                <div className="space-y-2">
                  {metrics.disk.map((d, i) => (
                    <div key={i} className="flex items-center gap-3">
                      <span className="w-32 shrink-0 truncate font-mono text-xs text-kernel-ink">
                        {d.mount}
                      </span>
                      <div className="h-2 flex-1 overflow-hidden rounded-full bg-kernel-bg">
                        <div
                          className={cn(
                            "h-full rounded-full transition-all",
                            d.usage >= 90
                              ? "bg-danger"
                              : d.usage >= 70
                                ? "bg-amber"
                                : "bg-neon",
                          )}
                          style={{ width: `${Math.min(100, d.usage)}%` }}
                        />
                      </div>
                      <span className="w-40 shrink-0 text-right font-mono text-[10px] text-kernel-muted">
                        {formatBytes(d.used)} / {formatBytes(d.total)} (
                        {d.usage.toFixed(1)}%)
                      </span>
                    </div>
                  ))}
                </div>
              </Panel>
            )}

            {/* 进程列表 */}
            <Panel title="Top 进程（按 CPU 排序）">
              <div className="overflow-x-auto">
                <table className="w-full font-mono text-xs">
                  <thead>
                    <tr className="border-b border-kernel-border text-left text-[10px] uppercase tracking-widest text-kernel-muted">
                      <th className="px-3 py-2">PID</th>
                      <th className="px-3 py-2">用户</th>
                      <th className="px-3 py-2 text-right">CPU%</th>
                      <th className="px-3 py-2 text-right">MEM%</th>
                      <th className="px-3 py-2">命令</th>
                    </tr>
                  </thead>
                  <tbody>
                    {metrics.topProcesses.map((p, i) => (
                      <tr
                        key={i}
                        className="border-b border-kernel-border/50 transition-colors hover:bg-kernel-raised/50"
                      >
                        <td className="px-3 py-1.5 text-kernel-muted">{p.pid}</td>
                        <td className="px-3 py-1.5 text-kernel-ink">{p.user}</td>
                        <td
                          className={cn(
                            "px-3 py-1.5 text-right",
                            p.cpu > 50
                              ? "text-danger"
                              : p.cpu > 20
                                ? "text-amber"
                                : "text-neon",
                          )}
                        >
                          {p.cpu.toFixed(1)}
                        </td>
                        <td className="px-3 py-1.5 text-right text-kernel-ink">
                          {p.mem.toFixed(1)}
                        </td>
                        <td className="max-w-md truncate px-3 py-1.5 text-kernel-ink/80">
                          {p.command}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </Panel>
          </div>
        )}
      </div>
    </AppLayout>
  );
}

function MetricCard({
  icon,
  title,
  accent,
  children,
}: {
  icon: React.ReactNode;
  title: string;
  accent: "amber" | "neon";
  children: React.ReactNode;
}) {
  return (
    <div className="flex flex-col items-center rounded-lg border border-kernel-border bg-kernel-panel/60 p-5 shadow-panel animate-fade-up">
      <div className="mb-3 flex w-full items-center gap-2 border-b border-kernel-border pb-3">
        <span
          className={cn(
            "flex h-7 w-7 items-center justify-center rounded-md border",
            accent === "amber"
              ? "border-amber/40 text-amber"
              : "border-neon/40 text-neon",
          )}
        >
          {icon}
        </span>
        <span className="font-mono text-[10px] uppercase tracking-widest text-kernel-muted">
          {title}
        </span>
      </div>
      {children}
    </div>
  );
}

function Panel({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <div className="rounded-lg border border-kernel-border bg-kernel-panel/60 p-5 shadow-panel">
      <h3 className="mb-4 flex items-center gap-2 font-mono text-[10px] uppercase tracking-widest text-amber">
        <span className="h-px w-6 bg-amber/50" />
        {title}
      </h3>
      {children}
    </div>
  );
}

function Info({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center gap-2">
      <span className="text-[10px] uppercase tracking-widest text-kernel-muted">
        {label}
      </span>
      <span className="text-kernel-ink">{value}</span>
    </div>
  );
}

function formatBytes(bytes: number): string {
  if (!bytes || bytes <= 0) return "0 B";
  const units = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  const v = bytes / Math.pow(1024, i);
  return `${v.toFixed(i === 0 ? 0 : 1)} ${units[i]}`;
}

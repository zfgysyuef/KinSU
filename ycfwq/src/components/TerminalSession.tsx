import { useEffect, useRef, useState } from "react";
import { Terminal as XTerm } from "@xterm/xterm";
import { FitAddon } from "@xterm/addon-fit";
import { WebLinksAddon } from "@xterm/addon-web-links";
import "@xterm/xterm/css/xterm.css";
import { Loader2, WifiOff, AlertTriangle } from "lucide-react";
import { openTerminalSocket } from "@/lib/api";
import type { ServerMessage } from "@shared/types";
import { cn } from "@/lib/utils";

export type SessionStatus = "connecting" | "connected" | "closed" | "error";

interface TerminalSessionProps {
  connectionId: number;
  title: string;
  subtitle: string;
  active: boolean;
  onStatusChange: (status: SessionStatus) => void;
}

export function TerminalSession({
  connectionId,
  title,
  subtitle,
  active,
  onStatusChange,
}: TerminalSessionProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const termRef = useRef<XTerm | null>(null);
  const fitRef = useRef<FitAddon | null>(null);
  const wsRef = useRef<WebSocket | null>(null);
  const [status, setStatus] = useState<SessionStatus>("connecting");
  const [elapsed, setElapsed] = useState(0);
  const [booting, setBooting] = useState(true);

  // 初始化终端（仅一次）
  useEffect(() => {
    if (!containerRef.current) return;
    const term = new XTerm({
      cursorBlink: true,
      cursorStyle: "bar",
      fontFamily: '"JetBrains Mono", monospace',
      fontSize: 14,
      lineHeight: 1.2,
      allowProposedApi: true,
      theme: {
        background: "#0a0e14",
        foreground: "#c8d0de",
        cursor: "#f5a623",
        cursorAccent: "#0a0e14",
        selectionBackground: "rgba(245,166,35,0.25)",
        black: "#0a0e14",
        red: "#ff5c57",
        green: "#3dd68c",
        yellow: "#f5a623",
        blue: "#4a9eff",
        magenta: "#c678dd",
        cyan: "#56b6c2",
        white: "#c8d0de",
        brightBlack: "#6b7689",
        brightRed: "#ff8c88",
        brightGreen: "#5be69b",
        brightYellow: "#ffb84d",
        brightBlue: "#6fb6ff",
        brightMagenta: "#d894e8",
        brightCyan: "#7fd0d8",
        brightWhite: "#ffffff",
      },
    });
    const fit = new FitAddon();
    term.loadAddon(fit);
    term.loadAddon(new WebLinksAddon());
    term.open(containerRef.current);
    termRef.current = term;
    fitRef.current = fit;

    try {
      fit.fit();
    } catch {
      /* noop */
    }

    // 启动扫描线动画
    const bootTimer = setTimeout(() => setBooting(false), 2200);

    return () => {
      clearTimeout(bootTimer);
      try {
        wsRef.current?.close();
      } catch {
        /* noop */
      }
      term.dispose();
      termRef.current = null;
    };
  }, [connectionId]);

  // 建立 WebSocket
  useEffect(() => {
    const term = termRef.current;
    if (!term) return;

    const ws = openTerminalSocket(connectionId);
    wsRef.current = ws;

    const updateStatus = (s: SessionStatus) => {
      setStatus(s);
      onStatusChange(s);
    };

    updateStatus("connecting");
    term.write("\r\n\x1b[33m⟡ 正在建立到远程主机的 SSH 通道...\x1b[0m\r\n");

    ws.onopen = () => {
      // 发送初始 resize
      try {
        fitRef.current?.fit();
      } catch {
        /* noop */
      }
      const cols = term.cols || 80;
      const rows = term.rows || 24;
      ws.send(JSON.stringify({ type: "resize", cols, rows }));
    };

    ws.onmessage = (ev) => {
      let msg: ServerMessage;
      try {
        msg = JSON.parse(ev.data);
      } catch {
        return;
      }
      switch (msg.type) {
        case "ready":
          updateStatus("connected");
          term.write("\r\n\x1b[32m✔ SSH 通道已就绪\x1b[0m\r\n\r\n");
          break;
        case "data":
          term.write(msg.data);
          break;
        case "error":
          updateStatus("error");
          term.write(`\r\n\x1b[31m✘ ${msg.message}\x1b[0m\r\n`);
          break;
        case "closed":
          updateStatus("closed");
          term.write(`\r\n\x1b[33m⟡ 连接已关闭${msg.reason ? `（${msg.reason}）` : ""}\x1b[0m\r\n`);
          break;
      }
    };

    ws.onerror = () => {
      updateStatus("error");
      term.write("\r\n\x1b[31m✘ WebSocket 错误\x1b[0m\r\n");
    };

    ws.onclose = () => {
      if (status !== "error") {
        updateStatus("closed");
      }
    };

    // 用户输入
    const disposable = term.onData((data) => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: "input", data }));
      }
    });

    return () => {
      disposable.dispose();
      try {
        ws.close();
      } catch {
        /* noop */
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [connectionId]);

  // 计时
  useEffect(() => {
    if (status !== "connected") {
      setElapsed(0);
      return;
    }
    const start = Date.now();
    const t = setInterval(() => setElapsed(Math.floor((Date.now() - start) / 1000)), 1000);
    return () => clearInterval(t);
  }, [status]);

  // 活动时重新 fit
  useEffect(() => {
    if (active) {
      const t = setTimeout(() => {
        try {
          fitRef.current?.fit();
        } catch {
          /* noop */
        }
      }, 50);
      return () => clearTimeout(t);
    }
  }, [active]);

  // 窗口尺寸变化
  useEffect(() => {
    if (!active) return;
    const onResize = () => {
      const fit = fitRef.current;
      const term = termRef.current;
      const ws = wsRef.current;
      if (!fit || !term || !ws) return;
      try {
        fit.fit();
      } catch {
        /* noop */
      }
      if (ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: "resize", cols: term.cols, rows: term.rows }));
      }
    };
    window.addEventListener("resize", onResize);
    return () => window.removeEventListener("resize", onResize);
  }, [active]);

  return (
    <div className="relative flex h-full flex-col">
      {/* 信息条 */}
      <div className="flex h-9 shrink-0 items-center justify-between border-b border-kernel-border bg-kernel-panel/60 px-3 font-mono text-[10px] uppercase tracking-widest text-kernel-muted">
        <div className="flex items-center gap-2">
          <span className="text-amber">{title}</span>
          <span className="text-kernel-muted/50">·</span>
          <span>{subtitle}</span>
        </div>
        <div className="flex items-center gap-3">
          <span>
            {status === "connected" ? formatElapsed(elapsed) : "—"}
          </span>
          <StatusBadge status={status} />
        </div>
      </div>

      {/* 终端容器 */}
      <div className="relative min-h-0 flex-1 bg-kernel-bg">
        {booting && (
          <div className="pointer-events-none absolute inset-0 z-10 overflow-hidden">
            <div className="absolute left-0 right-0 h-px animate-scanline bg-gradient-to-r from-transparent via-amber to-transparent" />
          </div>
        )}
        <div ref={containerRef} className="xterm-wrapper" />
        {(status === "closed" || status === "error") && (
          <div className="absolute inset-0 z-20 flex items-center justify-center bg-kernel-bg/80 backdrop-blur-sm">
            <div className="flex flex-col items-center gap-3 text-center">
              {status === "error" ? (
                <AlertTriangle className="h-8 w-8 text-danger" />
              ) : (
                <WifiOff className="h-8 w-8 text-kernel-muted" />
              )}
              <p className="font-mono text-sm text-kernel-ink">
                {status === "error" ? "连接发生错误" : "会话已断开"}
              </p>
              <button
                onClick={() => window.location.reload()}
                className="rounded-md border border-amber/50 px-4 py-1.5 font-mono text-xs uppercase tracking-wider text-amber transition-colors hover:bg-amber/10"
              >
                重新连接
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

function StatusBadge({ status }: { status: SessionStatus }) {
  const map = {
    connecting: { text: "连接中", icon: <Loader2 className="h-3 w-3 animate-spin" />, cls: "text-amber border-amber/40" },
    connected: { text: "在线", icon: <span className="h-1.5 w-1.5 rounded-full bg-neon" />, cls: "text-neon border-neon/40" },
    closed: { text: "已断开", icon: <WifiOff className="h-3 w-3" />, cls: "text-kernel-muted border-kernel-border" },
    error: { text: "错误", icon: <AlertTriangle className="h-3 w-3" />, cls: "text-danger border-danger/40" },
  };
  const m = map[status];
  return (
    <span
      className={cn(
        "flex items-center gap-1 rounded border px-2 py-0.5 text-[9px]",
        m.cls,
      )}
    >
      {m.icon}
      {m.text}
    </span>
  );
}

function formatElapsed(s: number): string {
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  const sec = s % 60;
  const pad = (n: number) => String(n).padStart(2, "0");
  return h > 0 ? `${pad(h)}:${pad(m)}:${pad(sec)}` : `${pad(m)}:${pad(sec)}`;
}

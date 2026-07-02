import { cn } from "@/lib/utils";

interface RingProgressProps {
  value: number; // 0-100
  size?: number;
  stroke?: number;
  label?: string;
  sublabel?: string;
  color?: "amber" | "neon" | "danger";
  className?: string;
}

const colorMap = {
  amber: { stroke: "#f5a623", glow: "rgba(245,166,35,0.4)" },
  neon: { stroke: "#3dd68c", glow: "rgba(61,214,140,0.4)" },
  danger: { stroke: "#ff5c57", glow: "rgba(255,92,87,0.4)" },
};

export function RingProgress({
  value,
  size = 140,
  stroke = 10,
  label,
  sublabel,
  color = "amber",
  className,
}: RingProgressProps) {
  const radius = (size - stroke) / 2;
  const circumference = 2 * Math.PI * radius;
  const clamped = Math.max(0, Math.min(100, value));
  const offset = circumference - (clamped / 100) * circumference;
  const c = colorMap[color];

  const autoColor: typeof color =
    clamped >= 90 ? "danger" : clamped >= 70 ? "amber" : color;
  const ac = colorMap[autoColor];

  return (
    <div
      className={cn("relative inline-flex items-center justify-center", className)}
      style={{ width: size, height: size }}
    >
      <svg width={size} height={size} className="-rotate-90">
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke="#1f2937"
          strokeWidth={stroke}
        />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={ac.stroke}
          strokeWidth={stroke}
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          strokeLinecap="round"
          style={{
            transition: "stroke-dashoffset 0.6s ease, stroke 0.3s ease",
            filter: `drop-shadow(0 0 6px ${ac.glow})`,
          }}
        />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        <span
          className="font-mono font-bold leading-none"
          style={{ fontSize: size * 0.22, color: ac.stroke }}
        >
          {clamped.toFixed(clamped < 10 ? 1 : 0)}
          <span style={{ fontSize: size * 0.1 }}>%</span>
        </span>
        {label && (
          <span className="mt-1 text-[10px] uppercase tracking-widest text-kernel-muted">
            {label}
          </span>
        )}
        {sublabel && (
          <span className="mt-0.5 font-mono text-[10px] text-kernel-ink/60">
            {sublabel}
          </span>
        )}
      </div>
      {/* 保留 c 引用避免未使用警告 */}
      <span className="hidden">{c.stroke}</span>
    </div>
  );
}

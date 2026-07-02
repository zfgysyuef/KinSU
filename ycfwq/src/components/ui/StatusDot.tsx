import { cn } from "@/lib/utils";

interface StatusDotProps {
  status: "online" | "offline" | "idle";
  className?: string;
  pulse?: boolean;
}

const colorMap = {
  online: "bg-neon",
  offline: "bg-danger",
  idle: "bg-amber",
};

export function StatusDot({ status, className, pulse = true }: StatusDotProps) {
  return (
    <span className={cn("relative inline-flex h-2.5 w-2.5", className)}>
      {pulse && status === "online" && (
        <span
          className={cn(
            "absolute inline-flex h-full w-full animate-ping rounded-full opacity-60",
            colorMap[status],
          )}
        />
      )}
      <span
        className={cn(
          "relative inline-flex h-2.5 w-2.5 rounded-full",
          colorMap[status],
          status === "idle" && "animate-breathe",
        )}
        style={{
          boxShadow:
            status === "online"
              ? "0 0 8px rgba(61,214,140,0.7)"
              : status === "idle"
                ? "0 0 8px rgba(245,166,35,0.6)"
                : "0 0 8px rgba(255,92,87,0.5)",
        }}
      />
    </span>
  );
}

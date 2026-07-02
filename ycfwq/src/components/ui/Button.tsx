import { forwardRef, type ButtonHTMLAttributes } from "react";
import { cn } from "@/lib/utils";

type Variant = "primary" | "secondary" | "danger" | "ghost" | "neon";
type Size = "sm" | "md" | "lg";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  size?: Size;
}

const variantClasses: Record<Variant, string> = {
  primary:
    "bg-amber text-kernel-bg font-semibold border border-amber hover:bg-amber-glow hover:shadow-amber-glow",
  secondary:
    "bg-transparent text-kernel-ink border border-kernel-border hover:border-amber/60 hover:text-amber",
  danger:
    "bg-transparent text-danger border border-danger/50 hover:bg-danger/10 hover:border-danger",
  ghost:
    "bg-transparent text-kernel-muted border border-transparent hover:text-kernel-ink hover:bg-kernel-raised",
  neon:
    "bg-transparent text-neon border border-neon/50 hover:bg-neon/10 hover:border-neon hover:shadow-[0_0_0_1px_rgba(61,214,140,0.4),0_0_24px_rgba(61,214,140,0.15)]",
};

const sizeClasses: Record<Size, string> = {
  sm: "h-8 px-3 text-xs",
  md: "h-10 px-4 text-sm",
  lg: "h-12 px-6 text-base",
};

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = "secondary", size = "md", ...props }, ref) => {
    return (
      <button
        ref={ref}
        className={cn(
          "inline-flex items-center justify-center gap-2 rounded-md font-mono uppercase tracking-wider transition-all duration-150 focus:outline-none focus-visible:ring-2 focus-visible:ring-amber/50 disabled:cursor-not-allowed disabled:opacity-40",
          variantClasses[variant],
          sizeClasses[size],
          className,
        )}
        {...props}
      />
    );
  },
);
Button.displayName = "Button";

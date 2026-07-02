/** @type {import('tailwindcss').Config} */

export default {
  darkMode: "class",
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    container: {
      center: true,
    },
    extend: {
      colors: {
        // 工业控制台深色基底
        kernel: {
          bg: "#0a0e14",
          panel: "#11161f",
          raised: "#161c28",
          border: "#1f2937",
          muted: "#6b7689",
          ink: "#c8d0de",
        },
        // 强调色
        amber: {
          DEFAULT: "#f5a623",
          glow: "#ffb84d",
          dim: "#8a5e1a",
        },
        neon: {
          DEFAULT: "#3dd68c",
          dim: "#1f7a4f",
        },
        danger: {
          DEFAULT: "#ff5c57",
          dim: "#7a2a28",
        },
      },
      fontFamily: {
        display: ['"Space Mono"', "monospace"],
        mono: ['"JetBrains Mono"', "ui-monospace", "monospace"],
        sans: ['"IBM Plex Sans"', "system-ui", "sans-serif"],
      },
      boxShadow: {
        "inner-glow": "inset 0 0 0 1px rgba(245,166,35,0.25), inset 0 0 24px rgba(245,166,35,0.06)",
        "panel": "0 1px 0 rgba(255,255,255,0.03), 0 8px 32px rgba(0,0,0,0.4)",
        "amber-glow": "0 0 0 1px rgba(245,166,35,0.4), 0 0 24px rgba(245,166,35,0.15)",
      },
      keyframes: {
        "scanline": {
          "0%": { transform: "translateY(-100%)" },
          "100%": { transform: "translateY(100vh)" },
        },
        "breathe": {
          "0%, 100%": { opacity: "1" },
          "50%": { opacity: "0.4" },
        },
        "fade-up": {
          "0%": { opacity: "0", transform: "translateY(8px)" },
          "100%": { opacity: "1", transform: "translateY(0)" },
        },
      },
      animation: {
        "scanline": "scanline 2.2s ease-out forwards",
        "breathe": "breathe 2s ease-in-out infinite",
        "fade-up": "fade-up 0.5s ease-out forwards",
      },
    },
  },
  plugins: [],
};

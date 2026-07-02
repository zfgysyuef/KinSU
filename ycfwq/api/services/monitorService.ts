/**
 * 系统监控采集与解析
 * 通过 SSH 执行一段合并脚本，一次性拿到系统信息，再在服务端解析
 */
import { execOnce } from "./sshService.js";
import type { ConnectionConfig, MonitorMetrics } from "@shared/types";

// 合并为一次 SSH exec 调用，减少往返
const COLLECT_SCRIPT = `cat /etc/hostname 2>/dev/null; echo "|||"; uptime -p 2>/dev/null || uptime; echo "|||"; (cat /etc/os-release 2>/dev/null | grep PRETTY_NAME | cut -d= -f2 | tr -d '"') || uname -s; echo "|||"; uname -r; echo "|||"; grep -c ^processor /proc/cpuinfo; echo "|||"; grep -m1 "model name" /proc/cpuinfo | cut -d: -f2 | sed 's/^ //'; echo "|||"; cat /proc/loadavg; echo "|||"; head -n 1 /proc/stat; echo "|||"; free -b | awk 'NR==2{print $2":"$3":"$7}'; echo "|||"; df -B1 -P 2>/dev/null | awk 'NR>1 && $2 ~ /^[0-9]+$/ {print $2":"$3":"$4":"$6}'; echo "|||END|||"; sleep 0.5; head -n 1 /proc/stat; echo "|||"; ps -eo pid,user,%cpu,%mem,comm --sort=-%cpu 2>/dev/null | head -n 11`;

function parseSection(raw: string[]): string {
  return raw.map((l) => l.trim()).join("\n");
}

export async function collectMetrics(
  conn: ConnectionConfig,
): Promise<MonitorMetrics> {
  const result = await execOnce(conn, COLLECT_SCRIPT, 25000);
  if (result.exitCode !== 0 && !result.stdout) {
    throw new Error(result.stderr || "采集失败");
  }

  const output = result.stdout;
  // 用 "|||END|||" 分隔前后两段（采集 CPU 占用需要两次 /proc/stat 采样）
  const [mainPart, afterEnd] = output.split("|||END|||");
  const sections = (mainPart || output)
    .split("|||")
    .map((s) => s.split("\n").filter((l) => l.trim() !== ""));

  const hostname = parseSection(sections[0] || []);
  const uptime = parseSection(sections[1] || []);
  const os = parseSection(sections[2] || []);
  const kernel = parseSection(sections[3] || []);
  const cores = parseInt(parseSection(sections[4] || ["1"]), 10) || 1;
  const cpuModel = parseSection(sections[5] || []);
  const loadavgRaw = parseSection(sections[6] || ["0 0 0"]).split(/\s+/);
  const loadAvg: [number, number, number] = [
    parseFloat(loadavgRaw[0]) || 0,
    parseFloat(loadavgRaw[1]) || 0,
    parseFloat(loadavgRaw[2]) || 0,
  ];
  const stat1 = parseSection(sections[7] || []);
  const memRaw = parseSection(sections[8] || ["0:0:0"]).split(":");
  const memTotal = parseInt(memRaw[0] || "0", 10) || 0;
  const memUsed = parseInt(memRaw[1] || "0", 10) || 0;
  const memAvailable = parseInt(memRaw[2] || "0", 10) || 0;
  const memUsage = memTotal > 0 ? (memUsed / memTotal) * 100 : 0;

  // 磁盘
  const diskLines = sections[9] || [];
  const disk = diskLines.map((line) => {
    const [total, used, avail, mount] = line.split(":");
    const t = parseInt(total, 10) || 0;
    const u = parseInt(used, 10) || 0;
    const a = parseInt(avail, 10) || 0;
    return {
      total: t,
      used: u,
      available: a,
      usage: t > 0 ? (u / t) * 100 : 0,
      mount,
    };
  });

  // CPU 使用率：用两次 /proc/stat 采样
  let cpuUsage = 0;
  const afterSections = (afterEnd || "")
    .split("|||")
    .map((s) => s.split("\n").filter((l) => l.trim() !== ""));
  const stat2 = parseSection(afterSections[0] || []);
  cpuUsage = computeCpuUsage(stat1, stat2);

  // 进程列表
  const procLines = (afterSections[1] || []).slice(1); // 去掉表头
  const topProcesses = procLines.map((line) => {
    const parts = line.trim().split(/\s+/);
    return {
      pid: parseInt(parts[0], 10) || 0,
      user: parts[1] || "",
      cpu: parseFloat(parts[2]) || 0,
      mem: parseFloat(parts[3]) || 0,
      command: parts.slice(4).join(" "),
    };
  });

  return {
    hostname,
    uptime,
    os,
    kernel,
    cpu: {
      usage: cpuUsage,
      cores,
      model: cpuModel,
      loadAvg,
    },
    memory: {
      total: memTotal,
      used: memUsed,
      available: memAvailable,
      usage: memUsage,
    },
    disk,
    topProcesses,
  };
}

function computeCpuUsage(stat1: string, stat2: string): number {
  const parse = (line: string) => {
    const parts = line.split(/\s+/);
    // 格式: cpu user nice system idle iowait irq softirq steal ...
    const nums = parts.slice(1).map((n) => parseInt(n, 10) || 0);
    const idle = (nums[3] || 0) + (nums[4] || 0);
    const total = nums.reduce((a, b) => a + b, 0);
    return { idle, total };
  };
  try {
    const a = parse(stat1);
    const b = parse(stat2);
    const totalDiff = b.total - a.total;
    const idleDiff = b.idle - a.idle;
    if (totalDiff <= 0) return 0;
    return Math.max(0, Math.min(100, ((totalDiff - idleDiff) / totalDiff) * 100));
  } catch {
    return 0;
  }
}

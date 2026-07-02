/**
 * AES-256-GCM 加解密 — 用于保护连接凭据
 * 密钥从环境变量 RK_ENCRYPTION_KEY 派生；未设置时使用开发默认值并告警
 */
import crypto from "crypto";

const RAW_KEY =
  process.env.RK_ENCRYPTION_KEY ||
  "remote-kernel-dev-key-please-change-in-production";

// 派生 32 字节密钥
const KEY = crypto.createHash("sha256").update(RAW_KEY).digest();

if (!process.env.RK_ENCRYPTION_KEY) {
  // 仅警告一次
  console.warn(
    "[crypto] RK_ENCRYPTION_KEY 未设置，使用开发默认密钥。生产环境请通过环境变量注入。",
  );
}

export interface EncryptedPayload {
  iv: string;
  tag: string;
  ct: string;
}

export function encrypt(plain: string | undefined | null): string | null {
  if (plain === undefined || plain === null || plain === "") return null;
  const iv = crypto.randomBytes(12);
  const cipher = crypto.createCipheriv("aes-256-gcm", KEY, iv);
  const ct = Buffer.concat([cipher.update(plain, "utf8"), cipher.final()]);
  const tag = cipher.getAuthTag();
  const payload: EncryptedPayload = {
    iv: iv.toString("hex"),
    tag: tag.toString("hex"),
    ct: ct.toString("hex"),
  };
  return JSON.stringify(payload);
}

export function decrypt(enc: string | null | undefined): string | undefined {
  if (!enc) return undefined;
  try {
    const payload = JSON.parse(enc) as EncryptedPayload;
    const iv = Buffer.from(payload.iv, "hex");
    const tag = Buffer.from(payload.tag, "hex");
    const ct = Buffer.from(payload.ct, "hex");
    const decipher = crypto.createDecipheriv("aes-256-gcm", KEY, iv);
    decipher.setAuthTag(tag);
    const plain = Buffer.concat([decipher.update(ct), decipher.final()]);
    return plain.toString("utf8");
  } catch {
    return undefined;
  }
}

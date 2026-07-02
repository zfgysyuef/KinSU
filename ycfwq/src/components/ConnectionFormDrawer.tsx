import { useEffect, useState } from "react";
import { X, Loader2, KeyRound, Lock, Plug, Save } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { useConnectionsStore } from "@/store/connections";
import { cn } from "@/lib/utils";
import type { AuthType, ConnectionInput, ConnectionListItem } from "@shared/types";

interface ConnectionFormDrawerProps {
  open: boolean;
  /** 编辑模式时传入原数据；新建时为 null */
  connection: ConnectionListItem | null;
  onClose: () => void;
  onSaved?: (item: ConnectionListItem) => void;
}

interface FormState {
  name: string;
  host: string;
  port: number;
  username: string;
  authType: AuthType;
  password: string;
  privateKey: string;
  passphrase: string;
}

const empty: FormState = {
  name: "",
  host: "",
  port: 22,
  username: "root",
  authType: "password",
  password: "",
  privateKey: "",
  passphrase: "",
};

export function ConnectionFormDrawer({
  open,
  connection,
  onClose,
  onSaved,
}: ConnectionFormDrawerProps) {
  const { create, update, testDry, test } = useConnectionsStore();
  const [form, setForm] = useState<FormState>(empty);
  const [submitting, setSubmitting] = useState(false);
  const [testing, setTesting] = useState(false);
  const [testResult, setTestResult] = useState<string | null>(null);
  const [testOk, setTestOk] = useState<boolean | null>(null);
  const [error, setError] = useState<string | null>(null);

  const isEdit = !!connection;

  useEffect(() => {
    if (open) {
      setTestResult(null);
      setTestOk(null);
      setError(null);
      if (connection) {
        setForm({
          name: connection.name,
          host: connection.host,
          port: connection.port,
          username: connection.username,
          authType: connection.authType,
          password: "",
          privateKey: "",
          passphrase: "",
        });
      } else {
        setForm(empty);
      }
    }
  }, [open, connection]);

  if (!open) return null;

  const set = <K extends keyof FormState>(k: K, v: FormState[K]) =>
    setForm((f) => ({ ...f, [k]: v }));

  const buildInput = (): ConnectionInput => ({
    name: form.name,
    host: form.host,
    port: form.port,
    username: form.username,
    authType: form.authType,
    password: form.authType === "password" ? form.password : undefined,
    privateKey: form.authType === "privateKey" ? form.privateKey : undefined,
    passphrase: form.authType === "privateKey" ? form.passphrase : undefined,
  });

  const handleTest = async () => {
    setTesting(true);
    setTestResult(null);
    setTestOk(null);
    try {
      // 编辑模式且未填敏感字段时，用已存连接测试
      if (
        isEdit &&
        connection &&
        ((form.authType === "password" && !form.password) ||
          (form.authType === "privateKey" && !form.privateKey))
      ) {
        const r = await test(connection.id);
        setTestOk(r.ok);
        setTestResult(r.message);
      } else {
        const r = await testDry(buildInput());
        setTestOk(r.ok);
        setTestResult(r.message);
      }
    } catch (e: any) {
      setTestOk(false);
      setTestResult(e?.message || "测试失败");
    } finally {
      setTesting(false);
    }
  };

  const handleSave = async () => {
    setError(null);
    if (!form.name.trim()) return setError("请填写连接名称");
    if (!form.host.trim()) return setError("请填写主机地址");
    if (!form.username.trim()) return setError("请填写用户名");
    if (form.authType === "password" && !isEdit && !form.password)
      return setError("请填写密码");
    if (form.authType === "privateKey" && !isEdit && !form.privateKey)
      return setError("请填写私钥");

    setSubmitting(true);
    try {
      let item: ConnectionListItem;
      if (isEdit && connection) {
        const patch: Partial<ConnectionInput> = {
          name: form.name,
          host: form.host,
          port: form.port,
          username: form.username,
          authType: form.authType,
        };
        if (form.authType === "password" && form.password)
          patch.password = form.password;
        if (form.authType === "privateKey" && form.privateKey)
          patch.privateKey = form.privateKey;
        if (form.authType === "privateKey" && form.passphrase)
          patch.passphrase = form.passphrase;
        item = await update(connection.id, patch);
      } else {
        item = await create(buildInput());
      }
      onSaved?.(item);
      onClose();
    } catch (e: any) {
      setError(e?.message || "保存失败");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />
      <div className="relative h-full w-full max-w-md animate-fade-up border-l border-kernel-border bg-kernel-panel shadow-panel">
        {/* 头部 */}
        <div className="flex h-16 items-center justify-between border-b border-kernel-border px-5">
          <div className="flex items-center gap-2">
            <Plug className="h-4 w-4 text-amber" />
            <h2 className="font-display text-sm font-bold uppercase tracking-wider text-kernel-ink">
              {isEdit ? "编辑连接" : "新建连接"}
            </h2>
          </div>
          <button
            onClick={onClose}
            className="text-kernel-muted transition-colors hover:text-danger"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* 表单 */}
        <div className="h-[calc(100%-128px)] overflow-y-auto p-5">
          <div className="space-y-5">
            <Field label="连接名称">
              <input
                className={inputCls}
                value={form.name}
                onChange={(e) => set("name", e.target.value)}
                placeholder="例如：生产网关-01"
              />
            </Field>

            <div className="grid grid-cols-3 gap-3">
              <div className="col-span-2">
                <Field label="主机地址">
                  <input
                    className={inputCls}
                    value={form.host}
                    onChange={(e) => set("host", e.target.value)}
                    placeholder="192.168.1.10 或 example.com"
                  />
                </Field>
              </div>
              <Field label="端口">
                <input
                  type="number"
                  className={inputCls}
                  value={form.port}
                  onChange={(e) => set("port", parseInt(e.target.value, 10) || 22)}
                />
              </Field>
            </div>

            <Field label="用户名">
              <input
                className={inputCls}
                value={form.username}
                onChange={(e) => set("username", e.target.value)}
                placeholder="root"
              />
            </Field>

            {/* 认证方式切换 */}
            <Field label="认证方式">
              <div className="grid grid-cols-2 gap-2">
                <AuthOption
                  active={form.authType === "password"}
                  onClick={() => set("authType", "password")}
                  icon={<Lock className="h-3.5 w-3.5" />}
                  label="密码"
                />
                <AuthOption
                  active={form.authType === "privateKey"}
                  onClick={() => set("authType", "privateKey")}
                  icon={<KeyRound className="h-3.5 w-3.5" />}
                  label="SSH 私钥"
                />
              </div>
            </Field>

            {form.authType === "password" ? (
              <Field
                label="密码"
                hint={isEdit ? "留空表示不修改原密码" : undefined}
              >
                <input
                  type="password"
                  className={inputCls}
                  value={form.password}
                  onChange={(e) => set("password", e.target.value)}
                  placeholder={isEdit ? "••••••••" : "输入登录密码"}
                  autoComplete="new-password"
                />
              </Field>
            ) : (
              <>
                <Field
                  label="私钥"
                  hint={isEdit ? "留空表示不修改原私钥" : undefined}
                >
                  <textarea
                    className={cn(inputCls, "h-32 resize-none font-mono text-xs")}
                    value={form.privateKey}
                    onChange={(e) => set("privateKey", e.target.value)}
                    placeholder={
                      isEdit
                        ? "-----BEGIN OPENSSH PRIVATE KEY-----\n..."
                        : "粘贴私钥内容（含 -----BEGIN/END-----）"
                    }
                  />
                </Field>
                <Field label="私钥口令（可选）">
                  <input
                    type="password"
                    className={inputCls}
                    value={form.passphrase}
                    onChange={(e) => set("passphrase", e.target.value)}
                    placeholder="若私钥有口令则填写"
                    autoComplete="new-password"
                  />
                </Field>
              </>
            )}

            {/* 测试结果 */}
            {testResult && (
              <div
                className={cn(
                  "rounded-md border px-3 py-2 font-mono text-xs",
                  testOk
                    ? "border-neon/40 bg-neon/5 text-neon"
                    : "border-danger/40 bg-danger/5 text-danger",
                )}
              >
                {testOk ? "✓ " : "✗ "}
                {testResult}
              </div>
            )}

            {error && (
              <div className="rounded-md border border-danger/40 bg-danger/5 px-3 py-2 font-mono text-xs text-danger">
                {error}
              </div>
            )}
          </div>
        </div>

        {/* 底部操作 */}
        <div className="absolute bottom-0 left-0 right-0 flex h-16 items-center justify-end gap-3 border-t border-kernel-border bg-kernel-panel px-5">
          <Button
            variant="ghost"
            onClick={handleTest}
            disabled={testing || submitting}
          >
            {testing ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <Plug className="h-4 w-4" />
            )}
            测试连接
          </Button>
          <Button
            variant="primary"
            onClick={handleSave}
            disabled={submitting || testing}
          >
            {submitting ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <Save className="h-4 w-4" />
            )}
            保存
          </Button>
        </div>
      </div>
    </div>
  );
}

const inputCls =
  "w-full rounded-md border border-kernel-border bg-kernel-bg/60 px-3 py-2 text-sm text-kernel-ink placeholder:text-kernel-muted/50 focus:border-amber/60 focus:outline-none focus:ring-1 focus:ring-amber/30 transition-colors";

function Field({
  label,
  hint,
  children,
}: {
  label: string;
  hint?: string;
  children: React.ReactNode;
}) {
  return (
    <div>
      <div className="mb-1.5 flex items-center justify-between">
        <label className="font-mono text-[10px] uppercase tracking-widest text-kernel-muted">
          {label}
        </label>
        {hint && (
          <span className="font-mono text-[10px] text-kernel-muted/70">
            {hint}
          </span>
        )}
      </div>
      {children}
    </div>
  );
}

function AuthOption({
  active,
  onClick,
  icon,
  label,
}: {
  active: boolean;
  onClick: () => void;
  icon: React.ReactNode;
  label: string;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={cn(
        "flex items-center justify-center gap-2 rounded-md border px-3 py-2 font-mono text-xs uppercase tracking-wider transition-all",
        active
          ? "border-amber/60 bg-amber/10 text-amber shadow-inner-glow"
          : "border-kernel-border bg-kernel-bg/40 text-kernel-muted hover:border-kernel-muted hover:text-kernel-ink",
      )}
    >
      {icon}
      {label}
    </button>
  );
}

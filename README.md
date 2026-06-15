<div align="center">

# 🌸 FollKernel · 福尔内核

**Advanced kernel-based root solution for Android GKI devices**  
**基于 KernelSU 架构的 Android GKI 内核 Root 解决方案**

[![Release](https://img.shields.io/github/v/release/Spring-bulid/FollKernel?label=Release&color=ec407a)](https://github.com/Spring-bulid/FollKernel/releases/tag/v0.0.1)
[![License](https://img.shields.io/badge/License-GPL--3.0-blue)](LICENSE)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen)](https://android-arsenal.com/api?level=21)

---

### 🌐 44 Languages · 44 种语言 · 44 भाषाएँ · 44 언어 · 44 языков · 44 لغة

| 🇸🇦 العربية | 🇦🇿 Azərbaycan | 🇧🇬 Български | 🇧🇩 বাংলা | 🇧🇦 Bosanski |
|:--:|:--:|:--:|:--:|:--:|
| 🇩🇰 Dansk | 🇩🇪 Deutsch | 🇬🇧 English | 🇪🇸 Español | 🇪🇪 Eesti |
| 🇮🇷 فارسی | 🇵🇭 Filipino | 🇫🇷 Français | 🇪🇸 Galego | 🇮🇳 हिन्दी |
| 🇭🇷 Hrvatski | 🇭🇺 Magyar | 🇮🇩 Indonesia | 🇮🇹 Italiano | 🇮🇱 עברית |
| 🇯🇵 日本語 | 🇰🇭 ភាសាខ្មែរ | 🇮🇳 ಕನ್ನಡ | 🇰🇷 한국어 | 🇱🇹 Lietuvių |
| 🇱🇻 Latviešu | 🇮🇳 मराठी | 🇲🇾 Melayu | 🇲🇲 မြန်မာ | 🇳🇱 Nederlands |
| 🇵🇱 Polski | 🇧🇷 Português (BR) | 🇵🇹 Português (PT) | 🇷🇴 Română | 🇷🇺 Русский |
| 🇸🇮 Slovenščina | 🇷🇸 Српски | 🇮🇳 తెలుగు | 🇹🇭 ไทย | 🇹🇷 Türkçe |
| 🇺🇦 Українська | 🇻🇳 Tiếng Việt | 🇨🇳 简体中文 | 🇭🇰 繁體中文 (HK) | 🇹🇼 繁體中文 (TW) |

> FollKernel automatically adapts to your system language. No manual switching needed.  
> FollKernel 自动跟随系统语言，无需手动切换。

</div>

---

<div align="center">

## ⚡ Quick Start · 快速开始

| Step | English | 简体中文 |
|:----:|---------|----------|
| 1 | Install `FollKernel-v0.0.1.apk` | 安装 FollKernel APK |
| 2 | Tap **Load LKM** if "Not installed" but rooted | 如显示"未安装"但有 Root，点击 **加载 LKM** |
| 3 | Or go to **Install → Direct Install** | 或前往 **安装 → 直接安装** |

</div>

---

## 🌟 Features · 功能特性

<table>
<tr><th colspan="2">🔐 Root & Permission · 权限管理</th></tr>
<tr><td width="50%">

**EN** — Per-app root grant/deny with allow-once / always / never / timeout modes

</td><td width="50%">

**ZH** — 按应用授权 Root，支持单次/始终/拒绝/超时模式

</td></tr>
<tr><td>

**EN** — Fine-grained control: UID/GID remapping, capabilities, SELinux domain rules, mount namespace isolation

</td><td>

**ZH** — 精细化控制：UID/GID 映射、capabilities、SELinux 域规则、挂载命名空间隔离

</td></tr>
<tr><td>

**EN** — Reusable root permission profile templates (grant all / deny all / custom)

</td><td>

**ZH** — 可复用的 Root 权限模板（全部授权/全部拒绝/自定义）

</td></tr>
<tr><td>

**EN** — ADB root shell with per-app profile enforcement

</td><td>

**ZH** — ADB Root Shell，遵循每个应用的权限配置

</td></tr>
<tr><td>

**EN** — Complete root access audit log, searchable and exportable

</td><td>

**ZH** — 完整的 Root 访问审计日志，可搜索、可导出

</td></tr>
</table>

<table>
<tr><th colspan="2">📦 Module System · 模块系统</th></tr>
<tr><td width="50%">

**EN** — OverlayFS-based Magisk-compatible systemless module framework

</td><td width="50%">

**ZH** — 基于 OverlayFS 的无系统修改模块框架，兼容 Magisk

</td></tr>
<tr><td>

**EN** — Execute custom actions from module's `action.sh`

</td><td>

**ZH** — 执行模块 `action.sh` 中的自定义操作

</td></tr>
<tr><td>

**EN** — Per-module key-value config settings (persistent & temporary)

</td><td>

**ZH** — 每个模块独立的键值配置（持久化 & 临时）

</td></tr>
<tr><td>

**EN** — Lua scripting runtime for module automation

</td><td>

**ZH** — Lua 脚本运行时，用于模块自动化

</td></tr>
<tr><td>

**EN** — Built-in WebView server for module web interfaces

</td><td>

**ZH** — 内建 WebView 服务，支持模块 Web 界面

</td></tr>
<tr><td>

**EN** — Browse, search, download and install modules from online repositories

</td><td>

**ZH** — 在线仓库浏览、搜索、下载和安装模块

</td></tr>
<tr><td>

**EN** — Launcher shortcuts for module quick actions

</td><td>

**ZH** — 启动器快捷方式，快速操作模块

</td></tr>
<tr><td>

**EN** — Automatic version check against GitHub releases

</td><td>

**ZH** — 自动检查 GitHub Release 版本更新

</td></tr>
</table>

<table>
<tr><th colspan="2">⚙️ Kernel Integration · 内核集成</th></tr>
<tr><td width="50%">

**EN** — Direct install/update KSU into boot image, OTA-aware slot management

</td><td width="50%">

**ZH** — 直接修补刷入 Boot 镜像，支持 OTA 槽位管理

</td></tr>
<tr><td>

**EN** — Load kernel module dynamically (insmod) without kernel rebuild

</td><td>

**ZH** — 动态加载内核模块 (insmod)，无需重新编译内核

</td></tr>
<tr><td>

**EN** — Built-in kernel modules for all supported KMIs, one-tap load from APK

</td><td>

**ZH** — 内置全部 KMI 内核模块，APK 内一键加载

</td></tr>
<tr><td>

**EN** — Kernel Patch Module: inline-hook and syscall-table-hook support

</td><td>

**ZH** — 内核补丁模块 KPM：支持 inline-hook 和 syscall-table-hook

</td></tr>
<tr><td>

**EN** — Late-load KSU post-boot on permissive SELinux (magica service)

</td><td>

**ZH** — SELinux Permissive 下越狱模式延迟加载 (magica)

</td></tr>
<tr><td>

**EN** — Safe Mode: boot with all modules disabled for recovery

</td><td>

**ZH** — 安全模式：禁用所有模块启动，用于恢复

</td></tr>
</table>

<table>
<tr><th colspan="2">🛡️ SuSFS Integration · SuSFS 集成</th></tr>
<tr><td width="50%">

**EN** — **SUS_PATH**: Hide files and directories from userspace

</td><td width="50%">

**ZH** — **SUS_PATH**：对用户态隐藏文件和目录

</td></tr>
<tr><td>

**EN** — **SUS_MOUNT**: Hide mount points from `/proc/mounts`

</td><td>

**ZH** — **SUS_MOUNT**：从 `/proc/mounts` 隐藏挂载点

</td></tr>
<tr><td>

**EN** — **SUS_KSTAT**: Hide kernel kstat entries

</td><td>

**ZH** — **SUS_KSTAT**：隐藏内核 kstat 条目

</td></tr>
<tr><td>

**EN** — **TRY_UMOUNT**: Force unmount of suspicious bind mounts

</td><td>

**ZH** — **TRY_UMOUNT**：强制卸载可疑 bind 挂载

</td></tr>
<tr><td>

**EN** — **SPOOF_UNAME**: Spoof kernel uname string

</td><td>

**ZH** — **SPOOF_UNAME**：伪装内核 uname 信息

</td></tr>
<tr><td>

**EN** — **OPEN_REDIRECT**: Redirect file open attempts

</td><td>

**ZH** — **OPEN_REDIRECT**：重定向文件打开请求

</td></tr>
<tr><td>

**EN** — **SUS_MAP**: Map file paths for hiding

</td><td>

**ZH** — **SUS_MAP**：映射文件路径实现隐藏

</td></tr>
</table>

<table>
<tr><th colspan="2">🔄 OTA & Installation · OTA 与安装</th></tr>
<tr><td width="50%">

**EN** — Direct Install: flash patched boot image from app

</td><td width="50%">

**ZH** — 直接安装：从 APP 刷入修补后的 Boot 镜像

</td></tr>
<tr><td>

**EN** — Select File: install from local boot.img or LKM .ko

</td><td>

**ZH** — 选择文件：从本地 boot.img 或 .ko 文件安装

</td></tr>
<tr><td>

**EN** — OTA Survival: install to inactive slot after OTA update

</td><td>

**ZH** — OTA 保留：OTA 后安装到非活跃槽位

</td></tr>
<tr><td>

**EN** — Restore Stock Boot: flash clean boot image

</td><td>

**ZH** — 还原 Boot：刷入原始 Boot 镜像

</td></tr>
<tr><td>

**EN** — Flash Modules: install ZIP modules directly, no recovery needed

</td><td>

**ZH** — 刷入模块：直接安装 ZIP 模块，无需 Recovery

</td></tr>
</table>

<table>
<tr><th colspan="2">🎨 User Experience · 用户体验</th></tr>
<tr><td width="50%">

**EN** — Material You dynamic color theming (light/dark)

</td><td width="50%">

**ZH** — Material You 动态取色主题（浅色/深色）

</td></tr>
<tr><td>

**EN** — Configurable accent color palette presets

</td><td>

**ZH** — 可配置的强调色调色板预设

</td></tr>
<tr><td>

**EN** — Soft rounded icon corners (14dp radius)

</td><td>

**ZH** — 柔润 R 角图标 (14dp)

</td></tr>
<tr><td>

**EN** — Full-text search across SuperUser list

</td><td>

**ZH** — SuperUser 列表全文本搜索

</td></tr>
<tr><td>

**EN** — Optional app lock (PIN/password) for SuperUser

</td><td>

**ZH** — 可选的应用锁（PIN/密码）保护 SuperUser

</td></tr>
<tr><td>

**EN** — Reboot menu: Reboot / Soft / Recovery / Bootloader / Download

</td><td>

**ZH** — 重启菜单：重启/软重启/Recovery/Bootloader/Download

</td></tr>
<tr><td>

**EN** — Device info: kernel, SELinux, seccomp, LKM/GKI detection

</td><td>

**ZH** — 设备信息：内核版本、SELinux、seccomp、LKM/GKI 检测

</td></tr>
</table>

<table>
<tr><th colspan="2">🔒 Security & Audit · 安全与审计</th></tr>
<tr><td width="50%">

**EN** — SELinux enforcing status verification

</td><td width="50%">

**ZH** — SELinux 强制状态检测

</td></tr>
<tr><td>

**EN** — HTTPS-only with strict certificate validation

</td><td>

**ZH** — 仅 HTTPS，严格证书校验

</td></tr>
<tr><td>

**EN** — ADB root confirmation guard

</td><td>

**ZH** — ADB Root 确认保护

</td></tr>
<tr><td>

**EN** — Sulog export with device/fingerprint/KMI metadata

</td><td>

**ZH** — SuLog 导出含设备/指纹/KMI 元数据

</td></tr>
</table>

<table>
<tr><th colspan="2">🔧 SM8650 Build · SM8650 构建系统</th></tr>
<tr><td width="50%">

**EN** — 7 KMI targets: android12-5.10 → android16-6.12

</td><td width="50%">

**ZH** — 7 个 KMI 目标：android12-5.10 → android16-6.12

</td></tr>
<tr><td>

**EN** — OnePlus 12 / OPPO Find X7 Ultra builder scripts

</td><td>

**ZH** — 一加 12 / OPPO Find X7 Ultra 编译脚本

</td></tr>
<tr><td>

**EN** — KPM / SUSFS / BBR / LZ4 / ZSTD / Droidspaces support

</td><td>

**ZH** — 支持 KPM / SUSFS / BBR / LZ4 / ZSTD / Droidspaces

</td></tr>
<tr><td>

**EN** — ReSukiSU & KernelSU Next kernel backend options

</td><td>

**ZH** — 可选 ReSukiSU 或 KernelSU Next 内核后端

</td></tr>
</table>

---

## 📥 Download · 下载

[![Download APK](https://img.shields.io/badge/Download-APK-ec407a?style=for-the-badge)](https://github.com/Spring-bulid/FollKernel/releases/download/v0.0.1/FollKernel-v0.0.1.apk)
[![Download LKM](https://img.shields.io/badge/Download-LKM.zip-2196f3?style=for-the-badge)](https://github.com/Spring-bulid/FollKernel/releases/download/v0.0.1/FollKernel-LKM-KPM.zip)

| File | Size | Description · 描述 |
|------|------|-----|
| `FollKernel-v0.0.1.apk` | ~10 MB | Manager APK with bundled LKM modules · 含内置 LKM 模块的管理器 |
| `FollKernel-LKM-KPM.zip` | ~657 KB | Standalone KMI kernel modules (7 KMIs) · 独立 KMI 内核模块 |

---

## 🛠️ Build · 编译

```bash
# APK · 管理器
cd KernelSU/manager
./gradlew assembleRelease

# LKM kernel modules · 内核模块 (WSL)
wsl -d Ubuntu-22.04 -u root -- bash /mnt/d/rekernel/_rebuild_all_ko.sh
```

---

## 🙏 Credits · 致谢

| Project | Contribution · 贡献 |
|---------|---------------------|
| [KernelSU](https://github.com/tiann/KernelSU) | Core manager architecture & module system · 核心管理器和模块系统 |
| [ReSukiSU](https://github.com/ReSukiSU/ReSukiSU) | SUSFS integration & kernel feature set · SUSFS 集成和内核特性 |
| [KernelPatch](https://github.com/bmax121/KernelPatch) | KPM kernel patching reference · KPM 内核补丁参考 |
| [APatch](https://github.com/bmax121/APatch) | KPM architecture inspiration · KPM 架构灵感 |
| [KernelSU Next](https://github.com/KernelSU-Next/KernelSU-Next) | Optional KSU Next backend · 可选 KSU Next 后端 |
| [cctv18](https://github.com/cctv18) | SM8650 builder scripts & SUSFS patches · SM8650 编译脚本和补丁 |

---

<div align="center">

### 📜 License · 许可证

**GPL-3.0** — Free and open source software · 自由开源软件

🌸 *Made with passion for the Android rooting community* 🌸

</div>

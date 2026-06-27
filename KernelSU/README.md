[English](#english) | 中文

<div align="center">

<img src="website/docs/public/logo.png" alt="KinSU" width="120" height="120">

# KinSU

**基于 Android 内核的 root 方案 · KernelSU 的独立进化分支**

<br>

<a href="https://github.com/Spring-bulid/KinSU/releases/latest"><img src="https://img.shields.io/github/v/release/Spring-bulid/KinSU?label=Latest%20Release&logo=github&color=blueviolet" alt="Latest Release"></a>
<a href="https://github.com/Spring-bulid/KinSU/releases/latest"><img src="https://img.shields.io/github/downloads/Spring-bulid/KinSU/total?logo=android&color=green" alt="Downloads"></a>
<a href="LICENSE"><img src="https://img.shields.io/badge/License-GPL%20v3-blue.svg?logo=gnu" alt="License"></a>
<img src="https://img.shields.io/badge/Platform-Android%2012%2B-brightgreen?logo=android" alt="Platform">
<img src="https://img.shields.io/badge/Arch-arm64--v8a%20%7C%20x86__64-blue?logo=arm" alt="Architecture">

<br>
<br>

</div>

---

## 目录

- [什么是 KinSU](#什么是-kinsu)
- [核心特性](#核心特性)
- [与 KernelSU 的区别](#与-kernelsu-的区别)
- [项目架构](#项目架构)
- [KMI 支持列表](#kmi-支持列表)
- [兼容性](#兼容性)
- [快速开始](#快速开始)
- [从源码构建](#从源码构建)
- [GKI 内核编译教程](#gki-内核编译教程)
- [技术栈](#技术栈)
- [许可证](#许可证)
- [鸣谢](#鸣谢)

---

## 什么是 KinSU

KinSU 是一个**基于 Linux 内核的 Android root 权限管理方案**。它通过内核模块（LKM）在内核层面拦截和管理 `su` 调用，而非依赖用户态的 su 二进制文件。

**这意味着：**

- Root 权限的授予由内核直接控制，无法被用户态程序绕过或伪造
- 不修改 `/system` 分区，真正做到**无系统修改（systemless）**
- 通过 App Profile 对每个应用独立设置 root 策略，把 root 权力关进笼子里

KinSU 脱胎于 [KernelSU](https://github.com/tiann/KernelSU)，在保留其全部内核能力的基础上，进行了**深度的品牌独立化改造**。

---

## 核心特性

### 内核级 su 和权限管理

Root 权限直接通过内核 IOCTL 接口授予，整个流程绕过了传统的用户态 `su` 二进制。内核通过 `setresuid` 系统调用钩子拦截所有提权请求，只有在 allowlist 中的应用才能获得 root。

### Metamodule 模块系统

通过 OverlayFS 挂载技术实现可插拔的无系统修改。模块以 ZIP 包形式安装，支持：

- 安装/卸载/启用/禁用操作
- 生命周期脚本：`post-fs-data.sh`、`service.sh`、`post-mount.sh`、`boot-completed.sh`
- 自定义 OverlayFS 挂载规则
- Metamodule：单一活跃的元模块，可 hook 所有其他模块的挂载和安装行为

### App Profile

为每个应用单独配置 root 权限策略：

- **UID/GID/Capabilities**：控制进程运行时的身份
- **SELinux 域**：自定义 SELinux 策略语句
- **Umount 模块**：对非 root 应用卸载模块挂载，隐藏 root 痕迹
- **模板系统**：预定义策略模板，一键应用到多个应用
- **No New Privs**：阻止进一步的权限提升

### 全新自定义 UI

基于 Jetpack Compose + Material Design 3 完全重设计的界面：

- 原生深色/浅色主题，支持 Material You 动态取色
- 流畅的导航动画和手势交互
- 模块仓库浏览器，支持在线搜索和安装
- WebUI 渲染引擎，模块可提供 Web 界面
- SU 日志查看器，实时监控 root 调用

### 自编译内核模块

APK 内嵌 7 个预编译的 KMI 内核模块，覆盖 Android 12 ~ 16：

- 编译产物由 `rust_embed` 宏嵌入到 `ksud` 守护进程
- 在 Manager 中直接选择 KMI 版本刷入
- 支持使用本地编译的 `.ko` 文件作为备选

---

## 与 KernelSU 的区别

| 方面 | KernelSU | KinSU |
|------|----------|----------|
| 品牌名 | KernelSU | KinSU |
| UI 框架 | MIUIX (小米风格) | Material Design 3 (全新重写) |
| 守护进程 | `libksud.so` | `libKinSUd.so` |
| 内核模块 | `kernelsu.ko` | `KinSU.ko` |
| 包名 | `me.weishu.kernelsu` | `me.weishu.KinSU` |
| 所有字符串 | KernelSU | KinSU (40+ 语言) |
| 核心功能 | 完全兼容 | 完全兼容 |

---

## 项目架构

```
┌─────────────────────────────────────────────────────────────┐
│                   Android Manager APK                         │
│  ┌──────────┐  ┌─────────────┐  ┌──────────────────────┐    │
│  │ Kotlin   │  │  Compose UI │  │  C++ JNI (jni.cc)     │    │
│  │ ViewModels│  │  Material 3 │  │  IOCTL 直接通信       │    │
│  └──────────┘  └─────────────┘  └──────────┬─────────────┘    │
│                                              │                 │
├──────────────────────────────────────────────┼─────────────────┤
│                   Rust Userspace              │                 │
│  ┌──────────────────────────────────────────┐│                 │
│  │  ksud (守护进程)                          ││                 │
│  │  ├─ 模块管理 (install/uninstall/enable)   ││                 │
│  │  ├─ Boot Image 修补 (boot_patch)         ││                 │
│  │  ├─ LKM 延迟加载 (late_load)             ││                 │
│  │  ├─ App Profile 管理 (profile)           ││                 │
│  │  ├─ SELinux 策略 (sepolicy)             ││                 │
│  │  ├─ SU 日志 (sulog)                     ││                 │
│  │  └─ Root Shell (su)                     ││                 │
│  ├──────────────────────────────────────────┤│                 │
│  │  ksuinit (早期启动)                       ││                 │
│  │  └─ 加载 KinSU.ko → execve 真实 init  ││                 │
│  └──────────────────────────────────────────┘│                 │
│                                              │                 │
├──────────────────────────────────────────────┼─────────────────┤
│                 Linux Kernel Module           │                 │
│  ┌───────────────────────────────────────────┴──────────────┐ │
│  │  KinSU.ko                                              │ │
│  │  ├─ supercall.c        IOCTL 调度                        │ │
│  │  ├─ allowlist.c        应用白名单                         │ │
│  │  ├─ app_profile.c      每应用 root 策略                   │ │
│  │  ├─ setuid_hook.c      拦截 setresuid 系统调用            │ │
│  │  ├─ lsm_hook.c         LSM 钩子                          │ │
│  │  ├─ selinux/rules.c    SELinux 域管理                    │ │
│  │  ├─ sulog/             SU 日志事件                        │ │
│  │  └─ feature/           特性开关                          │ │
│  │     ├─ adb_root       ADB root 权限                     │ │
│  │     ├─ kernel_umount  内核层卸载                         │ │
│  │     ├─ sulog          SU 日志                            │ │
│  │     ├─ sucompat       兼容 /system/bin/su               │ │
│  │     └─ selinux_hide   SELinux 隐藏                      │ │
│  └───────────────────────────────────────────┴──────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 三种通信通道

| 通道 | 机制 | 用途 |
|------|------|------|
| **IOCTL** | `[ksu_driver]` 匿名 inode，通过 `reboot()` 魔法数注入 FD | App Profile、Allowlist、Feature 配置 |
| **Root Shell** | ksud 也是 `su`，`libsu` 调用 ksud CLI | 模块安装、Boot Patch、Sepolicy |
| **Sulog FD** | `KSU_IOCTL_GET_SULOG_FD` + epoll | 内核日志事件流 |

### 两种启动路径

**路径 A：GKI 预打包（推荐）**

```
Bootloader → Linux Kernel (含 KinSU.ko)
  → ksuinit 替换 /init
  → ksuinit 通过 init_module() 加载 KinSU.ko
  → 所有内核子系统在早期启动中初始化
  → ksuinit execves 真正的 /init
```

**路径 B：Magica 越狱延迟加载（锁 bootloader 设备）**

```
Bootloader → Stock Kernel
  → AppZygotePreload 启动 MagicaService
  → Fork ksud "late-load --magica 5555"
  → ksud 通过属性操作启用 adbd root
  → ADB TCP 连接，exec 自身为 "late-load --post-magica"
  → 检测 KMI，从内嵌资源加载 KinSU.ko
  → 运行阶段脚本，重启 Manager
```

---

## KMI 支持列表

| KMI | 内核版本 | Android 版本 | 状态 |
|-----|---------|-------------|------|
| `android12-5.10` | 5.10 | 12 | 预编译 |
| `android13-5.10` | 5.10 | 13 | 预编译 |
| `android13-5.15` | 5.15 | 13 | 预编译 |
| `android14-5.15` | 5.15 | 14 | 预编译 |
| `android14-6.1` | 6.1 | 14 | 预编译 |
| `android15-6.6` | 6.6 | 15 | 预编译 |
| `android16-6.12` | 6.12 | 16 | 预编译 |

---

## 兼容性

| 条件 | 支持状态 |
|------|---------|
| Android GKI 2.0 (Kernel 5.10+) | 官方支持 |
| 旧内核 (4.14+) | 需手动编译 |
| WSA / ChromeOS / 容器化 Android | 支持 |
| 架构 `arm64-v8a` | 支持 |
| 架构 `x86_64` | 支持 |
| Magisk 共存 | 自动检测并跳过冲突 |

---

## 快速开始

1. 从 [Releases](https://github.com/Spring-bulid/KinSU/releases/latest) 下载最新 APK
2. 安装 APK 到你的 Android 设备
3. 打开 KinSU Manager
4. **GKI 设备**：在 Manager 中选择对应 KMI 版本，一键刷入
5. **非 GKI 设备**：手动编译内核模块，使用「使用本地 LKM 文件」刷入
6. 重启设备，开始使用 root 权限管理

---

## 从源码构建

### 环境要求

- **Rust** 1.80+
- **Android NDK** 27+
- **Gradle** 9.5+
- **JDK** 21+
- **Android SDK** 37 (compileSdk)

### 构建步骤

```bash
# 1. 编译 ksud 守护进程（两个架构）
cd KinSU
cargo build --release --target aarch64-linux-android -p ksud
cargo build --release --target x86_64-linux-android -p ksud

# 2. 复制编译产物到 jniLibs
cp target/aarch64-linux-android/release/ksud \
   manager/app/src/main/jniLibs/arm64-v8a/libKinSUd.so
cp target/x86_64-linux-android/release/ksud \
   manager/app/src/main/jniLibs/x86_64/libKinSUd.so

# 3. 编译内核模块（可选，用于替换占位文件）
# 需要 Linux 环境 + DDK 工具
cd kernel
./build-all.sh

# 4. 构建 Manager APK
cd ../manager
ANDROID_HOME=/path/to/sdk ./gradlew assembleRelease
```

APK 输出位置：`manager/app/build/outputs/apk/release/KinSU_*-release.apk`

---

## GKI 内核编译教程

完整的 GKI 内核模块编译指南，涵盖三种编译方式（DDK 工具、手动编译、setup.sh 集成），以及常见问题排查。

**[查看完整教程](docs/gki-build-guide.md)**

---

## 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| **Kernel** | C, Linux Kernel API | 内核模块 (LKM) |
| **Userspace** | Rust | 守护进程 + 早期启动 |
| **JNI** | C++17 | Kotlin ↔ Kernel 桥接 |
| **Manager** | Kotlin, Jetpack Compose | Android UI |
| **构建** | Cargo, Gradle, CMake | 跨平台构建 |

### Rust 关键依赖

- `rustix`：系统调用封装
- `android-bootimg`：Android Boot Image 解析
- `nom`：SELinux 策略解析器
- `clap`：CLI 参数解析
- `bindgen`：内核 UAPI 绑定生成
- `adb_client`：Magica 路径 ADB 通信

---

## 许可证

- `kernel/` 目录下的所有文件：[GPL-2.0-only](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
- 其余所有部分：[GPL-3.0-or-later](https://www.gnu.org/licenses/gpl-3.0.html)

---

## 鸣谢

KinSU 基于以下开源项目构建：

- [**KernelSU**](https://github.com/tiann/KernelSU) — 上游项目，提供完整的内核 root 方案
- [**kernel-assisted-superuser**](https://git.zx2c4.com/kernel-assisted-superuser/about/) — 最初的灵感来源
- [**Magisk**](https://github.com/topjohnwu/Magisk) — 强大的 Android root 工具
- [**genuine**](https://github.com/brevent/genuine/) — APK v2 签名验证
- [**Diamorphine**](https://github.com/m0nad/Diamorphine) — rootkit 技术参考

### 版权声明

KinSU 是 [KernelSU](https://github.com/tiann/KernelSU) 的衍生作品（derivative work）。根据 GNU General Public License（GPL v2/v3）的要求，特此声明：

- **原作者**：[weishu](https://github.com/tiann)（KernelSU 项目作者）
- **原项目**：https://github.com/tiann/KernelSU
- **原许可协议**：GPL v2（kernel 部分）/ GPL v3（userspace 与 manager 部分）
- **提交记录**：KinSU 完整保留了 KernelSU 的原始提交历史，未做任何篡改或洗稿
- **贡献者**：weishu 及所有 [KernelSU 贡献者](https://github.com/tiann/KernelSU/graphs/contributors)

KinSU 在此基础上进行的所有修改均同样遵循 GPL 协议开源。完整声明请见 [NOTICE](NOTICE) 文件。

---

<h2 id="english">English</h2>

<div align="center">

<img src="website/docs/public/logo.png" alt="KinSU" width="120" height="120">

# KinSU

**A kernel-based root solution for Android. Rebranded and evolved from KernelSU.**

<br>

<a href="https://github.com/Spring-bulid/KinSU/releases/latest"><img src="https://img.shields.io/github/v/release/Spring-bulid/KinSU?label=Latest%20Release&logo=github&color=blueviolet" alt="Latest Release"></a>
<a href="https://github.com/Spring-bulid/KinSU/releases/latest"><img src="https://img.shields.io/github/downloads/Spring-bulid/KinSU/total?logo=android&color=green" alt="Downloads"></a>
<a href="LICENSE"><img src="https://img.shields.io/badge/License-GPL%20v3-blue.svg?logo=gnu" alt="License"></a>
<img src="https://img.shields.io/badge/Platform-Android%2012%2B-brightgreen?logo=android" alt="Platform">
<img src="https://img.shields.io/badge/Arch-arm64--v8a%20%7C%20x86__64-blue?logo=arm" alt="Architecture">

</div>

## What is KinSU

KinSU is a **kernel-level root access management solution** for Android. It intercepts `su` calls through a Linux kernel module (LKM) at the kernel level, rather than relying on userspace `su` binaries.

This means root access is **granted directly by the kernel**, bypassing traditional userspace tools — making it resistant to tampering and detection.

## Features

- **Kernel-based su**: Root access via kernel IOCTL — no userspace `su` binary
- **Metamodule System**: Pluggable systemless modifications via OverlayFS
- **App Profile**: Per-app root UID/GID/caps/SELinux domain policies
- **Custom UI**: Fully redesigned with Jetpack Compose + Material Design 3
- **7 Pre-compiled KMIs**: android12-5.10 through android16-6.12

## Project Architecture

```
Kotlin/Compose Manager → C++ JNI → Rust ksud daemon → C kernel module (KinSU.ko)
```

Three communication channels between userspace and kernel:

1. **IOCTL** via `[ksu_driver]` anonymous inode (App Profile, Feature config)
2. **Root Shell** via ksud CLI (Module management, Boot patching)
3. **Sulog FD** via epoll (Kernel event streaming)

## KMI Support

| KMI | Kernel | Android |
|-----|--------|---------|
| android12-5.10 | 5.10 | 12 |
| android13-5.10 | 5.10 | 13 |
| android13-5.15 | 5.15 | 13 |
| android14-5.15 | 5.15 | 14 |
| android14-6.1 | 6.1 | 14 |
| android15-6.6 | 6.6 | 15 |
| android16-6.12 | 6.12 | 16 |

## Quick Start

Download the latest APK from [Releases](https://github.com/Spring-bulid/KinSU/releases/latest), install, select your KMI version, and flash.

## GKI Kernel Build Guide

Complete guide for compiling KinSU GKI kernel modules with three methods (DDK tool, manual build, setup.sh integration).

**[View Full Guide](docs/gki-build-guide.md)**

## License

- `kernel/`: [GPL-2.0-only](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
- Everything else: [GPL-3.0-or-later](https://www.gnu.org/licenses/gpl-3.0.html)

## Credits

[KernelSU](https://github.com/tiann/KernelSU) · [kernel-assisted-superuser](https://git.zx2c4.com/kernel-assisted-superuser/about/) · [Magisk](https://github.com/topjohnwu/Magisk)

### Copyright Notice

KinSU is a derivative work of [KernelSU](https://github.com/tiann/KernelSU). In accordance with the GNU General Public License (GPL v2/v3):

- **Original Author**: [weishu](https://github.com/tiann) (author of the KernelSU project)
- **Original Project**: https://github.com/tiann/KernelSU
- **Original License**: GPL v2 (kernel) / GPL v3 (userspace & manager)
- **Commit History**: KinSU preserves the complete original commit history of KernelSU without any tampering or rewriting
- **Contributors**: weishu and all [KernelSU contributors](https://github.com/tiann/KernelSU/graphs/contributors)

All modifications made in KinSU are released under the same GPL license. See the [NOTICE](NOTICE) file for the full statement.

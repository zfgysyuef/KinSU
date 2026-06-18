<div align="center">

<img src="icon.png" width="128" height="128" alt="FollKernel" style="border-radius: 28px; box-shadow: 0 12px 40px rgba(236, 64, 122, 0.25);">

# FollKernel · 福尔内核

> *“把 Root 权限交给最懂你的人 —— 也就是你自己。”*

**Advanced kernel-based root solution for Android GKI devices**  
**基于 KernelSU 架构的 Android GKI 内核 Root 解决方案**

[![Release](https://img.shields.io/github/v/release/Spring-bulid/FollKernel?label=%E6%9C%80%E6%96%B0%E7%89%88%E6%9C%AC&color=ec407a&style=for-the-badge)](https://github.com/Spring-bulid/FollKernel/releases/latest)
[![License](https://img.shields.io/badge/License-GPL--3.0-ff80ab?style=for-the-badge)](LICENSE)
[![API](https://img.shields.io/badge/API-31%2B-e040fb?style=for-the-badge)](https://android-arsenal.com/api?level=31)

</div>

---

## 你好，冒险者

**FollKernel** 是一款从 **KernelSU** 生态中诞生的 Android Root 授权管理器。她不会擅自把 Root 权限交给陌生人，而是把最终决定权郑重地交到你手里。

这里不是冷冰冰的工具箱，而是属于你的玩机空间。模块、KPM、主题色、刷写、日志，全部被她整理得井井有条。只要你的设备搭载了 KernelSU 或 KernelPatch，FollKernel 就会带你一起探索 Android 的深层世界。

> **EN** — FollKernel is a modern root manager built on top of KernelSU. It hands the root authorization back to you, with a polished UI, module support, KPM kernel patch loading, and one-click AnyKernel3 flashing.

---

## 她能为你做什么

<div align="center">

| 能力 | 中文介绍 | EN Description |
|:----:|---------|----------------|
| 超级用户授权 | 决定哪些应用可以、哪些应用不可以触碰 Root | Per-app superuser grant/deny with lifetime control |
| 模块管理 | 安装 / 启用 / 禁用 / 卸载 ZIP 模块 | Install, enable, disable and uninstall ZIP modules |
| KPM 内核补丁 | 加载 .kpm 补丁，GKI 模式下也能嵌入 | Load .kpm kernel patches, embeddable in GKI mode |
| AnyKernel3 刷写 | 在应用内修补 boot / init_boot | Patch boot / init_boot via AnyKernel3 ZIP |
| 主题与外观 | 动态取色 + 多套主题色 + 深色模式 | Material You theming with accent presets & dark mode |
| 应用内更新 | 自动从 GitHub Releases 检查新版本 | Auto-check for updates from GitHub Releases |

</div>

---

## 快速开始

```text
① 安装 FollKernel APK
② 确认内核已集成 KernelSU / KernelPatch
③ 授权、装模块、刷写、换主题，开始你的冒险
```

> **EN Quick Start**
> 1. Install the latest FollKernel APK.
> 2. Make sure your kernel already embeds KernelSU / KernelPatch.
> 3. Grant root, manage modules, flash, and theme your device.

---

## 下载

<div align="center">

[![Download APK](https://img.shields.io/badge/%E4%B8%8B%E8%BD%BD%20APK-ec407a?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Spring-bulid/FollKernel/releases/latest)
[![Official Docs](https://img.shields.io/badge/%E5%AE%98%E6%96%B9%E6%96%87%E6%A1%A3-7c4dff?style=for-the-badge)](https://spring-bulid.github.io/FollKernel/)

</div>

| 文件 | 大小 | 说明 |
|------|------|------|
| `FollKernel_v30022_30022-release.apk` | ~10.5 MB | 管理器本体（含内置 LKM） |

---

## 构建

```bash
# 编译 APK
# Build the manager APK
cd KernelSU/manager
./gradlew assembleRelease
```

> 提示：Windows 环境下请使用 `gradlew.bat`。

---

## 致谢

FollKernel 的成长离不开前辈们的肩膀。

| 项目 / Project | 贡献 / Contribution |
|----------------|---------------------|
| [KernelSU](https://github.com/tiann/KernelSU) | 内核级 Root 能力与安全模型 |
| [FolkPatch](https://github.com/LyraVoid/FolkPatch) | 多布局与视觉灵感 |
| [KernelPatch](https://github.com/bmax121/KernelPatch) | KPM 内核补丁参考 |
| [APatch](https://github.com/bmax121/APatch) | KPM 架构灵感 |

还要感谢 **松板有希**、**心动了么** 等伙伴一路相伴。

---

<div align="center">

### 许可证

**GPL-3.0** — 自由开源，永远保持热爱。

*“愿你刷机不翻车，Root 不报错，模块都兼容，主题都好看。”*

*Made with passion for the Android rooting community*

</div>

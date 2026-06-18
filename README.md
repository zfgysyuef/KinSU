<div align="center">

<img src="icon.png" width="128" height="128" alt="FollKernel" style="border-radius: 28px; box-shadow: 0 12px 40px rgba(236, 64, 122, 0.25);">

# FollKernel · 福尔内核

> *“把 Root 权限交给最懂你的人 —— 也就是你自己。”*

**基于 KernelSU 架构的 Android GKI 内核 Root 解决方案**

[![Release](https://img.shields.io/github/v/release/Spring-bulid/FollKernel?label=%E6%9C%80%E6%96%B0%E7%89%88%E6%9C%AC&color=ec407a&style=for-the-badge)](https://github.com/Spring-bulid/FollKernel/releases/latest)
[![License](https://img.shields.io/badge/License-GPL--3.0-ff80ab?style=for-the-badge)](LICENSE)
[![API](https://img.shields.io/badge/API-31%2B-e040fb?style=for-the-badge)](https://android-arsenal.com/api?level=31)

**[English](README_EN.md) | 简体中文**

</div>

---

## 你好，冒险者

**FollKernel** 是一款从 **KernelSU** 生态中诞生的 Android Root 授权管理器。她不会擅自把 Root 权限交给陌生人，而是把最终决定权郑重地交到你手里。

这里不是冷冰冰的工具箱，而是属于你的玩机空间。模块、KPM、主题色、刷写、日志，全部被她整理得井井有条。只要你的设备搭载了 KernelSU 或 KernelPatch，FollKernel 就会带你一起探索 Android 的深层世界。

FollKernel 诞生于对 Root 管理器的新想象：它应该在底层保持 KernelSU 的内核级安全模型，在上层提供现代、直观、可自定义的交互体验。她既是你的授权中枢，也是你的模块仓库、刷写助手和主题工坊。

---

## 她能为你做什么

<div align="center">

| 能力 | 介绍 |
|:----:|---------|
| 超级用户授权 | 决定哪些应用可以、哪些应用不可以触碰 Root，支持永久授权、单次授权与定时回收 |
| 模块管理 | 安装 / 启用 / 禁用 / 卸载 ZIP 模块，支持 KernelSU 与 Magisk 风格模块 |
| KPM 内核补丁 | 加载 `.kpm` 补丁，GKI 模式下也能嵌入，开机自动加载与手动调试兼顾 |
| AnyKernel3 刷写 | 在应用内修补 `boot` / `init_boot`，刷写前自动备份原镜像 |
| 主题与外观 | 动态取色、多套主题色、深色 / 浅色模式，打造专属管理器 |
| 应用内更新 | 自动从 GitHub Releases 检查新版本，一键跳转下载 |
| 日志与审计 | 查看 Root 授权记录、模块加载日志与内核补丁运行状态 |

</div>

---

## 为什么选择 FollKernel

Android 上的 Root 方案有很多，但 FollKernel 尝试在几个关键点上做得更好：

- **授权归你**：Root 不是应用天生应得的权利。每一次授权、拒绝或回收，都由你决定。
- **内核级安全**：依托 KernelSU 的内核钩子与安全上下文，FollKernel 在系统最底层执行授权策略。
- **现代体验**：Material Design 3 Expressive 视觉、大圆角、动态取色与流畅动画，让管理器不再像工具箱。
- **扩展友好**：模块、KPM、AnyKernel3 三种扩展方式覆盖从应用到内核的不同需求。
- **透明开源**：源码、构建流程与发布资产全部公开，遵循 GPL-3.0 协议。

---

## 快速开始

```text
① 安装 FollKernel APK
② 确认内核已集成 KernelSU / KernelPatch
③ 授权、装模块、刷写、换主题，开始你的冒险
```

更详细的安装与使用说明请访问 [官方文档](https://spring-bulid.github.io/FollKernel/)。

---

## 下载

<div align="center">

[![下载 APK](https://img.shields.io/badge/%E4%B8%8B%E8%BD%BD%20APK-ec407a?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Spring-bulid/FollKernel/releases/latest)
[![官方文档](https://img.shields.io/badge/%E5%AE%98%E6%96%B9%E6%96%87%E6%A1%A3-7c4dff?style=for-the-badge)](https://spring-bulid.github.io/FollKernel/)

</div>

| 文件 | 大小 | 说明 |
|------|------|------|
| `FollKernel_v30022_30022-release.apk` | ~10.5 MB | 管理器本体（含内置 LKM） |

---

## 构建

```bash
# 编译 APK
cd KernelSU/manager
./gradlew assembleRelease
```

提示：Windows 环境下请使用 `gradlew.bat`。

---

## 项目结构

```text
FollKernel/
├── KernelSU/          # 管理器与内核模块源码
├── kernel/            # FollKernel 内核补丁源码
├── FollKernel-site/   # 官方文档站
├── README.md          # 简体中文说明
└── README_EN.md       # English README
```

---

## 致谢

FollKernel 的成长离不开前辈们的肩膀。

| 项目 | 贡献 |
|------|------|
| [KernelSU](https://github.com/tiann/KernelSU) | 内核级 Root 能力与安全模型 |
| [FolkPatch](https://github.com/LyraVoid/FolkPatch) | 多布局与视觉灵感 |
| [KernelPatch](https://github.com/bmax121/KernelPatch) | KPM 内核补丁参考 |
| [APatch](https://github.com/bmax121/APatch) | KPM 架构灵感 |

还要感谢 **松板有希**、**心动了嘛** 等伙伴一路相伴。

---

<div align="center">

### 许可证

**GPL-3.0** — 自由开源，永远保持热爱。

*“愿你刷机不翻车，Root 不报错，模块都兼容，主题都好看。”*

</div>

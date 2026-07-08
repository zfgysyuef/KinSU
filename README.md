<p align="center">
<img src="icon.png" width="100" alt="KinSU Logo">
</p>

<h1 align="center">KinSU</h1>

<p align="center">
<strong>Android GKI Kernel Root Solution based on KernelSU</strong>
</p>

<p align="center">
<a href="https://github.com/Spring-bulid/KinSU/releases/latest"><img src="https://img.shields.io/github/v/release/Spring-bulid/KinSU?color=ec407a&style=flat-square" alt="Release"></a>
<a href="LICENSE"><img src="https://img.shields.io/badge/License-GPL--3.0-ff80ab?style=flat-square" alt="License"></a>
<a href="https://github.com/Spring-bulid/KinSU/actions"><img src="https://img.shields.io/github/actions/workflow/status/Spring-bulid/KinSU/build-kinsu-mt6989.yml?style=flat-square" alt="CI"></a>
</p>

<p align="center">
<a href="README_EN.md">English</a> | 简体中文
</p>

---

## 简介

KinSU 是基于 [KernelSU](https://github.com/tiann/KernelSU) 的 Android Root 授权管理器，面向 GKI 内核设备。它在内核层面执行 Root 授权策略，同时提供现代化的管理器界面。

## 功能

- **Root 授权管理** — 细粒度控制应用的 Root 访问权限，支持永久、单次和定时回收
- **模块管理** — 安装 / 启用 / 禁用 / 卸载模块，兼容 KernelSU 和 Magisk 格式
- **KPM 内核补丁** — 加载 `.kpm` 补丁模块，支持 GKI 模式下开机自动加载
- **AnyKernel3 刷写** — 应用内刷写 `boot` / `init_boot` 镜像，自动备份原始镜像
- **主题定制** — Material Design 3 动态取色，支持深色 / 浅色模式切换
- **应用内更新** — 自动检测 GitHub Releases 新版本并跳转下载

## 安装

1. 下载最新版本的 [KinSU APK](https://github.com/Spring-bulid/KinSU/releases/latest) 并安装
2. 确认设备内核已集成 KernelSU 或 KernelPatch
3. 启动 KinSU，授予 Root 权限即可开始使用

详细文档请访问 [KinSU 文档站](https://spring-bulid.github.io/KinSU/)。

## 构建

```bash
# 编译管理器 APK
cd KernelSU/manager
./gradlew assembleRelease
```

Windows 环境使用 `gradlew.bat`。

## 项目结构

```
KinSU/
├── KernelSU/              # 管理器与内核模块源码
│   └── manager/           # Android 管理器应用
├── kernel/                # 内核补丁源码
├── userspace/             # 用户空间组件
├── kinsu-site/            # 文档站点
└── scripts/               # 构建与辅助脚本
```

## 依赖

| 项目 | 说明 |
|------|------|
| [KernelSU](https://github.com/tiann/KernelSU) | 内核级 Root 授权框架 |
| [KernelPatch](https://github.com/bmax121/KernelPatch) | KPM 内核补丁参考实现 |

## 许可证

本项目基于 [GPL-3.0](LICENSE) 协议开源。
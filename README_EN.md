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
English | <a href="README.md">简体中文</a>
</p>

---

## Introduction

KinSU is an Android Root authorization manager built on [KernelSU](https://github.com/tiann/KernelSU), targeting GKI kernel devices. It enforces root authorization at the kernel level while providing a modern management interface.

## Features

- **Root Authorization** — Fine-grained control over app root access, with permanent, one-time, and time-limited grants
- **Module Management** — Install / enable / disable / uninstall modules, compatible with KernelSU and Magisk formats
- **KPM Kernel Patches** — Load `.kpm` patch modules with automatic boot-time loading in GKI mode
- **AnyKernel3 Flashing** — Flash `boot` / `init_boot` images from within the app, with automatic backup
- **Theming** — Material Design 3 dynamic color theming with dark / light mode support
- **In-App Updates** — Auto-detect new versions from GitHub Releases and redirect to download

## Installation

1. Download the latest [KinSU APK](https://github.com/Spring-bulid/KinSU/releases/latest) and install it
2. Ensure your device kernel integrates KernelSU or KernelPatch
3. Launch KinSU and grant root access to get started

For detailed documentation, visit the [KinSU Documentation Site](https://spring-bulid.github.io/KinSU/).

## Build

```bash
# Build the manager APK
cd KernelSU/manager
./gradlew assembleRelease
```

On Windows, use `gradlew.bat`.

## Project Structure

```
KinSU/
├── KernelSU/              # Manager and kernel module source
│   └── manager/           # Android manager application
├── kernel/                # Kernel patch source
├── userspace/             # Userspace components
├── kinsu-site/            # Documentation site
└── scripts/               # Build and utility scripts
```

## Dependencies

| Project | Description |
|---------|-------------|
| [KernelSU](https://github.com/tiann/KernelSU) | Kernel-level root authorization framework |
| [KernelPatch](https://github.com/bmax121/KernelPatch) | KPM kernel patch reference implementation |

## License

This project is licensed under [GPL-3.0](LICENSE).
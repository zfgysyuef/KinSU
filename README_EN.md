<div align="center">

<img src="icon.png" width="128" height="128" alt="KinSU" style="border-radius: 28px; box-shadow: 0 12px 40px rgba(236, 64, 122, 0.25);">

# KinSU · Gold Kernel

> *"Give root access to the one who knows your device best — yourself."*

**Advanced kernel-based root solution for Android GKI devices, built on KernelSU**

[![Release](https://img.shields.io/github/v/release/Spring-bulid/KinSU?label=Latest&color=ec407a&style=for-the-badge)](https://github.com/Spring-bulid/KinSU/releases/latest)
[![License](https://img.shields.io/badge/License-GPL--3.0-ff80ab?style=for-the-badge)](LICENSE)
[![API](https://img.shields.io/badge/API-31%2B-e040fb?style=for-the-badge)](https://android-arsenal.com/api?level=31)

**[简体中文](README.md) | English**

</div>

---

## Hello, Explorer

**KinSU** is an Android root authorization manager born from the **KernelSU** ecosystem. It never hands root access to strangers; the final decision always belongs to you.

This is not a cold toolbox, but a personal space for Android enthusiasts. Modules, KPM, theming, flashing, and logs are all organized in one place. As long as your device is powered by KernelSU or KernelPatch, KinSU will help you explore the deeper side of Android.

---

## What It Can Do for You

| Capability | Description |
|:----:|---------|
| Superuser Authorization | Decide which apps may access root, with permanent, one-time, and revocable grants |
| Module Management | Install, enable, disable, and uninstall ZIP modules, supporting KernelSU and Magisk-style modules |
| KPM Kernel Patches | Load `.kpm` patches, embeddable in GKI mode, with boot-time auto-load and manual debugging |
| AnyKernel3 Flashing | Patch `boot` / `init_boot` from within the app, with automatic backup of the original image |
| Theming | Dynamic theming, multiple accent colors, dark / light mode |
| In-App Updates | Automatically check for new versions from GitHub Releases |
| Logs & Auditing | View root authorization records, module loading logs, and kernel patch status |

---

## Why KinSU

- **Authorization belongs to you**: Root is not a birthright of apps. Every grant, denial, or revocation is your decision.
- **Kernel-level security**: Relying on KernelSU's kernel hooks and security contexts, KinSU enforces authorization policy at the lowest level.
- **Modern experience**: Material Design 3 Expressive visuals, large radii, dynamic coloring, and smooth animations.
- **Extension-friendly**: Modules, KPM, and AnyKernel3 cover different needs from apps to the kernel.
- **Transparent open source**: Source code, build process, and release assets are all public under GPL-3.0.

---

## Quick Start

```
① Install the KinSU APK
② Make sure your kernel integrates KernelSU / KernelPatch
③ Grant root, manage modules, flash, and theme your device
```

For more details, visit the [official documentation](https://spring-bulid.github.io/KinSU/).

---

## Download

[![Download APK](https://img.shields.io/badge/Download%20APK-ec407a?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Spring-bulid/KinSU/releases/latest)
[![Official Docs](https://img.shields.io/badge/Docs-7c4dff?style=for-the-badge)](https://spring-bulid.github.io/KinSU/)

| File | Size | Description |
|------|------|-------------|
| `KinSU_3.1.8_30036-release.apk` | ~8.5 MB | Manager app (includes built-in LKM) |

---

## Build

```bash
cd KernelSU/manager
./gradlew assembleRelease
```

On Windows, use `gradlew.bat`.

---

## Project Structure

```
KinSU/
├── KernelSU/          # Manager and kernel module source code
├── kernel/            # KinSU kernel patch source code
├── kinsu-site/        # Official documentation site
├── README.md          # Simplified Chinese README
└── README_EN.md       # English README
```

---

## Credits

| Project | Contribution |
|---------|--------------|
| [KernelSU](https://github.com/tiann/KernelSU) | Kernel-level root capability and security model |
| [FolkPatch](https://github.com/LyraVoid/FolkPatch) | Multi-layout and visual inspiration |
| [KernelPatch](https://github.com/bmax121/KernelPatch) | KPM kernel patch reference |
| [APatch](https://github.com/bmax121/APatch) | KPM architecture inspiration |

Special thanks to **Matsuzaka Yuki**, **mrbeer1960**, and everyone who supports the project.

---

## License

**GPL-3.0** — Free, open source, and built with passion.
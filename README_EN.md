<div align="center">

<img src="icon.png" width="128" height="128" alt="KinSU" style="border-radius: 28px; box-shadow: 0 12px 40px rgba(236, 64, 122, 0.25);">

# KinSU 路 Gold Kernel

> *"Give root access to the one who knows your device best 鈥?yourself."*

**Advanced kernel-based root solution for Android GKI devices, built on KernelSU**

[![Release](https://img.shields.io/github/v/release/Spring-bulid/KinSU?label=Latest&color=ec407a&style=for-the-badge)](https://github.com/Spring-bulid/KinSU/releases/latest)
[![License](https://img.shields.io/badge/License-GPL--3.0-ff80ab?style=for-the-badge)](LICENSE)
[![API](https://img.shields.io/badge/API-31%2B-e040fb?style=for-the-badge)](https://android-arsenal.com/api?level=31)

**[绠€浣撲腑鏂嘳(README.md) | English**

</div>

---

## Hello, Explorer

**KinSU** is an Android root authorization manager born from the **KernelSU** ecosystem. It never hands root access to strangers; the final decision always belongs to you.

This is not a cold toolbox, but a personal space for Android enthusiasts. Modules, KPM, theming, flashing, and logs are all organized in one place. As long as your device is powered by KernelSU or KernelPatch, KinSU will help you explore the deeper side of Android.

KinSU was created from a new vision of what a root manager should be: keep KernelSU's kernel-level security model at the bottom, and provide a modern, intuitive, and customizable experience on top. It is your authorization hub, module repository, flashing assistant, and theme workshop.

---

## What It Can Do for You

<div align="center">

| Capability | Description |
|:----:|---------|
| Superuser Authorization | Decide which apps may access root and which may not, with permanent, one-time, and revocable grants |
| Module Management | Install, enable, disable, and uninstall ZIP modules, supporting both KernelSU and Magisk-style modules |
| KPM Kernel Patches | Load `.kpm` patches, embeddable in GKI mode, with boot-time auto-load and manual debugging |
| AnyKernel3 Flashing | Patch `boot` / `init_boot` from within the app, with automatic backup of the original image |
| Theming & Appearance | Dynamic theming, multiple accent colors, dark / light mode 鈥?make the manager yours |
| In-App Updates | Automatically check for new versions from GitHub Releases and jump to download with one tap |
| Logs & Auditing | View root authorization records, module loading logs, and kernel patch runtime status |

</div>

---

## Why KinSU

There are many root solutions on Android, but KinSU tries to do a few things better:

- **Authorization belongs to you**: Root is not a birthright of apps. Every grant, denial, or revocation is your decision.
- **Kernel-level security**: Relying on KernelSU's kernel hooks and security contexts, KinSU enforces authorization policy at the lowest level of the system.
- **Modern experience**: Material Design 3 Expressive visuals, large radii, dynamic coloring, and smooth animations make the manager feel less like a toolbox.
- **Extension-friendly**: Modules, KPM, and AnyKernel3 cover different needs ranging from apps to the kernel.
- **Transparent open source**: Source code, build process, and release assets are all public under the GPL-3.0 license.

---

## Quick Start

```text
鈶?Install the KinSU APK
鈶?Make sure your kernel already integrates KernelSU / KernelPatch
鈶?Grant root, manage modules, flash, and theme your device
```

For more detailed installation and usage instructions, visit the [official documentation](https://spring-bulid.github.io/KinSU/).

---

## Download

<div align="center">

[![Download APK](https://img.shields.io/badge/Download%20APK-ec407a?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Spring-bulid/KinSU/releases/latest)
[![Official Docs](https://img.shields.io/badge/Official%20Docs-7c4dff?style=for-the-badge)](https://spring-bulid.github.io/KinSU/)

</div>

| File | Size | Description |
|------|------|-------------|
| `KinSU_3.1.8_30036-release.apk` | ~8.5 MB | Manager app (includes built-in LKM) |

---

## Build

```bash
# Build the manager APK
cd KernelSU/manager
./gradlew assembleRelease
```

Note: On Windows, use `gradlew.bat` instead.

---

## Project Structure

```text
KinSU/
鈹溾攢鈹€ KernelSU/          # Manager and kernel module source code
鈹溾攢鈹€ kernel/            # KinSU kernel patch source code
鈹溾攢鈹€ kinsu-site/        # Official documentation site
鈹溾攢鈹€ README.md          # Simplified Chinese README
鈹斺攢鈹€ README_EN.md       # English README
```

---

## Credits

KinSU stands on the shoulders of these projects.

| Project | Contribution |
|---------|--------------|
| [KernelSU](https://github.com/tiann/KernelSU) | Kernel-level root capability and security model |
| [FolkPatch](https://github.com/LyraVoid/FolkPatch) | Multi-layout and visual inspiration |
| [KernelPatch](https://github.com/bmax121/KernelPatch) | KPM kernel patch reference |
| [APatch](https://github.com/bmax121/APatch) | KPM architecture inspiration |

Special thanks to **Matsuzaka Yuki**, **mrbeer1960**, and everyone who supports the project.

---

<div align="center">

### License

**GPL-3.0** 鈥?Free, open source, and built with passion.

*"May your flashing succeed, root never fail, modules stay compatible, and themes always look good."*

</div>
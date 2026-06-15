# FollKernel

Kernel-based root solution for Android GKI devices, built on KernelSU architecture.

## Features

- **SuperUser** — Per-app root grant/deny with persistent profiles
- **App Profile** — UID/GID, capabilities, SELinux rules, namespaces per app
- **Modules** — OverlayFS systemless modules (Magisk-compatible)
- **WebUI** — Module web interface support
- **SuSFS** — Samsung SUSFS integration (hide/unmount/spoof)
- **SuLog** — Root access audit log
- **Template** — Reusable root profile templates
- **Module Repo** — Browse & install modules from online repositories
- **Flash** — Direct ZIP/APK flash without recovery
- **Boot Patch** — Install/update KSU to boot image, OTA-aware
- **LKM Mode** — Load kernel module at runtime (no kernel rebuild needed)
- **KPM Mode** — Kernel Patch Module support (inline hook / syscall table hook)
- **ADB Root** — ADB root shell with profile control
- **Safe Mode** — Boot with modules disabled
- **Jailbreak** — Late-load KSU via magica on permissive SELinux
- **7 KMI Coverage** — android12-5.10 through android16-6.12
- **30+ Languages** — Full i18n support
- **Soft Rounded Icons** — Refined 14dp icon corners

## Quick Start

1. Install `FollKernel-v0.0.1.apk`
2. If app shows "Not installed" but you have root → tap **Load LKM** to auto-detect KMI and load the built-in kernel module
3. Or go to Install → Direct install to patch and flash boot image

## Download

[Latest Release](https://github.com/Spring-bulid/FollKernel/releases/tag/v0.0.1)

## Build

```bash
# APK
cd KernelSU/manager
./gradlew assembleRelease

# LKM modules (WSL)
wsl -d Ubuntu-22.04 -u root -- bash /mnt/d/rekernel/_rebuild_all_ko.sh
```

## Credits

- [KernelSU](https://github.com/tiann/KernelSU) — Core architecture
- [ReSukiSU](https://github.com/ReSukiSU/ReSukiSU) — SUSFS integration
- [APatch](https://github.com/bmax121/APatch) / [KernelPatch](https://github.com/bmax121/KernelPatch) — KPM reference
- [cctv18](https://github.com/cctv18) — SM8650 builder scripts

## License

GPL-3.0

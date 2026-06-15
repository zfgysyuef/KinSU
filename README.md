# FollKernel

Advanced kernel-based root solution for Android GKI devices, built on the KernelSU architecture with extensive enhancements.

[![GitHub Release](https://img.shields.io/github/v/release/Spring-bulid/FollKernel?label=release)](https://github.com/Spring-bulid/FollKernel/releases/tag/v0.0.1)
![License](https://img.shields.io/badge/license-GPL--3.0-blue)

## All Features

### Root & Permission Management

- **SuperUser** — Per-app root grant/deny with allow-once / always / never / timeout modes
- **App Profile** — Fine-grained control: UID/GID remapping, capabilities, SELinux domain rules, mount namespace isolation
- **Root Profile Templates** — Reusable root permission profiles (grant all / deny all / custom)
- **ADB Root** — ADB root shell with per-app profile enforcement
- **SuLog** — Complete root access audit log, searchable and exportable

### Module System

- **Systemless Modules** — OverlayFS-based Magisk-compatible module framework
- **Module Action Support** — Execute custom actions from module's `action.sh`
- **Module Configuration** — Per-module key-value settings (persistent & temporary)
- **Module Lua Scripting** — Lua runtime for module automation
- **WebUI** — Built-in WebView server for module web interfaces (`assets/` HTML/CSS/JS)
- **Module Repo** — Browse, search, download and install modules from online repositories
- **Module Shortcuts** — Launcher shortcuts for module quick actions
- **Module Update Checker** — Automatic version check against GitHub releases

### Kernel Integration

- **Boot Patch** — Direct install/update KSU into boot image, OTA-aware slot management
- **LKM Mode** — Load kernel module dynamically (insmod) without kernel rebuild
- **Bundle LKM** — Built-in kernel modules for all supported KMIs, one-tap load from APK
- **KPM Mode** — Kernel Patch Module: inline-hook and syscall-table-hook support
- **Late Load / Jailbreak** — Load KSU post-boot on permissive SELinux (magica service)
- **Safe Mode** — Boot with all modules disabled for recovery

### SuSFS Integration

- **SUS_PATH** — Hide files and directories from userspace
- **SUS_MOUNT** — Hide mount points from `/proc/mounts`
- **SUS_KSTAT** — Hide kernel kstat entries
- **TRY_UMOUNT** — Force unmount of suspicious bind mounts
- **SPOOF_UNAME** — Spoof kernel uname string
- **OPEN_REDIRECT** — Redirect file open attempts
- **SUS_MAP** — Map file paths for hiding

### OTA & Installation

- **Direct Install** — Flash patched boot image directly from app
- **Select File** — Install from local boot.img or LKM .ko file
- **OTA Survival** — Install to inactive slot after OTA
- **Restore Stock Boot** — Flash clean boot image for uninstallation
- **Flash Modules** — Install module ZIPs directly without recovery

### User Experience

- **44 Locale Languages** — Arabic, Azerbaijani, Bulgarian, Bengali, Bosnian, Danish, German, Spanish, Estonian, Persian, Filipino, French, Galician, Hindi, Croatian, Hungarian, Indonesian, Italian, Hebrew, Japanese, Khmer, Kannada, Korean, Lithuanian, Latvian, Marathi, Malay, Burmese, Dutch, Polish, Portuguese (BR/PT), Romanian, Russian, Slovenian, Serbian, Telugu, Thai, Turkish, Ukrainian, Vietnamese, Chinese (Simplified/Traditional/HK)
- **Material You Design** — Dynamic color theming with light/dark mode
- **Color Palette** — Configurable accent color presets
- **Soft Rounded Icons** — Refined 14dp corner radius for all app icons
- **Search** — Full-text search across SuperUser list (app name / package / UID)
- **Pin/Password Lock** — Optional app lock for SuperUser management
- **Reboot Menu** — Reboot / Soft reboot / Recovery / Bootloader / Download mode
- **Device Info** — Kernel version, SELinux status, seccomp, LKM/GKI detection

### Security & Audit

- **SELinux Checker** — Verify SELinux enforcing status
- **Network Security Config** — HTTPS-only with strict certificate validation
- **ADB Root Guard** — ADB root only in permissive mode with user confirmation
- **Sulog Export** — Log export with device/fingerprint/KMI metadata included

### SM8650 Build System

- **7 KMI Targets** — android12-5.10, android13-5.10, android13-5.15, android14-5.15, android14-6.1, android15-6.6, android16-6.12
- **OnePlus 12 / OPPO Find X7 Ultra** — Dedicated SM8650 builder scripts with KPM/SUSFS/BBR/LZ4
- **ReSukiSU KernelSU** — SUSFS-ready kernel integration
- **KernelSU Next Support** — Optional KSU Next with WildKSU manager patch

## Quick Start

1. Install `FollKernel-v0.0.1.apk`
2. If app shows "Not installed" but you have root → tap **Load LKM** to auto-detect KMI and load the built-in kernel module
3. Or go to **Install** → **Direct install** to patch and flash the boot image

## Download

[Latest Release → FollKernel v0.0.1](https://github.com/Spring-bulid/FollKernel/releases/tag/v0.0.1)

| File | Description |
|------|-------------|
| `FollKernel-v0.0.1.apk` | Manager APK with bundled LKM modules |
| `FollKernel-LKM-KPM.zip` | Standalone KMI kernel modules (7 KMIs) |

## Build

```bash
# APK
cd KernelSU/manager
./gradlew assembleRelease

# LKM kernel modules (WSL)
wsl -d Ubuntu-22.04 -u root -- bash /mnt/d/rekernel/_rebuild_all_ko.sh
```

## Credits

- [KernelSU](https://github.com/tiann/KernelSU) — Core manager architecture & module system
- [ReSukiSU](https://github.com/ReSukiSU/ReSukiSU) — SUSFS integration and kernel feature set
- [KernelPatch](https://github.com/bmax121/KernelPatch) / [APatch](https://github.com/bmax121/APatch) — KPM kernel patching reference
- [cctv18](https://github.com/cctv18) — SM8650 builder scripts and SUSFS kernel patches
- [KernelSU Next](https://github.com/KernelSU-Next/KernelSU-Next) — Optional KSU Next kernel backend

## License

GPL-3.0 — Free and open source software.

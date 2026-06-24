# KinSU

**[English](#english) | [中文](#中文) | [Русский](#русский) | [日本語](#日本語)**

---

## English

A kernel-based root manager for Android, based on [KernelSU](https://github.com/tiann/KernelSU) with LKM (Loadable Kernel Module) as the primary mode.

### Features

- LKM (Loadable Kernel Module) as the primary working mode
- GKI device support with kernel-level root access
- Module management with WebUI support
- Superuser management with app profile
- SuSFS (Suspend FS) support
- KPM (Kernel Patch Module) support
- Customizable theme with dynamic color palette
- Font selection (Default / iPhone style)
- Multi-language support

### Requirements

- Android device with LKM or GKI support
- Unlocked bootloader

### Version

- Manager: 1.12 (11250)
- Minimum kernel version: 11250

### Build

```bash
./gradlew assembleRelease
```

The APK will be generated at `app/build/outputs/apk/release/app-release.apk`.

### License

This project is based on [KernelSU](https://github.com/tiann/KernelSU).

### Contributors

This project is based on KernelSU, thanks to all its contributors. See the full list on the [KinSU Website](https://spring-bulid.github.io/).

---

## 中文

基于 [KernelSU](https://github.com/tiann/KernelSU) 的 Android 内核级 Root 管理器，以 LKM（可加载内核模块）为基准模式。

### 功能特性

- 以 LKM（可加载内核模块）为基准工作模式
- 支持 GKI 设备内核级 Root 权限
- 模块管理，支持 WebUI
- 超级用户管理，支持应用配置文件
- SuSFS（挂载隔离）支持
- KPM（内核补丁模块）支持
- 可自定义主题，支持动态取色
- 字体选择（默认 / iPhone 风格）
- 多语言支持

### 环境要求

- 支持 LKM 或 GKI 的 Android 设备
- 已解锁 Bootloader

### 版本信息

- 管理器版本：1.12 (11250)
- 最低内核版本：11250

### 构建

```bash
./gradlew assembleRelease
```

APK 输出路径：`app/build/outputs/apk/release/app-release.apk`

### 许可证

本项目基于 [KernelSU](https://github.com/tiann/KernelSU) 开发。

### 贡献者

本项目基于 KernelSU 开发，感谢所有贡献者。完整列表见 [KinSU 官网](https://spring-bulid.github.io/)。

---

## Русский

Менеджер root-прав на уровне ядра для Android, основанный на [KernelSU](https://github.com/tiann/KernelSU) с LKM (загружаемый модуль ядра) в качестве основного режима.

### Возможности

- LKM (загружаемый модуль ядра) как основной рабочий режим
- Поддержка устройств GKI с root-доступом на уровне ядра
- Управление модулями с поддержкой WebUI
- Управление суперпользователем с профилями приложений
- Поддержка SuSFS (изоляция монтирования)
- Поддержка KPM (модуль патча ядра)
- Настраиваемая тема с динамической палитрой цветов
- Выбор шрифта (Стандартный / Стиль iPhone)
- Многоязычная поддержка

### Требования

- Устройство Android с поддержкой LKM или GKI
- Разблокированный загрузчик

### Версия

- Менеджер: 1.12 (11250)
- Минимальная версия ядра: 11250

### Сборка

```bash
./gradlew assembleRelease
```

APK будет создан по пути `app/build/outputs/apk/release/app-release.apk`.

### Лицензия

Этот проект основан на [KernelSU](https://github.com/tiann/KernelSU).

### Участники

Этот проект основан на KernelSU, спасибо всем участникам. Полный список на [сайте KinSU](https://spring-bulid.github.io/).

---

## 日本語

[KernelSU](https://github.com/tiann/KernelSU) をベースにした Android カーネルレベルのルートマネージャー。LKM（ローダブルカーネルモジュール）を基準モードとしています。

### 機能

- LKM（ローダブルカーネルモジュール）を基準動作モードとする
- GKI デバイス向けカーネルレベルのルートアクセス対応
- WebUI 対応モジュール管理
- アプリプロファイル付きスーパーユーザー管理
- SuSFS（マウント分離）対応
- KPM（カーネルパッチモジュール）対応
- ダイナミックカラー対応のカスタムテーマ
- フォント選択（デフォルト / iPhone スタイル）
- 多言語対応

### 動作要件

- LKM または GKI 対応の Android デバイス
- ブートローダー解除済み

### バージョン

- マネージャー：1.12 (11250)
- 最小カーネルバージョン：11250

### ビルド

```bash
./gradlew assembleRelease
```

APK は `app/build/outputs/apk/release/app-release.apk` に出力されます。

### ライセンス

このプロジェクトは [KernelSU](https://github.com/tiann/KernelSU) をベースにしています。

### 貢献者

このプロジェクトは KernelSU をベースにしており、全ての貢献者に感謝します。完全なリストは [KinSU ウェブサイト](https://spring-bulid.github.io/) をご覧ください。

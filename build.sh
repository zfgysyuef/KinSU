#!/usr/bin/env bash
set -Eeuo pipefail

# Build the arm64 ksuinit/ksud binaries and a debug manager APK.
# Requirements: JDK 21, Rust, Android SDK, and Linux Android NDK r29.

ROOT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
NDK_VERSION="29.0.14206865"
ANDROID_API="26"
TRIPLE="aarch64-linux-android"

export PATH="$HOME/.cargo/bin:$PATH"
cd "$ROOT_DIR"

fail() {
    echo "error: $*" >&2
    exit 1
}

command -v cargo >/dev/null || fail "cargo is not installed"
command -v rustup >/dev/null || fail "rustup is not installed"
command -v java >/dev/null || fail "JDK 21 is not installed"

SDK_ROOT="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
[[ -n "$SDK_ROOT" ]] || fail "set ANDROID_SDK_ROOT (or ANDROID_HOME) to a Linux Android SDK"

NDK_HOME="${ANDROID_NDK_HOME:-$SDK_ROOT/ndk/$NDK_VERSION}"
LLVM_BIN="$NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin"
CLANG="$LLVM_BIN/${TRIPLE}${ANDROID_API}-clang"

[[ -x "$CLANG" ]] || fail "Linux NDK $NDK_VERSION was not found at $NDK_HOME"
[[ -f manager/gradlew ]] || fail "manager/gradlew is missing"
command -v unzip >/dev/null || fail "unzip is not installed"

export ANDROID_HOME="$SDK_ROOT"
export ANDROID_SDK_ROOT="$SDK_ROOT"
export ANDROID_NDK_HOME="$NDK_HOME"

echo "[1/4] Building static arm64 ksuinit"
rustup target add aarch64-unknown-linux-musl
export CARGO_TARGET_AARCH64_UNKNOWN_LINUX_MUSL_LINKER="$CLANG"
RUSTFLAGS="-C link-arg=-no-pie" \
    cargo build --locked --package ksuinit --target aarch64-unknown-linux-musl --release

mkdir -p userspace/ksud/bin/aarch64
cp -f target/aarch64-unknown-linux-musl/release/ksuinit \
    userspace/ksud/bin/aarch64/ksuinit

echo "[2/4] Building Android arm64 ksud"
rustup target add "$TRIPLE"
# Reuse the same NDK environment as the release workflow.
source .github/scripts/setup-rust-build.sh "$TRIPLE" "$ANDROID_API"
# rust-embed does not always notice replaced files under userspace/ksud/bin.
cargo clean --package ksud --target "$TRIPLE" --release
cargo build --locked --package ksud --target "$TRIPLE" --release

mkdir -p manager/app/src/main/jniLibs/arm64-v8a
cp -f "target/$TRIPLE/release/ksud" \
    manager/app/src/main/jniLibs/arm64-v8a/libkinsud.so

echo "[3/4] Building manager APK"
(cd manager && bash ./gradlew --no-daemon :app:clean :app:assembleDebug)

echo "[4/4] Collecting outputs"
mkdir -p dist
mapfile -t APKS < <(find manager/app/build/outputs/apk/debug -maxdepth 1 \
    -type f -name '*-debug.apk' -print)
(( ${#APKS[@]} == 1 )) || fail "expected exactly one debug APK, found ${#APKS[@]}"

APK_ENTRIES="$(unzip -Z1 "${APKS[0]}")"
for entry in \
    lib/arm64-v8a/libkinsud.so \
    lib/arm64-v8a/libKinSU.so \
    lib/arm64-v8a/libadbroot.so \
    assets/kpm/kpimg \
    assets/kpm/kptools; do
    grep -Fxq "$entry" <<<"$APK_ENTRIES" || fail "APK is missing $entry"
done

cp -f "${APKS[0]}" dist/KinSU-KPM-debug.apk
cp -f "target/$TRIPLE/release/ksud" dist/ksud-arm64
cp -f target/aarch64-unknown-linux-musl/release/ksuinit dist/ksuinit-arm64

echo
echo "Build complete:"
sha256sum dist/KinSU-KPM-debug.apk dist/ksud-arm64 dist/ksuinit-arm64

#!/usr/bin/env bash
# Build kptools-android and kpimg from SukiSU_KernelPatch_patch
# This script must be run on Linux (or WSL) with Android NDK installed

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
FOLLKERNEL_DIR="$(dirname "$SCRIPT_DIR")"
KP_PATCH_DIR="$FOLLKERNEL_DIR/SukiSU_KernelPatch_patch"
KSUD_BIN_DIR="$FOLLKERNEL_DIR/KernelSU/userspace/ksud/bin/aarch64"

# Check if SukiSU_KernelPatch_patch exists
if [ ! -d "$KP_PATCH_DIR" ]; then
    echo "ERROR: SukiSU_KernelPatch_patch not found at $KP_PATCH_DIR"
    echo "Please clone it first: git clone https://github.com/SukiSU-Ultra/SukiSU_KernelPatch_patch.git"
    exit 1
fi

# Check for Android NDK
if [ -z "$ANDROID_NDK_HOME" ]; then
    # Try common NDK locations
    if [ -d "$HOME/Android/Sdk/ndk" ]; then
        ANDROID_NDK_HOME=$(ls -d "$HOME/Android/Sdk/ndk/"* | sort -V | tail -1)
    elif [ -d "/opt/android-ndk" ]; then
        ANDROID_NDK_HOME="/opt/android-ndk"
    else
        echo "ERROR: ANDROID_NDK_HOME not set and NDK not found"
        echo "Please set ANDROID_NDK_HOME to your NDK installation path"
        exit 1
    fi
fi

echo "Using NDK: $ANDROID_NDK_HOME"

# Build kpimg
echo "=== Building kpimg ==="
cd "$KP_PATCH_DIR/kernel"
make clean 2>/dev/null || true
make ANDROID=1 CLANG_PATH="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin"
if [ ! -f "res/kpimg" ]; then
    echo "ERROR: kpimg build failed"
    exit 1
fi
echo "kpimg built successfully"

# Build kptools-android
echo "=== Building kptools-android ==="
cd "$KP_PATCH_DIR/tools"
mkdir -p build-android && cd build-android
cmake .. \
    -DCMAKE_TOOLCHAIN_FILE="$ANDROID_NDK_HOME/build/cmake/android.toolchain.cmake" \
    -DANDROID_ABI=arm64-v8a \
    -DANDROID_PLATFORM=android-21 \
    -DCMAKE_BUILD_TYPE=Release
make -j$(nproc)
if [ ! -f "kptools" ]; then
    echo "ERROR: kptools-android build failed"
    exit 1
fi
echo "kptools-android built successfully"

# Copy binaries to ksud assets
echo "=== Installing binaries ==="
cp "$KP_PATCH_DIR/kernel/res/kpimg" "$KSUD_BIN_DIR/kpimg"
cp "$KP_PATCH_DIR/tools/build-android/kptools" "$KSUD_BIN_DIR/kptools"
chmod 755 "$KSUD_BIN_DIR/kptools"

echo "=== Done ==="
echo "kpimg: $(wc -c < "$KSUD_BIN_DIR/kpimg") bytes"
echo "kptools: $(wc -c < "$KSUD_BIN_DIR/kptools") bytes"
echo ""
echo "Now rebuild ksud with: cd KernelSU/userspace/ksud && cargo build --target aarch64-linux-android --release"

#!/bin/bash
set -e
# Build follkernel.ko for android14-6.1 with CORRECT signature values
# Using OPPO OnePlus SM8650 kernel source (NOT GKI)

KERNEL_SRC="/tmp/android_kernel_common_oneplus_sm8650-oneplus-sm8650_b_16.0.0_oneplus12"
MODULE_SRC="/mnt/d/KinSU/KernelSU/kernel"
OUT_DIR="/tmp/ksu_build_out"

# Signature values from extract_sig.py
export KSU_EXPECTED_SIZE=0x2d4
export KSU_EXPECTED_HASH="eb4ae49882e018241dc118c1bad8697da6f744c6583b55fa4371784634e9cccf"

echo "=== Extracting kernel source ==="
cd /tmp
# Remove old extraction if exists
rm -rf "$KERNEL_SRC" 2>/dev/null || true
unzip -q oneplus_kernel.zip
ls -la "$KERNEL_SRC/Makefile"

echo "=== Building follkernel.ko ==="
cd "$MODULE_SRC"
mkdir -p "$OUT_DIR"

# Use the same build approach as CI
make -C "$KERNEL_SRC" \
    M="$OUT_DIR" \
    src="$MODULE_SRC" \
    ARCH=arm64 \
    CROSS_COMPILE=aarch64-linux-gnu- \
    CC=clang \
    LD=ld.lld \
    CONFIG_KSU=m \
    KSU_EXPECTED_SIZE="$KSU_EXPECTED_SIZE" \
    KSU_EXPECTED_HASH="$KSU_EXPECTED_HASH" \
    -j$(nproc) modules 2>&1 | tail -30

echo "=== Stripping ==="
llvm-strip -d "$OUT_DIR/follkernel.ko" 2>/dev/null || aarch64-linux-gnu-strip --strip-debug "$OUT_DIR/follkernel.ko" 2>/dev/null || true

echo "=== Result ==="
ls -la "$OUT_DIR/follkernel.ko"
echo "=== Verifying signature ==="
strings "$OUT_DIR/follkernel.ko" | grep -c "$KSU_EXPECTED_HASH" && echo "HASH FOUND" || echo "Hash not in strings (normal for stripped .ko)"
echo "=== Copying to bin/aarch64 ==="
cp "$OUT_DIR/follkernel.ko" "/mnt/d/KinSU/KernelSU/userspace/ksud/bin/aarch64/android14-6.1_follkernel.ko"
echo "DONE"

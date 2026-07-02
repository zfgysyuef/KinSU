#!/bin/bash
# Build the 3 failed KMIs
set -e

KERNEL_SRC="/mnt/d/KinSU/KernelSU/kernel"
OUT_DIR="/mnt/d/KinSU/KernelSU/userspace/ksud/bin/aarch64"

for kmi in android12-5.10 android13-5.10 android13-5.15; do
  DDK_ROOT="/home/hanha/ddk-build/${kmi//./_}-20260313"
  echo "============================================"
  echo "=== Building $kmi ==="
  echo "============================================"
  bash /mnt/d/KinSU/tmp/build_ko_chroot.sh "$kmi" "$DDK_ROOT" "$KERNEL_SRC" "$OUT_DIR" 2>&1 | tail -25
  echo
done

echo "============================================"
echo "=== All .ko files ==="
echo "============================================"
ls -la "$OUT_DIR"/*_follkernel.ko

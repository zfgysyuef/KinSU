#!/bin/bash
# Build all 7 KMI .ko files
set -e

DDK_RELEASE="20260313"
KERNEL_SRC="/mnt/d/KinSU/KernelSU/kernel"
OUT_DIR="/mnt/d/KinSU/KernelSU/userspace/ksud/bin/aarch64"

KMIS=(
  "android12-5.10"
  "android13-5.10"
  "android13-5.15"
  "android14-5.15"
  "android14-6.1"
  "android15-6.6"
  "android16-6.12"
)

for kmi in "${KMIS[@]}"; do
  DDK_ROOT="/home/hanha/ddk-build/${kmi//./_}-${DDK_RELEASE}"
  if [ ! -d "$DDK_ROOT/opt/ddk" ]; then
    echo "=== SKIP $kmi (DDK not downloaded) ==="
    continue
  fi
  echo "============================================"
  echo "=== Building $kmi ==="
  echo "============================================"
  bash /mnt/d/KinSU/tmp/build_ko_chroot.sh "$kmi" "$DDK_ROOT" "$KERNEL_SRC" "$OUT_DIR" 2>&1 | tail -30
  echo
done

echo "============================================"
echo "=== All builds complete ==="
echo "============================================"
ls -la "$OUT_DIR"/*_follkernel.ko

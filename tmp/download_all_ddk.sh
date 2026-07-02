#!/bin/bash
# Download all remaining KMI DDK images
set -e

KMIS=(
  "android12-5.10"
  "android13-5.10"
  "android13-5.15"
  "android14-5.15"
  "android15-6.6"
  "android16-6.12"
)

DDK_RELEASE="20260313"

for kmi in "${KMIS[@]}"; do
  OUT_DIR="/home/hanha/ddk-build/${kmi//./_}-${DDK_RELEASE}"
  if [ -d "$OUT_DIR/opt/ddk" ]; then
    echo "=== SKIP $kmi (already exists) ==="
    continue
  fi
  echo "=== Downloading $kmi ==="
  python3 /mnt/d/KinSU/tmp/pull_ddk.py "${kmi}-${DDK_RELEASE}" 2>&1 | tail -20
  echo "=== Done $kmi ==="
  echo
done

echo "=== All downloads complete ==="
ls -d /home/hanha/ddk-build/*/

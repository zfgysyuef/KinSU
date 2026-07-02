#!/bin/bash
KO_DIR="/mnt/d/KinSU/KernelSU/userspace/ksud/bin/aarch64"
for f in ${KO_DIR}/*_follkernel.ko; do
  echo "=== $(basename $f) ==="
  modinfo "$f" 2>/dev/null | grep -E 'vermagic|description|license|depends|name' || echo "modinfo failed, trying strings"
  strings "$f" | grep -E 'vermagic' | head -3
  echo
done

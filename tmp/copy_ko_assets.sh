#!/bin/bash
set -e
KO_SRC="/mnt/d/KinSU/KernelSU/userspace/ksud/bin/aarch64"
KO_DST="/mnt/d/KinSU/KernelSU/manager/app/src/main/assets/lkm"

for kmi in android12-5.10 android13-5.10 android13-5.15 android14-5.15 android14-6.1 android15-6.6 android16-6.12; do
  cp "${KO_SRC}/${kmi}_follkernel.ko" "${KO_DST}/${kmi}_rekernel.ko"
  echo "Copied ${kmi}_follkernel.ko -> ${kmi}_rekernel.ko"
done

echo "Done!"
ls -la "$KO_DST/"

#!/bin/bash
set -e
cd /mnt/d/FollKernel/KernelSU

git commit -m "$(cat <<'EOF'
fix: v3.1.6 - fix kinsu.ko not loading due to filename mismatch

Critical fix: ksuinit was loading /follkernel.ko (legacy FollKernel name)
but boot_patch.rs was placing KinSU.ko into ramdisk, causing the kernel
module to never load and manager to show "not installed".

Changes:
- Rebuild ksuinit to load /KinSU.ko (matches boot_patch.rs)
- Fix boot_patch.rs: kinsu.ko -> KinSU.ko in ramdisk (4 places)
- Unify libkinsud.so filename across KsuCli.kt, repack_apk.py, jniLibs
- Remove panda icon from HomeMaterial top bar
- GKI detection prioritizes init_boot partition over uname
- Bump version to 3.1.6 (30034)
EOF
)"

echo "=== Commit done ==="
git log --oneline -3

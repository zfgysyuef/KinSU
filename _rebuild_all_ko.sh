#!/bin/bash
set -u

# === IMPORTANT: Before building, update KSU_EXPECTED_SIZE and KSU_EXPECTED_HASH ===
# Run: python3 /mnt/d/rekernel/extract_cert.py <path-to-rekernel-manager.apk>
# Then update the values in /mnt/d/rekernel/KernelSU/kernel/Kbuild:
#   KSU_EXPECTED_SIZE := 0x<size_from_output>
#   KSU_EXPECTED_HASH := <hash_from_output>
#
# The default values below are placeholder/dummy signatures:
#   KSU_EXPECTED_SIZE := 0x2e8
#   KSU_EXPECTED_HASH := 48c973bf9702ba7013e5986e45996454e0bc8389397737b75dfe12903deb1ad9
# The hash 48c973bf... on line ~61 below is also a placeholder used for post-build verification.
# These MUST be updated to match the actual Rekernel Manager APK before production builds.
#
# Rebuild all 7 KMIs with updated EXPECTED_SIZE/EXPECTED_HASH for Rekernel debug signature
declare -A CLANG_MAP=(
  ["android12-5.10"]="clang-r416183b"
  ["android13-5.10"]="clang-r450784e"
  ["android13-5.15"]="clang-r450784e"
  ["android14-5.15"]="clang-r487747c"
  ["android14-6.1"]="clang-r487747c"
  ["android15-6.6"]="clang-r510928"
  ["android16-6.12"]="clang-r536225"
)
# KMIs that need BTF disabled (pahole too old for 6.6/6.12)
NEW_KMIS="android15-6.6 android16-6.12"

KERN=/root/rekernel-kernel
OUT=/mnt/d/rekernel/_ko_output
mkdir -p "$OUT"

# sync updated Kbuild from Windows repo
cp /mnt/d/rekernel/KernelSU/kernel/Kbuild "$KERN/Kbuild"
grep -E 'KSU_EXPECTED_SIZE|KSU_EXPECTED_HASH' "$KERN/Kbuild" | grep -v ifndef | grep -v endif

cd "$KERN"
for kmi in android12-5.10 android13-5.10 android13-5.15 android14-5.15 android14-6.1 android15-6.6 android16-6.12; do
  clang=${CLANG_MAP[$kmi]}
  KDIR=/opt/ddk/kdir/$kmi
  echo ""
  echo "========== BUILDING $kmi with $clang =========="

  make -C "$KDIR" M="$KERN" src="$KERN" clean 2>/dev/null 1>&2 || true
  rm -f "$KERN"/kernelsu.ko "$KERN"/kernelsu.o "$KERN"/Module.symvers 2>/dev/null

  export KDIR
  export PATH=/opt/ddk/clang/$clang/bin:$PATH
  export CROSS_COMPILE=aarch64-linux-gnu-
  export ARCH=arm64
  export LLVM=1
  export LLVM_IAS=1
  export CONFIG_KSU=m

  BTF_OPTS=""
  for nk in $NEW_KMIS; do
    if [ "$kmi" = "$nk" ]; then BTF_OPTS="PAHOLE=/bin/true RESOLVE_BTFIDS=/bin/true"; fi
  done

  if make KSU_MANAGER_PACKAGE="com.mikokernel" $BTF_OPTS -C "$KDIR" M="$KERN" src="$KERN" modules -j"$(nproc)" 2>&1 | tail -2; then
    if [ -f "$KERN/follkernel.ko" ]; then
      llvm-strip -d "$KERN/follkernel.ko"
      cp "$KERN/follkernel.ko" "$OUT/${kmi}_rekernel.ko"
      sz=$(stat -c%s "$OUT/${kmi}_rekernel.ko")
      hash_in=$(strings "$OUT/${kmi}_rekernel.ko" 2>/dev/null | grep -o '48c973bf9702ba7013e5986e45996454e0bc8389397737b75dfe12903deb1ad9' | head -1)
      pkg_in=$(strings "$OUT/${kmi}_rekernel.ko" 2>/dev/null | grep -o 'com.mikokernel' | head -1)
      echo "OK $kmi -> ${kmi}_rekernel.ko ($sz bytes) hash_ok=${hash_in:+yes} pkg=${pkg_in:+com.mikokernel}"
    else
      echo "FAIL $kmi: no follkernel.ko"
    fi
  else
    echo "FAIL $kmi: compile error"
  fi
done

echo ""
echo "========== FINAL OUTPUT =========="
ls -la "$OUT"/*.ko 2>/dev/null
echo "DONE_ALL"

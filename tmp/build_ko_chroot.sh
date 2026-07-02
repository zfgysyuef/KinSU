#!/bin/bash
# Build KinSU .ko for a specific KMI using DDK rootfs via chroot
# Usage: build_ko_chroot.sh <kmi> <ddk_rootfs> <kernel_src_dir> <output_dir>
set -e

KMI="$1"
DDK_ROOT="$2"
KERNEL_SRC="$3"
OUT_DIR="$4"

case "$KMI" in
  android12-5.10) CLANG_VER="clang-r416183b" ;;
  android13-5.10|android13-5.15) CLANG_VER="clang-r450784e" ;;
  android14-5.15|android14-6.1) CLANG_VER="clang-r487747c" ;;
  android15-6.6) CLANG_VER="clang-r510928" ;;
  android16-6.12) CLANG_VER="clang-r536225" ;;
  *) echo "Unknown KMI: $KMI"; exit 1 ;;
esac

echo "=== Build KinSU .ko for $KMI (clang=$CLANG_VER) ==="

# Copy kernel source into DDK rootfs
TMP_KERNEL="$DDK_ROOT/tmp/follkernel-src"
TMP_UAPI="$DDK_ROOT/tmp/uapi"
echo "[1/6] Copying kernel source..."
rm -rf "$TMP_KERNEL" "$TMP_UAPI"
mkdir -p "$TMP_KERNEL"
cp -a "$KERNEL_SRC"/* "$TMP_KERNEL"/ 2>/dev/null || true
cp -a "$KERNEL_SRC"/.git "$TMP_KERNEL"/ 2>/dev/null || true

# Copy uapi directory (kernel/include/uapi is a symlink to ../../uapi)
UAPI_SRC="$KERNEL_SRC/../uapi"
if [ -d "$UAPI_SRC" ]; then
  cp -a "$UAPI_SRC" "$TMP_UAPI"
  # Replace the broken symlink file with a proper symlink
  rm -f "$TMP_KERNEL/include/uapi"
  ln -s /tmp/uapi "$TMP_KERNEL/include/uapi"
fi

# Write build script
cat > "$DDK_ROOT/tmp/build.sh" <<'BUILDEOF'
#!/bin/bash
set -e

# Read variables from /tmp/build.env
source /tmp/build.env

export PATH=$CLANG_BIN:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
export LD_LIBRARY_PATH=$CLANG_LIB
export KDIR=$KDIR_VAL
export CROSS_COMPILE=aarch64-linux-gnu-
export ARCH=arm64
export LLVM=1
export LLVM_IAS=1
export CC=clang
export CONFIG_KSU=m
export KSU_EXPECTED_SIZE=0x2d4
export KSU_EXPECTED_HASH=eb4ae49882e0ea80c13989ea2141368b1f819e2bfe10801b3c15b743bd647dab
export KSU_MANAGER_PACKAGE=com.mikokernel

echo "[chroot] PATH=$PATH"
echo "[chroot] clang: $(clang --version 2>&1 | head -1)"
echo "[chroot] KDIR: $KDIR"
echo "[chroot] nproc: $(nproc)"

cd /tmp/follkernel-src
echo "[chroot] Building follkernel.ko..."
make -j$(nproc) 2>&1 || {
  echo "=== BUILD FAILED, verbose retry ==="
  make V=1 -j1 2>&1 | tail -80
  exit 1
}

echo "[chroot] Build complete!"
ls -la follkernel.ko

# Strip debug symbols
llvm-strip -d follkernel.ko 2>/dev/null || strip -d follkernel.ko 2>/dev/null || true
echo "[chroot] Stripped"

cp follkernel.ko /tmp/output_ko.ko
echo "[chroot] Done"
BUILDEOF

# Write env file - auto-detect lib directory (lib64 for old clang, lib for new)
DDK_ROOT_ABS="$(cd "$DDK_ROOT" && pwd)"
if [ -d "$DDK_ROOT_ABS/opt/ddk/clang/$CLANG_VER/lib64" ]; then
  CLANG_LIB_DIR="lib64"
else
  CLANG_LIB_DIR="lib"
fi
cat > "$DDK_ROOT/tmp/build.env" <<EOF
CLANG_BIN=/opt/ddk/clang/$CLANG_VER/bin
CLANG_LIB=/opt/ddk/clang/$CLANG_VER/$CLANG_LIB_DIR
KDIR_VAL=/opt/ddk/kdir/$KMI
EOF

chmod +x "$DDK_ROOT/tmp/build.sh"

echo "[2/6] Setting up chroot mounts..."
# Create dev nodes if missing
mkdir -p "$DDK_ROOT/dev" "$DDK_ROOT/proc"

echo "[3/6] Building in chroot..."
# Use unshare with mount namespace to bind mount /dev and /proc
unshare --user --map-root-user --mount bash -c "
  mount --bind /dev '$DDK_ROOT/dev' 2>/dev/null || true
  mount --bind /proc '$DDK_ROOT/proc' 2>/dev/null || true
  chroot '$DDK_ROOT' /bin/bash /tmp/build.sh
"

echo "[4/6] Copying .ko out..."
mkdir -p "$OUT_DIR"
cp "$DDK_ROOT/tmp/output_ko.ko" "$OUT_DIR/${KMI}_follkernel.ko"
ls -la "$OUT_DIR/${KMI}_follkernel.ko"

echo "[5/6] Cleanup..."
rm -rf "$TMP_KERNEL" "$TMP_UAPI" "$DDK_ROOT/tmp/build.sh" "$DDK_ROOT/tmp/build.env" "$DDK_ROOT/tmp/output_ko.ko"

echo "[6/6] Done: $OUT_DIR/${KMI}_follkernel.ko"

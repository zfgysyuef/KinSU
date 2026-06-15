#!/bin/bash
set -e

echo "===== FollKernel GKI Build ====="
KERNEL_SRC=/root/follkernel_src

# Install clang-20
if ! command -v clang-20 &>/dev/null; then
  echo ">>> Installing clang-20..."
  cd /tmp
  wget -nv https://apt.llvm.org/llvm.sh
  chmod +x llvm.sh
  ./llvm.sh 20 all
fi
echo ">>> Clang: $(clang-20 --version | head -1)"

# Install build deps
apt-get update -qq
apt-get install -y -qq bison flex git lld zip make gcc python3 python-is-python3 bc libssl-dev libelf-dev cpio ccache

# Clone kernel source
if [ ! -d "$KERNEL_SRC/common/.git" ]; then
  echo ">>> Cloning kernel source (~2GB)..."
  rm -rf "$KERNEL_SRC"
  mkdir -p "$KERNEL_SRC"
  cd "$KERNEL_SRC"
  git clone --depth=1 https://github.com/cctv18/android_kernel_common_oneplus_sm8650 \
    -b oneplus/sm8650_b_16.0.0_oneplus12 common
fi
cd "$KERNEL_SRC"
echo ">>> Kernel source ready"

# Clean ABI and dirty suffix
rm -f common/android/abi_gki_protected_exports_*
for f in common/scripts/setlocalversion; do
  sed -i 's/ -dirty//g' "$f"
  sed -i '$i res=$(echo "$res" | sed '\''s/-dirty//g'\'')' "$f"
done
for f in common/scripts/setlocalversion; do
  sed -i "\$s|echo \"\\\$res\"|echo \"-follkernel-1.0\"|" "$f"
done

# Integrate FollKernel KSU
echo ">>> Integrating FollKernel..."
cd "$KERNEL_SRC/common"
rm -rf KernelSU drivers/kernelsu
git clone --depth=1 https://github.com/Spring-bulid/Rekernel.git KernelSU
ln -sf "$(realpath KernelSU/kernel)" drivers/kernelsu
echo ">>> FollKernel integrated"
cd "$KERNEL_SRC"

# SuSFS patches
echo ">>> Applying SuSFS..."
git clone --depth=1 https://github.com/cctv18/susfs4oki.git susfs -b oki-android14-6.1
cp ./susfs/kernel_patches/50_add_susfs_in_gki-android14-6.1.patch ./common/
cp ./susfs/kernel_patches/fs/* ./common/fs/ 2>/dev/null || true
cp ./susfs/kernel_patches/include/linux/* ./common/include/linux/ 2>/dev/null || true
cd ./common
patch -p1 -F 3 < 50_add_susfs_in_gki-android14-6.1.patch || true
cd ..

# LZ4+ZSTD
echo ">>> LZ4 1.10.0 & ZSTD 1.5.7..."
git clone --depth=1 https://github.com/cctv18/oppo_oplus_realme_sm8650.git oppo_ref
cp ./oppo_ref/zram_patch/001-lz4.patch ./common/
cp ./oppo_ref/zram_patch/lz4armv8.S ./common/lib/
cp ./oppo_ref/zram_patch/002-zstd.patch ./common/
cd ./common
git apply -p1 < 001-lz4.patch || true
patch -p1 < 002-zstd.patch || true
cd ..
rm -rf oppo_ref susfs

# Config
echo ">>> Configuring defconfig..."
DEFCONFIG=./common/arch/arm64/configs/gki_defconfig

cat >> "$DEFCONFIG" <<'EOF'
CONFIG_KSU=y
CONFIG_KSU_MANAGER_PACKAGE=com.mikokernel
CONFIG_KSU_SUSFS=y
CONFIG_KSU_SUSFS_HAS_MAGIC_MOUNT=y
CONFIG_KSU_SUSFS_SUS_PATH=y
CONFIG_KSU_SUSFS_SUS_MOUNT=y
CONFIG_KSU_SUSFS_AUTO_ADD_SUS_KSU_DEFAULT_MOUNT=y
CONFIG_KSU_SUSFS_AUTO_ADD_SUS_BIND_MOUNT=y
CONFIG_KSU_SUSFS_SUS_KSTAT=y
CONFIG_KSU_SUSFS_TRY_UMOUNT=y
CONFIG_KSU_SUSFS_AUTO_ADD_TRY_UMOUNT_FOR_BIND_MOUNT=y
CONFIG_KSU_SUSFS_SPOOF_UNAME=y
CONFIG_KSU_SUSFS_ENABLE_LOG=y
CONFIG_KSU_SUSFS_HIDE_KSU_SUSFS_SYMBOLS=y
CONFIG_KSU_SUSFS_SPOOF_CMDLINE_OR_BOOTCONFIG=y
CONFIG_KSU_SUSFS_OPEN_REDIRECT=y
CONFIG_KSU_SUSFS_SUS_MAP=y
CONFIG_TMPFS_XATTR=y
CONFIG_TMPFS_POSIX_ACL=y
CONFIG_CC_OPTIMIZE_FOR_PERFORMANCE=y
CONFIG_HEADERS_INSTALL=n
CONFIG_MQ_IOSCHED_SSG=y
CONFIG_MQ_IOSCHED_SSG_CGROUP=y
EOF

sed -i 's/check_defconfig//' ./common/build.config.gki

# Build
echo ">>> Building kernel..."
cd "$KERNEL_SRC/common"
export ARCH=arm64
export CROSS_COMPILE=aarch64-linux-gnu-
export CROSS_COMPILE_ARM32=arm-linux-gnueabihf-

make -j$(nproc) \
    CC=clang-20 \
    LD=ld.lld-20 \
    HOSTCC=clang-20 \
    HOSTLD=ld.lld-20 \
    O=out \
    KSU_MANAGER_PACKAGE="com.mikokernel" \
    KCFLAGS+=-O2 \
    KCFLAGS+=-Wno-error \
    gki_defconfig all

echo ">>> BUILD DONE"
ls -lh out/arch/arm64/boot/Image

# Package AnyKernel3
cd "$KERNEL_SRC"
echo ">>> Packaging AnyKernel3..."
git clone https://github.com/cctv18/AnyKernel3 --depth=1
rm -rf ./AnyKernel3/.git
cp ./common/out/arch/arm64/boot/Image ./AnyKernel3/

ZIP_NAME="FollKernel-SM8650-A15-6.1.118-susfs-$(date +%Y%m%d)"
cd ./AnyKernel3
zip -r "../${ZIP_NAME}.zip" ./*
echo ">>> Package: $(realpath "../${ZIP_NAME}.zip")"
ls -lh "../${ZIP_NAME}.zip"

# Copy to Windows
mkdir -p /mnt/d/rekernel/_gki_output
cp "../${ZIP_NAME}.zip" /mnt/d/rekernel/_gki_output/
echo "DONE: /mnt/d/rekernel/_gki_output/${ZIP_NAME}.zip"

#!/bin/bash
set -e
cd /root
KSRC=/root/follkernel_gki

echo "=== 1. Install deps ==="
apt-get update -qq && apt-get install -y -qq clang-20 lld-20 llvm-20 bison flex git zip make gcc python-is-python3 bc libssl-dev libelf-dev cpio ccache aria2 2>&1 | tail -1

echo "=== 2. Clone kernel source (aria2c fast) ==="
rm -rf "$KSRC" /tmp/common.zip
aria2c -s16 -x16 -k1M "https://github.com/cctv18/android_kernel_common_oneplus_sm8650/archive/refs/heads/oneplus/sm8650_b_16.0.0_oneplus12.zip" -o /tmp/common.zip 2>&1 | tail -2
unzip -q /tmp/common.zip -d "$KSRC" && rm /tmp/common.zip
mv "$KSRC"/android_kernel_common_oneplus_sm8650-* "$KSRC/common"

echo "=== 3. Clean ABI + suffix ==="
rm -f "$KSRC/common/android/abi_gki_protected_exports_"*
sed -i 's/ -dirty//g; $i res=$(echo "$res" | sed '"'"'s/-dirty//g'"'"')' "$KSRC/common/scripts/setlocalversion"
sed -i '$s|echo "$res"|echo "-FollKernel"|' "$KSRC/common/scripts/setlocalversion"

echo "=== 4. Integrate FollKernel KSU ==="
cd "$KSRC/common"
rm -rf KernelSU drivers/kernelsu
git clone --depth=1 https://github.com/Spring-bulid/Rekernel.git KernelSU 2>&1 | tail -1
ln -sf "$(realpath KernelSU/kernel)" drivers/kernelsu

echo "=== 5. SuSFS ==="
cd "$KSRC"
git clone --depth=1 https://github.com/cctv18/susfs4oki.git -b oki-android14-6.1 susfs 2>&1 | tail -1
cp susfs/kernel_patches/50_add_susfs_in_gki-android14-6.1.patch common/
cp susfs/kernel_patches/fs/* common/fs/ 2>/dev/null; cp susfs/kernel_patches/include/linux/* common/include/linux/ 2>/dev/null
cd common; patch -p1 -F3 < 50_add_susfs_in_gki-android14-6.1.patch || true; cd ..

echo "=== 6. Config ==="
DEF=common/arch/arm64/configs/gki_defconfig
cat >> "$DEF" <<'X'
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
CONFIG_KSU_SUSFS_SPOOF_UNAME=y
CONFIG_KSU_SUSFS_OPEN_REDIRECT=y
CONFIG_KSU_SUSFS_SUS_MAP=y
CONFIG_TMPFS_XATTR=y
CONFIG_TMPFS_POSIX_ACL=y
CONFIG_CC_OPTIMIZE_FOR_PERFORMANCE=y
CONFIG_HEADERS_INSTALL=n
X
sed -i 's/check_defconfig//' common/build.config.gki

echo "=== 7. Build kernel ==="
cd "$KSRC/common"
export ARCH=arm64 CROSS_COMPILE=aarch64-linux-gnu- CROSS_COMPILE_ARM32=arm-linux-gnueabihf-
make -j$(nproc) CC=clang-20 LD=ld.lld-20 HOSTCC=clang-20 HOSTLD=ld.lld-20 O=out KSU_MANAGER_PACKAGE="com.mikokernel" KCFLAGS+=-O2 KCFLAGS+=-Wno-error gki_defconfig all 2>&1 | tail -5

echo "=== 8. Image: ==="
ls -lh "$KSRC/common/out/arch/arm64/boot/Image"

echo "=== 9. Package AnyKernel3 ==="
cd "$KSRC"
rm -rf AnyKernel3
git clone --depth=1 https://github.com/cctv18/AnyKernel3 2>&1 | tail -1
rm -rf AnyKernel3/.git
cp common/out/arch/arm64/boot/Image AnyKernel3/
ZIP="FollKernel-SM8650-OKI-susfs-$(date +%Y%m%d).zip"
cd AnyKernel3; zip -r "../$ZIP" ./*; cd ..
mkdir -p /mnt/d/rekernel/_gki_output
cp "$ZIP" /mnt/d/rekernel/_gki_output/
echo "DONE: $(ls -lh /mnt/d/rekernel/_gki_output/$ZIP)"

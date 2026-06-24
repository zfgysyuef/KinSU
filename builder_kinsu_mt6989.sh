#!/bin/bash
set -e

# ===== KinSU 天玑9400e (MT6989) 6.1.115 OKI内核编译脚本 =====
# 基于 cctv18/oppo_oplus_realme_sm8650 的 MT6989 构建脚本
# 集成 KinSU + SuSFS + KPM

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "===== KinSU 天玑9400e MT6989 6.1.115 OKI内核编译脚本 ====="
echo ">>> 读取用户配置..."
MANIFEST=${MANIFEST:-oppo+oplus+realme}
read -p "请输入自定义内核后缀（默认：kinsu-mt6989-6.1.115）: " CUSTOM_SUFFIX
CUSTOM_SUFFIX=${CUSTOM_SUFFIX:-kinsu-mt6989-6.1.115}
read -p "是否启用susfs？(y/n，默认：y): " APPLY_SUSFS
APPLY_SUSFS=${APPLY_SUSFS:-y}
read -p "是否启用 KPM？(y-启用 KpatchNext独立kpm实现, n-关闭kpm，默认：n): " USE_PATCH_LINUX
USE_PATCH_LINUX=${USE_PATCH_LINUX:-n}
read -p "是否应用 lz4 1.10.0 & zstd 1.5.7 补丁？(y/n，默认：y): " APPLY_LZ4
APPLY_LZ4=${APPLY_LZ4:-y}
read -p "是否应用 lz4kd 补丁？(y/n，默认：n): " APPLY_LZ4KD
APPLY_LZ4KD=${APPLY_LZ4KD:-n}
read -p "是否启用网络功能增强优化配置？(y/n，默认：n): " APPLY_BETTERNET
APPLY_BETTERNET=${APPLY_BETTERNET:-n}
read -p "是否添加 BBR 等一系列拥塞控制算法？(y添加/n禁用/d默认，默认：n): " APPLY_BBR
APPLY_BBR=${APPLY_BBR:-n}
read -p "是否添加 Droidspaces 容器支持？(n禁用/s标准/e扩展，默认：n): " APPLY_DROIDSPACES
APPLY_DROIDSPACES=${APPLY_DROIDSPACES:-n}
read -p "是否启用三星SSG IO调度器？(y/n，默认：y): " APPLY_SSG
APPLY_SSG=${APPLY_SSG:-y}
read -p "是否启用Re-Kernel？(y/n，默认：n): " APPLY_REKERNEL
APPLY_REKERNEL=${APPLY_REKERNEL:-n}
read -p "是否启用内核级基带保护？(y/n，默认：y): " APPLY_BBG
APPLY_BBG=${APPLY_BBG:-y}

echo
echo "===== 配置信息 ====="
echo "适用机型: $MANIFEST (天玑9400e/MT6989)"
echo "自定义内核后缀: -$CUSTOM_SUFFIX"
echo "KSU分支版本: KinSU (Spring-bulid/KinSU)"
echo "启用susfs: $APPLY_SUSFS"
echo "启用 KPM: $USE_PATCH_LINUX"
echo "应用 lz4&zstd 补丁: $APPLY_LZ4"
echo "应用 lz4kd 补丁: $APPLY_LZ4KD"
echo "应用网络功能增强优化配置: $APPLY_BETTERNET"
echo "应用 BBR 等算法: $APPLY_BBR"
echo "应用 Droidspaces 容器支持: $APPLY_DROIDSPACES"
echo "启用三星SSG IO调度器: $APPLY_SSG"
echo "启用Re-Kernel: $APPLY_REKERNEL"
echo "启用内核级基带保护: $APPLY_BBG"
echo "===================="
echo

# ===== 创建工作目录 =====
WORKDIR="$SCRIPT_DIR"
cd "$WORKDIR"

# ===== 安装构建依赖 =====
echo ">>> 安装构建依赖..."

SU() {
    if [ "$(id -u)" -eq 0 ]; then
        "$@"
    else
        sudo "$@"
    fi
}

SU apt-mark hold firefox && apt-mark hold libc-bin && apt-mark hold man-db
SU rm -rf /var/lib/man-db/auto-update
SU apt-get update
SU apt-get install --no-install-recommends -y curl bison flex clang binutils dwarves git lld pahole zip perl make gcc python3 python-is-python3 bc libssl-dev libelf-dev cpio xz-utils tar unzip
SU rm -rf ./llvm.sh && wget https://apt.llvm.org/llvm.sh && chmod +x llvm.sh
SU ./llvm.sh 20 all

# ===== 初始化仓库 =====
echo ">>> 初始化仓库..."
rm -rf kernel_workspace
mkdir kernel_workspace
cd kernel_workspace
git clone --depth=1 https://github.com/cctv18/android_kernel_oneplus_mt6989 -b oneplus/mt6989_v_15.0.2_ace5_race common
echo ">>> 初始化仓库完成"

# ===== 清除 abi 文件、去除 -dirty 后缀 =====
echo ">>> 正在清除 ABI 文件及去除 dirty 后缀..."
rm common/android/abi_gki_protected_exports_* || true

for f in common/scripts/setlocalversion; do
    sed -i 's/ -dirty//g' "$f"
    sed -i '$i res=$(echo "$res" | sed '\''s/-dirty//g'\'')' "$f"
done

# ===== 替换版本后缀 =====
echo ">>> 替换内核版本后缀..."
for f in ./common/scripts/setlocalversion; do
    sed -i "\$s|echo \"\\\$res\"|echo \"-${CUSTOM_SUFFIX}\"|" "$f"
done

# ===== 拉取 KinSU 并设置版本号 =====
echo ">>> 拉取 KinSU (Spring-bulid/KinSU) 并设置版本..."
cd "$WORKDIR/kernel_workspace"

# Clone KinSU
if [ ! -d "KinSU" ]; then
    git clone https://github.com/Spring-bulid/KinSU
fi
cd KinSU
git pull 2>/dev/null || true
cd "$WORKDIR/kernel_workspace"

# Setup KinSU kernel driver symlink
DRIVER_DIR="$WORKDIR/kernel_workspace/common/drivers"
if [ ! -d "$DRIVER_DIR" ]; then
    DRIVER_DIR="$WORKDIR/kernel_workspace/drivers"
fi

cd "$DRIVER_DIR"
ln -sf "../../KinSU/kernel" "kernelsu"
cd "$WORKDIR/kernel_workspace"

# Add KinSU to drivers Makefile
DRIVER_MAKEFILE=$DRIVER_DIR/Makefile
DRIVER_KCONFIG=$DRIVER_DIR/Kconfig
grep -q "kernelsu" "$DRIVER_MAKEFILE" || printf "obj-\$(CONFIG_KSU) += kernelsu/\n" >> "$DRIVER_MAKEFILE"
grep -q "kernelsu" "$DRIVER_KCONFIG" || sed -i "/endmenu/i\\source \"drivers/kernelsu/Kconfig\"" "$DRIVER_KCONFIG"

echo ">>> KinSU 内核驱动集成完成"

# ===== 克隆补丁仓库&应用 SUSFS 补丁 =====
cd "$WORKDIR/kernel_workspace"
echo ">>> 应用 SUSFS&hook 补丁..."
if [[ "$APPLY_SUSFS" == [yY] ]]; then
    echo ">>> 克隆 SuSFS 补丁仓库..."
    git clone --depth=1 https://github.com/cctv18/susfs4oki.git susfs4ksu -b oki-android14-6.1
    wget https://github.com/cctv18/oppo_oplus_realme_sm8650/raw/refs/heads/main/other_patch/69_hide_stuff.patch -O ./common/69_hide_stuff.patch
    cp ./susfs4ksu/kernel_patches/50_add_susfs_in_gki-android14-6.1.patch ./common/
    cp ./susfs4ksu/kernel_patches/fs/* ./common/fs/
    cp ./susfs4ksu/kernel_patches/include/linux/* ./common/include/linux/
    cd ./common
    patch -p1 < 50_add_susfs_in_gki-android14-6.1.patch || true
    patch -p1 -F 3 < 69_hide_stuff.patch || true
else
    echo ">>> 未开启susfs，跳过susfs补丁配置..."
fi

# ===== 应用 KinSU SuSFS 补丁 =====
cd "$WORKDIR/kernel_workspace"
if [[ "$APPLY_SUSFS" == [yY] ]]; then
    echo ">>> 应用 KinSU SuSFS 内核补丁..."
    # KinSU 内置了 SuSFS 支持，通过 Kbuild 中的 KSU_SUSFS 标志启用
    # 需要在 Kbuild 中添加 susfs 编译选项
    KINSU_KBUILD="$WORKDIR/kernel_workspace/KinSU/kernel/Kbuild"
    if [ -f "$KINSU_KBUILD" ]; then
        # 确保 SuSFS 编译标志已添加
        grep -q "KSU_SUSFS" "$KINSU_KBUILD" || echo "
# SUSFS support for KinSU
ifdef KSU_SUSFS
ccflags-y += -DCONFIG_KSU_SUSFS=1
kinsu-objs += susfs/susfs.o
kinsu-objs += susfs/susfs_coredump.o
kinsu-objs += susfs/sucompat.o
\$(info -- KinSU SUSFS: enabled)
endif" >> "$KINSU_KBUILD"
        echo ">>> KinSU SuSFS 补丁已应用"
    fi
fi

cd "$WORKDIR/kernel_workspace"

# ===== 应用 LZ4 & ZSTD 补丁 =====
if [[ "$APPLY_LZ4" == "y" || "$APPLY_LZ4" == "Y" ]]; then
    echo ">>> 正在添加lz4 1.10.0 & zstd 1.5.7补丁..."
    git clone --depth=1 https://github.com/cctv18/oppo_oplus_realme_sm8650.git
    cp ./oppo_oplus_realme_sm8650/zram_patch/001-lz4.patch ./common/
    cp ./oppo_oplus_realme_sm8650/zram_patch/lz4armv8.S ./common/lib
    cp ./oppo_oplus_realme_sm8650/zram_patch/002-zstd.patch ./common/
    cd "$WORKDIR/kernel_workspace/common"
    git apply -p1 < 001-lz4.patch || true
    patch -p1 < 002-zstd.patch || true
    cd "$WORKDIR/kernel_workspace"
else
    echo ">>> 跳过 LZ4&ZSTD 补丁..."
    cd "$WORKDIR/kernel_workspace"
fi

# ===== 应用 lz4kd 补丁 =====
if [[ "$APPLY_LZ4KD" == [yY] ]]; then
    echo ">>> 正在添加 lz4kd 补丁..."
    if [ ! -d "SukiSU_patch" ]; then
        git clone --depth=1 https://github.com/ShirkNeko/SukiSU_patch.git
    fi
    cp -r ./SukiSU_patch/other/zram/lz4k/include/linux/* ./common/include/linux/
    cp -r ./SukiSU_patch/other/zram/lz4k/lib/* ./common/lib
    cp -r ./SukiSU_patch/other/zram/lz4k/crypto/* ./common/crypto
    cp ./SukiSU_patch/other/zram/zram_patch/6.1/lz4kd.patch ./common/
    cd "$WORKDIR/kernel_workspace/common"
    patch -p1 < lz4kd.patch || true
    cd "$WORKDIR/kernel_workspace"
fi

# ===== 添加 defconfig 配置项 =====
echo ">>> 添加 defconfig 配置项..."
DEFCONFIG_FILE=./common/arch/arm64/configs/gki_defconfig

# 写入 KinSU 配置
echo "CONFIG_KSU=y" >> "$DEFCONFIG_FILE"

if [[ "$APPLY_SUSFS" == [yY] ]]; then
    echo "CONFIG_KSU_SUSFS=y" >> "$DEFCONFIG_FILE"
    echo "CONFIG_KSU_SUSFS_HAS_MAGIC_MOUNT=y" >> "$DEFCONFIG_FILE"
    echo "CONFIG_KSU_SUSFS_SUS_PATH=y" >> "$DEFCONFIG_FILE"
    echo "CONFIG_KSU_SUSFS_SUS_MOUNT=y" >> "$DEFCONFIG_FILE"
    echo "CONFIG_KSU_SUSFS_AUTO_ADD_SUS_KSU_DEFAULT_MOUNT=y" >> "$DEFCONFIG_FILE"
    echo "CONFIG_KSU_SUSFS_AUTO_ADD_SUS_BIND_MOUNT=y" >> "$DEFCONFIG_FILE"
    echo "CONFIG_KSU_SUSFS_SUS_KSTAT=y" >> "$DEFCONFIG_FILE"
    echo "CONFIG_KSU_SUSFS_TRY_UMOUNT=y" >> "$DEFCONFIG_FILE"
    echo "CONFIG_KSU_SUSFS_AUTO_ADD_TRY_UMOUNT_FOR_BIND_MOUNT=y" >> "$DEFCONFIG_FILE"
    echo "CONFIG_KSU_SUSFS_SPOOF_UNAME=y" >> "$DEFCONFIG_FILE"
    echo "CONFIG_KSU_SUSFS_ENABLE_LOG=y" >> "$DEFCONFIG_FILE"
    echo "CONFIG_KSU_SUSFS_HIDE_KSU_SUSFS_SYMBOLS=y" >> "$DEFCONFIG_FILE"
    echo "CONFIG_KSU_SUSFS_SPOOF_CMDLINE_OR_BOOTCONFIG=y" >> "$DEFCONFIG_FILE"
    echo "CONFIG_KSU_SUSFS_OPEN_REDIRECT=y" >> "$DEFCONFIG_FILE"
    echo "CONFIG_KSU_SUSFS_SUS_MAP=y" >> "$DEFCONFIG_FILE"
else
    echo "CONFIG_KSU_SUSFS=n" >> "$DEFCONFIG_FILE"
fi

# KinSU 特有配置
echo "# KinSU Manager package" >> "$DEFCONFIG_FILE"
echo "CONFIG_KSU_MANAGER_PACKAGE=com.mikokernel" >> "$DEFCONFIG_FILE"

echo ">>> defconfig 配置完成"

# ===== 应用其他补丁 =====
cd "$WORKDIR/kernel_workspace"

# 网络功能增强
if [[ "$APPLY_BETTERNET" == [yY] ]]; then
    echo ">>> 应用网络功能增强配置..."
    cat >> "$DEFCONFIG_FILE" << 'EOF'
CONFIG_NETFILTER=y
CONFIG_NETFILTER_ADVANCED=y
CONFIG_NF_CONNTRACK=y
CONFIG_NF_NAT=y
CONFIG_NF_TABLES=y
CONFIG_NF_TABLES_IPV4=y
CONFIG_NF_TABLES_IPV6=y
CONFIG_NFT_COMPAT=y
CONFIG_NETFILTER_XTABLES=y
CONFIG_NETFILTER_XT_MATCH_ADDRTYPE=y
CONFIG_NETFILTER_XT_MATCH_CONNTRACK=y
CONFIG_NETFILTER_XT_MATCH_IPRANGE=y
CONFIG_NETFILTER_XT_MATCH_LIMIT=y
CONFIG_NETFILTER_XT_MATCH_MAC=y
CONFIG_NETFILTER_XT_MATCH_MULTIPORT=y
CONFIG_NETFILTER_XT_MATCH_RECENT=y
CONFIG_NETFILTER_XT_MATCH_STATE=y
CONFIG_NETFILTER_XT_MATCH_STATISTIC=y
CONFIG_NETFILTER_XT_TARGET_CHECKSUM=y
CONFIG_IP_NF_IPTABLES=y
CONFIG_IP6_NF_IPTABLES=y
CONFIG_IP_NF_FILTER=y
CONFIG_IP6_NF_FILTER=y
CONFIG_IP_NF_NAT=y
CONFIG_IP6_NF_NAT=y
CONFIG_IP_SET=y
CONFIG_IP_SET_BITMAP_IP=y
CONFIG_IP_SET_BITMAP_PORT=y
CONFIG_IP_SET_HASH_IP=y
CONFIG_IP_SET_HASH_NET=y
EOF
fi

# ===== 禁用 defconfig 检查 =====
echo ">>> 禁用 defconfig 检查..."
sed -i 's/check_defconfig//' ./common/build.config.gki

# ===== 编译内核 =====
echo ">>> 开始编译内核..."
cd "$WORKDIR/kernel_workspace/common"
make -j$(nproc --all) LLVM=-20 ARCH=arm64 CROSS_COMPILE=aarch64-linux-gnu- CROSS_COMPILE_ARM32=arm-linux-gnuabeihf- CC=clang LD=ld.lld HOSTCC=clang HOSTLD=ld.lld O=out KCFLAGS+=-O2 KCFLAGS+=-Wno-error KSU_SUSFS=$(if [[ "$APPLY_SUSFS" == [yY] ]]; then echo 1; else echo 0; fi) gki_defconfig all

# ===== 打包 =====
echo ">>> 打包内核镜像..."
cd "$WORKDIR/kernel_workspace"

# 创建 AnyKernel3 包
if [ ! -d "AnyKernel3" ]; then
    git clone --depth=1 https://github.com/osm0sis/AnyKernel3
fi

cp ./common/out/arch/arm64/boot/Image.gz ./AnyKernel3/
cp ./common/out/arch/arm64/boot/dtbs/*.dtb ./AnyKernel3/dtb/ 2>/dev/null || true
cp ./common/out/arch/arm64/boot/dtbo.img ./AnyKernel3/ 2>/dev/null || true

cd AnyKernel3

# 设置包名
ZIP_NAME="KinSU-MT6989-6.1.115"
if [[ "$APPLY_SUSFS" == [yY] ]]; then
    ZIP_NAME="${ZIP_NAME}-susfs"
fi
ZIP_NAME="${ZIP_NAME}-$(date +%Y%m%d)"

# 修改 AnyKernel3 配置
sed -i "s/kernel.name=.*/kernel.name=KinSU MT6989/" anykernel.sh
sed -i "s/kernel.string=.*/kernel.string=KinSU for MT6989 (Dimensity 9400e)/" anykernel.sh
sed -i "s/kernel.author=.*/kernel.author=Spring-bulid/" anykernel.sh

zip -r9 "$ZIP_NAME.zip" . -x .git README.md .gitignore anykernel.sh patching.sh

echo
echo "===== 编译完成 ====="
echo "内核包: $WORKDIR/kernel_workspace/AnyKernel3/$ZIP_NAME.zip"
echo "KSU分支: KinSU"
echo "SuSFS: $APPLY_SUSFS"
echo "===================="

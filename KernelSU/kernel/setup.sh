#!/bin/sh
set -eux

# KinSU kernel integration script
# Usage: curl -LSs "https://raw.githubusercontent.com/Spring-bulid/KinSU/main/kernel/setup.sh" | bash -s [branch]
# Or:   ./setup.sh [branch]

GKI_ROOT=$(pwd)

echo "[+] KinSU setup script"
echo "[+] GKI_ROOT: $GKI_ROOT"

if test -d "$GKI_ROOT/common/drivers"; then
     DRIVER_DIR="$GKI_ROOT/common/drivers"
elif test -d "$GKI_ROOT/drivers"; then
     DRIVER_DIR="$GKI_ROOT/drivers"
else
     echo '[ERROR] "drivers/" directory is not found.'
     echo '[+] You should modify this script by yourself.'
     exit 127
fi

# Clone KinSU if not exists
test -d "$GKI_ROOT/KinSU" || git clone https://github.com/Spring-bulid/KinSU
cd "$GKI_ROOT/KinSU"
git stash 2>/dev/null || true
git checkout main 2>/dev/null || true
git pull 2>/dev/null || true

# Checkout specific branch if provided
if [ -z "${1-}" ]; then
    git checkout main 2>/dev/null || true
else
    git checkout "$1" 2>/dev/null || true
fi
cd "$GKI_ROOT"

echo "[+] GKI_ROOT: $GKI_ROOT"
echo "[+] Copy KinSU driver to $DRIVER_DIR"

# Create symlink to kernel directory
cd "$DRIVER_DIR"
if test -d "$GKI_ROOT/common/drivers"; then
     ln -sf "../../KinSU/kernel" "kernelsu"
elif test -d "$GKI_ROOT/drivers"; then
     ln -sf "../KinSU/kernel" "kernelsu"
fi
cd "$GKI_ROOT"

echo '[+] Add KinSU driver to Makefile'

DRIVER_MAKEFILE=$DRIVER_DIR/Makefile
DRIVER_KCONFIG=$DRIVER_DIR/Kconfig
grep -q "kernelsu" "$DRIVER_MAKEFILE" || printf "obj-\$(CONFIG_KSU) += kernelsu/\n" >> "$DRIVER_MAKEFILE"
grep -q "kernelsu" "$DRIVER_KCONFIG" || sed -i "/endmenu/i\\source \"drivers/kernelsu/Kconfig\"" "$DRIVER_KCONFIG"

echo '[+] KinSU setup done.'

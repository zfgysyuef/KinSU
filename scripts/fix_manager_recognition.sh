#!/bin/bash
# Fix KernelSU Manager Recognition Bug
# This script extracts APK signature and updates kernel configuration

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
KERNEL_DIR="$PROJECT_ROOT/KernelSU/kernel"
MANAGER_DIR="$PROJECT_ROOT/KernelSU/manager"

echo "=========================================="
echo "KinSU Manager Recognition Fix"
echo "=========================================="
echo ""

# Step 1: Find or download APK
APK_PATH="$PROJECT_ROOT/manager.apk"
if [ ! -f "$APK_PATH" ]; then
    echo "Downloading latest KinSU manager APK..."
    curl -L "https://github.com/Spring-bulid/KinSU/releases/latest/download/KinSU_v30022_30022-release.apk" -o "$APK_PATH"
fi
echo "APK: $APK_PATH"

# Step 2: Extract v2 signing certificate
echo ""
echo "Extracting APK v2 signing certificate..."
python "$SCRIPT_DIR/extract_sig.py" 2>&1
if [ $? -ne 0 ]; then
    echo "ERROR: Signature extraction failed"
    exit 1
fi

# Step 3: Verify generated files
echo ""
echo "Verifying..."
SIG_H="$KERNEL_DIR/manager/manager_signature.h"
if [ -f "$SIG_H" ]; then
    echo "--- manager_signature.h ---"
    cat "$SIG_H"
    echo ""
else
    echo "ERROR: manager_signature.h not generated"
    exit 1
fi

# Step 4: Ensure apk_sign.c includes the signature header
APK_SIGN_C="$KERNEL_DIR/manager/apk_sign.c"
if ! grep -q '#include "manager/manager_signature.h"' "$APK_SIGN_C"; then
    echo "Adding #include to apk_sign.c..."
    sed -i '/#include "manager\/apk_sign.h"/a #include "manager/manager_signature.h"' "$APK_SIGN_C"
fi

echo ""
echo "=========================================="
echo "FIX COMPLETE"
echo "=========================================="
echo ""
echo "Root cause: Kbuild default EXPECTED_SIZE/HASH were from original"
echo "KernelSU, not KinSU's signing key."
echo ""
echo "Fixed:"
echo "  1. Kbuild defaults updated to KinSU's actual signature"
echo "  2. manager_signature.h created for safety fallback"
echo "  3. apk_sign.c includes signature header"
echo ""
echo "Next steps:"
echo "  1. Rebuild kernel module:"
echo "     cd $KERNEL_DIR && make -C \$KDIR M=\$(pwd) modules"
echo "  2. Patch boot image with updated follkernel.ko"
echo "  3. Flash patched image to device"
echo "  4. Install manager APK"
echo ""
echo "Verification: Natives.isManager should return true"

#!/usr/bin/env python3
"""Replace .ko files in APK assets/lkm/ AND replace libfollkerneld.so, then re-sign."""
import os, sys, shutil, zipfile
from zipfile import ZIP_DEFLATED, ZipFile, ZipInfo

APK_SRC = "/mnt/d/KinSU/KernelSU/manager/app/build/outputs/apk/release/KinSU_v30022_30022-release.apk"
KO_DIR = "/mnt/d/KinSU/KernelSU/userspace/ksud/bin/aarch64"
KSUD_PATH = "/mnt/d/KinSU/KernelSU/target/aarch64-linux-android/release/ksud"
OUT_DIR = "/mnt/d/KinSU/KernelSU/dist"
KEYSTORE = "/mnt/d/KinSU/KernelSU/manager/follkernel_build.jks"
KEY_ALIAS = "follkernel"
KEY_PASS = "follkernel123"
STORE_PASS = "follkernel123"
APKSIGNER = "/home/hanha/Android/Sdk/build-tools/37.0.0/apksigner"
ZIPALIGN = "/home/hanha/Android/Sdk/build-tools/37.0.0/zipalign"
LLVM_STRIP = "/usr/bin/llvm-strip"

KMI_LIST = [
    "android12-5.10", "android13-5.10", "android13-5.15",
    "android14-5.15", "android14-6.1", "android15-6.6", "android16-6.12",
]

# Strip ksud
print("Stripping ksud...")
import tempfile
with tempfile.TemporaryDirectory() as tmp:
    stripped_ksud = os.path.join(tmp, "ksud.stripped")
    shutil.copy2(KSUD_PATH, stripped_ksud)
    os.system(f'{LLVM_STRIP} --strip-all "{stripped_ksud}"')
    with open(stripped_ksud, 'rb') as f:
        ksud_data = f.read()
    print(f"  ksud stripped: {len(ksud_data)} bytes")

# Build replacement map for .ko files
ko_replacements = {}
for kmi in KMI_LIST:
    asset_name = f"assets/lkm/{kmi}_rekernel.ko"
    ko_path = os.path.join(KO_DIR, f"{kmi}_follkernel.ko")
    if os.path.exists(ko_path):
        with open(ko_path, 'rb') as f:
            ko_replacements[asset_name] = f.read()
        print(f"  Loaded: {asset_name}")

# Output paths
out_unsigned = os.path.join(OUT_DIR, "KinSU_v30022_30022-release-unsigned.apk")
out_aligned = os.path.join(OUT_DIR, "KinSU_v30022_30022-release-aligned.apk")
out_signed = os.path.join(OUT_DIR, "KinSU_v30022_30022-release.apk")

for f in [out_unsigned, out_aligned, out_signed]:
    if os.path.exists(f):
        os.remove(f)

print(f"\nProcessing APK: {APK_SRC}")

# Rebuild APK
print("Rebuilding APK...")
replaced_ko = 0
replaced_ksud = False
with ZipFile(APK_SRC, 'r') as zin, ZipFile(out_unsigned, 'w') as zout:
    for info in zin.infolist():
        name = info.filename

        # Replace .ko in assets/lkm/
        if name in ko_replacements:
            new_info = ZipInfo(filename=name, date_time=info.date_time)
            new_info.compress_type = ZIP_DEFLATED
            new_info.external_attr = info.external_attr
            zout.writestr(new_info, ko_replacements[name])
            replaced_ko += 1
            continue

        # Replace libfollkerneld.so with ksud
        if name.startswith("lib/") and name.endswith("/libfollkerneld.so"):
            new_info = ZipInfo(filename=name, date_time=(1980, 1, 1, 0, 0, 0))
            new_info.compress_type = ZIP_DEFLATED
            new_info.external_attr = info.external_attr
            zout.writestr(new_info, ksud_data)
            replaced_ksud = True
            continue

        # Copy everything else
        data = zin.read(name)
        new_info = ZipInfo(filename=name, date_time=info.date_time)
        new_info.compress_type = info.compress_type
        new_info.external_attr = info.external_attr
        new_info.comment = info.comment
        new_info.create_system = info.create_system
        new_info.extra = info.extra
        if info.compress_type == ZIP_DEFLATED:
            zout.writestr(new_info, data, compress_type=ZIP_DEFLATED)
        else:
            zout.writestr(new_info, data)

print(f"  Replaced {replaced_ko} .ko files in assets/lkm/")
print(f"  Replaced libfollkerneld.so: {replaced_ksud}")

# Zipalign
print("Running zipalign...")
ret = os.system(f'{ZIPALIGN} -P 16 -f 4 "{out_unsigned}" "{out_aligned}"')
if not os.path.exists(out_aligned):
    print("ERROR: zipalign failed!")
    sys.exit(1)

# Sign
print("Signing APK...")
ret = os.system(f'{APKSIGNER} sign --v1-signing-enabled false --v2-signing-enabled true --v3-signing-enabled false --v4-signing-enabled false --ks {KEYSTORE} --ks-key-alias {KEY_ALIAS} --ks-pass pass:{STORE_PASS} --key-pass pass:{KEY_PASS} --out "{out_signed}" "{out_aligned}"')
if not os.path.exists(out_signed):
    print("ERROR: apksigner failed!")
    sys.exit(1)

# Cleanup
for f in [out_unsigned, out_aligned]:
    if os.path.exists(f):
        os.remove(f)

# Verify
print("\n=== Verification ===")
os.system(f'{APKSIGNER} verify --print-certs "{out_signed}"')

# Check contents
print("\nKey files in APK:")
with ZipFile(out_signed, 'r') as z:
    for name in z.namelist():
        if 'lkm/' in name or 'libfollkerneld' in name:
            info = z.getinfo(name)
            print(f"  {name}: {info.file_size} bytes")

print(f"\nDone! Output: {out_signed}")
print(f"Size: {os.path.getsize(out_signed)} bytes")

#!/usr/bin/env python3
"""Replace .ko files in APK assets/lkm/ and re-sign."""
import os, sys, tempfile, zipfile
from zipfile import ZIP_DEFLATED, ZipFile, ZipInfo

APK_SRC = "/mnt/d/KinSU/KernelSU/manager/app/build/outputs/apk/release/KinSU_v30022_30022-release.apk"
KO_DIR = "/mnt/d/KinSU/KernelSU/userspace/ksud/bin/aarch64"
OUT_DIR = "/mnt/d/KinSU/KernelSU/dist"
KEYSTORE = "/mnt/d/KinSU/KernelSU/manager/follkernel_build.jks"
KEY_ALIAS = "follkernel"
KEY_PASS = "follkernel123"
STORE_PASS = "follkernel123"
APKSIGNER = "/home/hanha/Android/Sdk/build-tools/37.0.0/apksigner"
ZIPALIGN = "/home/hanha/Android/Sdk/build-tools/37.0.0/zipalign"

KMI_LIST = [
    "android12-5.10", "android13-5.10", "android13-5.15",
    "android14-5.15", "android14-6.1", "android15-6.6", "android16-6.12",
]

# Build replacement map: assets/lkm/<kmi>_rekernel.ko -> <kmi>_follkernel.ko
replacements = {}
for kmi in KMI_LIST:
    asset_name = f"assets/lkm/{kmi}_rekernel.ko"
    ko_path = os.path.join(KO_DIR, f"{kmi}_follkernel.ko")
    if os.path.exists(ko_path):
        replacements[asset_name] = ko_path
        print(f"  Will replace: {asset_name}")

# Output paths
out_unsigned = os.path.join(OUT_DIR, "KinSU_v30022_30022-release-repack-unsigned.apk")
out_aligned = os.path.join(OUT_DIR, "KinSU_v30022_30022-release-repack-aligned.apk")
out_signed = os.path.join(OUT_DIR, "KinSU_v30022_30022-release.apk")

# Clean stale outputs
for f in [out_unsigned, out_aligned, out_signed]:
    if os.path.exists(f):
        os.remove(f)

print(f"\nProcessing APK: {APK_SRC}")

# Read replacement data
ko_data = {}
for asset_name, ko_path in replacements.items():
    with open(ko_path, 'rb') as f:
        ko_data[asset_name] = f.read()
    print(f"  Loaded: {asset_name} ({len(ko_data[asset_name])} bytes)")

# Rebuild APK with replaced .ko files
print("\nRebuilding APK with new .ko files...")
with ZipFile(APK_SRC, 'r') as zin, ZipFile(out_unsigned, 'w') as zout:
    for info in zin.infolist():
        name = info.filename
        if name in ko_data:
            # Replace with new .ko
            new_info = ZipInfo(filename=name, date_time=info.date_time)
            new_info.compress_type = ZIP_DEFLATED
            new_info.external_attr = info.external_attr
            zout.writestr(new_info, ko_data[name])
            print(f"  Replaced: {name}")
        else:
            # Copy original
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

print(f"\nUnsigned APK: {out_unsigned}")

# Zipalign
print("Running zipalign...")
os.system(f'{ZIPALIGN} -P 16 -f 4 "{out_unsigned}" "{out_aligned}"')
if not os.path.exists(out_aligned):
    print("ERROR: zipalign failed!")
    sys.exit(1)

# Sign
print("Signing APK...")
os.system(f'{APKSIGNER} sign --v1-signing-enabled false --v2-signing-enabled true --v3-signing-enabled false --v4-signing-enabled false --ks {KEYSTORE} --ks-key-alias {KEY_ALIAS} --ks-pass pass:{STORE_PASS} --key-pass pass:{KEY_PASS} --out "{out_signed}" "{out_aligned}"')
if not os.path.exists(out_signed):
    print("ERROR: apksigner failed!")
    sys.exit(1)

# Cleanup intermediates
for f in [out_unsigned, out_aligned]:
    if os.path.exists(f):
        os.remove(f)

# Verify
print("\nVerifying...")
os.system(f'{APKSIGNER} verify --print-certs "{out_signed}"')

# Check .ko files in final APK
print("\nFinal APK .ko files:")
with ZipFile(out_signed, 'r') as z:
    for name in z.namelist():
        if 'lkm/' in name and name.endswith('.ko'):
            info = z.getinfo(name)
            print(f"  {name}: {info.file_size} bytes")

print(f"\nDone! Output: {out_signed}")
print(f"Size: {os.path.getsize(out_signed)} bytes")

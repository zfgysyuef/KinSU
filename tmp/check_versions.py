#!/usr/bin/env python3
"""Check if old libkinsud.so has v3.1.5 filename fix."""
import zipfile

apk_path = r'D:\FollKernel\tmp\KinSU_3.1.5_30033-release.apk'
with zipfile.ZipFile(apk_path) as z:
    old = z.read('lib/arm64-v8a/libkinsud.so')
    new = z.read('lib/arm64-v8a/libKinSUd.so')

# v3.1.5 specific strings
v315_strings = [
    b'kernelsu_patched_init_boot',
    b'kernelsu_patched_boot_',
    b'init_boot.img',
    b'boot.img',
    b'boot-patch',
    b'--libadbroot',
    # v3.1.4 fix strings
    b'Restoring stock init',
    b'Detected legacy KernelSU',
    b'Adding KinSU LKM',
    # boot partition selection
    b'choose_boot_partition',
    b'init_boot',
    b'vendor_boot',
    # KMI detection
    b'get_current_kmi',
    b'parse_kmi',
]

print(f"{'String':<40} {'OLD':>5} {'NEW':>5}")
print("-" * 55)
for s in v315_strings:
    old_count = old.count(s)
    new_count = new.count(s)
    marker = " ***" if old_count != new_count else ""
    print(f"{s.decode():<40} {old_count:>5} {new_count:>5}{marker}")

# Check file sizes
print(f"\nOLD size: {len(old):,} bytes")
print(f"NEW size: {len(new):,} bytes")
print(f"Diff: {len(new) - len(old):,} bytes")

# Check build dates/timestamps by looking for date strings
import re
print("\nDate strings in OLD:")
dates_old = set(re.findall(rb'2026-\d{2}-\d{2}', old))
for d in sorted(dates_old):
    print(f"  {d.decode()}")

print("\nDate strings in NEW:")
dates_new = set(re.findall(rb'2026-\d{2}-\d{2}', new))
for d in sorted(dates_new):
    print(f"  {d.decode()}")

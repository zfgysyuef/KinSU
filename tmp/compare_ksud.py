#!/usr/bin/env python3
"""Compare libkinsud.so (old, jniLibs) vs libKinSUd.so (new, repacked) in APK."""
import zipfile, sys

apk_path = r'D:\FollKernel\tmp\KinSU_3.1.5_30033-release.apk'

with zipfile.ZipFile(apk_path) as z:
    old = z.read('lib/arm64-v8a/libkinsud.so')
    new = z.read('lib/arm64-v8a/libKinSUd.so')

    print(f"OLD libkinsud.so: {len(old):,} bytes")
    print(f"NEW libKinSUd.so: {len(new):,} bytes")

    # Check for expected hash
    expected_hash = b'eb4ae49882e0ea80c13989ea2141368b1f819e2bfe10801b3c15b743bd647dab'
    print(f"\nOLD contains expected hash (ASCII): {old.count(expected_hash)}")
    print(f"NEW contains expected hash (ASCII): {new.count(expected_hash)}")

    # Check binary form
    expected_bin = bytes.fromhex(expected_hash.decode())
    print(f"OLD contains expected hash (binary): {old.count(expected_bin)}")
    print(f"NEW contains expected hash (binary): {new.count(expected_bin)}")

    # Check for kinsu.ko filenames
    for suffix in [b'_kinsu.ko', b'_kernelsu.ko', b'_follkernel.ko']:
        old_count = old.count(suffix)
        new_count = new.count(suffix)
        if old_count or new_count:
            print(f"\n{suffix.decode()}:")
            print(f"  OLD: {old_count}")
            print(f"  NEW: {new_count}")

    # Check for package names
    for pkg in [b'com.mikokernel', b'me.weishu.kernelsu', b'com.follkernel']:
        old_count = old.count(pkg)
        new_count = new.count(pkg)
        if old_count or new_count:
            print(f"\n{pkg.decode()}:")
            print(f"  OLD: {old_count}")
            print(f"  NEW: {new_count}")

    # Check for version strings
    for ver in [b'v3.1.3', b'v3.1.4', b'v3.1.5', b'30033', b'30032', b'30031']:
        old_count = old.count(ver)
        new_count = new.count(ver)
        if old_count or new_count:
            print(f"\n{ver.decode()}:")
            print(f"  OLD: {old_count}")
            print(f"  NEW: {new_count}")

    # Check for boot_patch related strings
    for s in [b'Adding KinSU LKM', b'Detected legacy KernelSU', b'Restoring stock init',
              b'kinsu_patched', b'kernelsu_patched', b'boot.img', b'init_boot']:
        old_count = old.count(s)
        new_count = new.count(s)
        if old_count or new_count:
            print(f"\n'{s.decode()}':")
            print(f"  OLD: {old_count}")
            print(f"  NEW: {new_count}")

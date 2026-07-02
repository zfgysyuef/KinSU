#!/usr/bin/env python3
"""Check embedded kinsu.ko files and KMI versions in the APK's libkinsud.so."""
import zipfile, sys, struct

apk_path = sys.argv[1] if len(sys.argv) > 1 else r'D:\FollKernel\tmp\KinSU_3.1.5_30033-release.apk'

with zipfile.ZipFile(apk_path) as z:
    print(f"APK: {apk_path}")
    print(f"\nLib files in APK:")
    for n in sorted(z.namelist()):
        if n.startswith('lib/'):
            info = z.getinfo(n)
            print(f"  {n}: {info.file_size:,} bytes")

    # Read libkinsud.so
    lib_name = 'lib/arm64-v8a/libkinsud.so'
    if lib_name not in z.namelist():
        # Try alternative names
        for alt in ['lib/arm64-v8a/libkinsud.so', 'lib/arm64-v8a/libfollkerneld.so', 'lib/arm64-v8a/libkernelsud.so']:
            if alt in z.namelist():
                lib_name = alt
                break
        else:
            print(f"\nERROR: No libkinsud.so found!")
            sys.exit(1)

    print(f"\nAnalyzing: {lib_name}")
    lib_data = z.read(lib_name)
    print(f"Size: {len(lib_data):,} bytes")

    # Search for KMI strings (e.g., "android14-6.1_kinsu.ko")
    print(f"\nSearching for KMI-related strings:")
    kmi_files = set()
    for suffix in [b'_kinsu.ko', b'_kernelsu.ko', b'_follkernel.ko']:
        idx = 0
        while True:
            idx = lib_data.find(suffix, idx)
            if idx < 0:
                break
            # Extract the full filename by going backwards
            start = idx
            while start > 0 and lib_data[start-1:start] in [b'%c' % c for c in range(32, 127)]:
                start -= 1
            name = lib_data[start:idx + len(suffix)].decode('ascii', errors='replace')
            if name and len(name) < 100:
                kmi_files.add(name)
            idx += len(suffix)

    for name in sorted(kmi_files):
        print(f"  {name}")

    # Check for vermagic strings (kernel version info in .ko files)
    print(f"\nSearching for vermagic strings in embedded .ko files:")
    vermagic_pattern = b'vermagic='
    idx = 0
    count = 0
    while count < 20:
        idx = lib_data.find(vermagic_pattern, idx)
        if idx < 0:
            break
        # Extract vermagic string (null-terminated)
        end = lib_data.find(b'\x00', idx)
        if end > idx:
            vermagic = lib_data[idx:end].decode('ascii', errors='replace')
            print(f"  {vermagic}")
            count += 1
        idx = end + 1

    # Also search for "android" version strings
    print(f"\nSearching for android KMI version strings:")
    for ver in [b'android12-5.10', b'android13-5.10', b'android13-5.15',
                b'android14-5.15', b'android14-6.1', b'android15-6.6', b'android16-6.12']:
        count = lib_data.count(ver)
        if count > 0:
            print(f"  {ver.decode()}: {count} occurrence(s)")

    # Check for the expected hash string
    expected_hash = b'eb4ae49882e0ea80c13989ea2141368b1f819e2bfe10801b3c15b743bd647dab'
    print(f"\nExpected hash in libkinsud.so: {lib_data.count(expected_hash)} occurrence(s)")

    # Check for package name
    for pkg in [b'com.mikokernel', b'me.weishu.kernelsu', b'com.follkernel']:
        count = lib_data.count(pkg)
        if count > 0:
            print(f"Package '{pkg.decode()}': {count} occurrence(s)")

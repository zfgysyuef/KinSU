#!/usr/bin/env python3
"""Try to decompress gzip data in ksud to find kinsu.ko with expected hash."""
import zlib, struct

apk_path = r'D:\FollKernel\tmp\KinSU_3.1.5_30033-release.apk'
expected_hash = b'eb4ae49882e0ea80c13989ea2141368b1f819e2bfe10801b3c15b743bd647dab'

with open(apk_path, 'rb') as f:
    import zipfile
    z = zipfile.ZipFile(apk_path)
    old = z.read('lib/arm64-v8a/libkinsud.so')
    new = z.read('lib/arm64-v8a/libKinSUd.so')

def find_gzip_and_decompress(data, label, max_attempts=50):
    """Find gzip streams and try to decompress them."""
    print(f"\n=== {label} ({len(data):,} bytes) ===")
    gzip_magic = b'\x1f\x8b\x08'
    idx = 0
    found_with_hash = 0
    found_total = 0
    while idx < len(data) - 10:
        idx = data.find(gzip_magic, idx)
        if idx < 0:
            break
        # Try to decompress as gzip
        try:
            decompressed = zlib.decompressobj(zlib.MAX_WBITS | 16).decompress(data[idx:idx+500000])
            if len(decompressed) > 1000:  # Only care about large chunks
                found_total += 1
                has_hash = decompressed.count(expected_hash)
                has_elf = decompressed.startswith(b'\x7fELF')
                if has_hash or has_elf:
                    print(f"  Gzip at 0x{idx:x}: decompressed={len(decompressed):,} bytes, "
                          f"hash={has_hash}, ELF={has_elf}")
                    if has_hash:
                        found_with_hash += 1
                        # Find which KMI this is
                        for kmi in [b'android12-5.10', b'android13-5.10', b'android13-5.15',
                                    b'android14-5.15', b'android14-6.1', b'android15-6.6',
                                    b'android16-6.12']:
                            if kmi in decompressed:
                                print(f"    KMI: {kmi.decode()}")
        except Exception:
            pass
        idx += 3
    print(f"  Total gzip streams: {found_total}, with hash: {found_with_hash}")
    return found_with_hash

old_count = find_gzip_and_decompress(old, "OLD libkinsud.so")
new_count = find_gzip_and_decompress(new, "NEW libKinSUd.so")

print(f"\n{'='*60}")
print(f"SUMMARY:")
print(f"  OLD: {old_count} kinsu.ko files with expected hash")
print(f"  NEW: {new_count} kinsu.ko files with expected hash")

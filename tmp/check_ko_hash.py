#!/usr/bin/env python3
"""Check if kinsu.ko files in repo and embedded in ksud contain the expected hash."""
import os, sys

expected_hash = b'eb4ae49882e0ea80c13989ea2141368b1f819e2bfe10801b3c15b743bd647dab'

# 1. Check repo kinsu.ko files
print("=" * 60)
print("1. Checking repo bin/aarch64/*.ko files")
print("=" * 60)
repo_dir = r'd:\FollKernel\KernelSU\userspace\ksud\bin\aarch64'
if os.path.isdir(repo_dir):
    for f in sorted(os.listdir(repo_dir)):
        if f.endswith('.ko'):
            path = os.path.join(repo_dir, f)
            with open(path, 'rb') as fp:
                data = fp.read()
            count_ascii = data.count(expected_hash)
            count_bin = data.count(bytes.fromhex(expected_hash.decode()))
            # Also check for the hash as individual bytes array
            print(f"  {f}: {len(data):,} bytes, hash(ascii)={count_ascii}, hash(bin)={count_bin}")
else:
    print(f"  Directory not found: {repo_dir}")

# 2. Check kinsu.ko embedded in libkinsud.so (old)
print("\n" + "=" * 60)
print("2. Extracting kinsu.ko from OLD libkinsud.so (jniLibs)")
print("=" * 60)
import zipfile
apk_path = r'D:\FollKernel\tmp\KinSU_3.1.5_30033-release.apk'
with zipfile.ZipFile(apk_path) as z:
    old_so = z.read('lib/arm64-v8a/libkinsud.so')
    new_so = z.read('lib/arm64-v8a/libKinSUd.so')

# ELF magic for .ko files
elf_magic = b'\x7fELF'

def find_elf_files(data, label):
    """Find all ELF files embedded in data."""
    print(f"\n  Searching ELF files in {label} ({len(data):,} bytes):")
    idx = 0
    found = []
    while True:
        idx = data.find(elf_magic, idx)
        if idx < 0:
            break
        # ELF header: check if it's 64-bit (byte 4 = 2) and ET_REL (type 1 at offset 16)
        if idx + 20 < len(data):
            ei_class = data[idx + 4]  # 1=32bit, 2=64bit
            e_type = data[idx + 16] | (data[idx + 17] << 8)  # 1=REL, 2=EXEC, 3=DYN
            if ei_class == 2 and e_type == 1:  # 64-bit relocatable (.ko)
                # Try to find the end by looking for the next ELF or section header
                # Just extract a reasonable chunk and check for hash
                chunk_size = min(500000, len(data) - idx)  # 500KB max
                chunk = data[idx:idx + chunk_size]
                hash_count = chunk.count(expected_hash)
                if hash_count > 0:
                    print(f"    ELF at 0x{idx:x}: contains expected hash ({hash_count}x)")
                    found.append(idx)
                else:
                    # Check larger chunk
                    chunk2 = data[idx:idx + 2000000]
                    hash_count2 = chunk2.count(expected_hash)
                    if hash_count2 > 0:
                        print(f"    ELF at 0x{idx:x}: contains expected hash in 2MB chunk ({hash_count2}x)")
                        found.append(idx)
        idx += 4
    print(f"  Found {len(found)} ELF .ko files with expected hash")
    return found

find_elf_files(old_so, "OLD libkinsud.so")
find_elf_files(new_so, "NEW libKinSUd.so")

# 3. Direct comparison - check if both .so have the same kinsu.ko content
print("\n" + "=" * 60)
print("3. Comparing kinsu.ko content between OLD and NEW")
print("=" * 60)

# Search for a known kinsu.ko filename to find its embedded data
# RustEmbed stores files with their names
for kmi_name in [b'android14-6.1_kinsu.ko', b'android13-5.15_kinsu.ko']:
    print(f"\n  Looking for {kmi_name.decode()}:")
    old_idx = old_so.find(kmi_name)
    new_idx = new_so.find(kmi_name)
    print(f"    OLD: filename at 0x{old_idx:x}" if old_idx >= 0 else "    OLD: not found")
    print(f"    NEW: filename at 0x{new_idx:x}" if new_idx >= 0 else "    NEW: not found")

# 4. Check if the .so files contain the expected hash anywhere
print("\n" + "=" * 60)
print("4. Checking expected hash in entire .so files")
print("=" * 60)
print(f"  OLD libkinsud.so: hash(ascii)={old_so.count(expected_hash)}, hash(bin)={old_so.count(bytes.fromhex(expected_hash.decode()))}")
print(f"  NEW libKinSUd.so: hash(ascii)={new_so.count(expected_hash)}, hash(bin)={new_so.count(bytes.fromhex(expected_hash.decode()))}")

# 5. Check what hash IS in the kinsu.ko files
# Look for 64-char hex strings (SHA256 hashes)
import re
print("\n" + "=" * 60)
print("5. Looking for any SHA256 hash strings in .so files")
print("=" * 60)
for label, data in [("OLD", old_so), ("NEW", new_so)]:
    # Find all 64-char hex strings
    pattern = re.compile(rb'[0-9a-f]{64}')
    matches = set(pattern.findall(data))
    print(f"\n  {label} unique 64-char hex strings: {len(matches)}")
    for m in sorted(matches)[:10]:
        print(f"    {m.decode()}")

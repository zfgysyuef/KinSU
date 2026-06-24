#!/usr/bin/env python3
"""Find and patch EXPECTED_SIZE/EXPECTED_HASH in .ko binary"""
import struct, sys, os

ko_path = sys.argv[1] if len(sys.argv) > 1 else r'd:\KinSU\KernelSU\userspace\ksud\bin\aarch64\android14-6.1_kinsu.ko'
with open(ko_path, 'rb') as f:
    data = bytearray(f.read())

old_size = 0x2E8  # int
new_size = 0x2D4
old_hash_b = b'48c973bf9702ba7013e5986e45996454e0bc8389397737b75dfe12903deb1ad9'
new_hash_b = b'eb4ae49882e018241dc118c1bad8697da6f744c6583b55fa4371784634e9cccf'

print(f"File: {ko_path} ({len(data)} bytes)")

# Search for old_size as 32-bit LE immediate in various contexts
# In ARM64, MOVK+MOVW or LDR can contain 0x2E8
size_le = struct.pack('<I', old_size)
print(f"\nSearching for old size 0x{old_size:X} = LE bytes {size_le.hex()}")

# Find the size in the data section (near string references)
found = []
for i in range(len(data) - 3):
    if data[i:i+4] == size_le:
        found.append(i)

print(f"  Found {len(found)} occurrences at offsets: {[hex(x) for x in found[:20]]}")

# Search for hash string bytes
print(f"\nSearching for old hash ({len(old_hash_b)} chars)")
hash_positions = []
for i in range(len(data) - len(old_hash_b)):
    if data[i:i+len(old_hash_b)] == old_hash_b:
        hash_positions.append(i)

print(f"  Hash as ASCII: {len(hash_positions)} occurrences at {[hex(x) for x in hash_positions]}")

# Search for hash as hex bytes (each pair converted)
hash_bytes = bytes.fromhex(old_hash_b.decode())
print(f"\nSearching for hash as raw bytes ({len(hash_bytes)} bytes)")
raw_positions = [i for i in range(len(data)-len(hash_bytes)) if data[i:i+len(hash_bytes)] == hash_bytes]
print(f"  Found {len(raw_positions)} occurrences at {[hex(x) for x in raw_positions]}")

# Try to find using grep-like approach: find the hash string split/encoded
# Sometimes compilers split long strings
print(f"\nTrying hash fragment search:")
for fragment_len in [8, 16, 32]:
    fragment = old_hash_b[:fragment_len]
    count = data.count(fragment)
    print(f"  First {fragment_len} chars '{fragment.decode()}': {count} times")

# Also check modinfo vermagic for size hint
print(f"\nSearching for '0x2e8' as text:")
print(f"  {data.count(b'0x2e8')} times")

# Check the .rodata section - search for 'KSU_EXPECTED' or 'EXPECTED'
print(f"\nSearching for signature-related strings:")
for s in [b'EXPECTED_SIZE', b'EXPECTED_HASH', b'KSU_EXPECTED', b'apk_sign']:
    c = data.count(s)
    if c:
        idx = data.find(s)
        print(f"  '{s.decode()}': {c} times, first at {hex(idx)}")
        # Show context
        start = max(0, idx - 20)
        end = min(len(data), idx + len(s) + 50)
        context = data[start:end]
        printable = ''.join(chr(b) if 32<=b<127 else '.' for b in context)
        print(f"    Context: {printable}")

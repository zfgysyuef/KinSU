import sys

ko_path = sys.argv[1]
with open(ko_path, 'rb') as f:
    data = f.read()

old_hash = '48c973bf9702ba7013e5986e45996454e0bc8389397737b75dfe12903deb1ad9'
new_hash = 'eb4ae49882e018241dc118c1bad8697da6f744c6583b55fa4371784634e9cccf'
new_size = 0x2d4

print(f"File size: {len(data)}")

# Search for old size (0x2e8) as LE int32
import struct
old_size_le = struct.pack('<I', 0x2e8)
new_size_le = struct.pack('<I', new_size)
print(f"Old size 0x2e8 LE occurrences: {data.count(old_size_le)}")
print(f"New size 0x2d4 LE occurrences: {data.count(new_size_le)}")

# Find 0x2e8 as immediate in instructions
positions = []
for i in range(len(data) - 4):
    if data[i:i+4] == old_size_le:
        positions.append(i)
print(f"0x2e8 positions: {[hex(p) for p in positions[:10]]}")

# Search hash fragments
print("\nHash fragment search (4-char):")
for i in range(0, 60, 4):
    frag = old_hash[i:i+8]
    c = data.count(frag.encode())
    if c > 0:
        print(f"  [{i}:{i+8}] {frag}: {c}")

# Try BOTH upper and lower case
print("\nAll case variants:")
for h in [old_hash, old_hash.upper()]:
    c = data.count(h.encode())
    if c > 0:
        print(f"  Full hash (case variant): {c}")

# Special: check if there's a 32-byte sequence
old_bytes = bytes.fromhex(old_hash)
new_bytes = bytes.fromhex(new_hash)
print(f"\nRaw bytes old: {data.count(old_bytes)}")
print(f"Raw bytes new: {data.count(new_bytes)}")

# Check strings near "apk_sign" or "manager"
for marker in [b'ksu_debug_manager', b'expected_size', b'expected_sha256', b'check_v2']:
    idx = data.find(marker)
    if idx >= 0:
        ctx = data[max(0,idx-30):idx+len(marker)+80]
        printable = ''.join(chr(b) if 32<=b<127 else '.' for b in ctx)
        print(f"\nNear '{marker.decode(errors='replace')}' at {hex(idx)}:")
        print(f"  {printable}")

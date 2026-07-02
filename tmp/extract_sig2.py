#!/usr/bin/env python3
"""Extract APK v2 certificate by searching for DER certificate pattern."""
import struct, hashlib, sys

apk_path = sys.argv[1] if len(sys.argv) > 1 else r'D:\FollKernel\tmp\KinSU_3.1.5_30033-release.apk'

with open(apk_path, 'rb') as f:
    data = f.read()

print(f"APK: {apk_path}")
print(f"Size: {len(data)} bytes")

# Find EOCD
eocd = data.rfind(b'PK\x05\x06')
cd_offset = struct.unpack_from('<I', data, eocd + 16)[0]
sig_block_size = struct.unpack_from('<Q', data, cd_offset - 24)[0]

# v2 signature block范围
pairs_start = cd_offset - sig_block_size
pairs_end = cd_offset - 24

print(f"Signing block: 0x{pairs_start:x} - 0x{pairs_end:x}")

# 在签名块范围内搜索 DER 证书
# X.509 证书以 SEQUENCE 开头: 0x30 0x82 XX XX (长度用 2 字节表示)
# RSA 2048 证书通常约 700-900 字节
print("\nSearching for DER certificates (0x30 0x82 pattern):")
cert_candidates = []
pos = pairs_start
while pos < pairs_end - 4:
    if data[pos] == 0x30 and data[pos + 1] == 0x82:
        cert_len = struct.unpack_from('>H', data, pos + 2)[0]  # 大端序
        total_len = cert_len + 4
        # 证书长度合理范围: 500-2000 字节
        if 500 <= total_len <= 2000 and pos + total_len <= pairs_end:
            cert_data = data[pos:pos + total_len]
            sha256 = hashlib.sha256(cert_data).hexdigest()
            cert_candidates.append((pos, total_len, sha256, cert_data))
            print(f"  Found at 0x{pos:x}: len={total_len} (0x{total_len:x}), sha256={sha256}")
    pos += 1

print(f"\nTotal candidates: {len(cert_candidates)}")

expected_hash = "eb4ae49882e0ea80c13989ea2141368b1f819e2bfe10801b3c15b743bd647dab"
print(f"Expected hash (in kinsu.ko): {expected_hash}")

for i, (offset, length, sha256, _) in enumerate(cert_candidates):
    match = "MATCH!" if sha256 == expected_hash else "NO MATCH"
    print(f"  Candidate {i}: offset=0x{offset:x}, size=0x{length:x}, sha256={sha256} [{match}]")

# 也检查整个签名块里是否包含 expected_hash 的原始字节
expected_bytes = bytes.fromhex(expected_hash)
print(f"\nSearching for expected hash as raw bytes in signing block:")
count = data[pairs_start:pairs_end].count(expected_bytes)
print(f"  Found {count} occurrence(s)")

# 搜索 expected hash 的 ASCII 字符串形式
expected_ascii = expected_hash.encode()
print(f"\nSearching for expected hash as ASCII string in signing block:")
count = data[pairs_start:pairs_end].count(expected_ascii)
print(f"  Found {count} occurrence(s)")

# 搜索整个 APK 中的 expected hash
print(f"\nSearching for expected hash as raw bytes in entire APK:")
count = data.count(expected_bytes)
print(f"  Found {count} occurrence(s)")

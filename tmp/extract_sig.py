#!/usr/bin/env python3
"""Extract APK v2/v3 signing certificate SHA256 hash and size."""
import struct, hashlib, sys

apk_path = sys.argv[1] if len(sys.argv) > 1 else r'D:\FollKernel\tmp\KinSU_3.1.5_30033-release.apk'

with open(apk_path, 'rb') as f:
    data = f.read()

print(f"APK: {apk_path}")
print(f"Size: {len(data)} bytes")

# Find EOCD (End of Central Directory)
eocd = data.rfind(b'PK\x05\x06')
if eocd < 0:
    print("ERROR: EOCD not found"); sys.exit(1)

cd_offset = struct.unpack_from('<I', data, eocd + 16)[0]
print(f"Central Directory offset: 0x{cd_offset:x}")

# APK Signing Block 的重复 size 字段在 cd_offset - 24
# size 值不包括自身 8 字节，但包括 pairs + 8(repeated size) + 16(magic)
sig_block_size = struct.unpack_from('<Q', data, cd_offset - 24)[0]
print(f"Signing block size (repeated field): {sig_block_size}")

# pairs 从 cd_offset - sig_block_size 开始
# pairs 到 cd_offset - 24 结束
pairs_start = cd_offset - sig_block_size
pairs_end = cd_offset - 24
print(f"Pairs range: 0x{pairs_start:x} - 0x{pairs_end:x}")

# Verify magic at cd_offset - 16
magic = data[cd_offset - 16:cd_offset]
print(f"Magic: {magic}")
if magic != b'APK Sig Block 42':
    print("ERROR: Magic mismatch"); sys.exit(1)

def parse_v2_signature(value_data):
    """Parse v2/v3 signature block to extract certificate."""
    try:
        pos = 0
        # signers sequence length (uint64)
        signers_len = struct.unpack_from('<Q', value_data, pos)[0]
        pos += 8
        print(f"    Signers sequence length: {signers_len}")
        
        # first signer length (uint64)
        signer_len = struct.unpack_from('<Q', value_data, pos)[0]
        pos += 8
        print(f"    First signer length: {signer_len}")
        
        # signed data length (uint64)
        signed_data_len = struct.unpack_from('<Q', value_data, pos)[0]
        pos += 8
        print(f"    Signed data length: {signed_data_len}")
        
        # digests sequence length (uint64)
        digests_len = struct.unpack_from('<Q', value_data, pos)[0]
        pos += 8
        print(f"    Digests length: {digests_len}")
        pos += digests_len  # skip digests
        
        # certificates sequence length (uint64)
        certs_len = struct.unpack_from('<Q', value_data, pos)[0]
        pos += 8
        print(f"    Certificates sequence length: {certs_len}")
        
        # first certificate length (uint32)
        cert_len = struct.unpack_from('<I', value_data, pos)[0]
        pos += 4
        cert_data = value_data[pos:pos + cert_len]
        
        sha256 = hashlib.sha256(cert_data).hexdigest()
        print(f"\n    *** Certificate length: {cert_len} (0x{cert_len:x}) ***")
        print(f"    *** Certificate SHA256: {sha256} ***")
        print(f"\n    Expected (in manager_signature.h): eb4ae49882e0ea80c13989ea2141368b1f819e2bfe10801b3c15b743bd647dab")
        print(f"    Match: {sha256 == 'eb4ae49882e0ea80c13989ea2141368b1f819e2bfe10801b3c15b743bd647dab'}")
        
    except Exception as e:
        print(f"    Parse error: {e}")
        import traceback; traceback.print_exc()

# Parse ID-value pairs
pos = pairs_start
while pos < pairs_end:
    if pos + 12 > pairs_end:
        break
    entry_len = struct.unpack_from('<Q', data, pos)[0]
    if entry_len < 4 or pos + 8 + entry_len > pairs_end:
        print(f"  Invalid entry at 0x{pos:x}: len={entry_len}")
        break
    entry_id = struct.unpack_from('<I', data, pos + 8)[0]
    value_data = data[pos + 12:pos + 8 + entry_len]

    print(f"\n  Entry at 0x{pos:x}: len={entry_len}, id=0x{entry_id:x}")

    if entry_id == 0x7109871a:
        print("  -> APK Signature Scheme v2")
        parse_v2_signature(value_data)
    elif entry_id == 0xf05368c0:
        print("  -> APK Signature Scheme v3")
        parse_v2_signature(value_data)
    else:
        print(f"  -> Other block (id=0x{entry_id:x}), len={len(value_data)}")

    pos += 8 + entry_len

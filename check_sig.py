import struct, hashlib, sys

apk = sys.argv[1] if len(sys.argv) > 1 else 'FollKernel_v30022.apk'
with open(apk, 'rb') as f:
    data = f.read()

pos = data.rfind(b'PK\x05\x06')
cd_offset = struct.unpack_from('<I', data, pos + 16)[0]
block_size = struct.unpack_from('<Q', data, cd_offset - 24)[0]
block_start = cd_offset - block_size - 8

off = block_start + 8
found = False
while off < cd_offset - 24:
    seq_len = struct.unpack_from('<Q', data, off)[0]
    if seq_len + 8 > cd_offset - off: break
    seq_id = struct.unpack_from('<I', data, off + 8)[0]
    if seq_id == 0x7109871a:
        inner = off + 12
        ssl = struct.unpack_from('<I', data, inner)[0]
        sl = struct.unpack_from('<I', data, inner + 4)[0]
        sdl = struct.unpack_from('<I', data, inner + 8)[0]
        dl = struct.unpack_from('<I', data, inner + 12)[0]
        inner2 = inner + 16 + dl
        inner2 += 4  # certs sequence length
        cl = struct.unpack_from('<I', data, inner2)[0]
        cert = data[inner2+4:inner2+4+cl]
        h = hashlib.sha256(cert).hexdigest()
        print(f'APK cert size: {cl}')
        print(f'APK cert hash: {h}')
        print(f'Expected size: 724 (0x2d4)')
        print(f'Expected hash: a23fe337cb870393959384b66050ae8e9d717701cf681ef21f741afa5469919f')
        print(f'MATCH: {cl == 724 and h == "a23fe337cb870393959384b66050ae8e9d717701cf681ef21f741afa5469919f"}')
        found = True
        break
    off += seq_len + 8

if not found:
    print('ERROR: v2 signature not found')

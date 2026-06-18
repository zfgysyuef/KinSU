"""Extract APK v2 signing cert hash for KernelSU manager recognition."""
import struct, hashlib, sys, os

def main(apk_path):
    with open(apk_path, 'rb') as f:
        data = f.read()

    # Find EOCD
    pos = data.rfind(b'PK\x05\x06')
    if pos < 0:
        print("ERROR: cannot find EOCD"); return 1

    cd_offset = struct.unpack_from('<I', data, pos + 16)[0]
    print(f"EOCD at {pos}, CentralDir at {cd_offset}")

    # APK Signing Block
    if cd_offset < 24 or data[cd_offset-16:cd_offset] != b'APK Sig Block 42':
        print("ERROR: no APK Sig Block v2"); return 1

    block_size = struct.unpack_from('<Q', data, cd_offset - 24)[0]
    block_start = cd_offset - block_size - 8
    print(f"APK Sig Block at {block_start}, size {block_size}")

    # Parse pairs
    off = block_start + 8
    while off < cd_offset - 24:
        seq_len = struct.unpack_from('<Q', data, off)[0]
        if seq_len + 8 > block_size: break
        seq_id = struct.unpack_from('<I', data, off + 8)[0]
        inner_off = off + 12  # skip seq_len(8) + id(4)
        value_len = seq_len - 4

        if seq_id == 0x7109871a:  # v2 signing
            # signer-sequence: length(4) + signers...
            signer_seq_len = struct.unpack_from('<I', data, inner_off)[0]
            # signer: length(4) + signed-data-length(4) + digests + certs + attrs
            signer_start = inner_off + 4
            signer_len = struct.unpack_from('<I', data, signer_start)[0]
            signer_data = signer_start + 4
            # signed data length
            signed_data_len = struct.unpack_from('<I', data, signer_data)[0]
            # digests starts after 3 fields: signer-len(4), signed-data-len(4)
            digests_start = signer_data + 4 + 4
            digests_len = struct.unpack_from('<I', data, digests_start)[0]
            # certs start after digests
            certs_start = digests_start + 4 + digests_len
            certs_seq_len = struct.unpack_from('<I', data, certs_start)[0]
            # individual cert
            cert_len = struct.unpack_from('<I', data, certs_start + 4)[0]
            cert_data = data[certs_start + 8:certs_start + 8 + cert_len]

            if 0 < cert_len < 8192:
                h = hashlib.sha256(cert_data).hexdigest()
                print(f"\nPACKAGE: com.mikokernel")
                print(f"EXPECTED_SIZE: {cert_len}")
                print(f"EXPECTED_HASH: {h}")

                script_dir = os.path.dirname(os.path.abspath(__file__))
                out = os.path.join(script_dir, '..', 'KernelSU', 'kernel', 'manager', 'manager_signature.h')
                os.makedirs(os.path.dirname(out), exist_ok=True)
                with open(out, 'w') as fp:
                    fp.write(
                        f'/* Auto-generated manager signature */\n'
                        f'#define KSU_MANAGER_PACKAGE "com.mikokernel"\n'
                        f'#define EXPECTED_SIZE 0x{cert_len:x}\n'
                        f'#define EXPECTED_HASH "{h}"\n'
                    )
                print(f"\nWritten: {out}")
                return 0
            else:
                print(f"WARN: cert size {cert_len} unexpected"); return 1

        off += seq_len + 8

    print("ERROR: v2 signature not found"); return 1

if __name__ == '__main__':
    apk = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'manager.apk')
    print(f"Analyzing: {apk}")
    sys.exit(main(apk))

"""Extract APK v2 signing cert hash for KinSU manager recognition."""
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
            signer_seq_len = struct.unpack_from('<I', data, inner_off)[0]
            signer_start = inner_off + 4
            signer_len = struct.unpack_from('<I', data, signer_start)[0]
            signer_data = signer_start + 4
            signed_data_len = struct.unpack_from('<I', data, signer_data)[0]
            digests_start = signer_data + 4 + 4
            digests_len = struct.unpack_from('<I', data, digests_start)[0]
            certs_start = digests_start + 4 + digests_len
            certs_seq_len = struct.unpack_from('<I', data, certs_start)[0]
            cert_len = struct.unpack_from('<I', data, certs_start + 4)[0]
            cert_data = data[certs_start + 8:certs_start + 8 + cert_len]

            if 0 < cert_len < 8192:
                h = hashlib.sha256(cert_data).hexdigest()
                print(f"\nPACKAGE: com.mikokernel")
                print(f"EXPECTED_SIZE: 0x{cert_len:x}")
                print(f"EXPECTED_HASH: {h}")

                script_dir = os.path.dirname(os.path.abspath(__file__))
                out = os.path.join(script_dir, '..', 'KernelSU', 'kernel', 'manager', 'manager_signature.h')
                os.makedirs(os.path.dirname(out), exist_ok=True)
                with open(out, 'w') as fp:
                    fp.write(
                        '/* Auto-generated manager signature for KinSU\n'
                        ' *\n'
                        ' * These values identify the KinSU manager APK.\n'
                        ' * The kernel module uses these to verify the manager app\n'
                        ' * when searching /data/app for the correct APK.\n'
                        ' *\n'
                        ' * Regenerate with: python scripts/extract_sig.py\n'
                        ' */\n'
                        '\n'
                        '#ifndef __KSU_MANAGER_SIGNATURE_H\n'
                        '#define __KSU_MANAGER_SIGNATURE_H\n'
                        '\n'
                        '/* Manager package name */\n'
                        '#ifndef KSU_MANAGER_PACKAGE\n'
                        '#define KSU_MANAGER_PACKAGE "com.mikokernel"\n'
                        '#endif\n'
                        '\n'
                        f'/* APK v2 signing certificate size in bytes ({cert_len} = 0x{cert_len:x}) */\n'
                        '#ifndef EXPECTED_SIZE\n'
                        f'#define EXPECTED_SIZE 0x{cert_len:x}\n'
                        '#endif\n'
                        '\n'
                        '/* SHA256 hash of the APK v2 signing certificate */\n'
                        '#ifndef EXPECTED_HASH\n'
                        f'#define EXPECTED_HASH "{h}"\n'
                        '#endif\n'
                        '\n'
                        '/*\n'
                        ' * To add a fallback signature (e.g., for debug builds or re-signed APKs),\n'
                        ' * define EXPECTED_SIZE2 and EXPECTED_HASH2 before including this file,\n'
                        ' * or pass them as compiler flags:\n'
                        ' *   KSU_EXPECTED_SIZE2=... KSU_EXPECTED_HASH2=...\n'
                        ' */\n'
                        '\n'
                        '#endif /* __KSU_MANAGER_SIGNATURE_H */\n'
                    )
                print(f"\nWritten: {out}")
                return 0
            else:
                print(f"WARN: cert size {cert_len} unexpected"); return 1

        off += seq_len + 8

    print("ERROR: v2 signature not found"); return 1

if __name__ == '__main__':
    if len(sys.argv) > 1:
        apk = sys.argv[1]
    else:
        apk = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'manager.apk')
    print(f"Analyzing: {apk}")
    sys.exit(main(apk))

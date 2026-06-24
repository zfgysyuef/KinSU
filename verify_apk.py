import zipfile, struct, sys, os

apk_path = sys.argv[1] if len(sys.argv) > 1 else r'd:\KinSU\KernelSU\dist\KinSU_v30022_30022-release.apk'

with zipfile.ZipFile(apk_path) as z:
    print(f"APK: {apk_path}")
    print(f"Total entries: {len(z.namelist())}")
    
    # Check lib files
    for n in sorted(z.namelist()):
        if 'lib/' in n:
            info = z.getinfo(n)
            print(f"  {n}: {info.file_size:,} bytes")
    
    # Verify APK signing block
    data = open(apk_path, 'rb').read()
    # Find EOCD
    eocd = data.rfind(b'PK\x05\x06')
    if eocd >= 0:
        cd_offset = struct.unpack_from('<I', data, eocd + 16)[0]
        # V2 signing block is before CD
        if cd_offset >= 32:
            sig_block_size = struct.unpack_from('<Q', data, cd_offset - 24)[0]
            print(f"\nAPK Signing Block: {sig_block_size} bytes at offset {cd_offset - 8 - sig_block_size}")
            # Check for v2/v3 signatures
            sig_data = data[cd_offset - 8 - sig_block_size:cd_offset - 24]
            if b'APK Sig Block 42' in sig_data[:16]:
                print("  APK Signature Scheme v2/v3: PRESENT")
    
    # Check for required lib
    lib_name = 'lib/arm64-v8a/libKinSUd.so'
    if lib_name in z.namelist():
        lib_data = z.read(lib_name)
        print(f"\n{lib_name}: {len(lib_data):,} bytes")
        has_ksuinit = b'Hello, KernelSU!' in lib_data
        has_KinSU = b'KinSU.ko' in lib_data
        elfs = lib_data.count(b'\x7fELF')
        print(f"  Contains 'Hello, KernelSU!': {has_ksuinit}")
        print(f"  Contains 'KinSU.ko': {has_KinSU}")
        print(f"  ELF headers count: {elfs}")
    else:
        print(f"\nWARNING: {lib_name} NOT FOUND in APK!")

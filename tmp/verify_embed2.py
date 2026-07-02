import os, zipfile

apk_path = '/mnt/d/KinSU/KernelSU/dist/KinSU_v30022_30022-release.apk'
# Check libfollkerneld.so size in APK
with zipfile.ZipFile(apk_path, 'r') as z:
    for name in z.namelist():
        if 'follkerneld' in name or 'follkernel' in name:
            info = z.getinfo(name)
            print(f'{name}: compressed={info.compress_size}, uncompressed={info.file_size}')

# Also check the ksud binary for .ko content via decompression
# rust-embed uses compression, so we need to check at runtime
# Let's just check the file sizes
ko_dir = '/mnt/d/KinSU/KernelSU/userspace/ksud/bin/aarch64'
print('\n.ko files on disk:')
for f in sorted(os.listdir(ko_dir)):
    if f.endswith('_follkernel.ko'):
        size = os.path.getsize(os.path.join(ko_dir, f))
        print(f'  {f}: {size} bytes')

ksud_path = '/mnt/d/KinSU/KernelSU/target/aarch64-linux-android/release/ksud'
print(f'\nksud size: {os.path.getsize(ksud_path)} bytes')

# Check if the .ko files are referenced in the rust-embed metadata
ksud = open(ksud_path, 'rb').read()
for f in sorted(os.listdir(ko_dir)):
    if f.endswith('_follkernel.ko'):
        name_bytes = f.encode()
        idx = ksud.find(name_bytes)
        if idx >= 0:
            print(f'  {f}: name found at offset 0x{idx:x}')
        else:
            print(f'  {f}: name NOT found in ksud!')

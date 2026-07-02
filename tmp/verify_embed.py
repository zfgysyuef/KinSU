import sys

ksud = open('/mnt/d/KinSU/KernelSU/target/aarch64-linux-android/release/ksud', 'rb').read()
ksuinit = open('/mnt/d/KinSU/KernelSU/userspace/ksud/bin/aarch64/ksuinit', 'rb').read()

# Find ksuinit in ksud
idx = ksud.find(ksuinit[:100])
if idx >= 0:
    print(f'ksuinit found at offset {idx} (0x{idx:x})')
    end_idx = idx + len(ksuinit)
    if ksud[idx:end_idx] == ksuinit:
        print(f'Full ksuinit embedded correctly ({len(ksuinit)} bytes)')
    else:
        print('ksuinit partial match only!')
else:
    print('ksuinit NOT found in ksud!')

# Also check .ko files
import os
ko_dir = '/mnt/d/KinSU/KernelSU/userspace/ksud/bin/aarch64'
for f in sorted(os.listdir(ko_dir)):
    if f.endswith('_follkernel.ko'):
        ko_data = open(os.path.join(ko_dir, f), 'rb').read()
        idx = ksud.find(ko_data[:100])
        if idx >= 0:
            print(f'{f}: found at offset 0x{idx:x} ({len(ko_data)} bytes)')
        else:
            print(f'{f}: NOT found in ksud!')

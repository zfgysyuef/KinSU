#!/usr/bin/env python3
"""
Binary-patch .ko files to embed a new EXPECTED_HASH and optionally EXPECTED_SIZE.

This patches the literal SHA256 hash string stored in the kernel module's
.rodata section, so the kernel module recognizes the correct manager APK.

Usage:
    python scripts/patch_ko_signatures.py <new_hash> [new_size_hex]

If new_size_hex is omitted, only the hash is patched (size stays unchanged).
"""

import sys
import struct
from pathlib import Path

LKM_DIR = Path(__file__).resolve().parent.parent / "KernelSU" / "manager" / "app" / "src" / "main" / "assets" / "lkm"

# Current values in pre-compiled .ko files
OLD_HASH = "93382c37e103fc4c3eee98bda5f96c577e3d7769ba424f01d897ba01fb08cf33"

# ARM64 MOVZ immediate encoding for 0x2d4 (=724)
# MOVZ w?, #0x2d4: the 16-bit immediate 0x02d4 appears at bits [20:5]
# We search for any MOVZ with hw=0 containing 0x02d4 as imm16
OLD_SIZE = 0x2d4  # 724 bytes


def patch_ko(filepath: Path, new_hash: str, new_size: int | None = None) -> bool:
    """Patch a single .ko file. Returns True if successful."""
    with open(filepath, "rb") as f:
        data = bytearray(f.read())

    old_hash_bytes = OLD_HASH.encode("ascii")
    new_hash_bytes = new_hash.encode("ascii")

    if len(new_hash_bytes) != 64:
        print(f"  ERROR: new hash must be 64 hex chars, got {len(new_hash_bytes)}")
        return False

    # 1. Patch hash string
    idx = data.find(old_hash_bytes)
    if idx < 0:
        print(f"  WARNING: old hash not found in {filepath.name}, skipping")
        return False

    # Verify unique
    idx2 = data.find(old_hash_bytes, idx + 1)
    if idx2 >= 0:
        print(f"  WARNING: hash appears multiple times ({idx}, {idx2}), may be unsafe to patch")
        return False

    print(f"  Patching hash at offset {idx} (0x{idx:x})")
    data[idx:idx + 64] = new_hash_bytes

    # 2. Patch EXPECTED_SIZE if requested and different
    if new_size is not None and new_size != OLD_SIZE:
        old_size_bytes = struct.pack("<I", OLD_SIZE)
        new_size_bytes = struct.pack("<I", new_size)

        # Search for 0x2d4 as a standalone 4-byte value in .rodata
        # (The compiler might store it as a literal pool entry)
        patched_size = False
        pos = 0
        while True:
            p = data.find(old_size_bytes, pos)
            if p < 0:
                break
            # Check if this looks like a data section entry (aligned, near other constants)
            data[p:p + 4] = new_size_bytes
            print(f"  Patched size 0x{OLD_SIZE:x}->0x{new_size:x} at offset {p} (0x{p:x})")
            patched_size = True
            pos = p + 4

        if not patched_size:
            # Try ARM64 immediate encoding in .text section
            # MOVZ: imm16 at bits [20:5]. For 0x2d4, imm16=0x02d4
            old_imm = OLD_SIZE & 0xFFFF  # 0x02d4
            new_imm = new_size & 0xFFFF

            # Look for MOVZ instructions containing the immediate
            # MOVZ format: [31:23]=0_10_100101, [22:21]=hw, [20:5]=imm16, [4:0]=Rd
            found_arm = False
            for i in range(0, len(data) - 3, 4):
                instr = struct.unpack_from("<I", data, i)[0]
                # Check MOVZ: sf=0 (bit31=0), opc=10 (movz), then check imm16
                if (instr >> 23) & 0x1FF == 0b0_10_100101:
                    imm16 = (instr >> 5) & 0xFFFF
                    hw = (instr >> 21) & 0x3
                    if imm16 == old_imm and hw == 0:
                        # Replace the immediate
                        new_instr = (instr & ~(0xFFFF << 5)) | (new_imm << 5)
                        struct.pack_into("<I", data, i, new_instr)
                        print(f"  Patched ARM64 MOVZ at {i} (0x{i:x}): 0x{instr:08x} -> 0x{new_instr:08x}")
                        found_arm = True

            if not found_arm:
                print(f"  WARNING: could not find EXPECTED_SIZE 0x{OLD_SIZE:x} in binary")

    # Write back
    with open(filepath, "wb") as f:
        f.write(data)

    return True


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)

    # Find the current hash in the first .ko file
    first_ko = sorted(LKM_DIR.glob("*_rekernel.ko"))[0]
    with open(first_ko, "rb") as f:
        data = f.read()
    # Search for any 64-char hex string (the hash)
    import re
    matches = re.findall(rb"[0-9a-f]{64}", data)
    if matches:
        global OLD_HASH
        OLD_HASH = matches[0].decode("ascii")
        print(f"Auto-detected old hash: {OLD_HASH}")
    else:
        print(f"ERROR: no 64-char hex string found in {first_ko.name}")
        sys.exit(1)

    new_hash = sys.argv[1].strip().lower()
    if len(new_hash) != 64 or not all(c in "0123456789abcdef" for c in new_hash):
        print("ERROR: hash must be 64 hex characters")
        sys.exit(1)

    new_size = None
    if len(sys.argv) >= 3:
        val = sys.argv[2].strip()
        if val.startswith("0x"):
            new_size = int(val, 16)
        else:
            new_size = int(val)

    print(f"New hash: {new_hash}")
    if new_size is not None:
        print(f"New size: {new_size} (0x{new_size:x})")
    print(f"Target directory: {LKM_DIR}")
    print()

    success = 0
    for ko_file in sorted(LKM_DIR.glob("*_rekernel.ko")):
        print(f"Processing {ko_file.name}...")
        if patch_ko(ko_file, new_hash, new_size):
            success += 1
        print()

    print(f"Done: {success} .ko files patched")

    if success > 0:
        print()
        print("Next steps:")
        print("1. Re-patch boot image with updated .ko files")
        print("2. Flash patched boot image")
        print("3. Install matching manager APK")


if __name__ == "__main__":
    main()

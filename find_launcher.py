#!/usr/bin/env python3
import os
from PIL import Image

res_dir = "/mnt/d/FollKernel/tmp/folkpatch_extract/res"
candidates = []

for f in os.listdir(res_dir):
    if not f.endswith(".png"):
        continue
    path = os.path.join(res_dir, f)
    try:
        sz = os.path.getsize(path)
        if sz < 2000 or sz > 300000:
            continue
        img = Image.open(path)
        w, h = img.size
        # Launcher icons are typically square, 48-192px
        if w == h and 48 <= w <= 256:
            candidates.append((w, sz, f, img.mode))
        # Also catch non-square that might be icons
        elif 48 <= w <= 256 and 48 <= h <= 256:
            candidates.append((w, sz, f"{w}x{h} {img.mode} {f}", "rect"))
    except Exception as e:
        pass

candidates.sort(key=lambda x: x[0])
print("Square icons (likely launcher):")
for c in candidates:
    print(f"  {c[0]}x{c[0]} {c[2]} ({c[1]} bytes) {c[3]}")

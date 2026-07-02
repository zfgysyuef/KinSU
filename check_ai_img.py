#!/usr/bin/env python3
"""Check if AI-generated image is valid or a placeholder."""
import os
from PIL import Image

files = [
    "/mnt/d/FollKernel/tmp/bohe_ai_icon_v1.png",
    "/mnt/d/FollKernel/tmp/bohe_ai_icon_v2.png",
    "/mnt/d/FollKernel/tmp/bohe_ai_circle.png",
    "/mnt/d/FollKernel/tmp/icon_comparison.png",
]

for p in files:
    if not os.path.exists(p):
        print(f"{os.path.basename(p)}: NOT FOUND")
        continue
    sz = os.path.getsize(p)
    try:
        img = Image.open(p)
        print(f"{os.path.basename(p)}: {sz} bytes, {img.size[0]}x{img.size[1]} {img.mode}")
        # Sample center pixel to check if it's a real image or placeholder
        w, h = img.size
        px = img.convert("RGB").load()
        print(f"  center pixel: {px[w//2, h//2]}")
        print(f"  corner pixel: {px[0, 0]}")
    except Exception as e:
        print(f"{os.path.basename(p)}: ERROR - {e}")

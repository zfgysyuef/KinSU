#!/usr/bin/env python3
"""
Analyze the FolkPatch icon style and describe it for AI generation.
Also create a visual contact sheet of all candidate icons.
"""
import os
from PIL import Image, ImageDraw

icons = [
    "/mnt/d/FollKernel/tmp/folkpatch_icon_96.png",
    "/mnt/d/FollKernel/tmp/folkpatch_icon_108.png",
    "/mnt/d/FollKernel/tmp/folkpatch_icon_144.png",
    "/mnt/d/FollKernel/tmp/folkpatch_icon_192.png",
    "/mnt/d/FollKernel/tmp/folkpatch_icon_216.png",
]

print("=== FolkPatch Icon Analysis ===\n")
for p in icons:
    if not os.path.exists(p):
        continue
    img = Image.open(p).convert("RGBA")
    w, h = img.size
    print(f"\n{os.path.basename(p)}: {w}x{h}")

    # Sample colors from corners and center
    pixels = img.load()
    corners = [
        ("top-left", pixels[0, 0]),
        ("top-right", pixels[w-1, 0]),
        ("bottom-left", pixels[0, h-1]),
        ("bottom-right", pixels[w-1, h-1]),
        ("center", pixels[w//2, h//2]),
    ]
    print("  Colors:")
    for name, color in corners:
        print(f"    {name}: RGBA{color}")

    # Check if corners are transparent (rounded/circle icon)
    tl_alpha = pixels[0, 0][3]
    tr_alpha = pixels[w-1, 0][3]
    bl_alpha = pixels[0, h-1][3]
    br_alpha = pixels[w-1, h-1][3]
    if tl_alpha == 0 and tr_alpha == 0 and bl_alpha == 0 and br_alpha == 0:
        print("  Shape: ROUNDED/CIRCLE (transparent corners)")
    else:
        print("  Shape: SQUARE (opaque corners)")

    # Find dominant non-transparent colors
    from collections import Counter
    color_counter = Counter()
    for x in range(0, w, max(1, w//20)):
        for y in range(0, h, max(1, h//20)):
            r, g, b, a = pixels[x, y]
            if a > 128:
                # Quantize to reduce color count
                rq, gq, bq = r//32*32, g//32*32, b//32*32
                color_counter[(rq, gq, bq)] += 1
    print("  Top colors (quantized):")
    for color, count in color_counter.most_common(5):
        print(f"    RGB{color}: {count} samples")

# Create a contact sheet
sheet = Image.new("RGBA", (256*5, 256), (255, 255, 255, 255))
for i, p in enumerate(icons):
    if not os.path.exists(p):
        continue
    img = Image.open(p).convert("RGBA")
    # Scale up to 256x256 for visibility
    img_big = img.resize((256, 256), Image.LANCZOS)
    sheet.paste(img_big, (i*256, 0), img_big)

sheet.save("/mnt/d/FollKernel/tmp/folkpatch_icons_sheet.png")
print("\n\nContact sheet saved: /mnt/d/FollKernel/tmp/folkpatch_icons_sheet.png")

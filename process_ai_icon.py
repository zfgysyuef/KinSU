#!/usr/bin/env python3
"""
Process AI-generated Bohr icon: create circle-cropped version matching
FolkPatch style, and a comparison sheet with the original AnQuQu icon.
"""
from PIL import Image, ImageDraw

# AI generated icon
ai_icon = Image.open("/mnt/d/FollKernel/tmp/bohe_ai_icon_v2.png").convert("RGBA")
print(f"AI icon: {ai_icon.size} {ai_icon.mode}")

# FolkPatch AnQuQu icon (192x192 reference)
folk_icon = Image.open("/mnt/d/FollKernel/tmp/folkpatch_icon_192.png").convert("RGBA")
print(f"FolkPatch icon: {folk_icon.size} {folk_icon.mode}")

# Create circle-cropped version of AI icon (512x512 for quality)
size = 512
ai_resized = ai_icon.resize((size, size), Image.LANCZOS)

# Circle mask
mask = Image.new("L", (size, size), 0)
draw = ImageDraw.Draw(mask)
draw.ellipse((0, 0, size, size), fill=255)

# Apply circle mask
ai_circle = ai_resized.copy()
ai_circle.putalpha(mask)
ai_circle.save("/mnt/d/FollKernel/tmp/bohe_ai_circle.png")
print(f"Circle version saved: bohe_ai_circle.png")

# Create comparison sheet: FolkPatch icon | AI Bohr icon | AI Bohr circle
sheet_w = 320 * 3
sheet_h = 400
sheet = Image.new("RGBA", (sheet_w, sheet_h), (245, 245, 245, 255))

# FolkPatch icon (scaled up)
folk_big = folk_icon.resize((320, 320), Image.LANCZOS)
sheet.paste(folk_big, (0, 40), folk_big)

# AI icon (scaled)
ai_big = ai_icon.resize((320, 320), Image.LANCZOS)
sheet.paste(ai_big, (320, 40), ai_big if ai_big.mode == "RGBA" else None)

# AI circle icon
ai_circle_big = ai_circle.resize((320, 320), Image.LANCZOS)
sheet.paste(ai_circle_big, (640, 40), ai_circle_big)

# Add labels using simple text
from PIL import ImageFont
try:
    font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 24)
except:
    font = ImageFont.load_default()

draw = ImageDraw.Draw(sheet)
draw.text((160, 8), "FolkPatch AnQuQu", fill=(50, 50, 50), font=font, anchor="mt")
draw.text((480, 8), "AI Bohr (square)", fill=(50, 50, 50), font=font, anchor="mt")
draw.text((800, 8), "AI Bohr (circle)", fill=(50, 50, 50), font=font, anchor="mt")

sheet.save("/mnt/d/FollKernel/tmp/icon_comparison.png")
print(f"Comparison sheet saved: icon_comparison.png")
print(f"\nAll files in /mnt/d/FollKernel/tmp/:")
import os
for f in ["bohe_ai_icon_v2.png", "bohe_ai_circle.png", "icon_comparison.png", "folkpatch_icon_192.png"]:
    p = f"/mnt/d/FollKernel/tmp/{f}"
    if os.path.exists(p):
        print(f"  {f}: {os.path.getsize(p)} bytes")

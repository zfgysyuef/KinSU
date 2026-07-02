from PIL import Image, ImageDraw
import os, base64

# Use the high-res full-body source but crop tightly around the face
src = "/mnt/d/FollKernel/tmp/bohe_web_raw.jpg"
# Also the small head portrait for a possible alternative
src2 = "/mnt/d/FollKernel/tmp/bohe_web_raw2.jpg"
out_dir = "/mnt/d/FollKernel/tmp"

img = Image.open(src).convert("RGBA")
w, h = img.size
print(f"Full source: {img.size}")

# Tight face crop (head + a bit of shoulders/hair)
left, top, right, bottom = 150, 150, 450, 450
face_crop = img.crop((left, top, right, bottom))
print(f"Face crop: {face_crop.size}")

# Small portrait upscale for comparison
small = Image.open(src2).convert("RGBA")
print(f"Small portrait: {small.size}")

bg_color = (245, 240, 230, 255)

def make_circle_icon(source, size, bg=bg_color):
    src_resized = source.resize((size, size), Image.LANCZOS)
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    margin = size // 40
    draw.ellipse((margin, margin, size - margin, size - margin), fill=255)
    circle = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    circle.paste(src_resized, (0, 0), mask)
    bg_img = Image.new("RGBA", (size, size), bg)
    bg_img.paste(circle, (0, 0), circle)
    return bg_img

# Generate previews
variants = [
    (face_crop, "bohe_icon_face_192.png", 192),
    (face_crop, "bohe_icon_face_512.png", 512),
    (small, "bohe_icon_small_192.png", 192),
]
for source, name, s in variants:
    icon = make_circle_icon(source, s)
    path = os.path.join(out_dir, name)
    icon.save(path)
    print(f"Saved {path}")

# HTML preview
html = """<!DOCTYPE html>
<html><head><meta charset="utf-8"><title>Bohr Icon Preview v2</title>
<style>
body { background: #f0f0f0; font-family: sans-serif; padding: 20px; }
.img-box { display: inline-block; margin: 10px; text-align: center; background: white; padding: 15px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.img-box img { max-width: 300px; max-height: 300px; border: 1px solid #ddd; }
.img-box .label { margin-top: 8px; font-size: 14px; color: #333; }
</style></head><body>
<h2>Bohr Icon Preview v2 (face crop from high-res source)</h2>
"""
images = [
    ("/mnt/d/FollKernel/tmp/folkpatch_icon_192.png", "FolkPatch AnQuQu (reference)"),
    ("/mnt/d/FollKernel/tmp/bohe_icon_face_192.png", "Face crop circle 192px"),
    ("/mnt/d/FollKernel/tmp/bohe_icon_face_512.png", "Face crop circle 512px"),
    ("/mnt/d/FollKernel/tmp/bohe_icon_small_192.png", "Small portrait upscale 192px"),
]
for path, label in images:
    try:
        with open(path, "rb") as f:
            data = base64.b64encode(f.read()).decode()
        ext = path.split(".")[-1]
        html += f'<div class="img-box"><img src="data:image/{ext};base64,{data}"><div class="label">{label}</div></div>\n'
    except Exception as e:
        html += f'<div class="img-box"><div class="label">{label}: ERROR - {e}</div></div>\n'
html += "</body></html>"
with open(f"{out_dir}/preview_bohe_v2.html", "w", encoding="utf-8") as f:
    f.write(html)
print("Preview saved:", f"{out_dir}/preview_bohe_v2.html")

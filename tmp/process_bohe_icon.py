from PIL import Image, ImageDraw, ImageFilter
import os, base64

src = "/mnt/d/FollKernel/tmp/bohe_web_raw.jpg"
out_dir = "/mnt/d/FollKernel/tmp"
img = Image.open(src).convert("RGBA")
print(f"Source: {img.size}")

# Crop a square around the head/upper body
w, h = img.size
cx, cy = w // 2, 340  # approximate face center
crop_size = 520
left = max(0, cx - crop_size // 2)
top = max(0, cy - crop_size // 2)
right = min(w, left + crop_size)
bottom = min(h, top + crop_size)
cropped = img.crop((left, top, right, bottom))
print(f"Crop box: {(left, top, right, bottom)}")

# Background color (cream beige)
bg_color = (245, 240, 230, 255)

def make_circle_icon(source, size):
    # Resize source to fill the circle
    src_resized = source.resize((size, size), Image.LANCZOS)
    # Create circular mask
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    margin = size // 40
    draw.ellipse((margin, margin, size - margin, size - margin), fill=255)
    # Apply mask
    circle = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    circle.paste(src_resized, (0, 0), mask)
    # Composite on beige background
    bg = Image.new("RGBA", (size, size), bg_color)
    bg.paste(circle, (0, 0), circle)
    return bg

sizes = [(192, "preview_192"), (512, "preview_512")]
for s, name in sizes:
    icon = make_circle_icon(cropped, s)
    path = os.path.join(out_dir, f"bohe_icon_{name}.png")
    icon.save(path)
    print(f"Saved {path}")

# Create comparison HTML with reference and our icon
html = """<!DOCTYPE html>
<html><head><meta charset="utf-8"><title>Bohr Icon Preview</title>
<style>
body { background: #f0f0f0; font-family: sans-serif; padding: 20px; }
.img-box { display: inline-block; margin: 10px; text-align: center; background: white; padding: 15px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.img-box img { max-width: 300px; max-height: 300px; border: 1px solid #ddd; }
.img-box .label { margin-top: 8px; font-size: 14px; color: #333; }
</style></head><body>
<h2>Bohr Icon Preview (web source + FolkPatch circle style)</h2>
"""
images = [
    ("/mnt/d/FollKernel/tmp/folkpatch_icon_192.png", "FolkPatch AnQuQu (reference)"),
    ("/mnt/d/FollKernel/tmp/bohe_icon_preview_192.png", "Bohr circle 192px"),
    ("/mnt/d/FollKernel/tmp/bohe_icon_preview_512.png", "Bohr circle 512px"),
    ("/mnt/d/FollKernel/tmp/bohe_web_raw.jpg", "Original web source"),
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
with open(f"{out_dir}/preview_bohe.html", "w", encoding="utf-8") as f:
    f.write(html)
print("Preview saved:", f"{out_dir}/preview_bohe.html")

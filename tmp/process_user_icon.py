from PIL import Image, ImageFilter, ImageEnhance, ImageDraw
import os, base64

src = "/mnt/d/FollKernel/tmp/user_icon_raw2.jpg"
out_dir = "/mnt/d/FollKernel/tmp"
img = Image.open(src).convert("RGBA")
print(f"Source: {img.size}")

# Make square by padding with white background
w, h = img.size
size = max(w, h)
square = Image.new("RGBA", (size, size), (255, 255, 255, 255))
offset = ((size - w) // 2, (size - h) // 2)
square.paste(img, offset, img)

# Enhance clarity: sharpen and slightly increase contrast/sharpness
square = square.filter(ImageFilter.UnsharpMask(radius=2, percent=120, threshold=3))
enhancer = ImageEnhance.Contrast(square)
square = enhancer.enhance(1.05)
enhancer = ImageEnhance.Sharpness(square)
square = enhancer.enhance(1.1)

# Save a high-res master
master = square.resize((512, 512), Image.LANCZOS)
master.save(os.path.join(out_dir, "user_icon_master.png"))
print("Saved master 512x512")

# Generate legacy mipmap sizes
mipmap_sizes = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}
for dpi, s in mipmap_sizes.items():
    icon = square.resize((s, s), Image.LANCZOS)
    icon.save(os.path.join(out_dir, f"ic_launcher_{dpi}.png"))
    # round uses same content (launcher will mask)
    icon.save(os.path.join(out_dir, f"ic_launcher_round_{dpi}.png"))
    print(f"Generated mipmap-{dpi} {s}x{s}")

# Generate adaptive foreground sizes (transparent outside content not needed, keep white bg)
fg_sizes = {
    "mdpi": 108,
    "hdpi": 162,
    "xhdpi": 216,
    "xxhdpi": 324,
    "xxxhdpi": 432,
}
for dpi, s in fg_sizes.items():
    fg = square.resize((s, s), Image.LANCZOS)
    fg.save(os.path.join(out_dir, f"ic_launcher_foreground_{dpi}.png"))
    print(f"Generated drawable-{dpi} foreground {s}x{s}")

# Also create a circular-crop preview for comparison
def circle_crop(src_img, size):
    resized = src_img.resize((size, size), Image.LANCZOS)
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size, size), fill=255)
    out = Image.new("RGBA", (size, size), (255, 255, 255, 0))
    out.paste(resized, (0, 0), mask)
    bg = Image.new("RGBA", (size, size), (255, 255, 255, 255))
    bg.paste(out, (0, 0), out)
    return bg

circle_512 = circle_crop(square, 512)
circle_512.save(os.path.join(out_dir, "user_icon_circle_512.png"))
print("Saved circular preview 512x512")

# HTML preview
html = """<!DOCTYPE html>
<html><head><meta charset="utf-8"><title>User Icon Preview</title>
<style>
body { background: #f0f0f0; font-family: sans-serif; padding: 20px; }
.img-box { display: inline-block; margin: 10px; text-align: center; background: white; padding: 15px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.img-box img { max-width: 300px; max-height: 300px; border: 1px solid #ddd; image-rendering: auto; }
.img-box .label { margin-top: 8px; font-size: 14px; color: #333; }
</style></head><body>
<h2>User Provided Icon Preview</h2>
"""
images = [
    ("/mnt/d/FollKernel/tmp/user_icon_raw2.jpg", "Original downloaded"),
    ("/mnt/d/FollKernel/tmp/user_icon_master.png", "Square master 512px (sharpened)"),
    ("/mnt/d/FollKernel/tmp/user_icon_circle_512.png", "Circular crop 512px"),
    ("/mnt/d/FollKernel/tmp/ic_launcher_xxxhdpi.png", "Legacy launcher 192px"),
    ("/mnt/d/FollKernel/tmp/ic_launcher_foreground_xxxhdpi.png", "Adaptive foreground 432px"),
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
with open(f"{out_dir}/preview_user_icon.html", "w", encoding="utf-8") as f:
    f.write(html)
print("Preview saved:", f"{out_dir}/preview_user_icon.html")

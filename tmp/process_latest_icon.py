from PIL import Image, ImageFilter, ImageEnhance, ImageDraw
import os, base64

src = "/mnt/c/Users/hanha/Pictures/Screenshots/C939DA2401A81C320AB1817A6DCF12A9.png"
out_dir = "/mnt/d/FollKernel/tmp"

img = Image.open(src).convert("RGBA")
w, h = img.size
print(f"Source: {w}x{h}")

# Find and crop the largest square from center
size = min(w, h)
left = (w - size) // 2
top = (h - size) // 2
right = left + size
bottom = top + size
square = img.crop((left, top, right, bottom))
print(f"Crop box: ({left}, {top}, {right}, {bottom})")

# Enhance clarity
square = square.filter(ImageFilter.UnsharpMask(radius=2, percent=120, threshold=3))
enhancer = ImageEnhance.Contrast(square)
square = enhancer.enhance(1.05)
enhancer = ImageEnhance.Sharpness(square)
square = enhancer.enhance(1.1)

# Save high-res master
master = square.resize((512, 512), Image.LANCZOS)
master.save(os.path.join(out_dir, "latest_icon_master.png"))
print("Saved master 512x512")

# Generate legacy mipmap sizes
mipmap_sizes = {
    "ldpi": 36,
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}
for dpi, s in mipmap_sizes.items():
    icon = square.resize((s, s), Image.LANCZOS)
    icon.save(os.path.join(out_dir, f"latest_icon_{dpi}.png"))
    icon.save(os.path.join(out_dir, f"latest_icon_round_{dpi}.png"))
    print(f"Generated mipmap-{dpi} {s}x{s}")

# Generate adaptive foreground sizes
fg_sizes = {
    "mdpi": 108,
    "hdpi": 162,
    "xhdpi": 216,
    "xxhdpi": 324,
    "xxxhdpi": 432,
}
for dpi, s in fg_sizes.items():
    fg = square.resize((s, s), Image.LANCZOS)
    fg.save(os.path.join(out_dir, f"latest_icon_foreground_{dpi}.png"))
    print(f"Generated drawable-{dpi} foreground {s}x{s}")

print("Done")
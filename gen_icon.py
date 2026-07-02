from PIL import Image, ImageDraw
import os

src = r"C:\Users\hanha\Pictures\4E9C6FFC3D878F6A3FA7B43DD1B1A908.jpg"
img = Image.open(src)
print(f"Source: {os.path.basename(src)}")
print(f"Size: {img.size}, Mode: {img.mode}")
print(f"File size: {os.path.getsize(src)} bytes")

if img.mode != "RGB":
    img = img.convert("RGB")

w, h = img.size
print(f"Dimensions: {w}x{h}")

# Center crop to square
side = min(w, h)
left = (w - side) // 2
top = (h - side) // 2
right = left + side
bottom = top + side
square = img.crop((left, top, right, bottom))
print(f"Cropped to square: {square.size}")

res_dir = r"d:\FollKernel\KernelSU\manager\app\src\main\res"

# Density -> launcher icon size (48dp)
launcher_sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

# 1) Standard launcher icons (full-bleed square)
for folder, size in launcher_sizes.items():
    out = os.path.join(res_dir, folder, "ic_launcher.png")
    square.resize((size, size), Image.LANCZOS).save(out, "PNG")
    print(f"Saved: {out} ({size}x{size})")

# 2) Round launcher icons (circular mask, full-bleed)
for folder, size in launcher_sizes.items():
    out = os.path.join(res_dir, folder, "ic_launcher_round.png")
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).ellipse([0, 0, size - 1, size - 1], fill=255)
    round_img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    round_img.paste(square.resize((size, size), Image.LANCZOS), (0, 0), mask)
    round_img.save(out, "PNG")
    print(f"Saved: {out} ({size}x{size})")

# 3) Adaptive icon foreground -> drawable-*/ (referenced by mipmap-anydpi/ic_launcher.xml)
# Total canvas = 108dp; safe zone = center 72dp (~66.67%). Scale image to safe zone and
# center it so the system's edge crop never cuts the image.
fg_folders = {
    "drawable-mdpi": 108,
    "drawable-hdpi": 162,
    "drawable-xhdpi": 216,
    "drawable-xxhdpi": 324,
    "drawable-xxxhdpi": 432,
}
SAFE_RATIO = 72.0 / 108.0  # 0.6667
for folder, canvas in fg_folders.items():
    out = os.path.join(res_dir, folder, "ic_launcher_foreground.png")
    fg = Image.new("RGBA", (canvas, canvas), (0, 0, 0, 0))
    inner = int(canvas * SAFE_RATIO)
    scaled = square.resize((inner, inner), Image.LANCZOS)
    off = (canvas - inner) // 2
    fg.paste(scaled, (off, off))
    fg.save(out, "PNG")
    print(f"Saved foreground: {out} ({canvas}x{canvas}, inner={inner})")

# 4) Remove stale foreground previously generated under mipmap-*/ (wrong path)
for folder in launcher_sizes:
    stale = os.path.join(res_dir, folder, "ic_launcher_foreground.png")
    if os.path.exists(stale):
        os.remove(stale)
        print(f"Removed stale: {stale}")

print("\nAll icons generated successfully!")

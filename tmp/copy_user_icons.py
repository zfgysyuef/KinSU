import shutil, os

src_dir = "/mnt/d/FollKernel/tmp"
dst_root = "/mnt/d/FollKernel/KernelSU/manager/app/src/main/res"

copies = [
    # mipmap legacy icons
    ("ic_launcher_ldpi.png", "mipmap-ldpi/ic_launcher.png"),
    ("ic_launcher_mdpi.png", "mipmap-mdpi/ic_launcher.png"),
    ("ic_launcher_hdpi.png", "mipmap-hdpi/ic_launcher.png"),
    ("ic_launcher_xhdpi.png", "mipmap-xhdpi/ic_launcher.png"),
    ("ic_launcher_xxhdpi.png", "mipmap-xxhdpi/ic_launcher.png"),
    ("ic_launcher_xxxhdpi.png", "mipmap-xxxhdpi/ic_launcher.png"),
    # round icons
    ("ic_launcher_round_mdpi.png", "mipmap-mdpi/ic_launcher_round.png"),
    ("ic_launcher_round_hdpi.png", "mipmap-hdpi/ic_launcher_round.png"),
    ("ic_launcher_round_xhdpi.png", "mipmap-xhdpi/ic_launcher_round.png"),
    ("ic_launcher_round_xxhdpi.png", "mipmap-xxhdpi/ic_launcher_round.png"),
    ("ic_launcher_round_xxxhdpi.png", "mipmap-xxxhdpi/ic_launcher_round.png"),
    # adaptive foregrounds
    ("ic_launcher_foreground_mdpi.png", "drawable-mdpi/ic_launcher_foreground.png"),
    ("ic_launcher_foreground_hdpi.png", "drawable-hdpi/ic_launcher_foreground.png"),
    ("ic_launcher_foreground_xhdpi.png", "drawable-xhdpi/ic_launcher_foreground.png"),
    ("ic_launcher_foreground_xxhdpi.png", "drawable-xxhdpi/ic_launcher_foreground.png"),
    ("ic_launcher_foreground_xxxhdpi.png", "drawable-xxxhdpi/ic_launcher_foreground.png"),
]

for src_name, dst_rel in copies:
    src = os.path.join(src_dir, src_name)
    dst = os.path.join(dst_root, dst_rel)
    if not os.path.exists(src):
        print(f"MISSING source: {src}")
        continue
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    shutil.copy2(src, dst)
    print(f"Copied {src_name} -> {dst_rel}")

print("Done")

import zipfile, os

apk = r"D:\FollKernel\KernelSU\dist\KinSU_3.1.7_30035-release.apk"
out = r"d:\FollKernel\tmp\extracted_icon_xxxhdpi.png"
with zipfile.ZipFile(apk, "r") as z:
    data = z.read("res/mipmap-xxxhdpi-v4/ic_launcher.png")
    with open(out, "wb") as f:
        f.write(data)
print("extracted", out, len(data))

import zipfile

apk = r"D:\FollKernel\KernelSU\dist\KinSU_3.1.7_30035-release.apk"
out = r"d:\FollKernel\tmp\extracted_icon_192.png"
with zipfile.ZipFile(apk, "r") as z:
    data = z.read("res/o-.png")
    with open(out, "wb") as f:
        f.write(data)
print("extracted", out, len(data))

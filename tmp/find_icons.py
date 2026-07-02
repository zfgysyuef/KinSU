import zipfile, io
from PIL import Image

apk = r"D:\FollKernel\KernelSU\dist\KinSU_3.1.7_30035-release.apk"
with zipfile.ZipFile(apk, "r") as z:
    for name in z.namelist():
        if not name.endswith(".png"):
            continue
        data = z.read(name)
        try:
            img = Image.open(io.BytesIO(data))
        except Exception:
            continue
        print(name, img.size)

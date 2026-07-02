import os, shutil
from pathlib import Path

os.environ["ANDROID_SDK_ROOT"] = "/home/hanha/Android/Sdk"
print("SDK_ROOT:", os.environ.get("ANDROID_SDK_ROOT"))
print("which zipalign:", shutil.which("zipalign"))
print("which apksigner:", shutil.which("apksigner"))

sdk_root = os.environ.get("ANDROID_SDK_ROOT")
build_tools = Path(sdk_root) / "build-tools"
print("build-tools exists:", build_tools.exists())
if build_tools.exists():
    for v in build_tools.iterdir():
        print("  version:", v.name)
        z = v / "zipalign"
        a = v / "apksigner"
        print("    zipalign:", z.exists(), z)
        print("    apksigner:", a.exists(), a)

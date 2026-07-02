#!/usr/bin/env python3
"""Download DDK image, extract, and inspect environment for building .ko."""
import sys, json, os, subprocess, urllib.request, tarfile

REGISTRY = "ghcr.io"
REPO = "ylarod/ddk-min"
TAG = sys.argv[1] if len(sys.argv) > 1 else "android14-6.1-20260313"
BASE_DIR = "/home/hanha/ddk-build"
OUT_DIR = os.path.join(BASE_DIR, TAG.replace(".", "_"))
WORK_DIR = os.path.join(BASE_DIR, f"dl-{os.getpid()}")

os.makedirs(WORK_DIR, exist_ok=True)
os.makedirs(OUT_DIR, exist_ok=True)

# 1. Get token
print(f"[1/5] Token for {REPO}:{TAG}...", flush=True)
token_url = f"https://{REGISTRY}/token?scope=repository:{REPO}:pull&service={REGISTRY}"
with urllib.request.urlopen(token_url, timeout=60) as r:
    token = json.load(r)["token"]

# 2. Get manifest
print(f"[2/5] Manifest...", flush=True)
manifest_url = f"https://{REGISTRY}/v2/{REPO}/manifests/{TAG}"
auth = {"Authorization": f"Bearer {token}",
        "Accept": "application/vnd.oci.image.manifest.v1+json, application/vnd.docker.distribution.manifest.v2+json"}
with urllib.request.urlopen(urllib.request.Request(manifest_url, headers=auth), timeout=60) as r:
    manifest = json.load(r)

layers = manifest.get("layers", [])
total = sum(l.get("size", 0) for l in layers)
print(f"  {len(layers)} layers, {total/1024/1024:.1f} MB", flush=True)

# 3. Download with aria2c
print(f"[3/5] Downloading...", flush=True)
input_file = os.path.join(WORK_DIR, "aria2-input.txt")
with open(input_file, "w") as f:
    for i, layer in enumerate(layers):
        digest = layer["digest"]
        blob_url = f"https://{REGISTRY}/v2/{REPO}/blobs/{digest}"
        out_name = f"layer-{i:02d}.tar.gz"
        f.write(f"{blob_url}\n  dir={WORK_DIR}\n  out={out_name}\n  header=Authorization: Bearer {token}\n")

result = subprocess.run(
    ["aria2c", "-i", input_file, "-j4", "-x16", "-s16", "-k1M",
     "--console-log-level=warn", "--summary-interval=10", "--auto-file-renaming=false",
     "--allow-overwrite=true"], cwd=WORK_DIR)
if result.returncode != 0:
    print(f"aria2c failed: {result.returncode}", flush=True)
    sys.exit(1)

# 4. Extract
print(f"[4/5] Extracting to {OUT_DIR}...", flush=True)
for i, layer in enumerate(layers):
    layer_file = os.path.join(WORK_DIR, f"layer-{i:02d}.tar.gz")
    if not os.path.exists(layer_file):
        continue
    size_mb = os.path.getsize(layer_file) / 1024 / 1024
    print(f"  [{i+1}/{len(layers)}] {size_mb:.1f} MB", flush=True)
    with open(layer_file, "rb") as f:
        with tarfile.open(fileobj=f, mode="r:*") as tar:
            for member in tar:
                try:
                    tar.extract(member, OUT_DIR)
                except Exception:
                    pass

# 5. Cleanup download
import shutil
shutil.rmtree(WORK_DIR, ignore_errors=True)
print(f"[5/5] Done. Rootfs at {OUT_DIR}", flush=True)

# Inspect environment
print("\n=== ENV INSPECTION ===", flush=True)
# Find kernel source / build tree
for name in ["vmlinux", "Module.symvers"]:
    r = subprocess.run(["find", OUT_DIR, "-name", name, "-not", "-path", "*/proc/*"],
                       capture_output=True, text=True, timeout=120)
    print(f"{name}: {r.stdout.strip()[:500]}", flush=True)

# Find clang
r = subprocess.run(["find", OUT_DIR, "-name", "clang", "-type", "f"],
                   capture_output=True, text=True, timeout=60)
print(f"clang: {r.stdout.strip()[:500]}", flush=True)

# Check env files
for envf in [os.path.join(OUT_DIR, "etc/environment"),
             os.path.join(OUT_DIR, "etc/profile.d/ksu.sh"),
             os.path.join(OUT_DIR, "root/.bashrc")]:
    if os.path.exists(envf):
        print(f"\n--- {envf} ---", flush=True)
        with open(envf) as f:
            print(f.read()[:500], flush=True)

# List top-level
print(f"\nTop-level: {sorted(os.listdir(OUT_DIR))[:20]}", flush=True)

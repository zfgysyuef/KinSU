#!/usr/bin/env python3
"""Pull OCI image layers from ghcr.io without docker."""
import sys, json, os, urllib.request, urllib.error

REGISTRY = "ghcr.io"
REPO = "ylarod/ddk-min"
TAG = sys.argv[1] if len(sys.argv) > 1 else "android14-6.1-20260313"
OUT_DIR = sys.argv[2] if len(sys.argv) > 2 else "/tmp/ddk-rootfs"

def http_get(url, headers=None):
    req = urllib.request.Request(url, headers=headers or {})
    return urllib.request.urlopen(req, timeout=60)

# 1. Get anonymous token
print(f"[1/4] Getting token for {REPO}:{TAG}...")
token_url = f"https://{REGISTRY}/token?scope=repository:{REPO}:pull&service={REGISTRY}"
with http_get(token_url) as r:
    token = json.load(r)["token"]
print(f"  token: {token[:30]}...")

auth = {"Authorization": f"Bearer {token}"}

# 2. Get manifest
print(f"[2/4] Getting manifest...")
manifest_url = f"https://{REGISTRY}/v2/{REPO}/manifests/{TAG}"
auth["Accept"] = "application/vnd.oci.image.manifest.v1+json, application/vnd.docker.distribution.manifest.v2+json"
with http_get(manifest_url, auth) as r:
    manifest = json.load(r)

layers = manifest.get("layers", [])
print(f"  layers: {len(layers)}")
total_size = sum(l.get("size", 0) for l in layers)
print(f"  total size: {total_size / (1024*1024):.1f} MB")

# 3. Download and extract layers
os.makedirs(OUT_DIR, exist_ok=True)
print(f"[3/4] Downloading layers to {OUT_DIR}...")

import tarfile, io
for i, layer in enumerate(layers):
    size_mb = layer.get("size", 0) / (1024*1024)
    digest = layer["digest"]
    print(f"  [{i+1}/{len(layers)}] {digest[:25]}... ({size_mb:.1f} MB)")
    blob_url = f"https://{REGISTRY}/v2/{REPO}/blobs/{digest}"
    with http_get(blob_url, auth) as r:
        # Stream extract
        with tarfile.open(fileobj=io.BytesIO(r.read()), mode="r|*") as tar:
            for member in tar:
                try:
                    tar.extract(member, OUT_DIR)
                except Exception as e:
                    print(f"    warn: {member.name}: {e}")

print(f"[4/4] Done. Rootfs at {OUT_DIR}")
print("Contents:")
for p in os.listdir(OUT_DIR)[:20]:
    print(f"  {p}")

#!/usr/bin/env python3
"""Check OCI image manifest from ghcr.io."""
import sys, json, urllib.request

REGISTRY = "ghcr.io"
REPO = "ylarod/ddk-min"
TAG = sys.argv[1] if len(sys.argv) > 1 else "android14-6.1-20260313"

# 1. Get anonymous token
print(f"Getting token for {REPO}:{TAG}...")
token_url = f"https://{REGISTRY}/token?scope=repository:{REPO}:pull&service={REGISTRY}"
with urllib.request.urlopen(token_url, timeout=60) as r:
    token = json.load(r)["token"]
print(f"  token: {token[:30]}...")

auth = {"Authorization": f"Bearer {token}",
        "Accept": "application/vnd.oci.image.manifest.v1+json, application/vnd.docker.distribution.manifest.v2+json"}

# 2. Get manifest
print(f"Getting manifest...")
manifest_url = f"https://{REGISTRY}/v2/{REPO}/manifests/{TAG}"
with urllib.request.urlopen(urllib.request.Request(manifest_url, headers=auth), timeout=60) as r:
    manifest = json.load(r)

layers = manifest.get("layers", [])
print(f"layers: {len(layers)}")
total = sum(l.get("size", 0) for l in layers)
print(f"total: {total/1024/1024:.1f} MB")
for i, l in enumerate(layers):
    print(f"  [{i+1}] {l.get('size',0)/1024/1024:.1f} MB  {l['digest'][:40]}")

import subprocess, base64, json, os, sys

REPO = "Spring-bulid/KinSU"
ROOT = r"d:\FollKernel\KernelSU"
PARENT_SHA = "d4a3aaeab28da8ebbefe4bfb1d478c2aae8fcaae"  # remote main
BASE_TREE = "8cb52ea7aa306c17613a53ed11e65201bf381b2d"   # remote main tree
LOCAL_HEAD = "de199fc53ffec50c7ed55ca466b3ee29a83d24c9"

def gh_api(*args, input_bytes=None):
    cmd = ["gh", "api"] + list(args)
    r = subprocess.run(cmd, input=input_bytes, capture_output=True)
    if r.returncode != 0:
        sys.stderr.write(f"gh api failed: {r.stderr.decode('utf-8', 'replace')}\n")
        sys.stderr.write(f"stdout: {r.stdout[:500]}\n")
        raise RuntimeError(f"gh api {args[1] if len(args)>1 else args} failed")
    return r.stdout

def gh_api_json(*args, input_bytes=None):
    return json.loads(gh_api(*args, input_bytes=input_bytes).decode("utf-8"))

# File changes: (path, mode, kind)  kind: 'mod' or 'del'
changes = [
    ("manager/app/src/main/res/drawable-hdpi/ic_launcher_foreground.png", "100644", "mod"),
    ("manager/app/src/main/res/drawable-mdpi/ic_launcher_foreground.png", "100644", "mod"),
    ("manager/app/src/main/res/drawable-xhdpi/ic_launcher_foreground.png", "100644", "mod"),
    ("manager/app/src/main/res/drawable-xxhdpi/ic_launcher_foreground.png", "100644", "mod"),
    ("manager/app/src/main/res/drawable-xxxhdpi/ic_launcher_foreground.png", "100644", "mod"),
    ("manager/app/src/main/res/mipmap-hdpi/ic_launcher_foreground.png", "100644", "del"),
    ("manager/app/src/main/res/mipmap-mdpi/ic_launcher_foreground.png", "100644", "del"),
    ("manager/app/src/main/res/mipmap-xhdpi/ic_launcher_foreground.png", "100644", "del"),
    ("manager/app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.png", "100644", "del"),
    ("manager/app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png", "100644", "del"),
    ("manager/build.gradle.kts", "100644", "mod"),
]

tree_updates = []
for path, mode, kind in changes:
    if kind == "del":
        tree_updates.append({"path": path, "mode": mode, "type": "blob", "sha": None})
        print(f"  del {path}")
        continue
    full = os.path.join(ROOT, path.replace("/", os.sep))
    with open(full, "rb") as f:
        content = f.read()
    b64 = base64.b64encode(content).decode("ascii")
    # Create blob
    blob_payload = json.dumps({"content": b64, "encoding": "base64"}).encode("utf-8")
    blob = gh_api_json("--method", "POST", f"repos/{REPO}/git/blobs",
                       "-H", "Content-Type: application/json",
                       "--input", "-", input_bytes=blob_payload)
    sha = blob["sha"]
    tree_updates.append({"path": path, "mode": mode, "type": "blob", "sha": sha})
    print(f"  blob {path} -> {sha[:8]} (size={len(content)})")

# Create tree
tree_payload = json.dumps({"base_tree": BASE_TREE, "tree": tree_updates}).encode("utf-8")
tree = gh_api_json("--method", "POST", f"repos/{REPO}/git/trees",
                   "-H", "Content-Type: application/json",
                   "--input", "-", input_bytes=tree_payload)
new_tree_sha = tree["sha"]
print(f"New tree: {new_tree_sha}")

# Create commit
commit_payload = json.dumps({
    "message": "fix: place adaptive foreground in drawable-*/ and fit to safe zone, bump v3.1.11",
    "tree": new_tree_sha,
    "parents": [PARENT_SHA],
    "author": {"name": "Spring-bulid", "email": "Spring-bulid@users.noreply.github.com"},
    "committer": {"name": "Spring-bulid", "email": "Spring-bulid@users.noreply.github.com"},
}).encode("utf-8")
commit = gh_api_json("--method", "POST", f"repos/{REPO}/git/commits",
                     "-H", "Content-Type: application/json",
                     "--input", "-", input_bytes=commit_payload)
new_commit_sha = commit["sha"]
print(f"New commit: {new_commit_sha}")

# Update refs/heads/main (force to be safe)
ref_payload = json.dumps({"sha": new_commit_sha, "force": False}).encode("utf-8")
gh_api("--method", "PATCH", f"repos/{REPO}/git/refs/heads/main",
       "-H", "Content-Type: application/json",
       "--input", "-", input_bytes=ref_payload)
print(f"Updated refs/heads/main -> {new_commit_sha}")

# Create tag object
tag_payload = json.dumps({
    "tag": "v3.1.11",
    "message": "v3.1.11",
    "object": new_commit_sha,
    "type": "commit",
    "tagger": {"name": "Spring-bulid", "email": "Spring-bulid@users.noreply.github.com"},
}).encode("utf-8")
tag = gh_api_json("--method", "POST", f"repos/{REPO}/git/tags",
                  "-H", "Content-Type: application/json",
                  "--input", "-", input_bytes=tag_payload)
tag_sha = tag["sha"]
print(f"Tag object: {tag_sha}")

# Create refs/tags/v3.1.11
ref2_payload = json.dumps({"sha": tag_sha}).encode("utf-8")
gh_api("--method", "POST", f"repos/{REPO}/git/refs",
       "-H", "Content-Type: application/json",
       "--input", "-", input_bytes=ref2_payload)
print(f"Created refs/tags/v3.1.11 -> {tag_sha}")
print("\nPush via API done!")

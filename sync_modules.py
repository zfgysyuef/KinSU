#!/usr/bin/env python3
"""
Sync KernelSU ecosystem modules to KinSU-Modules repository.
Searches GitHub for KernelSU-related module repositories, fetches their latest
release information, and generates a modules.json file compatible with the
KinSU Manager ModuleRepoRepositoryImpl parser.
"""

import json
import os
import re
import sys
import time
import urllib.request
import urllib.error

GITHUB_API = "https://api.github.com"
TOKEN = os.environ.get("GH_TOKEN") or os.environ.get("GITHUB_TOKEN") or ""

SEARCH_QUERIES = [
    "KernelSU module",
    "KernelSU module in:name,description",
    "topic:kernelsu",
    "topic:kernel-su",
    "KernelSU root module",
    "KernelSU Magisk module",
    "KernelSU susfs",
    "KernelSU KPM",
    "KernelSU WebUI",
    "KernelSU font",
    "KernelSU theme",
]

MIN_STARS = 5
MAX_MODULES = 200


def api_get(url, retries=3):
    """Make an authenticated GitHub API GET request with rate-limit handling."""
    for attempt in range(retries):
        try:
            req = urllib.request.Request(url)
            req.add_header("Accept", "application/vnd.github+json")
            req.add_header("User-Agent", "KinSU-Module-Sync")
            if TOKEN:
                req.add_header("Authorization", f"Bearer {TOKEN}")
            with urllib.request.urlopen(req, timeout=30) as resp:
                return json.loads(resp.read().decode("utf-8"))
        except urllib.error.HTTPError as e:
            if e.code == 403 and "rate limit" in e.read().decode("utf-8", "ignore").lower():
                wait = 30 * (attempt + 1)
                print(f"  Rate limited, waiting {wait}s...", file=sys.stderr)
                time.sleep(wait)
                continue
            if e.code == 404:
                return None
            if attempt < retries - 1:
                time.sleep(5 * (attempt + 1))
                continue
            print(f"  API error {e.code} for {url}", file=sys.stderr)
            return None
        except Exception as e:
            if attempt < retries - 1:
                time.sleep(5 * (attempt + 1))
                continue
            print(f"  Error: {e}", file=sys.stderr)
            return None
    return None


def search_repositories(query, per_page=30, page=1):
    """Search GitHub repositories."""
    q = urllib.parse.quote(query)
    url = f"{GITHUB_API}/search/repositories?q={q}&sort=stars&order=desc&per_page={per_page}&page={page}"
    data = api_get(url)
    if data and "items" in data:
        return data["items"]
    return []


def get_latest_release(owner, repo):
    """Get the latest release for a repository."""
    url = f"{GITHUB_API}/repos/{owner}/{repo}/releases/latest"
    return api_get(url)


def extract_version_code(tag_name, assets):
    """Try to extract a version code from tag name or asset names."""
    # Try to find a zip asset and extract version code from its name
    for asset in assets or []:
        name = asset.get("name", "")
        # Look for patterns like v1.0.0, 1.0.0, etc. in asset names
        m = re.search(r'(\d+)', name)
        if m:
            # Try to find a multi-digit version code
            nums = re.findall(r'(\d+)', name)
            if nums:
                # Use the last number group as version code if it looks like one
                try:
                    vc = int(nums[-1])
                    if vc > 0:
                        return vc
                except ValueError:
                    pass

    # Fall back to tag name
    nums = re.findall(r'(\d+)', tag_name or "")
    if nums:
        try:
            return int(nums[-1])
        except ValueError:
            pass
    return 0


def find_module_asset(assets):
    """Find the module zip asset from release assets."""
    if not assets:
        return None
    # Prefer .zip files
    for asset in assets:
        name = asset.get("name", "").lower()
        if name.endswith(".zip") and "module" in name:
            return asset
    for asset in assets:
        name = asset.get("name", "").lower()
        if name.endswith(".zip"):
            return asset
    # Fall back to any file
    for asset in assets:
        name = asset.get("name", "").lower()
        if name.endswith(".apk") or name.endswith(".km"):
            return asset
    return None


def build_module_entry(repo):
    """Build a module entry from a repository."""
    full_name = repo.get("full_name", "")
    name = repo.get("name", "")
    owner = repo.get("owner", {}).get("login", "")
    description = repo.get("description") or ""
    stars = repo.get("stargazers_count", 0)
    updated = repo.get("updated_at", "")
    created = repo.get("created_at", "")
    html_url = repo.get("html_url", "")

    # Get latest release
    release = get_latest_release(owner, name)
    if not release:
        return None

    assets = release.get("assets", [])
    asset = find_module_asset(assets)
    if not asset:
        return None

    tag = release.get("tag_name", "")
    release_name = release.get("name", "") or tag
    release_time = release.get("published_at", "")
    download_url = asset.get("browser_download_url", "")
    version_code = extract_version_code(tag, assets)

    return {
        "moduleId": full_name.replace("/", "_"),
        "moduleName": name,
        "authors": [{"name": owner, "link": f"https://github.com/{owner}"}],
        "summary": description[:200] if description else "",
        "metamodule": False,
        "stargazerCount": stars,
        "updatedAt": updated,
        "createdAt": created,
        "latestRelease": {
            "name": release_name,
            "version": tag,
            "time": release_time,
            "downloadUrl": download_url,
            "versionCode": version_code,
        },
    }


def main():
    print("=== KinSU Module Sync ===", file=sys.stderr)

    seen = set()
    modules = []

    for query in SEARCH_QUERIES:
        print(f"Searching: {query}", file=sys.stderr)
        repos = search_repositories(query, per_page=30)
        for repo in repos:
            full_name = repo.get("full_name", "")
            stars = repo.get("stargazers_count", 0)
            if full_name in seen:
                continue
            if stars < MIN_STARS:
                continue
            seen.add(full_name)
            print(f"  Processing: {full_name} (stars={stars})", file=sys.stderr)
            entry = build_module_entry(repo)
            if entry:
                modules.append(entry)
                print(f"    OK: {entry['moduleName']} -> {entry['latestRelease']['downloadUrl']}", file=sys.stderr)
            else:
                print(f"    SKIP: no release/asset", file=sys.stderr)

            if len(modules) >= MAX_MODULES:
                break
        if len(modules) >= MAX_MODULES:
            break

    # Sort by stars descending
    modules.sort(key=lambda x: x.get("stargazerCount", 0), reverse=True)

    output = json.dumps(modules, indent=2, ensure_ascii=False)
    with open("modules.json", "w", encoding="utf-8") as f:
        f.write(output)

    print(f"\n=== Done: {len(modules)} modules written to modules.json ===", file=sys.stderr)


if __name__ == "__main__":
    import urllib.parse
    main()

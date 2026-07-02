import urllib.request, os

urls = [
    ("https://aka.doubaocdn.com/s/FCyv1wgDSZ", "bohe_gamersky1.jpg"),
    ("https://aka.doubaocdn.com/s/XPbb1wgDSZ", "bohe_gamersky2.jpg"),
]
for url, name in urls:
    req = urllib.request.Request(url, headers={"User-Agent":"Mozilla/5.0"})
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            final = resp.geturl()
            data = resp.read()
            out = os.path.join(r"d:\FollKernel\tmp", name)
            with open(out, "wb") as f:
                f.write(data)
            print(name, "len", len(data), "final", final)
    except Exception as e:
        print(name, "error", e)

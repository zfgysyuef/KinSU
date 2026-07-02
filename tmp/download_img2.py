import urllib.request, os

urls = [
    ("https://aka.doubaocdn.com/s/OUjJ1wgDRa", "bohe_web_raw2.jpg"),
    ("https://aka.doubaocdn.com/s/JL9l1wgDRa", "bohe_web_raw3.jpg"),
    ("https://aka.doubaocdn.com/s/ZRyU1wgDRa", "bohe_web_raw4.jpg"),
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
            print(name, "final", final, "len", len(data))
    except Exception as e:
        print(name, "error", e)

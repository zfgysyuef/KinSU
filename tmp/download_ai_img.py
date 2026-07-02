import urllib.request, os

url = "https://aka.doubaocdn.com/s/BKrx1wgDWz"
req = urllib.request.Request(url, headers={"User-Agent":"Mozilla/5.0"})
try:
    with urllib.request.urlopen(req, timeout=60) as resp:
        final = resp.geturl()
        print("final", final)
        print("content-type", resp.headers.get('Content-Type'))
        data = resp.read()
        out = r"d:\FollKernel\tmp\bohe_ai_icon_v4.jpg"
        with open(out, "wb") as f:
            f.write(data)
        print("saved", out, len(data))
except Exception as e:
    print("error", e)

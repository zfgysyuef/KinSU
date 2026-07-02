import urllib.request, os

url = "https://ts1.tc.mm.bing.net/th/id/OIP-C.EeoKJtE2gUyW9KQMhUEphwAAAA?rs=1&pid=ImgDetMain&o=7&rm=3"
req = urllib.request.Request(url, headers={"User-Agent":"Mozilla/5.0"})
try:
    with urllib.request.urlopen(req, timeout=60) as resp:
        final = resp.geturl()
        print("final", final)
        print("content-type", resp.headers.get('Content-Type'))
        data = resp.read()
        out = r"d:\FollKernel\tmp\user_icon_raw.jpg"
        with open(out, "wb") as f:
            f.write(data)
        print("saved", out, len(data))
except Exception as e:
    print("error", e)

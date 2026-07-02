import urllib.request, urllib.parse

prompt = "a cute anime girl with mint green hair, simple app icon, pastel background"
url = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=" + urllib.parse.quote(prompt) + "&image_size=square"
req = urllib.request.Request(url, method='GET')
try:
    with urllib.request.urlopen(req, timeout=120) as resp:
        print("status", resp.status)
        print("content-type", resp.headers.get('Content-Type'))
        print("cachekey", resp.headers.get('X-Kfc-Cachekey'))
        data = resp.read()
        out = r"d:\FollKernel\tmp\bohe_ai_icon_test2.png"
        with open(out, "wb") as f:
            f.write(data)
        print("saved", out, len(data))
except Exception as e:
    print("error", e)

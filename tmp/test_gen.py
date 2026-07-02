import urllib.request, urllib.parse, os, sys

prompt = "anime style app icon of Bohr (薄荷) from Neverness to Everness game, cute mint green haired girl, sleepy expression, cream beige background, soft pastel colors, simple flat illustration, centered head portrait, circle icon style"
url = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=" + urllib.parse.quote(prompt) + "&image_size=square"
req = urllib.request.Request(url, method='GET')
try:
    with urllib.request.urlopen(req, timeout=120) as resp:
        print("status", resp.status)
        print("headers", dict(resp.headers))
        data = resp.read()
        out = r"d:\FollKernel\tmp\bohe_ai_icon_test.png"
        with open(out, "wb") as f:
            f.write(data)
        print("saved", out, len(data))
except Exception as e:
    print("error", e)

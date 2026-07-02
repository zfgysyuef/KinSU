import urllib.request, os

base = "OIP-C.EeoKJtE2gUyW9KQMhUEphwAAAA"
variants = [
    "https://ts1.tc.mm.bing.net/th/id/" + base,
    "https://tse1.mm.bing.net/th?id=" + base.replace("-C.", "."),
    "https://tse1.mm.bing.net/th?id=OIP.EeoKJtE2gUyW9KQMhUEphwHaHa&pid=Api",
    "https://tse1.mm.bing.net/th?id=OIP.EeoKJtE2gUyW9KQMhUEphwHaIU&pid=Api",
    "https://tse1.mm.bing.net/th?id=OIP.EeoKJtE2gUyW9KQMhUEphwHaHx&pid=Api",
]
for url in variants:
    req = urllib.request.Request(url, headers={"User-Agent":"Mozilla/5.0"})
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            final = resp.geturl()
            ct = resp.headers.get('Content-Type')
            data = resp.read()
            print(url, "->", final, ct, len(data))
    except Exception as e:
        print(url, "error", e)

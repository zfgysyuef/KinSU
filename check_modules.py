import json
with open(r"C:\Users\hanha\AppData\Local\Temp\modules.json", "r", encoding="utf-8") as f:
    data = json.load(f)
print(f"MODULES_COUNT:{len(data)}")
for m in data[:10]:
    name = m.get("moduleName", "")
    stars = m.get("stargazerCount", 0)
    print(f"  - {name} | Stars:{stars}")

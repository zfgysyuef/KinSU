with open('d:\\KinSU\\kinsu-site\\index.html', 'r', encoding='utf-8') as f:
    content = f.read()

# Find the specific broken pre() call and merge lines
old_broken = """pre('<!-- webroot/index.html -->\\n<!DOCTYPE html>\\n<html>\\n<head>\\n    <meta charset="utf-8">\\n    <title>\\u6211\\u7684\\u6a21\\u5757</title>\\n    <style>\\n        body { font-family: sans-serif; padding: 20px; }\\n        button { padding: 10px 20px; margin: 5px; }\\n    
</style>\\n</head>\\n<body>\\n    <h1>\\u6a21\\u5757\\u63a7\\u5236\\u53f0</h1>\\n    <button onclick="toggle()">\\u5f00\\u542f/\\u5173\\u95ed</button>\\n    <div id="status">\\u72b6\\u6001\\uff1a\\u672a\\u77e5</div>\\n    <script>\\n        function toggle() {\\n            KinSU.exec("toggle_feature", (code, out) => {\\n                document.getElementById("status").textContent = out;\\n            });\\n        }\\n    <\\\\/script>\\n</body>\\n</html>')+"""

new_fixed = """pre('<!-- webroot/index.html -->\\n<!DOCTYPE html>\\n<html>\\n<head>\\n    <meta charset="utf-8">\\n    <title>\\u6211\\u7684\\u6a21\\u5757</title>\\n    <style>\\n        body { font-family: sans-serif; padding: 20px; }\\n        button { padding: 10px 20px; margin: 5px; }\\n    </style>\\n</head>\\n<body>\\n    <h1>\\u6a21\\u5757\\u63a7\\u5236\\u53f0</h1>\\n    <button onclick="toggle()">\\u5f00\\u542f/\\u5173\\u95ed</button>\\n    <div id="status">\\u72b6\\u6001\\uff1a\\u672a\\u77e5</div>\\n    <script>\\n        function toggle() {\\n            KinSU.exec("toggle_feature", (code, out) => {\\n                document.getElementById("status").textContent = out;\\n            });\\n        }\\n    <\\\\/script>\\n</body>\\n</html>')+"""

if old_broken in content:
    content = content.replace(old_broken, new_fixed)
    print('Fixed multiline pre()')
else:
    print('Pattern not found, trying alternate')
    # Try reading lines and merging
    lines = content.split('\n')
    for i, line in enumerate(lines):
        if '<!-- webroot/index.html -->' in line and line.startswith("pre('"):
            # Merge with next line if next line starts with </style>
            if i+1 < len(lines) and lines[i+1].strip().startswith('</style>'):
                merged = line.rstrip() + lines[i+1].lstrip()
                lines[i] = merged
                lines.pop(i+1)
                content = '\n'.join(lines)
                print('Fixed by line merge')
                break

with open('d:\\KinSU\\kinsu-site\\index.html', 'w', encoding='utf-8') as f:
    f.write(content)
print('Done. Length:', len(content))

with open('d:\\KinSU\\kinsu-site\\index.html', 'r', encoding='utf-8') as f:
    content = f.read()

# Remove the wrongly inserted CSS from JS string
bad_css = """.features::before,.comparison::before{content:'';position:absolute;right:-100px;top:50%;transform:translateY(-50%);width:400px;height:400px;background:url('https://acg.yaohud.cn/dm/adaptive.php') center/cover no-repeat;border-radius:50%;opacity:.08;filter:blur(2px);pointer-events:none;z-index:0}
.getting-started::before,.contributors::before{content:'';position:absolute;left:-80px;top:20%;width:350px;height:350px;background:url('https://acg.yaohud.cn/dm/adaptive.php') center/cover no-repeat;border-radius:50%;opacity:.08;filter:blur(2px);pointer-events:none;z-index:0}
.features-inner,.getting-started-inner,.comparison-inner,.contributors-inner{position:relative;z-index:1}
.hero-bg{background:url('https://api.btstu.cn/sjbz/api.php?lx=dongman') center/cover no-repeat}
"""
content = content.replace(bad_css, '')

# Insert CSS before the first </style>
section_bg_css = """
.features::before,.comparison::before{content:"";position:absolute;right:-100px;top:50%;transform:translateY(-50%);width:400px;height:400px;background:url('https://acg.yaohud.cn/dm/adaptive.php') center/cover no-repeat;border-radius:50%;opacity:.08;filter:blur(2px);pointer-events:none;z-index:0}
.getting-started::before,.contributors::before{content:"";position:absolute;left:-80px;top:20%;width:350px;height:350px;background:url('https://acg.yaohud.cn/dm/adaptive.php') center/cover no-repeat;border-radius:50%;opacity:.08;filter:blur(2px);pointer-events:none;z-index:0}
.features-inner,.getting-started-inner,.comparison-inner,.contributors-inner{position:relative;z-index:1}
.hero-bg{background:url('https://api.btstu.cn/sjbz/api.php?lx=dongman') center/cover no-repeat}
"""

first_style_end = content.find('</style>')
if first_style_end >= 0:
    content = content[:first_style_end] + section_bg_css + content[first_style_end:]

with open('d:\\KinSU\\kinsu-site\\index.html', 'w', encoding='utf-8') as f:
    f.write(content)
print('Fixed. Length:', len(content))

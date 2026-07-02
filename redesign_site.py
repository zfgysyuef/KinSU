import re

# Read current file
with open('d:\\KinSU\\kinsu-site\\index.html', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update dates: 2024-12-15 -> 2026-06-24
content = content.replace('2024-12-15', '2026-06-24')
content = content.replace('2024 KinSU', '2026 KinSU')

# 2. Replace emoji icons with SVG/icons (feature cards)
# We need to replace the features array
old_features = """var features=[
  {icon:'\\u2694\\ufe0f',name:'LKM \\u57fa\\u51c6\\u6a21\\u5f0f',desc:'\\u4ee5\\u53ef\\u52a0\\u8f7d\\u5185\\u6838\\u6a21\\u5757\\u4e3a\\u5192\\u9669\\u7684\\u57fa\\u77f3\\uff0c\\u65e0\\u9700\\u91cd\\u7f16\\u8bd1\\u5185\\u6838\\u5373\\u53ef\\u83b7\\u5f97 Root \\u529b\\u91cf'},
  {icon:'\\ud83c\\udfb2',name:'GKI \\u5f02\\u4e16\\u754c\\u9002\\u914d',desc:'\\u5b8c\\u7f8e\\u9002\\u914d Android GKI \\u8bbe\\u5907\\u7684\\u5185\\u6838\\u7ea7 Root'},
  {icon:'\\ud83d\\udcd3',name:'\\u6a21\\u5757\\u9b54\\u6cd5\\u4e66',desc:'\\u5f3a\\u5927\\u7684\\u6a21\\u5757\\u7ba1\\u7406\\u7cfb\\u7edf\\uff0c\\u652f\\u6301 WebUI \\u53ef\\u89c6\\u5316\\u64cd\\u4f5c'},
  {icon:'\\ud83d\\udee1\\ufe0f',name:'\\u8d85\\u7ea7\\u7528\\u6237\\u6743\\u9650',desc:'\\u7cbe\\u7ec6\\u7684\\u5e94\\u7528\\u914d\\u7f6e\\u6587\\u4ef6\\u7cfb\\u7edf\\uff0c\\u4e3a\\u6bcf\\u4e00\\u4f4d\\u5192\\u9669\\u8005\\u91cf\\u8eab\\u5b9a\\u5236 Root \\u6743\\u9650'},
  {icon:'\\ud83c\\udfa8',name:'\\u4e3b\\u9898\\u5e7b\\u5316\\u672f',desc:'\\u52a8\\u6001\\u53d6\\u8272\\u4e3b\\u9898\\u5f15\\u64ce\\uff0c\\u652f\\u6301 iPhone \\u98ce\\u683c\\u5b57\\u4f53\\u5e7b\\u5316'},
  {icon:'\\ud83c\\udf0d',name:'\\u591a\\u8bed\\u8a00\\u7ed3\\u754c',desc:'\\u8de8\\u8d8a\\u8bed\\u8a00\\u4e4b\\u58c1\\uff0c\\u652f\\u6301\\u4e2d\\u6587\\u3001\\u82f1\\u8bed\\u3001\\u4fc4\\u8bed\\u3001\\u65e5\\u8bed\\u7b49\\u591a\\u79cd\\u8bed\\u8a00'}
];"""

new_features = """var features=[
  {icon:'<svg viewBox=\"0 0 24 24\" width=\"24\" height=\"24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><path d=\"M12 2L2 7l10 5 10-5-10-5z\"/><path d=\"M2 17l10 5 10-5\"/><path d=\"M2 12l10 5 10-5\"/></svg>',name:'LKM \\u57fa\\u51c6\\u6a21\\u5f0f',desc:'\\u57fa\\u4e8e\\u53ef\\u52a0\\u8f7d\\u5185\\u6838\\u6a21\\u5757\\u6280\\u672f\\uff0c\\u65e0\\u9700\\u91cd\\u65b0\\u7f16\\u8bd1\\u5185\\u6838\\u5373\\u53ef\\u83b7\\u53d6 Root \\u6743\\u9650'},
  {icon:'<svg viewBox=\"0 0 24 24\" width=\"24\" height=\"24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><rect x=\"4\" y=\"4\" width=\"16\" height=\"16\" rx=\"2\"/><path d=\"M4 12h16M12 4v16\"/></svg>',name:'GKI \\u8bbe\\u5907\\u9002\\u914d',desc:'\\u9002\\u914d Android GKI \\u8bbe\\u5907\\u7684\\u901a\\u7528\\u5185\\u6838\\u955c\\u50cf\\uff0c\\u5237\\u5165\\u66f4\\u7b80\\u4fbf'},
  {icon:'<svg viewBox=\"0 0 24 24\" width=\"24\" height=\"24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><path d=\"M4 19.5A2.5 2.5 0 016.5 17H20\"/><path d=\"M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z\"/></svg>',name:'\\u6a21\\u5757\\u7cfb\\u7edf',desc:'\\u5b8c\\u5584\\u7684\\u6a21\\u5757\\u7ba1\\u7406\\u4e0e\\u5b89\\u88c5\\u673a\\u5236\\uff0c\\u652f\\u6301 WebUI \\u53ef\\u89c6\\u5316\\u914d\\u7f6e'},
  {icon:'<svg viewBox=\"0 0 24 24\" width=\"24\" height=\"24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><path d=\"M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z\"/></svg>',name:'\\u8d85\\u7ea7\\u7528\\u6237\\u7ba1\\u7406',desc:'\\u7cbe\\u7ec6\\u7684\\u5e94\\u7528\\u7ea7 Root \\u6743\\u9650\\u63a7\\u5236\\uff0c\\u4fdd\\u62a4\\u7cfb\\u7edf\\u5b89\\u5168'},
  {icon:'<svg viewBox=\"0 0 24 24\" width=\"24\" height=\"24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><circle cx=\"12\" cy=\"12\" r=\"10\"/><path d=\"M12 2a14.5 14.5 0 000 20 14.5 14.5 0 000-20\"/><path d=\"M2 12h20\"/></svg>',name:'\\u4e3b\\u9898\\u4e0e\\u5b57\\u4f53',desc:'\\u52a8\\u6001\\u53d6\\u8272\\u4e3b\\u9898\\u5f15\\u64ce\\uff0c\\u652f\\u6301 iPhone \\u98ce\\u683c\\u5b57\\u4f53\\u9009\\u62e9'},
  {icon:'<svg viewBox=\"0 0 24 24\" width=\"24\" height=\"24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><path d=\"M5 12h14M12 5l7 7-7 7\"/></svg>',name:'\\u591a\\u8bed\\u8a00\\u652f\\u6301',desc:'\\u652f\\u6301\\u4e2d\\u6587\\u3001\\u82f1\\u8bed\\u3001\\u4fc4\\u8bed\\u3001\\u65e5\\u8bed\\u7b49\\u591a\\u79cd\\u8bed\\u8a00\\u754c\\u9762'}
];"""

content = content.replace(old_features, new_features)

# 3. Update text translations to be more professional
old_T = """var T={
  navHome:'\\u9996\\u9875',navDocs:'\\u6587\\u6863',
  heroTitle:'KinSU',
  heroSubtitle:'\\u7a7f\\u8d8a\\u5185\\u6838\\u7684\\u5f02\\u4e16\\u754c Root \\u5192\\u9669',
  heroDesc:'\\u4ee5 LKM \\u4e3a\\u5251\\uff0c\\u65a9\\u65ad\\u6743\\u9650\\u7684\\u67b7\\u9501',
  btnAdventure:'\\u5f00\\u542f\\u5192\\u9669',btnGuide:'\\u9605\\u8bfb\\u653b\\u7565',
  featuresTitle:'\\u5192\\u9669\\u8005\\u7684\\u529b\\u91cf',
  featuresDesc:'\\u638c\\u63e1\\u8fd9\\u4e9b\\u6838\\u5fc3\\u80fd\\u529b\\uff0c\\u5f00\\u542f\\u4f60\\u7684\\u5185\\u6838\\u4e4b\\u65c5',
  gsTitle:'\\u5f00\\u59cb\\u5192\\u9669',gsDesc:'\\u53ea\\u9700\\u56db\\u6b65\\uff0c\\u5373\\u53ef\\u8e0f\\u4e0a\\u5185\\u6838\\u4e4b\\u65c5',
  compTitle:'\\u5192\\u9669\\u8005\\u5bf9\\u6bd4',compDesc:'\\u4e0e\\u5176\\u4ed6 Root \\u65b9\\u6848\\u7684\\u5168\\u9762\\u5bf9\\u6bd4',
  contribTitle:'\\u5192\\u9669\\u8005\\u540d\\u518c',contribDesc:'\\u611f\\u8c22\\u6bcf\\u4e00\\u4f4d\\u4e3a KinSU \\u8d21\\u732e\\u529b\\u91cf\\u7684\\u5192\\u9669\\u8005',
  footer:'\\u00a9 2026 KinSU \\u9879\\u76ee\\u7ec4 \\u00b7 \\u4ee5\\u5f00\\u6e90\\u4e4b\\u540d\\uff0c\\u884c\\u81ea\\u7531\\u4e4b\\u5f92',
  tocLabel:'\\u76ee\\u5f55',sidebarTitle:'KinSU \\u6587\\u6863',
  updatedLabel:'\\u66f4\\u65b0\\u4e8e',readingTime:'\\u9884\\u8ba1\\u9605\\u8bfb',minute:'\\u5206\\u949f'
};"""

new_T = """var T={
  navHome:'\\u9996\\u9875',navDocs:'\\u6587\\u6863',
  heroTitle:'KinSU',
  heroSubtitle:'\\u57fa\\u4e8e KernelSU \\u7684 Android \\u5185\\u6838\\u7ea7 Root \\u7ba1\\u7406\\u5668',
  heroDesc:'\\u91c7\\u7528 LKM \\u6a21\\u5f0f\\uff0c\\u517c\\u5bb9 GKI \\u8bbe\\u5907\\uff0c\\u63d0\\u4f9b\\u5b89\\u5168\\u3001\\u7075\\u6d3b\\u7684\\u6743\\u9650\\u7ba1\\u7406\\u65b9\\u6848',
  btnAdventure:'\\u4e0b\\u8f7d\\u4f53\\u9a8c',btnGuide:'\\u67e5\\u770b\\u6587\\u6863',
  featuresTitle:'\\u6838\\u5fc3\\u80fd\\u529b',
  featuresDesc:'\\u4e13\\u4e3a\\u8ffd\\u6c42\\u81ea\\u7531\\u63a7\\u5236\\u7684\\u7528\\u6237\\u8bbe\\u8ba1\\u7684\\u5173\\u952e\\u529f\\u80fd',
  gsTitle:'\\u5feb\\u901f\\u5f00\\u59cb',gsDesc:'\\u56db\\u4e2a\\u7b80\\u5355\\u6b65\\u9aa4\\uff0c\\u5f00\\u542f\\u4f60\\u7684 Root \\u4e4b\\u65c5',
  compTitle:'\\u65b9\\u6848\\u5bf9\\u6bd4',compDesc:'\\u4e0e\\u5176\\u4ed6\\u6d41\\u884c Root \\u65b9\\u6848\\u7684\\u529f\\u80fd\\u5bf9\\u6bd4',
  contribTitle:'\\u8d21\\u732e\\u8005',contribDesc:'\\u611f\\u8c22\\u6bcf\\u4e00\\u4f4d\\u4e3a KinSU \\u8d21\\u732e\\u4ee3\\u7801\\u548c\\u6587\\u6863\\u7684\\u5f00\\u53d1\\u8005',
  footer:'\\u00a9 2026 KinSU \\u9879\\u76ee\\u7ec4 \\u00b7 \\u57fa\\u4e8e KernelSU \\u5f00\\u6e90\\u9879\\u76ee',
  tocLabel:'\\u76ee\\u5f55',sidebarTitle:'KinSU \\u6587\\u6863',
  updatedLabel:'\\u66f4\\u65b0\\u4e8e',readingTime:'\\u9884\\u8ba1\\u9605\\u8bfb',minute:'\\u5206\\u949f'
};"""

content = content.replace(old_T, new_T)

# 4. Update doc tree icons (remove emoji)
old_doctree = """var docTree=[
  {id:'quickstart',icon:'\\ud83d\\udcd6',title:'\\u5feb\\u901f\\u5f00\\u59cb',children:[
    {id:'install',title:'\\u5b89\\u88c5\\u6307\\u5357'},
    {id:'flash-kernel',title:'\\u5237\\u5165\\u5185\\u6838'},
    {id:'first-setup',title:'\\u9996\\u6b21\\u914d\\u7f6e'}
  ]},
  {id:'core',icon:'\\ud83d\\udee1\\ufe0f',title:'\\u6838\\u5fc3\\u529f\\u80fd',children:[
    {id:'lkm-mode',title:'LKM \\u6a21\\u5f0f\\u8be6\\u89e3'},
    {id:'gki-adapt',title:'GKI \\u8bbe\\u5907\\u9002\\u914d'},
    {id:'superuser',title:'\\u8d85\\u7ea7\\u7528\\u6237\\u7ba1\\u7406'},
    {id:'module-system',title:'\\u6a21\\u5757\\u7cfb\\u7edf'}
  ]},
  {id:'advanced',icon:'\\u26a1',title:'\\u9ad8\\u7ea7\\u529f\\u80fd',children:[
    {id:'susfs',title:'SuSFS \\u9690\\u8eab\\u672f'},
    {id:'kpm',title:'KPM \\u5185\\u6838\\u8865\\u4e01'},
    {id:'theme',title:'\\u4e3b\\u9898\\u5e7b\\u5316\\u672f'}
  ]},
  {id:'security',icon:'\\ud83d\\udd12',title:'\\u5b89\\u5168\\u52a0\\u56fa',children:[
    {id:'security-fixes',title:'\\u5b89\\u5168\\u4fee\\u590d\\u6e05\\u5355'},
    {id:'cmd-injection',title:'\\u547d\\u4ee4\\u6ce8\\u5165\\u9632\\u62a4'},
    {id:'path-traversal',title:'\\u8def\\u5f84\\u7a7f\\u8d8a\\u9632\\u62a4'}
  ]},
  {id:'dev',icon:'\\ud83d\\udcdd',title:'\\u5f00\\u53d1\\u6307\\u5357',children:[
    {id:'module-dev',title:'\\u6a21\\u5757\\u5f00\\u53d1'},
    {id:'webui-dev',title:'WebUI \\u5f00\\u53d1'},
    {id:'contribute',title:'\\u8d21\\u732e\\u4ee3\\u7801'}
  ]}
];"""

new_doctree = """var docTree=[
  {id:'quickstart',icon:'',title:'\\u5feb\\u901f\\u5f00\\u59cb',children:[
    {id:'install',title:'\\u5b89\\u88c5\\u6307\\u5357'},
    {id:'flash-kernel',title:'\\u5237\\u5165\\u5185\\u6838'},
    {id:'first-setup',title:'\\u9996\\u6b21\\u914d\\u7f6e'}
  ]},
  {id:'core',icon:'',title:'\\u6838\\u5fc3\\u529f\\u80fd',children:[
    {id:'lkm-mode',title:'LKM \\u6a21\\u5f0f\\u8be6\\u89e3'},
    {id:'gki-adapt',title:'GKI \\u8bbe\\u5907\\u9002\\u914d'},
    {id:'superuser',title:'\\u8d85\\u7ea7\\u7528\\u6237\\u7ba1\\u7406'},
    {id:'module-system',title:'\\u6a21\\u5757\\u7cfb\\u7edf'}
  ]},
  {id:'advanced',icon:'',title:'\\u9ad8\\u7ea7\\u529f\\u80fd',children:[
    {id:'susfs',title:'SuSFS \\u9690\\u8eab\\u672f'},
    {id:'kpm',title:'KPM \\u5185\\u6838\\u8865\\u4e01'},
    {id:'theme',title:'\\u4e3b\\u9898\\u4e0e\\u5b57\\u4f53'}
  ]},
  {id:'security',icon:'',title:'\\u5b89\\u5168\\u52a0\\u56fa',children:[
    {id:'security-fixes',title:'\\u5b89\\u5168\\u4fee\\u590d\\u6e05\\u5355'},
    {id:'cmd-injection',title:'\\u547d\\u4ee4\\u6ce8\\u5165\\u9632\\u62a4'},
    {id:'path-traversal',title:'\\u8def\\u5f84\\u7a7f\\u8d8a\\u9632\\u62a4'}
  ]},
  {id:'dev',icon:'',title:'\\u5f00\\u53d1\\u6307\\u5357',children:[
    {id:'module-dev',title:'\\u6a21\\u5757\\u5f00\\u53d1'},
    {id:'webui-dev',title:'WebUI \\u5f00\\u53d1'},
    {id:'contribute',title:'\\u8d21\\u732e\\u4ee3\\u7801'}
  ]}
];"""

content = content.replace(old_doctree, new_doctree)

# 5. Remove emoji from callout icons
content = content.replace("var icon=kind==='warning'?'\\u26a0\\ufe0f':kind==='tip'?'\\ud83d\\udca1':'\\u2139\\ufe0f';", "var icon=kind==='warning'?'!':kind==='tip'?'i':'i';")

# 6. Replace check/cross marks with text
content = content.replace("ct+='<td class=\"check\">\\u2713</td>';", "ct+='<td class=\"check\">\\u662f</td>';")
content = content.replace("ct+='<td class=\"cross\">\\u2717</td>';", "ct+='<td class=\"cross\">\\u5426</td>';")

# 7. Update CSS variables and styles
old_css_vars = """:root{--primary:#3370ff;--primary-light:#e8f0ff;--primary-dark:#245bdb;--bg:#fff;--bg-gray:#f5f6f7;--bg-dark:#1a1a2e;--text-primary:#1f2329;--text-secondary:#3b3f46;--text-tertiary:#8f959e;--border:#e4e7ed;--border-light:#eff0f1;--code-bg:#f2f4f7;--code-dark-bg:#1e1e2e;--code-dark-text:#cdd6f4;--sidebar-w:260px;--toc-w:200px;--navbar-h:56px;--shadow-sm:0 1px 2px rgba(0,0,0,.06);--shadow-md:0 4px 12px rgba(0,0,0,.08);--shadow-lg:0 8px 24px rgba(0,0,0,.12);--radius-sm:6px;--radius-md:8px;--radius-lg:12px;--tr:.25s cubic-bezier(.4,0,.2,1)}"""

new_css_vars = """:root{--primary:#5b7cff;--primary-light:#eef1ff;--primary-dark:#4259d8;--bg:#fafbff;--bg-gray:#f0f3fa;--bg-dark:#1a1d2e;--text-primary:#1a1f36;--text-secondary:#4b5163;--text-tertiary:#7d839c;--border:#e1e6f0;--border-light:#eef1f8;--code-bg:#f5f7fb;--code-text:#2d3348;--code-dark-bg:#f5f7fb;--code-dark-text:#2d3348;--sidebar-w:260px;--toc-w:200px;--navbar-h:56px;--shadow-sm:0 2px 4px rgba(91,124,255,.06);--shadow-md:0 8px 24px rgba(91,124,255,.1);--shadow-lg:0 16px 48px rgba(91,124,255,.14);--radius-sm:8px;--radius-md:14px;--radius-lg:20px;--tr:.25s cubic-bezier(.4,0,.2,1)}"""

content = content.replace(old_css_vars, new_css_vars)

# 8. Update pre code colors in CSS
content = content.replace(".doc-body code{background:#f2f4f7;padding:2px 6px;border-radius:4px;font-size:14px;font-family:'Fira Code',Consolas,monospace;color:#d63384}", ".doc-body code{background:#eef1f8;padding:2px 6px;border-radius:4px;font-size:14px;font-family:'Fira Code',Consolas,monospace;color:#5b7cff}")
content = content.replace(".doc-body pre{background:#1e1e2e;color:#cdd6f4;border-radius:8px;padding:16px;margin:12px 0 16px;overflow-x:auto;font-size:14px;line-height:1.6;font-family:'Fira Code',Consolas,monospace}", ".doc-body pre{background:#f5f7fb;color:#2d3348;border:1px solid #e1e6f0;border-radius:12px;padding:18px;margin:14px 0 18px;overflow-x:auto;font-size:14px;line-height:1.65;font-family:'Fira Code',Consolas,monospace;box-shadow:inset 0 1px 2px rgba(0,0,0,.03)}")

# 9. Update callout styles to remove emojis and use cleaner colors
content = content.replace(".callout-icon{font-size:18px;flex-shrink:0;margin-top:1px}", ".callout-icon{font-size:16px;font-weight:700;flex-shrink:0;margin-top:1px;width:22px;height:22px;border-radius:50%;display:flex;align-items:center;justify-content:center}")
content = content.replace(".callout-info{background:#e8f0ff;border:1px solid #b8d0ff;color:#1a56db}", ".callout-info{background:#f0f4ff;border:1px solid #c8d4ff;color:#4259d8}")
content = content.replace(".callout-warning{background:#fff7e6;border:1px solid #ffe0a0;color:#ad6800}", ".callout-warning{background:#fff8f0;border:1px solid #ffd9b3;color:#a65c00}")
content = content.replace(".callout-tip{background:#e6f9e6;border:1px solid #a8e6a8;color:#1a7a1a}", ".callout-tip{background:#f0faf0;border:1px solid #b8e6b8;color:#1a7a1a}")

# 10. Add anime background to sections via CSS - update body and section styles
content = content.replace(".features{padding:80px 24px;max-width:1200px;margin:0 auto}", ".features{padding:100px 24px;max-width:1200px;margin:0 auto;position:relative}")
content = content.replace(".getting-started{padding:80px 24px;background:var(--bg-gray)}", ".getting-started{padding:100px 24px;background:linear-gradient(135deg,#f0f3fa 0%,#f8faff 50%,#f0f3fa 100%);position:relative;overflow:hidden}")
content = content.replace(".comparison{padding:80px 24px;max-width:960px;margin:0 auto}", ".comparison{padding:100px 24px;max-width:960px;margin:0 auto;position:relative}")
content = content.replace(".contributors{padding:80px 24px;background:var(--bg-gray)}", ".contributors{padding:100px 24px;background:linear-gradient(180deg,#f8faff 0%,#f0f3fa 100%);position:relative;overflow:hidden}")

# 11. Add section background images via CSS pseudo elements (insert before closing </style>)
section_bg_css = """
.features::before,.comparison::before{content:'';position:absolute;right:-100px;top:50%;transform:translateY(-50%);width:400px;height:400px;background:url('https://acg.yaohud.cn/dm/adaptive.php') center/cover no-repeat;border-radius:50%;opacity:.08;filter:blur(2px);pointer-events:none;z-index:0}
.getting-started::before,.contributors::before{content:'';position:absolute;left:-80px;top:20%;width:350px;height:350px;background:url('https://acg.yaohud.cn/dm/adaptive.php') center/cover no-repeat;border-radius:50%;opacity:.08;filter:blur(2px);pointer-events:none;z-index:0}
.features-inner,.getting-started-inner,.comparison-inner,.contributors-inner{position:relative;z-index:1}
.hero-bg{background:url('https://api.btstu.cn/sjbz/api.php?lx=dongman') center/cover no-repeat}
"""

# Insert before </style>
content = content.replace("</style>", section_bg_css + "</style>")

# 12. Update feature cards style
content = content.replace(".feature-card{padding:28px;border-radius:var(--radius-lg);background:var(--bg);border:1px solid var(--border-light);transition:all var(--tr)}", ".feature-card{padding:32px;border-radius:var(--radius-lg);background:rgba(255,255,255,.9);border:1px solid var(--border-light);transition:all var(--tr);backdrop-filter:blur(8px)}")
content = content.replace(".feature-icon{width:44px;height:44px;border-radius:var(--radius-md);background:var(--primary-light);display:flex;align-items:center;justify-content:center;font-size:22px;margin-bottom:16px}", ".feature-icon{width:48px;height:48px;border-radius:var(--radius-md);background:linear-gradient(135deg,var(--primary-light) 0%,#fff 100%);display:flex;align-items:center;justify-content:center;color:var(--primary);margin-bottom:18px;box-shadow:0 4px 12px rgba(91,124,255,.12)}")

# 13. Update section titles
content = content.replace(".section-title{font-size:32px;font-weight:700;text-align:center;margin-bottom:12px;color:var(--text-primary)}", ".section-title{font-size:36px;font-weight:700;text-align:center;margin-bottom:14px;color:var(--text-primary);letter-spacing:-.5px}")
content = content.replace(".section-desc{font-size:16px;color:var(--text-tertiary);text-align:center;margin-bottom:48px}", ".section-desc{font-size:17px;color:var(--text-tertiary);text-align:center;margin-bottom:56px;max-width:600px;margin-left:auto;margin-right:auto}")

# 14. Update hero gradient to be softer
content = content.replace(".hero-bg::after{content:'';position:absolute;inset:0;background:linear-gradient(135deg,rgba(26,26,46,.85),rgba(51,112,255,.4))}", ".hero-bg::after{content:'';position:absolute;inset:0;background:linear-gradient(135deg,rgba(26,29,46,.82) 0%,rgba(66,89,216,.35) 50%,rgba(91,124,255,.25) 100%)}")

# 15. Update comparison table check/cross colors
content = content.replace(".check{color:#52c41a;font-weight:700}", ".check{color:#2da44e;font-weight:600}")
content = content.replace(".cross{color:#ff4d4f;font-weight:700}", ".cross{color:#cf4b4b;font-weight:600}")

# 16. Update buttons style
content = content.replace(".btn-primary{padding:12px 32px;border-radius:var(--radius-md);background:var(--primary);color:#fff;font-size:15px;font-weight:600;transition:all var(--tr);box-shadow:0 4px 16px rgba(51,112,255,.4)}", ".btn-primary{padding:14px 36px;border-radius:var(--radius-md);background:linear-gradient(135deg,var(--primary) 0%,var(--primary-dark) 100%);color:#fff;font-size:15px;font-weight:600;transition:all var(--tr);box-shadow:0 6px 20px rgba(91,124,255,.35)}")
content = content.replace(".btn-primary:hover{background:var(--primary-dark);transform:translateY(-1px);box-shadow:0 6px 20px rgba(51,112,255,.5);color:#fff}", ".btn-primary:hover{transform:translateY(-2px);box-shadow:0 8px 28px rgba(91,124,255,.45);color:#fff}")
content = content.replace(".btn-secondary{padding:12px 32px;border-radius:var(--radius-md);background:rgba(255,255,255,.15);color:#fff;font-size:15px;font-weight:600;border:1px solid rgba(255,255,255,.3);transition:all var(--tr);backdrop-filter:blur(4px)}", ".btn-secondary{padding:14px 36px;border-radius:var(--radius-md);background:rgba(255,255,255,.12);color:#fff;font-size:15px;font-weight:600;border:1px solid rgba(255,255,255,.25);transition:all var(--tr);backdrop-filter:blur(8px)}")

# 17. Update docs page styles for cleaner look
content = content.replace(".docs-sidebar{width:var(--sidebar-w);min-width:var(--sidebar-w);background:var(--bg-gray);border-right:1px solid var(--border-light);overflow-y:auto;padding:16px 0;transition:transform var(--tr)}", ".docs-sidebar{width:var(--sidebar-w);min-width:var(--sidebar-w);background:#fff;border-right:1px solid var(--border-light);overflow-y:auto;padding:16px 0;transition:transform var(--tr)}")
content = content.replace(".docs-main{flex:1;overflow-y:auto;position:relative;background:var(--bg)}", ".docs-main{flex:1;overflow-y:auto;position:relative;background:#fafbff}")
content = content.replace(".docs-toc{width:var(--toc-w);min-width:var(--toc-w);padding:32px 16px;overflow-y:auto;border-left:1px solid var(--border-light)}", ".docs-toc{width:var(--toc-w);min-width:var(--toc-w);padding:32px 16px;overflow-y:auto;border-left:1px solid var(--border-light);background:#fff}")

# 18. Remove callout emoji text in existing content (replace specific callout text)
# The callout function will handle new ones, but old unicode in text? Let's check if any remain
# Most were in callout('tip','...') etc, the function now uses ! or i

# 19. Replace contribution IDs with actual GitHub avatars if possible, or keep as is
# Keep current avatars but maybe add a subtle background

# 20. Update comparison table header
content = content.replace("var ct='<table class=\"comparison-table\"><thead><tr><th>\\u529f\\u80fd</th><th>KinSU</th><th>KernelSU</th><th>Magisk</th></tr></thead><tbody>';", "var ct='<table class=\"comparison-table\"><thead><tr><th>\\u529f\\u80fd</th><th>KinSU</th><th>KernelSU</th><th>Magisk</th></tr></thead><tbody>';")

# 21. Update hero title/subtitle sizes
content = content.replace(".hero-title{font-size:clamp(48px,8vw,80px);font-weight:700;color:#fff;letter-spacing:-1px;margin-bottom:12px;text-shadow:0 2px 20px rgba(0,0,0,.3)}", ".hero-title{font-size:clamp(44px,7vw,72px);font-weight:800;color:#fff;letter-spacing:-1.5px;margin-bottom:16px;text-shadow:0 4px 30px rgba(0,0,0,.25)}")
content = content.replace(".hero-subtitle{font-size:clamp(18px,3vw,24px);color:rgba(255,255,255,.9);font-weight:400;margin-bottom:8px}", ".hero-subtitle{font-size:clamp(18px,3vw,26px);color:rgba(255,255,255,.95);font-weight:500;margin-bottom:12px}")
content = content.replace(".hero-desc{font-size:16px;color:rgba(255,255,255,.7);margin-bottom:32px}", ".hero-desc{font-size:17px;color:rgba(255,255,255,.78);margin-bottom:38px;max-width:560px;margin-left:auto;margin-right:auto;line-height:1.7}")

# 22. Update table styles
content = content.replace(".comparison-table th{background:var(--bg-gray);padding:14px 20px;font-size:14px;font-weight:600;text-align:left;border-bottom:2px solid var(--border)}", ".comparison-table th{background:linear-gradient(135deg,var(--primary-light) 0%,#fff 100%);padding:16px 22px;font-size:14px;font-weight:600;text-align:left;border-bottom:2px solid var(--border);color:var(--text-primary)}")
content = content.replace(".comparison-table td{padding:12px 20px;font-size:14px;border-bottom:1px solid var(--border-light)}", ".comparison-table td{padding:14px 22px;font-size:14px;border-bottom:1px solid var(--border-light)}")
content = content.replace(".comparison-table tr:hover td{background:var(--primary-light)}", ".comparison-table tr:hover td{background:rgba(91,124,255,.06)}")

# 23. Update navbar
content = content.replace(".navbar{position:fixed;top:0;left:0;right:0;height:var(--navbar-h);background:rgba(255,255,255,.92);backdrop-filter:blur(12px);border-bottom:1px solid var(--border-light);z-index:1000;display:flex;align-items:center;padding:0 24px;transition:background var(--tr)}", ".navbar{position:fixed;top:0;left:0;right:0;height:var(--navbar-h);background:rgba(255,255,255,.95);backdrop-filter:blur(16px);border-bottom:1px solid var(--border-light);z-index:1000;display:flex;align-items:center;padding:0 28px;transition:background var(--tr);box-shadow:0 1px 3px rgba(0,0,0,.04)}")
content = content.replace(".nav-logo{font-size:20px;font-weight:700;color:var(--primary);display:flex;align-items:center;gap:8px;cursor:pointer}", ".nav-logo{font-size:20px;font-weight:700;color:var(--primary);display:flex;align-items:center;gap:10px;cursor:pointer}")

# 24. Fix dark navbar on docs
content = content.replace(".navbar.dark{background:rgba(26,26,46,.92);border-bottom-color:rgba(255,255,255,.08)}", ".navbar.dark{background:rgba(26,29,46,.95);border-bottom-color:rgba(255,255,255,.08);box-shadow:0 1px 3px rgba(0,0,0,.1)}")

# 25. Update doc title and body
content = content.replace(".doc-title{font-size:28px;font-weight:700;color:var(--text-primary);margin-bottom:8px;padding-bottom:16px;border-bottom:1px solid var(--border-light)}", ".doc-title{font-size:32px;font-weight:700;color:var(--text-primary);margin-bottom:10px;padding-bottom:18px;border-bottom:1px solid var(--border-light);letter-spacing:-.5px}")
content = content.replace(".doc-body h1{font-size:26px;font-weight:600;color:#1f2329;margin-top:32px;margin-bottom:12px;padding-bottom:8px;border-bottom:1px solid var(--border-light)}", ".doc-body h1{font-size:28px;font-weight:700;color:#1a1f36;margin-top:36px;margin-bottom:14px;padding-bottom:10px;border-bottom:1px solid var(--border-light);letter-spacing:-.3px}")
content = content.replace(".doc-body h2{font-size:22px;font-weight:600;color:#1f2329;margin-top:28px;margin-bottom:10px}", ".doc-body h2{font-size:24px;font-weight:600;color:#1a1f36;margin-top:30px;margin-bottom:12px}")
content = content.replace(".doc-body h3{font-size:18px;font-weight:600;color:#1f2329;margin-top:24px;margin-bottom:8px}", ".doc-body h3{font-size:20px;font-weight:600;color:#1a1f36;margin-top:26px;margin-bottom:10px}")

# 26. Update step cards
content = content.replace(".step{text-align:center}", ".step{text-align:center;padding:24px;border-radius:var(--radius-md);background:rgba(255,255,255,.7);border:1px solid var(--border-light);transition:all var(--tr)}")
content = content.replace(".step-num{width:48px;height:48px;border-radius:50%;background:var(--primary);color:#fff;font-size:20px;font-weight:700;display:flex;align-items:center;justify-content:center;margin:0 auto 16px}", ".step-num{width:52px;height:52px;border-radius:50%;background:linear-gradient(135deg,var(--primary) 0%,var(--primary-dark) 100%);color:#fff;font-size:20px;font-weight:700;display:flex;align-items:center;justify-content:center;margin:0 auto 18px;box-shadow:0 6px 16px rgba(91,124,255,.25)}")
content = content.replace(".step-title{font-size:16px;font-weight:600;margin-bottom:8px}", ".step-title{font-size:17px;font-weight:600;margin-bottom:10px;color:var(--text-primary)}")
content = content.replace(".step-desc{font-size:14px;color:var(--text-secondary)}", ".step-desc{font-size:15px;color:var(--text-secondary);line-height:1.6}")

# 27. Update footer
content = content.replace(".footer{padding:32px 24px;text-align:center;border-top:1px solid var(--border-light);color:var(--text-tertiary);font-size:14px}", ".footer{padding:40px 24px;text-align:center;border-top:1px solid var(--border-light);color:var(--text-tertiary);font-size:14px;background:#fff}")

# 28. Update avatar grid style
content = content.replace(".avatar{width:48px;height:48px;border-radius:50%;border:2px solid var(--bg);box-shadow:var(--shadow-sm);transition:transform var(--tr)}", ".avatar{width:44px;height:44px;border-radius:50%;border:2px solid #fff;box-shadow:var(--shadow-sm);transition:transform var(--tr)}")
content = content.replace(".avatar:hover{transform:scale(1.2);z-index:1}", ".avatar:hover{transform:scale(1.15);z-index:1;box-shadow:var(--shadow-md)}")

# Write back
with open('d:\\KinSU\\kinsu-site\\index.html', 'w', encoding='utf-8') as f:
    f.write(content)

print('Redesign applied. Length:', len(content))

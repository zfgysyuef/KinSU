import re

with open('d:\\KinSU\\kinsu-site\\index.html', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Remove all emoji prefixes from callout text
# Pattern: callout('xxx','\uXXXX\uXXXX text...')  -> remove the emoji + space
content = re.sub(r"callout\('(warning|info|tip)','\\u[0-9a-fA-F]{4}\\u[0-9a-fA-F]{4} ", "callout('\\1','", content)
content = re.sub(r"callout\('(warning|info|tip)','\\u[0-9a-fA-F]{4} ", "callout('\\1','", content)

# 2. Update hero section - add more content and highlight styling
# Add highlight CSS
highlight_css = """
.hero-highlight{color:#ffd700;font-weight:600;position:relative;display:inline-block}
.hero-highlight::after{content:'';position:absolute;left:0;right:0;bottom:-2px;height:2px;background:linear-gradient(90deg,transparent,#ffd700,transparent);border-radius:1px}
.hero-stats{display:flex;gap:40px;justify-content:center;margin-top:32px;flex-wrap:wrap}
.hero-stat{text-align:center}
.hero-stat-num{font-size:32px;font-weight:800;color:#fff;text-shadow:0 2px 10px rgba(0,0,0,.3)}
.hero-stat-label{font-size:13px;color:rgba(255,255,255,.7);margin-top:4px}
.hero-badges{display:flex;gap:12px;justify-content:center;margin-top:24px;flex-wrap:wrap}
.hero-badge{padding:6px 16px;border-radius:20px;background:rgba(255,255,255,.12);border:1px solid rgba(255,255,255,.2);font-size:13px;color:rgba(255,255,255,.85);backdrop-filter:blur(8px)}
@keyframes float{0%,100%{transform:translateY(0)}50%{transform:translateY(-8px)}}
@keyframes shimmer{0%{background-position:-200% 0}100%{background-position:200% 0}}
.hero-title-shimmer{background:linear-gradient(90deg,#fff 0%,#a0c4ff 25%,#fff 50%,#a0c4ff 75%,#fff 100%);background-size:200% auto;-webkit-background-clip:text;background-clip:text;-webkit-text-fill-color:transparent;animation:shimmer 4s linear infinite}
@keyframes pulse-glow{0%,100%{box-shadow:0 6px 20px rgba(91,124,255,.35)}50%{box-shadow:0 6px 30px rgba(91,124,255,.55)}}
.btn-primary{animation:pulse-glow 3s ease-in-out infinite}
@keyframes slide-in-left{from{opacity:0;transform:translateX(-30px)}to{opacity:1;transform:translateX(0)}}
@keyframes slide-in-right{from{opacity:0;transform:translateX(30px)}to{opacity:1;transform:translateX(0)}}
@keyframes scale-in{from{opacity:0;transform:scale(.9)}to{opacity:1;transform:scale(1)}}
.feature-card{opacity:0;animation:scale-in .5s ease forwards}
.feature-card:nth-child(1){animation-delay:.1s}
.feature-card:nth-child(2){animation-delay:.2s}
.feature-card:nth-child(3){animation-delay:.3s}
.feature-card:nth-child(4){animation-delay:.4s}
.feature-card:nth-child(5){animation-delay:.5s}
.feature-card:nth-child(6){animation-delay:.6s}
.step{opacity:0;animation:slide-in-left .5s ease forwards}
.step:nth-child(1){animation-delay:.1s}
.step:nth-child(2){animation-delay:.2s}
.step:nth-child(3){animation-delay:.3s}
.step:nth-child(4){animation-delay:.4s}
@keyframes gradient-shift{0%{background-position:0% 50%}50%{background-position:100% 50%}100%{background-position:0% 50%}}
.getting-started{background:linear-gradient(135deg,#f0f3fa,#f8faff,#eef1ff,#f0f3fa);background-size:300% 300%;animation:gradient-shift 12s ease infinite}
.contributors{background:linear-gradient(135deg,#f8faff,#f0f3fa,#eef1ff,#f8faff);background-size:300% 300%;animation:gradient-shift 12s ease infinite}
.section-underline{width:60px;height:3px;background:linear-gradient(90deg,var(--primary),var(--primary-dark));border-radius:2px;margin:0 auto 56px}
"""

# Insert before the features::before CSS
content = content.replace('.features::before,.comparison::before', highlight_css + '\n.features::before,.comparison::before')

# 3. Update hero HTML structure with more content
old_hero = """  <section class="hero" id="hero">
    <div class="hero-bg"></div>
    <div class="hero-content">
      <h1 class="hero-title animate-in" id="heroTitle"></h1>
      <p class="hero-subtitle animate-in animate-delay-1" id="heroSubtitle"></p>
      <p class="hero-desc animate-in animate-delay-2" id="heroDesc"></p>
      <div class="hero-buttons animate-in animate-delay-3" id="heroButtons"></div>
    </div>
  </section>"""

new_hero = """  <section class="hero" id="hero">
    <div class="hero-bg"></div>
    <div class="hero-content">
      <h1 class="hero-title animate-in hero-title-shimmer" id="heroTitle"></h1>
      <p class="hero-subtitle animate-in animate-delay-1" id="heroSubtitle"></p>
      <p class="hero-desc animate-in animate-delay-2" id="heroDesc"></p>
      <div class="hero-badges animate-in animate-delay-2" id="heroBadges"></div>
      <div class="hero-buttons animate-in animate-delay-3" id="heroButtons"></div>
      <div class="hero-stats animate-in animate-delay-3" id="heroStats"></div>
    </div>
  </section>"""

content = content.replace(old_hero, new_hero)

# 4. Add section underline divs
content = content.replace(
    '<div class="features-grid" id="featuresGrid"></div>',
    '<div class="section-underline"></div><div class="features-grid" id="featuresGrid"></div>'
)
content = content.replace(
    '<div class="steps" id="stepsContainer"></div>',
    '<div class="section-underline"></div><div class="steps" id="stepsContainer"></div>'
)
content = content.replace(
    '<div id="comparisonTable"></div>',
    '<div class="section-underline"></div><div id="comparisonTable"></div>'
)
content = content.replace(
    '<div class="avatar-grid" id="avatarGrid"></div>',
    '<div class="section-underline"></div><div class="avatar-grid" id="avatarGrid"></div>'
)

# 5. Update JS text with highlights and more content
old_T = """var T={
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

new_T = """var T={
  navHome:'\\u9996\\u9875',navDocs:'\\u6587\\u6863',
  heroTitle:'KinSU',
  heroSubtitle:'\\u57fa\\u4e8e KernelSU \\u7684 <span class="hero-highlight">\\u5185\\u6838\\u7ea7 Root</span> \\u7ba1\\u7406\\u5668',
  heroDesc:'\\u91c7\\u7528 <span class="hero-highlight">LKM \\u53ef\\u52a0\\u8f7d\\u5185\\u6838\\u6a21\\u5757</span> \\u6280\\u672f\\uff0c\\u5b8c\\u7f8e\\u517c\\u5bb9 GKI \\u8bbe\\u5907\\uff0c\\u5185\\u7f6e <span class="hero-highlight">21+ \\u9879\\u5b89\\u5168\\u4fee\\u590d</span>\\uff0c\\u63d0\\u4f9b\\u5b89\\u5168\\u3001\\u7075\\u6d3b\\u3001\\u9ad8\\u6548\\u7684\\u6743\\u9650\\u7ba1\\u7406\\u65b9\\u6848\\u3002\\u652f\\u6301 SuSFS \\u9690\\u8eab\\u3001KPM \\u5185\\u6838\\u8865\\u4e01\\u3001\\u52a8\\u6001\\u4e3b\\u9898\\u4e0e iPhone \\u5b57\\u4f53\\u3002',
  btnAdventure:'\\u4e0b\\u8f7d\\u4f53\\u9a8c',btnGuide:'\\u67e5\\u770b\\u6587\\u6863',
  featuresTitle:'\\u6838\\u5fc3\\u80fd\\u529b',
  featuresDesc:'\\u4e3a\\u8ffd\\u6c42\\u81ea\\u7531\\u4e0e\\u5b89\\u5168\\u7684\\u7528\\u6237\\u6253\\u9020\\u7684\\u5168\\u9762\\u529f\\u80fd\\u4f53\\u7cfb',
  gsTitle:'\\u5feb\\u901f\\u5f00\\u59cb',gsDesc:'\\u56db\\u4e2a\\u7b80\\u5355\\u6b65\\u9aa4\\uff0c\\u5f00\\u542f\\u4f60\\u7684 Root \\u4e4b\\u65c5',
  compTitle:'\\u65b9\\u6848\\u5bf9\\u6bd4',compDesc:'\\u4e0e\\u5176\\u4ed6\\u6d41\\u884c Root \\u65b9\\u6848\\u7684\\u5168\\u9762\\u529f\\u80fd\\u5bf9\\u6bd4',
  contribTitle:'\\u8d21\\u732e\\u8005',contribDesc:'\\u611f\\u8c22\\u6bcf\\u4e00\\u4f4d\\u4e3a KinSU \\u8d21\\u732e\\u4ee3\\u7801\\u548c\\u6587\\u6863\\u7684\\u5f00\\u53d1\\u8005',
  footer:'\\u00a9 2026 KinSU \\u9879\\u76ee\\u7ec4 \\u00b7 \\u57fa\\u4e8e KernelSU \\u5f00\\u6e90\\u9879\\u76ee \\u00b7 <a href="https://github.com/Spring-bulid/KinSU-Modules" target="_blank" style="color:var(--primary)">\\u6a21\\u5757\\u4ed3\\u5e93</a>',
  tocLabel:'\\u76ee\\u5f55',sidebarTitle:'KinSU \\u6587\\u6863',
  updatedLabel:'\\u66f4\\u65b0\\u4e8e',readingTime:'\\u9884\\u8ba1\\u9605\\u8bfb',minute:'\\u5206\\u949f'
};

var heroBadges=['LKM \\u6a21\\u5f0f','GKI \\u517c\\u5bb9','SuSFS \\u9690\\u8eab','KPM \\u8865\\u4e01','21+ \\u5b89\\u5168\\u4fee\\u590d','\\u591a\\u8bed\\u8a00','iPhone \\u5b57\\u4f53','\\u5f00\\u6e90'];
var heroStats=[
  {num:'21+',label:'\\u5b89\\u5168\\u4fee\\u590d'},
  {num:'4',label:'\\u8bed\\u8a00\\u652f\\u6301'},
  {num:'100%',label:'\\u5f00\\u6e90\\u514d\\u8d39'},
  {num:'GPL-2.0',label:'\\u5f00\\u6e90\\u534f\\u8bae'}
];"""

content = content.replace(old_T, new_T)

# 6. Update initLanding function to populate new elements
old_init = """function initLanding(){
  document.getElementById('heroTitle').textContent=T.heroTitle;
  document.getElementById('heroSubtitle').innerHTML=T.heroSubtitle;
  document.getElementById('heroDesc').innerHTML=T.heroDesc;
  document.getElementById('heroButtons').innerHTML=
    '<a href="https://github.com/Spring-bulid/KinSU/releases/latest" target="_blank" class="btn-primary">'+T.btnAdventure+'</a>'+
    '<button class="btn-secondary" onclick="switchToDocs()">'+T.btnGuide+'</button>';
  document.getElementById('featuresTitle').textContent=T.featuresTitle;
  document.getElementById('featuresDesc').textContent=T.featuresDesc;"""

new_init = """function initLanding(){
  document.getElementById('heroTitle').textContent=T.heroTitle;
  document.getElementById('heroSubtitle').innerHTML=T.heroSubtitle;
  document.getElementById('heroDesc').innerHTML=T.heroDesc;
  document.getElementById('heroBadges').innerHTML=heroBadges.map(function(b){return '<span class="hero-badge">'+b+'</span>';}).join('');
  document.getElementById('heroButtons').innerHTML=
    '<a href="https://github.com/Spring-bulid/KinSU/releases/latest" target="_blank" class="btn-primary">'+T.btnAdventure+'</a>'+
    '<button class="btn-secondary" onclick="switchToDocs()">'+T.btnGuide+'</button>';
  document.getElementById('heroStats').innerHTML=heroStats.map(function(s){return '<div class="hero-stat"><div class="hero-stat-num">'+s.num+'</div><div class="hero-stat-label">'+s.label+'</div></div>';}).join('');
  document.getElementById('featuresTitle').textContent=T.featuresTitle;
  document.getElementById('featuresDesc').textContent=T.featuresDesc;"""

content = content.replace(old_init, new_init)

# 7. Update footer to include module repo link
old_footer_init = "document.getElementById('footer').textContent=T.footer;"
new_footer_init = "document.getElementById('footer').innerHTML=T.footer;"
content = content.replace(old_footer_init, new_footer_init)

# 8. Update hero background to use multiple anime APIs for reliability
content = content.replace(
    ".hero-bg{background:url('https://api.btstu.cn/sjbz/api.php?lx=dongman') center/cover no-repeat}",
    ".hero-bg{background:linear-gradient(135deg,#1a1d2e,#2d3561,#1a1d2e),url('https://acg.yaohud.cn/dm/adaptive.php') center/cover no-repeat;background-blend-mode:overlay}"
)

# 9. Update feature descriptions to be more detailed
old_features = """var features=[
  {icon:'<svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>',name:'LKM \\u57fa\\u51c6\\u6a21\\u5f0f',desc:'\\u57fa\\u4e8e\\u53ef\\u52a0\\u8f7d\\u5185\\u6838\\u6a21\\u5757\\u6280\\u672f\\uff0c\\u65e0\\u9700\\u91cd\\u65b0\\u7f16\\u8bd1\\u5185\\u6838\\u5373\\u53ef\\u83b7\\u53d6 Root \\u6743\\u9650'},
  {icon:'<svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><rect x="4" y="4" width="16" height="16" rx="2"/><path d="M4 12h16M12 4v16"/></svg>',name:'GKI \\u8bbe\\u5907\\u9002\\u914d',desc:'\\u9002\\u914d Android GKI \\u8bbe\\u5907\\u7684\\u901a\\u7528\\u5185\\u6838\\u955c\\u50cf\\uff0c\\u5237\\u5165\\u66f4\\u7b80\\u4fbf'},
  {icon:'<svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/></svg>',name:'\\u6a21\\u5757\\u7cfb\\u7edf',desc:'\\u5b8c\\u5584\\u7684\\u6a21\\u5757\\u7ba1\\u7406\\u4e0e\\u5b89\\u88c5\\u673a\\u5236\\uff0c\\u652f\\u6301 WebUI \\u53ef\\u89c6\\u5316\\u914d\\u7f6e'},
  {icon:'<svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>',name:'\\u8d85\\u7ea7\\u7528\\u6237\\u7ba1\\u7406',desc:'\\u7cbe\\u7ec6\\u7684\\u5e94\\u7528\\u7ea7 Root \\u6743\\u9650\\u63a7\\u5236\\uff0c\\u4fdd\\u62a4\\u7cfb\\u7edf\\u5b89\\u5168'},
  {icon:'<svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 2a14.5 14.5 0 000 20 14.5 14.5 0 000-20"/><path d="M2 12h20"/></svg>',name:'\\u4e3b\\u9898\\u4e0e\\u5b57\\u4f53',desc:'\\u52a8\\u6001\\u53d6\\u8272\\u4e3b\\u9898\\u5f15\\u64ce\\uff0c\\u652f\\u6301 iPhone \\u98ce\\u683c\\u5b57\\u4f53\\u9009\\u62e9'},
  {icon:'<svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14M12 5l7 7-7 7"/></svg>',name:'\\u591a\\u8bed\\u8a00\\u652f\\u6301',desc:'\\u652f\\u6301\\u4e2d\\u6587\\u3001\\u82f1\\u8bed\\u3001\\u4fc4\\u8bed\\u3001\\u65e5\\u8bed\\u7b49\\u591a\\u79cd\\u8bed\\u8a00\\u754c\\u9762'}
];"""

new_features = """var features=[
  {icon:'<svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>',name:'LKM \\u53ef\\u52a0\\u8f7d\\u5185\\u6838\\u6a21\\u5757',desc:'\\u91c7\\u7528\\u53ef\\u52a0\\u8f7d\\u5185\\u6838\\u6a21\\u5757\\u6280\\u672f\\uff0c\\u65e0\\u9700\\u91cd\\u65b0\\u7f16\\u8bd1\\u5185\\u6838\\u5373\\u53ef\\u83b7\\u53d6 Root \\u6743\\u9650\\u3002\\u652f\\u6301\\u70ed\\u91cd\\u8f7d\\uff0c\\u5347\\u7ea7\\u66f4\\u65b9\\u4fbf\\uff0c\\u4e0d\\u5f71\\u54cd\\u7cfb\\u7edf\\u7a33\\u5b9a\\u6027\\u3002'},
  {icon:'<svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><rect x="4" y="4" width="16" height="16" rx="2"/><path d="M4 12h16M12 4v16"/></svg>',name:'GKI \\u8bbe\\u5907\\u5b8c\\u7f8e\\u9002\\u914d',desc:'\\u5b8c\\u7f8e\\u9002\\u914d Android 12+ GKI \\u8bbe\\u5907\\uff0c\\u81ea\\u52a8\\u68c0\\u6d4b init_boot \\u5206\\u533a\\uff0c\\u667a\\u80fd\\u8bc6\\u522b\\u7b2c\\u4e09\\u65b9\\u5185\\u6838\\uff0c\\u5237\\u5165\\u66f4\\u7b80\\u5355\\u5b89\\u5168\\u3002'},
  {icon:'<svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/></svg>',name:'\\u6a21\\u5757\\u7cfb\\u7edf\\u4e0e WebUI',desc:'\\u5b8c\\u5584\\u7684\\u6a21\\u5757\\u7ba1\\u7406\\u4e0e\\u5b89\\u88c5\\u673a\\u5236\\uff0c\\u652f\\u6301 WebUI \\u53ef\\u89c6\\u5316\\u914d\\u7f6e\\u3002\\u6a21\\u5757\\u4ed3\\u5e93\\u5b9e\\u65f6\\u540c\\u6b65\\uff0c\\u81ea\\u52a8\\u76d1\\u63a7\\u66f4\\u65b0\\u3002'},
  {icon:'<svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>',name:'\\u8d85\\u7ea7\\u7528\\u6237\\u6743\\u9650\\u7ba1\\u7406',desc:'\\u7cbe\\u7ec6\\u7684\\u5e94\\u7528\\u7ea7 Root \\u6743\\u9650\\u63a7\\u5236\\uff0c\\u652f\\u6301\\u9ed8\\u8ba4\\u3001\\u5e94\\u7528\\u63a5\\u5165\\u3001Shell \\u6a21\\u677f\\u7b49\\u591a\\u79cd\\u6388\\u6743\\u65b9\\u5f0f\\uff0c\\u4fdd\\u62a4\\u7cfb\\u7edf\\u5b89\\u5168\\u3002'},
  {icon:'<svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 2a14.5 14.5 0 000 20 14.5 14.5 0 000-20"/><path d="M2 12h20"/></svg>',name:'\\u52a8\\u6001\\u4e3b\\u9898\\u4e0e\\u5b57\\u4f53',desc:'\\u5185\\u7f6e\\u52a8\\u6001\\u53d6\\u8272\\u4e3b\\u9898\\u5f15\\u64ce\\uff0c\\u652f\\u6301 iPhone \\u98ce\\u683c\\u5b57\\u4f53\\u3001\\u591a\\u79cd\\u8c03\\u8272\\u677f\\u3001\\u5f69\\u8679\\u6e32\\u67d3\\u7b49\\u4e3b\\u9898\\u6548\\u679c\\u3002'},
  {icon:'<svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14M12 5l7 7-7 7"/></svg>',name:'\\u591a\\u8bed\\u8a00\\u4e0e\\u5b89\\u5168\\u52a0\\u56fa',desc:'\\u652f\\u6301\\u4e2d\\u6587\\u3001\\u82f1\\u8bed\\u3001\\u4fc4\\u8bed\\u3001\\u65e5\\u8bed\\u7b49\\u591a\\u8bed\\u8a00\\u754c\\u9762\\uff0c\\u5185\\u7f6e 21+ \\u9879\\u5b89\\u5168\\u4fee\\u590d\\uff0c\\u5168\\u9762\\u9632\\u5fa1\\u547d\\u4ee4\\u6ce8\\u5165\\u4e0e\\u8def\\u5f84\\u7a7f\\u8d8a\\u3002'}
];"""

content = content.replace(old_features, new_features)

# Write back
with open('d:\\KinSU\\kinsu-site\\index.html', 'w', encoding='utf-8') as f:
    f.write(content)

print('Enhanced. Length:', len(content))

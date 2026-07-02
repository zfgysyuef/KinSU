<div align="center">

<img src="icon.png" width="128" height="128" alt="KinSU" style="border-radius: 28px; box-shadow: 0 12px 40px rgba(236, 64, 122, 0.25);">

# KinSU 路 閲戣嫃鍐呮牳

> *"鎶?Root 鏉冮檺浜ょ粰鏈€鎳備綘鐨勪汉 鈥斺€?涔熷氨鏄綘鑷繁銆?*

**鍩轰簬 KernelSU 鏋舵瀯鐨?Android GKI 鍐呮牳 Root 瑙ｅ喅鏂规**

[![Release](https://img.shields.io/github/v/release/Spring-bulid/KinSU?label=%E6%9C%80%E6%96%B0%E7%89%88%E6%9C%AC&color=ec407a&style=for-the-badge)](https://github.com/Spring-bulid/KinSU/releases/latest)
[![License](https://img.shields.io/badge/License-GPL--3.0-ff80ab?style=for-the-badge)](LICENSE)
[![API](https://img.shields.io/badge/API-31%2B-e040fb?style=for-the-badge)](https://android-arsenal.com/api?level=31)
[![visitors](https://camo.githubusercontent.com/99eead2c09ce2e00fe47c57777db95507f06194b0ef7ce812f145876790617e0/68747470733a2f2f636f756e742e6765746c6f6c692e636f6d2f6765742f407869616f746f6e67363636362e6769746875622e696f3f7468656d653d72756c653334)](https://github.com/Spring-bulid/KinSU)

**[English](README_EN.md) | 绠€浣撲腑鏂?*

</div>

---

## 浣犲ソ锛屽啋闄╄€?
**KinSU** 鏄竴娆句粠 **KernelSU** 鐢熸€佷腑璇炵敓鐨?Android Root 鎺堟潈绠＄悊鍣ㄣ€傚ス涓嶄細鎿呰嚜鎶?Root 鏉冮檺浜ょ粰闄岀敓浜猴紝鑰屾槸鎶婃渶缁堝喅瀹氭潈閮戦噸鍦颁氦鍒颁綘鎵嬮噷銆?
杩欓噷涓嶆槸鍐峰啺鍐扮殑宸ュ叿绠憋紝鑰屾槸灞炰簬浣犵殑鐜╂満绌洪棿銆傛ā鍧椼€並PM銆佷富棰樿壊銆佸埛鍐欍€佹棩蹇楋紝鍏ㄩ兘琚ス鏁寸悊寰椾簳浜曟湁鏉°€傚彧瑕佷綘鐨勮澶囨惌杞戒簡 KernelSU 鎴?KernelPatch锛孠inSU 灏变細甯︿綘涓€璧锋帰绱?Android 鐨勬繁灞備笘鐣屻€?
KinSU 璇炵敓浜庡 Root 绠＄悊鍣ㄧ殑鏂版兂璞★細瀹冨簲璇ュ湪搴曞眰淇濇寔 KernelSU 鐨勫唴鏍哥骇瀹夊叏妯″瀷锛屽湪涓婂眰鎻愪緵鐜颁唬銆佺洿瑙傘€佸彲鑷畾涔夌殑浜や簰浣撻獙銆傚ス鏃㈡槸浣犵殑鎺堟潈涓灑锛屼篃鏄綘鐨勬ā鍧椾粨搴撱€佸埛鍐欏姪鎵嬪拰涓婚宸ュ潑銆?
---

## 濂硅兘涓轰綘鍋氫粈涔?
<div align="center">

| 鑳藉姏 | 浠嬬粛 |
|:----:|---------|
| 瓒呯骇鐢ㄦ埛鎺堟潈 | 鍐冲畾鍝簺搴旂敤鍙互銆佸摢浜涘簲鐢ㄤ笉鍙互瑙︾ Root锛屾敮鎸佹案涔呮巿鏉冦€佸崟娆℃巿鏉冧笌瀹氭椂鍥炴敹 |
| 妯″潡绠＄悊 | 瀹夎 / 鍚敤 / 绂佺敤 / 鍗歌浇 ZIP 妯″潡锛屾敮鎸?KernelSU 涓?Magisk 椋庢牸妯″潡 |
| KPM 鍐呮牳琛ヤ竵 | 鍔犺浇 `.kpm` 琛ヤ竵锛孏KI 妯″紡涓嬩篃鑳藉祵鍏ワ紝寮€鏈鸿嚜鍔ㄥ姞杞戒笌鎵嬪姩璋冭瘯鍏奸【 |
| AnyKernel3 鍒峰啓 | 鍦ㄥ簲鐢ㄥ唴淇ˉ `boot` / `init_boot`锛屽埛鍐欏墠鑷姩澶囦唤鍘熼暅鍍?|
| 涓婚涓庡瑙?| 鍔ㄦ€佸彇鑹层€佸濂椾富棰樿壊銆佹繁鑹?/ 娴呰壊妯″紡锛屾墦閫犱笓灞炵鐞嗗櫒 |
| 搴旂敤鍐呮洿鏂?| 鑷姩浠?GitHub Releases 妫€鏌ユ柊鐗堟湰锛屼竴閿烦杞笅杞?|
| 鏃ュ織涓庡璁?| 鏌ョ湅 Root 鎺堟潈璁板綍銆佹ā鍧楀姞杞芥棩蹇椾笌鍐呮牳琛ヤ竵杩愯鐘舵€?|

</div>

---

## 涓轰粈涔堥€夋嫨 KinSU

Android 涓婄殑 Root 鏂规鏈夊緢澶氾紝浣?KinSU 灏濊瘯鍦ㄥ嚑涓叧閿偣涓婂仛寰楁洿濂斤細

- **鎺堟潈褰掍綘**锛歊oot 涓嶆槸搴旂敤澶╃敓搴旀湁鐨勬潈鍒┿€傛瘡涓€娆℃巿鏉冦€佹嫆缁濇垨鍥炴敹锛岄兘鐢变綘鍐冲畾銆?- **鍐呮牳绾у畨鍏?*锛氫緷鎵?KernelSU 鐨勫唴鏍搁挬瀛愪笌瀹夊叏涓婁笅鏂囷紝KinSU 鍦ㄧ郴缁熸渶搴曞眰鎵ц鎺堟潈绛栫暐銆?- **鐜颁唬浣撻獙**锛歁aterial Design 3 Expressive 瑙嗚銆佸ぇ鍦嗚銆佸姩鎬佸彇鑹蹭笌娴佺晠鍔ㄧ敾锛岃绠＄悊鍣ㄤ笉鍐嶅儚宸ュ叿绠便€?- **鎵╁睍鍙嬪ソ**锛氭ā鍧椼€並PM銆丄nyKernel3 涓夌鎵╁睍鏂瑰紡瑕嗙洊浠庡簲鐢ㄥ埌鍐呮牳鐨勪笉鍚岄渶姹傘€?- **閫忔槑寮€婧?*锛氭簮鐮併€佹瀯寤烘祦绋嬩笌鍙戝竷璧勪骇鍏ㄩ儴鍏紑锛岄伒寰?GPL-3.0 鍗忚銆?
---

## 蹇€熷紑濮?
```text
鈶?瀹夎 KinSU APK
鈶?纭鍐呮牳宸查泦鎴?KernelSU / KernelPatch
鈶?鎺堟潈銆佽妯″潡銆佸埛鍐欍€佹崲涓婚锛屽紑濮嬩綘鐨勫啋闄?```

鏇磋缁嗙殑瀹夎涓庝娇鐢ㄨ鏄庤璁块棶 [瀹樻柟鏂囨。](https://spring-bulid.github.io/KinSU/)銆?
---

## 涓嬭浇

<div align="center">

[![涓嬭浇 APK](https://img.shields.io/badge/%E4%B8%8B%E8%BD%BD%20APK-ec407a?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Spring-bulid/KinSU/releases/latest)
[![瀹樻柟鏂囨。](https://img.shields.io/badge/%E5%AE%98%E6%96%B9%E6%96%87%E6%A1%A3-7c4dff?style=for-the-badge)](https://spring-bulid.github.io/KinSU/)

</div>

| 鏂囦欢 | 澶у皬 | 璇存槑 |
|------|------|------|
| `KinSU_3.1.8_30036-release.apk` | ~8.5 MB | 绠＄悊鍣ㄦ湰浣擄紙鍚唴寤?LKM锛?|

---

## 鏋勫缓

```bash
# 缂栬瘧 APK
cd KernelSU/manager
./gradlew assembleRelease
```

鎻愮ず锛歐indows 鐜涓嬭浣跨敤 `gradlew.bat`銆?
---

## 椤圭洰缁撴瀯

```text
KinSU/
鈹溾攢鈹€ KernelSU/          # 绠＄悊鍣ㄤ笌鍐呮牳妯″潡婧愮爜
鈹溾攢鈹€ kernel/            # KinSU 鍐呮牳琛ヤ竵婧愮爜
鈹溾攢鈹€ kinsu-site/        # 瀹樻柟鏂囨。绔?鈹溾攢鈹€ README.md          # 绠€浣撲腑鏂囪鏄?鈹斺攢鈹€ README_EN.md       # English README
```

---

## 鑷磋阿

KinSU 鐨勬垚闀跨涓嶅紑鍓嶈緢浠殑鑲╄唨銆?
| 椤圭洰 | 璐＄尞 |
|------|------|
| [KernelSU](https://github.com/tiann/KernelSU) | 鍐呮牳绾?Root 鑳藉姏涓庡畨鍏ㄦā鍨?|
| [FolkPatch](https://github.com/LyraVoid/FolkPatch) | 澶氬竷灞€涓庤瑙夌伒鎰?|
| [KernelPatch](https://github.com/bmax121/KernelPatch) | KPM 鍐呮牳琛ヤ竵鍙傝€?|
| [APatch](https://github.com/bmax121/APatch) | KPM 鏋舵瀯鐏垫劅 |

杩樿鎰熻阿 **鏉惧潅鏈夊笇**銆?*mrbeer1960** 浠ュ強鎵€鏈夋敮鎸侀」鐩殑浼欎即涓€璺浉浼淬€?
---

<div align="center">

### 璁稿彲璇?
**GPL-3.0** 鈥?鑷敱寮€婧愶紝姘歌繙淇濇寔鐑埍銆?
*"鎰夸綘鍒锋満涓嶇炕杞︼紝Root 涓嶆姤閿欙紝妯″潡閮藉吋瀹癸紝涓婚閮藉ソ鐪嬨€?*

</div>
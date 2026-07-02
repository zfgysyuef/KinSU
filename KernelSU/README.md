[English](#english) | 涓枃

<div align="center">

<img src="website/docs/public/logo.png" alt="KinSU" width="120" height="120">

# KinSU

**鍩轰簬 Android 鍐呮牳鐨?root 鏂规 路 KernelSU 鐨勭嫭绔嬭繘鍖栧垎鏀?*

<br>

<a href="https://github.com/Spring-bulid/KinSU/releases/latest"><img src="https://img.shields.io/github/v/release/Spring-bulid/KinSU?label=Latest%20Release&logo=github&color=blueviolet" alt="Latest Release"></a>
<a href="https://github.com/Spring-bulid/KinSU/releases/latest"><img src="https://img.shields.io/github/downloads/Spring-bulid/KinSU/total?logo=android&color=green" alt="Downloads"></a>
<a href="LICENSE"><img src="https://img.shields.io/badge/License-GPL%20v3-blue.svg?logo=gnu" alt="License"></a>
<img src="https://img.shields.io/badge/Platform-Android%2012%2B-brightgreen?logo=android" alt="Platform">
<img src="https://img.shields.io/badge/Arch-arm64--v8a%20%7C%20x86__64-blue?logo=arm" alt="Architecture">
<img src="https://camo.githubusercontent.com/99eead2c09ce2e00fe47c57777db95507f06194b0ef7ce812f145876790617e0/68747470733a2f2f636f756e742e6765746c6f6c692e636f6d2f6765742f407869616f746f6e67363636362e6769746875622e696f3f7468656d653d72756c653334" alt="visitors">

<br>
<br>

</div>

---

## 鐩綍

- [浠€涔堟槸 KinSU](#浠€涔堟槸-kinsu)
- [鏍稿績鐗规€(#鏍稿績鐗规€?
- [涓?KernelSU 鐨勫尯鍒玗(#涓?kernelsu-鐨勫尯鍒?
- [椤圭洰鏋舵瀯](#椤圭洰鏋舵瀯)
- [KMI 鏀寔鍒楄〃](#kmi-鏀寔鍒楄〃)
- [鍏煎鎬(#鍏煎鎬?
- [蹇€熷紑濮媇(#蹇€熷紑濮?
- [浠庢簮鐮佹瀯寤篯(#浠庢簮鐮佹瀯寤?
- [GKI 鍐呮牳缂栬瘧鏁欑▼](#gki-鍐呮牳缂栬瘧鏁欑▼)
- [鎶€鏈爤](#鎶€鏈爤)
- [璁稿彲璇乚(#璁稿彲璇?
- [楦ｈ阿](#楦ｈ阿)

---

## 浠€涔堟槸 KinSU

KinSU 鏄竴涓?*鍩轰簬 Linux 鍐呮牳鐨?Android root 鏉冮檺绠＄悊鏂规**銆傚畠閫氳繃鍐呮牳妯″潡锛圠KM锛夊湪鍐呮牳灞傞潰鎷︽埅鍜岀鐞?`su` 璋冪敤锛岃€岄潪渚濊禆鐢ㄦ埛鎬佺殑 su 浜岃繘鍒舵枃浠躲€?

**杩欐剰鍛崇潃锛?*

- Root 鏉冮檺鐨勬巿浜堢敱鍐呮牳鐩存帴鎺у埗锛屾棤娉曡鐢ㄦ埛鎬佺▼搴忕粫杩囨垨浼€?
- 涓嶄慨鏀?`/system` 鍒嗗尯锛岀湡姝ｅ仛鍒?*鏃犵郴缁熶慨鏀癸紙systemless锛?*
- 閫氳繃 App Profile 瀵规瘡涓簲鐢ㄧ嫭绔嬭缃?root 绛栫暐锛屾妸 root 鏉冨姏鍏宠繘绗煎瓙閲?

KinSU 鑴辫儙浜?[KernelSU](https://github.com/tiann/KernelSU)锛屽湪淇濈暀鍏跺叏閮ㄥ唴鏍歌兘鍔涚殑鍩虹涓婏紝杩涜浜?*娣卞害鐨勫搧鐗岀嫭绔嬪寲鏀归€?*銆?

---

## 鏍稿績鐗规€?

### 鍐呮牳绾?su 鍜屾潈闄愮鐞?

Root 鏉冮檺鐩存帴閫氳繃鍐呮牳 IOCTL 鎺ュ彛鎺堜簣锛屾暣涓祦绋嬬粫杩囦簡浼犵粺鐨勭敤鎴锋€?`su` 浜岃繘鍒躲€傚唴鏍搁€氳繃 `setresuid` 绯荤粺璋冪敤閽╁瓙鎷︽埅鎵€鏈夋彁鏉冭姹傦紝鍙湁鍦?allowlist 涓殑搴旂敤鎵嶈兘鑾峰緱 root銆?

### Metamodule 妯″潡绯荤粺

閫氳繃 OverlayFS 鎸傝浇鎶€鏈疄鐜板彲鎻掓嫈鐨勬棤绯荤粺淇敼銆傛ā鍧椾互 ZIP 鍖呭舰寮忓畨瑁咃紝鏀寔锛?

- 瀹夎/鍗歌浇/鍚敤/绂佺敤鎿嶄綔
- 鐢熷懡鍛ㄦ湡鑴氭湰锛歚post-fs-data.sh`銆乣service.sh`銆乣post-mount.sh`銆乣boot-completed.sh`
- 鑷畾涔?OverlayFS 鎸傝浇瑙勫垯
- Metamodule锛氬崟涓€娲昏穬鐨勫厓妯″潡锛屽彲 hook 鎵€鏈夊叾浠栨ā鍧楃殑鎸傝浇鍜屽畨瑁呰涓?

### App Profile

涓烘瘡涓簲鐢ㄥ崟鐙厤缃?root 鏉冮檺绛栫暐锛?

- **UID/GID/Capabilities**锛氭帶鍒惰繘绋嬭繍琛屾椂鐨勮韩浠?
- **SELinux 鍩?*锛氳嚜瀹氫箟 SELinux 绛栫暐璇彞
- **Umount 妯″潡**锛氬闈?root 搴旂敤鍗歌浇妯″潡鎸傝浇锛岄殣钘?root 鐥曡抗
- **妯℃澘绯荤粺**锛氶瀹氫箟绛栫暐妯℃澘锛屼竴閿簲鐢ㄥ埌澶氫釜搴旂敤
- **No New Privs**锛氶樆姝㈣繘涓€姝ョ殑鏉冮檺鎻愬崌

### 鍏ㄦ柊鑷畾涔?UI

鍩轰簬 Jetpack Compose + Material Design 3 瀹屽叏閲嶈璁＄殑鐣岄潰锛?

- 鍘熺敓娣辫壊/娴呰壊涓婚锛屾敮鎸?Material You 鍔ㄦ€佸彇鑹?
- 娴佺晠鐨勫鑸姩鐢诲拰鎵嬪娍浜や簰
- 妯″潡浠撳簱娴忚鍣紝鏀寔鍦ㄧ嚎鎼滅储鍜屽畨瑁?
- WebUI 娓叉煋寮曟搸锛屾ā鍧楀彲鎻愪緵 Web 鐣岄潰
- SU 鏃ュ織鏌ョ湅鍣紝瀹炴椂鐩戞帶 root 璋冪敤

### 鑷紪璇戝唴鏍告ā鍧?

APK 鍐呭祵 7 涓缂栬瘧鐨?KMI 鍐呮牳妯″潡锛岃鐩?Android 12 ~ 16锛?

- 缂栬瘧浜х墿鐢?`rust_embed` 瀹忓祵鍏ュ埌 `ksud` 瀹堟姢杩涚▼
- 鍦?Manager 涓洿鎺ラ€夋嫨 KMI 鐗堟湰鍒峰叆
- 鏀寔浣跨敤鏈湴缂栬瘧鐨?`.ko` 鏂囦欢浣滀负澶囬€?

---

## 涓?KernelSU 鐨勫尯鍒?

| 鏂归潰 | KernelSU | KinSU |
|------|----------|----------|
| 鍝佺墝鍚?| KernelSU | KinSU |
| UI 妗嗘灦 | MIUIX (灏忕背椋庢牸) | Material Design 3 (鍏ㄦ柊閲嶅啓) |
| 瀹堟姢杩涚▼ | `libksud.so` | `libKinSUd.so` |
| 鍐呮牳妯″潡 | `kernelsu.ko` | `KinSU.ko` |
| 鍖呭悕 | `me.weishu.kernelsu` | `me.weishu.KinSU` |
| 鎵€鏈夊瓧绗︿覆 | KernelSU | KinSU (40+ 璇█) |
| 鏍稿績鍔熻兘 | 瀹屽叏鍏煎 | 瀹屽叏鍏煎 |

---

## 椤圭洰鏋舵瀯

```
鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?
鈹?                  Android Manager APK                         鈹?
鈹? 鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹? 鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹? 鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?   鈹?
鈹? 鈹?Kotlin   鈹? 鈹? Compose UI 鈹? 鈹? C++ JNI (jni.cc)     鈹?   鈹?
鈹? 鈹?ViewModels鈹? 鈹? Material 3 鈹? 鈹? IOCTL 鐩存帴閫氫俊       鈹?   鈹?
鈹? 鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹? 鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹? 鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?   鈹?
鈹?                                             鈹?                鈹?
鈹溾攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹尖攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?
鈹?                  Rust Userspace              鈹?                鈹?
鈹? 鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹愨攤                 鈹?
鈹? 鈹? ksud (瀹堟姢杩涚▼)                          鈹傗攤                 鈹?
鈹? 鈹? 鈹溾攢 妯″潡绠＄悊 (install/uninstall/enable)   鈹傗攤                 鈹?
鈹? 鈹? 鈹溾攢 Boot Image 淇ˉ (boot_patch)         鈹傗攤                 鈹?
鈹? 鈹? 鈹溾攢 LKM 寤惰繜鍔犺浇 (late_load)             鈹傗攤                 鈹?
鈹? 鈹? 鈹溾攢 App Profile 绠＄悊 (profile)           鈹傗攤                 鈹?
鈹? 鈹? 鈹溾攢 SELinux 绛栫暐 (sepolicy)             鈹傗攤                 鈹?
鈹? 鈹? 鈹溾攢 SU 鏃ュ織 (sulog)                     鈹傗攤                 鈹?
鈹? 鈹? 鈹斺攢 Root Shell (su)                     鈹傗攤                 鈹?
鈹? 鈹溾攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹も攤                 鈹?
鈹? 鈹? ksuinit (鏃╂湡鍚姩)                       鈹傗攤                 鈹?
鈹? 鈹? 鈹斺攢 鍔犺浇 KinSU.ko 鈫?execve 鐪熷疄 init  鈹傗攤                 鈹?
鈹? 鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹樷攤                 鈹?
鈹?                                             鈹?                鈹?
鈹溾攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹尖攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?
鈹?                Linux Kernel Module           鈹?                鈹?
鈹? 鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹粹攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹?
鈹? 鈹? KinSU.ko                                              鈹?鈹?
鈹? 鈹? 鈹溾攢 supercall.c        IOCTL 璋冨害                        鈹?鈹?
鈹? 鈹? 鈹溾攢 allowlist.c        搴旂敤鐧藉悕鍗?                        鈹?鈹?
鈹? 鈹? 鈹溾攢 app_profile.c      姣忓簲鐢?root 绛栫暐                   鈹?鈹?
鈹? 鈹? 鈹溾攢 setuid_hook.c      鎷︽埅 setresuid 绯荤粺璋冪敤            鈹?鈹?
鈹? 鈹? 鈹溾攢 lsm_hook.c         LSM 閽╁瓙                          鈹?鈹?
鈹? 鈹? 鈹溾攢 selinux/rules.c    SELinux 鍩熺鐞?                   鈹?鈹?
鈹? 鈹? 鈹溾攢 sulog/             SU 鏃ュ織浜嬩欢                        鈹?鈹?
鈹? 鈹? 鈹斺攢 feature/           鐗规€у紑鍏?                         鈹?鈹?
鈹? 鈹?    鈹溾攢 adb_root       ADB root 鏉冮檺                     鈹?鈹?
鈹? 鈹?    鈹溾攢 kernel_umount  鍐呮牳灞傚嵏杞?                        鈹?鈹?
鈹? 鈹?    鈹溾攢 sulog          SU 鏃ュ織                            鈹?鈹?
鈹? 鈹?    鈹溾攢 sucompat       鍏煎 /system/bin/su               鈹?鈹?
鈹? 鈹?    鈹斺攢 selinux_hide   SELinux 闅愯棌                      鈹?鈹?
鈹? 鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹粹攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹?
鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?
```

### 涓夌閫氫俊閫氶亾

| 閫氶亾 | 鏈哄埗 | 鐢ㄩ€?|
|------|------|------|
| **IOCTL** | `[ksu_driver]` 鍖垮悕 inode锛岄€氳繃 `reboot()` 榄旀硶鏁版敞鍏?FD | App Profile銆丄llowlist銆丗eature 閰嶇疆 |
| **Root Shell** | ksud 涔熸槸 `su`锛宍libsu` 璋冪敤 ksud CLI | 妯″潡瀹夎銆丅oot Patch銆丼epolicy |
| **Sulog FD** | `KSU_IOCTL_GET_SULOG_FD` + epoll | 鍐呮牳鏃ュ織浜嬩欢娴?|

### 涓ょ鍚姩璺緞

**璺緞 A锛欸KI 棰勬墦鍖咃紙鎺ㄨ崘锛?*

```
Bootloader 鈫?Linux Kernel (鍚?KinSU.ko)
  鈫?ksuinit 鏇挎崲 /init
  鈫?ksuinit 閫氳繃 init_module() 鍔犺浇 KinSU.ko
  鈫?鎵€鏈夊唴鏍稿瓙绯荤粺鍦ㄦ棭鏈熷惎鍔ㄤ腑鍒濆鍖?
  鈫?ksuinit execves 鐪熸鐨?/init
```

**璺緞 B锛歁agica 瓒婄嫳寤惰繜鍔犺浇锛堥攣 bootloader 璁惧锛?*

```
Bootloader 鈫?Stock Kernel
  鈫?AppZygotePreload 鍚姩 MagicaService
  鈫?Fork ksud "late-load --magica 5555"
  鈫?ksud 閫氳繃灞炴€ф搷浣滃惎鐢?adbd root
  鈫?ADB TCP 杩炴帴锛宔xec 鑷韩涓?"late-load --post-magica"
  鈫?妫€娴?KMI锛屼粠鍐呭祵璧勬簮鍔犺浇 KinSU.ko
  鈫?杩愯闃舵鑴氭湰锛岄噸鍚?Manager
```

---

## KMI 鏀寔鍒楄〃

| KMI | 鍐呮牳鐗堟湰 | Android 鐗堟湰 | 鐘舵€?|
|-----|---------|-------------|------|
| `android12-5.10` | 5.10 | 12 | 棰勭紪璇?|
| `android13-5.10` | 5.10 | 13 | 棰勭紪璇?|
| `android13-5.15` | 5.15 | 13 | 棰勭紪璇?|
| `android14-5.15` | 5.15 | 14 | 棰勭紪璇?|
| `android14-6.1` | 6.1 | 14 | 棰勭紪璇?|
| `android15-6.6` | 6.6 | 15 | 棰勭紪璇?|
| `android16-6.12` | 6.12 | 16 | 棰勭紪璇?|

---

## 鍏煎鎬?

| 鏉′欢 | 鏀寔鐘舵€?|
|------|---------|
| Android GKI 2.0 (Kernel 5.10+) | 瀹樻柟鏀寔 |
| 鏃у唴鏍?(4.14+) | 闇€鎵嬪姩缂栬瘧 |
| WSA / ChromeOS / 瀹瑰櫒鍖?Android | 鏀寔 |
| 鏋舵瀯 `arm64-v8a` | 鏀寔 |
| 鏋舵瀯 `x86_64` | 鏀寔 |
| Magisk 鍏卞瓨 | 鑷姩妫€娴嬪苟璺宠繃鍐茬獊 |

---

## 蹇€熷紑濮?

1. 浠?[Releases](https://github.com/Spring-bulid/KinSU/releases/latest) 涓嬭浇鏈€鏂?APK
2. 瀹夎 APK 鍒颁綘鐨?Android 璁惧
3. 鎵撳紑 KinSU Manager
4. **GKI 璁惧**锛氬湪 Manager 涓€夋嫨瀵瑰簲 KMI 鐗堟湰锛屼竴閿埛鍏?
5. **闈?GKI 璁惧**锛氭墜鍔ㄧ紪璇戝唴鏍告ā鍧楋紝浣跨敤銆屼娇鐢ㄦ湰鍦?LKM 鏂囦欢銆嶅埛鍏?
6. 閲嶅惎璁惧锛屽紑濮嬩娇鐢?root 鏉冮檺绠＄悊

---

## 浠庢簮鐮佹瀯寤?

### 鐜瑕佹眰

- **Rust** 1.80+
- **Android NDK** 27+
- **Gradle** 9.5+
- **JDK** 21+
- **Android SDK** 37 (compileSdk)

### 鏋勫缓姝ラ

```bash
# 1. 缂栬瘧 ksud 瀹堟姢杩涚▼锛堜袱涓灦鏋勶級
cd KinSU
cargo build --release --target aarch64-linux-android -p ksud
cargo build --release --target x86_64-linux-android -p ksud

# 2. 澶嶅埗缂栬瘧浜х墿鍒?jniLibs
cp target/aarch64-linux-android/release/ksud \
   manager/app/src/main/jniLibs/arm64-v8a/libKinSUd.so
cp target/x86_64-linux-android/release/ksud \
   manager/app/src/main/jniLibs/x86_64/libKinSUd.so

# 3. 缂栬瘧鍐呮牳妯″潡锛堝彲閫夛紝鐢ㄤ簬鏇挎崲鍗犱綅鏂囦欢锛?
# 闇€瑕?Linux 鐜 + DDK 宸ュ叿
cd kernel
./build-all.sh

# 4. 鏋勫缓 Manager APK
cd ../manager
ANDROID_HOME=/path/to/sdk ./gradlew assembleRelease
```

APK 杈撳嚭浣嶇疆锛歚manager/app/build/outputs/apk/release/KinSU_*-release.apk`

---

## GKI 鍐呮牳缂栬瘧鏁欑▼

瀹屾暣鐨?GKI 鍐呮牳妯″潡缂栬瘧鎸囧崡锛屾兜鐩栦笁绉嶇紪璇戞柟寮忥紙DDK 宸ュ叿銆佹墜鍔ㄧ紪璇戙€乻etup.sh 闆嗘垚锛夛紝浠ュ強甯歌闂鎺掓煡銆?

**[鏌ョ湅瀹屾暣鏁欑▼](docs/gki-build-guide.md)**

---

## 鎶€鏈爤

| 灞傜骇 | 鎶€鏈?| 璇存槑 |
|------|------|------|
| **Kernel** | C, Linux Kernel API | 鍐呮牳妯″潡 (LKM) |
| **Userspace** | Rust | 瀹堟姢杩涚▼ + 鏃╂湡鍚姩 |
| **JNI** | C++17 | Kotlin 鈫?Kernel 妗ユ帴 |
| **Manager** | Kotlin, Jetpack Compose | Android UI |
| **鏋勫缓** | Cargo, Gradle, CMake | 璺ㄥ钩鍙版瀯寤?|

### Rust 鍏抽敭渚濊禆

- `rustix`锛氱郴缁熻皟鐢ㄥ皝瑁?
- `android-bootimg`锛欰ndroid Boot Image 瑙ｆ瀽
- `nom`锛歋ELinux 绛栫暐瑙ｆ瀽鍣?
- `clap`锛欳LI 鍙傛暟瑙ｆ瀽
- `bindgen`锛氬唴鏍?UAPI 缁戝畾鐢熸垚
- `adb_client`锛歁agica 璺緞 ADB 閫氫俊

---

## 璁稿彲璇?

- `kernel/` 鐩綍涓嬬殑鎵€鏈夋枃浠讹細[GPL-2.0-only](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
- 鍏朵綑鎵€鏈夐儴鍒嗭細[GPL-3.0-or-later](https://www.gnu.org/licenses/gpl-3.0.html)

---

## 楦ｈ阿

KinSU 鍩轰簬浠ヤ笅寮€婧愰」鐩瀯寤猴細

- [**KernelSU**](https://github.com/tiann/KernelSU) 鈥?涓婃父椤圭洰锛屾彁渚涘畬鏁寸殑鍐呮牳 root 鏂规
- [**kernel-assisted-superuser**](https://git.zx2c4.com/kernel-assisted-superuser/about/) 鈥?鏈€鍒濈殑鐏垫劅鏉ユ簮
- [**Magisk**](https://github.com/topjohnwu/Magisk) 鈥?寮哄ぇ鐨?Android root 宸ュ叿
- [**genuine**](https://github.com/brevent/genuine/) 鈥?APK v2 绛惧悕楠岃瘉
- [**Diamorphine**](https://github.com/m0nad/Diamorphine) 鈥?rootkit 鎶€鏈弬鑰?

### 鐗堟潈澹版槑

KinSU 鏄?[KernelSU](https://github.com/tiann/KernelSU) 鐨勮鐢熶綔鍝侊紙derivative work锛夈€傛牴鎹?GNU General Public License锛圙PL v2/v3锛夌殑瑕佹眰锛岀壒姝ゅ０鏄庯細

- **鍘熶綔鑰?*锛歔weishu](https://github.com/tiann)锛圞ernelSU 椤圭洰浣滆€咃級
- **鍘熼」鐩?*锛歨ttps://github.com/tiann/KernelSU
- **鍘熻鍙崗璁?*锛欸PL v2锛坘ernel 閮ㄥ垎锛? GPL v3锛坲serspace 涓?manager 閮ㄥ垎锛?
- **鎻愪氦璁板綍**锛欿inSU 瀹屾暣淇濈暀浜?KernelSU 鐨勫師濮嬫彁浜ゅ巻鍙诧紝鏈仛浠讳綍绡℃敼鎴栨礂绋?
- **璐＄尞鑰?*锛歸eishu 鍙婃墍鏈?[KernelSU 璐＄尞鑰匽(https://github.com/tiann/KernelSU/graphs/contributors)

KinSU 鍦ㄦ鍩虹涓婅繘琛岀殑鎵€鏈変慨鏀瑰潎鍚屾牱閬靛惊 GPL 鍗忚寮€婧愩€傚畬鏁村０鏄庤瑙?[NOTICE](NOTICE) 鏂囦欢銆?

---

<h2 id="english">English</h2>

<div align="center">

<img src="website/docs/public/logo.png" alt="KinSU" width="120" height="120">

# KinSU

**A kernel-based root solution for Android. Rebranded and evolved from KernelSU.**

<br>

<a href="https://github.com/Spring-bulid/KinSU/releases/latest"><img src="https://img.shields.io/github/v/release/Spring-bulid/KinSU?label=Latest%20Release&logo=github&color=blueviolet" alt="Latest Release"></a>
<a href="https://github.com/Spring-bulid/KinSU/releases/latest"><img src="https://img.shields.io/github/downloads/Spring-bulid/KinSU/total?logo=android&color=green" alt="Downloads"></a>
<a href="LICENSE"><img src="https://img.shields.io/badge/License-GPL%20v3-blue.svg?logo=gnu" alt="License"></a>
<img src="https://img.shields.io/badge/Platform-Android%2012%2B-brightgreen?logo=android" alt="Platform">
<img src="https://img.shields.io/badge/Arch-arm64--v8a%20%7C%20x86__64-blue?logo=arm" alt="Architecture">
<img src="https://camo.githubusercontent.com/99eead2c09ce2e00fe47c57777db95507f06194b0ef7ce812f145876790617e0/68747470733a2f2f636f756e742e6765746c6f6c692e636f6d2f6765742f407869616f746f6e67363636362e6769746875622e696f3f7468656d653d72756c653334" alt="visitors">

</div>

## What is KinSU

KinSU is a **kernel-level root access management solution** for Android. It intercepts `su` calls through a Linux kernel module (LKM) at the kernel level, rather than relying on userspace `su` binaries.

This means root access is **granted directly by the kernel**, bypassing traditional userspace tools 鈥?making it resistant to tampering and detection.

## Features

- **Kernel-based su**: Root access via kernel IOCTL 鈥?no userspace `su` binary
- **Metamodule System**: Pluggable systemless modifications via OverlayFS
- **App Profile**: Per-app root UID/GID/caps/SELinux domain policies
- **Custom UI**: Fully redesigned with Jetpack Compose + Material Design 3
- **7 Pre-compiled KMIs**: android12-5.10 through android16-6.12

## Project Architecture

```
Kotlin/Compose Manager 鈫?C++ JNI 鈫?Rust ksud daemon 鈫?C kernel module (KinSU.ko)
```

Three communication channels between userspace and kernel:

1. **IOCTL** via `[ksu_driver]` anonymous inode (App Profile, Feature config)
2. **Root Shell** via ksud CLI (Module management, Boot patching)
3. **Sulog FD** via epoll (Kernel event streaming)

## KMI Support

| KMI | Kernel | Android |
|-----|--------|---------|
| android12-5.10 | 5.10 | 12 |
| android13-5.10 | 5.10 | 13 |
| android13-5.15 | 5.15 | 13 |
| android14-5.15 | 5.15 | 14 |
| android14-6.1 | 6.1 | 14 |
| android15-6.6 | 6.6 | 15 |
| android16-6.12 | 6.12 | 16 |

## Quick Start

Download the latest APK from [Releases](https://github.com/Spring-bulid/KinSU/releases/latest), install, select your KMI version, and flash.

## GKI Kernel Build Guide

Complete guide for compiling KinSU GKI kernel modules with three methods (DDK tool, manual build, setup.sh integration).

**[View Full Guide](docs/gki-build-guide.md)**

## License

- `kernel/`: [GPL-2.0-only](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
- Everything else: [GPL-3.0-or-later](https://www.gnu.org/licenses/gpl-3.0.html)

## Credits

[KernelSU](https://github.com/tiann/KernelSU) 路 [kernel-assisted-superuser](https://git.zx2c4.com/kernel-assisted-superuser/about/) 路 [Magisk](https://github.com/topjohnwu/Magisk)

### Copyright Notice

KinSU is a derivative work of [KernelSU](https://github.com/tiann/KernelSU). In accordance with the GNU General Public License (GPL v2/v3):

- **Original Author**: [weishu](https://github.com/tiann) (author of the KernelSU project)
- **Original Project**: https://github.com/tiann/KernelSU
- **Original License**: GPL v2 (kernel) / GPL v3 (userspace & manager)
- **Commit History**: KinSU preserves the complete original commit history of KernelSU without any tampering or rewriting
- **Contributors**: weishu and all [KernelSU contributors](https://github.com/tiann/KernelSU/graphs/contributors)

All modifications made in KinSU are released under the same GPL license. See the [NOTICE](NOTICE) file for the full statement.

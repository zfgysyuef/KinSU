Add-Type -AssemblyName System.Web
$prompt = "anime style app icon of Bohr (薄荷) from Neverness to Everness game, cute mint green haired girl, sleepy expression, cream beige background, soft pastel colors, simple flat illustration, centered head portrait, circle icon style"
$enc = [System.Web.HttpUtility]::UrlEncode($prompt)
$url = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=$enc&image_size=square"
$out = "d:\FollKernel\tmp\bohe_ai_icon_v3.png"
Invoke-WebRequest -Uri $url -OutFile $out -UseBasicParsing -TimeoutSec 120
$file = Get-Item $out
Write-Output "Size: $($file.Length) bytes"

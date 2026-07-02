# Generate AI icon with proper URL encoding
Add-Type -AssemblyName System.Web

$prompt = "anime style app icon, cute girl with mint green hair, sleepy drowsy expression, green eyes, mint theme, simple flat illustration, cream beige background, character head and shoulders centered, circle cropped style, clean minimal anime art, kawaii mascot icon, soft pastel mint green tones, white cream background"
$encoded = [System.Web.HttpUtility]::UrlEncode($prompt)
$url = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=$encoded&image_size=square"

$outPath = "d:\FollKernel\tmp\bohe_ai_icon_v2.png"
Invoke-WebRequest -Uri $url -OutFile $outPath -UseBasicParsing -TimeoutSec 90
$file = Get-Item $outPath
Write-Output "Generated: $($file.Name) ($($file.Length) bytes)"

Get-ChildItem -Path "D:\KinSU\KernelSU" -Recurse -Include *.kt,*.java,*.rs,*.c,*.h,*.xml,*.json,*.toml,*.py,*.sh,*.md,*.kts,*.properties,*.txt,*.cfg,*.yml,*.yaml,*.js,*.ts -File | ForEach-Object {
    $file = $_.FullName
    $content = [System.IO.File]::ReadAllText($file)
    $original = $content

    # Skip binary files and .git directory
    if ($file -match '\\.git\\' -or $file -match '\\build\\' -or $file -match '\\.gradle\\') { return }

    # Replace KinSU -> KinSU (case sensitive)
    $content = $content -replace 'KinSU', 'KinSU'

    # Replace follkernel -> kinsu (case sensitive)
    $content = $content -replace 'follkernel', 'kinsu'

    # Replace FOLLKERNEL -> KINSU
    $content = $content -replace 'FOLLKERNEL', 'KINSU'

    # Replace libfollkerneld.so -> libkinsud.so
    $content = $content -replace 'libfollkerneld\.so', 'libkinsud.so'

    # Replace me.weishu.follkernel -> me.weishu.kinsu
    $content = $content -replace 'me\.weishu\.follkernel', 'me.weishu.kinsu'

    if ($content -ne $original) {
        [System.IO.File]::WriteAllText($file, $content)
        Write-Output "Updated: $file"
    }
}
Write-Output "Done!"

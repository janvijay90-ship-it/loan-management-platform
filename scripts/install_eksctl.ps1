$ErrorActionPreference = "Stop"
$zipPath = "$env:USERPROFILE\eksctl.zip"
$binDir = "$env:USERPROFILE\bin"

if (-not (Test-Path $binDir)) {
    New-Item -ItemType Directory -Path $binDir -Force | Out-Null
}

Write-Host "Downloading eksctl..." -ForegroundColor Cyan
Invoke-WebRequest -Uri "https://github.com/eksctl-io/eksctl/releases/latest/download/eksctl_Windows_amd64.zip" -OutFile $zipPath

Write-Host "Extracting to $binDir..." -ForegroundColor Cyan
Expand-Archive -Path $zipPath -DestinationPath $binDir -Force
Remove-Item $zipPath

$userPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($userPath -notlike "*$binDir*") {
    [Environment]::SetEnvironmentVariable("Path", "$userPath;$binDir", "User")
    $env:Path = "$env:Path;$binDir"
}

Write-Host "Testing eksctl version..." -ForegroundColor Green
& "$binDir\eksctl.exe" version

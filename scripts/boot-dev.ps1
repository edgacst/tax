# Load .env, verify DB, start backend
# Usage:  .\scripts\boot-dev.ps1

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

& "$root\scripts\test-db.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "Fix DB first:  .\scripts\init-db.ps1" -ForegroundColor Yellow
    exit 1
}

if (Test-Path "$root\.env") {
    Get-Content "$root\.env" -Encoding UTF8 | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq "" -or $line.StartsWith("#")) { return }
        $eq = $line.IndexOf("=")
        if ($eq -lt 1) { return }
        $name = $line.Substring(0, $eq).Trim()
        $value = $line.Substring($eq + 1).Trim()
        Set-Item -Path "Env:$name" -Value $value
    }
}

$port8080 = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($port8080) {
    Write-Host "[WARN] Port 8080 is already in use. Stop the old backend (Ctrl+C in its window) then run this script again." -ForegroundColor Red
    Write-Host "       New API (POST/PUT) will not work until you restart." -ForegroundColor Yellow
    exit 1
}

Write-Host "[...] Starting backend only (8080). For the web UI use:  .\scripts\start-dev.ps1" -ForegroundColor Yellow
Write-Host "[...] Compiling latest backend ..." -ForegroundColor Cyan
.\gradlew.bat :taxflow-app:classes -q
if ($LASTEXITCODE -ne 0) { exit 1 }
Write-Host "[...] Starting backend (Flyway migrations run on first boot) ..." -ForegroundColor Cyan
.\gradlew.bat :taxflow-app:bootRun --args="--spring.profiles.active=dev"

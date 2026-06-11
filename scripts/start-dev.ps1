# TaxFlow local dev (Windows)
# Usage:  cd C:\Users\USER\Desktop\tax
#         .\scripts\start-dev.ps1

$ErrorActionPreference = "Continue"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

function Load-DotEnv([string]$path) {
    if (-not (Test-Path $path)) { return }
    Get-Content $path -Encoding UTF8 | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq "" -or $line.StartsWith("#")) { return }
        $eq = $line.IndexOf("=")
        if ($eq -lt 1) { return }
        $name = $line.Substring(0, $eq).Trim()
        $value = $line.Substring($eq + 1).Trim()
        Set-Item -Path "Env:$name" -Value $value
    }
}

function Test-PortListening([int]$port) {
    return [bool](Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue)
}

if (-not (Test-Path "$root\.env")) {
    Write-Host ""
    Write-Host "[Setup] copy .env.example .env  and set NTS_BIZ_VERIFY_SERVICE_KEY" -ForegroundColor Yellow
    Write-Host ""
}

Load-DotEnv "$root\.env"

$dbPort = if ($env:DB_PORT) { [int]$env:DB_PORT } else { 15432 }
$dbUp = (Test-PortListening $dbPort) -or (Test-PortListening 5432)
if ($dbUp) {
    Write-Host "[OK] Postgres port open ($dbPort or 5432)" -ForegroundColor Green
    & "$root\scripts\test-db.ps1" 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[WARN] Port open but taxflow DB not ready. Run: .\scripts\init-db.ps1" -ForegroundColor Yellow
    }
} else {
    Write-Host "[...] Starting Postgres (docker compose)..." -ForegroundColor Cyan
    $dockerOut = docker compose up -d postgres 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "[WARN] Docker failed. DB is not running." -ForegroundColor Red
        Write-Host "  1) Open Docker Desktop and wait until it is fully started (green)" -ForegroundColor Yellow
        Write-Host "  2) If still fails: Docker Desktop -> Troubleshoot -> Restart" -ForegroundColor Yellow
        Write-Host "  3) Then run this script again" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Backend will still start but may fail without DB." -ForegroundColor Yellow
        if ($dockerOut) { Write-Host $dockerOut -ForegroundColor DarkGray }
    } else {
        Write-Host "[OK] Postgres container started" -ForegroundColor Green
        Start-Sleep -Seconds 3
    }
}

if (Test-PortListening 8080) {
    Write-Host "[WARN] Port 8080 already in use — old backend still running." -ForegroundColor Red
    Write-Host "       Run:  .\scripts\restart-backend.ps1   (or Ctrl+C in the backend window first)" -ForegroundColor Yellow
    Write-Host "       New APIs (POST/PUT) will NOT work until you restart the backend." -ForegroundColor Yellow
} else {
    Write-Host "[...] Backend (8080) in new window..." -ForegroundColor Cyan
    $backendCmd = @"
Set-Location '$root'
if (Test-Path '.env') {
  Get-Content '.env' -Encoding UTF8 | ForEach-Object {
    if (`$_ -match '^\s*#' -or `$_ -notmatch '=') { return }
    `$i = `$_ IndexOf('=')
    Set-Item Env:`$(`$_.Substring(0,`$i).Trim()) `$_.Substring(`$i+1).Trim()
  }
}
.\gradlew.bat :taxflow-app:classes -q
if (`$LASTEXITCODE -ne 0) { exit 1 }
.\gradlew.bat :taxflow-app:bootRun --args='--spring.profiles.active=dev'
"@
    Start-Process powershell -ArgumentList @("-NoExit", "-Command", $backendCmd)
    Start-Sleep -Seconds 2
}

Write-Host "[...] Frontend (5173) in new window..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @("-NoExit", "-Command", "Set-Location '$root\frontend'; npm run dev")

Start-Sleep -Seconds 3
Start-Process "http://127.0.0.1:5173/tools/biz-verify"

Write-Host ""
Write-Host "[Done] Browser opened." -ForegroundColor Green
Write-Host "  Wait for 'Started TaxFlowApplication' in the backend window, then refresh." -ForegroundColor Green
Write-Host "  502 = backend still starting." -ForegroundColor Yellow
Write-Host ""

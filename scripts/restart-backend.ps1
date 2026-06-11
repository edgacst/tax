# Stop old backend on 8080 and start with latest code
# Usage:  .\scripts\restart-backend.ps1

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

function Get-ListenerPid([int]$port) {
    $line = netstat -ano | findstr ":$port " | findstr "LISTENING" | Select-Object -First 1
    if (-not $line) { return $null }
    return [int](($line -split '\s+')[-1])
}

$pid8080 = Get-ListenerPid 8080
if ($pid8080) {
    Write-Host "[...] Stopping backend on port 8080 (PID $pid8080) ..." -ForegroundColor Cyan
    try {
        Stop-Process -Id $pid8080 -Force -ErrorAction Stop
        Start-Sleep -Seconds 2
    } catch {
        Write-Host "[FAIL] Could not stop PID $pid8080. Close the backend PowerShell window (Ctrl+C) or end java.exe in Task Manager." -ForegroundColor Red
        exit 1
    }
}

if (Get-ListenerPid 8080) {
    Write-Host "[FAIL] Port 8080 is still in use." -ForegroundColor Red
    exit 1
}

if (Test-Path "$root\.env") {
    Get-Content "$root\.env" -Encoding UTF8 | ForEach-Object {
        if ($_ -match '^\s*#' -or $_ -notmatch '=') { return }
        $i = $_.IndexOf('=')
        Set-Item Env:$($_.Substring(0, $i).Trim()) $_.Substring($i + 1).Trim()
    }
}

Write-Host "[...] Compiling backend ..." -ForegroundColor Cyan
.\gradlew.bat :taxflow-app:classes -q
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host "[...] Starting backend on http://127.0.0.1:8080 ..." -ForegroundColor Cyan
.\gradlew.bat :taxflow-app:bootRun --args="--spring.profiles.active=dev"

# PostgreSQL 18 postgres 비밀번호 재설정 (Windows, 관리자 PowerShell)
# Usage:
#   관리자 PowerShell 에서:
#   .\scripts\reset-postgres-password.ps1 -NewPassword "MyNewPass123!"

param(
    [Parameter(Mandatory = $true)]
    [string]$NewPassword,
    [string]$ServiceName = "postgresql-x64-18",
    [string]$DataDir = "C:\Program Files\PostgreSQL\18\data",
    [string]$Psql = "C:\Program Files\PostgreSQL\18\bin\psql.exe"
)

$ErrorActionPreference = "Stop"

if (-not ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole(
        [Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host "[FAIL] Run PowerShell as Administrator (right-click -> Run as administrator)" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $Psql)) {
    Write-Host "[FAIL] psql not found: $Psql" -ForegroundColor Red
    exit 1
}

$hba = Join-Path $DataDir "pg_hba.conf"
if (-not (Test-Path $hba)) {
    Write-Host "[FAIL] pg_hba.conf not found: $hba" -ForegroundColor Red
    exit 1
}

$backup = "$hba.backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
Copy-Item $hba $backup
Write-Host "[OK] Backup: $backup" -ForegroundColor Green

$content = Get-Content $hba -Raw -Encoding UTF8
$patched = $content -replace '(?m)^(host\s+all\s+all\s+127\.0\.0\.1/32\s+)\S+', '${1}trust'
$patched = $patched -replace '(?m)^(host\s+all\s+all\s+::1/128\s+)\S+', '${1}trust'
if ($patched -eq $content) {
    Write-Host "[WARN] pg_hba.conf pattern not matched; check file manually." -ForegroundColor Yellow
}
# PostgreSQL rejects pg_hba.conf with UTF-8 BOM (PowerShell Set-Content adds one).
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($hba, $patched, $utf8NoBom)

function Restart-PostgresService([string]$Name, [string]$DataDirectory) {
    Write-Host "[...] Stopping $Name ..." -ForegroundColor Cyan
    Stop-Service $Name -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
    try {
        Start-Service $Name -ErrorAction Stop
        Start-Sleep -Seconds 3
        return
    } catch {
        Write-Host "[WARN] Start-Service failed; trying pg_ctl ..." -ForegroundColor Yellow
    }
    $pgCtl = Join-Path (Split-Path $Psql -Parent) "pg_ctl.exe"
    if (-not (Test-Path $pgCtl)) {
        throw "Could not start PostgreSQL service or pg_ctl."
    }
    & $pgCtl -D $DataDirectory start -w -t 30 | Out-Host
    if ($LASTEXITCODE -ne 0) {
        throw "PostgreSQL did not start. Check log in $DataDirectory\log"
    }
}

Restart-PostgresService $ServiceName $DataDir

Write-Host "[...] Setting new postgres password ..." -ForegroundColor Cyan
$escaped = $NewPassword -replace "'", "''"
$sql = "ALTER USER postgres WITH PASSWORD '$escaped';"
& $psql -U postgres -h 127.0.0.1 -p 5432 -d postgres -c $sql

if ($LASTEXITCODE -ne 0) {
    Write-Host "[FAIL] Could not set password. Restoring pg_hba.conf from backup." -ForegroundColor Red
    [System.IO.File]::WriteAllText($hba, [System.IO.File]::ReadAllText($backup), $utf8NoBom)
    Restart-PostgresService $ServiceName $DataDir
    exit 1
}

[System.IO.File]::WriteAllText($hba, [System.IO.File]::ReadAllText($backup), $utf8NoBom)
Write-Host "[OK] pg_hba.conf restored" -ForegroundColor Green
Restart-PostgresService $ServiceName $DataDir

Write-Host ""
Write-Host "[OK] postgres password updated." -ForegroundColor Green
Write-Host "Add to .env:" -ForegroundColor Cyan
Write-Host "  POSTGRES_SUPERUSER_PASSWORD=$NewPassword" -ForegroundColor White
Write-Host ""
Write-Host "Then:" -ForegroundColor Cyan
Write-Host "  .\scripts\init-db.ps1" -ForegroundColor White
Write-Host "  .\scripts\boot-dev.ps1" -ForegroundColor White
Write-Host ""

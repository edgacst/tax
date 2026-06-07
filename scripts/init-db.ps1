# TaxFlow PostgreSQL 초기 설정 (1회)
# Usage:
#   .env 에 POSTGRES_SUPERUSER_PASSWORD=postgres설치비밀번호 추가 후
#   .\scripts\init-db.ps1
# 또는
#   .\scripts\init-db.ps1 -PostgresPassword "your_password"

param(
    [string]$PostgresPassword = "",
    [int]$Port = 5432
)

$ErrorActionPreference = "Continue"
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

function Find-Psql {
    foreach ($path in @(
        "C:\Program Files\PostgreSQL\18\bin\psql.exe",
        "C:\Program Files\PostgreSQL\16\bin\psql.exe"
    )) {
        if (Test-Path $path) { return $path }
    }
    return $null
}

Load-DotEnv "$root\.env"

$psql = Find-Psql
if (-not $psql) {
    Write-Host "[FAIL] psql not found. Install PostgreSQL 16+." -ForegroundColor Red
    exit 1
}

if (-not $PostgresPassword) {
    $PostgresPassword = $env:POSTGRES_SUPERUSER_PASSWORD
}
if (-not $PostgresPassword) {
    Write-Host ""
    Write-Host "postgres superuser password (PostgreSQL install password):" -ForegroundColor Cyan
    $sec = Read-Host -AsSecureString
    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($sec)
    $PostgresPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
}

$env:PGPASSWORD = $PostgresPassword

Write-Host "[...] Checking postgres connection on port $Port ..." -ForegroundColor Cyan
$ping = & $psql -U postgres -h 127.0.0.1 -p $Port -d postgres -c "SELECT 1" -t 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[FAIL] Cannot connect as postgres. Wrong password or Postgres not running." -ForegroundColor Red
    Write-Host $ping
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
    exit 1
}

Write-Host "[...] Creating role taxflow ..." -ForegroundColor Cyan
$roleSql = @'
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'taxflow') THEN
    CREATE ROLE taxflow WITH LOGIN PASSWORD 'taxflow_dev_password';
  ELSE
    ALTER ROLE taxflow WITH LOGIN PASSWORD 'taxflow_dev_password';
  END IF;
END
$$;
'@
& $psql -U postgres -h 127.0.0.1 -p $Port -d postgres -c $roleSql 2>&1 | Out-Host

Write-Host "[...] Creating database taxflow ..." -ForegroundColor Cyan
& $psql -U postgres -h 127.0.0.1 -p $Port -d postgres -c "CREATE DATABASE taxflow OWNER taxflow;" 2>&1 | Out-Host

Write-Host "[...] Enabling pgcrypto ..." -ForegroundColor Cyan
& $psql -U postgres -h 127.0.0.1 -p $Port -d taxflow -c "CREATE EXTENSION IF NOT EXISTS pgcrypto;" 2>&1 | Out-Host

$env:PGPASSWORD = "taxflow_dev_password"
$test = & $psql -U taxflow -h 127.0.0.1 -p $Port -d taxflow -c "SELECT 1 AS ok" -t 2>&1
Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue

if ($LASTEXITCODE -ne 0 -or ($test -notmatch "1")) {
    Write-Host "[FAIL] taxflow user cannot connect." -ForegroundColor Red
    Write-Host $test
    exit 1
}

# Ensure .env has DB settings
$keep = @()
if (Test-Path "$root\.env") {
    Get-Content "$root\.env" -Encoding UTF8 | ForEach-Object {
        if ($_ -match "^\s*DB_") { return }
        $keep += $_
    }
}
$dbBlock = @(
    "",
    "# DB (auto-filled by init-db.ps1)",
    "DB_HOST=127.0.0.1",
    "DB_PORT=$Port",
    "DB_NAME=taxflow",
    "DB_USERNAME=taxflow",
    "DB_PASSWORD=taxflow_dev_password"
)
($keep + $dbBlock) | Set-Content "$root\.env" -Encoding UTF8

Write-Host ""
Write-Host "[OK] Database ready." -ForegroundColor Green
Write-Host "     user: taxflow  password: taxflow_dev_password  db: taxflow  port: $Port" -ForegroundColor Green
Write-Host "     .env updated with DB_* variables." -ForegroundColor Green
Write-Host ""
Write-Host "Next:  .\scripts\boot-dev.ps1" -ForegroundColor Cyan
Write-Host ""

# Verify taxflow DB connection using .env
# Usage: .\scripts\test-db.ps1

$root = Split-Path -Parent $PSScriptRoot

function Load-DotEnv([string]$path) {
    if (-not (Test-Path $path)) { return $false }
    Get-Content $path -Encoding UTF8 | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq "" -or $line.StartsWith("#")) { return }
        $eq = $line.IndexOf("=")
        if ($eq -lt 1) { return }
        Set-Item -Path "Env:$($line.Substring(0,$eq).Trim())" $line.Substring($eq + 1).Trim()
    }
    return $true
}

if (-not (Load-DotEnv "$root\.env")) {
    Write-Host "[FAIL] No .env file. Run: copy .env.example .env" -ForegroundColor Red
    exit 1
}

$dbHost = if ($env:DB_HOST) { $env:DB_HOST } else { "127.0.0.1" }
$port = if ($env:DB_PORT) { $env:DB_PORT } else { "5432" }
$db = if ($env:DB_NAME) { $env:DB_NAME } else { "taxflow" }
$user = if ($env:DB_USERNAME) { $env:DB_USERNAME } else { "taxflow" }
$pass = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "taxflow_dev_password" }

$psql = @(
    "C:\Program Files\PostgreSQL\18\bin\psql.exe",
    "C:\Program Files\PostgreSQL\16\bin\psql.exe"
) | Where-Object { Test-Path $_ } | Select-Object -First 1

if (-not $psql) {
    Write-Host "[WARN] psql not found; skipping CLI test." -ForegroundColor Yellow
    exit 0
}

$env:PGPASSWORD = $pass
$result = & $psql -U $user -h $dbHost -p $port -d $db -c "SELECT 1 AS ok" -t 2>&1
Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue

if ($LASTEXITCODE -eq 0 -and ($result -match "1")) {
    Write-Host "[OK] DB connection: $user@${dbHost}:$port/$db" -ForegroundColor Green
    exit 0
}

Write-Host "[FAIL] DB connection failed: $user@${dbHost}:$port/$db" -ForegroundColor Red
Write-Host $result
Write-Host "Run:  .\scripts\init-db.ps1" -ForegroundColor Yellow
exit 1

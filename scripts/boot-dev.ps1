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

Write-Host "[...] Starting backend (Flyway migrations run on first boot) ..." -ForegroundColor Cyan
.\gradlew.bat :taxflow-app:bootRun --args="--spring.profiles.active=dev"

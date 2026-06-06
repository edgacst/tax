# TaxFlow 로컬 한 번에 실행 (Windows)
# 사용법:  .\scripts\start-dev.ps1
# 사전: Docker Desktop 실행, Node.js 설치, .env 파일에 인증키 설정

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

function Load-DotEnv([string]$path) {
    if (-not (Test-Path $path)) { return }
    Get-Content $path | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq "" -or $line.StartsWith("#")) { return }
        $eq = $line.IndexOf("=")
        if ($eq -lt 1) { return }
        $name = $line.Substring(0, $eq).Trim()
        $value = $line.Substring($eq + 1).Trim()
        Set-Item -Path "Env:$name" -Value $value
    }
}

if (-not (Test-Path "$root\.env")) {
    Write-Host ""
    Write-Host "  [1회만] copy .env.example .env  후 .env 에 공공데이터포털 인증키를 넣으세요." -ForegroundColor Yellow
    Write-Host ""
}

Load-DotEnv "$root\.env"

Write-Host "Postgres 시작..." -ForegroundColor Cyan
docker compose up -d postgres
if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker가 꺼져 있거나 docker compose 실패. Docker Desktop을 켜고 다시 실행하세요." -ForegroundColor Red
    exit 1
}

Write-Host "백엔드(8080) 새 창..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    "-NoExit", "-Command",
    "Set-Location '$root'; if (Test-Path '.env') { Get-Content '.env' | ForEach-Object { if (`$_ -match '^\s*#' -or `$_ -notmatch '=') { return }; `$i=`$_ IndexOf('='); Set-Item Env:`$(`$_.Substring(0,`$i).Trim()) `$_.Substring(`$i+1).Trim() } }; .\gradlew.bat :taxflow-app:bootRun --args='--spring.profiles.active=dev'"
)

Start-Sleep -Seconds 2
Write-Host "프론트(5173) 새 창..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @("-NoExit", "-Command", "Set-Location '$root\frontend'; npm run dev")

Start-Sleep -Seconds 3
Start-Process "http://127.0.0.1:5173/tools/biz-verify"

Write-Host ""
Write-Host "  브라우저를 열었습니다." -ForegroundColor Green
Write-Host "  백엔드 창에 'Started TaxFlowApplication' 이 보일 때까지 30초~1분 기다린 뒤 조회하세요." -ForegroundColor Green
Write-Host "  502 나오면 = 아직 백엔드 기동 중. 잠시 후 새로고침." -ForegroundColor Yellow
Write-Host ""

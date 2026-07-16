# 企业级OA系统V2 - 一键启动 (Windows)
# 1. 初始化数据库  2. 启动后端  3. 启动前端

param(
    [switch]$SkipDb
)

$ErrorActionPreference = "Stop"
$OaRoot = $PSScriptRoot
$ProjectDir = Join-Path $OaRoot "springboot"
$FrontendDir = Join-Path $ProjectDir "src\main\resources\admin\admin"
$LogFile = Join-Path $OaRoot "springboot.log"

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  企业级OA V2 - 一键启动" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

# 1. 初始化数据库
if (-not $SkipDb) {
    Write-Host "`n[1/3] 初始化数据库..." -ForegroundColor Yellow
    $sqlFile = Join-Path $OaRoot "db-v2.sql"
    if (Test-Path $sqlFile) {
        & mysql -uroot -p123456 < $sqlFile
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✓ 数据库初始化完成" -ForegroundColor Green
        } else {
            Write-Host "  ✗ 数据库初始化失败" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "  ✗ 找不到 $sqlFile" -ForegroundColor Red
        exit 1
    }
}

# 2. 启动后端
Write-Host "`n[2/3] 启动Spring Boot后端..." -ForegroundColor Yellow
Set-Location $ProjectDir
$job = Start-Job -ScriptBlock {
    Set-Location $using:ProjectDir
    & mvn spring-boot:run -q
} -Name "OABackend"

Write-Host "  等待后端启动 (最多60秒)..." -ForegroundColor Gray
$ready = $false
for ($i = 1; $i -le 30; $i++) {
    Start-Sleep -Seconds 2
    try {
        $resp = Invoke-WebRequest -Uri "http://localhost:8080/springboot-oa-v2/users/session-test" -TimeoutSec 3 -ErrorAction Stop
        if ($resp.StatusCode -eq 200) {
            $ready = $true
            break
        }
    } catch {}
    Write-Host "." -NoNewline
}
Write-Host ""

if ($ready) {
    Write-Host "  ✓ 后端启动成功" -ForegroundColor Green
} else {
    Write-Host "  ✗ 后端启动超时, 请检查日志" -ForegroundColor Red
    Stop-Job $job -ErrorAction SilentlyContinue
    Remove-Job $job -ErrorAction SilentlyContinue
    exit 1
}

# 3. 启动前端
Write-Host "`n[3/3] 启动前端..." -ForegroundColor Yellow
if (Test-Path $FrontendDir) {
    Set-Location $FrontendDir
    if (-not (Test-Path "node_modules")) {
        Write-Host "  安装前端依赖 (首次较慢)..." -ForegroundColor Gray
        npm install
    }
    Start-Process -FilePath "cmd" -ArgumentList "/c npm run serve" -WindowStyle Normal
    Write-Host "  ✓ 前端启动命令已发出" -ForegroundColor Green
} else {
    Write-Host "  ✗ 找不到前端目录 $FrontendDir" -ForegroundColor Red
}

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "  后端进程仍在运行 (Job ID: $($job.Id))" -ForegroundColor Yellow
Write-Host "  查看日志: Receive-Job -Id $($job.Id) -Keep" -ForegroundColor Yellow
Write-Host "  停止后端: Stop-Job -Id $($job.Id); Remove-Job -Id $($job.Id)" -ForegroundColor Yellow
Write-Host "  后端API: http://localhost:8080/springboot-oa-v2" -ForegroundColor Cyan
Write-Host "  前端:    http://localhost:8081" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

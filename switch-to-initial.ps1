# 초기 프로젝트 버전으로 전환하는 스크립트
Write-Host "초기 프로젝트 버전으로 전환 중..." -ForegroundColor Yellow

# 작업 디렉토리가 깨끗한지 확인
$status = git status --porcelain
if ($status) {
    Write-Host "경고: 작업 디렉토리에 변경사항이 있습니다." -ForegroundColor Red
    Write-Host "변경사항을 저장하거나 버리시겠습니까? (y/n): " -NoNewline
    $response = Read-Host
    if ($response -eq "y") {
        Write-Host "변경사항을 stash에 저장합니다..." -ForegroundColor Yellow
        git stash push -m "Auto-stash before switching to initial-project"
    } else {
        Write-Host "작업을 취소합니다." -ForegroundColor Red
        exit 1
    }
}

# 초기 프로젝트 버전으로 checkout
git checkout initial-project

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ 초기 프로젝트 버전으로 성공적으로 전환되었습니다!" -ForegroundColor Green
    Write-Host "현재 커밋: " -NoNewline
    git log -1 --oneline
} else {
    Write-Host "✗ 전환 실패" -ForegroundColor Red
    exit 1
}


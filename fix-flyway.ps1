# Flyway 마이그레이션 오류 수정 스크립트
Write-Host "Flyway 마이그레이션 오류를 수정합니다..." -ForegroundColor Yellow

# Flyway repair 실행
Write-Host "`n1. Flyway repair 실행 중..." -ForegroundColor Cyan
mvn flyway:repair

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✓ Flyway repair가 성공적으로 완료되었습니다!" -ForegroundColor Green
    Write-Host "`n이제 애플리케이션을 실행할 수 있습니다." -ForegroundColor Green
    Write-Host "실행: mvn spring-boot:run" -ForegroundColor Cyan
} else {
    Write-Host "`n✗ Flyway repair 실패" -ForegroundColor Red
    Write-Host "`n대안: 데이터베이스에서 직접 수정이 필요할 수 있습니다." -ForegroundColor Yellow
    Write-Host "MySQL에 접속하여 다음 쿼리를 실행하세요:" -ForegroundColor Yellow
    Write-Host "DELETE FROM flyway_schema_history WHERE success = 0 AND version = '9';" -ForegroundColor Cyan
    Write-Host "또는" -ForegroundColor Yellow
    Write-Host "UPDATE flyway_schema_history SET success = 1 WHERE version = '9' AND success = 0;" -ForegroundColor Cyan
    exit 1
}


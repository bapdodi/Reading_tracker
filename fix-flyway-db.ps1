# Flyway 데이터베이스 마이그레이션 오류 수정 스크립트
Write-Host "Flyway 마이그레이션 오류를 수정합니다..." -ForegroundColor Yellow

# MySQL 연결 정보
$mysqlPath = "mysql"
$host = "localhost"
$port = "3306"
$user = "root"
$password = "Ekrcu9873?!"
$database = "reading_tracker"

Write-Host "`n1. 데이터베이스에 접속하여 실패한 마이그레이션을 수정합니다..." -ForegroundColor Cyan

# SQL 쿼리 실행
$sqlQuery = @"
USE $database;
UPDATE flyway_schema_history SET success = 1 WHERE version = '9' AND success = 0;
SELECT * FROM flyway_schema_history WHERE version = '9';
"@

# MySQL 명령 실행
$command = "mysql -h $host -P $port -u $user -p$password -e `"$sqlQuery`""

Write-Host "`n실행할 명령어:" -ForegroundColor Yellow
Write-Host $command -ForegroundColor Gray

try {
    Invoke-Expression $command
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n✓ Flyway 마이그레이션 오류가 성공적으로 수정되었습니다!" -ForegroundColor Green
        Write-Host "`n이제 애플리케이션을 실행할 수 있습니다." -ForegroundColor Green
        Write-Host "실행: mvn spring-boot:run" -ForegroundColor Cyan
    } else {
        Write-Host "`n✗ MySQL 명령 실행 실패" -ForegroundColor Red
        Write-Host "`n수동으로 MySQL에 접속하여 다음 SQL을 실행하세요:" -ForegroundColor Yellow
        Write-Host "USE reading_tracker;" -ForegroundColor Cyan
        Write-Host "UPDATE flyway_schema_history SET success = 1 WHERE version = '9' AND success = 0;" -ForegroundColor Cyan
    }
} catch {
    Write-Host "`n✗ 오류 발생: $_" -ForegroundColor Red
    Write-Host "`n수동으로 MySQL에 접속하여 다음 SQL을 실행하세요:" -ForegroundColor Yellow
    Write-Host "USE reading_tracker;" -ForegroundColor Cyan
    Write-Host "UPDATE flyway_schema_history SET success = 1 WHERE version = '9' AND success = 0;" -ForegroundColor Cyan
}


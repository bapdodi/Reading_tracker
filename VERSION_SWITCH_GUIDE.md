# 버전 전환 가이드

이 프로젝트에는 두 가지 중요한 버전이 태그로 설정되어 있습니다:

- **initial-project**: 초기 프로젝트 커밋 버전
- **fix-dto**: Fix: DTO 패키지명 수정 버전

## 빠른 전환 방법

### 방법 1: PowerShell 스크립트 사용 (권장)

1. **초기 프로젝트 버전으로 전환:**
   ```powershell
   .\switch-to-initial.ps1
   ```

2. **Fix: DTO 버전으로 복원:**
   ```powershell
   .\switch-to-fix-dto.ps1
   ```

### 방법 2: Git 명령어 직접 사용

1. **초기 프로젝트 버전으로 전환:**
   ```powershell
   git checkout initial-project
   ```

2. **Fix: DTO 버전으로 복원:**
   ```powershell
   git checkout fix-dto
   ```

3. **main 브랜치로 돌아가기:**
   ```powershell
   git checkout main
   ```

## 주의사항

⚠️ **중요**: 버전을 전환하기 전에 작업 디렉토리에 변경사항이 있다면:
- 저장하려면: `git stash` 사용
- 버리려면: `git checkout -- .` 또는 `git reset --hard`

## 현재 버전 확인

```powershell
git log -1 --oneline
git tag --points-at HEAD
```

## 태그 정보 확인

```powershell
git tag -l
git show initial-project
git show fix-dto
```


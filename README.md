# Reading Tracker

독서 기록 사이트

## 프로젝트 개요

- **프로젝트명**: Reading Tracker (독서 기록 사이트)
- **기술 스택**: Spring Boot 3.2.0, Java 17, MySQL, JPA, Flyway
- **데이터베이스**: MySQL 8.0

## 환경 설정

### .env 파일 설정

`.env` 파일은 개발 환경 기본값과 동일하게 설정되어 있습니다. 프로젝트 루트 디렉토리에 `.env` 파일을 생성하여 환경 변수를 설정할 수 있습니다.

#### 환경 변수 목록

| 환경 변수 | 설명 | 기본값 (개발 환경) |
|----------|------|------------------|
| `DB_URL` | 데이터베이스 연결 URL | `jdbc:mysql://localhost:3306/reading_tracker?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `DB_USERNAME` | 데이터베이스 사용자명 | `root` |
| `DB_PASSWORD` | 데이터베이스 비밀번호 | `Ekrcu9873?!` |
| `JWT_SECRET` | JWT 토큰 시크릿 키 | `mySecretKey1234567890123456789012345678901234567890123456789012345678901234567890` |
| `FRONTEND_URL` | 프론트엔드 URL | `http://localhost:3000` |
| `ALADIN_API_KEY` | 알라딘 API 키 | `ttbdpsk15151102001` |
| `SPRING_PROFILES_ACTIVE` | Spring Boot 프로파일 | `dev` |

#### .env 파일 생성 방법

1. 프로젝트 루트 디렉토리에서 `.env.example` 파일을 복사하여 `.env` 파일을 생성합니다:
   ```bash
   cp .env.example .env
   ```

2. `.env` 파일을 열어 실제 값으로 수정합니다. 개발 환경의 경우 기본값과 동일하게 설정하는 것을 권장합니다.

3. `.env` 파일은 Git에 커밋되지 않습니다 (`.gitignore`에 포함되어 있음).

#### 참고 사항

- **개발 환경**: `.env` 파일의 값들을 `application-dev.yml`의 기본값과 동일하게 설정하는 것을 권장합니다.
- **프로덕션 환경**: 프로덕션 환경에서는 환경 변수가 필수이며, 각 환경에 맞는 실제 비밀번호와 API 키를 설정해야 합니다.
- `.env.example` 파일은 템플릿 파일로 Git에 커밋되며, 팀원들이 참고할 수 있습니다.
- 환경 변수 우선순위: 환경 변수 값 > 기본값

## 실행 방법

### 개발 환경 실행

```bash
# 기본값으로 개발 환경 실행 (application.yml의 spring.profiles.active: dev)
mvn spring-boot:run
```

### 프로덕션 환경 실행

```bash
# 환경 변수 설정 필수
$env:DB_PASSWORD="your_production_password"
$env:JWT_SECRET="your_production_secret"
# ... 기타 환경 변수 설정

# 프로덕션 환경으로 실행
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## API 문서

애플리케이션 실행 후 다음 URL에서 Swagger UI를 확인할 수 있습니다:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI 문서: `http://localhost:8080/v3/api-docs`

## 프로젝트 구조

자세한 프로젝트 구조와 아키텍처는 [ARCHITECTURE.md](ARCHITECTURE.md)를 참고하세요.


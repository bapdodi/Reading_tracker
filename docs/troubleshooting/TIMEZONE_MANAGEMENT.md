# 시간대(Timezone) 관리 문제 및 개선

## 문제 정의

메모 작성 시 시간 필드값(`memoStartTime`, `createdAt`, `updatedAt`)이 한국 시간대(KST, UTC+9)가 아닌 UTC 시간대로 저장되는 문제가 발생했습니다.

### 증상

- 사용자가 한국 시간 `2024-01-01 15:00:00`에 메모를 작성했을 때
- 데이터베이스에 저장되는 시간: `2024-01-01 06:00:00` (UTC, 9시간 차이)
- 사용자가 확인하는 시간과 실제 저장된 시간이 불일치

---

## 원인 분석

### 1. `memoStartTime` 필드

#### 문제점
- **프론트엔드**: `new Date().toISOString()` 사용
  - `toISOString()`은 브라우저의 로컬 시간을 UTC로 변환한 ISO 8601 문자열을 반환
  - 한국 시간(KST, UTC+9)에서 실행 시 9시간이 빼진 UTC 시간이 전송됨
  - 예: 한국 시간 `15:00:00` → UTC `06:00:00`으로 전송

- **백엔드**: `LocalDateTime`으로 파싱
  - Jackson이 ISO 8601 문자열을 `LocalDateTime`으로 파싱
  - `LocalDateTime`은 타임존 정보가 없으므로 파싱된 값을 그대로 사용
  - UTC 시간 문자열이 그대로 `LocalDateTime`으로 변환되어 타임존 정보 손실

#### 코드 위치
- 프론트엔드: `분산2_프로젝트_프론트/js/views/pages/flow-view.js:1405`
  ```javascript
  memoStartTime: new Date().toISOString()
  ```

- 백엔드: `MemoCreateRequest.java:25`
  ```java
  private LocalDateTime memoStartTime;
  ```

---

### 2. `createdAt` 필드

#### 문제점
- **백엔드**: JPA `@CreatedDate`로 자동 설정
  - `LocalDateTime.now()` 사용 (서버 타임존 기준)
  - 서버 타임존이 한국 시간이 아니면 오프셋 발생 가능
  - JVM 기본 타임존 사용 (보통 서버 OS 타임존)

#### 코드 위치
- 백엔드: `Memo.java:58-60`
  ```java
  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
  ```

---

### 3. `updatedAt` 필드

#### 문제점
- **백엔드**: JPA `@LastModifiedDate`로 자동 설정
  - `LocalDateTime.now()` 사용 (서버 타임존 기준)
  - 서버 타임존이 한국 시간이 아니면 오프셋 발생 가능

#### 코드 위치
- 백엔드: `Memo.java:62-64`
  ```java
  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
  ```

---

### 4. 데이터베이스 타임존 설정

#### 문제점
- **MySQL 연결**: `serverTimezone=UTC`로 설정
  - 데이터베이스가 UTC 시간대로 동작
  - 한국 시간대와 9시간 차이

#### 코드 위치
- 설정 파일: `application-dev.yml:3`
  ```yaml
  serverTimezone=UTC
  ```

---

## 시간 필드 생성 위치 결정

### `memoStartTime` (메모 시작 시간)

**현재 위치**: 프론트엔드에서 생성
- **목적**: 비즈니스 로직 (사용자가 메모 작성을 시작한 시간)
- **결정**: 프론트엔드에서 생성 유지
- **이유**: 사용자 경험 측면에서 사용자가 메모 작성을 시작한 시점을 기록하는 것이 적합

**개선 방안**: 한국 시간대 기준으로 ISO 문자열 생성

---

### `createdAt` (DB 레코드 생성 시간)

**현재 위치**: 백엔드에서 자동 생성 (JPA Auditing)
- **목적**: 기술적 메타데이터 (감사 목적)
- **결정**: 백엔드에서 생성 유지
- **이유**:
  1. **보안**: 클라이언트에서 조작 불가능
  2. **신뢰성**: 서버 시간 기준으로 실제 DB 저장 시점 기록
  3. **감사 목적**: 데이터 변경 이력 추적, 보안 감사, 문제 발생 시 원인 분석
  4. **정확성**: 네트워크 지연, 서버 처리 시간 등을 고려한 실제 저장 시점

**개선 방안**: 한국 시간대 기준으로 `LocalDateTime.now()` 설정

---

### `updatedAt` (DB 레코드 수정 시간)

**현재 위치**: 백엔드에서 자동 생성 (JPA Auditing)
- **목적**: 기술적 메타데이터 (감사 목적)
- **결정**: 백엔드에서 생성 유지
- **이유**: `createdAt`과 동일한 이유

**개선 방안**: 한국 시간대 기준으로 `LocalDateTime.now()` 설정

---

## 개선 방안

### 방안 1: 프론트엔드 + 백엔드 + DB 모두 한국 시간대 적용 (채택)

#### 1. 프론트엔드 수정
- **목표**: 한국 시간대 기준 ISO 문자열 생성
- **위치**: `flow-view.js:1405`
- **변경 내용**: `toISOString()` 대신 한국 시간대 기준으로 ISO 문자열 생성

#### 2. 백엔드 서버 타임존 설정
- **목표**: JVM 기본 타임존을 한국 시간대로 설정
- **위치**: 새로운 `TimeZoneConfig.java` 생성 또는 `JpaConfig.java`에 추가
- **변경 내용**: `@PostConstruct`에서 `TimeZone.setDefault()` 호출

#### 3. JPA Auditing 커스터마이징
- **목표**: `@CreatedDate`, `@LastModifiedDate`가 한국 시간대 기준으로 설정
- **위치**: `JpaConfig.java`에 추가
- **변경 내용**: 커스텀 `DateTimeProvider` 구현

#### 4. 데이터베이스 타임존 설정
- **목표**: MySQL 연결 시 한국 시간대 사용
- **위치**: `application-dev.yml:3`
- **변경 내용**: `serverTimezone=UTC` → `serverTimezone=Asia/Seoul`

---

## 구현 세부사항

### 1. 프론트엔드: 한국 시간대 기준 ISO 문자열 생성

```javascript
// 변경 전
memoStartTime: new Date().toISOString()

// 변경 후
memoStartTime: (() => {
  const now = new Date();
  const koreaTime = new Date(now.toLocaleString('en-US', { timeZone: 'Asia/Seoul' }));
  return koreaTime.toISOString();
})()
```

또는 더 간단하게:
```javascript
memoStartTime: new Date(new Date().toLocaleString('en-US', { timeZone: 'Asia/Seoul' })).toISOString()
```

---

### 2. 백엔드: 서버 타임존 설정

```java
@Configuration
public class TimeZoneConfig {
    @PostConstruct
    public void setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
```

---

### 3. JPA Auditing: 한국 시간대 기준 DateTimeProvider

```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    
    @Bean
    public DateTimeProvider dateTimeProvider() {
        return () -> LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }
}
```

---

### 4. 데이터베이스: MySQL 타임존 설정

```yaml
# 변경 전
serverTimezone=UTC

# 변경 후
serverTimezone=Asia/Seoul
```

---

## 예상 효과

### 변경 전
- 한국 시간 `2024-01-01 15:00:00`에 메모 작성
- `memoStartTime`: `2024-01-01T06:00:00` (UTC, 9시간 차이)
- `createdAt`: 서버 타임존 기준 (한국 시간이 아닐 수 있음)
- `updatedAt`: 서버 타임존 기준 (한국 시간이 아닐 수 있음)

### 변경 후
- 한국 시간 `2024-01-01 15:00:00`에 메모 작성
- `memoStartTime`: `2024-01-01T15:00:00` (한국 시간대)
- `createdAt`: `2024-01-01 15:00:00` (한국 시간대)
- `updatedAt`: `2024-01-01 15:00:00` (한국 시간대)

---

## 주의사항

1. **기존 데이터**: 이미 저장된 UTC 시간 데이터는 그대로 유지됨 (변환 불가)
2. **서버 재시작**: 타임존 설정이 적용되려면 서버 재시작 필요
3. **테스트**: 변경 후 시간 저장이 정상인지 확인 필요
4. **다른 서비스 영향**: 서버 타임존 변경은 다른 기능에도 영향을 줄 수 있으므로 주의 필요

---

## 참고사항

- 한국 시간대(KST, UTC+9)는 일광절약시간을 사용하지 않으므로 `Asia/Seoul`로 고정 사용 가능
- 향후 다른 국가 지원이 필요한 경우, 사용자별 타임존 설정을 고려해야 함
- 현재는 한국 사용자만을 대상으로 하므로 단순화된 구현 채택

---

## 관련 파일

- 프론트엔드: `분산2_프로젝트_프론트/js/views/pages/flow-view.js`
- 백엔드 Entity: `분산2_프로젝트/src/main/java/com/readingtracker/server/dbms/entity/Memo.java`
- 백엔드 DTO: `분산2_프로젝트/src/main/java/com/readingtracker/server/dto/requestDTO/MemoCreateRequest.java`
- 백엔드 Config: `분산2_프로젝트/src/main/java/com/readingtracker/server/config/JpaConfig.java`
- DB 설정: `분산2_프로젝트/src/main/resources/application-dev.yml`

---

## 작성일

2024년 작성


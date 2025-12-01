# 순환 참조(Circular Reference) 문제 해결

## 문제 상황

로그인 성능 최적화를 위해 `JwtService`에 비동기 처리를 추가한 후, Spring Boot 애플리케이션 실행 시 다음과 같은 에러가 발생했습니다:

```
***************************
APPLICATION FAILED TO START
***************************

Description:

The dependencies of some of the beans in the application context form a cycle:  

   authController (field private com.readingtracker.server.service.AuthService com.readingtracker.server.controller.v1.AuthController.authService)              
      ↓
   authService (field private com.readingtracker.server.service.JwtService com.readingtracker.server.service.AuthService.jwtService)                            
┌─────┐
|  jwtService (field private com.readingtracker.server.service.JwtService com.readingtracker.server.service.JwtService.self)                                    
└─────┘

Action:

Relying upon circular references is discouraged and they are prohibited by default. Update your application to remove the dependency cycle between beans. As a last resort, it may be possible to break the cycle automatically by setting spring.main.allow-circular-references to true.
```

## 원인 분석

### 1. 문제 발생 배경

로그인 성능 최적화를 위해 `JwtService.generateTokens()` 메서드에서 디바이스 정보와 Refresh Token 저장을 비동기로 처리하도록 변경했습니다. Spring의 `@Async` 어노테이션은 프록시를 통해 동작하므로, 자기 자신의 메서드를 호출할 때는 프록시를 거쳐야 합니다.

### 2. 순환 참조 발생 원인

**문제가 된 코드**:
```java
@Service
@Transactional
public class JwtService {
    @Autowired
    private JwtService self;  // 자기 자신을 주입받으려고 시도
    
    public TokenResult generateTokens(...) {
        self.saveOrUpdateDeviceAsync(...);
        self.saveRefreshTokenAsync(...);
    }
}
```

**의존성 체인**:
1. `AuthController` → `AuthService` 주입
2. `AuthService` → `JwtService` 주입
3. `JwtService` → `JwtService` (자기 자신) 주입 시도
4. **순환 참조 발생**: `JwtService`가 아직 생성 중인데 자기 자신을 주입받으려고 함

### 3. Spring의 순환 참조 정책

Spring Boot 2.6부터 순환 참조가 기본적으로 금지되었습니다. 이는 다음과 같은 이유 때문입니다:

- **초기화 순서 문제**: 빈 생성 순서가 불명확해짐
- **의존성 관리 복잡도 증가**: 순환 참조는 설계 문제의 신호
- **테스트 어려움**: 순환 참조가 있는 코드는 테스트하기 어려움

## 해결 방법

### 해결책 1: `@Lazy` 어노테이션 사용 (권장)

**수정된 코드**:
```java
@Service
@Transactional
public class JwtService {
    @Autowired
    @Lazy
    private JwtService self;  // @Lazy로 지연 주입
    
    public TokenResult generateTokens(...) {
        self.saveOrUpdateDeviceAsync(...);
        self.saveRefreshTokenAsync(...);
    }
}
```

**작동 원리**:
- `@Lazy`는 빈을 실제로 사용할 때까지 주입을 지연시킵니다
- `self` 필드에 접근할 때 프록시 객체가 주입됩니다
- 순환 참조 문제를 해결하면서도 프록시를 통해 `@Async`가 정상 동작합니다

**장점**:
- ✅ 구현이 간단하고 명확함
- ✅ Spring 프록시를 통해 `@Async` 정상 동작 보장
- ✅ 성능 오버헤드 없음
- ✅ Spring에서 권장하는 패턴

**단점**:
- ⚠️ 자기 참조가 명시적으로 드러남 (하지만 이는 의도된 설계)

### 해결책 2: `ApplicationContext`를 통한 조회

**대안 코드**:
```java
@Service
@Transactional
public class JwtService {
    @Autowired
    private ApplicationContext applicationContext;
    
    public TokenResult generateTokens(...) {
        JwtService self = applicationContext.getBean(JwtService.class);
        self.saveOrUpdateDeviceAsync(...);
        self.saveRefreshTokenAsync(...);
    }
}
```

**장점**:
- ✅ 순환 참조 문제 해결
- ✅ 자기 참조 필드 불필요

**단점**:
- ❌ `ApplicationContext` 의존성 추가 (불필요한 의존성)
- ❌ `getBean()` 호출 시 런타임 오버헤드
- ❌ 타입 안전성 저하 (런타임 에러 가능성)
- ❌ 테스트 시 `ApplicationContext` 모킹 필요
- ❌ 코드 가독성 저하

### 해결책 3: 별도 서비스로 분리

**대안 코드**:
```java
@Service
public class JwtAsyncService {
    @Async("taskExecutor")
    @Transactional
    public void saveOrUpdateDeviceAsync(User user, String deviceId, String deviceName, String platform) {
        // 구현
    }
    
    @Async("taskExecutor")
    @Transactional
    public void saveRefreshTokenAsync(User user, String deviceId, String refreshToken) {
        // 구현
    }
}

@Service
public class JwtService {
    @Autowired
    private JwtAsyncService jwtAsyncService;
    
    public TokenResult generateTokens(...) {
        jwtAsyncService.saveOrUpdateDeviceAsync(...);
        jwtAsyncService.saveRefreshTokenAsync(...);
    }
}
```

**장점**:
- ✅ 순환 참조 완전 제거
- ✅ 관심사 분리 (비동기 로직 분리)
- ✅ 테스트 용이성 향상

**단점**:
- ❌ 클래스 분리로 인한 복잡도 증가
- ❌ 비동기 메서드만을 위한 별도 서비스 생성 (과도한 분리 가능성)
- ❌ 코드 변경 범위 증가

### 해결책 4: `spring.main.allow-circular-references=true` 설정 (비권장)

**application.yml**:
```yaml
spring:
  main:
    allow-circular-references: true
```

**장점**:
- ✅ 코드 수정 불필요

**단점**:
- ❌ **근본적인 해결책이 아님** (순환 참조를 허용할 뿐)
- ❌ Spring Boot에서 권장하지 않는 방법
- ❌ 설계 문제를 은폐하는 것
- ❌ 향후 유지보수 시 문제 발생 가능성

## 최종 선택: `@Lazy` 사용

### 선택 이유

1. **간단하고 명확함**: 최소한의 코드 변경으로 문제 해결
2. **Spring 프록시 보장**: `@Async`가 정상 동작함을 보장
3. **성능**: 추가 오버헤드 없음
4. **Spring 권장 패턴**: Spring 공식 문서에서 권장하는 방법
5. **유지보수성**: 코드가 명확하고 이해하기 쉬움

### 다른 방법과의 비교

| 방법 | 복잡도 | 성능 | 유지보수성 | Spring 권장 | 순환 참조 해결 |
|------|--------|------|-----------|-------------|---------------|
| `@Lazy` | ⭐ 낮음 | ⭐⭐⭐ 우수 | ⭐⭐⭐ 우수 | ✅ 권장 | ✅ 완전 해결 |
| `ApplicationContext` | ⭐⭐ 중간 | ⭐⭐ 보통 | ⭐⭐ 보통 | ❌ 비권장 | ✅ 완전 해결 |
| 별도 서비스 분리 | ⭐⭐⭐ 높음 | ⭐⭐⭐ 우수 | ⭐⭐⭐ 우수 | ✅ 권장 | ✅ 완전 해결 |
| `allow-circular-references` | ⭐ 낮음 | ⭐⭐⭐ 우수 | ⭐ 낮음 | ❌ 비권장 | ❌ 은폐만 함 |

## 수정된 코드

### 파일: `src/main/java/com/readingtracker/server/service/JwtService.java`

**변경 전**:
```java
@Service
@Transactional
public class JwtService {
    @Autowired
    private JwtService self;  // 순환 참조 발생
    
    // ...
}
```

**변경 후**:
```java
import org.springframework.context.annotation.Lazy;

@Service
@Transactional
public class JwtService {
    // 자기 자신 주입 (비동기 메서드 호출을 위해 필요)
    // @Lazy를 사용하여 순환 참조 방지
    @Autowired
    @Lazy
    private JwtService self;
    
    // ...
}
```

## 검증

### 1. 애플리케이션 시작 확인

수정 후 애플리케이션이 정상적으로 시작되는지 확인:

```bash
mvn spring-boot:run
```

**예상 결과**:
- 순환 참조 에러 없이 애플리케이션이 정상 시작됨
- `JwtService` 빈이 정상적으로 생성됨
- 비동기 메서드가 프록시를 통해 정상 동작함

### 2. 로그인 기능 테스트

로그인 기능이 정상적으로 동작하는지 확인:

1. 로그인 API 호출
2. 토큰이 정상적으로 생성되는지 확인
3. 디바이스 정보와 Refresh Token이 비동기로 저장되는지 확인

## 추가 고려사항

### 1. `@Lazy`의 동작 방식

- `@Lazy`는 빈을 실제로 사용할 때까지 주입을 지연시킵니다
- 첫 번째 접근 시 프록시 객체가 주입되고, 이후에는 동일한 인스턴스를 사용합니다
- Spring의 프록시 메커니즘을 통해 `@Async`가 정상 동작합니다

### 2. 테스트 시 주의사항

`@Lazy`를 사용한 경우 테스트 시에도 동일한 방식으로 주입해야 합니다:

```java
@SpringBootTest
class JwtServiceTest {
    @Autowired
    @Lazy
    private JwtService jwtService;
    
    // 테스트 코드
}
```

### 3. 성능 영향

`@Lazy`는 첫 번째 접근 시 약간의 오버헤드가 있지만, 이후에는 일반 필드 주입과 동일한 성능을 보입니다. 비동기 메서드 호출 시점에는 이미 빈이 생성되어 있으므로 성능 영향은 미미합니다.

## 참고 자료

- [Spring Framework - Circular Dependencies](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html#beans-factory-collaborators-circular-dependencies)
- [Spring Boot - Circular References](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.6-Release-Notes#circular-references-prohibited-by-default)
- [Spring @Async Documentation](https://docs.spring.io/spring-framework/reference/integration/scheduling.html#scheduling-annotation-support)

## 작성일

2025-01-28





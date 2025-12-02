# 비기능 품질 개선 구현 로드맵

> **목적**: Fault Tolerance(장애 허용) 비기능 품질 개선을 위한 전체 구현 계획 및 로드맵  
> **범위**: 오프라인 메모 동기화, MySQL 이중화, 클라이언트 기능 완성  
> **최종 업데이트**: 2024년

---

## 📋 목차

1. [개요](#개요)
2. [비기능 품질 시나리오](#비기능-품질-시나리오)
3. [구현 순서 및 단계](#구현-순서-및-단계)
4. [시나리오 1: 오프라인 메모 동기화](#시나리오-1-오프라인-메모-동기화)
5. [시나리오 2: MySQL 이중화 및 양방향 동기화](#시나리오-2-mysql-이중화-및-양방향-동기화)
6. [클라이언트 기능 완성](#클라이언트-기능-완성)
7. [권장 해결 로직](#권장-해결-로직)
8. [리스크 관리](#리스크-관리)
9. [참고 자료](#참고-자료)

---

## 개요

본 문서는 Reading Tracker 프로젝트의 비기능 품질 개선을 위한 전체 구현 계획을 정리합니다. 두 가지 주요 Fault Tolerance 시나리오를 단계적으로 구현하여 시스템의 안정성과 가용성을 향상시킵니다.

### 핵심 원칙

1. **단계적 구현**: 복잡한 인프라 변경 전에 클라이언트 기능을 먼저 안정화
2. **리스크 격리**: 각 단계를 독립적으로 검증하여 전체 시스템 안정성 보장
3. **검증 우선**: 각 단계 완료 후 충분한 테스트를 통해 안정성 확보

---

## 비기능 품질 시나리오

### 시나리오 1: 오프라인 메모 동기화

**목적**: 네트워크가 없는 환경에서도 메모를 작성하고, 네트워크 복구 시 자동으로 서버에 동기화

**특징**:
- 클라이언트 측 구현 (웹)
- 백엔드 API 변경 불필요
- Offline-First 아키텍처

**상세 문서**: [OFFLINE_MEMO_SYNC.md](./OFFLINE_MEMO_SYNC.md)

### 시나리오 2: MySQL 이중화 및 양방향 동기화

**목적**: 데이터베이스 장애 시에도 서비스 지속성을 보장하기 위한 Master-Master 구성

**요구사항**:
- 두 개의 Master DBMS 구성
- 각 DBMS에서 Read, Insert, Update, Delete 모두 가능
- 두 DBMS 간 완전한 데이터 동기화 (데이터 무결성 보장)
- Read 작업: 하나의 DB 장애 시 다른 DB에서 읽기
- Write 작업: 하나의 DB에 먼저 실행 → 성공 시 다른 DB에도 실행 → 실패 시 양쪽 모두 롤백

**특징**:
- 서버/인프라 측 구현
- 분산 트랜잭션 관리 필요
- 백엔드 코드 대폭 수정 필요

---

## 구현 순서 및 단계

### 권장 순서: 단계별 순차 진행 (옵션 A)

```
Phase 1: 클라이언트 기능 완성 (2-3주)
    ↓
Phase 2: 인프라 개선 (3-4주)
```

### 단계별 상세 계획

#### Phase 1: 클라이언트 기능 완성

**기간**: 2-3주  
**목표**: 웹 UI 오프라인 동기화 구현

**작업 내용**:
1. 웹 UI 오프라인 동기화 구현
   - IndexedDB 스키마 설계 및 구현
   - 오프라인 메모 작성 기능
   - 동기화 큐 관리
   - 네트워크 복구 감지 및 자동 동기화
   - UI 통합 및 동기화 상태 표시

**완료 기준**:
- [ ] 오프라인 상태에서 메모 작성 가능
- [ ] 네트워크 복구 시 자동 동기화 작동
- [ ] 웹 UI에서 동기화 상태 표시

#### Phase 2: 인프라 개선

**기간**: 3-4주  
**목표**: MySQL Master-Master 구성 및 분산 트랜잭션 관리

**작업 내용**:
1. MySQL Master-Master 구성
   - 두 개의 MySQL 인스턴스 설정
   - 양방향 복제 구성
   - 데이터 동기화 검증

2. 백엔드 코드 수정
   - Connection Pool 다중화 (Primary/Secondary)
   - 커스텀 트랜잭션 매니저 구현
   - 분산 트랜잭션 관리 로직
   - Read Failover 로직
   - Write 동기화 및 롤백 메커니즘

3. 모든 Service 메서드 수정
   - `@Transactional` 메서드 수정 (17개)
   - Read 작업: Failover 로직 추가
   - Write 작업: 2PC 패턴 적용

4. 테스트 및 검증
   - 단위 테스트
   - 통합 테스트
   - 장애 시나리오 테스트

**완료 기준**:
- [ ] 두 MySQL 인스턴스 정상 동작
- [ ] 양방향 복제 정상 작동
- [ ] Read Failover 정상 작동
- [ ] Write 동기화 및 롤백 정상 작동
- [ ] 모든 Service 메서드 수정 완료
- [ ] 장애 시나리오 테스트 통과

---

## 시나리오 1: 오프라인 메모 동기화

### 개요

네트워크가 없는 환경에서 메모를 작성하고, 네트워크 복구 시 자동으로 서버에 동기화하는 기능입니다.

### 아키텍처

```
[사용자 메모 작성]
        ↓
[로컬 저장소에 저장] ← IndexedDB (웹)
        ↓
[UI 즉시 업데이트]
        ↓
[네트워크 상태 확인]
        ├─ 온라인 → [동기화 큐에 추가] → [서버로 전송]
        └─ 오프라인 → [대기 상태 표시]
                            ↓
                    [네트워크 재연결 감지]
                            ↓
                    [대기 중인 메모 동기화]
                            ↓
                    [서버 응답 처리]
                            ├─ 성공 → [로컬 메모 업데이트 (서버 ID)]
                            └─ 실패 → [재시도 큐에 추가]
```

### 구현 방법

#### 웹 (JavaScript)

**기술 스택**:
- IndexedDB: 로컬 저장소
- Service Worker: 네트워크 모니터링 (선택사항)
- `navigator.onLine` API: 네트워크 상태 감지
- 서버 헬스체크: 실제 서버 연결 가능 여부 확인

**주요 컴포넌트**:
1. `IndexedDBManager`: 로컬 저장소 관리
2. `OfflineMemoService`: 오프라인 메모 작성 및 관리
3. `SyncQueueManager`: 동기화 큐 관리
4. `NetworkMonitor`: 네트워크 상태 모니터링 (헬스체크 포함)

#### 네트워크 연결 감지 및 자동 동기화 메커니즘

**핵심 원리**:
- `navigator.onLine` API로 네트워크 어댑터 상태 확인
- `online` / `offline` 이벤트로 네트워크 상태 변경 감지
- **서버 헬스체크**: 실제 서버 연결 가능 여부 확인 (권장)

**구현 방식**:

```javascript
// utils/network-monitor.js
class NetworkMonitor {
    async onNetworkOnline() {
        // 1초 대기 (네트워크 안정화)
        await this.delay(1000);
        
        // 2. 실제 서버 연결 가능 여부 확인 (헬스체크)
        const isServerReachable = await this.checkServerHealth();
        
        if (isServerReachable) {
            // 서버에 실제로 연결 가능 → 동기화 시작
            await offlineMemoService.syncPendingMemos();
        } else {
            // 네트워크는 연결되었지만 서버 접근 불가
            console.warn('네트워크는 연결되었지만 서버에 접근할 수 없습니다.');
            // 재시도 예약
            setTimeout(() => this.onNetworkOnline(), 5000);
        }
    }
    
    /**
     * 서버 헬스체크 (실제 연결 가능 여부 확인)
     */
    async checkServerHealth() {
        try {
            // 간단한 HEAD 요청으로 서버 응답 확인
            const response = await fetch('http://localhost:8080/api/v1/health', {
                method: 'HEAD',
                signal: AbortSignal.timeout(3000)  // 3초 타임아웃
            });
            return response.ok;
        } catch (error) {
            console.error('서버 헬스체크 실패:', error);
            return false;
        }
    }
}
```

**이점**:
- `navigator.onLine`만으로는 Wi-Fi 연결되어 있지만 인터넷 접속 불가 상황을 감지하지 못함
- 헬스체크를 통해 실제 서버 연결 가능 여부를 확인하여 불필요한 동기화 시도 방지
- 서버 접근 불가 시 자동 재시도로 안정성 향상

**상세 구현**: [OFFLINE_MEMO_SYNC.md](./OFFLINE_MEMO_SYNC.md) 참조

### 동기화 전략

1. **낙관적 업데이트**: 메모 작성 즉시 로컬 저장 및 UI 업데이트
2. **자동 재시도**: Exponential Backoff 전략 (5초, 10초, 20초)
3. **순차 동기화**: `memoStartTime` 기준 정렬 후 순차 동기화
4. **부분 실패 처리**: 일부 메모만 실패해도 나머지는 계속 진행

### 백엔드 변경사항

**변경 불필요**: 기존 API 그대로 사용
- `POST /api/v1/memos`: 메모 작성
- `GET /api/v1/memos/books/{userBookId}`: 메모 조회

---

## 시나리오 2: MySQL 이중화 및 양방향 동기화

### 개요

두 개의 Master DBMS를 구성하여 데이터베이스 장애 시에도 서비스 지속성을 보장합니다.

### 요구사항 상세

#### 1. Master-Master 구성
- 두 개의 MySQL 인스턴스를 모두 Master로 설정
- 각 DBMS에서 Read, Insert, Update, Delete 모두 가능

#### 2. 데이터 무결성
- 두 DBMS 간 완전한 데이터 동기화 보장
- MySQL Replication 또는 커스텀 동기화 로직 사용

#### 3. Read 작업 (90% 사용)
- 하나의 DB에서 데이터 읽기 시도
- 실패 시 자동으로 다른 DB에서 읽기 (Failover)
- 사용자에게는 투명하게 처리

#### 4. Write 작업 (10% 사용)
- **Phase 1**: Primary DB에 먼저 실행
- **Phase 2**: 성공 시 Secondary DB에도 동일 작업 실행
- **실패 처리**: 하나의 DB에서 실패 시 양쪽 모두 롤백
- 사용자에게는 try-catch exception 처리로 실패 알림

### 아키텍처

```
[사용자 요청]
        ↓
[Service Layer]
        ↓
    ┌───┴───┐
    │       │
[Read]   [Write]
    │       │
    │   ┌───┴───┐
    │   │       │
    │ [Primary] [Secondary]
    │   │       │
    │   └───┬───┘
    │       │
    │   [2PC Pattern]
    │       │
    │   ├─ 성공 → Commit
    │   └─ 실패 → Rollback (양쪽 모두)
    │
[Failover]
    │
    ├─ Primary 성공 → 반환
    └─ Primary 실패 → Secondary 시도
```

### 구현 방법

#### 옵션 1: MySQL Replication + 커스텀 트랜잭션 관리 (권장)

**장점**:
- MySQL의 네이티브 복제 기능 활용
- 데이터 동기화 자동화
- 커스텀 트랜잭션 관리로 롤백 제어 가능

**구현 단계**:

1. **MySQL Master-Master 구성**
   ```sql
   -- Primary DB 설정
   server-id = 1
   log-bin = mysql-bin
   binlog-format = ROW
   
   -- Secondary DB 설정
   server-id = 2
   log-bin = mysql-bin
   binlog-format = ROW
   
   -- 양방향 복제 설정
   CHANGE MASTER TO ...
   START SLAVE;
   ```

2. **Connection Pool 다중화**
   ```yaml
   # application.yml
   spring:
     datasource:
       primary:
         url: jdbc:mysql://primary-db:3306/reading_tracker
         username: root
         password: ${PRIMARY_DB_PASSWORD}
       secondary:
         url: jdbc:mysql://secondary-db:3306/reading_tracker
         username: root
         password: ${SECONDARY_DB_PASSWORD}
   ```

3. **커스텀 트랜잭션 매니저**
   ```java
   @Configuration
   public class DualMasterTransactionConfig {
       
       @Bean
       @Primary
       public DataSource primaryDataSource() {
           // Primary DB 설정
       }
       
       @Bean
       public DataSource secondaryDataSource() {
           // Secondary DB 설정
       }
       
       @Bean
       public DualMasterTransactionManager transactionManager() {
           return new DualMasterTransactionManager(
               primaryDataSource(), 
               secondaryDataSource()
           );
       }
   }
   ```

4. **분산 트랜잭션 관리**
   ```java
   @Service
   public class DualMasterMemoService {
       
       @Autowired
       private PrimaryDataSource primaryDS;
       
       @Autowired
       private SecondaryDataSource secondaryDS;
       
       public Memo createMemo(User user, Memo memo) {
           Memo savedMemo = null;
           
           try {
               // Phase 1: Primary DB에 실행
               savedMemo = primaryDS.save(memo);
               
               // Phase 2: Secondary DB에 실행
               secondaryDS.save(memo);
               
               // 양쪽 모두 성공 시 커밋
               return savedMemo;
               
           } catch (Exception e) {
               // 실패 시 Primary도 롤백
               if (savedMemo != null) {
                   try {
                       primaryDS.delete(savedMemo.getId());
                   } catch (Exception rollbackError) {
                       // 롤백 실패 로깅
                   }
               }
               throw e;
           }
       }
   }
   ```

5. **Read Failover 로직**
   ```java
   @Service
   public class DualMasterMemoService {
       
       public List<Memo> getMemos(User user, Long userBookId) {
           // Primary DB에서 시도
           try {
               return primaryDS.findByUserBookId(userBookId);
           } catch (Exception e) {
               // Primary 실패 시 Secondary 시도
               log.warn("Primary DB 접근 실패, Secondary DB로 전환", e);
               return secondaryDS.findByUserBookId(userBookId);
           }
       }
   }
   ```

#### 옵션 2: 2PC (Two-Phase Commit) 패턴

**장점**:
- 강력한 일관성 보장
- 표준 분산 트랜잭션 프로토콜

**단점**:
- 구현 복잡도 높음
- 성능 오버헤드
- JTA 라이브러리 필요 (Atomikos, Bitronix)

**구현 예시**:
```java
@Service
public class DualMasterMemoService {
    
    @Autowired
    @Qualifier("jtaTransactionManager")
    private PlatformTransactionManager transactionManager;
    
    @Transactional
    public Memo createMemo(User user, Memo memo) {
        // JTA가 자동으로 2PC 처리
        return memoRepository.save(memo);
    }
}
```

### 수정이 필요한 Service 메서드

현재 프로젝트에서 `@Transactional` 어노테이션이 있는 메서드:

1. **MemoService** (7개)
   - `createMemo()`: Write
   - `updateMemo()`: Write
   - `deleteMemo()`: Write
   - `getMemoById()`: Read
   - `getTodayFlowGroupedByBook()`: Read
   - `getTodayFlowGroupedByTag()`: Read
   - `getBookMemosByDate()`: Read
   - `getAllBookMemos()`: Read
   - `getBooksWithRecentMemos()`: Read
   - `closeBook()`: Write
   - `getMemoDates()`: Read

2. **UserService** (1개)
   - `getUserByLoginId()`: Read

3. **BookService** (2개)
   - `addBookToShelf()`: Write
   - `getBooksByCategory()`: Read

4. **AuthService** (1개)
   - `register()`: Write

5. **JwtService** (3개)
   - `generateTokens()`: Write
   - `refreshAccessToken()`: Write
   - `validateToken()`: Read

6. **UserDeviceService** (3개)
   - `registerDevice()`: Write
   - `getUserDevices()`: Read
   - `deleteDevice()`: Write

**총 17개 메서드 수정 필요**

### 데이터 무결성 보장

#### 1. 동기화 검증
- 주기적으로 두 DB의 데이터 일관성 검증
- 불일치 발견 시 알림 및 복구

#### 2. 충돌 해결
- 동일한 레코드에 대한 동시 수정 시 처리
- Last-Write-Wins 또는 사용자 확인 방식

#### 3. 롤백 메커니즘
- Write 작업 실패 시 양쪽 DB 모두 롤백
- 트랜잭션 로그를 통한 복구

### 모니터링

1. **동기화 상태 모니터링**
   - Replication 지연 시간
   - 동기화 실패 횟수

2. **DB 상태 모니터링**
   - 각 DB의 연결 상태
   - 쿼리 성능

3. **Failover 모니터링**
   - Primary → Secondary 전환 횟수
   - Failover 성공/실패 통계

---

## 클라이언트 기능 완성

### 웹 UI 오프라인 동기화

#### 구현 단계

1. **IndexedDB 스키마 설계**
   - `offline_memos` 테이블
   - `sync_queue` 테이블
   - 인덱스 설계

2. **오프라인 메모 작성 기능**
   - 로컬 ID 생성 (UUID)
   - 로컬 저장소에 저장
   - UI 즉시 업데이트

3. **동기화 큐 관리**
   - 큐 항목 생성/관리
   - 상태 관리 (PENDING, SYNCING, SUCCESS, FAILED)
   - 재시도 로직

4. **네트워크 복구 감지**
   - `navigator.onLine` API
   - `online` 이벤트 리스너
   - 자동 동기화 트리거

5. **UI 통합**
   - 동기화 상태 표시
   - 에러 피드백
   - 수동 재시도 버튼

#### 상세 구현 가이드

[OFFLINE_MEMO_SYNC.md](./OFFLINE_MEMO_SYNC.md) 문서 참조

---

## 권장 해결 로직

### 시나리오 1: 오프라인 메모 동기화

#### 웹 (JavaScript)

**핵심 로직**:
```javascript
// 1. 메모 작성 (오프라인 지원)
async createMemo(memoData) {
    // 로컬 저장소에 먼저 저장
    const localMemo = await offlineMemoService.createMemo(memoData);
    
    // 온라인 상태면 즉시 동기화 시도
    if (networkMonitor.isOnline) {
        offlineMemoService.syncPendingMemos();
    }
    
    return localMemo;
}

// 2. 네트워크 복구 감지
window.addEventListener('online', () => {
    // 1초 대기 후 동기화
    setTimeout(async () => {
        await offlineMemoService.syncPendingMemos();
    }, 1000);
});

// 3. 동기화 실행
async syncPendingMemos() {
    const pendingMemos = await dbManager.getPendingMemos();
    
    for (const memo of pendingMemos) {
        try {
            // 서버 API 호출
            const response = await apiClient.post('/memos', {
                userBookId: memo.userBookId,
                content: memo.content,
                // ...
            });
            
            // 서버 ID로 업데이트
            await dbManager.updateMemoWithServerId(
                memo.localId, 
                response.data.id
            );
        } catch (error) {
            // 재시도 큐에 추가
            await syncQueueManager.markAsFailed(memo.syncQueueId, error);
        }
    }
}
```

### 시나리오 2: MySQL 이중화

#### 커스텀 트랜잭션 매니저

```java
public class DualMasterTransactionManager {
    
    private final DataSource primaryDS;
    private final DataSource secondaryDS;
    
    public <T> T executeInTransaction(
            Function<DataSource, T> operation) {
        
        T primaryResult = null;
        boolean primarySuccess = false;
        
        try {
            // Phase 1: Primary DB에 실행
            primaryResult = operation.apply(primaryDS);
            primarySuccess = true;
            
            // Phase 2: Secondary DB에 실행
            operation.apply(secondaryDS);
            
            // 양쪽 모두 성공 시 커밋
            return primaryResult;
            
        } catch (Exception e) {
            // 실패 시 Primary도 롤백
            if (primarySuccess && primaryResult != null) {
                try {
                    rollbackPrimary(primaryResult);
                } catch (Exception rollbackError) {
                    log.error("Primary DB 롤백 실패", rollbackError);
                }
            }
            throw e;
        }
    }
}
```

#### Read Failover

```java
public class DualMasterReadService {
    
    public <T> T readWithFailover(
            Function<DataSource, T> readOperation) {
        
        // Primary DB에서 시도
        try {
            return readOperation.apply(primaryDS);
        } catch (Exception e) {
            log.warn("Primary DB 읽기 실패, Secondary DB로 전환", e);
            
            // Secondary DB에서 시도
            try {
                return readOperation.apply(secondaryDS);
            } catch (Exception e2) {
                log.error("Secondary DB 읽기도 실패", e2);
                throw new DatabaseUnavailableException("모든 DB 접근 실패", e2);
            }
        }
    }
}
```

#### Service 메서드 수정 예시

```java
@Service
public class MemoService {
    
    @Autowired
    private DualMasterTransactionManager transactionManager;
    
    @Autowired
    private DualMasterReadService readService;
    
    // Write 작업
    public Memo createMemo(User user, Memo memo) {
        return transactionManager.executeInTransaction(ds -> {
            // Primary와 Secondary 모두에 실행
            MemoRepository repo = new MemoRepository(ds);
            return repo.save(memo);
        });
    }
    
    // Read 작업
    @Transactional(readOnly = true)
    public List<Memo> getMemos(User user, Long userBookId) {
        return readService.readWithFailover(ds -> {
            MemoRepository repo = new MemoRepository(ds);
            return repo.findByUserBookId(userBookId);
        });
    }
}
```

---

## 리스크 관리

### 시나리오별 리스크

#### 시나리오 1: 오프라인 메모 동기화

**리스크**:
- IndexedDB 데이터 손실
- 동기화 실패 시 데이터 누락
- 네트워크 복구 감지 실패

**완화 방안**:
- 정기적인 로컬 데이터 백업
- 동기화 상태 모니터링
- 수동 동기화 버튼 제공

#### 시나리오 2: MySQL 이중화

**리스크**:
- 두 DB 간 데이터 불일치
- 동기화 지연
- 분산 트랜잭션 실패
- Failover 실패

**완화 방안**:
- 주기적인 데이터 일관성 검증
- Replication 지연 모니터링
- 자동 복구 메커니즘
- 장애 시나리오 테스트

### 구현 순서의 중요성

**옵션 A (권장)**: 클라이언트 기능 먼저 → 인프라 개선
- ✅ 안정적인 백엔드 API 위에서 클라이언트 개발
- ✅ 각 단계 독립적으로 검증 가능
- ✅ 인프라 변경 시 클라이언트 기능은 안정적

**옵션 B (비권장)**: 인프라 개선 먼저 → 클라이언트 기능
- ❌ 불안정한 인프라 위에서 클라이언트 개발
- ❌ 인프라 버그가 클라이언트 개발 지연
- ❌ 전체 시스템 불안정

---

## 참고 자료

### 관련 문서

- [오프라인 메모 동기화 상세 설계](./OFFLINE_MEMO_SYNC.md)
- [멀티 디바이스 오프라인 동기화](./MULTI_DEVICE_SYNC.md)
- [Fault Tolerance 테스트](./FAULT_TOLERANCE_TESTING.md)

### 외부 자료

#### 오프라인 동기화
- [IndexedDB API](https://developer.mozilla.org/en-US/docs/Web/API/IndexedDB_API)
- [Offline-First Architecture](https://offlinefirst.org/)

#### MySQL 이중화
- [MySQL Replication](https://dev.mysql.com/doc/refman/8.0/en/replication.html)
- [MySQL Master-Master Replication](https://dev.mysql.com/doc/refman/8.0/en/replication-multi-master.html)
- [Spring DataSource Routing](https://www.baeldung.com/spring-abstract-routing-data-source)
- [Two-Phase Commit](https://en.wikipedia.org/wiki/Two-phase_commit_protocol)

---

## 다음 단계

1. **Phase 1 시작**: 웹 UI 오프라인 동기화 구현
2. **문서 검토**: [OFFLINE_MEMO_SYNC.md](./OFFLINE_MEMO_SYNC.md) 상세 검토
3. **프로토타입**: 작은 규모로 프로토타입 구현 및 검증
4. **단계별 완료**: 각 Phase 완료 후 충분한 테스트 수행

---

**문서 버전**: 1.0  
**최종 업데이트**: 2024년  
**작성자**: Development Team


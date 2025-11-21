# 태그 저장 방식 설계 결정서

## 1. 요구사항 분석

### 1.1 현재 요구사항
- 메모에 여러 태그를 부여할 수 있음 (예: ["인상깊은구절", "의문점", "인용"])
- **해설집 생성 시 메모 태그별로 메모를 분류하는 로직 필요**
- 태그별로 메모를 그룹화하여 해설집 생성

### 1.2 해설집 생성 시나리오
```
해설집 생성 프로세스:
1. 특정 책의 모든 메모 조회
2. 메모를 태그별로 그룹화
   - "인상깊은구절" 태그가 있는 메모들
   - "의문점" 태그가 있는 메모들
   - "인용" 태그가 있는 메모들
3. 태그별로 정렬된 해설집 생성
```

---

## 2. 태그 저장 방식 비교

### 옵션 1: JSON 문자열로 저장 (현재 계획)

#### 구조
```sql
CREATE TABLE memo (
    ...
    tags VARCHAR(500),  -- JSON: ["태그1", "태그2"]
    ...
);
```

#### 장점
- ✅ 구현이 간단하고 빠름
- ✅ 추가 테이블 불필요
- ✅ 메모 조회 시 태그 정보를 함께 가져올 수 있음

#### 단점
- ❌ 태그별 메모 조회가 어려움 (LIKE 검색 필요)
- ❌ 태그별 통계/분석이 복잡함
- ❌ 해설집 생성 시 태그별 그룹화가 비효율적
  ```sql
  -- 비효율적인 쿼리 예시
  SELECT * FROM memo 
  WHERE tags LIKE '%"인상깊은구절"%'
  ```
- ❌ 태그 정규화 불가 (오타, 대소문자 구분 등 문제)
- ❌ 인덱스 활용 불가

#### 해설집 생성 시 문제점
```java
// 비효율적인 방식
List<Memo> allMemos = memoRepository.findByBookId(bookId);
Map<String, List<Memo>> groupedByTag = new HashMap<>();

for (Memo memo : allMemos) {
    List<String> tags = parseJsonTags(memo.getTags());  // JSON 파싱
    for (String tag : tags) {
        groupedByTag.computeIfAbsent(tag, k -> new ArrayList<>()).add(memo);
    }
}
// 모든 메모를 메모리로 가져온 후 애플리케이션에서 그룹화
```

---

### 옵션 2: 별도 Tags 테이블 + Many-to-Many 관계 (권장)

#### 구조
```sql
-- Tags 테이블 (태그 마스터)
CREATE TABLE tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,  -- 태그 이름 (예: "인상깊은구절")
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tags_name (name)
);

-- Memo-Tag 중간 테이블 (Many-to-Many)
CREATE TABLE memo_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    memo_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (memo_id) REFERENCES memo(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    UNIQUE KEY uk_memo_tag (memo_id, tag_id),  -- 중복 방지
    INDEX idx_memo_tags_memo (memo_id),
    INDEX idx_memo_tags_tag (tag_id)
);

-- Memo 테이블 (tags 컬럼 제거)
CREATE TABLE memo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    page_number INT NOT NULL,
    content TEXT NOT NULL,
    memo_start_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES user_books(id) ON DELETE CASCADE,
    INDEX idx_memo_user_book (user_id, book_id),
    INDEX idx_memo_created_at (created_at),
    INDEX idx_memo_page_number (book_id, page_number)
);
```

#### 장점
- ✅ **태그별 메모 조회가 효율적** (인덱스 활용 가능)
  ```sql
  -- 효율적인 쿼리
  SELECT m.* FROM memo m
  INNER JOIN memo_tags mt ON m.id = mt.memo_id
  INNER JOIN tags t ON mt.tag_id = t.id
  WHERE t.name = '인상깊은구절' AND m.book_id = ?
  ```
- ✅ **해설집 생성 시 태그별 그룹화가 효율적**
  ```java
  // 효율적인 방식
  Map<String, List<Memo>> groupedByTag = memoRepository
      .findMemosGroupedByTag(bookId);
  // DB에서 이미 그룹화된 결과 반환
  ```
- ✅ 태그 정규화 가능 (오타 방지, 대소문자 통일)
- ✅ 태그 통계/분석 용이
- ✅ 태그 자동완성, 인기 태그 등 확장 기능 구현 용이
- ✅ 데이터 무결성 보장 (외래키 제약)

#### 단점
- ❌ 구현 복잡도 증가 (추가 테이블 2개)
- ❌ 조인 쿼리 필요 (성능 최적화 필요)
- ❌ 메모 조회 시 태그를 함께 가져오려면 조인 필요

#### 해설집 생성 시 장점
```java
// 효율적인 방식
@Query("SELECT t.name, m FROM Memo m " +
       "JOIN m.tags t " +
       "WHERE m.userShelfBook.id = :bookId " +
       "ORDER BY t.name, m.memoStartTime")
Map<String, List<Memo>> findMemosGroupedByTag(@Param("bookId") Long bookId);

// 또는
@Query("SELECT t.name, m FROM Memo m " +
       "JOIN m.tags t " +
       "WHERE m.userShelfBook.id = :bookId AND t.name IN :tagNames " +
       "ORDER BY t.name, m.memoStartTime")
List<Object[]> findMemosByTags(@Param("bookId") Long bookId, 
                               @Param("tagNames") List<String> tagNames);
```

---

### 옵션 3: 하이브리드 방식 (Tags 테이블 + Memo에 태그 ID 배열 저장)

#### 구조
```sql
CREATE TABLE tags (...);
CREATE TABLE memo (
    ...
    tag_ids VARCHAR(500),  -- JSON: [1, 2, 3] (태그 ID 배열)
    ...
);
```

#### 평가
- ❌ 정규화의 이점을 잃음
- ❌ JSON 파싱 필요
- ❌ 옵션 1과 2의 단점만 결합

**→ 권장하지 않음**

---

## 3. 성능 비교

### 시나리오: 해설집 생성 (책당 100개 메모, 평균 3개 태그)

#### 옵션 1 (JSON 문자열)
```sql
-- 1. 모든 메모 조회
SELECT * FROM memo WHERE book_id = ?;  -- 100개 메모

-- 2. 애플리케이션에서 JSON 파싱 및 그룹화
-- 메모리 사용: 100개 메모 전체 로드
-- CPU 사용: JSON 파싱 100회
```

#### 옵션 2 (Many-to-Many)
```sql
-- 1. 태그별 메모 조회 (인덱스 활용)
SELECT m.*, t.name as tag_name 
FROM memo m
INNER JOIN memo_tags mt ON m.id = mt.memo_id
INNER JOIN tags t ON mt.tag_id = t.id
WHERE m.book_id = ?
ORDER BY t.name, m.memo_start_time;

-- 또는 태그별로 그룹화된 결과
SELECT t.name, GROUP_CONCAT(m.id) as memo_ids
FROM memo m
INNER JOIN memo_tags mt ON m.id = mt.memo_id
INNER JOIN tags t ON mt.tag_id = t.id
WHERE m.book_id = ?
GROUP BY t.name;
```

**성능 비교:**
- 옵션 1: O(n) 메모리 + O(n) 파싱 시간
- 옵션 2: O(n) 메모리 + DB 레벨 그룹화 (인덱스 활용)

---

## 4. 최종 권장안: 옵션 2 (Many-to-Many 관계)

### 4.1 결정 근거
1. **해설집 생성이 핵심 기능**: 태그별 메모 분류가 필수
2. **확장성**: 추후 태그 통계, 인기 태그, 태그 검색 등 기능 추가 용이
3. **데이터 무결성**: 태그 정규화로 데이터 품질 향상
4. **성능**: 인덱스 활용으로 태그별 조회 최적화

### 4.2 구현 구조

#### 엔티티 설계
```java
// Tag 엔티티
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;
    
    @ManyToMany(mappedBy = "tags")
    private List<Memo> memos = new ArrayList<>();
}

// Memo 엔티티 (수정)
@Entity
@Table(name = "memo")
public class Memo {
    // ... 기존 필드들 ...
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "memo_tags",
        joinColumns = @JoinColumn(name = "memo_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();
}
```

#### Repository 메서드
```java
public interface MemoRepository extends JpaRepository<Memo, Long> {
    // 태그별 메모 조회
    @Query("SELECT m FROM Memo m " +
           "JOIN m.tags t " +
           "WHERE m.userShelfBook.id = :bookId AND t.name = :tagName " +
           "ORDER BY m.memoStartTime ASC")
    List<Memo> findByBookIdAndTagName(@Param("bookId") Long bookId, 
                                       @Param("tagName") String tagName);
    
    // 태그별로 그룹화된 메모 조회 (해설집 생성용)
    @Query("SELECT t.name, m FROM Memo m " +
           "JOIN m.tags t " +
           "WHERE m.userShelfBook.id = :bookId " +
           "ORDER BY t.name, m.memoStartTime ASC")
    List<Object[]> findMemosGroupedByTag(@Param("bookId") Long bookId);
}
```

---

## 5. 구현 시 고려사항

### 5.1 태그 생성 전략
- **자동 생성**: 메모 저장 시 존재하지 않는 태그는 자동 생성
- **대소문자 처리**: 태그 이름을 소문자로 통일하여 저장
- **중복 방지**: UNIQUE 제약으로 동일 태그 중복 방지

### 5.2 성능 최적화
- **지연 로딩**: `FetchType.LAZY`로 태그는 필요 시에만 로드
- **배치 조인**: 해설집 생성 시 `@EntityGraph` 또는 `JOIN FETCH` 활용
- **인덱스**: `memo_tags(memo_id)`, `memo_tags(tag_id)` 인덱스 필수

### 5.3 태그 관리
- **태그 삭제**: 사용되지 않는 태그 정리 (선택적)
- **태그 통계**: 태그별 메모 개수 통계 (추후 기능)

---

## 6. 결론

**최종 결정: 옵션 2 (Many-to-Many 관계) 채택**

이유:
1. 해설집 생성 시 태그별 메모 분류가 핵심 요구사항
2. DB 레벨에서 효율적인 태그별 그룹화 가능
3. 확장성과 데이터 무결성 확보
4. 성능 최적화 가능 (인덱스 활용)

구현 복잡도는 증가하지만, 장기적으로 유지보수성과 확장성이 우수합니다.


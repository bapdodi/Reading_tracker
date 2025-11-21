-- 메모 테이블 생성
CREATE TABLE memo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,  -- UserShelfBook의 id를 참조
    page_number INT NOT NULL,  -- 메모 작성 시점의 SESSION 모드 기준 초기 위치 (정렬 방식 변경 시에도 변경하지 않는 메타데이터)
    content TEXT NOT NULL,  -- 메모 내용 (필수)
    memo_start_time TIMESTAMP NOT NULL,  -- 메모가 작성된 시간 (사용자가 메모 작성을 시작한 시간, 타임라인 정렬 기준)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- DB 레코드 생성 시간 (감사 목적)
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES user_books(id) ON DELETE CASCADE,
    INDEX idx_memo_user_book (user_id, book_id),
    INDEX idx_memo_created_at (created_at),
    INDEX idx_memo_page_number (book_id, page_number),
    INDEX idx_memo_memo_start_time (memo_start_time)  -- 타임라인 정렬용
);


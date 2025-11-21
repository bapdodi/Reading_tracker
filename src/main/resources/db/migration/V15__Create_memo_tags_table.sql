-- 메모-태그 중간 테이블 생성 (Many-to-Many 관계)
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


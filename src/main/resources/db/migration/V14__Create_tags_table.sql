-- 태그 마스터 테이블 생성
CREATE TABLE tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category ENUM('TYPE', 'TOPIC') NOT NULL,  -- 태그 대분류 (유형/주제)
    code VARCHAR(50) NOT NULL,                 -- 캐논컬 키 (예: 'impressive-quote')
    sort_order INT NOT NULL,                   -- 정렬 순서 (가나다순)
    is_active BOOLEAN NOT NULL DEFAULT TRUE,   -- 활성화 상태
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tags_category_code (category, code),  -- 카테고리별 code 유니크 제약
    INDEX idx_tags_category (category),
    INDEX idx_tags_code (code),
    INDEX idx_tags_category_sort (category, sort_order)
);


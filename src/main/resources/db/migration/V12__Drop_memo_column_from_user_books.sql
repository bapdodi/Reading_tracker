-- user_books 테이블에서 memo 컬럼 삭제
-- memo 컬럼은 사용되지 않으며, 메모 기능은 memo 테이블을 통해 구현됩니다
ALTER TABLE user_books DROP COLUMN memo;


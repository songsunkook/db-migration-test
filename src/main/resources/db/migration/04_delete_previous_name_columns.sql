-- Contract 단계: 기존 스키마 제거
-- 설명: 기존 스키마 컬럼들을 제거하고 신규 스키마를 정리한다.

-- 1. 기존 스키마 컬럼 제거
ALTER TABLE users 
    DROP COLUMN first_name,
    DROP COLUMN last_name;

-- 2. 신규 스키마 정리 (NOT NULL 제약조건 추가)
ALTER TABLE users 
    MODIFY COLUMN full_name VARCHAR(255) NOT NULL;

-- 3. 스키마 확인
-- DESCRIBE users;

-- 4. 데이터 확인
-- SELECT id, full_name, email, created_at, updated_at
-- FROM users
-- LIMIT 5;

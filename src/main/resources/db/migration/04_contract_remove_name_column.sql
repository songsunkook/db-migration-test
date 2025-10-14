-- Contract 단계: 기존 first_name, last_name 컬럼 제거
-- 모든 애플리케이션이 full_name만 사용하도록 배포되고 백필이 완료된 후 실행

-- 먼저 full_name을 NOT NULL로 변경
ALTER TABLE users MODIFY COLUMN full_name VARCHAR(255) NOT NULL;

-- 기존 first_name, last_name 컬럼 제거
ALTER TABLE users DROP COLUMN first_name;
ALTER TABLE users DROP COLUMN last_name;

-- 3단계: Back Fill (기존 데이터 마이그레이션)
-- 기존 데이터의 full_name 컬럼을 first_name + last_name으로 채움

-- 소규모 테이블의 경우 직접 실행
-- 대용량 테이블의 경우 Spring Batch나 별도 배치 작업 권장
UPDATE users
SET full_name = CONCAT(first_name, ' ', last_name)
WHERE full_name IS NULL;

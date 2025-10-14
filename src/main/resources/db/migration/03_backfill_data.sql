-- 백필(Backfill) 단계: first_name + last_name을 조합하여 full_name 생성
-- 주의: 대용량 테이블의 경우 Spring Batch나 별도 배치 작업 권장

-- 소규모 테이블의 경우 직접 실행:
UPDATE users
SET full_name = CONCAT(first_name, ' ', last_name)
WHERE full_name IS NULL;

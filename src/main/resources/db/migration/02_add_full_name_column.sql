-- Expand 단계: 검색 성능 향상을 위한 full_name 컬럼 추가
-- 이 DDL은 MySQL 8.0에서 INSTANT 알고리즘을 사용하여 Online으로 실행됩니다.

-- Online DDL 확인 및 실행
ALTER TABLE users
    ADD COLUMN full_name VARCHAR(255) NULL AFTER last_name,
    ALGORITHM = INPLACE,
    LOCK = NONE;

-- 만약 INSTANT가 지원되지 않는 경우 INPLACE로 fallback
-- ALTER TABLE users
-- ADD COLUMN full_name VARCHAR(255) NULL AFTER last_name,
-- ALGORITHM=INPLACE, LOCK=NONE;

-- 대용량 테이블에서 Offline DDL이 필요한 경우 gh-ost 사용:
-- gh-ost \
--   --host=localhost \
--   --port=3307 \
--   --user=root \
--   --password=0000 \
--   --database=test_db \
--   --table=users \
--   --alter="ADD COLUMN full_name VARCHAR(255) NULL AFTER last_name" \
--   --execute

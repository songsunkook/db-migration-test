# Migration Progress

## 현재 단계: 3단계 - Back Fill (기존 데이터 마이그레이션)

### 수행 작업
- 기존 데이터의 `full_name` 컬럼을 실제 값으로 채움
- NULL인 `full_name`을 `first_name + last_name`으로 업데이트

### 실행 쿼리
```sql
UPDATE users 
SET full_name = CONCAT(first_name, ' ', last_name) 
WHERE full_name IS NULL;
```

### 마이그레이션 전후 비교

#### 마이그레이션 전
```sql
-- 기존 데이터 예시
+----+------------+-----------+-----------+------------------+
| id | first_name | last_name | full_name | email            |
+----+------------+-----------+-----------+------------------+
| 1  | 홍         | 길동      | NULL      | hong@example.com |
| 2  | 김         | 철수      | NULL      | kim@example.com  |
+----+------------+-----------+-----------+------------------+
```

#### 마이그레이션 후
```sql
-- 마이그레이션 완료 후
+----+------------+-----------+-----------+------------------+
| id | first_name | last_name | full_name | email            |
+----+------------+-----------+-----------+------------------+
| 1  | 홍         | 길동      | 홍 길동   | hong@example.com |
| 2  | 김         | 철수      | 김 철수   | kim@example.com  |
+----+------------+-----------+-----------+------------------+
```

### 실행 쿼리

1. 데이터 수가 적은 경우
```sql
UPDATE users
SET full_name = CONCAT(first_name, ' ', last_name)
WHERE full_name IS NULL;
```

2. 데이터 수가 많은 경우
   - 배치 작업으로 처리
   - 청크 단위로 분할 실행
   - 서비스 영향 최소화를 위해 off-peak 시간에 진행

### 배치 처리 예시 (대용량 데이터용)
```sql
-- 1000건씩 처리하는 배치 방식
UPDATE users 
SET full_name = CONCAT(first_name, ' ', last_name) 
WHERE full_name IS NULL 
LIMIT 1000;

-- 완료될 때까지 반복 실행
-- 진행률 확인: SELECT COUNT(*) FROM users WHERE full_name IS NULL;
```

### 검증 쿼리
```sql
-- 마이그레이션 완료 확인
SELECT COUNT(*) as null_count 
FROM users 
WHERE full_name IS NULL;

-- 결과: null_count = 0 이어야 함

-- 데이터 일관성 확인
SELECT id, first_name, last_name, full_name,
    CASE 
        WHEN full_name = CONCAT(first_name, ' ', last_name) THEN 'OK'
        ELSE 'MISMATCH'
    END as consistency_check
FROM users 
ORDER BY id;
```

### 애플리케이션 상태
- ✅ 모든 데이터에 `full_name` 값 존재
- ✅ 기존 코드 동작: 정상 (변경 없음)
- ✅ 새로운 스키마 준비 완료

### 다음 단계 준비사항
- 4단계: Read Conversion 구현
- 애플리케이션에서 새로운 스키마(`full_name`)로 점진적 읽기 전환
- 기존 스키마는 백업/대체 역할

### 주의사항
- **트랜잭션**: 대용량 데이터의 경우 적절한 배치 크기로 분할
- **모니터링**: 마이그레이션 진행률과 서버 부하 모니터링
- **검증**: 마이그레이션 완료 후 데이터 일관성 검증 필수

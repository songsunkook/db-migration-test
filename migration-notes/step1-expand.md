# Migration Progress

## 현재 단계: 1단계 - Expand (스키마 확장)

### 수행 작업
- `users` 테이블에 `full_name` 컬럼 추가
- 기존 스키마(`first_name`, `last_name`)는 유지

### 기존 테이블 구조
```sql
-- users 테이블 구조 (1단계 이전)
+------------+--------------+------+-----+---------------------+----------------+
| Field      | Type         | Null | Key | Default             | Extra          |
+------------+--------------+------+-----+---------------------+----------------+
| id         | bigint       | NO   | PRI | NULL                | auto_increment |
| first_name | varchar(100) | NO   |     | NULL                |                |
| last_name  | varchar(100) | NO   |     | NULL                |                |
| email      | varchar(255) | NO   |     | NULL                |                |
| created_at | timestamp    | YES  |     | CURRENT_TIMESTAMP   |                |
| updated_at | timestamp    | YES  |     | CURRENT_TIMESTAMP   | on update CURR |
+------------+--------------+------+-----+---------------------+----------------+
```

### 실행 쿼리
```sql
ALTER TABLE users
    ADD COLUMN full_name VARCHAR(255) NULL AFTER last_name,
    ALGORITHM = INPLACE,
    LOCK = NONE;
```

### 변경된 테이블 구조
```sql
-- 변경 후 users 테이블 구조 (1단계 완료)
+------------+--------------+------+-----+---------------------+----------------+
| Field      | Type         | Null | Key | Default             | Extra          |
+------------+--------------+------+-----+---------------------+----------------+
| id         | bigint       | NO   | PRI | NULL                | auto_increment |
| first_name | varchar(100) | NO   |     | NULL                |                |
| last_name  | varchar(100) | NO   |     | NULL                |                |
| full_name  | varchar(255) | YES  |     | NULL                |                |
| email      | varchar(255) | NO   |     | NULL                |                |
| created_at | timestamp    | YES  |     | CURRENT_TIMESTAMP   |                |
| updated_at | timestamp    | YES  |     | CURRENT_TIMESTAMP   | on update CURR |
+------------+--------------+------+-----+---------------------+----------------+
```

### 애플리케이션 상태
- ✅ 기존 스키마로 읽기/쓰기 정상 작동
- ⚠️ 신규 컬럼(`full_name`)은 NULL 상태

### 다음 단계 준비사항
- 2단계: Dual Write 구현
- 애플리케이션 코드에서 양쪽 스키마 동시 쓰기 지원

# DB Schema Migration Without Downtime

서비스 중단 없이 DB 스키마를 변경하는 실습 프로젝트입니다.

## 🎯 프로젝트 목적

**기술 블로그 글**: "서비스 중단 없이 DB 스키마 변경하기"의 실습 예제

실무에서 발생하는 다음과 같은 문제들을 해결하는 방법을 학습합니다:
- ALTER TABLE로 인한 서비스 장애
- 스키마 변경과 애플리케이션 배포 시점의 불일치
- 대용량 테이블의 무중단 스키마 변경

## 🏗️ 실습 환경

- **Backend**: Spring Boot 3.x + JPA
- **Database**: MySQL 8.0
- **Feature Flag**: Togglz
- **Required**: Docker

## 🚀 Quick Start

### 1. 환경 시작
```bash
# MySQL 환경 시작
./scripts/start.sh

# 애플리케이션 실행
./gradlew bootRun
```

### 2. 웹 인터페이스 접속
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Feature Flag 관리**: http://localhost:8080/togglz-console (4단계부터 사용)

### 3. API 테스트
```bash
# 사용자 목록 조회
curl http://localhost:8080/api/users

# Feature Flag 상태 확인 (4단계부터 사용)
curl http://localhost:8080/api/feature-flags
```

## 📝 실습 시나리오

### 현재 상황
`users` 테이블에서 `first_name`, `last_name`을 개별 관리 중

### 목표
`full_name` 컬럼으로 데이터 이전 후 기존 컬럼 제거

## 🌿 브랜치별 실습

각 단계별로 브랜치를 구성하여 점진적 배포 과정을 시뮬레이션합니다:

```
main (초기 상태)
├── step1-expand (스키마 확장)
├── step2-dual-write (Dual Write 구현)  
├── step3-backfill (데이터 마이그레이션)
├── step4-read-conversion (읽기 전환)
├── step5-cleanup (코드 정리)
└── step6-contract (스키마 축소)
```

### 실습 방법
1. **각 단계별 브랜치 체크아웃**하여 해당 단계의 코드와 변경사항 확인
2. **`migration-notes/current-step.md`**에서 현재 단계 상태 및 수행 작업 확인
3. **미리 생성된 PR**을 통해 단계별 변경사항과 실제 배포 과정 검토

## 📚 실습 단계

### 0단계: 초기 상태 확인

**테이블 구조 확인**
```sql
-- MySQL 접속
mysql -h localhost -P 3306 -u root -p0000 test_db

-- 테이블 구조 확인 (기존 스키마)
DESCRIBE users;
```

**API 테스트**
```bash
# 사용자 생성 (기존 스키마)
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john.doe@example.com"}'

# 생성된 사용자 조회
curl http://localhost:8080/api/users/1
```

### 1단계: Expand - 스키마 변경 대상 추가 (DDL)

스키마 확장만 하고 기존 스키마는 유지합니다. 신규 컬럼은 아직 NULL 상태입니다.

```sql
-- 서비스 중단 없이 새 컬럼 추가
ALTER TABLE users 
ADD COLUMN full_name VARCHAR(255) NULL,
ALGORITHM=INSTANT, LOCK=NONE;

-- 변경 확인
DESCRIBE users;
```

### 2단계: Dual Write - 기존/신규 스키마 쓰기 대응 (APP)

애플리케이션이 이미 Dual Write로 구현되어 있습니다. 기존/신규 스키마 양쪽에 모두 쓰기 작업을 수행합니다.

```bash
# 새 사용자 생성 (full_name도 함께 저장됨)
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Smith","email":"jane.smith@example.com"}'

# Dual Write 확인
curl http://localhost:8080/api/users/2
```

현재 코드에서 `UserService.createUser()` 메소드가 Dual Write를 자동으로 수행합니다:
- `firstName`, `lastName` (기존 스키마)
- `fullName` (신규 스키마)

### 3단계: Back Fill - 기존 데이터 마이그레이션 (BATCH)

기존 데이터를 신규 스키마로 마이그레이션합니다.

```sql
-- 기존 데이터에 full_name 채우기
UPDATE users 
SET full_name = CONCAT(first_name, ' ', last_name) 
WHERE full_name IS NULL;

-- 백필 결과 확인
SELECT id, first_name, last_name, full_name FROM users;
```

### 4단계: Read Conversion - 읽기 전환 (APP)

Feature Flag로 신규 스키마 읽기를 점진적으로 전환합니다. Dual Write는 유지됩니다.

```bash
# 신규 스키마 읽기 전환 활성화
curl -X PUT http://localhost:8080/api/feature-flags/new-schema-read \
  -H "Content-Type: application/json" \
  -d '{"enabled":true}'

# 점진적 배포: 50% 사용자에게만 적용
curl -X PUT http://localhost:8080/api/feature-flags/percentage-rollout \
  -H "Content-Type: application/json" \
  -d '{"percentage":50}'

# 읽기 방식 확인
curl http://localhost:8080/api/feature-flags/user/1/status
curl http://localhost:8080/api/users/1/display-name
```

### 5단계: Clean Up - 코드 정리 (APP)

- Feature Flag 제거
- Dual Write 제거 (신규 스키마만 사용)
- 기존 스키마 관련 코드 제거

```bash
# 100% 사용자에게 신규 스키마 적용
curl -X PUT http://localhost:8080/api/feature-flags/percentage-rollout \
  -H "Content-Type: application/json" \
  -d '{"percentage":100}'
```

### 6단계: Contract - 기존 스키마 대상 제거 (DDL)

애플리케이션이 더이상 기존 스키마를 참조하지 않은 후 기존 스키마를 제거합니다.

```sql
-- full_name을 NOT NULL로 변경
ALTER TABLE users MODIFY COLUMN full_name VARCHAR(255) NOT NULL;

-- 기존 컬럼 제거 (Clean Up 완료 후 실행)
ALTER TABLE users DROP COLUMN first_name;
ALTER TABLE users DROP COLUMN last_name;

-- 인덱스 추가
CREATE INDEX idx_users_full_name ON users(full_name);
```

## 📚 참고 자료

- [MySQL Online DDL](https://dev.mysql.com/doc/refman/8.0/en/innodb-online-ddl.html)
- [gh-ost](https://github.com/github/gh-ost)
- [Togglz](https://www.togglz.org/)

## 🤝 Contributing

실습 중 발견한 이슈나 개선 사항이 있다면 Issue를 등록해 주세요.

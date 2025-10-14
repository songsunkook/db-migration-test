# DB 스키마 무중단 변경 실습 가이드

이 프로젝트는 서비스 중단 없이 DB 스키마를 변경하는 방법을 실습할 수 있는 예제입니다.

## 🎯 실습 목표

1. **Online DDL vs Offline DDL** 이해하기
2. **Expand-Contract 패턴** 적용하기  
3. **Dual Write 전략**으로 데이터 일관성 보장하기
4. **Feature Flag**로 점진적 배포하기
5. **Read-After-Write Consistency** 문제 해결하기

## 📋 실습 시나리오

**현재 상황**: users 테이블에서 `first_name`, `last_name`을 개별 관리 중  
**목표**: 검색 성능 향상을 위해 `full_name` 컬럼 추가 후 기존 컬럼 제거

## 🛠️ 환경 구성

### 1. Docker로 MySQL 실행

```bash
# MySQL Master/Replica 환경 시작
docker-compose up -d

# 컨테이너 상태 확인
docker-compose ps
```

### 2. 애플리케이션 실행

```bash
# 의존성 설치 및 실행
./gradlew bootRun
```

### 3. 초기 상태 확인

```bash
# 기본 사용자 목록 조회
curl http://localhost:8080/api/users

# Feature Flag 상태 확인  
curl http://localhost:8080/api/feature-flags
```

## 📚 실습 단계

### 단계 1: 초기 상태 파악

**현재 스키마 확인**
```sql
-- MySQL 접속
docker exec -it mysql-master mysql -u root -p0000 moko

-- 테이블 구조 확인
DESCRIBE users;
```

**API 테스트**
```bash
# 사용자 생성
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john.doe@example.com"}'

# 생성된 사용자 조회
curl http://localhost:8080/api/users/1
```

### 단계 2: Expand - 새 컬럼 추가

**DDL 실행**
```sql
-- Online DDL로 컬럼 추가 (서비스 중단 없음)
ALTER TABLE users 
ADD COLUMN full_name VARCHAR(255) NULL AFTER last_name,
ALGORITHM=INSTANT, LOCK=NONE;

-- 변경 확인
DESCRIBE users;
```

**Dual Write 확인 (자동 활성화)**
```bash
# Togglz 상태 확인
curl http://localhost:8080/api/feature-flags/status

# Togglz 웹 콘솔 접속 (브라우저)
open http://localhost:8080/togglz-console

# 참고: Dual Write는 Expand 단계에서 자동으로 활성화됩니다
# Feature Flag와 무관하게 신/구 스키마 모두에 데이터를 저장합니다

# 현재 Feature Flag 상태 확인
curl http://localhost:8080/api/feature-flags
```

**Dual Write 테스트**
```bash
# 새 사용자 생성 (full_name도 함께 저장됨)
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Smith","email":"jane.smith@example.com"}'

# 데이터 확인
curl http://localhost:8080/api/users/2
```

### 단계 3: 백필(Backfill) - 기존 데이터 마이그레이션

```sql
-- 기존 데이터에 full_name 채우기
UPDATE users 
SET full_name = CONCAT(first_name, ' ', last_name) 
WHERE full_name IS NULL;

-- 백필 결과 확인
SELECT id, first_name, last_name, full_name FROM users;
```

### 단계 4: 신규 스키마 읽기 전환 (Feature Flag)

**이제 Feature Flag의 진짜 역할이 시작됩니다!**

```bash
# 신규 스키마 읽기 전환 활성화
curl -X PUT http://localhost:8080/api/feature-flags/new-schema-read \
  -H "Content-Type: application/json" \
  -d '{"enabled":true}'

# 점진적 배포: 50% 사용자에게만 적용
curl -X PUT http://localhost:8080/api/feature-flags/percentage-rollout \
  -H "Content-Type: application/json" \
  -d '{"percentage":50}'

# 특정 사용자의 읽기 방식 확인
curl http://localhost:8080/api/feature-flags/user/1/status
curl http://localhost:8080/api/feature-flags/user/2/status

# Display Name API로 읽기 테스트
curl http://localhost:8080/api/users/1/display-name
curl "http://localhost:8080/api/users/1/display-name?fromMaster=true"

# 다양한 사용자로 테스트해서 일부는 신규, 일부는 기존 스키마 사용 확인
for i in {1..10}; do
  echo "User $i 읽기 방식:"
  curl -s http://localhost:8080/api/feature-flags/user/$i/status | jq .readMethod
done
```

### 단계 5: Read-After-Write Consistency 테스트

```bash
# 이름 업데이트
curl -X PUT http://localhost:8080/api/users/1/name \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Johnny","lastName":"Doe"}'

# 즉시 읽기 (Replica에서)
curl http://localhost:8080/api/users/1

# 강제로 Master에서 읽기
curl "http://localhost:8080/api/users/1?fromMaster=true"
```

### 단계 6: Contract - 기존 컬럼 제거

**100% 배포 후 실행**
```bash
# 100% 사용자에게 적용
curl -X PUT http://localhost:8080/api/feature-flags/percentage-rollout \
  -H "Content-Type: application/json" \
  -d '{"percentage":100}'
```

**DDL 실행**
```sql
-- full_name을 NOT NULL로 변경
ALTER TABLE users MODIFY COLUMN full_name VARCHAR(255) NOT NULL;

-- 기존 컬럼 제거 (주의: 애플리케이션 배포 후 실행)
ALTER TABLE users DROP COLUMN first_name;
ALTER TABLE users DROP COLUMN last_name;

-- 인덱스 추가
CREATE INDEX idx_users_full_name ON users(full_name);
```

## 🔍 모니터링 및 검증

### Feature Flag 상태 모니터링
```bash
# 전체 Feature Flag 상태 확인
curl http://localhost:8080/api/feature-flags

# 특정 사용자별 적용 상태 확인
for i in {1..10}; do
  echo "User $i:"
  curl -s http://localhost:8080/api/feature-flags/user/$i/enabled | jq
done
```

### 성능 테스트
```bash
# 대량 사용자 생성
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/users \
    -H "Content-Type: application/json" \
    -d "{\"firstName\":\"User$i\",\"lastName\":\"Test\",\"email\":\"user$i@example.com\"}" &
done
wait

# Replica와 Master 응답 시간 비교
time curl "http://localhost:8080/api/users?fromMaster=false"
time curl "http://localhost:8080/api/users?fromMaster=true"
```

## 🚨 주의사항

### DDL 실행 전 체크리스트
- [ ] 백업 완료
- [ ] 피크 시간 회피  
- [ ] 애플리케이션 Dual Write 준비 완료
- [ ] 롤백 계획 수립

### Feature Flag 적용 순서
1. **10%** - 소수 사용자 대상 테스트
2. **50%** - 절반 사용자 대상 안정성 검증  
3. **100%** - 전체 적용

### 복제 지연 대응
- 중요한 읽기는 `fromMaster=true` 옵션 사용
- 일반적인 목록 조회는 Replica 활용
- 쓰기 후 즉시 읽기가 필요한 경우 Master 사용

## 🔄 롤백 시나리오

### Feature Flag 롤백
```bash
# Dual Write 비활성화
curl -X PUT http://localhost:8080/api/feature-flags/dual-write \
  -H "Content-Type: application/json" \
  -d '{"enabled":false}'

# 점진적 롤백
curl -X PUT http://localhost:8080/api/feature-flags/percentage-rollout \
  -H "Content-Type: application/json" \
  -d '{"percentage":0}'
```

### DDL 롤백 (Contract 단계에서)
```sql
-- 기존 컬럼 복구 (백업에서)
ALTER TABLE users ADD COLUMN first_name VARCHAR(50);
ALTER TABLE users ADD COLUMN last_name VARCHAR(50);

-- 데이터 복구
UPDATE users SET 
  first_name = SUBSTRING_INDEX(full_name, ' ', 1),
  last_name = SUBSTRING_INDEX(full_name, ' ', -1)
WHERE first_name IS NULL;
```

## 🎯 핵심 포인트

1. **Online DDL**: `ALGORITHM=INSTANT, LOCK=NONE` 명시
2. **Feature Flag**: 점진적 배포로 위험 최소화  
3. **Dual Write**: 구/신 스키마 동시 지원
4. **일관성**: Master 강제 읽기로 복제 지연 대응
5. **모니터링**: 각 단계별 상태 추적 필수

이 실습을 통해 대규모 서비스에서 안전하게 스키마를 변경하는 전체 프로세스를 경험할 수 있습니다.
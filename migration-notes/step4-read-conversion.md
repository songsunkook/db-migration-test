# Migration Progress

## 현재 단계: 4단계 - Read Conversion (읽기 전환)

### 수행 작업
- Feature Flag를 사용한 점진적 읽기 전환 구현
- 새로운 스키마(`full_name`)로 읽기 시작
- 기존 스키마는 백업/대체 역할 유지
- Dual Write는 계속 유지

#### 사용자 식별 및 일관된 경험 보장
- **헤더 기반 사용자 식별**: `userId` 헤더로 사용자를 구분
- **해시 기반 일관성**: Togglz `GradualActivationStrategy`가 사용자 ID를 해싱하여 동일 사용자는 항상 동일한 결과 보장
  - 한 번 새로운 스키마로 분기된 사용자는 설정 변경 전까지 계속 새로운 스키마 사용

```java
// TogglzConfig.java - 헤더 기반 사용자 식별
@Bean
public UserProvider userProvider() {
    return () -> {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        
        // userId 헤더로 사용자 식별
        String userId = request.getHeader("userId");
        if (userId != null && !userId.trim().isEmpty()) {
            return new SimpleFeatureUser(userId.trim(), false);
        }
        return new SimpleFeatureUser("anonymous", false);
    };
}
```

#### User 도메인 수정
```java
public class User {
    // 기존 필드들...
    
    @Column(name = "full_name")
    private String fullName;
    
    // Feature Flag 기반 읽기 로직
    public String getFullName() {
        // Togglz가 현재 HTTP 요청의 사용자 컨텍스트에서 자동으로 판단
        boolean useNewSchema = FeatureFlags.USE_NEW_SCHEMA.isActive();

        if (useNewSchema && fullName != null) {
            return fullName;
        }
        if (fullName == null) {
            if (useNewSchema) {
                // 이미 읽기 전환되었는데 신규 스키마가 null인 경우 로그 남기기
                log.warn("Full name is null, falling back to firstName + lastName");
            }
            return firstName + " " + lastName;
        }
        return fullName;
    }
}
```

### 데이터 안정성
- ✅ **Dual Write 유지**: 모든 쓰기는 양쪽 스키마에 저장
- ✅ **Fallback 보장**: 새로운 스키마 실패 시 기존 스키마로 대체
- ✅ **무중단 전환**: Feature Flag로 읽기 전환 비율 무중단 조정 가능
- ✅ **롤백 가능**: 언제든 Feature Flag로 기존 스키마 복귀 가능

### 설정 변경 방법

#### 1. Togglz 관리 콘솔 (권장)
```
http://localhost:8080/togglz-console
```

- Use New Schema for Reading의 Activation Strategy를 Gradual Rollout으로 설정
- Enabled를 활성화하고 Percentage를 원하는 비율(예: 30)로 설정
- 웹 UI를 통한 실시간 Feature Flag 관리
- 재배포 없이 즉시 적용
- GradualActivationStrategy로 0-100% 점진적 롤아웃 가능

#### 2. REST API를 통한 확인
```bash
# 현재 상태 조회
curl -X GET http://localhost:8080/api/feature-flags/USE_NEW_SCHEMA

# 특정 사용자의 분기 상태 확인
curl -X GET http://localhost:8080/api/feature-flags/current-user \
  -H "userId: user123"
```

#### 3. 사용자별 분기 테스트
```bash
# 30% 설정 시 동일 사용자는 항상 동일한 결과
for i in {1..5}; do
  curl -H "userId: user123" http://localhost:8080/api/feature-flags/current-user
done

# 다른 사용자들로 분포 확인
for user in user001 user002 user003 user004 user005; do
  curl -H "userId: $user" http://localhost:8080/api/feature-flags/current-user
done
```

### 테스트 결과
- ✅ 모든 기존 테스트 통과
- ✅ Feature Flag false: 기존 동작 유지
- ✅ Feature Flag true: 새로운 스키마 사용 (3단계 Back Fill 후)
- ✅ Fallback 로직 정상 작동

### 다음 단계 준비사항
- 5단계: Cleanup (코드 정리)
- 새로운 스키마만 사용하도록 코드 정리

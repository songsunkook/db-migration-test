# Migration Progress

## 현재 단계: 2단계 - Dual Write (이중 쓰기)

### 수행 작업
- 애플리케이션 코드에서 기존 스키마와 새로운 스키마에 동시 쓰기
- `User` 엔티티에 `fullName` 필드 추가
- 생성/수정 시 `firstName + lastName`과 `fullName`에 동시 저장

### 테이블 구조 (변경 없음)
```sql
-- users 테이블 구조 (2단계)
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

### 애플리케이션 코드 변경사항

#### User 엔티티 수정
```java
@Entity
@Table(name = "users")
public class User {
    // 기존 필드들...
    
    @Column(name = "full_name")
    private String fullName;  // 새로 추가된 필드
    
    public User(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.fullName = firstName + " " + lastName;  // Dual Write
        // ...
    }
    
    public void updateName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;  // Dual Write
        // ...
    }
}
```

### Dual Write 동작 방식
1. **사용자 생성**: `firstName`, `lastName`, `fullName` 모두 저장
2. **사용자 수정**: `firstName`, `lastName`, `fullName` 모두 업데이트
3. **읽기**: 기존 스키마 사용: `fullName = firstName + lastName`

### 데이터 상태
- ✅ 새로 생성되는 데이터: 기존/신규 컬럼 양쪽에 저장
- ⚠️ 기존 데이터: `full_name`이 NULL인 상태 유지
- ✅ 기존 API 동작: 변경 없음 (하위 호환성 보장)

### 테스트 결과
- ✅ 모든 기존 테스트 통과
- ✅ JSONAssert를 통한 응답 포맷 검증 정상
- ✅ 사용자 생성/조회/수정 API 정상 작동

### 다음 단계 준비사항
- 3단계: Back Fill 구현
- 기존 데이터 마이그레이션 작업

### 주의사항
- 기존 데이터의 `full_name` 컬럼은 여전히 NULL
- 데이터 마이그레이션은 별도 작업 필요(3단계)
- 모든 쓰기 작업이 양쪽 스키마에 반영되어야 함

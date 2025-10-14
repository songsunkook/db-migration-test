# 📜 Scripts

간단한 실행 스크립트입니다.

## 🚀 사용법

### 환경 시작
```bash
./scripts/start.sh
```
MySQL 컨테이너와 레플리케이션을 자동으로 설정합니다.

### 상태 확인
```bash
./scripts/check.sh
```
레플리케이션 상태를 확인합니다.

### 환경 정리
```bash
./scripts/stop.sh
```
모든 컨테이너를 중지합니다.

## 📋 실행 순서

```bash
# 1. 환경 시작
./scripts/start.sh

# 2. 애플리케이션 실행
./gradlew bootRun

# 3. 브라우저에서 테스트
# - Swagger UI: http://localhost:8080/swagger-ui.html
# - Togglz 콘솔: http://localhost:8080/togglz-console
```

## 📚 DB Migration 실습

실제 무중단 스키마 변경은 [MIGRATION_GUIDE.md](../MIGRATION_GUIDE.md)를 참고하세요.
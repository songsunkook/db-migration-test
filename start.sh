#!/bin/bash

echo "🚀 DB Migration 실습 환경을 시작합니다..."

# Docker Compose 실행
echo "📦 MySQL Master/Replica 환경 시작 중..."
docker-compose up -d

# 컨테이너 시작 대기
echo "⏱️ MySQL 컨테이너 시작 대기 중..."
sleep 10

# 컨테이너 상태 확인
echo "📊 컨테이너 상태 확인:"
docker-compose ps

echo ""
echo "✅ 환경 준비 완료!"
echo ""
echo "🔧 다음 단계:"
echo "1. 애플리케이션 실행: ./gradlew bootRun"
echo "2. 실습 가이드 확인: cat MIGRATION_GUIDE.md"
echo "3. API 테스트: curl http://localhost:8080/api/users"
echo "4. Feature Flag 콘솔: http://localhost:8080/togglz-console"
echo ""
echo "📚 주요 문서:"
echo "- README.md: 프로젝트 개요"
echo "- MIGRATION_GUIDE.md: 상세 실습 가이드"
echo ""
echo "🎯 이 실습에서 배울 내용:"
echo "- Online DDL vs Offline DDL"
echo "- Expand-Contract 패턴"
echo "- Dual Write 전략"
echo "- Togglz Feature Flag (웹 콘솔 포함)"
echo "- Read-After-Write Consistency"
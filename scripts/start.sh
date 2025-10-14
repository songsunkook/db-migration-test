#!/bin/bash

# DB Migration 프로젝트 시작 스크립트

echo "🚀 DB Migration 환경을 시작합니다..."

# Docker 컨테이너 시작
echo "1️⃣ MySQL 컨테이너 시작 중..."
docker run -d \
  --name mysql-db \
  -e MYSQL_ROOT_PASSWORD=0000 \
  -e MYSQL_DATABASE=test_db \
  -p 3306:3306 \
  mysql:8.0

echo "2️⃣ MySQL 초기화 대기 중..."
sleep 20

# 연결 테스트
echo "3️⃣ 연결 확인 중..."
if ! docker exec mysql-db mysql -u root --password=0000 -e "SELECT 1" 2>/dev/null >/dev/null; then
    echo "❌ MySQL 연결 실패. 다시 시도해주세요."
    exit 1
fi

echo ""
echo "🎯 환경 준비 완료!"
echo "==============================================="
echo "📋 연결 정보:"
echo "  MySQL DB: localhost:3306 (root/0000)"
echo "  Database: test_db"
echo ""
echo "🚀 다음 단계:"
echo "  ./gradlew bootRun"
echo "  http://localhost:8080/swagger-ui.html"
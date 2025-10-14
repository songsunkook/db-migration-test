#!/bin/bash

# DB Migration 프로젝트 시작 스크립트

echo "🚀 DB Migration 환경을 시작합니다..."

# Docker 컨테이너 시작
echo "1️⃣ MySQL 컨테이너 시작 중..."
docker compose up -d

sleep 15

# 연결 테스트
echo "2️⃣ 연결 확인 중..."
if ! docker exec mysql-master mysql -u root -h 127.0.0.1 --password=0000 -e "SELECT 1" 2>/dev/null >/dev/null; then
    echo "❌ MySQL 연결 실패. 다시 시도해주세요."
    exit 1
fi

# 레플리케이션 설정
echo "3️⃣ 레플리케이션 설정 중..."
docker exec mysql-replica mysql -u root -h 127.0.0.1 --password=0000 -e "
    STOP SLAVE;
    RESET SLAVE ALL;
    CHANGE MASTER TO
        MASTER_HOST='mysql-master',
        MASTER_USER='replica_user',
        MASTER_PASSWORD='replica_password',
        MASTER_AUTO_POSITION=1;
    START SLAVE;
" 2>/dev/null

sleep 3

# 결과 확인
SLAVE_STATUS=$(docker exec mysql-replica mysql -u root -h 127.0.0.1 --password=0000 -e "SHOW SLAVE STATUS\G" 2>/dev/null)
IO_RUNNING=$(echo "$SLAVE_STATUS" | grep "Slave_IO_Running:" | awk '{print $2}')
SQL_RUNNING=$(echo "$SLAVE_STATUS" | grep "Slave_SQL_Running:" | awk '{print $2}')

echo ""
echo "🎯 환경 준비 완료!"
echo "==============================================="
echo "📋 연결 정보:"
echo "  Master: localhost:3306 (root/0000)"
echo "  Replica: localhost:3307 (root/0000)"
if [ "$IO_RUNNING" = "Yes" ] && [ "$SQL_RUNNING" = "Yes" ]; then
    echo "  레플리케이션: ✅ 활성화"
else
    echo "  레플리케이션: ⚠️ 확인 필요"
    echo ""
    echo "🔍 레플리케이션 상태 확인:"
    echo "  ./scripts/check.sh"
fi
echo ""
echo "🚀 다음 단계:"
echo "  ./gradlew bootRun"
echo "  http://localhost:8080/swagger-ui.html"
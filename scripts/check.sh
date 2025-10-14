#!/bin/bash

# 레플리케이션 상태 확인 스크립트

echo "🔍 레플리케이션 상태를 확인합니다..."
echo ""

# Master 상태
echo "📊 Master 상태:"
echo "----------------------------------------"
docker exec mysql-master mysql -u root -h 127.0.0.1 --password=0000 -e "SHOW MASTER STATUS\G" 2>/dev/null

echo ""

# Replica 상태
echo "📊 Replica 상태:"
echo "----------------------------------------"
SLAVE_STATUS=$(docker exec mysql-replica mysql -u root -h 127.0.0.1 --password=0000 -e "SHOW SLAVE STATUS\G" 2>/dev/null)

if [ -z "$SLAVE_STATUS" ]; then
    echo "❌ 레플리케이션이 설정되지 않았습니다."
    echo ""
    echo "🛠️ 해결 방법:"
    echo "  ./scripts/start.sh  # 다시 실행해보세요"
else
    echo "$SLAVE_STATUS"
    echo ""
    
    # 핵심 정보만 추출
    IO_RUNNING=$(echo "$SLAVE_STATUS" | grep "Slave_IO_Running:" | awk '{print $2}')
    SQL_RUNNING=$(echo "$SLAVE_STATUS" | grep "Slave_SQL_Running:" | awk '{print $2}')
    SECONDS_BEHIND=$(echo "$SLAVE_STATUS" | grep "Seconds_Behind_Master:" | awk '{print $2}')
    
    echo "🔍 핵심 정보:"
    echo "  IO Thread: $IO_RUNNING"
    echo "  SQL Thread: $SQL_RUNNING"
    echo "  지연 시간: ${SECONDS_BEHIND}초"
    echo ""
    
    if [ "$IO_RUNNING" = "Yes" ] && [ "$SQL_RUNNING" = "Yes" ]; then
        echo "✅ 레플리케이션이 정상 작동 중입니다!"
    else
        echo "❌ 레플리케이션에 문제가 있습니다."
        
        echo ""
        echo "🛠️ 문제 해결:"
        echo "  ./scripts/fix-replication.sh"
    fi
fi


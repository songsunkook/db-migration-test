#!/bin/bash

# 레플리케이션 인증 문제 수정 스크립트

echo "🔧 레플리케이션 인증 문제를 수정합니다..."

# Master에서 replica_user를 mysql_native_password로 다시 생성
echo "1️⃣ Master에서 replica_user 재설정..."
docker exec mysql-master mysql -u root -h 127.0.0.1 --password=0000 -e "
    DROP USER IF EXISTS 'replica_user'@'%';
    CREATE USER 'replica_user'@'%' IDENTIFIED WITH mysql_native_password BY 'replica_password';
    GRANT REPLICATION SLAVE ON *.* TO 'replica_user'@'%';
    GRANT SELECT ON *.* TO 'replica_user'@'%';
    FLUSH PRIVILEGES;
" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "   ✅ replica_user 재설정 완료"
else
    echo "   ❌ replica_user 설정 실패"
    exit 1
fi

# Replica에서 레플리케이션 재시작
echo "2️⃣ Replica 레플리케이션 재시작..."
docker exec mysql-replica mysql -u root -h 127.0.0.1 --password=0000 -e "
    STOP SLAVE;
    RESET SLAVE ALL;
    CHANGE MASTER TO
        MASTER_HOST='mysql-master',
        MASTER_USER='replica_user',
        MASTER_PASSWORD='replica_password',
        MASTER_AUTO_POSITION=1,
        GET_MASTER_PUBLIC_KEY=1;
    START SLAVE;
" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "   ✅ 레플리케이션 재시작 완료"
else
    echo "   ❌ 레플리케이션 재시작 실패"
    exit 1
fi

# 잠시 대기 후 상태 확인
echo "3️⃣ 상태 확인 중..."
sleep 5

SLAVE_STATUS=$(docker exec mysql-replica mysql -u root -h 127.0.0.1 --password=0000 -e "SHOW SLAVE STATUS\G" 2>/dev/null)
IO_RUNNING=$(echo "$SLAVE_STATUS" | grep "Slave_IO_Running:" | awk '{print $2}')
SQL_RUNNING=$(echo "$SLAVE_STATUS" | grep "Slave_SQL_Running:" | awk '{print $2}')

echo ""
if [ "$IO_RUNNING" = "Yes" ] && [ "$SQL_RUNNING" = "Yes" ]; then
    echo "🎉 레플리케이션 수정 완료!"
    echo "   IO Thread: $IO_RUNNING"
    echo "   SQL Thread: $SQL_RUNNING"
else
    echo "⚠️ 여전히 문제가 있습니다:"
    echo "   IO Thread: $IO_RUNNING"  
    echo "   SQL Thread: $SQL_RUNNING"
    echo ""
    echo "🔍 상세 확인: ./scripts/check.sh"
fi
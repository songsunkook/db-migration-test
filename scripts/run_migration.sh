#!/bin/bash

# DB 연결 정보
HOST="mysql-master"
PORT="3307"
USER="root"
PASSWORD="0000"
DATABASE="moko"

echo "=== DB Offline Migration - Expand-Contract Pattern ==="
echo "경고: 이 스크립트는 OFFLINE DDL을 실행하므로 서비스 중단이 발생합니다."
echo ""

# 1단계: 초기 테이블 생성
echo "1단계: 초기 users 테이블 생성"
read -p "실행하시겠습니까? (y/n): " confirm
if [ "$confirm" = "y" ]; then
    mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE < src/main/resources/db/migration/01_create_users_table.sql
    echo "✓ 초기 테이블 생성 완료"
else
    echo "✗ 1단계 건너뜀"
fi

echo ""

# 2단계: Expand - 새 컬럼 추가
echo "2단계: Expand - full_name 컬럼 추가"
echo "경고: 이 작업은 테이블 락을 발생시켜 서비스가 일시 중단됩니다."
read -p "실행하시겠습니까? (y/n): " confirm
if [ "$confirm" = "y" ]; then
    mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE < src/main/resources/db/migration/02_expand_add_full_name_column.sql
    echo "✓ Expand 단계 완료"
    echo "이제 애플리케이션을 배포하여 dual write/read 로직을 적용하세요."
else
    echo "✗ 2단계 건너뜀"
fi

echo ""

# 3단계: Contract - 기존 컬럼 제거
echo "3단계: Contract - name 컬럼 제거"
echo "경고: 이 작업은 테이블 락을 발생시켜 서비스가 일시 중단됩니다."
echo "주의: 애플리케이션이 full_name만 사용하도록 배포된 후에만 실행하세요."
read -p "실행하시겠습니까? (y/n): " confirm
if [ "$confirm" = "y" ]; then
    mysql -h $HOST -P $PORT -u $USER -p$PASSWORD $DATABASE < src/main/resources/db/migration/03_contract_remove_name_column.sql
    echo "✓ Contract 단계 완료"
    echo "마이그레이션이 모두 완료되었습니다."
else
    echo "✗ 3단계 건너뜀"
fi

echo ""
echo "=== 마이그레이션 스크립트 종료 ==="
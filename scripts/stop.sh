#!/bin/bash

# DB Migration 프로젝트 정리 스크립트

echo "🛑 MySQL 환경을 정리합니다..."

docker compose down

echo "✅ 정리 완료!"
echo ""
echo "💡 데이터까지 삭제: docker compose down -v"
echo "🚀 재시작: ./scripts/start.sh"
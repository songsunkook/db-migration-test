#!/bin/bash

# DB Migration 프로젝트 정리 스크립트

echo "🛑 MySQL 환경을 정리합니다..."

# MySQL 컨테이너 정리
docker stop mysql-db 2>/dev/null || true
docker rm mysql-db 2>/dev/null || true

echo "✅ 정리 완료!"
echo ""
echo "🚀 재시작: ./scripts/start.sh"
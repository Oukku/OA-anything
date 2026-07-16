#!/bin/bash
# 一键启动开发环境 (MySQL + Redis 假设已运行)
# 用法: ./dev-start.sh
set -e

OaRoot="$(cd "$(dirname "$0")" && pwd)"
ProjectDir="$OaRoot/springboot"
FrontendDir="$ProjectDir/src/main/resources/admin/admin"

echo "=== 1. 初始化数据库 ==="
mysql -uroot -p123456 < "$OaRoot/db-v2.sql"
echo "  ✓ 数据库已初始化"

echo ""
echo "=== 2. 启动Redis (假设已通过 redis-server 或服务运行) ==="
redis-cli ping 2>/dev/null || {
  echo "  ✗ Redis未运行, 请先启动: redis-server"
  exit 1
}
echo "  ✓ Redis运行中"

echo ""
echo "=== 3. 启动Spring Boot后端 ==="
cd "$ProjectDir"
mvn spring-boot:run -Dspring-boot.run.profiles=dev

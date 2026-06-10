#!/bin/bash
# 图书馆管理系统 - 启动脚本
# 启动顺序: Nacos → auth-service/backend → gateway → frontend

set -e
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
NACOS_HOME="$HOME/Code/Homework/nacos-hw/nacos"


# --------------- 1. Nacos ---------------
echo "[1/5] 检查 Nacos..."
if curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8848/nacos 2>/dev/null | grep -q "302\|200"; then
    echo "  Nacos 已运行 (8848)"
else
    echo "  启动 Nacos..."
    nohup java \
        --add-opens java.base/java.io=ALL-UNNAMED \
        --add-opens java.base/java.lang=ALL-UNNAMED \
        --add-opens java.base/java.util=ALL-UNNAMED \
        --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
        --add-opens java.base/sun.net.www=ALL-UNNAMED \
        -Xms512m -Xmx512m -Xmn256m \
        -Dnacos.standalone=true \
        -Dnacos.home="$NACOS_HOME" \
        -Dloader.path="$NACOS_HOME/plugins,$NACOS_HOME/plugins/health,$NACOS_HOME/plugins/cmdb,$NACOS_HOME/plugins/selector" \
        -jar "$NACOS_HOME/target/nacos-server.jar" \
        --spring.config.additional-location="file:$NACOS_HOME/conf/" \
        --logging.config="$NACOS_HOME/conf/nacos-logback.xml" \
        --server.max-http-header-size=524288 \
        > /tmp/nacos.log 2>&1 &
    echo "  Nacos PID: $!"
    echo "  等待 Nacos 就绪..."
    for i in $(seq 1 60); do
        code=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8848/nacos 2>/dev/null)
        if [ "$code" = "302" ] || [ "$code" = "200" ]; then
            echo "  Nacos 就绪 (${i}s)"
            break
        fi
        sleep 1
    done
fi

# --------------- 2. auth-service ---------------
echo "[2/5] 启动 auth-service (8083)..."
cd "$PROJECT_DIR" && mvn -pl auth-service spring-boot:run -q > /tmp/auth-service.log 2>&1 &
echo "  PID: $!"

# --------------- 3. backend ---------------
echo "[3/5] 启动 backend (8081)..."
cd "$PROJECT_DIR" && mvn -pl backend spring-boot:run -q > /tmp/backend.log 2>&1 &
echo "  PID: $!"

# --------------- 4. gateway ---------------
echo "[4/5] 启动 gateway (8080)..."
cd "$PROJECT_DIR" && mvn -pl gateway spring-boot:run -q > /tmp/gateway.log 2>&1 &
echo "  PID: $!"

# --------------- 5. frontend ---------------
echo "[5/5] 启动 frontend (5173)..."
cd "$PROJECT_DIR/frontend" && npm run dev > /tmp/frontend.log 2>&1 &
echo "  PID: $!"

# --------------- 等待就绪 ---------------
echo ""
echo "等待服务就绪..."
sleep 10

check_port() {
    lsof -i ":$1" 2>/dev/null | grep -q LISTEN && echo "  ✔ $2 (:$1)" || echo "  ✘ $2 (:$1) 未启动"
}
check_port 8083 "auth-service"
check_port 8081 "backend"
check_port 8080 "gateway"
check_port 5173 "frontend"

echo ""
echo "============================================"
echo "  启动完成！"
echo "  前端: http://localhost:5173"
echo "  网关: http://localhost:8080"
echo "  账号: admin / admin123"
echo "============================================"

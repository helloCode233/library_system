#!/bin/bash
# AI智能图书管理系统 - 关闭脚本

echo "============================================"
echo "  AI智能图书管理系统 - 关闭中..."
echo "============================================"

kill_port() {
    local pid=$(lsof -t -i:"$1" 2>/dev/null)
    if [ -n "$pid" ]; then
        kill $pid 2>/dev/null
        echo "  ✔ 关闭 $2 (:$1) PID: $pid"
    else
        echo "  - $2 (:$1) 未运行"
    fi
}

kill_port 8080 "gateway"
kill_port 8083 "auth-service"
kill_port 8081 "backend"
kill_port 5173 "frontend"

# 可选: 关闭 Nacos（取消注释以启用）
# kill_port 8848 "nacos"

echo "============================================"
echo "  关闭完成！"
echo "============================================"

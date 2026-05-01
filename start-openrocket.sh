#!/bin/bash

set -e

echo "============================================"
echo "  OpenRocket 一键启动脚本"
echo "============================================"
echo ""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "[检查 Java 环境...]"
if java -version 2>&1 | grep -q "17\."; then
    echo "[✓] 已找到 JDK 17"
    echo ""
else
    echo "[✗] 当前 Java 版本不是 JDK 17"
    echo ""
    echo "[尝试查找 JDK 17...]"
    
    JAVA_17_FOUND=false
    
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        if "$JAVA_HOME/bin/java" -version 2>&1 | grep -q "17\."; then
            echo "[✓] 在 JAVA_HOME 中找到 JDK 17: $JAVA_HOME"
            export PATH="$JAVA_HOME/bin:$PATH"
            JAVA_17_FOUND=true
        fi
    fi
    
    if [ "$JAVA_17_FOUND" = false ]; then
        echo "[✗] 未找到 JDK 17"
        echo ""
        echo "============================================"
        echo "  错误：需要 JDK 17 才能运行 OpenRocket"
        echo "============================================"
        echo ""
        echo "请按以下步骤操作："
        echo "1. 下载并安装 JDK 17："
        echo "   - 推荐：https://adoptium.net/temurin/releases/?version=17"
        echo "   - 或：https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html"
        echo ""
        echo "2. 安装后重新运行此脚本"
        echo ""
        echo "或者，如果已安装 JDK 17 但未检测到："
        echo "   - 设置 JAVA_HOME 环境变量指向 JDK 17 安装目录"
        echo "   - 或将 JDK 17 的 bin 目录添加到 PATH 环境变量"
        echo ""
        exit 1
    fi
fi

echo ""
echo "[开始构建并启动 OpenRocket...]"
echo "============================================"
echo ""

./gradlew run --no-daemon

echo ""
echo "============================================"
echo "  OpenRocket 已成功启动"
echo "============================================"

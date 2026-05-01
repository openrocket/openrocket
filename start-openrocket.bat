@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ============================================
echo   OpenRocket 一键启动脚本
echo ============================================
echo.

set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

echo [检查 Java 环境...]
java -version 2>&1 | findstr "17\." >nul
if %errorlevel% equ 0 (
    echo [✓] 已找到 JDK 17
    echo.
) else (
    echo [✗] 当前 Java 版本不是 JDK 17
    echo.
    echo [尝试查找 JDK 17...]
    
    set "JAVA_17_FOUND=false"
    
    if defined JAVA_HOME (
        "!JAVA_HOME!\bin\java.exe" -version 2>&1 | findstr "17\." >nul
        if !errorlevel! equ 0 (
            echo [✓] 在 JAVA_HOME 中找到 JDK 17: !JAVA_HOME!
            set "PATH=!JAVA_HOME!\bin;!PATH!"
            set "JAVA_17_FOUND=true"
        )
    )
    
    if "!JAVA_17_FOUND!"=="false" (
        echo [✗] 未找到 JDK 17
        echo.
        echo ============================================
        echo   错误：需要 JDK 17 才能运行 OpenRocket
        echo ============================================
        echo.
        echo 请按以下步骤操作：
        echo 1. 下载并安装 JDK 17：
        echo    - 推荐：https://adoptium.net/temurin/releases/?version=17
        echo    - 或：https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
        echo.
        echo 2. 安装后重新运行此脚本
        echo.
        echo 或者，如果已安装 JDK 17 但未检测到：
        echo    - 设置 JAVA_HOME 环境变量指向 JDK 17 安装目录
        echo    - 或将 JDK 17 的 bin 目录添加到 PATH 环境变量
        echo.
        pause
        exit /b 1
    )
)

echo.
echo [开始构建并启动 OpenRocket...]
echo ============================================
echo.

gradlew.bat run --no-daemon

if %errorlevel% equ 0 (
    echo.
    echo ============================================
    echo   OpenRocket 已成功启动
    echo ============================================
) else (
    echo.
    echo ============================================
    echo   启动失败，错误代码: %errorlevel%
    echo ============================================
    echo.
    echo 可能的解决方法：
    echo 1. 确保网络连接正常（首次运行需要下载依赖）
    echo 2. 尝试运行: gradlew.bat clean build
    echo 3. 检查 JDK 17 是否正确安装
    echo.
    pause
    exit /b %errorlevel%
)

endlocal

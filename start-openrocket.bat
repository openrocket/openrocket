@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   OpenRocket 一键启动脚本
echo ============================================
echo.

set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

echo [检查 Java 环境...]
echo.

set "JAVA_VERSION="
for /f "tokens=3" %%a in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set "JAVA_VERSION=%%a"
    set "JAVA_VERSION=!JAVA_VERSION:"=!"
)

echo [调试] 检测到的 Java 版本字符串: !JAVA_VERSION!
echo.

set "IS_JAVA_17=false"

if not "!JAVA_VERSION!"=="" (
    echo !JAVA_VERSION! | findstr "^17" >nul
    if !errorlevel! equ 0 (
        set "IS_JAVA_17=true"
    )
    
    echo !JAVA_VERSION! | findstr "^1\.17" >nul
    if !errorlevel! equ 0 (
        set "IS_JAVA_17=true"
    )
)

if "!IS_JAVA_17!"=="true" (
    echo [OK] 已找到 JDK 17
    echo.
) else (
    echo [警告] 当前 Java 版本不是 JDK 17，尝试查找 JDK 17...
    echo.
    
    set "JAVA_17_FOUND=false"
    
    if defined JAVA_HOME (
        echo [调试] 检查 JAVA_HOME: !JAVA_HOME!
        if exist "!JAVA_HOME!\bin\java.exe" (
            set "TEMP_JAVA_VERSION="
            for /f "tokens=3" %%a in ('"!JAVA_HOME!\bin\java.exe" -version 2^>^&1 ^| findstr /i "version"') do (
                set "TEMP_JAVA_VERSION=%%a"
                set "TEMP_JAVA_VERSION=!TEMP_JAVA_VERSION:"=!"
            )
            echo [调试] JAVA_HOME 中的 Java 版本: !TEMP_JAVA_VERSION!
            
            if not "!TEMP_JAVA_VERSION!"=="" (
                echo !TEMP_JAVA_VERSION! | findstr "^17" >nul
                if !errorlevel! equ 0 (
                    echo [OK] 在 JAVA_HOME 中找到 JDK 17: !JAVA_HOME!
                    set "PATH=!JAVA_HOME!\bin;!PATH!"
                    set "JAVA_17_FOUND=true"
                )
                
                if "!JAVA_17_FOUND!"=="false" (
                    echo !TEMP_JAVA_VERSION! | findstr "^1\.17" >nul
                    if !errorlevel! equ 0 (
                        echo [OK] 在 JAVA_HOME 中找到 JDK 17: !JAVA_HOME!
                        set "PATH=!JAVA_HOME!\bin;!PATH!"
                        set "JAVA_17_FOUND=true"
                    )
                )
            )
        )
    )
    
    if "!JAVA_17_FOUND!"=="false" (
        echo.
        echo ============================================
        echo   错误：需要 JDK 17 才能运行 OpenRocket
        echo ============================================
        echo.
        echo 当前检测到的 Java 版本: !JAVA_VERSION!
        echo.
        echo 请按以下步骤操作：
        echo 1. 下载并安装 JDK 17：
        echo    - 推荐：https://adoptium.net/temurin/releases/?version=17
        echo    - 或：https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
        echo.
        echo 2. 安装后设置 JAVA_HOME 环境变量指向 JDK 17 安装目录
        echo.
        echo 或者，如果已安装 JDK 17 但未检测到：
        echo    - 请确保 JDK 17 的 bin 目录在 PATH 环境变量的最前面
        echo    - 或设置 JAVA_HOME 环境变量指向 JDK 17 安装目录
        echo.
        pause
        exit /b 1
    )
)

echo.
echo [开始构建并启动 OpenRocket...]
echo ============================================
echo.

call gradlew.bat run --no-daemon

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

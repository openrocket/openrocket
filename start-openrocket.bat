@echo off
setlocal enabledelayedexpansion

chcp 936 >nul 2>nul

echo ============================================
echo   OpenRocket Start Script
echo ============================================
echo.

set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

echo [Checking Java environment...]
echo.

set "JAVA_PATH="
for %%i in (java.exe) do set "JAVA_PATH=%%~$PATH:i"

if not defined JAVA_PATH (
    echo [ERROR] java.exe not found in PATH
    goto :java_not_found
)

echo [INFO] Found java.exe at: !JAVA_PATH!

set "JAVA_HOME_PATH="
for %%p in ("!JAVA_PATH!") do set "JAVA_HOME_PATH=%%~dpp"
set "JAVA_HOME_PATH=!JAVA_HOME_PATH:~0,-1!"
for %%p in ("!JAVA_HOME_PATH!") do set "JAVA_HOME_PATH=%%~dpp"
set "JAVA_HOME_PATH=!JAVA_HOME_PATH:~0,-1!"

echo [INFO] Java home detected as: !JAVA_HOME_PATH!
echo.

set "DETECTED_VERSION="
for /f "tokens=3" %%a in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set "DETECTED_VERSION=%%a"
    set "DETECTED_VERSION=!DETECTED_VERSION:"=!"
)

echo [INFO] Java version from PATH: !DETECTED_VERSION!
echo.

set "IS_JAVA_17=false"

if not "!DETECTED_VERSION!"=="" (
    echo !DETECTED_VERSION! | findstr /b "17" >nul
    if !errorlevel! equ 0 (
        set "IS_JAVA_17=true"
    )
    
    if "!IS_JAVA_17!"=="false" (
        echo !DETECTED_VERSION! | findstr /b "1\.17" >nul
        if !errorlevel! equ 0 (
            set "IS_JAVA_17=true"
        )
    )
)

if "!IS_JAVA_17!"=="true" (
    echo [OK] JDK 17 found in PATH
    echo.
    
    set "JAVA_HOME=!JAVA_HOME_PATH!"
    echo [INFO] Set JAVA_HOME to: !JAVA_HOME!
    
    endlocal & set "JAVA_HOME=!JAVA_HOME_PATH!"
    
    goto :start_application
) else (
    echo [WARNING] Java in PATH is not JDK 17
    echo.
    
    set "JAVA_17_FOUND=false"
    
    if defined JAVA_HOME (
        echo [INFO] Checking JAVA_HOME: !JAVA_HOME!
        if exist "!JAVA_HOME!\bin\java.exe" (
            set "HOME_JAVA_VERSION="
            for /f "tokens=3" %%a in ('"!JAVA_HOME!\bin\java.exe" -version 2^>^&1 ^| findstr /i "version"') do (
                set "HOME_JAVA_VERSION=%%a"
                set "HOME_JAVA_VERSION=!HOME_JAVA_VERSION:"=!"
            )
            echo [INFO] Java version in JAVA_HOME: !HOME_JAVA_VERSION!
            
            if not "!HOME_JAVA_VERSION!"=="" (
                echo !HOME_JAVA_VERSION! | findstr /b "17" >nul
                if !errorlevel! equ 0 (
                    echo [OK] Found JDK 17 in JAVA_HOME: !JAVA_HOME!
                    set "JAVA_17_FOUND=true"
                )
                
                if "!JAVA_17_FOUND!"=="false" (
                    echo !HOME_JAVA_VERSION! | findstr /b "1\.17" >nul
                    if !errorlevel! equ 0 (
                        echo [OK] Found JDK 17 in JAVA_HOME: !JAVA_HOME!
                        set "JAVA_17_FOUND=true"
                    )
                )
            )
        )
    )
    
    if "!JAVA_17_FOUND!"=="true" (
        goto :start_application
    )
    
    echo.
    echo [SEARCHING] Looking for JDK 17 in common locations...
    
    set "COMMON_DIRS=^
C:\Program Files\Java;^
C:\Program Files\Eclipse Adoptium;^
C:\Program Files\Microsoft;^
C:\Program Files\BellSoft;^
C:\Program Files\Azul\Zulu;^
C:\Program Files\Java\jdk-17;^
C:\Program Files\Eclipse Adoptium\jdk-17.*;^
C:\Program Files\Microsoft\jdk-17.*;^
C:\Program Files\BellSoft\LibericaJDK-17.*;^
C:\Program Files\Azul\Zulu\zulu-17.*"
    
    for %%d in (!COMMON_DIRS!) do (
        set "SEARCH_DIR=%%d"
        set "SEARCH_DIR=!SEARCH_DIR:;=!"
        
        if exist "!SEARCH_DIR!" (
            for /f "delims=" %%f in ('dir /b /ad "!SEARCH_DIR!\*17*" 2^>nul') do (
                set "CANDIDATE=!SEARCH_DIR!\%%f"
                if exist "!CANDIDATE!\bin\java.exe" (
                    set "CANDIDATE_VERSION="
                    for /f "tokens=3" %%a in ('"!CANDIDATE!\bin\java.exe" -version 2^>^&1 ^| findstr /i "version"') do (
                        set "CANDIDATE_VERSION=%%a"
                        set "CANDIDATE_VERSION=!CANDIDATE_VERSION:"=!"
                    )
                    
                    if not "!CANDIDATE_VERSION!"=="" (
                        echo !CANDIDATE_VERSION! | findstr /b "17" >nul
                        if !errorlevel! equ 0 (
                            echo [OK] Found JDK 17 at: !CANDIDATE!
                            set "JAVA_HOME=!CANDIDATE!"
                            set "JAVA_17_FOUND=true"
                            goto :found_java
                        )
                        
                        if "!JAVA_17_FOUND!"=="false" (
                            echo !CANDIDATE_VERSION! | findstr /b "1\.17" >nul
                            if !errorlevel! equ 0 (
                                echo [OK] Found JDK 17 at: !CANDIDATE!
                                set "JAVA_HOME=!CANDIDATE!"
                                set "JAVA_17_FOUND=true"
                                goto :found_java
                            )
                        )
                    )
                )
            )
        )
    )
    
    :found_java
    
    if "!JAVA_17_FOUND!"=="true" (
        endlocal & set "JAVA_HOME=%JAVA_HOME%"
        goto :start_application
    )
    
    goto :java_not_found
)

:start_application

echo.
echo [VERIFY] Confirming Java version...
for /f "tokens=3" %%a in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set "FINAL_VERSION=%%a"
    set "FINAL_VERSION=!FINAL_VERSION:"=!"
)
echo [INFO] Final Java version: !FINAL_VERSION!

if defined JAVA_HOME (
    echo [INFO] JAVA_HOME: !JAVA_HOME!
)
echo.

echo [STARTING] Building and launching OpenRocket...
echo ============================================
echo.

call gradlew.bat run --no-daemon

if %errorlevel% equ 0 (
    echo.
    echo ============================================
    echo   OpenRocket started successfully
    echo ============================================
) else (
    echo.
    echo ============================================
    echo   Failed to start, error code: %errorlevel%
    echo ============================================
    echo.
    echo Possible solutions:
    echo 1. Ensure network connection is working (first run needs to download dependencies)
    echo 2. Try running: gradlew.bat clean build
    echo 3. Check JDK 17 is properly installed
    echo.
    pause
    exit /b %errorlevel%
)

goto :end

:java_not_found

echo.
echo ============================================
echo   ERROR: JDK 17 not found
echo ============================================
echo.
echo Current Java version detected: !DETECTED_VERSION!
echo Current JAVA_HOME (if set): !JAVA_HOME!
echo.
echo Please follow these steps:
echo.
echo Method 1: Set PATH priority (recommended)
echo    Move JDK 17's bin directory to the front of PATH
echo    This ensures the system uses JDK 17 first
echo.
echo Method 2: Set JAVA_HOME
echo    Set JAVA_HOME environment variable to JDK 17 installation directory
echo    Example: set JAVA_HOME=C:\Program Files\Java\jdk-17
echo.
echo Method 3: Download and install JDK 17
echo    - Recommended: https://adoptium.net/temurin/releases/?version=17
echo    - Or: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
echo.
pause
exit /b 1

:end

endlocal

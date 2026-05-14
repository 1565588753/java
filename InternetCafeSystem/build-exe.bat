@echo off
chcp 65001 >nul
title 网吧计费管理系统 - EXE打包工具

echo ============================================
echo   网吧计费管理系统 - EXE 打包工具
echo ============================================
echo.

REM ==========================================
REM 第一步：检查 Java 环境
REM ==========================================
echo [1/4] 检查 Java 环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到 Java！请先安装 JDK 1.8 或更高版本。
    pause
    exit /b 1
)
echo [OK] Java 环境正常

REM ==========================================
REM 第二步：编译项目（如有 Maven）
REM ==========================================
echo [2/4] 编译项目...
if exist "pom.xml" (
    echo 检测到 Maven 项目，正在编译...
    call mvn clean package -DskipTests -q
    if %errorlevel% neq 0 (
        echo [错误] Maven 编译失败！
        pause
        exit /b 1
    )
    echo [OK] Maven 编译成功
    
    REM 复制 fat JAR
    if exist "target\InternetCafeSystem-1.0-SNAPSHOT-jar-with-dependencies.jar" (
        copy /Y "target\InternetCafeSystem-1.0-SNAPSHOT-jar-with-dependencies.jar" "InternetCafeSystem.jar" >nul
        echo [OK] 已复制 fat JAR
    )
) else (
    echo 使用已有的 InternetCafeSystem.jar
    if not exist "InternetCafeSystem.jar" (
        echo [错误] 未找到 InternetCafeSystem.jar！
        echo 请先运行 Maven 编译或将 JAR 文件放到当前目录。
        pause
        exit /b 1
    )
    echo [OK] 找到 InternetCafeSystem.jar
)

REM ==========================================
REM 第三步：检查 Launch4j
REM ==========================================
echo [3/4] 检查 Launch4j...
set LAUNCH4J_EXE=
if exist "launch4j\launch4jc.exe" (
    set LAUNCH4J_EXE=launch4j\launch4jc.exe
)
if exist "C:\Program Files\Launch4j\launch4jc.exe" (
    set LAUNCH4J_EXE=C:\Program Files\Launch4j\launch4jc.exe
)
if exist "C:\Program Files (x86)\Launch4j\launch4jc.exe" (
    set LAUNCH4J_EXE=C:\Program Files (x86)\Launch4j\launch4jc.exe
)

if "%LAUNCH4J_EXE%"=="" (
    echo.
    echo [提示] 未找到 Launch4j，将使用备用方案。
    echo.
    echo Launch4j 下载地址：https://launch4j.sourceforge.net/
    echo 下载后解压到当前目录的 launch4j 文件夹下，或安装到默认路径。
    echo.
    echo --- 备用方案：创建启动脚本 ---
    goto :create_bat
)

REM ==========================================
REM 第四步：生成 EXE
REM ==========================================
echo [4/4] 正在生成 EXE 文件...
echo 使用配置: launch4j-config.xml

"%LAUNCH4J_EXE%" launch4j-config.xml
if %errorlevel% neq 0 (
    echo [错误] EXE 生成失败！
    pause
    exit /b 1
)

echo.
echo ============================================
echo   EXE 生成成功！
echo   文件: InternetCafeSystem.exe
echo ============================================
echo.
echo 分发说明：
echo   1. 将 InternetCafeSystem.exe 和 jre 文件夹放在同一目录
echo   2. 确保 MySQL 数据库已配置并运行
echo   3. 双击 InternetCafeSystem.exe 即可运行
echo.
pause
exit /b 0

:create_bat
REM ==========================================
REM 备用方案：创建 bat 启动脚本
REM ==========================================
echo 正在创建启动脚本...
(
echo @echo off
echo title 网吧计费管理系统
echo chcp 65001 ^>nul
echo.
echo REM 查找 Java 运行时
echo set JAVA_EXE=
echo if exist "jre\bin\java.exe" set JAVA_EXE=jre\bin\java.exe
echo if exist "jre\bin\javaw.exe" set JAVA_EXE=jre\bin\javaw.exe
echo if "%%JAVA_EXE%%"=="" set JAVA_EXE=javaw
echo.
echo start "" "%%JAVA_EXE%%" -jar InternetCafeSystem.jar
) > "启动网吧计费管理系统.bat"

echo.
echo ============================================
echo   备用方案创建成功！
echo   文件: 启动网吧计费管理系统.bat
echo ============================================
echo.
echo 双击 启动网吧计费管理系统.bat 即可运行程序。
echo.
echo 要生成真正的 .exe 文件，请：
echo   1. 下载 Launch4j: https://launch4j.sourceforge.net/
echo   2. 解压到当前目录的 launch4j 文件夹
echo   3. 重新运行本脚本
echo.
pause
exit /b 0
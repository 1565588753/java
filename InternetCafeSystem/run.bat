@echo off
chcp 65001 >nul
title 网吧计费管理系统
echo 正在启动网吧计费管理系统...

REM 优先使用自带的 JRE
set JAVA_EXE=
if exist "jre\bin\javaw.exe" set JAVA_EXE=jre\bin\javaw.exe
if exist "jre\bin\java.exe"  set JAVA_EXE=jre\bin\java.exe

REM 如果自带 JRE 不存在，使用系统 JAVA_HOME
if "%JAVA_EXE%"=="" (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\javaw.exe" set JAVA_EXE=%JAVA_HOME%\bin\javaw.exe
        if exist "%JAVA_HOME%\bin\java.exe"  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
    )
)

REM 最后尝试 PATH 中的 java
if "%JAVA_EXE%"=="" set JAVA_EXE=javaw
if "%JAVA_EXE%"=="javaw" (
    where javaw >nul 2>&1
    if %errorlevel% neq 0 set JAVA_EXE=java
)

echo 使用 Java: %JAVA_EXE%
start "" "%JAVA_EXE%" -jar "%~dp0InternetCafeSystem.jar"
exit
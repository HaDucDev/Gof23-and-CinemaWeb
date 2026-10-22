@echo off
setlocal
chcp 65001 >nul
title CinemaMini - Initialize Problem PostgreSQL
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0database\initialize-data.ps1" %*
set "INIT_EXIT_CODE=%ERRORLEVEL%"
echo.
if "%INIT_EXIT_CODE%"=="0" (
    echo Initialization completed successfully.
) else (
    echo Initialization failed. Review the error above and the logs folder inside this stack.
)
echo.
pause
exit /b %INIT_EXIT_CODE%

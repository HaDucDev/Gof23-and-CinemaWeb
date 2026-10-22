@echo off
setlocal
chcp 65001 >nul
title CinemaMini - Initialize RabbitMQ
echo CinemaMini - Preparing RabbitMQ only
echo.
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0initialize-rabbitmq.ps1" %*
set "INIT_EXIT_CODE=%ERRORLEVEL%"
echo.
if "%INIT_EXIT_CODE%"=="0" (
    echo RabbitMQ initialization completed successfully.
) else (
    echo RabbitMQ initialization failed. Review the error above and the logs folder if present.
)
echo.
pause
exit /b %INIT_EXIT_CODE%

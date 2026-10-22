@echo off
setlocal
title CinemaMini - Frontend Problem

pushd "%~dp0frontend"
if errorlevel 1 (
  echo Khong tim thay thu muc frontend ben canh file BAT.
  pause
  exit /b 1
)

if not exist "node_modules\node\bin\node.exe" goto install
if not exist "node_modules\npm\bin\npm-cli.js" goto install
if not exist "node_modules\vite\bin\vite.js" goto install
if not exist "node_modules\react\package.json" goto install
if not exist "node_modules\react-dom\package.json" goto install
if not exist "node_modules\react-router-dom\package.json" goto install
goto run

:install
echo Dang cai cong cu va thu vien rieng cho du an. Lan dau can ket noi mang.
call ".\npm-project.cmd" install
if errorlevel 1 goto failed

:run
echo CinemaMini Frontend: http://127.0.0.1:5175
echo Giu cua so nay mo. Nhan Ctrl+C de dung frontend.
echo Backend can chay rieng tai cong 8083.
echo.
if /i "%~1"=="--no-open" (
  call ".\npm-project.cmd" run dev
) else (
  call ".\npm-project.cmd" run dev -- --open
)
set "frontendExitCode=%errorlevel%"
popd
if not "%frontendExitCode%"=="0" (
  echo.
  echo Khong chay duoc frontend. Xem loi o phia tren.
  pause
)
exit /b %frontendExitCode%

:failed
popd
echo.
echo Cai dat chua thanh cong. Kiem tra ket noi mang va loi o phia tren.
pause
exit /b 1

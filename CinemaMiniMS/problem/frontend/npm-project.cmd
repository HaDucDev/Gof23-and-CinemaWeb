@echo off
setlocal
if exist "%~dp0node_modules\node\bin\node.exe" (
  "%~dp0node_modules\node\bin\node.exe" "%~dp0scripts\project-npm.cjs" %*
) else (
  node "%~dp0scripts\project-npm.cjs" %*
)
exit /b %errorlevel%

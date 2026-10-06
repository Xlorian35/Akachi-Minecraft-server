@echo off
setlocal
cd /d "%~dp0"
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0install.ps1" -PackageZip "%~dp0AkachiLauncher.zip"
if errorlevel 1 (
  echo Akachi Launcher kurulumu basarisiz oldu.
  pause
  exit /b 1
)
exit /b 0
@echo off
setlocal
cd /d "%~dp0"

where javac >nul 2>nul
if errorlevel 1 (
    echo JDK 21 bulunamadi. JDK 21 kurup bu dosyayi tekrar calistir.
    echo https://adoptium.net/temurin/releases/?version=21
    pause
    exit /b 1
)

if not exist build\classes mkdir build\classes
javac --add-modules jdk.httpserver -encoding UTF-8 -d build\classes src\main\java\akachi\launcher\AkachiLauncher.java
if errorlevel 1 (
    echo Launcher derlenemedi. Yukaridaki hata mesajini kontrol et.
    pause
    exit /b 1
)

if not exist build\classes\akachi\launcher mkdir build\classes\akachi\launcher
copy /Y src\main\resources\akachi\launcher\akachi.jpg build\classes\akachi\launcher\akachi.jpg >nul
if errorlevel 1 (
    echo Akachi logo dosyasi kopyalanamadi.
    pause
    exit /b 1
)

java --add-modules jdk.httpserver -cp build\classes akachi.launcher.AkachiLauncher

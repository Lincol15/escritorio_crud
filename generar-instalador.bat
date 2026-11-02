@echo off
setlocal
rem Paso 12: genera el instalador de Windows con jpackage.
rem Requiere JDK 17+ y WiX Toolset 3.11 (solo para el tipo exe).
rem Uso: generar-instalador.bat          -> instalador .exe
rem      generar-instalador.bat portable -> carpeta con GestionPersonas.exe (sin WiX)

set TIPO=exe
if /I "%~1"=="portable" set TIPO=app-image

rem --- Buscar jpackage: JAVA_HOME, luego el PATH, luego C:\Program Files\Java ---
set JPACKAGE=
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jpackage.exe" set "JPACKAGE=%JAVA_HOME%\bin\jpackage.exe"
if not defined JPACKAGE for /f "delims=" %%J in ('where jpackage 2^>nul') do if not defined JPACKAGE set "JPACKAGE=%%J"
if not defined JPACKAGE for /d %%D in ("C:\Program Files\Java\jdk-*") do if exist "%%D\bin\jpackage.exe" set "JPACKAGE=%%D\bin\jpackage.exe"
if not defined JPACKAGE (
    echo No se encontro jpackage. Instala un JDK 17+ o define JAVA_HOME.
    exit /b 1
)
echo Usando %JPACKAGE%

rem --- WiX es obligatorio solo para el instalador .exe ---
if "%TIPO%"=="exe" (
    where candle >nul 2>nul || (
        echo No se encontro WiX Toolset en el PATH.
        echo Instalalo desde https://github.com/wixtoolset/wix3/releases ^(version 3.11^)
        echo o genera la version portable con: generar-instalador.bat portable
        exit /b 1
    )
)

call mvn clean package -DskipTests || exit /b 1

if exist dist rmdir /s /q dist
if exist instalador rmdir /s /q instalador
mkdir dist
copy target\crud-persona-spring-1.0.0.jar dist\ >nul

if "%TIPO%"=="exe" (
    "%JPACKAGE%" --type exe ^
      --name "GestionPersonas" ^
      --app-version 1.0.0 ^
      --input dist ^
      --main-jar crud-persona-spring-1.0.0.jar ^
      --icon src/main/resources/icono.ico ^
      --win-shortcut --win-menu ^
      --dest instalador || exit /b 1
) else (
    "%JPACKAGE%" --type app-image ^
      --name "GestionPersonas" ^
      --app-version 1.0.0 ^
      --input dist ^
      --main-jar crud-persona-spring-1.0.0.jar ^
      --icon src/main/resources/icono.ico ^
      --dest instalador || exit /b 1
)

echo.
echo Listo: revisa la carpeta instalador\

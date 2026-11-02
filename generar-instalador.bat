@echo off
rem Paso 12: genera el instalador de Windows con jpackage.
rem Requiere JDK 17+ y WiX Toolset 3.11 (solo para el tipo exe).
rem Uso: generar-instalador.bat        -> instalador .exe
rem      generar-instalador.bat portable -> carpeta con GestionPersonas.exe (sin WiX)

set TIPO=exe
if /I "%1"=="portable" set TIPO=app-image

call mvn clean package -DskipTests || exit /b 1

if exist dist rmdir /s /q dist
if exist instalador rmdir /s /q instalador
mkdir dist
copy target\crud-persona-spring-1.0.0.jar dist\

if "%TIPO%"=="exe" (
    jpackage --type exe ^
      --name "GestionPersonas" ^
      --app-version 1.0.0 ^
      --input dist ^
      --main-jar crud-persona-spring-1.0.0.jar ^
      --icon src/main/resources/icono.ico ^
      --win-shortcut --win-menu ^
      --dest instalador
) else (
    jpackage --type app-image ^
      --name "GestionPersonas" ^
      --app-version 1.0.0 ^
      --input dist ^
      --main-jar crud-persona-spring-1.0.0.jar ^
      --icon src/main/resources/icono.ico ^
      --dest instalador
)

echo.
echo Resultado en la carpeta instalador\

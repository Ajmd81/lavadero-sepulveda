@echo off
REM Script para generar ejecutable Windows para Lavadero Sepulveda CRM
REM Ejecutar este script en Windows desde el directorio raíz del proyecto

setlocal enabledelayedexpansion

echo ========================================
echo Lavadero Sepulveda CRM - Windows Builder
echo ========================================
echo.

REM Verificar si Maven está instalado
mvn --version >nul 2>&1
if errorlevel 1 (
    echo Error: Maven no está instalado o no está en el PATH
    echo Por favor, instala Maven en: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo [1/3] Compilando proyecto...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo Error en la compilación
    pause
    exit /b 1
)

echo.
echo [2/3] Generando ejecutable .exe...
jpackage --input target --name "Lavadero Sepulveda CRM" ^
  --main-jar lavadero-sepulveda-crm-1.0.0.jar ^
  --main-class com.lavaderosepulveda.crm.LavaderoSepulvedaCRMApplication ^
  --type exe --app-version 1.0.0 --vendor "Lavadero Sepulveda" ^
  --icon src/main/resources/images/logo_crm.png ^
  --java-options "-Xmx2048m" ^
  --win-menu --win-menu-group "Lavadero Sepulveda"

if errorlevel 1 (
    echo Error generando ejecutable
    pause
    exit /b 1
)

echo.
echo [3/3] Proceso completado con éxito!
echo.
echo El archivo ejecutable ha sido creado:
echo   - Lavadero Sepulveda CRM-1.0.0.exe
echo.
echo El instalador ha creado:
echo   - Entrada en el menú de Inicio
echo   - Icono en el escritorio (opcional)
echo.
pause

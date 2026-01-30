@echo off
REM Script para generar instalador MSI (Windows Installer) para Lavadero Sepulveda CRM
REM Esta opción es más profesional que .exe

setlocal enabledelayedexpansion

echo ========================================
echo Lavadero Sepulveda CRM - MSI Builder
echo ========================================
echo.

REM Verificar si Maven está instalado
mvn --version >nul 2>&1
if errorlevel 1 (
    echo Error: Maven no está instalado o no está en el PATH
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
echo [2/3] Generando instalador .msi...
jpackage --input target --name "Lavadero Sepulveda CRM" ^
  --main-jar lavadero-sepulveda-crm-1.0.0.jar ^
  --main-class com.lavaderosepulveda.crm.LavaderoSepulvedaCRMApplication ^
  --type msi --app-version 1.0.0 --vendor "Lavadero Sepulveda" ^
  --icon src/main/resources/images/logo_crm.png ^
  --java-options "-Xmx2048m" ^
  --win-menu --win-menu-group "Lavadero Sepulveda"

if errorlevel 1 (
    echo Error generando instalador
    pause
    exit /b 1
)

echo.
echo [3/3] Proceso completado con éxito!
echo.
echo El archivo instalador ha sido creado:
echo   - Lavadero Sepulveda CRM-1.0.0.msi
echo.
echo Para instalar:
echo   1. Haz doble clic en el archivo .msi
echo   2. Sigue el asistente de instalación
echo   3. La aplicación se instalará en Archivos de Programa
echo.
pause

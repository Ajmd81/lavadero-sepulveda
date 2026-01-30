@echo off
echo =====================================
echo   INICIO RAPIDO - CRM WEB
echo   Lavadero Sepulveda
echo =====================================
echo.

where node >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Node.js no esta instalado
    echo Descargalo desde: https://nodejs.org/
    pause
    exit /b 1
)

echo Node.js detectado
echo.

if not exist "node_modules" (
    echo Instalando dependencias...
    call npm install
    echo.
)

if not exist ".env" (
    echo Creando archivo .env...
    echo VITE_API_URL=http://localhost:8080/api > .env
)

echo Iniciando aplicacion...
echo URL: http://localhost:5173/admin/login
echo Usuario: admin
echo Password: admin123
echo.
echo Presiona Ctrl+C para detener
echo =====================================
echo.

call npm run dev

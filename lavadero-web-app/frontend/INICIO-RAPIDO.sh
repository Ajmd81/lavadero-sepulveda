#!/bin/bash

echo "====================================="
echo "  🚀 INICIO RÁPIDO - CRM WEB"
echo "  Lavadero Sepúlveda"
echo "====================================="
echo ""

# Verificar si Node.js está instalado
if ! command -v node &> /dev/null; then
    echo "❌ Error: Node.js no está instalado"
    echo "   Descárgalo desde: https://nodejs.org/"
    exit 1
fi

echo "✅ Node.js detectado: $(node -v)"
echo ""

# Verificar si las dependencias están instaladas
if [ ! -d "node_modules" ]; then
    echo "📦 Instalando dependencias..."
    npm install
    echo ""
fi

echo "🔧 Configuración de entorno:"
if [ ! -f ".env" ]; then
    echo "   Creando archivo .env..."
    echo "VITE_API_URL=http://localhost:8080/api" > .env
fi
echo "   ✅ Backend URL: $(cat .env | grep VITE_API_URL)"
echo ""

echo "🚀 Iniciando aplicación..."
echo "   📍 URL: http://localhost:5173/admin/login"
echo "   🔐 Usuario: admin (configura en backend)"
echo "   🔑 Contraseña: admin123 (configura en backend)"
echo ""
echo "Presiona Ctrl+C para detener"
echo "====================================="
echo ""

npm run dev

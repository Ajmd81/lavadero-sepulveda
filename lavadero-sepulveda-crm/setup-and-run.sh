#!/bin/bash
# Script para instalar Java con JavaFX y ejecutar Lavadero Sepulveda CRM

echo "=================================================="
echo "Setup Lavadero Sepulveda CRM"
echo "=================================================="
echo ""

# Verificar si brew está instalado
if ! command -v brew &> /dev/null; then
    echo "❌ Homebrew no está instalado"
    echo "Instálalo desde: https://brew.sh"
    exit 1
fi

echo "Instalando Temurin JDK 21 (con soporte completo)..."
brew install temurin@21

echo ""
echo "Configurando Java..."
export JAVA_HOME=$(/usr/libexec/java_home -v21)
echo "Java en: $JAVA_HOME"

echo ""
echo "Ejecutando aplicación..."
cd "$(dirname "$0")"

$JAVA_HOME/bin/java -XstartOnFirstThread -Xmx2048m -jar target/lavadero-sepulveda-crm-1.0.0.jar

#!/bin/bash
#
# Script para descargar e instalar Lavadero Sepulveda CRM en macOS
#
# Este script descarga JavaFX SDK y configura todo lo necesario para ejecutar la aplicación

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAVAFX_VERSION="21.0.1"
JAVAFX_DIR="$SCRIPT_DIR/javafx-sdk"
JAR_PATH="$SCRIPT_DIR/target/lavadero-sepulveda-crm-1.0.0.jar"

echo "=================================================="
echo "Instalador de Lavadero Sepulveda CRM - macOS"
echo "=================================================="
echo ""

# Detectar arquitectura
ARCH=$(uname -m)
if [ "$ARCH" = "arm64" ]; then
    JAVAFX_ARCH="aarch64"
    echo "✅ Sistema detectado: Apple Silicon (arm64)"
else
    JAVAFX_ARCH="x64"
    echo "✅ Sistema detectado: Intel Mac (x64)"
fi

# Verificar si JavaFX ya está descargado
if [ ! -d "$JAVAFX_DIR" ]; then
    echo ""
    echo "Descargando JavaFX SDK v$JAVAFX_VERSION..."
    cd "$SCRIPT_DIR"
    
    JAVAFX_URL="https://gluonhq.com/download/javafx-sdk-$JAVAFX_VERSION-macos-$JAVAFX_ARCH.zip"
    JAVAFX_ZIP="/tmp/javafx-sdk-$JAVAFX_VERSION.zip"
    
    if curl -L "$JAVAFX_URL" -o "$JAVAFX_ZIP" 2>/dev/null; then
        unzip -q "$JAVAFX_ZIP"
        rm "$JAVAFX_ZIP"
        echo "✅ JavaFX SDK descargado e instalado"
    else
        echo "⚠️  No se pudo descargar JavaFX. La aplicación podría no funcionar."
        echo "   Intenta descargar manualmente desde: https://gluonhq.com/products/javafx/"
    fi
else
    echo "✅ JavaFX SDK ya está disponible"
fi

# Ejecutar la aplicación
echo ""
echo "Iniciando Lavadero Sepulveda CRM..."
echo ""

if [ -d "$JAVAFX_DIR" ]; then
    # Ejecutar con JavaFX incluido
    java --module-path "$JAVAFX_DIR/lib" \
         --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.web,javafx.media \
         -XstartOnFirstThread \
         -Xmx2048m \
         -jar "$JAR_PATH"
else
    # Intentar ejecutar sin JavaFX (podría no funcionar)
    java -XstartOnFirstThread -Xmx2048m -jar "$JAR_PATH"
fi

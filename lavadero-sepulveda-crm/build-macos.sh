#!/bin/bash
# Script para generar DMG para macOS
# Uso: ./build-macos.sh

set -e

echo "========================================"
echo "Lavadero Sepulveda CRM - macOS Builder"
echo "========================================"
echo ""

# Verificar si Maven está instalado
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven no está instalado"
    exit 1
fi

# Verificar si jpackage está disponible
if ! command -v jpackage &> /dev/null; then
    echo "Error: jpackage no está disponible"
    echo "Asegúrate de tener Java 17+ instalado"
    exit 1
fi

echo "[1/4] Compilando proyecto..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "Error en la compilación"
    exit 1
fi

echo ""
echo "[2/4] Convirtiendo icono a formato ICNS..."
ICON_DIR="src/main/resources/images"
if [ -f "$ICON_DIR/logo_crm.png" ]; then
    sips -s format icns "$ICON_DIR/logo_crm.png" --out "$ICON_DIR/logo_crm.icns" 2>/dev/null || true
    echo "Icono convertido: $ICON_DIR/logo_crm.icns"
else
    echo "Advertencia: No se encontró logo_crm.png, se usará el icono por defecto"
fi

echo ""
echo "[3/4] Eliminando DMG anterior..."
rm -f Lavadero*.dmg

echo ""
echo "[4/4] Generando DMG..."
if [ -f "$ICON_DIR/logo_crm.icns" ]; then
    jpackage --input target \
        --name "Lavadero Sepulveda CRM" \
        --main-jar lavadero-sepulveda-crm-1.0.0.jar \
        --main-class com.lavaderosepulveda.crm.LavaderoSepulvedaCRMApplication \
        --type dmg \
        --app-version 1.0.0 \
        --vendor "Lavadero Sepulveda" \
        --icon "$ICON_DIR/logo_crm.icns" \
        --java-options "-Xmx2048m"
else
    jpackage --input target \
        --name "Lavadero Sepulveda CRM" \
        --main-jar lavadero-sepulveda-crm-1.0.0.jar \
        --main-class com.lavaderosepulveda.crm.LavaderoSepulvedaCRMApplication \
        --type dmg \
        --app-version 1.0.0 \
        --vendor "Lavadero Sepulveda" \
        --java-options "-Xmx2048m"
fi

if [ $? -eq 0 ]; then
    echo ""
    echo "==========================================="
    echo "✅ Proceso completado con éxito!"
    echo "==========================================="
    echo ""
    echo "El archivo instalador DMG ha sido creado:"
    ls -lh Lavadero*.dmg
    echo ""
    echo "Para instalar:"
    echo "  1. Haz doble clic en el archivo .dmg"
    echo "  2. Arrastra la aplicación a la carpeta 'Applications'"
    echo "  3. Ejecuta desde Launchpad o Applications"
else
    echo "Error generando DMG"
    exit 1
fi

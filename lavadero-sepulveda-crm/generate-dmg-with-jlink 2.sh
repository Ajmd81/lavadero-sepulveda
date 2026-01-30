#!/bin/bash
# Script para generar un DMG funcional con jlink y jpackage

set -e

cd "$(dirname "$0")"

echo "=================================================="
echo "Generando DMG funcional con Java Runtime"
echo "=================================================="
echo ""

# Paso 1: Compilar
echo "[1/4] Compilando..."
mvn clean package -DskipTests -q

# Paso 2: Crear runtime personalizado con jlink
echo "[2/4] Creando runtime Java con JavaFX..."
JAVA_HOME=$(/usr/libexec/java_home -v21)
RUNTIME_DIR="$PWD/runtime"

if [ ! -d "$RUNTIME_DIR" ]; then
    $JAVA_HOME/bin/jlink \
        --module-path $JAVA_HOME/jmods \
        --add-modules java.base,java.logging,java.desktop,javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.web,javafx.media \
        --strip-debug \
        --compress 2 \
        --output "$RUNTIME_DIR"
    
    echo "✅ Runtime creado"
else
    echo "✅ Runtime ya existe"
fi

# Paso 3: Generar DMG con jpackage usando el runtime
echo "[3/4] Generando DMG..."
rm -f *.dmg

$JAVA_HOME/bin/jpackage \
    --input target \
    --name "Lavadero Sepulveda CRM" \
    --main-jar lavadero-sepulveda-crm-1.0.0.jar \
    --main-class com.lavaderosepulveda.crm.LavaderoSepulvedaCRMApplication \
    --type dmg \
    --app-version 1.0.0 \
    --vendor "Lavadero Sepulveda" \
    --icon src/main/resources/images/logo_crm.icns \
    --java-options "-Xmx2048m" \
    --runtime-image "$RUNTIME_DIR"

echo ""
echo "=================================================="
echo "✅ DMG generado correctamente"
echo "=================================================="
ls -lh *.dmg

#!/bin/bash

# ================================================================
# INSTALADOR DE LAVADERO SEPULVEDA CRM - VERSIÓN SIMPLIFICADA
# ================================================================

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# Soporta tanto JavaFX 21 como 25
JAVAFX_SDK_PATH_25="$HOME/javafx-sdk-25"
JAVAFX_SDK_PATH_21="$HOME/javafx-sdk-21.0.1"
JAR_FILE="$SCRIPT_DIR/target/lavadero-sepulveda-crm-1.0.0.jar"

# Detectar qué versión de JavaFX está disponible
if [ -d "$JAVAFX_SDK_PATH_25" ]; then
    JAVAFX_SDK_PATH="$JAVAFX_SDK_PATH_25"
elif [ -d "$JAVAFX_SDK_PATH_21" ]; then
    JAVAFX_SDK_PATH="$JAVAFX_SDK_PATH_21"
else
    JAVAFX_SDK_PATH="$JAVAFX_SDK_PATH_25"  # Default para buscar
fi

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}  LAVADERO SEPULVEDA CRM - Instalador macOS${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

# PASO 1: Compilar
echo -e "${BLUE}==>${NC} Compilando proyecto..."
cd "$SCRIPT_DIR"
mvn clean package -DskipTests -q

if [ ! -f "$JAR_FILE" ]; then
    echo -e "${RED}✗${NC} Error: No se pudo compilar el JAR"
    exit 1
fi
echo -e "${GREEN}✓${NC} Compilación completada"
echo ""

# PASO 2: Buscar JavaFX SDK
if [ -d "$JAVAFX_SDK_PATH" ]; then
    echo -e "${GREEN}✓${NC} JavaFX SDK encontrado en: $JAVAFX_SDK_PATH"
    JAVAFX_PATH="$JAVAFX_SDK_PATH"
else
    echo -e "${YELLOW}!${NC} JavaFX SDK no encontrado"
    echo ""
    echo -e "${BLUE}==>${NC} Opciones para obtener JavaFX SDK:"
    echo ""
    echo "  OPCIÓN 1 - Descargar desde navegador (Recomendado):"
    echo "  1. Ve a: https://gluonhq.com/products/javafx/"
    echo "  2. Descarga: JavaFX SDK 25 o 21.0.1 (macOS)"
    echo "  3. Extrae el ZIP en: $HOME"
    echo ""
    echo "     Para Java 25: ~/javafx-sdk-25"
    echo "     Para Java 21: ~/javafx-sdk-21.0.1"
    echo ""
    echo "  OPCIÓN 2 - Usar jdk.java.net"
    echo "  https://jdk.java.net/javafx25"
    echo "  https://jdk.java.net/javafx21"
    echo ""
    echo "  Después de descargar/extraer, ejecuta este script de nuevo."
    echo ""
    exit 1
fi

# PASO 3: Ejecutar
echo -e "${BLUE}==>${NC} Iniciando aplicación..."
echo ""

java \
    --module-path "$JAVAFX_PATH/lib" \
    --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web \
    -XstartOnFirstThread \
    -Xmx2048m \
    -jar "$JAR_FILE" \
    "$@"

EXIT_CODE=$?
echo ""

if [ $EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Aplicación ejecutada correctamente"
else
    echo -e "${RED}✗${NC} Código de salida: $EXIT_CODE"
fi

exit $EXIT_CODE

#!/bin/bash
#  Ejecutor de Lavadero Sepulveda CRM para macOS
# 
# Este script ejecuta la aplicación con la configuración correcta

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAR="$SCRIPT_DIR/target/lavadero-sepulveda-crm-1.0.0.jar"

# Encontrar Java 21
JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || echo "/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home")

if [ ! -x "$JAVA_HOME/bin/java" ]; then
    echo "❌ Error: No se encontró Java 21"
    echo ""
    echo "Para instalar Java 21 con soporte JavaFX:"
    echo "  brew install temurin@21"
    exit 1
fi

if [ ! -f "$JAR" ]; then
    echo "❌ Error: JAR no encontrado en $JAR"
    echo ""
    echo "Asegúrate de compilar primero con:"
    echo "  mvn clean package -DskipTests"
    exit 1
fi

echo "Iniciando Lavadero Sepulveda CRM..."
echo "Java: $JAVA_HOME"
echo ""

# Ejecutar con opciones específicas para macOS
"$JAVA_HOME/bin/java" \
    -XstartOnFirstThread \
    -Xmx2048m \
    -jar "$JAR" \
    "$@"

#!/bin/bash
# Script para ejecutar Lavadero Sepulveda CRM en macOS

cd "$(dirname "$0")"

# Obtener la ruta del JAR
JAR_PATH="target/lavadero-sepulveda-crm-1.0.0.jar"

# Opciones JVM
JVM_OPTS="-Xmx2048m -XstartOnFirstThread"

# Ejecutar
java $JVM_OPTS -jar "$JAR_PATH"

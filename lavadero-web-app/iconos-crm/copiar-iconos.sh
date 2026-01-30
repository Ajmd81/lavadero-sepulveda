#!/bin/bash

# Script para copiar iconos del CRM al proyecto web
# Uso: ./copiar-iconos.sh [ruta-origen]

echo "🎨 Instalador de Iconos CRM - Lavadero Sepúlveda"
echo "================================================="
echo ""

# Ruta del proyecto (ajusta si es necesario)
PROYECTO="/Users/antoniojesus/Desktop/LavaderoSepulvedaWebCompleto/lavadero-web-app/frontend"
DESTINO="$PROYECTO/public/assets/icons"

# Ruta de origen (por defecto ~/Downloads)
ORIGEN="${1:-$HOME/Downloads}"

# Verificar que existe el proyecto
if [ ! -d "$PROYECTO" ]; then
    echo "❌ Error: No se encuentra el proyecto en $PROYECTO"
    echo "   Ajusta la variable PROYECTO en este script"
    exit 1
fi

# Crear carpeta de destino si no existe
echo "📁 Creando carpeta de iconos..."
mkdir -p "$DESTINO"

if [ ! -d "$DESTINO" ]; then
    echo "❌ Error: No se pudo crear la carpeta $DESTINO"
    exit 1
fi

echo "✅ Carpeta creada: $DESTINO"
echo ""

# Lista de iconos a copiar
iconos=(
    "analisis.png"
    "carro-de-la-compra.png"
    "citas.png"
    "cliente.png"
    "contabilidad.png"
    "estado-financiero.png"
    "facturacion.png"
    "facturaEmitida.png"
    "invoice.png"
    "logo_crm.png"
    "logo_crm_2.png"
    "modeloFiscal.png"
    "panel.png"
    "proveedor.png"
)

# Contador
copiados=0
faltantes=0

echo "📋 Copiando iconos desde: $ORIGEN"
echo ""

# Copiar cada icono
for icono in "${iconos[@]}"; do
    if [ -f "$ORIGEN/$icono" ]; then
        cp "$ORIGEN/$icono" "$DESTINO/"
        echo "✅ Copiado: $icono"
        ((copiados++))
    else
        echo "⚠️  No encontrado: $icono"
        ((faltantes++))
    fi
done

echo ""
echo "================================================="
echo "📊 Resumen:"
echo "   ✅ Copiados: $copiados iconos"
echo "   ⚠️  Faltantes: $faltantes iconos"
echo ""

if [ $copiados -gt 0 ]; then
    echo "🎉 ¡Iconos instalados correctamente!"
    echo ""
    echo "📝 Próximos pasos:"
    echo "   1. Copia CustomIcon.jsx a src/components/"
    echo "   2. Actualiza AdminLayout.jsx con la nueva versión"
    echo "   3. Ejecuta: npm run dev"
    echo ""
else
    echo "❌ No se copió ningún icono"
    echo ""
    echo "💡 Consejos:"
    echo "   - Verifica que los iconos estén en: $ORIGEN"
    echo "   - O especifica otra ruta: ./copiar-iconos.sh /ruta/a/iconos"
    echo ""
fi

echo "================================================="

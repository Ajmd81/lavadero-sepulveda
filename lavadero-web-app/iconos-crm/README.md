# 🎨 Pack de Iconos Personalizados - CRM Lavadero Sepúlveda

Este paquete te permite integrar los iconos de tu CRM de escritorio en la aplicación web.

---

## 📦 Contenido del Paquete

```
iconos-crm/
├── README.md                      # Este archivo
├── INSTRUCCIONES-ICONOS.md        # Guía detallada paso a paso
├── CustomIcon.jsx                 # Componente de iconos personalizados
├── AdminLayout.jsx                # Sidebar actualizado con iconos
├── Dashboard-ejemplo.jsx          # Ejemplo de uso en Dashboard
└── copiar-iconos.sh              # Script para copiar imágenes automáticamente
```

---

## 🚀 Instalación Rápida (3 pasos)

### 1️⃣ Copiar las Imágenes

**Opción A - Automática (Recomendado):**
```bash
# Navega a donde descargaste este paquete
cd /ruta/a/iconos-crm

# Ejecuta el script (asumiendo que las imágenes están en ~/Downloads)
./copiar-iconos.sh

# O especifica otra ubicación
./copiar-iconos.sh /ruta/a/tus/imagenes
```

**Opción B - Manual:**
Copia las 14 imágenes PNG a:
```
frontend/public/assets/icons/
```

### 2️⃣ Copiar los Componentes

```bash
# En tu proyecto frontend
cd /Users/antoniojesus/Desktop/LavaderoSepulvedaWebCompleto/lavadero-web-app/frontend

# Copiar CustomIcon
cp /ruta/a/iconos-crm/CustomIcon.jsx src/components/

# Hacer backup del AdminLayout actual
cp src/components/layout/AdminLayout.jsx src/components/layout/AdminLayout.jsx.backup

# Copiar nuevo AdminLayout
cp /ruta/a/iconos-crm/AdminLayout.jsx src/components/layout/
```

### 3️⃣ Probar

```bash
npm run dev
```

Abre `http://localhost:5173/admin` y verifica que los iconos se muestran correctamente.

---

## 🎯 Iconos Disponibles

| Código | Imagen | Descripción |
|--------|--------|-------------|
| `dashboard` | panel.png | Dashboard principal |
| `analisis` | analisis.png | Análisis y gráficos |
| `estadoFinanciero` | estado-financiero.png | Estado financiero |
| `cliente` | cliente.png | Gestión de clientes |
| `citas` | citas.png | Citas y agenda |
| `facturacion` | facturacion.png | Facturación |
| `facturaEmitida` | facturaEmitida.png | Facturas emitidas |
| `invoice` | invoice.png | Facturas |
| `contabilidad` | contabilidad.png | Contabilidad |
| `modeloFiscal` | modeloFiscal.png | Modelos fiscales/Gastos |
| `proveedor` | proveedor.png | Proveedores |
| `carrito` | carro-de-la-compra.png | Carrito de compra |
| `logo` | logo_crm.png | Logo del CRM |

---

## 💡 Uso Básico

### En cualquier componente:

```jsx
import CustomIcon from '../components/CustomIcon';

function MiComponente() {
  return (
    <div>
      {/* Básico */}
      <CustomIcon name="cliente" size={24} />
      
      {/* Con clase CSS */}
      <CustomIcon name="facturacion" size={32} className="mr-2" />
      
      {/* En un botón */}
      <button className="flex items-center gap-2">
        <CustomIcon name="citas" size={20} />
        <span>Nueva Cita</span>
      </button>
    </div>
  );
}
```

---

## 📚 Documentación Completa

Para instrucciones detalladas, consulta:
- **INSTRUCCIONES-ICONOS.md** - Guía paso a paso completa
- **Dashboard-ejemplo.jsx** - Ejemplo de implementación

---

## ✅ Verificación Post-Instalación

Después de instalar, verifica:

- ✅ Las imágenes están en `public/assets/icons/`
- ✅ CustomIcon.jsx está en `src/components/`
- ✅ AdminLayout.jsx está actualizado
- ✅ Los iconos se muestran en el sidebar
- ✅ El logo aparece en la parte superior del sidebar

---

## 🐛 Solución de Problemas

### Los iconos no se ven

1. Verifica la ruta: `public/assets/icons/`
2. Los nombres deben coincidir exactamente (case-sensitive)
3. Abre la consola del navegador (F12) y busca errores 404

### Iconos muy grandes/pequeños

Ajusta el prop `size`:
```jsx
<CustomIcon name="dashboard" size={16} />  // Pequeño
<CustomIcon name="dashboard" size={48} />  // Grande
```

### El sidebar se ve mal

Asegúrate de que:
- CustomIcon está importado correctamente en AdminLayout
- Todas las rutas de iconos en el mapeo son correctas

---

## 🎨 Personalización

### Agregar nuevos iconos:

1. Añade la imagen PNG a `public/assets/icons/`
2. Actualiza el mapeo en `CustomIcon.jsx`:

```jsx
const iconMap = {
  // ... iconos existentes
  miNuevoIcono: '/assets/icons/mi-nuevo-icono.png',
};
```

3. Usa el nuevo icono:
```jsx
<CustomIcon name="miNuevoIcono" size={24} />
```

---

## 📞 Soporte

Si encuentras problemas:
1. Revisa INSTRUCCIONES-ICONOS.md
2. Verifica la consola del navegador (F12)
3. Comprueba que todos los archivos están en las rutas correctas

---

## 📝 Notas

- Las imágenes deben estar en formato PNG
- Tamaño recomendado: 512x512px o superior
- Fondo transparente recomendado
- Optimiza las imágenes para web (usa TinyPNG si son muy pesadas)

---

## 🎉 ¡Listo!

Tu aplicación web ahora tiene el mismo aspecto visual que tu CRM de escritorio.

**Desarrollado por:** Antonio Jesús Martínez Díaz  
**Proyecto:** CRM Web - Lavadero Sepúlveda  
**Fecha:** Enero 2025  
**Versión:** 1.0.0

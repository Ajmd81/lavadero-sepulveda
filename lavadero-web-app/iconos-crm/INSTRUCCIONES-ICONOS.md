# 🎨 Guía de Instalación de Iconos Personalizados del CRM

Esta guía te ayudará a integrar los iconos de tu CRM de escritorio en la aplicación web.

## 📁 Paso 1: Organizar las Imágenes

### 1.1 Crear estructura de carpetas

En tu proyecto `lavadero-web-app/frontend/`, crea la siguiente estructura:

```
frontend/
├── public/
│   └── assets/
│       └── icons/
│           ├── analisis.png
│           ├── carro-de-la-compra.png
│           ├── citas.png
│           ├── cliente.png
│           ├── contabilidad.png
│           ├── estado-financiero.png
│           ├── facturacion.png
│           ├── facturaEmitida.png
│           ├── invoice.png
│           ├── logo_crm.png
│           ├── modeloFiscal.png
│           ├── panel.png
│           └── proveedor.png
└── src/
    └── components/
        └── CustomIcon.jsx  (archivo que te proporcioné)
```

### 1.2 Copiar las imágenes

Desde tu terminal (macOS):

```bash
# Navega a tu proyecto
cd /Users/antoniojesus/Desktop/LavaderoSepulvedaWebCompleto/lavadero-web-app/frontend

# Crea la carpeta de iconos en public
mkdir -p public/assets/icons

# Copia todas las imágenes de iconos a la carpeta
# (ajusta la ruta de origen según dónde hayas guardado las imágenes)
cp ~/Downloads/analisis.png public/assets/icons/
cp ~/Downloads/carro-de-la-compra.png public/assets/icons/
cp ~/Downloads/citas.png public/assets/icons/
cp ~/Downloads/cliente.png public/assets/icons/
cp ~/Downloads/contabilidad.png public/assets/icons/
cp ~/Downloads/estado-financiero.png public/assets/icons/
cp ~/Downloads/facturacion.png public/assets/icons/
cp ~/Downloads/facturaEmitida.png public/assets/icons/
cp ~/Downloads/invoice.png public/assets/icons/
cp ~/Downloads/logo_crm.png public/assets/icons/
cp ~/Downloads/modeloFiscal.png public/assets/icons/
cp ~/Downloads/panel.png public/assets/icons/
cp ~/Downloads/proveedor.png public/assets/icons/
```

**O simplemente arrastra y suelta** las imágenes desde Finder a la carpeta `public/assets/icons/`

---

## 📄 Paso 2: Instalar los Archivos Proporcionados

### 2.1 CustomIcon.jsx

Copia el archivo **CustomIcon.jsx** a:
```
src/components/CustomIcon.jsx
```

### 2.2 AdminLayout.jsx (actualizado)

**IMPORTANTE:** Haz un backup de tu AdminLayout actual primero:

```bash
cd src/components/layout/
cp AdminLayout.jsx AdminLayout.jsx.backup
```

Luego, reemplaza `src/components/layout/AdminLayout.jsx` con el nuevo archivo que te proporcioné.

---

## 🎨 Paso 3: Mapeo de Iconos

Los iconos están mapeados de la siguiente manera:

| Nombre en código | Imagen | Uso |
|-----------------|---------|-----|
| `dashboard` | panel.png | Dashboard principal |
| `analisis` | analisis.png | Análisis/Gráficos |
| `estadoFinanciero` | estado-financiero.png | Resumen financiero |
| `cliente` | cliente.png | Gestión de clientes |
| `citas` | citas.png | Citas y calendario |
| `facturacion` | facturacion.png | Módulo de facturación |
| `facturaEmitida` | facturaEmitida.png | Facturas emitidas |
| `invoice` | invoice.png | Facturas genéricas |
| `contabilidad` | contabilidad.png | Contabilidad |
| `modeloFiscal` | modeloFiscal.png | Gastos/Modelos fiscales |
| `proveedor` | proveedor.png | Proveedores |
| `carrito` | carro-de-la-compra.png | Carrito (uso futuro) |
| `logo` | logo_crm.png | Logo del CRM |

---

## 🚀 Paso 4: Usar los Iconos en Otros Componentes

### Importar el componente:

```jsx
import CustomIcon from '../components/CustomIcon';
```

### Usar el icono:

```jsx
// Básico
<CustomIcon name="cliente" size={24} />

// Con clase CSS
<CustomIcon name="facturacion" size={32} className="mr-2" />

// En un botón
<button className="flex items-center gap-2">
  <CustomIcon name="citas" size={20} />
  <span>Nueva Cita</span>
</button>
```

---

## 🎯 Paso 5: Actualizar Otros Componentes

Si quieres usar los iconos personalizados en otros lugares:

### Dashboard.jsx - Usar iconos en KPIs:

```jsx
<div className="bg-white rounded-lg shadow p-6">
  <div className="flex items-center gap-3">
    <CustomIcon name="cliente" size={40} />
    <div>
      <p className="text-gray-600">Total Clientes</p>
      <p className="text-3xl font-bold">{totalClientes}</p>
    </div>
  </div>
</div>
```

### Login.jsx - Agregar logo:

```jsx
<div className="text-center mb-8">
  <CustomIcon name="logo" size={120} className="mx-auto mb-4" />
  <h1 className="text-3xl font-bold">Lavadero Sepúlveda</h1>
</div>
```

---

## ✅ Verificación

Para verificar que todo funciona:

1. **Inicia el servidor de desarrollo:**
   ```bash
   npm run dev
   ```

2. **Abre el navegador** en `http://localhost:5173/admin`

3. **Deberías ver:**
   - Logo del lavadero en el sidebar
   - Iconos personalizados en el menú de navegación
   - Diseño visual del CRM de escritorio

---

## 🐛 Solución de Problemas

### Problema: Los iconos no se muestran

**Solución:**
1. Verifica que las imágenes están en `public/assets/icons/`
2. Verifica que los nombres de archivo coinciden exactamente (case-sensitive)
3. Abre la consola del navegador (F12) y busca errores 404

### Problema: Iconos demasiado grandes/pequeños

**Solución:**
Ajusta el prop `size`:
```jsx
<CustomIcon name="dashboard" size={20} />  // Más pequeño
<CustomIcon name="dashboard" size={48} />  // Más grande
```

### Problema: Iconos con fondo blanco

**Solución:**
Si tus iconos PNG tienen fondo, puedes editarlos en:
- Photoshop: Eliminar fondo
- GIMP: Agregar canal alpha y eliminar fondo
- Online: https://remove.bg

---

## 🎨 Personalización Adicional

### Agregar efectos hover:

```jsx
<CustomIcon 
  name="cliente" 
  size={24} 
  className="hover:scale-110 transition-transform cursor-pointer" 
/>
```

### Filtros CSS:

```jsx
<CustomIcon 
  name="dashboard" 
  size={24} 
  className="grayscale hover:grayscale-0 transition-all" 
/>
```

---

## 📦 Archivos Incluidos

✅ **CustomIcon.jsx** - Componente de iconos personalizados  
✅ **AdminLayout.jsx** - Sidebar actualizado con iconos del CRM  
✅ **INSTRUCCIONES-ICONOS.md** - Este archivo  

---

## 🎉 ¡Listo!

Ahora tu aplicación web tiene el mismo aspecto visual que tu CRM de escritorio.

**¿Dudas o problemas?** Consulta la documentación o contacta con soporte.

---

**Desarrollado por:** Antonio Jesús Martínez Díaz  
**Fecha:** Enero 2025  
**Versión:** 1.0.0

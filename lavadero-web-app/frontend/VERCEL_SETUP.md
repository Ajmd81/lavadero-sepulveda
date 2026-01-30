# 🚀 Configuración de Vercel para Lavadero Sepúlveda

## Variables de Entorno Requeridas

Para que la aplicación funcione correctamente en Vercel, necesitas configurar las siguientes variables de entorno:

### En Vercel Dashboard:

1. Ve a tu proyecto en [vercel.com](https://vercel.com)
2. Click en **Settings** → **Environment Variables**
3. Agrega esta variable:

| Variable | Valor | Notas |
|----------|-------|-------|
| `VITE_API_URL` | `https://lavadero-sepulveda-production.up.railway.app/api` | URL del backend en Railway |

### Por CLI (Vercel CLI):

```bash
# Instalar Vercel CLI si no lo tienes
npm i -g vercel

# Desde el directorio frontend
cd lavadero-web-app/frontend

# Agregar variable de entorno
vercel env add VITE_API_URL https://lavadero-sepulveda-production.up.railway.app/api

# Redeploy
vercel redeploy
```

## Estructura de la Aplicación

```
frontend/
├── src/
│   ├── pages/        # Páginas de la aplicación
│   ├── components/   # Componentes reutilizables
│   ├── services/     # Servicios de API
│   └── context/      # Context API
├── .env              # Variables locales (no subido a git)
├── .env.example      # Plantilla de variables
├── vercel.json       # Configuración de Vercel
└── package.json      # Dependencias
```

## Desarrollo Local

```bash
# Instalar dependencias
npm install

# Desarrollo con Vite
npm run dev

# Build para producción
npm run build

# Preview de producción
npm run preview
```

## Variables de Desarrollo Local

Para desarrollo local, crea un archivo `.env` con:

```env
VITE_API_URL=http://localhost:8080/api
```

## Endpoints de la API

La aplicación se conecta a:
- **Producción**: `https://lavadero-sepulveda-production.up.railway.app/api`
- **Desarrollo**: `http://localhost:8080/api`

Todos los endpoints están configurados en `src/services/`:
- `citaService` - Gestión de citas
- `clienteService` - Gestión de clientes
- `facturaService` - Gestión de facturas
- `gastoService` - Gestión de gastos
- `proveedorService` - Gestión de proveedores
- `facturaRecibidaService` - Facturas de proveedores

## Resolución de Problemas

### Error: "ERR_NAME_NOT_RESOLVED"

**Causa**: El dominio no está configurado correctamente.

**Solución**: Usa la URL de Railway directa:
- `https://lavadero-sepulveda-production.up.railway.app/api`

### Error: "Failed to load resource: CORS policy"

**Causa**: La variable `VITE_API_URL` no está configurada correctamente en Vercel.

**Solución**:
1. Verifica que en Vercel Settings → Environment Variables esté:
   - `VITE_API_URL = https://lavadero-sepulveda-production.up.railway.app/api`
2. Haz un redeploy: `vercel redeploy`

### Error: "Cannot GET /"

**Causa**: Vercel no está sirviendo la SPA correctamente.

**Solución**: El archivo `vercel.json` ya tiene la configuración correcta de rewrites. Verifica que esté presente.

## Deployment

Para desplegar cambios:

```bash
# En la rama main
git push origin main

# Vercel detecta automáticamente cambios en GitHub
# Si es necesario redeploy manual:
vercel redeploy
```

## Monitoreo de la API

Para verificar que el backend está en línea:
```
https://lavadero-sepulveda-production.up.railway.app/actuator/health
```

Debería retornar:
```json
{"status":"UP"}
```

## Soporte

Para problemas específicos:
1. Revisa los logs de Vercel: Project → Deployments → Última deploymnet → Logs
2. Abre la consola del navegador (F12) para ver errores de cliente
3. Verifica que el backend está en línea: `https://lavadero-sepulveda-production.up.railway.app/actuator/health`


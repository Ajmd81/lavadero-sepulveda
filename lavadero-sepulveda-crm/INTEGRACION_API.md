# Guía de Integración con CitasApi

## 🔌 Conexión del CRM con tu API

El CRM ahora está preparado para conectarse a tu API CitasApi del Lavadero Sepúlveda. A continuación te explico cómo configurarlo y qué endpoints necesitas en tu API.

## ⚙️ Configuración

### 1. Configurar URL de la API

Edita el archivo `src/main/resources/api-config.properties`:

```properties
# URL base de la API
# Para desarrollo local:
api.base.url=http://localhost:8080

# Para producción en Railway:
# api.base.url=https://tu-app.railway.app

# Endpoints (ajustar según tu API)
api.endpoints.clientes=/api/clientes
api.endpoints.citas=/api/citas
api.endpoints.servicios=/api/servicios
api.endpoints.facturas=/api/facturas
```

### 2. Configurar Autenticación (Opcional)

Si tu API requiere autenticación JWT:

```properties
api.auth.enabled=true
api.auth.token=tu-jwt-token-aqui
```

## 📡 Endpoints Requeridos en tu API

El CRM espera que tu API tenga los siguientes endpoints:

### **Clientes** (`/api/clientes`)

```
GET    /api/clientes                    - Listar todos los clientes
GET    /api/clientes/{id}                - Obtener cliente por ID
GET    /api/clientes/activos             - Clientes activos
GET    /api/clientes/telefono/{telefono} - Buscar por teléfono
GET    /api/clientes/buscar?nombre=X     - Buscar por nombre
GET    /api/clientes/top-facturacion?limit=X  - Top clientes
GET    /api/clientes/no-presentaciones?limit=X - Con más no presentaciones
GET    /api/clientes/count               - Contar clientes
POST   /api/clientes                     - Crear cliente
PUT    /api/clientes/{id}                - Actualizar cliente
DELETE /api/clientes/{id}                - Eliminar cliente
```

**Ejemplo de JSON Cliente:**
```json
{
  "id": 1,
  "nombre": "Juan",
  "apellidos": "García López",
  "telefono": "666111222",
  "email": "juan@email.com",
  "nif": "12345678A",
  "direccion": "Calle X, 123",
  "codigoPostal": "28001",
  "ciudad": "Madrid",
  "provincia": "Madrid",
  "matricula": "1234ABC",
  "marca": "BMW",
  "modelo": "Serie 3",
  "color": "Negro",
  "totalCitas": 10,
  "citasCompletadas": 8,
  "citasCanceladas": 1,
  "citasNoPresentadas": 1,
  "totalFacturado": 450.50,
  "activo": true
}
```

### **Citas** (`/api/citas`)

```
GET    /api/citas                        - Listar todas las citas
GET    /api/citas/{id}                   - Obtener cita por ID
GET    /api/citas/fecha/{fecha}          - Citas por fecha (YYYY-MM-DD)
GET    /api/citas/rango?inicio=X&fin=Y   - Citas por rango de fechas
GET    /api/citas/cliente/{clienteId}    - Citas de un cliente
GET    /api/citas/estado/{estado}        - Citas por estado
GET    /api/citas/pendientes             - Citas pendientes
GET    /api/citas/no-facturadas          - Citas completadas sin facturar
GET    /api/citas/count/hoy              - Contar citas de hoy
GET    /api/citas/count/estado/{estado}  - Contar por estado
POST   /api/citas                        - Crear cita
PUT    /api/citas/{id}                   - Actualizar cita
PUT    /api/citas/{id}/estado/{estado}   - Cambiar estado
POST   /api/citas/{id}/cancelar          - Cancelar cita
DELETE /api/citas/{id}                   - Eliminar cita
```

**Estados posibles:**
- PENDIENTE
- CONFIRMADA
- EN_PROCESO
- COMPLETADA
- CANCELADA
- NO_PRESENTADO

**Ejemplo de JSON Cita:**
```json
{
  "id": 1,
  "clienteId": 1,
  "fechaHora": "2024-12-25T10:00:00",
  "duracionEstimada": 60,
  "estado": "PENDIENTE",
  "serviciosIds": [1, 2],
  "observaciones": "Cliente prefiere interior completo",
  "matricula": "1234ABC",
  "marcaModelo": "BMW Serie 3",
  "recordatorioEnviado": false,
  "confirmacionEnviada": false,
  "facturada": false
}
```

### **Servicios** (`/api/servicios`)

```
GET    /api/servicios                    - Listar todos los servicios
GET    /api/servicios/{id}               - Obtener servicio por ID
GET    /api/servicios/activos            - Servicios activos
GET    /api/servicios/categoria/{cat}    - Servicios por categoría
GET    /api/servicios/buscar?nombre=X    - Buscar por nombre
GET    /api/servicios/categorias         - Listar categorías
POST   /api/servicios                    - Crear servicio
PUT    /api/servicios/{id}               - Actualizar servicio
DELETE /api/servicios/{id}               - Eliminar servicio
```

**Ejemplo de JSON Servicio:**
```json
{
  "id": 1,
  "nombre": "Lavado Exterior Completo",
  "descripcion": "Lavado exterior completo con secado",
  "precio": 25.00,
  "duracionEstimada": 30,
  "activo": true,
  "categoria": "LAVADO_COMPLETO",
  "iva": 21.0
}
```

## 🔧 Arquitectura de Integración

El CRM utiliza la siguiente estructura para consumir tu API:

```
┌─────────────────────┐
│   DashboardController│
│   (JavaFX UI)       │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  DashboardService   │  ← Lógica de negocio
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ ClienteApiService   │  ← Consumidor de API
│ CitaApiService      │
│ ServicioApiService  │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│    ApiClient        │  ← Cliente HTTP (OkHttp)
│  (Singleton)        │
└──────────┬──────────┘
           │
           ▼
    ┌──────────────┐
    │  CitasApi    │  ← Tu API REST
    │  (Railway)   │
    └──────────────┘
```

## 🚀 Pasos de Implementación

### Paso 1: Verifica tu API

Asegúrate de que tu API esté ejecutándose y responda correctamente:

```bash
# Probar que la API está activa
curl http://localhost:8080/api/clientes

# O si está en Railway
curl https://tu-app.railway.app/api/clientes
```

### Paso 2: Configura el CRM

1. Edita `api-config.properties` con la URL correcta
2. Compila el proyecto: `mvn clean install`
3. Ejecuta: `mvn javafx:run`

### Paso 3: Verifica la Conexión

Al iniciar, el CRM intentará conectarse a:
```
{api.base.url}/actuator/health
```

Si tu API no tiene actuator, considera agregarlo o el CRM mostrará una advertencia (pero funcionará igualmente).

## 🔄 Flujo de Datos

### Ejemplo: Cargar Dashboard

1. **Usuario** abre el dashboard
2. **DashboardController** llama a `DashboardService.obtenerMetricsHoy()`
3. **DashboardService** llama a:
   - `ClienteApiService.findActivos()`
   - `CitaApiService.countCitasHoy()`
   - `CitaApiService.countByEstado(PENDIENTE)`
4. **ClienteApiService** usa `ApiClient.get()` para hacer:
   ```
   GET {api.base.url}/api/clientes/activos
   ```
5. **ApiClient** realiza la petición HTTP con OkHttp
6. La **API** responde con JSON
7. **ApiClient** deserializa JSON a `ClienteDTO` usando Gson
8. Los datos vuelven al **DashboardController** que actualiza la UI

## 🐛 Troubleshooting

### Error: "No se pudo conectar a la API"

**Causas posibles:**
1. La API no está ejecutándose
2. La URL está mal configurada
3. Problemas de red/firewall
4. La API requiere autenticación y no está configurada

**Solución:**
```bash
# Verificar que la API responde
curl -v http://localhost:8080/api/clientes

# Ver logs del CRM
# Los logs mostrarán detalles de las peticiones HTTP
```

### Error: 401 Unauthorized

Tu API requiere autenticación. Configura en `api-config.properties`:
```properties
api.auth.enabled=true
api.auth.token=tu-jwt-token
```

### Error: 404 Not Found

Los endpoints no coinciden. Verifica en `api-config.properties` que las rutas sean correctas:
```properties
api.endpoints.clientes=/api/clientes  # Ajusta según tu API
```

### Error: CORS

Si la API está en otro dominio, asegúrate de tener CORS habilitado en Spring Boot:

```java
@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE");
            }
        };
    }
}
```

## 📊 Endpoints Adicionales Recomendados

Para aprovechar todas las funcionalidades del CRM, considera agregar estos endpoints a tu API:

### **Dashboard / Estadísticas**

```
GET /api/dashboard/metricas-hoy
GET /api/dashboard/facturacion-mensual
GET /api/dashboard/top-clientes?limit=10
```

### **Facturación**

```
GET    /api/facturas
GET    /api/facturas/{id}
GET    /api/facturas/cliente/{clienteId}
GET    /api/facturas/pendientes
GET    /api/facturas/periodo?inicio=X&fin=Y
POST   /api/facturas
PUT    /api/facturas/{id}
DELETE /api/facturas/{id}
```

## 🎯 Próximos Pasos

1. ✅ Verificar que todos los endpoints existen en tu API
2. ✅ Probar la conexión desde el CRM
3. ✅ Implementar endpoints faltantes si es necesario
4. ✅ Configurar autenticación si es requerida
5. ✅ Agregar endpoints de facturación
6. ✅ Implementar recordatorios automáticos

## 💡 Mejoras Futuras

- **Cache local**: Guardar datos en SQLite para trabajo offline
- **Sincronización**: Sincronizar cambios cuando vuelva la conexión
- **Websockets**: Actualizaciones en tiempo real
- **Batch operations**: Operaciones en lote para mejor rendimiento

---

**¿Necesitas ayuda?** Revisa los logs en `./logs/lavadero-crm.log` para ver detalles de las peticiones HTTP.

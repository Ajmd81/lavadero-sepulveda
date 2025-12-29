# Integración CRM con CitaApiController

Este documento explica cómo está integrado el CRM de Lavadero Sepúlveda con el backend REST del proyecto principal.

## Arquitectura de la Integración

### Backend (Proyecto Principal)
- **CitaApiController** (`src/main/java/com/lavaderosepulveda/app/controller/CitaApiController.java`)
  - Controlador REST que expone endpoints para gestión de citas
  - Endpoints principales:
    - `GET /api/citas` - Lista todas las citas
    - `GET /api/citas/{id}` - Obtiene una cita por ID
    - `POST /api/citas` - Crea una nueva cita
    - `PUT /api/citas/{id}` - Actualiza una cita
    - `PUT /api/citas/{id}/estado/{estado}` - Cambia solo el estado de una cita (sin validar horarios)
    - `DELETE /api/citas/{id}` - Elimina una cita
    - `GET /api/citas/horarios-disponibles?fecha={fecha}` - Obtiene horarios disponibles
    - `GET /api/tipos-lavado` - Obtiene tipos de lavado disponibles

- **JacksonConfig** (`src/main/java/com/lavaderosepulveda/app/config/JacksonConfig.java`)
  - Configura serialización de fechas y horas:
    - LocalDate → "dd/MM/yyyy"
    - LocalTime → "HH:mm:ss"

### CRM (Módulo JavaFX)
- **CitaApiService** (`lavadero-sepulveda-crm/src/main/java/com/lavaderosepulveda/crm/api/service/CitaApiService.java`)
  - Servicio que consume la API REST del backend
  - Métodos principales:
    - `findAll()` - Obtiene todas las citas
    - `findById(Long id)` - Obtiene una cita por ID
    - `findByFecha(LocalDate fecha)` - Filtra citas por fecha
    - `crear(CitaDTO cita)` - Crea una nueva cita
    - `actualizar(Long id, CitaDTO cita)` - Actualiza una cita
    - `cambiarEstado(Long id, EstadoCita estado)` - Cambia el estado de una cita
    - `eliminar(Long id)` - Elimina una cita

- **CitaMapper** (`lavadero-sepulveda-crm/src/main/java/com/lavaderosepulveda/crm/api/mapper/CitaMapper.java`)
  - Convierte entre `CitaApiResponseDTO` (JSON de la API) y `CitaDTO` (modelo del CRM)
  - Maneja la conversión de formatos de fecha/hora
  - Mapea tipos de lavado a servicios con precios

- **CitasController** (`lavadero-sepulveda-crm/src/main/java/com/lavaderosepulveda/crm/controller/CitasController.java`)
  - Controlador JavaFX que muestra las citas en la interfaz
  - Usa `CitaApiService` para cargar y gestionar citas

## Configuración

### 1. Configurar la URL de la API

Edita el archivo `lavadero-sepulveda-crm/src/main/resources/api-config.properties`:

```properties
# Para desarrollo local
api.base.url=http://localhost:8080

# Para producción en Railway
# api.base.url=https://lavadero-sepulveda-production.up.railway.app
```

### 2. Iniciar el Backend

```bash
cd lavadero-sepulveda
mvn spring-boot:run
```

El servidor estará disponible en `http://localhost:8080`

### 3. Iniciar el CRM

```bash
cd lavadero-sepulveda-crm
mvn javafx:run
```

## Formato de Datos

### CitaApiResponseDTO (JSON de la API)

```json
{
  "id": 1,
  "nombre": "Juan Pérez",
  "email": "juan@example.com",
  "telefono": "666123456",
  "modeloVehiculo": "Toyota Corolla",
  "tipoLavado": "LAVADO_COMPLETO_TURISMO",
  "fecha": "15/12/2025",
  "hora": "10:00:00",
  "estado": "PENDIENTE",
  "pagoAdelantado": false,
  "referenciaPago": null,
  "numeroBizum": null,
  "observaciones": "Cliente regular"
}
```

### CitaDTO (Modelo del CRM)

```java
CitaDTO {
  id: 1
  cliente: ClienteDTO {
    nombre: "Juan"
    apellidos: "Pérez"
    telefono: "666123456"
    email: "juan@example.com"
  }
  fechaHora: LocalDateTime (2025-12-15T10:00:00)
  servicios: [
    ServicioDTO {
      nombre: "Lavado Completo Turismo"
      precio: 19.01  // Sin IVA
      iva: 21.0
      precioConIva: 23.0
    }
  ]
  estado: EstadoCita.PENDIENTE
  marcaModelo: "Toyota Corolla"
  observaciones: "Cliente regular"
  facturada: false
}
```

## Mapeo de Tipos de Lavado a Precios

El `CitaMapper` incluye un mapa de precios que debe coincidir con los precios definidos en `TipoLavado` del backend:

| Tipo de Lavado | Precio con IVA |
|----------------|----------------|
| LAVADO_COMPLETO_TURISMO | 23.00€ |
| LAVADO_INTERIOR_TURISMO | 16.00€ |
| LAVADO_EXTERIOR_TURISMO | 12.00€ |
| LAVADO_COMPLETO_RANCHERA | 26.00€ |
| ... | ... |

**IMPORTANTE**: Si modificas los precios en el backend (`TipoLavado.java`), debes actualizar también el mapa `PRECIOS_LAVADO` en `CitaMapper.java`.

## Flujo de Datos

1. **Usuario abre vista de Citas en CRM**
   - `CitasController.initialize()` → `CitaApiService.findAll()` → `GET /api/citas`

2. **API devuelve JSON con citas**
   - Formato: `CitaApiResponseDTO[]`

3. **CRM convierte a modelo interno**
   - `CitaMapper.toDTOList()` convierte `CitaApiResponseDTO[]` → `CitaDTO[]`

4. **Datos se muestran en la tabla**
   - `CitasController.actualizarTabla()` muestra los datos en JavaFX

## Troubleshooting

### El CRM no se conecta a la API

1. Verifica que el backend esté corriendo: `curl http://localhost:8080/api/citas`
2. Verifica la configuración en `api-config.properties`
3. Revisa los logs del CRM en `./logs/lavadero-crm.log`

### Fechas no se muestran correctamente

Verifica que `JacksonConfig.java` tenga los formateadores correctos:
- LocalDate: "dd/MM/yyyy"
- LocalTime: "HH:mm:ss"

### Precios incorrectos en el CRM

Actualiza el mapa `PRECIOS_LAVADO` en `CitaMapper.java` para que coincida con `TipoLavado.java`

## Endpoints de la API

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/citas` | Lista todas las citas |
| GET | `/api/citas/{id}` | Obtiene una cita |
| POST | `/api/citas` | Crea una cita |
| PUT | `/api/citas/{id}` | Actualiza una cita |
| PUT | `/api/citas/{id}/estado/{estado}` | Cambia solo el estado (PENDIENTE, CONFIRMADA, COMPLETADA, CANCELADA, NO_PRESENTADO) |
| DELETE | `/api/citas/{id}` | Elimina una cita |
| GET | `/api/citas/horarios-disponibles?fecha={fecha}` | Horarios disponibles |
| GET | `/api/citas/verificar-disponibilidad?fecha={fecha}&hora={hora}` | Verifica disponibilidad |
| GET | `/api/citas/por-fecha` | Citas agrupadas por fecha |
| GET | `/api/citas/cliente/{telefono}` | Historial del cliente |
| GET | `/api/citas/estadisticas?fecha={fecha}` | Estadísticas de ocupación |
| GET | `/api/tipos-lavado` | Tipos de lavado disponibles |

## Notas de Desarrollo

- La API usa Spring Boot con Jackson para serialización JSON
- El CRM usa OkHttp (a través de `ApiClient`) para consumir la API
- El CRM hace filtrado local de datos (por fecha, estado, cliente) ya que la API no tiene filtros avanzados
- Los precios se calculan con IVA del 21% automáticamente en el CRM

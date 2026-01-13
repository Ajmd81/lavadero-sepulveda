# CRM Reorganizado - Lavadero Sepúlveda

## Estructura de Paquetes (Estándar)

```
com.lavaderosepulveda.crm/
├── LavaderoSepulvedaCRMApplication.java
├── api/
│   ├── client/              # Cliente HTTP
│   │   └── ApiClient.java
│   └── service/             # Servicios de consumo API
│       ├── CitaApiService.java
│       ├── ClienteApiService.java
│       ├── FacturaApiService.java
│       ├── FacturacionApiService.java
│       └── ServicioApiService.java
├── config/                  # Configuración
│   ├── ConfigManager.java
│   ├── DatabaseConfig.java
│   └── StageManager.java
├── controller/              # Controladores JavaFX
│   ├── CalendarioController.java
│   ├── CitasController.java
│   ├── ClientesController.java
│   ├── DashboardController.java
│   ├── FacturacionController.java
│   ├── FormularioFacturaRecibidaController.java
│   ├── FormularioGastoController.java
│   ├── FormularioProveedorController.java
│   ├── MainController.java
│   ├── PlantillaFacturaController.java
│   ├── ProveedoresController.java
│   └── ResumenFinancieroController.java
├── mapper/                  # Mappers (consolidados)
│   └── CitaMapper.java
├── model/
│   ├── dto/                 # Data Transfer Objects
│   │   ├── CitaApiResponseDTO.java
│   │   ├── CitaDTO.java
│   │   ├── ClienteDTO.java
│   │   ├── FacturaDTO.java
│   │   ├── FacturaEmitidaDTO.java
│   │   ├── FacturaRecibidaDTO.java
│   │   ├── GastoDTO.java
│   │   ├── ProveedorDTO.java
│   │   ├── ResumenFinancieroDTO.java
│   │   └── ServicioDTO.java
│   ├── entity/              # Entidades locales
│   │   ├── Cita.java
│   │   ├── Cliente.java
│   │   ├── Factura.java
│   │   ├── LineaFactura.java
│   │   ├── PlantillaFacturaConfig.java
│   │   └── Servicio.java
│   └── enums/               # Enumeraciones
│       └── EstadoCita.java
├── repository/              # Repositorios locales
│   ├── BaseRepository.java
│   ├── CitaRepository.java
│   ├── ClienteRepository.java
│   ├── FacturaRepository.java
│   └── ServicioRepository.java
├── service/                 # Servicios de negocio
│   ├── DashboardService.java
│   ├── EmailService.java
│   ├── FacturaService.java
│   ├── PDFService.java
│   ├── PlantillaFacturaService.java
│   └── WhatsAppService.java
└── util/                    # Utilidades
    ├── AlertUtil.java
    ├── DataInitializer.java
    ├── DateUtil.java
    └── ExcelClienteHandler.java
```

## Cambios Realizados

### 1. ApiClient movido a subpaquete
| Antes | Después |
|-------|---------|
| `api/ApiClient.java` | `api/client/ApiClient.java` |

### 2. DTOs consolidados en model/dto/
| Antes | Después |
|-------|---------|
| `api/dto/CitaDTO.java` | `model/dto/CitaDTO.java` |
| `api/dto/ClienteDTO.java` | `model/dto/ClienteDTO.java` |
| `model/FacturaEmitidaDTO.java` | `model/dto/FacturaEmitidaDTO.java` |
| `model/GastoDTO.java` | `model/dto/GastoDTO.java` |

### 3. Entidades movidas a model/entity/
| Antes | Después |
|-------|---------|
| `model/Cita.java` | `model/entity/Cita.java` |
| `model/Cliente.java` | `model/entity/Cliente.java` |
| `model/Factura.java` | `model/entity/Factura.java` |

### 4. Enums movidos a model/enums/
| Antes | Después |
|-------|---------|
| `model/EstadoCita.java` | `model/enums/EstadoCita.java` |

### 5. Mapper consolidado
| Antes | Después |
|-------|---------|
| `api/mapper/CitaMapper.java` | `mapper/CitaMapper.java` |
| `mapper/CitaMapper.java` (duplicado) | *(eliminado)* |

## Instrucciones de Migración

```bash
# 1. Backup de seguridad
cp -r src/main/java/com/lavaderosepulveda/crm src/main/java/com/lavaderosepulveda/crm.backup

# 2. Eliminar el código actual
rm -rf src/main/java/com/lavaderosepulveda/crm

# 3. Copiar el nuevo código (desde el ZIP descomprimido)
cp -r crm-reorganizado/src/main/java/com/lavaderosepulveda/crm src/main/java/com/lavaderosepulveda/

# 4. Compilar
mvn clean compile

# 5. Probar
mvn javafx:run
```

## Cambios de Imports

Los imports han sido actualizados automáticamente. Ejemplos:

```java
// Antes
import com.lavaderosepulveda.crm.api.ApiClient;
import com.lavaderosepulveda.crm.api.dto.CitaDTO;
import com.lavaderosepulveda.crm.model.Cita;
import com.lavaderosepulveda.crm.model.EstadoCita;

// Después
import com.lavaderosepulveda.crm.api.client.ApiClient;
import com.lavaderosepulveda.crm.model.dto.CitaDTO;
import com.lavaderosepulveda.crm.model.entity.Cita;
import com.lavaderosepulveda.crm.model.enums.EstadoCita;
```

## Convenciones de Nombres

| Tipo | Patrón | Ubicación |
|------|--------|-----------|
| DTO | `{Nombre}DTO.java` | `model/dto/` |
| Entidad | `{Nombre}.java` | `model/entity/` |
| Enum | `{NombreEnum}.java` | `model/enums/` |
| Controller | `{Modulo}Controller.java` | `controller/` |
| API Service | `{Modulo}ApiService.java` | `api/service/` |
| Service | `{Modulo}Service.java` | `service/` |
| Repository | `{Entidad}Repository.java` | `repository/` |
| Mapper | `{Entidad}Mapper.java` | `mapper/` |

---
*Generado el 13/01/2026 - Lavadero Sepúlveda CRM*

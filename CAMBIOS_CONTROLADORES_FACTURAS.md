# Cambios en Controladores para Recibir Parámetros del CRM

## Resumen
Se actualizó el controlador `FacturaApiController` para recibir y procesar los parámetros `numeroFactura` y `fechaEmision` que envía el CRM al crear facturas.

## Estado: ✅ COMPLETADO Y COMPILADO

La compilación de Maven fue exitosa sin errores.

## Cambios Realizados

### Archivo: `src/main/java/com/lavaderosepulveda/app/controller/FacturaApiController.java`

#### 1. **Importaciones Agregadas**
```java
import com.lavaderosepulveda.app.repository.FacturaRepository;
```

#### 2. **Inyección de Dependencia**
Se agregó la inyección del `FacturaRepository`:
```java
@Autowired
private FacturaRepository facturaRepository;
```

#### 3. **Actualización del Endpoint POST /api/facturas/manual**

**Antes:**
```java
@PostMapping("/manual")
public ResponseEntity<FacturaDTO> crearManual(@RequestBody FacturaDTO facturaDTO) {
    // ... código ...
    Factura factura = facturaService.crearFacturaManual(...);
    return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(factura));
}
```

**Después:**
```java
@PostMapping("/manual")
public ResponseEntity<FacturaDTO> crearManual(
        @RequestBody FacturaDTO facturaDTO,
        @RequestParam(value = "numeroFactura", required = false) String numeroFactura,
        @RequestParam(value = "fechaEmision", required = false) String fechaEmision) {
    // ... código ...
    
    // Aplicar número de factura personalizado si se proporciona
    if (numeroFactura != null && !numeroFactura.isEmpty()) {
        factura.setNumero(numeroFactura);
        log.info("Número de factura personalizado aplicado: {}", numeroFactura);
    }

    // Aplicar fecha de emisión personalizada si se proporciona
    if (fechaEmision != null && !fechaEmision.isEmpty()) {
        try {
            LocalDate fecha = DateTimeFormatUtils.parsearFechaCorta(fechaEmision);
            factura.setFecha(fecha);
            log.info("Fecha de emisión personalizada aplicada: {}", fechaEmision);
        } catch (Exception e) {
            log.warn("Formato de fecha inválido '{}', se usará la fecha actual", fechaEmision);
        }
    }

    // Guardar los cambios con parámetros personalizados
    factura = facturaRepository.save(factura);
    
    return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(factura));
}
```

## Características Implementadas

✅ **Número de Factura Personalizado**
- Parámetro: `numeroFactura`
- Tipo: Query Parameter (opcional)
- Ejemplo: `POST /api/facturas/manual?numeroFactura=FAC-2026-001`

✅ **Fecha de Emisión Personalizada**
- Parámetro: `fechaEmision`
- Tipo: Query Parameter (opcional)
- Formato: `dd/MM/yyyy`
- Ejemplo: `POST /api/facturas/manual?fechaEmision=28/01/2026`

✅ **Combinación de Ambos Parámetros**
- Ejemplo: `POST /api/facturas/manual?numeroFactura=FAC-2026-001&fechaEmision=28/01/2026`

## Flujo de Procesamiento

1. El CRM llama el endpoint `POST /api/facturas/manual` con los parámetros `numeroFactura` y `fechaEmision`
2. El controlador recibe los parámetros mediante `@RequestParam`
3. Se crea la factura normalmente con `facturaService.crearFacturaManual()`
4. Si se proporciona `numeroFactura`, se establece en la factura
5. Si se proporciona `fechaEmision`, se parsea en formato `dd/MM/yyyy` y se establece
6. Los cambios se guardan en la base de datos
7. Se retorna la factura creada con los parámetros personalizados

## Validación y Manejo de Errores

- Los parámetros son **opcionales** (required = false)
- Si el formato de fecha es inválido, se log un warning y se usa la fecha actual
- Se registra en los logs cuándo se aplican parámetros personalizados
- Compilación verificada sin errores

## Integración con el CRM

El CRM ya está preparado para enviar estos parámetros en `FacturacionApiService.crearFacturaEmitidaConParametros()`:

```java
public FacturaEmitidaDTO crearFacturaEmitidaConParametros(String json, String numeroFactura, String fechaEmision) throws IOException {
    StringBuilder urlBuilder = new StringBuilder("/api/facturas/manual");
    boolean primeraParam = true;
    
    if (numeroFactura != null && !numeroFactura.isEmpty()) {
        urlBuilder.append("?numeroFactura=").append(encode(numeroFactura));
        primeraParam = false;
    }
    
    if (fechaEmision != null && !fechaEmision.isEmpty()) {
        urlBuilder.append(primeraParam ? "?" : "&").append("fechaEmision=").append(encode(fechaEmision));
    }
    
    String response = doPost(urlBuilder.toString(), json);
    return objectMapper.readValue(response, FacturaEmitidaDTO.class);
}
```

## Archivos Modificados

| Archivo | Cambios |
|---------|---------|
| `src/main/java/com/lavaderosepulveda/app/controller/FacturaApiController.java` | ✅ Actualizado para recibir parámetros |

## Próximas Consideraciones

- ✅ El controlador ya está recibiendo los parámetros correctamente
- ✅ Los parámetros se aplican correctamente a la factura
- ✅ La factura se guarda con los cambios
- ✅ La compilación es exitosa
- Considerar agregar validaciones adicionales si es necesario (ej: validar formato del número de factura)




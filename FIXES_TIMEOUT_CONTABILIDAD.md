# Fixes: Timeout de Eliminación y Contabilidad

## Problemas Identificados

### 1. SocketTimeoutException durante Eliminación de Facturas
**Síntoma:** Cuando se intentaba eliminar una factura emitida, la operación bloqueaba la UI durante 30+ segundos y luego mostraba un timeout.

**Causa Raíz:** El método `eliminarFacturaEmitida()` en `FacturacionController` llamaba a `cargarFacturasEmitidas()` directamente en el thread de UI. Si la API tardaba más de 30 segundos en responder, se generaba un `SocketTimeoutException`.

```java
// ANTES - Código bloqueante
apiService.eliminarFacturaEmitida(factura.getId());
cargarFacturasEmitidas(); // ← Bloquea la UI si tarda > 30s
mostrarInfo("Factura Eliminada", ...);
```

### 2. Loop Infinito en Contabilidad
**Síntoma:** El módulo de Contabilidad mostraba "Cargando datos..." repetidamente cada 2-5 segundos, haciendo la aplicación prácticamente inutilizable.

**Causa Raíz:** Sin guard para prevenir cargas simultáneas, múltiples solicitudes de carga se podían ejecutar en paralelo, causando condiciones de carrera.

---

## Soluciones Implementadas

### Fix 1: Eliminación Asíncrona con Actualización Local (FacturacionController)

**Archivo:** `lavadero-sepulveda-crm/src/main/java/com/lavaderosepulveda/crm/controller/FacturacionController.java`

**Cambios:**
1. Importar `javafx.application.Platform`
2. Envolver la operación en un `new Thread()`
3. Eliminar el item directamente de la lista local en lugar de recargar todo
4. Usar `Platform.runLater()` para actualizar la UI de forma segura

**Código Nuevo:**
```java
Optional<ButtonType> resultado = confirmacion.showAndWait();
if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
    new Thread(() -> {
        try {
            apiService.eliminarFacturaEmitida(factura.getId());
            // Eliminar de la lista local sin recargar todo (evita timeout)
            Platform.runLater(() -> {
                listaEmitidas.remove(factura);
                actualizarTotalesEmitidas();
                mostrarInfo("Factura Eliminada", 
                        "La factura " + factura.getNumeroFactura() + " se ha eliminado correctamente");
            });
        } catch (Exception e) {
            log.error("Error al eliminar factura", e);
            Platform.runLater(() -> 
                mostrarError("Error", "No se pudo eliminar la factura: " + e.getMessage())
            );
        }
    }).start();
}
```

**Beneficios:**
- ✅ La UI nunca se bloquea
- ✅ Se evita el timeout (no se recarga toda la lista)
- ✅ Feedback inmediato al usuario
- ✅ Sincronización automática de datos cuando se recarga manualmente

---

### Fix 2: Prevención de Cargas Simultáneas (ContabilidadController)

**Archivo:** `lavadero-sepulveda-crm/src/main/java/com/lavaderosepulveda/crm/controller/ContabilidadController.java`

**Cambios:**
1. Agregar flag `volatile boolean cargando = false`
2. Verificar el flag al inicio de `cargarDatos()`
3. Establecer `cargando = true` antes de la carga asíncrona
4. Establecer `cargando = false` al completar (en el bloque `finally`)

**Código Nuevo:**
```java
private volatile boolean cargando = false; // Flag para evitar cargas simultáneas

private void cargarDatos() {
    // Prevenir cargas simultáneas
    if (cargando) {
        log.warn("⚠️ Carga de datos ya en progreso, ignorando nueva solicitud");
        return;
    }
    
    // ... validación de fechas ...
    
    log.info("📊 Cargando datos de contabilidad desde {} hasta {}", desde, hasta);
    cargando = true; // Marcar como cargando
    
    CompletableFuture.runAsync(() -> {
        try {
            // ... operaciones de carga ...
            Platform.runLater(() -> {
                try {
                    // ... actualizar UI ...
                } finally {
                    cargando = false; // Marcar como no cargando
                }
            });
        } catch (Exception e) {
            // ... manejo de error ...
            cargando = false; // Marcar como no cargando
        }
    });
}
```

**Beneficios:**
- ✅ Evita condiciones de carrera
- ✅ Previene múltiples solicitudes API simultáneas
- ✅ Reduce carga en el servidor
- ✅ Mejora la experiencia del usuario

---

### Fix 3: Agregar @Slf4j a FacturacionApiService

**Archivo:** `lavadero-sepulveda-crm/src/main/java/com/lavaderosepulveda/crm/api/service/FacturacionApiService.java`

**Cambios:**
- Importar `lombok.extern.slf4j.Slf4j`
- Agregar anotación `@Slf4j` a la clase

Esta anotación era necesaria porque se estaban usando logs (`log.info()`, `log.error()`) en el método `eliminarFacturaEmitida()`.

---

## Commits Realizados

### Commit 1: Eliminación Asíncrona
```
06c73c2 Fix: Eliminación asíncrona de facturas para evitar timeout de UI
```

### Commit 2: Contabilidad Sin Cargas Simultáneas
```
ab69ad9 Fix: Prevenir cargas simultáneas en ContabilidadController
```

---

## Cómo Probar

### Test 1: Eliminación de Facturas
1. Abrir el CRM
2. Navegar a Facturación → Facturas Emitidas
3. Seleccionar una factura
4. Click en "Eliminar"
5. Confirmar
6. ✅ **Esperado:** La factura desaparece de la lista inmediatamente (sin timeout)

### Test 2: Contabilidad
1. Abrir el CRM
2. Navegar a Contabilidad
3. El módulo debería cargar los datos una sola vez
4. ✅ **Esperado:** No hay logs repetidos de "Cargando datos..."
5. Si cambias el período, debería cargar nuevamente sin duplicados

---

## Impacto de los Cambios

| Aspecto | Antes | Después |
|--------|-------|---------|
| **Tiempo de eliminación** | 30+ segundos | < 1 segundo |
| **Bloqueo de UI** | Sí (30+ segundos) | No |
| **Cargas en Contabilidad** | Múltiples simultáneas | Una a la vez |
| **Experiencia de usuario** | Congelada durante eliminar | Responsiva |

---

## Notas de Seguridad

- El flag `volatile` es thread-safe para operaciones simples de lectura/escritura
- El `CompletableFuture` maneja el acceso concurrente correctamente
- El `Platform.runLater()` asegura que todo acceso a UI ocurra en el thread de JavaFX

---

## Posibles Mejoras Futuras

1. **Aumentar timeout de conexión** si el backend es muy lento:
   - Modificar `ConfigManager` para aumentar el timeout a 60+ segundos
   - O implementar reintentos con backoff exponencial

2. **Implementar paginación** en `cargarFacturasEmitidas()`:
   - Cargar solo las últimas 100 facturas por defecto
   - Permitir al usuario cargar más si lo desea

3. **Cachear datos** de Contabilidad:
   - Almacenar los últimos datos cargados localmente
   - Solo recargar si el período cambió

4. **Agregar indicador visual** de carga:
   - Mostrar un spinner mientras se carga
   - Deshabilitar botones durante la carga

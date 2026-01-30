package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.FacturaDTO;
import com.lavaderosepulveda.app.model.Factura;
import com.lavaderosepulveda.app.model.LineaFactura;
import com.lavaderosepulveda.app.model.enums.TipoFactura;
import com.lavaderosepulveda.app.model.enums.EstadoFactura;
import com.lavaderosepulveda.app.model.enums.MetodoPago;
import com.lavaderosepulveda.app.repository.FacturaRepository;
import com.lavaderosepulveda.app.service.FacturaService;
import com.lavaderosepulveda.app.util.DateTimeFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/facturas")
public class FacturaApiController {

    private static final Logger log = LoggerFactory.getLogger(FacturaApiController.class);

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private FacturaRepository facturaRepository;

    // ========================================
    // CRUD BÁSICO
    // ========================================

    /**
     * GET /api/facturas
     * Obtener todas las facturas (ordenadas por número por defecto)
     */
    @GetMapping
    public ResponseEntity<List<FacturaDTO>> listarFacturas(
            @RequestParam(value = "ordenar", required = false, defaultValue = "numero") String ordenar) {
        List<Factura> facturas = facturaService.obtenerTodas();
        
        // Ordenar según parámetro
        if ("numero".equalsIgnoreCase(ordenar)) {
            facturas.sort((a, b) -> {
                String numA = a.getNumero() != null ? a.getNumero() : "";
                String numB = b.getNumero() != null ? b.getNumero() : "";
                return numB.compareTo(numA); // Orden descendente (más nuevo primero)
            });
        } else if ("fecha".equalsIgnoreCase(ordenar)) {
            facturas.sort((a, b) -> b.getFecha().compareTo(a.getFecha())); // Más reciente primero
        }
        
        List<FacturaDTO> facturasDTO = facturas.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(facturasDTO);
    }

    /**
     * GET /api/facturas/{id}
     * Obtener factura por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FacturaDTO> obtenerPorId(@PathVariable Long id) {
        return facturaService.obtenerPorId(id)
                .map(factura -> ResponseEntity.ok(convertirADTO(factura)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/facturas/numero/{numero}
     * Obtener factura por número
     */
    @GetMapping("/numero/{numero}")
    public ResponseEntity<FacturaDTO> obtenerPorNumero(@PathVariable String numero) {
        return facturaService.obtenerPorNumero(numero)
                .map(factura -> ResponseEntity.ok(convertirADTO(factura)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/facturas/{id}
     * Eliminar factura
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            log.info("📋 Eliminando factura con ID: {}", id);
            facturaService.eliminar(id);
            log.info("✅ Factura {} eliminada correctamente", id);
            return ResponseEntity.ok(Map.of("mensaje", "Factura eliminada correctamente", "id", id));
        } catch (RuntimeException e) {
            log.error("❌ Error al eliminar factura {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", e.getMessage(),
                            "codigo", "ERROR_ELIMINAR_FACTURA",
                            "id", id
                    ));
        } catch (Exception e) {
            log.error("❌ Error inesperado al eliminar factura {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error interno del servidor: " + e.getMessage(),
                            "codigo", "ERROR_INTERNO",
                            "id", id
                    ));
        }
    }

    // ========================================
    // CREACIÓN DE FACTURAS
    // ========================================

    /**
     * POST /api/facturas
     * Crear factura manual genérica (redirección al método manual)
     * Permite crear facturas sin especificar la ruta /manual
     */
    @PostMapping
    public ResponseEntity<FacturaDTO> crear(@RequestBody FacturaDTO facturaDTO) {
        try {
            log.info("📋 Creando factura desde POST /api/facturas");
            TipoFactura tipo = TipoFactura.valueOf(facturaDTO.getTipo());

            List<LineaFactura> lineas = new ArrayList<>();
            if (facturaDTO.getLineas() != null) {
                for (FacturaDTO.LineaFacturaDTO lineaDTO : facturaDTO.getLineas()) {
                    LineaFactura linea = new LineaFactura();
                    linea.setConcepto(lineaDTO.getConcepto());
                    linea.setCantidad(lineaDTO.getCantidad() != null ? lineaDTO.getCantidad() : 1);
                    linea.setPrecioUnitario(lineaDTO.getPrecioUnitario());
                    linea.calcularSubtotal();
                    lineas.add(linea);
                }
            }

            Factura factura = facturaService.crearFacturaManual(
                    tipo,
                    facturaDTO.getClienteNombre(),
                    facturaDTO.getClienteNif(),
                    facturaDTO.getClienteDireccion(),
                    facturaDTO.getClienteTelefono(),
                    facturaDTO.getClienteEmail(),
                    lineas);

            // Guardar la factura
            factura = facturaRepository.save(factura);
            log.info("✅ Factura creada: ID={}, Número={}, Fecha={}", 
                    factura.getId(), factura.getNumero(), factura.getFecha());

            return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(factura));
        } catch (Exception e) {
            log.error("❌ Error al crear factura: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * POST /api/facturas/simplificada/cita/{citaId}
     * Crear factura simplificada desde una cita
     */
    @PostMapping("/simplificada/cita/{citaId}")
    public ResponseEntity<FacturaDTO> crearSimplificadaDesdeCita(@PathVariable Long citaId) {
        try {
            Factura factura = facturaService.crearFacturaSimplificadaDesdeCita(citaId);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(factura));
        } catch (RuntimeException e) {
            log.error("Error al crear factura simplificada: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/facturas/completa
     * Crear factura completa
     */
    @PostMapping("/completa")
    public ResponseEntity<FacturaDTO> crearCompleta(@RequestBody Map<String, Object> request) {
        try {
            Long clienteId = Long.valueOf(request.get("clienteId").toString());
            @SuppressWarnings("unchecked")
            List<Integer> citaIdsInt = (List<Integer>) request.get("citaIds");
            List<Long> citaIds = citaIdsInt.stream().map(Long::valueOf).collect(Collectors.toList());
            String clienteNif = (String) request.get("clienteNif");
            String clienteDireccion = (String) request.get("clienteDireccion");

            Factura factura = facturaService.crearFacturaCompleta(clienteId, citaIds, clienteNif, clienteDireccion);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(factura));
        } catch (Exception e) {
            log.error("Error al crear factura completa: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/facturas/manual
     * Crear factura manual sin citas asociadas
     * Parámetros opcionales:
     * - numeroFactura: Número de factura personalizado
     * - fechaEmision: Fecha de emisión en formato dd/MM/yyyy
     */
    @PostMapping("/manual")
    public ResponseEntity<FacturaDTO> crearManual(
            @RequestBody FacturaDTO facturaDTO,
            @RequestParam(value = "numeroFactura", required = false) String numeroFactura,
            @RequestParam(value = "fechaEmision", required = false) String fechaEmision) {
        try {
            TipoFactura tipo = TipoFactura.valueOf(facturaDTO.getTipo());

            List<LineaFactura> lineas = new ArrayList<>();
            if (facturaDTO.getLineas() != null) {
                for (FacturaDTO.LineaFacturaDTO lineaDTO : facturaDTO.getLineas()) {
                    LineaFactura linea = new LineaFactura();
                    linea.setConcepto(lineaDTO.getConcepto());
                    linea.setCantidad(lineaDTO.getCantidad() != null ? lineaDTO.getCantidad() : 1);
                    linea.setPrecioUnitario(lineaDTO.getPrecioUnitario());
                    linea.calcularSubtotal();
                    lineas.add(linea);
                }
            }

            Factura factura = facturaService.crearFacturaManual(
                    tipo,
                    facturaDTO.getClienteNombre(),
                    facturaDTO.getClienteNif(),
                    facturaDTO.getClienteDireccion(),
                    facturaDTO.getClienteTelefono(),
                    facturaDTO.getClienteEmail(),
                    lineas);

            // Aplicar número de factura personalizado si se proporciona
            if (numeroFactura != null && !numeroFactura.isEmpty()) {
                factura.setNumero(numeroFactura);
                log.info("✅ Número de factura personalizado aplicado: {} (será: {})", numeroFactura, factura.getNumero());
            }

            // Aplicar fecha de emisión personalizada si se proporciona
            if (fechaEmision != null && !fechaEmision.isEmpty()) {
                try {
                    LocalDate fecha = DateTimeFormatUtils.parsearFechaCorta(fechaEmision);
                    factura.setFecha(fecha);
                    log.info("✅ Fecha de emisión personalizada aplicada: {} -> {} (será: {})", fechaEmision, fecha, factura.getFecha());
                } catch (Exception e) {
                    log.warn("❌ Formato de fecha inválido '{}', se usará la fecha actual: {}", fechaEmision, e.getMessage());
                }
            }

            // Guardar los cambios con parámetros personalizados
            factura = facturaRepository.save(factura);
            
            // Log de verificación POST-SAVE
            log.info("📋 Factura guardada: numero='{}', fecha='{}', id={}", 
                    factura.getNumero(), factura.getFecha(), factura.getId());

            FacturaDTO resultado = convertirADTO(factura);
            log.info("📤 DTO retornado: numero='{}', fecha='{}', numeroFactura (esperado: '{}')", 
                    resultado.getNumero(), resultado.getFecha(), numeroFactura);

            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        } catch (Exception e) {
            log.error("Error al crear factura manual: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/facturas/{id} - Actualizar una factura existente
     * Actualiza los datos básicos y las líneas de la factura
     */
    @PutMapping("/{id}")
    public ResponseEntity<FacturaDTO> actualizar(
            @PathVariable Long id,
            @RequestBody FacturaDTO facturaDTO) {
        try {
            Factura facturaExistente = facturaRepository.findById(id)
                    .orElseThrow(() -> new Exception("Factura no encontrada"));

            // Actualizar datos básicos
            if (facturaDTO.getClienteNombre() != null) {
                facturaExistente.setClienteNombre(facturaDTO.getClienteNombre());
            }
            if (facturaDTO.getClienteNif() != null) {
                facturaExistente.setClienteNif(facturaDTO.getClienteNif());
            }
            if (facturaDTO.getClienteDireccion() != null) {
                facturaExistente.setClienteDireccion(facturaDTO.getClienteDireccion());
            }
            if (facturaDTO.getClienteEmail() != null) {
                facturaExistente.setClienteEmail(facturaDTO.getClienteEmail());
            }
            if (facturaDTO.getClienteTelefono() != null) {
                facturaExistente.setClienteTelefono(facturaDTO.getClienteTelefono());
            }

            // Actualizar líneas
            if (facturaDTO.getLineas() != null) {
                // Eliminar líneas existentes
                facturaExistente.getLineas().clear();

                // Agregar nuevas líneas
                for (FacturaDTO.LineaFacturaDTO lineaDTO : facturaDTO.getLineas()) {
                    LineaFactura linea = new LineaFactura();
                    linea.setConcepto(lineaDTO.getConcepto());
                    linea.setCantidad(lineaDTO.getCantidad() != null ? lineaDTO.getCantidad() : 1);
                    linea.setPrecioUnitario(lineaDTO.getPrecioUnitario());
                    linea.calcularSubtotal();
                    linea.setFactura(facturaExistente);
                    facturaExistente.getLineas().add(linea);
                }
            }

            // Recalcular totales
            BigDecimal baseImponible = facturaExistente.getLineas().stream()
                    .map(LineaFactura::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            facturaExistente.setBaseImponible(baseImponible);
            
            // Aplicar IVA si se proporciona
            if (facturaDTO.getTipoIva() != null) {
                facturaExistente.setTipoIva(facturaDTO.getTipoIva());
                BigDecimal iva = baseImponible.multiply(facturaDTO.getTipoIva()).divide(new BigDecimal(100));
                facturaExistente.setImporteIva(iva);
                facturaExistente.setTotal(baseImponible.add(iva));
            } else {
                facturaExistente.setTotal(baseImponible);
            }

            facturaExistente = facturaRepository.save(facturaExistente);
            
            log.info("✅ Factura actualizada: id='{}', numero='{}', cliente='{}'",
                    facturaExistente.getId(), facturaExistente.getNumero(), facturaExistente.getClienteNombre());

            FacturaDTO resultado = convertirADTO(facturaExistente);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al actualizar factura: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // GESTIÓN DE PAGOS
    // ========================================

    /**
     * PUT /api/facturas/{id}/pagar
     * Marcar factura como pagada
     */
    @PutMapping("/{id}/pagar")
    public ResponseEntity<FacturaDTO> marcarComoPagada(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            MetodoPago metodoPago = MetodoPago.valueOf(request.get("metodoPago"));
            Factura factura = facturaService.marcarComoPagada(id, metodoPago);
            return ResponseEntity.ok(convertirADTO(factura));
        } catch (Exception e) {
            log.error("Error al marcar factura como pagada: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // CONSULTAS
    // ========================================

    /**
     * GET /api/facturas/estado/{estado}
     * Obtener facturas por estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<FacturaDTO>> obtenerPorEstado(@PathVariable String estado) {
        try {
            EstadoFactura estadoFactura = EstadoFactura.valueOf(estado.toUpperCase());
            List<Factura> facturas = facturaService.obtenerPorEstado(estadoFactura);
            List<FacturaDTO> facturasDTO = facturas.stream()
                    .map(this::convertirADTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(facturasDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/facturas/pendientes
     * Obtener facturas pendientes de cobro
     */
    @GetMapping("/pendientes")
    public ResponseEntity<List<FacturaDTO>> obtenerPendientes() {
        List<Factura> facturas = facturaService.obtenerPendientes();
        List<FacturaDTO> facturasDTO = facturas.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(facturasDTO);
    }

    /**
     * GET /api/facturas/cliente/{clienteId}
     * Obtener facturas de un cliente
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<FacturaDTO>> obtenerPorCliente(@PathVariable Long clienteId) {
        List<Factura> facturas = facturaService.obtenerPorCliente(clienteId);
        List<FacturaDTO> facturasDTO = facturas.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(facturasDTO);
    }

    /**
     * GET /api/facturas/fecha?desde=dd/MM/yyyy&hasta=dd/MM/yyyy
     * Obtener facturas por rango de fechas
     */
    @GetMapping("/fecha")
    public ResponseEntity<List<FacturaDTO>> obtenerPorFechas(
            @RequestParam("desde") String desdeStr,
            @RequestParam("hasta") String hastaStr) {
        try {
            LocalDate desde = DateTimeFormatUtils.parsearFechaCorta(desdeStr);
            LocalDate hasta = DateTimeFormatUtils.parsearFechaCorta(hastaStr);

            List<Factura> facturas = facturaService.obtenerPorFechas(desde, hasta);
            List<FacturaDTO> facturasDTO = facturas.stream()
                    .map(this::convertirADTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(facturasDTO);
        } catch (Exception e) {
            log.error("Error al obtener facturas por fecha: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/facturas/buscar?q=texto
     * Buscar facturas
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<FacturaDTO>> buscar(@RequestParam("q") String texto) {
        List<Factura> facturas = facturaService.buscar(texto);
        List<FacturaDTO> facturasDTO = facturas.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(facturasDTO);
    }

    /**
     * GET /api/facturas/hoy
     * Obtener facturas de hoy
     */
    @GetMapping("/hoy")
    public ResponseEntity<List<FacturaDTO>> obtenerDeHoy() {
        List<Factura> facturas = facturaService.obtenerDeHoy();
        List<FacturaDTO> facturasDTO = facturas.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(facturasDTO);
    }

    // ========================================
    // ESTADÍSTICAS Y RESUMEN
    // ========================================

    /**
     * GET /api/facturas/resumen
     * Obtener resumen de facturación
     */
    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Object>> obtenerResumen() {
        Map<String, Object> resumen = facturaService.obtenerResumen();
        return ResponseEntity.ok(resumen);
    }

    /**
     * GET /api/facturas/emisor
     * Obtener datos fiscales del emisor
     */
    @GetMapping("/emisor")
    public ResponseEntity<Map<String, String>> obtenerDatosEmisor() {
        Map<String, String> datos = facturaService.obtenerDatosEmisor();
        return ResponseEntity.ok(datos);
    }

    // ========================================
    // MÉTODOS PRIVADOS
    // ========================================

    private FacturaDTO convertirADTO(Factura factura) {
        FacturaDTO dto = new FacturaDTO();
        dto.setId(factura.getId());
        // ✅ CORREGIDO: Usar 'numero' para compatibilidad con cliente FacturaEmitidaDTO que espera 'numeroFactura' (mapeado via @JsonAlias)
        dto.setNumero(factura.getNumero());
        // ✅ CORREGIDO: Usar LocalDate directamente - Jackson lo formateará con @JsonFormat(pattern="dd/MM/yyyy")
        dto.setFecha(factura.getFecha());
        dto.setTipo(factura.getTipo().name());
        dto.setEstado(factura.getEstado().name());
        dto.setMetodoPago(factura.getMetodoPago() != null ? factura.getMetodoPago().name() : null);

        // Cliente
        dto.setClienteId(factura.getCliente() != null ? factura.getCliente().getId() : null);
        dto.setClienteNombre(factura.getClienteNombre());
        dto.setClienteNif(factura.getClienteNif());
        dto.setClienteDireccion(factura.getClienteDireccion());
        dto.setClienteEmail(factura.getClienteEmail());
        dto.setClienteTelefono(factura.getClienteTelefono());

        // Importes
        dto.setBaseImponible(factura.getBaseImponible());
        dto.setTipoIva(factura.getTipoIva());
        dto.setImporteIva(factura.getImporteIva());
        dto.setTotal(factura.getTotal());

        // Otros
        dto.setFechaPago(factura.getFechaPago());
        dto.setObservaciones(factura.getObservaciones());

        // Líneas
        List<FacturaDTO.LineaFacturaDTO> lineasDTO = factura.getLineas().stream()
                .map(linea -> {
                    FacturaDTO.LineaFacturaDTO lineaDTO = new FacturaDTO.LineaFacturaDTO();
                    lineaDTO.setId(linea.getId());
                    lineaDTO.setCitaId(linea.getCitaId());
                    lineaDTO.setConcepto(linea.getConcepto());
                    lineaDTO.setCantidad(linea.getCantidad());
                    lineaDTO.setPrecioUnitario(linea.getPrecioUnitario());
                    lineaDTO.setSubtotal(linea.getSubtotal());
                    return lineaDTO;
                })
                .collect(Collectors.toList());
        dto.setLineas(lineasDTO);

        return dto;
    }
}

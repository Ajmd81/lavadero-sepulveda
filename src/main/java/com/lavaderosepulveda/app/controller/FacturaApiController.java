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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
    public ResponseEntity<Map<String, Object>> listarFacturas(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
            @RequestParam(value = "ordenar", required = false, defaultValue = "numero") String ordenar) {

        // Configurar ordenamiento
        Sort.Direction direction = Sort.Direction.DESC;
        String orderByField = "numero";

        if ("fecha".equalsIgnoreCase(ordenar)) {
            orderByField = "fecha";
            direction = Sort.Direction.DESC;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, orderByField));
        Page<Factura> pageFacturas = facturaRepository.findAllWithLineas(pageable);

        // Convertir a DTO
        List<FacturaDTO> facturasDTO = pageFacturas.getContent().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());

        // Crear respuesta con información de paginación
        Map<String, Object> respuesta = Map.of(
                "content", facturasDTO,
                "totalElements", pageFacturas.getTotalElements(),
                "totalPages", pageFacturas.getTotalPages(),
                "currentPage", pageFacturas.getNumber(),
                "pageSize", pageFacturas.getSize(),
                "hasNext", pageFacturas.hasNext(),
                "hasPrevious", pageFacturas.hasPrevious());

        return ResponseEntity.ok(respuesta);
    }

    /**
     * GET /api/facturas/todas
     * Obtener todas las facturas como lista simple (sin paginación) - Para JavaFX
     */
    @GetMapping("/todas")
    public ResponseEntity<List<FacturaDTO>> obtenerTodasLasFacturas() {
        List<Factura> facturas = facturaRepository.findAll();
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
        return facturaRepository.findByIdWithLineas(id)
                .map(factura -> ResponseEntity.ok(convertirADTO(factura)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/facturas/siguiente-numero
     * Obtener el siguiente número de factura disponible
     */
    @GetMapping("/siguiente-numero")
    public ResponseEntity<Map<String, String>> obtenerSiguienteNumero() {
        int anio = LocalDate.now().getYear();
        Integer siguienteNumero = facturaRepository.findMaxNumeroSecuencialByAnio(anio)
                .map(max -> max + 1)
                .orElse(1);
        String numero = String.format("%d/%03d", anio, siguienteNumero);
        return ResponseEntity.ok(Map.of("numero", numero));
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
                            "id", id));
        } catch (Exception e) {
            log.error("❌ Error inesperado al eliminar factura {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error interno del servidor: " + e.getMessage(),
                            "codigo", "ERROR_INTERNO",
                            "id", id));
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
    public ResponseEntity<?> crearManual(
            @RequestBody FacturaDTO facturaDTO,
            @RequestParam(value = "numeroFactura", required = false) String numeroFactura,
            @RequestParam(value = "fechaEmision", required = false) String fechaEmision) {
        try {
            log.info("📥 Recibiendo factura manual: {}", facturaDTO);
            log.info("📥 Parámetros: numeroFactura={}, fechaEmision={}", numeroFactura, fechaEmision);

            if (facturaDTO.getTipo() == null || facturaDTO.getTipo().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El tipo de factura es obligatorio", "field", "tipo"));
            }

            TipoFactura tipo;
            try {
                tipo = TipoFactura.valueOf(facturaDTO.getTipo());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Tipo de factura inválido: " + facturaDTO.getTipo(),
                        "field", "tipo",
                        "validValues", Arrays.toString(TipoFactura.values())));
            }

            if (facturaDTO.getClienteNombre() == null || facturaDTO.getClienteNombre().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El nombre del cliente es obligatorio", "field", "clienteNombre"));
            }

            List<LineaFactura> lineas = new ArrayList<>();
            if (facturaDTO.getLineas() == null || facturaDTO.getLineas().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Debe incluir al menos una línea en la factura", "field", "lineas"));
            }

            for (int i = 0; i < facturaDTO.getLineas().size(); i++) {
                FacturaDTO.LineaFacturaDTO lineaDTO = facturaDTO.getLineas().get(i);
                if (lineaDTO.getConcepto() == null || lineaDTO.getConcepto().isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error",
                            "La línea " + (i + 1) + " no tiene concepto", "field", "lineas[" + i + "].concepto"));
                }
                if (lineaDTO.getPrecioUnitario() == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "La línea " + (i + 1) + " no tiene precio unitario", "field",
                                    "lineas[" + i + "].precioUnitario"));
                }
                LineaFactura linea = new LineaFactura();
                linea.setConcepto(lineaDTO.getConcepto());
                linea.setCantidad(lineaDTO.getCantidad() != null ? lineaDTO.getCantidad() : 1);
                linea.setPrecioUnitario(lineaDTO.getPrecioUnitario());
                linea.calcularSubtotal();
                lineas.add(linea);
            }

            Factura factura = facturaService.crearFacturaManual(
                    tipo,
                    facturaDTO.getClienteNombre(),
                    facturaDTO.getClienteNif(),
                    facturaDTO.getClienteDireccion(),
                    facturaDTO.getClienteTelefono(),
                    facturaDTO.getClienteEmail(),
                    lineas);

            // ✅ FIX 1: Número de factura personalizado — parsear y aplicar también el
            // numeroSecuencial
            if (numeroFactura != null && !numeroFactura.isEmpty()) {
                factura.setNumero(numeroFactura);
                // Extraer la parte numérica para mantener el contador sincronizado
                // Formato esperado: YYYY/NNN
                try {
                    String[] partes = numeroFactura.split("/");
                    if (partes.length == 2) {
                        int secuencial = Integer.parseInt(partes[1]);
                        factura.setNumeroSecuencial(secuencial);
                        log.info("✅ Número personalizado aplicado: {}, secuencial={}", numeroFactura, secuencial);
                    }
                } catch (NumberFormatException e) {
                    log.warn("⚠️ No se pudo parsear el secuencial de: {}", numeroFactura);
                }
            }

            // ✅ FIX 2: Fecha personalizada
            if (fechaEmision != null && !fechaEmision.isEmpty()) {
                try {
                    LocalDate fecha = DateTimeFormatUtils.parsearFechaCorta(fechaEmision);
                    factura.setFecha(fecha);
                    log.info("✅ Fecha personalizada aplicada: {}", fecha);
                } catch (Exception e) {
                    log.warn("⚠️ Formato de fecha inválido '{}', se usará la fecha actual", fechaEmision);
                }
            }

            // ✅ FIX 3: Estado y método de pago desde el DTO
            if (facturaDTO.getEstado() != null && !facturaDTO.getEstado().isEmpty()) {
                try {
                    factura.setEstado(EstadoFactura.valueOf(facturaDTO.getEstado()));
                    log.info("✅ Estado aplicado: {}", facturaDTO.getEstado());
                } catch (IllegalArgumentException e) {
                    log.warn("⚠️ Estado inválido: {}", facturaDTO.getEstado());
                }
            }

            if (facturaDTO.getMetodoPago() != null && !facturaDTO.getMetodoPago().isEmpty()) {
                try {
                    factura.setMetodoPago(MetodoPago.valueOf(facturaDTO.getMetodoPago()));
                    // Si tiene método de pago y no tiene fecha de pago, poner hoy
                    if (factura.getFechaPago() == null) {
                        factura.setFechaPago(LocalDate.now());
                    }
                    log.info("✅ Método de pago aplicado: {}", facturaDTO.getMetodoPago());
                } catch (IllegalArgumentException e) {
                    log.warn("⚠️ Método de pago inválido: {}", facturaDTO.getMetodoPago());
                }
            }

            factura = facturaRepository.save(factura);

            log.info("📋 Factura guardada: numero='{}', estado='{}', metodoPago='{}', total={}, id={}",
                    factura.getNumero(), factura.getEstado(), factura.getMetodoPago(), factura.getTotal(),
                    factura.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(factura));

        } catch (Exception e) {
            log.error("❌ Error inesperado al crear factura manual: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Error al crear la factura: " + e.getMessage(),
                    "type", e.getClass().getSimpleName()));
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
        // ✅ CORREGIDO: Usar 'numero' para compatibilidad con cliente FacturaEmitidaDTO
        // que espera 'numeroFactura' (mapeado via @JsonAlias)
        dto.setNumero(factura.getNumero());
        // ✅ CORREGIDO: Usar LocalDate directamente - Jackson lo formateará con
        // @JsonFormat(pattern="dd/MM/yyyy")
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

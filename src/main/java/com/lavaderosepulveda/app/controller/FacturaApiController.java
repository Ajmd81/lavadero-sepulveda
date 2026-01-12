package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.FacturaDTO;
import com.lavaderosepulveda.app.model.Factura;
import com.lavaderosepulveda.app.model.LineaFactura;
import com.lavaderosepulveda.app.model.enums.TipoFactura;
import com.lavaderosepulveda.app.model.enums.EstadoFactura;
import com.lavaderosepulveda.app.model.enums.MetodoPago;
import com.lavaderosepulveda.app.service.FacturaService;
import com.lavaderosepulveda.app.util.DateTimeFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // ========================================
    // CRUD BÁSICO
    // ========================================

    /**
     * GET /api/facturas
     * Obtener todas las facturas
     */
    @GetMapping
    public ResponseEntity<List<FacturaDTO>> listarFacturas() {
        List<Factura> facturas = facturaService.obtenerTodas();
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
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            facturaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar factura: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // CREACIÓN DE FACTURAS
    // ========================================

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
     */
    @PostMapping("/manual")
    public ResponseEntity<FacturaDTO> crearManual(@RequestBody FacturaDTO facturaDTO) {
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

            return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(factura));
        } catch (Exception e) {
            log.error("Error al crear factura manual: {}", e.getMessage());
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
        dto.setNumero(factura.getNumero());
        dto.setFecha(DateTimeFormatUtils.formatearFechaCorta(factura.getFecha()));
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
        dto.setFechaPago(
                factura.getFechaPago() != null ? DateTimeFormatUtils.formatearFechaCorta(factura.getFechaPago())
                        : null);
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

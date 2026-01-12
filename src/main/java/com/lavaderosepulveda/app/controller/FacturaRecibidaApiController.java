package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.FacturaRecibidaDTO;
import com.lavaderosepulveda.app.service.FacturaRecibidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facturas-recibidas")
@CrossOrigin(origins = "*")
public class FacturaRecibidaApiController {

    @Autowired
    private FacturaRecibidaService facturaRecibidaService;

    @GetMapping
    public ResponseEntity<List<FacturaRecibidaDTO>> listar() {
        return ResponseEntity.ok(facturaRecibidaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaRecibidaDTO> obtener(@PathVariable Long id) {
        FacturaRecibidaDTO factura = facturaRecibidaService.obtenerPorId(id);
        if (factura == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(factura);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<FacturaRecibidaDTO>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(facturaRecibidaService.listarPorEstado(estado));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<FacturaRecibidaDTO>> listarPendientes() {
        return ResponseEntity.ok(facturaRecibidaService.listarPendientes());
    }

    @GetMapping("/vencidas")
    public ResponseEntity<List<FacturaRecibidaDTO>> listarVencidas() {
        return ResponseEntity.ok(facturaRecibidaService.listarVencidas());
    }

    @GetMapping("/proveedor/{proveedorId}")
    public ResponseEntity<List<FacturaRecibidaDTO>> listarPorProveedor(@PathVariable Long proveedorId) {
        return ResponseEntity.ok(facturaRecibidaService.listarPorProveedor(proveedorId));
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<FacturaRecibidaDTO>> listarPorCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(facturaRecibidaService.listarPorCategoria(categoria));
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<FacturaRecibidaDTO>> listarPorPeriodo(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        return ResponseEntity.ok(facturaRecibidaService.listarPorPeriodo(fechaInicio, fechaFin));
    }

    @GetMapping("/mes/{year}/{month}")
    public ResponseEntity<List<FacturaRecibidaDTO>> listarPorMes(
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(facturaRecibidaService.listarPorMes(year, month));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<FacturaRecibidaDTO>> buscar(@RequestParam String termino) {
        return ResponseEntity.ok(facturaRecibidaService.buscar(termino));
    }

    @PostMapping
    public ResponseEntity<FacturaRecibidaDTO> crear(@RequestBody FacturaRecibidaDTO dto) {
        return ResponseEntity.ok(facturaRecibidaService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacturaRecibidaDTO> actualizar(
            @PathVariable Long id,
            @RequestBody FacturaRecibidaDTO dto) {
        return ResponseEntity.ok(facturaRecibidaService.actualizar(id, dto));
    }

    @PutMapping("/{id}/pagar")
    public ResponseEntity<FacturaRecibidaDTO> marcarPagada(
            @PathVariable Long id,
            @RequestParam(required = false) String metodoPago) {
        return ResponseEntity.ok(facturaRecibidaService.marcarPagada(id, metodoPago));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        facturaRecibidaService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Factura eliminada correctamente"));
    }

    // Endpoints de resumen
    @GetMapping("/resumen/total")
    public ResponseEntity<Map<String, BigDecimal>> totalPorPeriodo(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        BigDecimal total = facturaRecibidaService.totalPorPeriodo(fechaInicio, fechaFin);
        BigDecimal iva = facturaRecibidaService.totalIvaSoportado(fechaInicio, fechaFin);
        return ResponseEntity.ok(Map.of("total", total, "ivaSoportado", iva));
    }

    @GetMapping("/resumen/categorias")
    public ResponseEntity<List<Object[]>> resumenPorCategoria(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        return ResponseEntity.ok(facturaRecibidaService.resumenPorCategoria(fechaInicio, fechaFin));
    }

    @GetMapping("/resumen/proveedores")
    public ResponseEntity<List<Object[]>> resumenPorProveedor(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        return ResponseEntity.ok(facturaRecibidaService.resumenPorProveedor(fechaInicio, fechaFin));
    }
}

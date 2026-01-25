package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.GastoDTO;
import com.lavaderosepulveda.app.service.GastoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gastos")
public class GastoApiController {

    @Autowired
    private GastoService gastoService;

    @GetMapping
    public ResponseEntity<List<GastoDTO>> listar() {
        return ResponseEntity.ok(gastoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GastoDTO> obtener(@PathVariable Long id) {
        GastoDTO gasto = gastoService.obtenerPorId(id);
        if (gasto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(gasto);
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<GastoDTO>> listarPorCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(gastoService.listarPorCategoria(categoria));
    }

    @GetMapping("/recurrentes")
    public ResponseEntity<List<GastoDTO>> listarRecurrentes() {
        return ResponseEntity.ok(gastoService.listarRecurrentes());
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<GastoDTO>> listarPendientesPago() {
        return ResponseEntity.ok(gastoService.listarPendientesPago());
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<GastoDTO>> listarPorPeriodo(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        return ResponseEntity.ok(gastoService.listarPorPeriodo(fechaInicio, fechaFin));
    }

    @GetMapping("/mes/{year}/{month}")
    public ResponseEntity<List<GastoDTO>> listarPorMes(
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(gastoService.listarPorMes(year, month));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<GastoDTO>> buscar(@RequestParam String termino) {
        return ResponseEntity.ok(gastoService.buscar(termino));
    }

    @PostMapping
    public ResponseEntity<GastoDTO> crear(@RequestBody GastoDTO dto) {
        return ResponseEntity.ok(gastoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GastoDTO> actualizar(@PathVariable Long id, @RequestBody GastoDTO dto) {
        return ResponseEntity.ok(gastoService.actualizar(id, dto));
    }

    @PutMapping("/{id}/pagar")
    public ResponseEntity<GastoDTO> marcarPagado(
            @PathVariable Long id,
            @RequestParam(required = false) String metodoPago) {
        return ResponseEntity.ok(gastoService.marcarPagado(id, metodoPago));
    }

    @PostMapping("/generar-recurrentes")
    public ResponseEntity<List<GastoDTO>> generarRecurrentes() {
        return ResponseEntity.ok(gastoService.generarRecurrentesMes());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        gastoService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Gasto eliminado correctamente"));
    }

    // Endpoints de resumen
    @GetMapping("/resumen/total")
    public ResponseEntity<Map<String, BigDecimal>> totalPorPeriodo(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        BigDecimal total = gastoService.totalPorPeriodo(fechaInicio, fechaFin);
        BigDecimal iva = gastoService.totalIvaSoportado(fechaInicio, fechaFin);
        return ResponseEntity.ok(Map.of("total", total, "ivaSoportado", iva));
    }

    @GetMapping("/resumen/categorias")
    public ResponseEntity<List<Object[]>> resumenPorCategoria(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        return ResponseEntity.ok(gastoService.resumenPorCategoria(fechaInicio, fechaFin));
    }

    @GetMapping("/resumen/evolucion")
    public ResponseEntity<List<Object[]>> evolucionMensual(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        return ResponseEntity.ok(gastoService.evolucionMensual(fechaInicio, fechaFin));
    }
}

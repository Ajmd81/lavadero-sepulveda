package com.lavaderosepulveda.app.fiscal.controller;

import com.lavaderosepulveda.app.dto.DeclaracionFiscalDTO;
import com.lavaderosepulveda.app.fiscal.service.DeclaracionFiscalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el historial de declaraciones fiscales.
 *
 * Endpoints:
 *   GET  /api/fiscal/declaraciones                        → todo el historial
 *   GET  /api/fiscal/declaraciones?modelo=303             → filtrar por modelo
 *   GET  /api/fiscal/declaraciones/pendientes             → solo GENERADO (no presentados)
 *   PUT  /api/fiscal/declaraciones/{id}/presentado        → marcar como presentado
 *   PUT  /api/fiscal/declaraciones/{id}/revertir          → revertir a GENERADO
 *   DELETE /api/fiscal/declaraciones/{id}                 → eliminar del historial
 */
@RestController
@RequestMapping("/api/fiscal/declaraciones")
@CrossOrigin(origins = "*")
public class DeclaracionFiscalController {

    @Autowired
    private DeclaracionFiscalService declaracionFiscalService;

    // ─────────────────────────────────────────────────────────────────────────
    // Consultas
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/fiscal/declaraciones
     * GET /api/fiscal/declaraciones?modelo=303
     * GET /api/fiscal/declaraciones?modelo=130
     */
    @GetMapping
    public ResponseEntity<List<DeclaracionFiscalDTO>> listar(
            @RequestParam(required = false) String modelo) {

        List<DeclaracionFiscalDTO> lista = (modelo != null && !modelo.isBlank())
                ? declaracionFiscalService.listarPorModelo(modelo)
                : declaracionFiscalService.listarTodas();

        return ResponseEntity.ok(lista);
    }

    /**
     * GET /api/fiscal/declaraciones/pendientes
     * Declaraciones generadas pero aún no presentadas ante la AEAT.
     */
    @GetMapping("/pendientes")
    public ResponseEntity<List<DeclaracionFiscalDTO>> listarPendientes() {
        return ResponseEntity.ok(declaracionFiscalService.listarGeneradas());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cambio de estado
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * PUT /api/fiscal/declaraciones/{id}/presentado
     * Body (opcional): { "fechaPresentacion": "dd/MM/yyyy" }
     * Si no se envía fecha se usa la fecha actual.
     */
    @PutMapping("/{id}/presentado")
    public ResponseEntity<DeclaracionFiscalDTO> marcarComoPresentado(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {

        LocalDate fecha = null;
        if (body != null && body.containsKey("fechaPresentacion")) {
            try {
                fecha = LocalDate.parse(body.get("fechaPresentacion"),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception ignored) { /* si el formato falla, usamos hoy */ }
        }

        DeclaracionFiscalDTO dto = declaracionFiscalService.marcarComoPresentado(id, fecha);
        return ResponseEntity.ok(dto);
    }

    /**
     * PUT /api/fiscal/declaraciones/{id}/revertir
     * Devuelve la declaración a estado GENERADO (por si se marcó por error).
     */
    @PutMapping("/{id}/revertir")
    public ResponseEntity<DeclaracionFiscalDTO> revertirPresentacion(@PathVariable Long id) {
        return ResponseEntity.ok(declaracionFiscalService.revertirPresentacion(id));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Eliminación
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * DELETE /api/fiscal/declaraciones/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Long id) {
        declaracionFiscalService.eliminar(id);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Declaración eliminada del historial",
                "id", id
        ));
    }
}

package com.lavaderosepulveda.app.controller.admin;

import com.lavaderosepulveda.app.model.cita.DiaCerrado;
import com.lavaderosepulveda.app.repository.cita.DiaCerradoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dias-cerrados")
public class DiaCerradoController {

    @Autowired
    private DiaCerradoRepository diasCerradoRepository;

    // ── CRM Admin ─────────────────────────────────────────────────────

    /** Todos los días cerrados en un rango (para el calendario del CRM) */
    @GetMapping("/rango")
    public ResponseEntity<List<DiaCerrado>> getPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        if (!rangoValido(inicio, fin)) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(diasCerradoRepository.findByRango(inicio, fin));
    }

    /** Marcar un día como cerrado */
    @PostMapping
    public ResponseEntity<?> marcar(@jakarta.validation.Valid @RequestBody DiaCerrado diaCerrado) {
        diaCerrado.setId(null); // nunca sobrescribir un registro existente
        if (diaCerrado.getTipo() == null || diaCerrado.getTipo().isBlank()) diaCerrado.setTipo("FESTIVO");
        if (diasCerradoRepository.existsByFecha(diaCerrado.getFecha())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El día " + diaCerrado.getFecha() + " ya está marcado como cerrado"));
        }
        DiaCerrado guardado = diasCerradoRepository.save(diaCerrado);
        return ResponseEntity.ok(guardado);
    }

    /** Desmarcar (reabrir) un día cerrado por su fecha */
    @DeleteMapping("/fecha/{fecha}")
    public ResponseEntity<?> desmarcarPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return diasCerradoRepository.findByFecha(fecha)
                .map(d -> {
                    diasCerradoRepository.delete(d);
                    return ResponseEntity.ok(Map.of("mensaje", "Día reabierto correctamente"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** Desmarcar por ID */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> desmarcarPorId(@PathVariable Long id) {
        if (!diasCerradoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        diasCerradoRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Día reabierto correctamente"));
    }

    // ── Web pública de reservas ───────────────────────────────────────

    /**
     * Devuelve solo las fechas cerradas en formato ISO (yyyy-MM-dd).
     * Usado por el formulario público de reservas para deshabilitar fechas.
     */
    @GetMapping("/fechas-cerradas")
    public ResponseEntity<List<String>> getFechasCerradas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        if (!rangoValido(inicio, fin)) return ResponseEntity.badRequest().build();
        List<String> fechas = diasCerradoRepository.findFechasByRango(inicio, fin)
                .stream()
                .map(LocalDate::toString)
                .collect(Collectors.toList());
        return ResponseEntity.ok(fechas);
    }

    /** Rango coherente y acotado (máx. ~13 meses) para evitar consultas abusivas desde la API pública. */
    private boolean rangoValido(LocalDate inicio, LocalDate fin) {
        return !fin.isBefore(inicio) && !fin.isAfter(inicio.plusDays(400));
    }
}

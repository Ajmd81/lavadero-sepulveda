package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.model.HorarioDiaSemana;
import com.lavaderosepulveda.app.model.enums.DiaSemana;
import com.lavaderosepulveda.app.service.HorarioDiaSemanaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Controller para gestionar horarios del lavadero por día de la semana
 * 
 * Endpoints:
 * - GET  /api/horarios                    → Obtener todos los horarios
 * - GET  /api/horarios/{diaSemana}        → Obtener horario de un día
 * - PUT  /api/horarios/{diaSemana}        → Actualizar horario (admin only)
 * - PUT  /api/horarios/bulk               → Actualizar múltiples (admin only)
 * 
 * Acceso:
 * - GET: Cualquier usuario autenticado
 * - PUT: Solo ADMIN
 */
@RestController
@RequestMapping("/api/horarios")
public class HorarioDiaSemanaController {

    private static final Logger logger = LoggerFactory.getLogger(HorarioDiaSemanaController.class);

    @Autowired
    private HorarioDiaSemanaService service;

    /**
     * GET /api/horarios
     * Obtiene todos los horarios de la semana
     */
    @GetMapping
    public ResponseEntity<List<HorarioDiaSemana>> obtenerTodos() {
        logger.info("GET /api/horarios - Obteniendo todos los horarios");
        List<HorarioDiaSemana> horarios = service.obtenerTodos();
        return ResponseEntity.ok(horarios);
    }

    /**
     * GET /api/horarios/{diaSemana}
     * Obtiene el horario de un día específico
     * Ejemplo: /api/horarios/LUNES
     */
    @GetMapping("/{diaSemana}")
    public ResponseEntity<HorarioDiaSemana> obtenerPorDia(@PathVariable DiaSemana diaSemana) {
        logger.info("GET /api/horarios/{} - Obteniendo horario", diaSemana);

        return service.obtenerPorDia(diaSemana)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/horarios/{diaSemana}
     * Actualiza el horario de un día específico
     * Acceso: Solo ADMIN
     */
    @PutMapping("/{diaSemana}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HorarioDiaSemana> actualizarDia(
            @PathVariable DiaSemana diaSemana,
            @Valid @RequestBody HorarioDiaSemana horario) {

        logger.info("PUT /api/horarios/{} - Actualizando horario", diaSemana);

        try {
            HorarioDiaSemana actualizado = service.actualizarHorarioDia(diaSemana, horario);
            logger.info("Horario actualizado para {}", diaSemana);
            return ResponseEntity.ok(actualizado);

        } catch (IllegalArgumentException e) {
            logger.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error al actualizar horario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/horarios/bulk
     * Actualiza múltiples horarios a la vez
     * Acceso: Solo ADMIN
     */
    @PutMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<HorarioDiaSemana>> actualizarMultiples(
            @Valid @RequestBody List<HorarioDiaSemana> horarios) {

        logger.info("PUT /api/horarios/bulk - Actualizando {} horarios", horarios.size());

        try {
            List<HorarioDiaSemana> actualizados = service.actualizarTodos(horarios);
            logger.info("Actualizados {} horarios", actualizados.size());
            return ResponseEntity.ok(actualizados);

        } catch (IllegalArgumentException e) {
            logger.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error al actualizar horarios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint de health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Horarios Service OK");
    }
}

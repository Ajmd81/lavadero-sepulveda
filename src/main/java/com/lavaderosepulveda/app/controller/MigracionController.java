package com.lavaderosepulveda.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller para migraciones de base de datos
 * USAR CON CUIDADO - Solo para administración
 */
@RestController
@RequestMapping("/api/admin/migracion")
public class MigracionController {

    private static final Logger log = LoggerFactory.getLogger(MigracionController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * POST /api/admin/migracion/telefono-nullable
     * Permitir que la columna telefono sea NULL
     */
    @PostMapping("/telefono-nullable")
    public ResponseEntity<?> hacerTelefonoNullable() {
        try {
            log.info("Ejecutando migración: telefono nullable...");

            // Modificar columna telefono para permitir NULL
            jdbcTemplate.execute("ALTER TABLE clientes MODIFY COLUMN telefono VARCHAR(255) NULL");

            // Quitar índice único si existe
            try {
                jdbcTemplate.execute("ALTER TABLE clientes DROP INDEX UK_clientes_telefono");
            } catch (Exception e) {
                log.info("Índice UK_clientes_telefono no existe o ya fue eliminado");
            }

            try {
                jdbcTemplate.execute("ALTER TABLE clientes DROP INDEX idx_clientes_telefono");
            } catch (Exception e) {
                log.info("Índice idx_clientes_telefono no existe o ya fue eliminado");
            }

            log.info("Migración completada: telefono ahora puede ser NULL");

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Migración completada",
                    "detalle", "La columna telefono ahora permite valores NULL"
            ));

        } catch (Exception e) {
            log.error("Error en migración: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Error en migración",
                    "detalle", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/admin/migracion/verificar-clientes
     * Verificar estructura de la tabla clientes
     */
    @GetMapping("/verificar-clientes")
    public ResponseEntity<?> verificarEstructuraClientes() {
        try {
            var resultado = jdbcTemplate.queryForList("DESCRIBE clientes");
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}
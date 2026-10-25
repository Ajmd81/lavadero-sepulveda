package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.model.ConfiguracionHorario;
import com.lavaderosepulveda.app.service.ConfiguracionHorarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Controller para gestionar la configuración de horarios del lavadero
 * 
 * Endpoints:
 * - GET  /api/configuracion-horario       → Obtener configuración actual
 * - PUT  /api/configuracion-horario       → Actualizar configuración (admin only)
 * 
 * Acceso: Solo usuarios autenticados con rol ADMIN
 */
@RestController
@RequestMapping("/api/configuracion-horario")
public class ConfiguracionHorarioController {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguracionHorarioController.class);

    @Autowired
    private ConfiguracionHorarioService service;

    /**
     * GET /api/configuracion-horario
     * 
     * Obtiene la configuración actual de horarios
     * Acceso: Cualquier usuario autenticado (público en CRM)
     * 
     * @return ConfiguracionHorario actual
     */
    @GetMapping
    public ResponseEntity<ConfiguracionHorario> obtenerConfiguracion() {
        logger.info("GET /api/configuracion-horario - Obteniendo configuración");
        
        ConfiguracionHorario config = service.obtenerConfiguracion();
        return ResponseEntity.ok(config);
    }

    /**
     * PUT /api/configuracion-horario
     * 
     * Actualiza la configuración de horarios
     * Acceso: Solo ADMIN
     * 
     * @param config Nueva configuración
     * @return ConfiguracionHorario actualizada
     * @throws IllegalArgumentException si la validación falla
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfiguracionHorario> actualizarConfiguracion(
            @Valid @RequestBody ConfiguracionHorario config) {

        logger.info("PUT /api/configuracion-horario - Actualizando configuración: {}", config);

        try {
            ConfiguracionHorario actualizada = service.actualizarConfiguracion(config);
            logger.info("Configuración actualizada exitosamente");
            return ResponseEntity.ok(actualizada);

        } catch (IllegalArgumentException e) {
            logger.error("Error de validación en configuración: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    /**
     * Endpoint de health check (público)
     * 
     * @return Estado del servicio
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Configuración Horario Service OK");
    }
}
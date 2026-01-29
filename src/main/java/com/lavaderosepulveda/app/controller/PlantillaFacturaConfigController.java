package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.model.PlantillaFacturaConfig;
import com.lavaderosepulveda.app.repository.PlantillaFacturaConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para gestionar la configuración de plantilla de facturas
 */
@RestController
@RequestMapping("/api/config/plantilla-factura")
public class PlantillaFacturaConfigController {

    private static final Logger log = LoggerFactory.getLogger(PlantillaFacturaConfigController.class);

    @Autowired
    private PlantillaFacturaConfigRepository configRepository;

    /**
     * GET /api/config/plantilla-factura
     * Obtener configuración actual
     */
    @GetMapping
    public ResponseEntity<PlantillaFacturaConfig> obtenerConfiguracion() {
        PlantillaFacturaConfig config = configRepository.findById(1L)
                .orElseGet(() -> {
                    // Crear configuración por defecto si no existe
                    PlantillaFacturaConfig nuevaConfig = new PlantillaFacturaConfig();
                    nuevaConfig.setId(1L);
                    return configRepository.save(nuevaConfig);
                });
        
        return ResponseEntity.ok(config);
    }

    /**
     * POST /api/config/plantilla-factura
     * Guardar/actualizar configuración
     */
    @PostMapping
    public ResponseEntity<PlantillaFacturaConfig> guardarConfiguracion(@RequestBody PlantillaFacturaConfig config) {
        try {
            // Siempre usar ID = 1 (solo hay una configuración)
            config.setId(1L);
            
            PlantillaFacturaConfig configGuardada = configRepository.save(config);
            log.info("Configuración de plantilla guardada");
            
            return ResponseEntity.ok(configGuardada);
            
        } catch (Exception e) {
            log.error("Error guardando configuración de plantilla", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * PUT /api/config/plantilla-factura
     * Actualizar configuración (alias de POST)
     */
    @PutMapping
    public ResponseEntity<PlantillaFacturaConfig> actualizarConfiguracion(@RequestBody PlantillaFacturaConfig config) {
        return guardarConfiguracion(config);
    }

    /**
     * DELETE /api/config/plantilla-factura
     * Restablecer configuración por defecto
     */
    @DeleteMapping
    public ResponseEntity<PlantillaFacturaConfig> restablecerConfiguracion() {
        try {
            // Eliminar configuración actual
            configRepository.deleteById(1L);
            
            // Crear nueva con valores por defecto
            PlantillaFacturaConfig config = new PlantillaFacturaConfig();
            config.setId(1L);
            PlantillaFacturaConfig configGuardada = configRepository.save(config);
            
            log.info("Configuración de plantilla restablecida a valores por defecto");
            
            return ResponseEntity.ok(configGuardada);
            
        } catch (Exception e) {
            log.error("Error restableciendo configuración de plantilla", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

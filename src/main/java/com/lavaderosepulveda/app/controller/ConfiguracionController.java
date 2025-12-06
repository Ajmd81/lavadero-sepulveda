package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.service.HorarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador refactorizado para obtener configuración de la aplicación
 * Usa HorarioService que centraliza la configuración de horarios
 */
@RestController
@RequestMapping("/api")
public class ConfiguracionController {

    @Autowired
    private HorarioService horarioService;

    /**
     * Obtiene la configuración completa de horarios desde HorarioService
     * Elimina la duplicación de configuración hardcodeada
     */
    @GetMapping("/configuracion")
    public ResponseEntity<Map<String, Object>> obtenerConfiguracion() {
        // Delegamos la lógica al servicio especializado que usa HorariosConfig
        Map<String, Object> configuracion = horarioService.obtenerConfiguracionHorarios();

        return ResponseEntity.ok(configuracion);
    }

    /**
     * Endpoint adicional para validar que la configuración sea correcta
     */
    @GetMapping("/configuracion/validar")
    public ResponseEntity<Map<String, Object>> validarConfiguracion() {
        boolean esValida = horarioService.validarConfiguracion();

        Map<String, Object> respuesta = Map.of(
                "configuracionValida", esValida,
                "mensaje", esValida ? "Configuración de horarios válida" : "Configuración de horarios inválida"
        );

        return ResponseEntity.ok(respuesta);
    }
}
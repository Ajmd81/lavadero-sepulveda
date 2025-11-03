package com.lavaderosepulveda.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ConfiguracionController {

    @GetMapping("/configuracion")
    public ResponseEntity<Map<String, Object>> obtenerConfiguracion() {
        Map<String, Object> configuracion = new HashMap<>();

        // Configuración de horarios
        configuracion.put("horaAperturaMañana", 8);
        configuracion.put("horaCierreMañana", 14);
        configuracion.put("horaAperturaTarde", 17);
        configuracion.put("horaCierreTarde", 20);
        configuracion.put("intervaloMinutos", 60);

        return ResponseEntity.ok(configuracion);
    }
}
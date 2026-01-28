package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.Modelo130DTO;
import com.lavaderosepulveda.app.dto.Modelo303DTO;
import com.lavaderosepulveda.app.service.ModelosFiscalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/modelos-fiscales")
public class ModelosFiscalesApiController {

    @Autowired
    private ModelosFiscalesService modelosFiscalesService;

    @GetMapping("/303")
    public ResponseEntity<Modelo303DTO> getModelo303(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer trimestre) {

        // Defaults
        if (anio == null)
            anio = java.time.LocalDate.now().getYear();
        if (trimestre == null) {
            int mes = java.time.LocalDate.now().getMonthValue();
            trimestre = (mes - 1) / 3 + 1;
        }

        Modelo303DTO dto = modelosFiscalesService.generarModelo303(anio, trimestre);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/130")
    public ResponseEntity<Modelo130DTO> getModelo130(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer trimestre) {

        // Defaults
        if (anio == null)
            anio = java.time.LocalDate.now().getYear();
        if (trimestre == null) {
            int mes = java.time.LocalDate.now().getMonthValue();
            trimestre = (mes - 1) / 3 + 1;
        }

        Modelo130DTO dto = modelosFiscalesService.generarModelo130(anio, trimestre);
        return ResponseEntity.ok(dto);
    }
}

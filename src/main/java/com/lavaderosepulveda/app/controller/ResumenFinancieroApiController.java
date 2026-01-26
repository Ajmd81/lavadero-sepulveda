package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.ResumenFinancieroDTO;
import com.lavaderosepulveda.app.service.ResumenFinancieroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/resumen-financiero")
public class ResumenFinancieroApiController {

    @Autowired
    private ResumenFinancieroService service;

    @GetMapping
    public ResponseEntity<ResumenFinancieroDTO> obtenerResumen(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        
        ResumenFinancieroDTO resumen = service.generarResumen(desde, hasta);
        return ResponseEntity.ok(resumen);
    }
}

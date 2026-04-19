package com.lavaderosepulveda.app.fiscal.controller;

import com.lavaderosepulveda.app.fiscal.service.DeclaracionFiscalService;
import com.lavaderosepulveda.app.fiscal.service.Modelo130BoeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@RestController
@RequestMapping("/api/fiscal/modelo130")
@CrossOrigin(origins = "*")
public class Modelo130BoeController {

    @Autowired
    private Modelo130BoeService modelo130BoeService;

    @Autowired
    private DeclaracionFiscalService declaracionFiscalService;

    @GetMapping("/{ejercicio}/{periodo}/exportar-boe")
    public ResponseEntity<byte[]> exportarBoeSimple(
            @PathVariable int ejercicio,
            @PathVariable String periodo,
            @RequestParam String nif,
            @RequestParam String nombre,
            @RequestParam(defaultValue = "0") BigDecimal ingresosTrimestre,
            @RequestParam(defaultValue = "0") BigDecimal gastosTrimestre,
            @RequestParam(defaultValue = "0") BigDecimal retencionesComputadas,
            @RequestParam(defaultValue = "0") BigDecimal pagosAnteriores
    ) throws IOException {

        byte[] contenido = modelo130BoeService.generarFicheroBoe(
                nif, nombre, ejercicio, periodo,
                ingresosTrimestre, gastosTrimestre,
                retencionesComputadas, pagosAnteriores
        );

        String nombreFichero = nif.toUpperCase() + ejercicio + periodo.toUpperCase() + ".130";

        BigDecimal rendimientoNeto = ingresosTrimestre.subtract(gastosTrimestre);
        if (rendimientoNeto.compareTo(BigDecimal.ZERO) < 0) rendimientoNeto = BigDecimal.ZERO;
        BigDecimal pagoFraccionado = rendimientoNeto
                .multiply(new BigDecimal("0.20"))
                .setScale(2, RoundingMode.HALF_UP);

        declaracionFiscalService.registrarGeneracion130(
                ejercicio, periodo,
                ingresosTrimestre, gastosTrimestre,
                rendimientoNeto, pagoFraccionado,
                nombreFichero
        );

        return descargar(contenido, nombreFichero);
    }

    @PostMapping("/exportar-boe")
    public ResponseEntity<byte[]> exportarBoeCompleto(
            @RequestBody Modelo130BoeService.Modelo130Datos datos
    ) throws IOException {

        byte[] contenido = modelo130BoeService.generarFicheroBoeCompleto(datos);

        String nombreFichero = datos.nif.toUpperCase() + datos.ejercicio + datos.periodo.toUpperCase() + ".130";

        BigDecimal rendimientoNeto = orCero(datos.ingresosTrimestre).subtract(orCero(datos.gastosTrimestre));
        if (rendimientoNeto.compareTo(BigDecimal.ZERO) < 0) rendimientoNeto = BigDecimal.ZERO;
        BigDecimal pagoFraccionado = rendimientoNeto
                .multiply(new BigDecimal("0.20"))
                .setScale(2, RoundingMode.HALF_UP);

        declaracionFiscalService.registrarGeneracion130(
                datos.ejercicio, datos.periodo,
                datos.ingresosTrimestre, datos.gastosTrimestre,
                rendimientoNeto, pagoFraccionado,
                nombreFichero
        );

        return descargar(contenido, nombreFichero);
    }

    private ResponseEntity<byte[]> descargar(byte[] contenido, String nombreFichero) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreFichero + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(contenido);
    }

    private BigDecimal orCero(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }
}

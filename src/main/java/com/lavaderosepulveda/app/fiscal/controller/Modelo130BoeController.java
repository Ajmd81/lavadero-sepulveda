package com.lavaderosepulveda.app.fiscal.controller;

import com.lavaderosepulveda.app.fiscal.service.DeclaracionFiscalService;

import com.lavaderosepulveda.app.fiscal.service.Modelo130BoeService;

import com.lavaderosepulveda.app.fiscal.service.Modelo130BoeService.Resultado130;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Exportación del Modelo 130.
 *
 *   GET  /api/fiscal/modelo130/{ejercicio}/{periodo}/exportar-boe
 *   POST /api/fiscal/modelo130/exportar-boe
 *
 * Ingresos y gastos son ACUMULADOS desde el 1 de enero.
 * Si no se envía {@code pagosAnteriores}, se calcula con el historial de 130 del mismo ejercicio.
 */
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
            @RequestParam(required = false) String apellidos,
            @RequestParam(defaultValue = "0") BigDecimal ingresosTrimestre,
            @RequestParam(defaultValue = "0") BigDecimal gastosTrimestre,
            @RequestParam(defaultValue = "0") BigDecimal retencionesComputadas,
            @RequestParam(required = false) BigDecimal pagosAnteriores
    ) {
        BigDecimal pagos = pagosAnteriores != null
                ? pagosAnteriores
                : declaracionFiscalService.sumarPagosFraccionadosAnteriores(ejercicio, periodo);

        Resultado130 r = modelo130BoeService.generar(
                nif, nombre, apellidos, ejercicio, periodo,
                ingresosTrimestre, gastosTrimestre, retencionesComputadas, pagos
        );

        declaracionFiscalService.registrarGeneracion130(
                ejercicio, periodo,
                ingresosTrimestre, gastosTrimestre,
                r.rendimientoNeto(), r.pagoFraccionado(),
                r.nombreFichero()
        );

        return descargar(r.contenido(), r.nombreFichero());
    }

    @PostMapping("/exportar-boe")
    public ResponseEntity<byte[]> exportarBoeCompleto(
            @RequestBody Modelo130BoeService.Modelo130Datos datos
    ) {
        BigDecimal pagos = datos.pagosAnteriores != null
                ? datos.pagosAnteriores
                : declaracionFiscalService.sumarPagosFraccionadosAnteriores(datos.ejercicio, datos.periodo);

        Resultado130 r = modelo130BoeService.generarCompleto(datos, pagos);

        declaracionFiscalService.registrarGeneracion130(
                datos.ejercicio, datos.periodo,
                datos.ingresosTrimestre, datos.gastosTrimestre,
                r.rendimientoNeto(), r.pagoFraccionado(),
                r.nombreFichero()
        );

        return descargar(r.contenido(), r.nombreFichero());
    }

    private ResponseEntity<byte[]> descargar(byte[] contenido, String nombreFichero) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreFichero + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(contenido.length)
                .body(contenido);
    }
}
package com.lavaderosepulveda.app.fiscal.controller;

import com.lavaderosepulveda.app.fiscal.service.DeclaracionFiscalService;

import com.lavaderosepulveda.app.fiscal.service.Modelo303BoeService;

import com.lavaderosepulveda.app.fiscal.service.Modelo303BoeService.Resultado303;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Exportación del Modelo 303.
 *
 *   GET  /api/fiscal/modelo303/{ejercicio}/{periodo}/exportar-boe   (solo 21 %)
 *   POST /api/fiscal/modelo303/exportar-boe                         (todos los tipos)
 *
 * La cabecera X-Cuotas-Pendientes-Compensar de la respuesta indica el saldo
 * a compensar que queda para el siguiente trimestre.
 */
@RestController
@RequestMapping("/api/fiscal/modelo303")
public class Modelo303BoeController {

    @Autowired
    private Modelo303BoeService modelo303BoeService;

    @Autowired
    private DeclaracionFiscalService declaracionFiscalService;

    @GetMapping("/{ejercicio}/{periodo}/exportar-boe")
    public ResponseEntity<byte[]> exportarBoeSimple(
            @PathVariable int ejercicio,
            @PathVariable String periodo,
            @RequestParam String nif,
            @RequestParam String nombre,
            @RequestParam(required = false) String apellidos,
            @RequestParam(defaultValue = "0") BigDecimal baseRep21,
            @RequestParam(required = false) BigDecimal cuotaRep21,
            @RequestParam(defaultValue = "0") BigDecimal baseSop21,
            @RequestParam(defaultValue = "0") BigDecimal cuotaSop21,
            @RequestParam(defaultValue = "0") BigDecimal cuotasCompensarPendientes,
            @RequestParam(required = false) String iban
    ) {
        Resultado303 r = modelo303BoeService.generar(
                nif, nombre, apellidos, ejercicio, periodo,
                baseRep21, cuotaRep21, baseSop21, cuotaSop21,
                cuotasCompensarPendientes, iban
        );

        BigDecimal cuotaRep = cuotaRep21 != null ? cuotaRep21 : baseRep21.multiply(new BigDecimal("0.21"));

        declaracionFiscalService.registrarGeneracion303(
                ejercicio, periodo,
                baseRep21, cuotaRep, baseSop21, cuotaSop21,
                r.resultado(),
                r.nombreFichero()
        );

        return descargar(r);
    }

    @PostMapping("/exportar-boe")
    public ResponseEntity<byte[]> exportarBoeCompleto(
            @jakarta.validation.Valid @RequestBody Modelo303BoeService.Modelo303Datos datos
    ) {
        Resultado303 r = modelo303BoeService.generarCompleto(datos);

        BigDecimal baseRepTotal  = orCero(datos.baseRep4).add(orCero(datos.baseRep10)).add(orCero(datos.baseRep21));
        BigDecimal cuotaRepTotal = orCero(datos.cuotaRep4).add(orCero(datos.cuotaRep10)).add(orCero(datos.cuotaRep21));
        BigDecimal baseSopTotal  = orCero(datos.baseSop4).add(orCero(datos.baseSop10)).add(orCero(datos.baseSop21));
        BigDecimal cuotaSopTotal = orCero(datos.cuotaSop4).add(orCero(datos.cuotaSop10)).add(orCero(datos.cuotaSop21));

        declaracionFiscalService.registrarGeneracion303(
                datos.ejercicio, datos.periodo,
                baseRepTotal, cuotaRepTotal, baseSopTotal, cuotaSopTotal,
                r.resultado(),
                r.nombreFichero()
        );

        return descargar(r);
    }

    private ResponseEntity<byte[]> descargar(Resultado303 r) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + r.nombreFichero() + "\"")
                .header("X-Tipo-Declaracion", r.tipoDeclaracion())
                .header("X-Resultado", r.resultado().toPlainString())
                .header("X-Cuotas-Pendientes-Compensar", r.cuotasPendientesCompensar().toPlainString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(r.contenido().length)
                .body(r.contenido());
    }

    private BigDecimal orCero(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }
}
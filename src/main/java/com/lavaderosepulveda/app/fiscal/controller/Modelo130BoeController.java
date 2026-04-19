package com.lavaderosepulveda.app.fiscal.controller;

import com.lavaderosepulveda.app.fiscal.service.Modelo130BoeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Controlador REST para exportar el Modelo 130 (IRPF — Pago fraccionado)
 * en formato BOE importable en la AEAT.
 *
 * Endpoints:
 *   GET  /api/fiscal/modelo130/{ejercicio}/{periodo}/exportar-boe   → caso habitual
 *   POST /api/fiscal/modelo130/exportar-boe                         → caso completo con body JSON
 *
 * El fichero descargado recibe el nombre estándar AEAT: {NIF}{EJERCICIO}{PERIODO}.130
 * Ejemplo: 12345678A20261T.130
 */
@RestController
@RequestMapping("/api/fiscal/modelo130")
@CrossOrigin(origins = "*")
public class Modelo130BoeController {

    @Autowired
    private Modelo130BoeService modelo130BoeService;

    // ─────────────────────────────────────────────────────────────────────────
    // GET — caso habitual de Lavadero Sepúlveda
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Exporta el Modelo 130 a partir de ingresos y gastos del trimestre.
     * El servicio calcula automáticamente el 20% del rendimiento neto.
     *
     * Ejemplo de llamada desde React:
     *   GET /api/fiscal/modelo130/2026/1T/exportar-boe
     *       ?nif=12345678A
     *       &nombre=SEPULVEDA GARCIA FRANCISCO
     *       &ingresosTrimestre=5000.00
     *       &gastosTrimestre=1800.00
     *       &retencionesComputadas=0
     *       &pagosAnteriores=0
     *
     * Casillas calculadas automáticamente:
     *   01 = ingresosTrimestre
     *   02 = gastosTrimestre
     *   03 = 01 - 02  (rendimiento neto)
     *   05 = max(03, 0)
     *   07 = 05 × 20%  (pago fraccionado bruto)
     *   11 = retencionesComputadas
     *   13 = pagosAnteriores
     *   14 = max(07 - 11 - 13, 0)  (resultado a ingresar)
     */
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

        return descargar(contenido, nif, ejercicio, periodo);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST completo — con DTO JSON
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Exporta el Modelo 130 a partir de un DTO JSON completo.
     *
     * Body JSON:
     * {
     *   "nif": "12345678A",
     *   "nombre": "SEPULVEDA GARCIA FRANCISCO",
     *   "ejercicio": 2026,
     *   "periodo": "1T",
     *   "ingresosTrimestre": 5000.00,
     *   "gastosTrimestre": 1800.00,
     *   "retencionesComputadas": 0,
     *   "pagosAnteriores": 0
     * }
     */
    @PostMapping("/exportar-boe")
    public ResponseEntity<byte[]> exportarBoeCompleto(
            @RequestBody Modelo130BoeService.Modelo130Datos datos
    ) throws IOException {

        byte[] contenido = modelo130BoeService.generarFicheroBoeCompleto(datos);
        return descargar(contenido, datos.nif, datos.ejercicio, datos.periodo);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    private ResponseEntity<byte[]> descargar(byte[] contenido, String nif,
                                              int ejercicio, String periodo) {
        String nombreFichero = nif.toUpperCase() + ejercicio + periodo.toUpperCase() + ".130";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreFichero + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(contenido);
    }
}

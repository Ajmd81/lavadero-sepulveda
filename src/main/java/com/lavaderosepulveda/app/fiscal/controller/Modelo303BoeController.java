package com.lavaderosepulveda.app.fiscal.controller;

import com.lavaderosepulveda.app.fiscal.service.Modelo303BoeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Controlador REST para exportar el Modelo 303 (IVA) en formato BOE importable en la AEAT.
 *
 * Endpoints:
 *   GET  /api/fiscal/modelo303/{ejercicio}/{periodo}/exportar-boe   → caso simple (solo 21%)
 *   POST /api/fiscal/modelo303/exportar-boe                         → caso completo con body JSON
 *
 * El fichero descargado recibe el nombre estándar AEAT: {NIF}{EJERCICIO}{PERIODO}.303
 * Ejemplo: 12345678A20261T.303
 */
@RestController
@RequestMapping("/api/fiscal/modelo303")
@CrossOrigin(origins = "*")
public class Modelo303BoeController {

    @Autowired
    private Modelo303BoeService modelo303BoeService;

    // ─────────────────────────────────────────────────────────────────────────
    // GET simple — solo tipo 21% (caso Lavadero Sepúlveda)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Exporta el Modelo 303 para el caso habitual: régimen general, tipo único 21%.
     *
     * Ejemplo de llamada desde React:
     *   GET /api/fiscal/modelo303/2026/1T/exportar-boe
     *       ?nif=12345678A
     *       &nombre=SEPULVEDA GARCIA FRANCISCO
     *       &baseRep21=3813.06
     *       &cuotaRep21=800.86
     *       &baseSop21=2513.07
     *       &cuotaSop21=240.71
     */
    @GetMapping("/{ejercicio}/{periodo}/exportar-boe")
    public ResponseEntity<byte[]> exportarBoeSimple(
            @PathVariable int ejercicio,
            @PathVariable String periodo,
            @RequestParam String nif,
            @RequestParam String nombre,
            @RequestParam(defaultValue = "0") BigDecimal baseRep21,
            @RequestParam(defaultValue = "0") BigDecimal cuotaRep21,
            @RequestParam(defaultValue = "0") BigDecimal baseSop21,
            @RequestParam(defaultValue = "0") BigDecimal cuotaSop21
    ) throws IOException {

        byte[] contenido = modelo303BoeService.generarFicheroBoe(
                nif, nombre, ejercicio, periodo,
                baseRep21, cuotaRep21,
                baseSop21, cuotaSop21
        );

        return descargar(contenido, nif, ejercicio, periodo, "303");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST completo — todos los tipos + bienes de inversión
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Exporta el Modelo 303 con todos los tipos de IVA y bienes de inversión.
     *
     * Body JSON (campos opcionales → defecto 0):
     * {
     *   "nif": "12345678A",
     *   "nombre": "SEPULVEDA GARCIA FRANCISCO",
     *   "ejercicio": 2026,
     *   "periodo": "1T",
     *   "baseRep21": 3813.06, "cuotaRep21": 800.86,
     *   "baseSop21": 2513.07, "cuotaSop21": 240.71,
     *   "baseInversion": 0,   "cuotaInversion": 0
     * }
     */
    @PostMapping("/exportar-boe")
    public ResponseEntity<byte[]> exportarBoeCompleto(
            @RequestBody Modelo303BoeService.Modelo303Datos datos
    ) throws IOException {

        byte[] contenido = modelo303BoeService.generarFicheroBoeCompleto(datos);
        return descargar(contenido, datos.nif, datos.ejercicio, datos.periodo, "303");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    private ResponseEntity<byte[]> descargar(byte[] contenido, String nif, int ejercicio,
                                              String periodo, String extension) {
        String nombreFichero = nif.toUpperCase() + ejercicio + periodo.toUpperCase() + "." + extension;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreFichero + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(contenido);
    }
}

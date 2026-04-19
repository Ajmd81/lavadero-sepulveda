package com.lavaderosepulveda.app.fiscal.controller;

import com.lavaderosepulveda.app.fiscal.service.DeclaracionFiscalService;
import com.lavaderosepulveda.app.fiscal.service.Modelo303BoeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/fiscal/modelo303")
@CrossOrigin(origins = "*")
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
            @RequestParam(defaultValue = "0") BigDecimal baseRep21,
            @RequestParam(defaultValue = "0") BigDecimal cuotaRep21,
            @RequestParam(defaultValue = "0") BigDecimal baseSop21,
            @RequestParam(defaultValue = "0") BigDecimal cuotaSop21
    ) throws IOException {

        byte[] contenido = modelo303BoeService.generarFicheroBoe(
                nif, nombre, ejercicio, periodo,
                baseRep21, cuotaRep21, baseSop21, cuotaSop21
        );

        String nombreFichero = nif.toUpperCase() + ejercicio + periodo.toUpperCase() + ".303";

        declaracionFiscalService.registrarGeneracion303(
                ejercicio, periodo,
                baseRep21, cuotaRep21, baseSop21, cuotaSop21,
                nombreFichero
        );

        return descargar(contenido, nombreFichero);
    }

    @PostMapping("/exportar-boe")
    public ResponseEntity<byte[]> exportarBoeCompleto(
            @RequestBody Modelo303BoeService.Modelo303Datos datos
    ) throws IOException {

        byte[] contenido = modelo303BoeService.generarFicheroBoeCompleto(datos);

        String nombreFichero = datos.nif.toUpperCase() + datos.ejercicio + datos.periodo.toUpperCase() + ".303";

        BigDecimal baseRepTotal  = orCero(datos.baseRep4).add(orCero(datos.baseRep10)).add(orCero(datos.baseRep21));
        BigDecimal cuotaRepTotal = orCero(datos.cuotaRep4).add(orCero(datos.cuotaRep10)).add(orCero(datos.cuotaRep21));
        BigDecimal baseSopTotal  = orCero(datos.baseSop4).add(orCero(datos.baseSop10)).add(orCero(datos.baseSop21));
        BigDecimal cuotaSopTotal = orCero(datos.cuotaSop4).add(orCero(datos.cuotaSop10)).add(orCero(datos.cuotaSop21));

        declaracionFiscalService.registrarGeneracion303(
                datos.ejercicio, datos.periodo,
                baseRepTotal, cuotaRepTotal, baseSopTotal, cuotaSopTotal,
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

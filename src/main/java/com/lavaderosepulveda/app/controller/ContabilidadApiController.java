package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.ContabilidadResumenDTO;
import com.lavaderosepulveda.app.service.ContabilidadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/contabilidad")

public class ContabilidadApiController {
    private static final Logger log = LoggerFactory.getLogger(ContabilidadApiController.class);

    @Autowired
    private ContabilidadService contabilidadService;

    /**
     * GET /api/contabilidad/resumen
     * Obtiene el resumen contable para un rango de fechas
     *
     * @param desde fecha inicio (YYYY-MM-DD)
     * @param hasta fecha fin (YYYY-MM-DD)
     * @return resumen con ingresos, base, IVA, facturas y desglose mensual
     */
    @GetMapping("/resumen")
    public ResponseEntity<ContabilidadResumenDTO> obtenerResumen(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        try {
            log.info("Solicitud de resumen contable: desde {} hasta {}", desde, hasta);
            ContabilidadResumenDTO resumen = contabilidadService.generarResumen(desde, hasta);
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            log.error("Error generando resumen contable", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/contabilidad/exportar-excel
     * Descarga reporte en Excel
     *
     * @param desde fecha inicio (YYYY-MM-DD)
     * @param hasta fecha fin (YYYY-MM-DD)
     * @return archivo Excel
     */
    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        try {
            log.info("Exportación a Excel: desde {} hasta {}", desde, hasta);
            byte[] excelBytes = contabilidadService.generarExcel(desde, hasta);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",
                    "contabilidad_" + desde + "_" + hasta + ".xlsx");

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error exportando Excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/contabilidad/exportar-pdf
     * Descarga reporte en PDF
     *
     * @param desde fecha inicio (YYYY-MM-DD)
     * @param hasta fecha fin (YYYY-MM-DD)
     * @return archivo PDF
     */
    @GetMapping("/exportar-pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        try {
            log.info("Exportación a PDF: desde {} hasta {}", desde, hasta);
            byte[] pdfBytes = contabilidadService.generarPdf(desde, hasta);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "contabilidad_" + desde + "_" + hasta + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error exportando PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

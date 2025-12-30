package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.model.Factura;
import com.lavaderosepulveda.app.service.FacturaPdfService;
import com.lavaderosepulveda.app.service.FacturaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/facturas")
public class FacturaPdfController {

    private static final Logger log = LoggerFactory.getLogger(FacturaPdfController.class);

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private FacturaPdfService facturaPdfService;

    /**
     * GET /api/facturas/{id}/pdf
     * Descargar PDF de factura
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long id) {
        try {
            Optional<Factura> facturaOpt = facturaService.obtenerPorId(id);
            
            if (facturaOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Factura factura = facturaOpt.get();
            byte[] pdfBytes = facturaPdfService.generarPdf(factura);

            String nombreArchivo = "Factura_" + factura.getNumero().replace("/", "-") + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            log.info("PDF descargado para factura {}", factura.getNumero());
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error al generar PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/facturas/{id}/pdf/preview
     * Ver PDF en el navegador (inline)
     */
    @GetMapping("/{id}/pdf/preview")
    public ResponseEntity<byte[]> previsualizarPdf(@PathVariable Long id) {
        try {
            Optional<Factura> facturaOpt = facturaService.obtenerPorId(id);
            
            if (facturaOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Factura factura = facturaOpt.get();
            byte[] pdfBytes = facturaPdfService.generarPdf(factura);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add("Content-Disposition", "inline; filename=Factura_" + 
                factura.getNumero().replace("/", "-") + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error al previsualizar PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

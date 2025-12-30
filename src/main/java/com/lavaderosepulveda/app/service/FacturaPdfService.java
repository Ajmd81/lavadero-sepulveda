package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.model.Factura;
import com.lavaderosepulveda.app.model.LineaFactura;
import com.lavaderosepulveda.app.model.TipoFactura;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class FacturaPdfService {

    private static final Logger log = LoggerFactory.getLogger(FacturaPdfService.class);

    // Datos del emisor
    private static final String EMISOR_NOMBRE = "ANTONIO JESUS MARTINEZ DÍAZ";
    private static final String EMISOR_NIF = "44372838L";
    private static final String EMISOR_DIRECCION = "C/ Ingeniero Ruiz de Azua s/n Local 8";
    private static final String EMISOR_CP_CIUDAD = "14006 Córdoba";
    private static final String NOMBRE_COMERCIAL = "LAVADERO SEPÚLVEDA";

    // Colores corporativos
    private static final Color COLOR_PRIMARIO = new Color(33, 150, 243); // Azul
    private static final Color COLOR_GRIS = new Color(158, 158, 158);
    private static final Color COLOR_LINEA = new Color(224, 224, 224);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Generar PDF de factura
     */
    public byte[] generarPdf(Factura factura) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Crear documento A4
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        
        document.open();

        // Fuentes
        Font fuenteTitulo = new Font(Font.HELVETICA, 24, Font.BOLD, COLOR_PRIMARIO);
        Font fuenteSubtitulo = new Font(Font.HELVETICA, 12, Font.NORMAL, COLOR_GRIS);
        Font fuenteNormal = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
        Font fuenteNegrita = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
        Font fuentePequena = new Font(Font.HELVETICA, 8, Font.NORMAL, COLOR_GRIS);
        Font fuenteTotal = new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_PRIMARIO);

        // ========== CABECERA ==========
        PdfPTable tablaCabecera = new PdfPTable(2);
        tablaCabecera.setWidthPercentage(100);
        tablaCabecera.setWidths(new float[]{60, 40});

        // Logo / Nombre comercial
        PdfPCell celdaLogo = new PdfPCell();
        celdaLogo.setBorder(Rectangle.NO_BORDER);
        celdaLogo.setPaddingBottom(20);
        
        Paragraph nombreEmpresa = new Paragraph(NOMBRE_COMERCIAL, fuenteTitulo);
        celdaLogo.addElement(nombreEmpresa);
        
        Paragraph datosEmpresa = new Paragraph();
        datosEmpresa.add(new Chunk(EMISOR_NOMBRE + "\n", fuenteNormal));
        datosEmpresa.add(new Chunk("NIF: " + EMISOR_NIF + "\n", fuenteNormal));
        datosEmpresa.add(new Chunk(EMISOR_DIRECCION + "\n", fuenteNormal));
        datosEmpresa.add(new Chunk(EMISOR_CP_CIUDAD, fuenteNormal));
        celdaLogo.addElement(datosEmpresa);
        tablaCabecera.addCell(celdaLogo);

        // Datos de la factura
        PdfPCell celdaFactura = new PdfPCell();
        celdaFactura.setBorder(Rectangle.NO_BORDER);
        celdaFactura.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaFactura.setPaddingBottom(20);

        String tipoFactura = factura.getTipo() == TipoFactura.SIMPLIFICADA ? 
            "FACTURA SIMPLIFICADA" : "FACTURA";
        
        Paragraph tituloFactura = new Paragraph(tipoFactura, fuenteSubtitulo);
        tituloFactura.setAlignment(Element.ALIGN_RIGHT);
        celdaFactura.addElement(tituloFactura);

        Paragraph numFactura = new Paragraph("Nº " + factura.getNumero(), fuenteTitulo);
        numFactura.setAlignment(Element.ALIGN_RIGHT);
        celdaFactura.addElement(numFactura);

        Paragraph fechaFactura = new Paragraph("Fecha: " + factura.getFecha().format(DATE_FORMATTER), fuenteNormal);
        fechaFactura.setAlignment(Element.ALIGN_RIGHT);
        celdaFactura.addElement(fechaFactura);

        tablaCabecera.addCell(celdaFactura);
        document.add(tablaCabecera);

        // Línea separadora
        document.add(crearLineaSeparadora());

        // ========== DATOS DEL CLIENTE ==========
        if (factura.getTipo() == TipoFactura.COMPLETA) {
            PdfPTable tablaCliente = new PdfPTable(1);
            tablaCliente.setWidthPercentage(50);
            tablaCliente.setHorizontalAlignment(Element.ALIGN_LEFT);
            tablaCliente.setSpacingBefore(15);
            tablaCliente.setSpacingAfter(15);

            PdfPCell celdaCliente = new PdfPCell();
            celdaCliente.setBackgroundColor(new Color(245, 245, 245));
            celdaCliente.setPadding(10);
            celdaCliente.setBorder(Rectangle.NO_BORDER);

            Paragraph tituloCliente = new Paragraph("DATOS DEL CLIENTE", fuenteNegrita);
            celdaCliente.addElement(tituloCliente);

            Paragraph datosCliente = new Paragraph();
            datosCliente.setSpacingBefore(5);
            if (factura.getClienteNombre() != null) {
                datosCliente.add(new Chunk(factura.getClienteNombre() + "\n", fuenteNormal));
            }
            if (factura.getClienteNif() != null && !factura.getClienteNif().isEmpty()) {
                datosCliente.add(new Chunk("NIF: " + factura.getClienteNif() + "\n", fuenteNormal));
            }
            if (factura.getClienteDireccion() != null && !factura.getClienteDireccion().isEmpty()) {
                datosCliente.add(new Chunk(factura.getClienteDireccion() + "\n", fuenteNormal));
            }
            if (factura.getClienteTelefono() != null) {
                datosCliente.add(new Chunk("Tel: " + factura.getClienteTelefono(), fuenteNormal));
            }
            celdaCliente.addElement(datosCliente);

            tablaCliente.addCell(celdaCliente);
            document.add(tablaCliente);
        } else {
            // Para factura simplificada, solo mostrar nombre si existe
            if (factura.getClienteNombre() != null && !factura.getClienteNombre().isEmpty()) {
                Paragraph clienteSimple = new Paragraph("Cliente: " + factura.getClienteNombre(), fuenteNormal);
                clienteSimple.setSpacingBefore(15);
                clienteSimple.setSpacingAfter(15);
                document.add(clienteSimple);
            }
        }

        // ========== TABLA DE CONCEPTOS ==========
        PdfPTable tablaConceptos = new PdfPTable(4);
        tablaConceptos.setWidthPercentage(100);
        tablaConceptos.setWidths(new float[]{50, 10, 20, 20});
        tablaConceptos.setSpacingBefore(10);

        // Cabecera de la tabla
        agregarCeldaCabecera(tablaConceptos, "CONCEPTO", fuenteNegrita);
        agregarCeldaCabecera(tablaConceptos, "CANT.", fuenteNegrita);
        agregarCeldaCabecera(tablaConceptos, "PRECIO", fuenteNegrita);
        agregarCeldaCabecera(tablaConceptos, "IMPORTE", fuenteNegrita);

        // Líneas de la factura
        for (LineaFactura linea : factura.getLineas()) {
            agregarCeldaConcepto(tablaConceptos, linea.getConcepto(), fuenteNormal);
            agregarCeldaCentrada(tablaConceptos, String.valueOf(linea.getCantidad()), fuenteNormal);
            agregarCeldaDerecha(tablaConceptos, formatearImporte(linea.getPrecioUnitario()), fuenteNormal);
            agregarCeldaDerecha(tablaConceptos, formatearImporte(linea.getSubtotal()), fuenteNormal);
        }

        document.add(tablaConceptos);

        // ========== TOTALES ==========
        PdfPTable tablaTotales = new PdfPTable(2);
        tablaTotales.setWidthPercentage(40);
        tablaTotales.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tablaTotales.setSpacingBefore(20);

        // Base imponible
        agregarFilaTotal(tablaTotales, "Base Imponible:", formatearImporte(factura.getBaseImponible()), fuenteNormal);
        
        // IVA
        agregarFilaTotal(tablaTotales, "IVA (" + factura.getTipoIva().intValue() + "%):", 
            formatearImporte(factura.getImporteIva()), fuenteNormal);
        
        // Total
        agregarFilaTotal(tablaTotales, "TOTAL:", formatearImporte(factura.getTotal()), fuenteTotal);

        document.add(tablaTotales);

        // ========== INFORMACIÓN DE PAGO ==========
        if (factura.getEstado().name().equals("PAGADA") && factura.getMetodoPago() != null) {
            Paragraph pagado = new Paragraph();
            pagado.setSpacingBefore(30);
            pagado.add(new Chunk("✓ PAGADO", new Font(Font.HELVETICA, 12, Font.BOLD, new Color(76, 175, 80))));
            pagado.add(new Chunk(" - " + factura.getMetodoPago().getDescripcion(), fuenteNormal));
            if (factura.getFechaPago() != null) {
                pagado.add(new Chunk(" (" + factura.getFechaPago().format(DATE_FORMATTER) + ")", fuentePequena));
            }
            document.add(pagado);
        }

        // ========== OBSERVACIONES ==========
        if (factura.getObservaciones() != null && !factura.getObservaciones().isEmpty()) {
            Paragraph obs = new Paragraph();
            obs.setSpacingBefore(20);
            obs.add(new Chunk("Observaciones: ", fuenteNegrita));
            obs.add(new Chunk(factura.getObservaciones(), fuenteNormal));
            document.add(obs);
        }

        // ========== PIE DE PÁGINA ==========
        Paragraph pie = new Paragraph();
        pie.setSpacingBefore(40);
        pie.setAlignment(Element.ALIGN_CENTER);
        pie.add(new Chunk("Gracias por su confianza", fuentePequena));
        document.add(pie);

        document.close();
        
        log.info("PDF generado para factura {}", factura.getNumero());
        return baos.toByteArray();
    }

    // ========== MÉTODOS AUXILIARES ==========

    private Paragraph crearLineaSeparadora() {
        Paragraph linea = new Paragraph();
        linea.setSpacingBefore(10);
        linea.setSpacingAfter(10);
        
        LineSeparator ls = new LineSeparator();
        ls.setLineColor(COLOR_LINEA);
        linea.add(ls);
        
        return linea;
    }

    private void agregarCeldaCabecera(PdfPTable tabla, String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBackgroundColor(new Color(245, 245, 245));
        celda.setPadding(8);
        celda.setBorderColor(COLOR_LINEA);
        tabla.addCell(celda);
    }

    private void agregarCeldaConcepto(PdfPTable tabla, String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setPadding(8);
        celda.setBorderColor(COLOR_LINEA);
        tabla.addCell(celda);
    }

    private void agregarCeldaCentrada(PdfPTable tabla, String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setPadding(8);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setBorderColor(COLOR_LINEA);
        tabla.addCell(celda);
    }

    private void agregarCeldaDerecha(PdfPTable tabla, String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setPadding(8);
        celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celda.setBorderColor(COLOR_LINEA);
        tabla.addCell(celda);
    }

    private void agregarFilaTotal(PdfPTable tabla, String etiqueta, String valor, Font fuente) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, fuente));
        celdaEtiqueta.setBorder(Rectangle.NO_BORDER);
        celdaEtiqueta.setPadding(5);
        celdaEtiqueta.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.addCell(celdaEtiqueta);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fuente));
        celdaValor.setBorder(Rectangle.NO_BORDER);
        celdaValor.setPadding(5);
        celdaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.addCell(celdaValor);
    }

    private String formatearImporte(java.math.BigDecimal importe) {
        if (importe == null) return "0,00 €";
        return String.format("%,.2f €", importe).replace(",", "X").replace(".", ",").replace("X", ".");
    }
}

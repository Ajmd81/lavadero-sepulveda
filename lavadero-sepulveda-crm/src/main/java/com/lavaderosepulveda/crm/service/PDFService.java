package com.lavaderosepulveda.crm.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.lavaderosepulveda.crm.model.Factura;
import com.lavaderosepulveda.crm.model.LineaFactura;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.time.format.DateTimeFormatter;

@Slf4j
public class PDFService {

    private static PDFService instance;
    private static final String PDF_DIR = "./facturas";

    private PDFService() {
        // Crear directorio de facturas si no existe
        File dir = new File(PDF_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static synchronized PDFService getInstance() {
        if (instance == null) {
            instance = new PDFService();
        }
        return instance;
    }

    public File generarFacturaPDF(Factura factura) {
        try {
            String filename = String.format("%s/Factura_%s.pdf",
                    PDF_DIR,
                    factura.getNumeroFactura().replace("/", "-"));

            File pdfFile = new File(filename);

            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Encabezado
            document.add(new Paragraph("LAVADERO SEPÚLVEDA")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("CIF: B12345678")
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Dirección: Calle Principal, 123, Madrid")
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Teléfono: +34 XXX XXX XXX")
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            // Información de la factura
            document.add(new Paragraph("FACTURA: " + factura.getNumeroFactura())
                    .setFontSize(16)
                    .setBold());

            document.add(new Paragraph("Fecha: " +
                    factura.getFechaFactura().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));

            if (factura.getFechaVencimiento() != null) {
                document.add(new Paragraph("Fecha de vencimiento: " +
                        factura.getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            }

            document.add(new Paragraph("\n"));

            // Datos del cliente
            document.add(new Paragraph("CLIENTE:")
                    .setBold());
            document.add(new Paragraph(factura.getCliente().getNombreCompleto()));

            if (factura.getCliente().getNif() != null && !factura.getCliente().getNif().isEmpty()) {
                document.add(new Paragraph("NIF: " + factura.getCliente().getNif()));
            }

            if (factura.getCliente().getDireccion() != null) {
                document.add(new Paragraph("Dirección: " + factura.getCliente().getDireccion()));
            }

            if (factura.getCliente().getTelefono() != null) {
                document.add(new Paragraph("Teléfono: " + factura.getCliente().getTelefono()));
            }

            if (factura.getCliente().getEmail() != null) {
                document.add(new Paragraph("Email: " + factura.getCliente().getEmail()));
            }

            document.add(new Paragraph("\n"));

            // Tabla de líneas de factura
            float[] columnWidths = { 3, 1, 2, 1, 1, 2 };
            Table table = new Table(columnWidths);
            table.setWidth(550);

            // Encabezados
            table.addHeaderCell("Concepto");
            table.addHeaderCell("Cant.");
            table.addHeaderCell("Precio Unit.");
            table.addHeaderCell("Desc.");
            table.addHeaderCell("IVA");
            table.addHeaderCell("Total");

            // Líneas
            for (LineaFactura linea : factura.getLineas()) {
                table.addCell(linea.getConcepto());
                table.addCell(String.valueOf(linea.getCantidad()));
                table.addCell(String.format("%.2f €", linea.getPrecioUnitario()));
                table.addCell(String.format("%.0f%%", linea.getDescuento()));
                table.addCell(String.format("%.0f%%", linea.getIva()));
                table.addCell(String.format("%.2f €", linea.getTotal()));
            }

            document.add(table);

            document.add(new Paragraph("\n"));

            // Totales
            document.add(new Paragraph("Base Imponible: " +
                    String.format("%.2f €", factura.getBaseImponible()))
                    .setTextAlignment(TextAlignment.RIGHT));

            document.add(new Paragraph("IVA: " +
                    String.format("%.2f €", factura.getTotalIva()))
                    .setTextAlignment(TextAlignment.RIGHT));

            document.add(new Paragraph("TOTAL: " +
                    String.format("%.2f €", factura.getTotalFactura()))
                    .setFontSize(14)
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT));

            document.add(new Paragraph("\n"));

            // Forma de pago - usar el método de pago real de la factura
            String metodoPago = "Pendiente";
            if (factura.getFormaPago() != null && !factura.getFormaPago().isEmpty()) {
                // Formatear el método de pago
                switch (factura.getFormaPago().toUpperCase()) {
                    case "EFECTIVO":
                        metodoPago = "Efectivo";
                        break;
                    case "TARJETA":
                        metodoPago = "Tarjeta";
                        break;
                    case "BIZUM":
                        metodoPago = "Bizum";
                        break;
                    case "TRANSFERENCIA":
                        metodoPago = "Transferencia";
                        break;
                    default:
                        metodoPago = factura.getFormaPago();
                }
            }
            document.add(new Paragraph("Forma de pago: " + metodoPago));

            // Estado
            document.add(new Paragraph("Estado: " + (factura.getPagada() ? "PAGADA" : "PENDIENTE DE PAGO"))
                    .setBold());

            // Observaciones
            if (factura.getObservaciones() != null && !factura.getObservaciones().isEmpty()) {
                document.add(new Paragraph("\n"));
                document.add(new Paragraph("Observaciones:")
                        .setBold());
                document.add(new Paragraph(factura.getObservaciones()));
            }

            // Pie de página
            document.add(new Paragraph("\n\n"));
            document.add(new Paragraph("Gracias por confiar en nuestros servicios")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic());

            document.close();

            log.info("PDF generado: {}", filename);
            return pdfFile;

        } catch (Exception e) {
            log.error("Error al generar PDF de factura", e);
            return null;
        }
    }
}

package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.model.Factura;
import com.lavaderosepulveda.app.model.LineaFactura;
import com.lavaderosepulveda.app.model.MetodoPago;
import com.lavaderosepulveda.app.model.PlantillaFacturaConfig;
import com.lavaderosepulveda.app.repository.PlantillaFacturaConfigRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

/**
 * Servicio para generar PDFs de facturas usando la plantilla configurable
 */
@Service
public class FacturaPdfService {

    private static final Logger log = LoggerFactory.getLogger(FacturaPdfService.class);

    @Autowired
    private PlantillaFacturaConfigRepository configRepository;

    private final NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Generar PDF de una factura
     */
    public byte[] generarPdf(Factura factura) throws Exception {
        // Obtener configuración de plantilla
        PlantillaFacturaConfig config = configRepository.findById(1L)
                .orElseGet(PlantillaFacturaConfig::new);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        document.open();

        // Colores desde configuración
        Color colorPrimario = hexToColor(config.getColorPrimario());
        Color colorTexto = hexToColor(config.getColorTexto());
        Color colorBorde = hexToColor(config.getColorBorde());

        // Fuentes
        Font fuenteTitulo = new Font(Font.HELVETICA, 18, Font.BOLD, colorPrimario);
        Font fuenteSubtitulo = new Font(Font.HELVETICA, 12, Font.BOLD, colorTexto);
        Font fuenteNormal = new Font(Font.HELVETICA, 10, Font.NORMAL, colorTexto);
        Font fuentePequena = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.GRAY);
        Font fuenteNegrita = new Font(Font.HELVETICA, 10, Font.BOLD, colorTexto);
        Font fuenteTotal = new Font(Font.HELVETICA, 12, Font.BOLD, colorPrimario);

        // ========================================
        // CABECERA
        // ========================================
        PdfPTable tablaCabecera = new PdfPTable(2);
        tablaCabecera.setWidthPercentage(100);
        tablaCabecera.setWidths(new float[]{1, 1});

        // Logo o nombre
        PdfPCell celdaLogo = new PdfPCell();
        celdaLogo.setBorder(Rectangle.NO_BORDER);
        celdaLogo.setVerticalAlignment(Element.ALIGN_TOP);

        if (config.getMostrarLogo() && config.getLogoBase64() != null && !config.getLogoBase64().isEmpty()) {
            try {
                String base64Data = config.getLogoBase64();
                if (base64Data.contains(",")) {
                    base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                }
                byte[] logoBytes = Base64.getDecoder().decode(base64Data);
                Image logo = Image.getInstance(logoBytes);
                logo.scaleToFit(config.getLogoAncho(), config.getLogoAlto());
                celdaLogo.addElement(logo);
            } catch (Exception e) {
                log.warn("Error cargando logo, usando texto", e);
                celdaLogo.addElement(new Paragraph(config.getEmisorNombre(), fuenteTitulo));
            }
        } else {
            celdaLogo.addElement(new Paragraph(config.getEmisorNombre(), fuenteTitulo));
        }
        tablaCabecera.addCell(celdaLogo);

        // Título y número de factura
        PdfPCell celdaTitulo = new PdfPCell();
        celdaTitulo.setBorder(Rectangle.NO_BORDER);
        celdaTitulo.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaTitulo.setVerticalAlignment(Element.ALIGN_TOP);

        String titulo = factura.getTipo() != null && factura.getTipo().name().equals("SIMPLIFICADA")
                ? config.getTituloFacturaSimplificada()
                : config.getTituloFactura();

        Paragraph pTitulo = new Paragraph(titulo, fuenteTitulo);
        pTitulo.setAlignment(Element.ALIGN_RIGHT);
        celdaTitulo.addElement(pTitulo);

        Paragraph pNumero = new Paragraph("Nº: " + factura.getNumero(), fuenteSubtitulo);
        pNumero.setAlignment(Element.ALIGN_RIGHT);
        celdaTitulo.addElement(pNumero);

        Paragraph pFecha = new Paragraph("Fecha: " + factura.getFecha().format(formatoFecha), fuenteNormal);
        pFecha.setAlignment(Element.ALIGN_RIGHT);
        celdaTitulo.addElement(pFecha);

        tablaCabecera.addCell(celdaTitulo);
        document.add(tablaCabecera);

        // Línea separadora
        PdfPTable lineaSeparadora = new PdfPTable(1);
        lineaSeparadora.setWidthPercentage(100);
        lineaSeparadora.setSpacingBefore(10);
        PdfPCell celdaLinea = new PdfPCell();
        celdaLinea.setBorderWidthTop(2);
        celdaLinea.setBorderColorTop(colorPrimario);
        celdaLinea.setBorderWidthBottom(0);
        celdaLinea.setBorderWidthLeft(0);
        celdaLinea.setBorderWidthRight(0);
        celdaLinea.setFixedHeight(5);
        lineaSeparadora.addCell(celdaLinea);
        document.add(lineaSeparadora);

        // ========================================
        // DATOS DEL EMISOR
        // ========================================
        Paragraph pEmisor = new Paragraph();
        pEmisor.setSpacingBefore(15);
        pEmisor.add(new Chunk(config.getEmisorNombre() + "\n", fuenteNegrita));
        pEmisor.add(new Chunk("NIF: " + config.getEmisorNif() + "\n", fuentePequena));
        pEmisor.add(new Chunk(config.getDireccionCompleta() + "\n", fuentePequena));

        if (config.getMostrarDatosContacto()) {
            if (config.getEmisorTelefono() != null && !config.getEmisorTelefono().isEmpty()) {
                pEmisor.add(new Chunk("Tel: " + config.getEmisorTelefono() + "\n", fuentePequena));
            }
            if (config.getEmisorEmail() != null && !config.getEmisorEmail().isEmpty()) {
                pEmisor.add(new Chunk(config.getEmisorEmail() + "\n", fuentePequena));
            }
        }
        document.add(pEmisor);

        // ========================================
        // DATOS DEL CLIENTE
        // ========================================
        PdfPTable tablaCliente = new PdfPTable(1);
        tablaCliente.setWidthPercentage(60);
        tablaCliente.setHorizontalAlignment(Element.ALIGN_LEFT);
        tablaCliente.setSpacingBefore(15);

        PdfPCell celdaCliente = new PdfPCell();
        celdaCliente.setBackgroundColor(new Color(249, 249, 249));
        celdaCliente.setBorderWidth(0);
        celdaCliente.setBorderWidthLeft(3);
        celdaCliente.setBorderColorLeft(colorPrimario);
        celdaCliente.setPadding(10);

        Paragraph pCliente = new Paragraph();
        pCliente.add(new Chunk("CLIENTE\n", fuentePequena));
        pCliente.add(new Chunk(factura.getClienteNombre() + "\n", fuenteNegrita));
        if (factura.getClienteNif() != null && !factura.getClienteNif().isEmpty()) {
            pCliente.add(new Chunk(factura.getClienteNif() + "\n", fuenteNormal));
        }
        if (factura.getClienteDireccion() != null && !factura.getClienteDireccion().isEmpty()) {
            pCliente.add(new Chunk(factura.getClienteDireccion() + "\n", fuenteNormal));
        }
        celdaCliente.addElement(pCliente);
        tablaCliente.addCell(celdaCliente);
        document.add(tablaCliente);

        // ========================================
        // TABLA DE CONCEPTOS
        // ========================================
        PdfPTable tablaConceptos = new PdfPTable(4);
        tablaConceptos.setWidthPercentage(100);
        tablaConceptos.setSpacingBefore(20);
        tablaConceptos.setWidths(new float[]{5, 1, 1.5f, 1.5f});

        // Cabecera de tabla
        Font fuenteCabeceraTabla = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        String[] cabeceras = {"Concepto", "Cant.", "Precio", "Importe"};
        for (String cabecera : cabeceras) {
            PdfPCell celda = new PdfPCell(new Phrase(cabecera, fuenteCabeceraTabla));
            celda.setBackgroundColor(colorPrimario);
            celda.setPadding(8);
            celda.setHorizontalAlignment(cabecera.equals("Concepto") ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            tablaConceptos.addCell(celda);
        }

        // Filas de conceptos
        Color colorFondoAlt = hexToColor(config.getColorFondoAlt());
        int fila = 0;
        for (LineaFactura linea : factura.getLineas()) {
            Color fondoFila = (config.getUsarFilasAlternas() && fila % 2 == 1) ? colorFondoAlt : Color.WHITE;

            // Concepto
            PdfPCell celdaConcepto = new PdfPCell(new Phrase(linea.getConcepto(), fuenteNormal));
            celdaConcepto.setPadding(8);
            celdaConcepto.setBackgroundColor(fondoFila);
            celdaConcepto.setBorderColor(colorBorde);
            tablaConceptos.addCell(celdaConcepto);

            // Cantidad
            PdfPCell celdaCantidad = new PdfPCell(new Phrase(String.valueOf(linea.getCantidad()), fuenteNormal));
            celdaCantidad.setPadding(8);
            celdaCantidad.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaCantidad.setBackgroundColor(fondoFila);
            celdaCantidad.setBorderColor(colorBorde);
            tablaConceptos.addCell(celdaCantidad);

            // Precio unitario
            PdfPCell celdaPrecio = new PdfPCell(new Phrase(formatoMoneda.format(linea.getPrecioUnitario()), fuenteNormal));
            celdaPrecio.setPadding(8);
            celdaPrecio.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaPrecio.setBackgroundColor(fondoFila);
            celdaPrecio.setBorderColor(colorBorde);
            tablaConceptos.addCell(celdaPrecio);

            // Subtotal
            PdfPCell celdaSubtotal = new PdfPCell(new Phrase(formatoMoneda.format(linea.getSubtotal()), fuenteNormal));
            celdaSubtotal.setPadding(8);
            celdaSubtotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaSubtotal.setBackgroundColor(fondoFila);
            celdaSubtotal.setBorderColor(colorBorde);
            tablaConceptos.addCell(celdaSubtotal);

            fila++;
        }

        document.add(tablaConceptos);

        // ========================================
        // TOTALES
        // ========================================
        PdfPTable tablaTotales = new PdfPTable(2);
        tablaTotales.setWidthPercentage(40);
        tablaTotales.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tablaTotales.setSpacingBefore(15);
        tablaTotales.setWidths(new float[]{1, 1});

        // Base imponible
        agregarFilaTotal(tablaTotales, "Base Imponible:", formatoMoneda.format(factura.getBaseImponible()), fuenteNormal, colorBorde);

        // IVA
        String textoIva = "IVA (" + factura.getTipoIva().intValue() + "%):";
        agregarFilaTotal(tablaTotales, textoIva, formatoMoneda.format(factura.getImporteIva()), fuenteNormal, colorBorde);

        // Total
        PdfPCell celdaTotalLabel = new PdfPCell(new Phrase("TOTAL:", fuenteTotal));
        celdaTotalLabel.setBorder(Rectangle.TOP);
        celdaTotalLabel.setBorderColorTop(colorPrimario);
        celdaTotalLabel.setBorderWidthTop(2);
        celdaTotalLabel.setPaddingTop(8);
        celdaTotalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tablaTotales.addCell(celdaTotalLabel);

        PdfPCell celdaTotalValor = new PdfPCell(new Phrase(formatoMoneda.format(factura.getTotal()), fuenteTotal));
        celdaTotalValor.setBorder(Rectangle.TOP);
        celdaTotalValor.setBorderColorTop(colorPrimario);
        celdaTotalValor.setBorderWidthTop(2);
        celdaTotalValor.setPaddingTop(8);
        celdaTotalValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tablaTotales.addCell(celdaTotalValor);

        document.add(tablaTotales);

        // ========================================
        // MARCA DE AGUA "PAGADA" (si aplica)
        // ========================================
        if (config.getMostrarMarcaAgua() && factura.getEstado() != null &&
                factura.getEstado().name().equals("PAGADA")) {

            PdfContentByte canvas = writer.getDirectContentUnder();
            canvas.saveState();
            PdfGState gs = new PdfGState();
            gs.setFillOpacity(0.1f);
            canvas.setGState(gs);

            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            canvas.beginText();
            canvas.setFontAndSize(bf, 80);
            canvas.setColorFill(hexToColor(config.getColorExito()));
            canvas.showTextAligned(Element.ALIGN_CENTER, "PAGADA",
                    PageSize.A4.getWidth() / 2, PageSize.A4.getHeight() / 2, 45);
            canvas.endText();
            canvas.restoreState();
        }

        // ========================================
        // PIE DE FACTURA
        // ========================================
        Paragraph pPie = new Paragraph();
        pPie.setSpacingBefore(30);
        pPie.setAlignment(Element.ALIGN_CENTER);

        // Pie de factura personalizado
        if (config.getPieFactura() != null && !config.getPieFactura().isEmpty()) {
            pPie.add(new Chunk(config.getPieFactura() + "\n\n", fuentePequena));
        }

        // Método de pago (si existe en la factura)
        if (factura.getMetodoPago() != null) {
            String metodoPagoFormateado = formatearMetodoPago(factura.getMetodoPago());
            pPie.add(new Chunk("Forma de pago: " + metodoPagoFormateado + "\n", fuenteNormal));
        }

        // Condiciones de pago de la plantilla (solo si no hay método de pago específico)
        if (config.getMostrarCondicionesPago() && config.getCondicionesPago() != null &&
                !config.getCondicionesPago().isEmpty() && factura.getMetodoPago() == null) {
            pPie.add(new Chunk(config.getCondicionesPago() + "\n", fuentePequena));
        }

        if (config.getMostrarCuentaBancaria() && config.getCuentaBancaria() != null &&
                !config.getCuentaBancaria().isEmpty()) {
            pPie.add(new Chunk("Cuenta: " + config.getCuentaBancaria() + "\n", fuentePequena));
        }

        if (config.getMostrarTextoGracias() && config.getTextoGracias() != null &&
                !config.getTextoGracias().isEmpty()) {
            pPie.add(new Chunk("\n" + config.getTextoGracias(),
                    new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY)));
        }

        document.add(pPie);

        document.close();
        return baos.toByteArray();
    }

    /**
     * Agregar fila a tabla de totales
     */
    private void agregarFilaTotal(PdfPTable tabla, String label, String valor, Font fuente, Color colorBorde) {
        PdfPCell celdaLabel = new PdfPCell(new Phrase(label, fuente));
        celdaLabel.setBorder(Rectangle.NO_BORDER);
        celdaLabel.setPadding(5);
        celdaLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.addCell(celdaLabel);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fuente));
        celdaValor.setBorder(Rectangle.NO_BORDER);
        celdaValor.setPadding(5);
        celdaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.addCell(celdaValor);
    }

    /**
     * Formatear método de pago para mostrar en el PDF
     */
    private String formatearMetodoPago(MetodoPago metodoPago) {
        if (metodoPago == null) {
            return "Pendiente";
        }

        switch (metodoPago) {
            case EFECTIVO:
                return "Efectivo";
            case TARJETA:
                return "Tarjeta";
            case BIZUM:
                return "Bizum";
            case TRANSFERENCIA:
                return "Transferencia";
            default:
                return metodoPago.name();
        }
    }

    /**
     * Convertir color hexadecimal a Color
     */
    private Color hexToColor(String hex) {
        if (hex == null || hex.isEmpty()) {
            return Color.BLACK;
        }
        hex = hex.replace("#", "");
        return new Color(
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16)
        );
    }
}
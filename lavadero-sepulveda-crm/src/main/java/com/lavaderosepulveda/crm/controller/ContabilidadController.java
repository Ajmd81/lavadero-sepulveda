package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.api.service.FacturacionApiService;
import com.lavaderosepulveda.crm.model.dto.FacturaEmitidaDTO;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import lombok.extern.slf4j.Slf4j;

// iText 7/8 imports
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

// POI imports
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.util.CellRangeAddress;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public class ContabilidadController implements Initializable {

    public ContabilidadController() {
        // Constructor vacío requerido por JavaFX
    }

    @FXML
    private ComboBox<String> comboPeriodo;
    @FXML
    private DatePicker dpDesde;
    @FXML
    private DatePicker dpHasta;
    @FXML
    private Button btnGenerar;
    @FXML
    private Button btnGenerarReporte;
    @FXML
    private Button btnExportarExcel;

    @FXML
    private Label lblIngresosTotales;
    @FXML
    private Label lblIvaRepercutido;
    @FXML
    private Label lblBaseImponible;
    @FXML
    private Label lblNumFacturas;

    @FXML
    private TabPane tabPane;
    @FXML
    private TableView<ResumenMensual> tablaResumenMensual;
    @FXML
    private TableColumn<ResumenMensual, String> colMes;
    @FXML
    private TableColumn<ResumenMensual, String> colFacturasMes;
    @FXML
    private TableColumn<ResumenMensual, String> colBaseMes;
    @FXML
    private TableColumn<ResumenMensual, String> colIvaMes;
    @FXML
    private TableColumn<ResumenMensual, String> colTotalMes;

    @FXML
    private TableView<ResumenCliente> tablaPorCliente;
    @FXML
    private TableColumn<ResumenCliente, String> colCliente;
    @FXML
    private TableColumn<ResumenCliente, String> colFacturasCliente;
    @FXML
    private TableColumn<ResumenCliente, String> colTotalCliente;

    @FXML
    private TableView<ResumenServicio> tablaPorServicio;
    @FXML
    private TableColumn<ResumenServicio, String> colServicio;
    @FXML
    private TableColumn<ResumenServicio, String> colCantidadServicio;
    @FXML
    private TableColumn<ResumenServicio, String> colTotalServicio;

    private FacturacionApiService facturacionService;
    private NumberFormat currencyFormat;
    private DateTimeFormatter monthFormatter;
    private DateTimeFormatter dateFormatter;
    private List<FacturaEmitidaDTO> facturasEmitidas;

    // Colores para PDF (iText 7/8)
    private static final DeviceRgb COLOR_VERDE = new DeviceRgb(46, 125, 50);
    private static final DeviceRgb COLOR_AZUL = new DeviceRgb(21, 101, 192);
    private static final DeviceRgb COLOR_AZUL_CLARO = new DeviceRgb(227, 242, 253);
    private static final DeviceRgb COLOR_VERDE_CLARO = new DeviceRgb(232, 245, 233);
    private static final DeviceRgb COLOR_NARANJA = new DeviceRgb(230, 81, 0);
    private static final DeviceRgb COLOR_NARANJA_CLARO = new DeviceRgb(255, 243, 224);
    private static final DeviceRgb COLOR_GRIS = new DeviceRgb(100, 100, 100);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Inicializando ContabilidadController...");
        try {
            facturacionService = FacturacionApiService.getInstance();
            currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
            monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"));
            dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            facturasEmitidas = new ArrayList<>();

            configurarFiltros();
            configurarTablas();
            Platform.runLater(this::cargarDatos);
            log.info("ContabilidadController inicializado correctamente");
        } catch (Exception e) {
            log.error("Error inicializando ContabilidadController", e);
        }
    }

    private void configurarFiltros() {
        if (comboPeriodo != null) {
            ObservableList<String> periodos = FXCollections.observableArrayList(
                    "Este mes", "Mes anterior", "Este trimestre", "Este año", "Personalizado");
            comboPeriodo.setItems(periodos);
            comboPeriodo.getSelectionModel().selectFirst();
            comboPeriodo.setOnAction(e -> actualizarFechasSegunPeriodo(comboPeriodo.getValue()));
        }

        LocalDate hoy = LocalDate.now();
        if (dpDesde != null)
            dpDesde.setValue(hoy.with(TemporalAdjusters.firstDayOfMonth()));
        if (dpHasta != null)
            dpHasta.setValue(hoy.with(TemporalAdjusters.lastDayOfMonth()));

        actualizarEstadoFechas();
    }

    private void actualizarFechasSegunPeriodo(String periodo) {
        if (periodo == null)
            return;
        LocalDate hoy = LocalDate.now();

        switch (periodo) {
            case "Este mes":
                if (dpDesde != null)
                    dpDesde.setValue(hoy.with(TemporalAdjusters.firstDayOfMonth()));
                if (dpHasta != null)
                    dpHasta.setValue(hoy.with(TemporalAdjusters.lastDayOfMonth()));
                break;
            case "Mes anterior":
                LocalDate mesAnterior = hoy.minusMonths(1);
                if (dpDesde != null)
                    dpDesde.setValue(mesAnterior.with(TemporalAdjusters.firstDayOfMonth()));
                if (dpHasta != null)
                    dpHasta.setValue(mesAnterior.with(TemporalAdjusters.lastDayOfMonth()));
                break;
            case "Este trimestre":
                int mesActual = hoy.getMonthValue();
                int primerMesTrimestre = ((mesActual - 1) / 3) * 3 + 1;
                LocalDate inicioTrimestre = LocalDate.of(hoy.getYear(), primerMesTrimestre, 1);
                if (dpDesde != null)
                    dpDesde.setValue(inicioTrimestre);
                if (dpHasta != null)
                    dpHasta.setValue(inicioTrimestre.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth()));
                break;
            case "Este año":
                if (dpDesde != null)
                    dpDesde.setValue(LocalDate.of(hoy.getYear(), 1, 1));
                if (dpHasta != null)
                    dpHasta.setValue(LocalDate.of(hoy.getYear(), 12, 31));
                break;
            default:
                break;
        }
        actualizarEstadoFechas();
    }

    private void actualizarEstadoFechas() {
        boolean esPersonalizado = comboPeriodo != null && "Personalizado".equals(comboPeriodo.getValue());
        if (dpDesde != null)
            dpDesde.setDisable(!esPersonalizado);
        if (dpHasta != null)
            dpHasta.setDisable(!esPersonalizado);
    }

    private void configurarTablas() {
        if (colMes != null) {
            colMes.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMes()));
        }
        if (colFacturasMes != null) {
            colFacturasMes.setCellValueFactory(
                    data -> new SimpleStringProperty(String.valueOf(data.getValue().getNumFacturas())));
        }
        if (colBaseMes != null) {
            colBaseMes.setCellValueFactory(
                    data -> new SimpleStringProperty(currencyFormat.format(data.getValue().getBase())));
        }
        if (colIvaMes != null) {
            colIvaMes.setCellValueFactory(
                    data -> new SimpleStringProperty(currencyFormat.format(data.getValue().getIva())));
        }
        if (colTotalMes != null) {
            colTotalMes.setCellValueFactory(
                    data -> new SimpleStringProperty(currencyFormat.format(data.getValue().getTotal())));
        }

        if (colCliente != null) {
            colCliente.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreCliente()));
        }
        if (colFacturasCliente != null) {
            colFacturasCliente.setCellValueFactory(
                    data -> new SimpleStringProperty(String.valueOf(data.getValue().getNumFacturas())));
        }
        if (colTotalCliente != null) {
            colTotalCliente.setCellValueFactory(
                    data -> new SimpleStringProperty(currencyFormat.format(data.getValue().getTotal())));
        }

        if (colServicio != null) {
            colServicio.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreServicio()));
        }
        if (colCantidadServicio != null) {
            colCantidadServicio.setCellValueFactory(
                    data -> new SimpleStringProperty(String.valueOf(data.getValue().getCantidad())));
        }
        if (colTotalServicio != null) {
            colTotalServicio.setCellValueFactory(
                    data -> new SimpleStringProperty(currencyFormat.format(data.getValue().getTotal())));
        }
    }

    @FXML
    private void cargarDatos() {
        LocalDate desde = dpDesde != null ? dpDesde.getValue() : LocalDate.now().withDayOfMonth(1);
        LocalDate hasta = dpHasta != null ? dpHasta.getValue() : LocalDate.now();

        if (desde == null || hasta == null) {
            mostrarAlerta("Error", "Debe seleccionar un rango de fechas", Alert.AlertType.WARNING);
            return;
        }

        log.info("Cargando datos de contabilidad desde {} hasta {}", desde, hasta);
        final LocalDate desdeF = desde;
        final LocalDate hastaF = hasta;

        CompletableFuture.runAsync(() -> {
            try {
                facturasEmitidas = facturacionService.obtenerFacturasEmitidas();

                List<FacturaEmitidaDTO> facturasFiltradas = facturasEmitidas.stream()
                        .filter(f -> {
                            LocalDate fechaFactura = parseFecha(f.getFechaEmision());
                            return fechaFactura != null && !fechaFactura.isBefore(desdeF)
                                    && !fechaFactura.isAfter(hastaF);
                        })
                        .collect(Collectors.toList());

                Platform.runLater(() -> {
                    actualizarTarjetas(facturasFiltradas);
                    actualizarTablaResumenMensual(facturasFiltradas);
                    actualizarTablaPorCliente(facturasFiltradas);
                });

            } catch (Exception e) {
                log.error("Error cargando datos de contabilidad", e);
                Platform.runLater(() -> mostrarAlerta("Error", "No se pudieron cargar los datos: " + e.getMessage(),
                        Alert.AlertType.ERROR));
            }
        });
    }

    private LocalDate parseFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.isEmpty())
            return null;
        try {
            if (fechaStr.contains("T")) {
                return LocalDate.parse(fechaStr.substring(0, 10));
            } else if (fechaStr.contains("/")) {
                return LocalDate.parse(fechaStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else {
                return LocalDate.parse(fechaStr);
            }
        } catch (Exception e) {
            log.warn("No se pudo parsear la fecha: '{}' con formatos soportados (ISO, dd/MM/yyyy)", fechaStr, e);
            return null;
        }
    }

    private void actualizarTarjetas(List<FacturaEmitidaDTO> facturas) {
        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;
        BigDecimal totalBase = BigDecimal.ZERO;

        for (FacturaEmitidaDTO factura : facturas) {
            if (factura.getTotal() != null)
                totalIngresos = totalIngresos.add(factura.getTotal());
            if (factura.getCuotaIva() != null)
                totalIva = totalIva.add(factura.getCuotaIva());
            if (factura.getBaseImponible() != null)
                totalBase = totalBase.add(factura.getBaseImponible());
        }

        if (lblIngresosTotales != null)
            lblIngresosTotales.setText(currencyFormat.format(totalIngresos));
        if (lblIvaRepercutido != null)
            lblIvaRepercutido.setText(currencyFormat.format(totalIva));
        if (lblBaseImponible != null)
            lblBaseImponible.setText(currencyFormat.format(totalBase));
        if (lblNumFacturas != null)
            lblNumFacturas.setText(String.valueOf(facturas.size()));
    }

    private void actualizarTablaResumenMensual(List<FacturaEmitidaDTO> facturas) {
        if (tablaResumenMensual == null)
            return;

        Map<YearMonth, List<FacturaEmitidaDTO>> porMes = facturas.stream()
                .filter(f -> parseFecha(f.getFechaEmision()) != null)
                .collect(Collectors.groupingBy(f -> YearMonth.from(parseFecha(f.getFechaEmision()))));

        List<ResumenMensual> resumenes = new ArrayList<>();
        for (Map.Entry<YearMonth, List<FacturaEmitidaDTO>> entry : porMes.entrySet()) {
            YearMonth mes = entry.getKey();
            List<FacturaEmitidaDTO> facturasDelMes = entry.getValue();

            BigDecimal base = facturasDelMes.stream()
                    .map(f -> f.getBaseImponible() != null ? f.getBaseImponible() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal iva = facturasDelMes.stream()
                    .map(f -> f.getCuotaIva() != null ? f.getCuotaIva() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal total = facturasDelMes.stream()
                    .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            resumenes.add(new ResumenMensual(mes.format(monthFormatter), mes, facturasDelMes.size(), base, iva, total));
        }

        resumenes.sort((a, b) -> b.getYearMonth().compareTo(a.getYearMonth()));
        tablaResumenMensual.setItems(FXCollections.observableArrayList(resumenes));
    }

    private void actualizarTablaPorCliente(List<FacturaEmitidaDTO> facturas) {
        if (tablaPorCliente == null)
            return;

        Map<String, List<FacturaEmitidaDTO>> porCliente = facturas.stream()
                .collect(Collectors
                        .groupingBy(f -> f.getClienteNombre() != null ? f.getClienteNombre() : "Sin cliente"));

        List<ResumenCliente> resumenes = new ArrayList<>();
        for (Map.Entry<String, List<FacturaEmitidaDTO>> entry : porCliente.entrySet()) {
            String cliente = entry.getKey();
            List<FacturaEmitidaDTO> facturasCliente = entry.getValue();

            BigDecimal total = facturasCliente.stream()
                    .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            resumenes.add(new ResumenCliente(cliente, facturasCliente.size(), total));
        }

        resumenes.sort((a, b) -> b.getTotal().compareTo(a.getTotal()));
        tablaPorCliente.setItems(FXCollections.observableArrayList(resumenes));
    }

    // ========================================
    // GENERACIÓN DE REPORTE PDF
    // ========================================

    @FXML
    private void generarReporte() {
        if (tablaResumenMensual == null || tablaResumenMensual.getItems().isEmpty()) {
            mostrarAlerta("Aviso", "No hay datos para generar el reporte. Cargue los datos primero.",
                    Alert.AlertType.WARNING);
            return;
        }

        if (dpDesde == null || dpDesde.getValue() == null || dpHasta == null || dpHasta.getValue() == null) {
            mostrarAlerta("Error", "Debe seleccionar un rango de fechas válido", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte de Contabilidad");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fileChooser.setInitialFileName("reporte_contabilidad_" +
                dpDesde.getValue().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" +
                dpHasta.getValue().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

        File file = fileChooser.showSaveDialog(getStage());

        if (file != null) {
            try {
                crearPDFReporte(file);

                Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                confirmacion.initOwner(getStage());
                confirmacion.setTitle("PDF Generado");
                confirmacion.setHeaderText("Reporte de contabilidad guardado correctamente");
                confirmacion.setContentText("¿Desea abrir el PDF ahora?");

                Optional<ButtonType> respuesta = confirmacion.showAndWait();
                if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file);
                    }
                }

            } catch (Exception e) {
                log.error("Error generando PDF", e);
                mostrarAlerta("Error", "Error al generar el PDF: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Crea el documento PDF con el reporte de contabilidad (iText 7/8)
     */
    private void crearPDFReporte(File file) throws Exception {
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // ========================================
        // TÍTULO
        // ========================================
        document.add(new Paragraph("REPORTE DE CONTABILIDAD")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.DARK_GRAY));

        document.add(new Paragraph("Período: " +
                dpDesde.getValue().format(dateFormatter) + " - " +
                dpHasta.getValue().format(dateFormatter))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));

        document.add(new Paragraph("Lavadero Sepúlveda")
                .setFontSize(12)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // ========================================
        // TARJETAS RESUMEN
        // ========================================
        Table tablaResumen = new Table(UnitValue.createPercentArray(4)).useAllAvailableWidth();

        tablaResumen.addCell(crearCeldaResumen("INGRESOS TOTALES",
                lblIngresosTotales != null ? lblIngresosTotales.getText() : "0,00 €",
                COLOR_VERDE_CLARO, COLOR_VERDE));
        tablaResumen.addCell(crearCeldaResumen("BASE IMPONIBLE",
                lblBaseImponible != null ? lblBaseImponible.getText() : "0,00 €",
                COLOR_AZUL_CLARO, COLOR_AZUL));
        tablaResumen.addCell(crearCeldaResumen("IVA REPERCUTIDO",
                lblIvaRepercutido != null ? lblIvaRepercutido.getText() : "0,00 €",
                COLOR_NARANJA_CLARO, COLOR_NARANJA));
        tablaResumen.addCell(crearCeldaResumen("Nº FACTURAS",
                lblNumFacturas != null ? lblNumFacturas.getText() : "0",
                COLOR_AZUL_CLARO, COLOR_AZUL));

        document.add(tablaResumen);
        document.add(new Paragraph(" "));

        // ========================================
        // RESUMEN MENSUAL
        // ========================================
        document.add(new Paragraph("RESUMEN MENSUAL")
                .setFontSize(12)
                .setBold()
                .setFontColor(ColorConstants.DARK_GRAY));

        Table tablaMensual = new Table(UnitValue.createPercentArray(new float[] { 3, 1, 2, 2, 2 }))
                .useAllAvailableWidth();

        tablaMensual.addHeaderCell(crearCeldaCabecera("Mes"));
        tablaMensual.addHeaderCell(crearCeldaCabecera("Facturas"));
        tablaMensual.addHeaderCell(crearCeldaCabecera("Base"));
        tablaMensual.addHeaderCell(crearCeldaCabecera("IVA"));
        tablaMensual.addHeaderCell(crearCeldaCabecera("Total"));

        BigDecimal totalBase = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;
        BigDecimal totalGeneral = BigDecimal.ZERO;
        int totalFacturas = 0;

        for (ResumenMensual rm : tablaResumenMensual.getItems()) {
            tablaMensual.addCell(new Cell().add(new Paragraph(rm.getMes()).setFontSize(9)));
            tablaMensual.addCell(new Cell().add(new Paragraph(String.valueOf(rm.getNumFacturas())).setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER)));
            tablaMensual.addCell(new Cell().add(new Paragraph(currencyFormat.format(rm.getBase())).setFontSize(9)
                    .setTextAlignment(TextAlignment.RIGHT)));
            tablaMensual.addCell(new Cell().add(new Paragraph(currencyFormat.format(rm.getIva())).setFontSize(9)
                    .setTextAlignment(TextAlignment.RIGHT)));
            tablaMensual.addCell(new Cell().add(new Paragraph(currencyFormat.format(rm.getTotal())).setFontSize(9)
                    .setTextAlignment(TextAlignment.RIGHT)));

            totalBase = totalBase.add(rm.getBase());
            totalIva = totalIva.add(rm.getIva());
            totalGeneral = totalGeneral.add(rm.getTotal());
            totalFacturas += rm.getNumFacturas();
        }

        // Fila de totales
        tablaMensual.addCell(
                new Cell().add(new Paragraph("TOTAL").setFontSize(9).setBold()).setBackgroundColor(COLOR_VERDE_CLARO));
        tablaMensual.addCell(new Cell().add(new Paragraph(String.valueOf(totalFacturas)).setFontSize(9).setBold()
                .setTextAlignment(TextAlignment.CENTER)).setBackgroundColor(COLOR_VERDE_CLARO));
        tablaMensual.addCell(new Cell().add(new Paragraph(currencyFormat.format(totalBase)).setFontSize(9).setBold()
                .setTextAlignment(TextAlignment.RIGHT)).setBackgroundColor(COLOR_VERDE_CLARO));
        tablaMensual.addCell(new Cell().add(new Paragraph(currencyFormat.format(totalIva)).setFontSize(9).setBold()
                .setTextAlignment(TextAlignment.RIGHT)).setBackgroundColor(COLOR_VERDE_CLARO));
        tablaMensual.addCell(new Cell().add(new Paragraph(currencyFormat.format(totalGeneral)).setFontSize(9).setBold()
                .setTextAlignment(TextAlignment.RIGHT)).setBackgroundColor(COLOR_VERDE_CLARO));

        document.add(tablaMensual);
        document.add(new Paragraph(" "));

        // ========================================
        // RESUMEN POR CLIENTE
        // ========================================
        if (tablaPorCliente != null && !tablaPorCliente.getItems().isEmpty()) {
            document.add(new Paragraph("FACTURACIÓN POR CLIENTE")
                    .setFontSize(12)
                    .setBold()
                    .setFontColor(ColorConstants.DARK_GRAY));

            Table tablaClientes = new Table(UnitValue.createPercentArray(new float[] { 4, 1, 2 }))
                    .useAllAvailableWidth();

            tablaClientes.addHeaderCell(crearCeldaCabecera("Cliente"));
            tablaClientes.addHeaderCell(crearCeldaCabecera("Facturas"));
            tablaClientes.addHeaderCell(crearCeldaCabecera("Total"));

            BigDecimal totalClientes = BigDecimal.ZERO;
            int facturasClientes = 0;

            for (ResumenCliente rc : tablaPorCliente.getItems()) {
                tablaClientes.addCell(new Cell().add(new Paragraph(rc.getNombreCliente()).setFontSize(9)));
                tablaClientes.addCell(new Cell().add(new Paragraph(String.valueOf(rc.getNumFacturas())).setFontSize(9)
                        .setTextAlignment(TextAlignment.CENTER)));
                tablaClientes.addCell(new Cell().add(new Paragraph(currencyFormat.format(rc.getTotal())).setFontSize(9)
                        .setTextAlignment(TextAlignment.RIGHT)));

                totalClientes = totalClientes.add(rc.getTotal());
                facturasClientes += rc.getNumFacturas();
            }

            // Fila de totales
            tablaClientes.addCell(new Cell().add(new Paragraph("TOTAL").setFontSize(9).setBold())
                    .setBackgroundColor(COLOR_VERDE_CLARO));
            tablaClientes
                    .addCell(
                            new Cell()
                                    .add(new Paragraph(String.valueOf(facturasClientes)).setFontSize(9).setBold()
                                            .setTextAlignment(TextAlignment.CENTER))
                                    .setBackgroundColor(COLOR_VERDE_CLARO));
            tablaClientes
                    .addCell(
                            new Cell()
                                    .add(new Paragraph(currencyFormat.format(totalClientes)).setFontSize(9).setBold()
                                            .setTextAlignment(TextAlignment.RIGHT))
                                    .setBackgroundColor(COLOR_VERDE_CLARO));

            document.add(tablaClientes);
        }

        // ========================================
        // PIE DE PÁGINA
        // ========================================
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Documento generado el " +
                LocalDate.now().format(dateFormatter) + " - Lavadero Sepúlveda CRM")
                .setFontSize(8)
                .setFontColor(COLOR_GRIS)
                .setTextAlignment(TextAlignment.CENTER));

        document.close();
        log.info("PDF de contabilidad generado: {}", file.getAbsolutePath());
    }

    /**
     * Crea una celda de resumen con estilo (iText 7/8)
     */
    private Cell crearCeldaResumen(String titulo, String valor, DeviceRgb bgColor, DeviceRgb fontColor) {
        Cell celda = new Cell()
                .setBackgroundColor(bgColor)
                .setPadding(8)
                .setTextAlignment(TextAlignment.CENTER);

        celda.add(new Paragraph(titulo)
                .setFontSize(8)
                .setBold()
                .setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        celda.add(new Paragraph(valor)
                .setFontSize(11)
                .setBold()
                .setFontColor(fontColor)
                .setTextAlignment(TextAlignment.CENTER));

        return celda;
    }

    /**
     * Crea una celda de cabecera (iText 7/8)
     */
    private Cell crearCeldaCabecera(String texto) {
        return new Cell()
                .add(new Paragraph(texto).setFontSize(9).setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(COLOR_AZUL)
                .setPadding(5)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private Stage getStage() {
        if (btnGenerarReporte != null && btnGenerarReporte.getScene() != null) {
            return (Stage) btnGenerarReporte.getScene().getWindow();
        }
        if (btnExportarExcel != null && btnExportarExcel.getScene() != null) {
            return (Stage) btnExportarExcel.getScene().getWindow();
        }
        if (comboPeriodo != null && comboPeriodo.getScene() != null) {
            return (Stage) comboPeriodo.getScene().getWindow();
        }
        return null;
    }

    @FXML
    private void exportarExcel() {
        if (tablaResumenMensual == null || tablaResumenMensual.getItems().isEmpty()) {
            mostrarAlerta("Aviso", "No hay datos para exportar. Cargue los datos primero.", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName(
                "contabilidad_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");

        Stage stage = getStage();
        if (stage == null)
            return;

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                // Crear estilos
                CellStyle styleHeader = workbook.createCellStyle();
                Font fontHeader = workbook.createFont();
                fontHeader.setBold(true);
                fontHeader.setColor(IndexedColors.WHITE.getIndex());
                styleHeader.setFont(fontHeader);
                styleHeader.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
                styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                styleHeader.setAlignment(HorizontalAlignment.CENTER);

                CellStyle styleTitle = workbook.createCellStyle();
                Font fontTitle = workbook.createFont();
                fontTitle.setBold(true);
                fontTitle.setFontHeightInPoints((short) 14);
                styleTitle.setFont(fontTitle);

                CellStyle styleCurrency = workbook.createCellStyle();
                styleCurrency.setDataFormat(workbook.createDataFormat().getFormat("\"€\" #,##0.00"));

                // ========================================
                // HOJA 1: RESUMEN GENERAL
                // ========================================
                Sheet sheetResumen = workbook.createSheet("Resumen General");
                int rowNum = 0;

                Row titleRow = sheetResumen.createRow(rowNum++);
                org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("RESUMEN CONTABLE - LAVADERO SEPÚLVEDA");
                titleCell.setCellStyle(styleTitle);
                sheetResumen.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

                rowNum++; // Línea en blanco

                Row periodRow = sheetResumen.createRow(rowNum++);
                periodRow.createCell(0).setCellValue("Período:");
                periodRow.createCell(1).setCellValue(dpDesde != null && dpDesde.getValue() != null ? 
                    dpDesde.getValue().format(dateFormatter) : "N/A");
                periodRow.createCell(2).setCellValue("hasta");
                periodRow.createCell(3).setCellValue(dpHasta != null && dpHasta.getValue() != null ? 
                    dpHasta.getValue().format(dateFormatter) : "N/A");

                rowNum++; // Línea en blanco

                // Tarjetas de resumen
                Row headerResumen = sheetResumen.createRow(rowNum++);
                String[] headersResumen = {"Ingresos Totales", "Base Imponible", "IVA Repercutido", "Nº Facturas"};
                for (int i = 0; i < headersResumen.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = headerResumen.createCell(i);
                    cell.setCellValue(headersResumen[i]);
                    cell.setCellStyle(styleHeader);
                }

                Row dataResumen = sheetResumen.createRow(rowNum++);
                dataResumen.createCell(0).setCellValue(lblIngresosTotales != null ? lblIngresosTotales.getText() : "0,00 €");
                dataResumen.createCell(1).setCellValue(lblBaseImponible != null ? lblBaseImponible.getText() : "0,00 €");
                dataResumen.createCell(2).setCellValue(lblIvaRepercutido != null ? lblIvaRepercutido.getText() : "0,00 €");
                dataResumen.createCell(3).setCellValue(lblNumFacturas != null ? lblNumFacturas.getText() : "0");

                sheetResumen.autoSizeColumn(0);
                sheetResumen.autoSizeColumn(1);
                sheetResumen.autoSizeColumn(2);
                sheetResumen.autoSizeColumn(3);

                // ========================================
                // HOJA 2: RESUMEN MENSUAL
                // ========================================
                Sheet sheetMensual = workbook.createSheet("Resumen Mensual");
                rowNum = 0;

                Row headerMensual = sheetMensual.createRow(rowNum++);
                String[] headersMensual = {"Mes", "Facturas", "Base Imponible", "IVA", "Total"};
                for (int i = 0; i < headersMensual.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = headerMensual.createCell(i);
                    cell.setCellValue(headersMensual[i]);
                    cell.setCellStyle(styleHeader);
                }

                BigDecimal totalBaseMensual = BigDecimal.ZERO;
                BigDecimal totalIvaMensual = BigDecimal.ZERO;
                BigDecimal totalGeneralMensual = BigDecimal.ZERO;
                int totalFacturasMensual = 0;

                for (ResumenMensual rm : tablaResumenMensual.getItems()) {
                    Row row = sheetMensual.createRow(rowNum++);
                    row.createCell(0).setCellValue(rm.getMes());
                    row.createCell(1).setCellValue(rm.getNumFacturas());
                    row.createCell(2).setCellValue(rm.getBase().doubleValue());
                    row.getCell(2).setCellStyle(styleCurrency);
                    row.createCell(3).setCellValue(rm.getIva().doubleValue());
                    row.getCell(3).setCellStyle(styleCurrency);
                    row.createCell(4).setCellValue(rm.getTotal().doubleValue());
                    row.getCell(4).setCellStyle(styleCurrency);

                    totalBaseMensual = totalBaseMensual.add(rm.getBase());
                    totalIvaMensual = totalIvaMensual.add(rm.getIva());
                    totalGeneralMensual = totalGeneralMensual.add(rm.getTotal());
                    totalFacturasMensual += rm.getNumFacturas();
                }

                // Fila de totales
                Row totalRow = sheetMensual.createRow(rowNum++);
                org.apache.poi.ss.usermodel.Cell cellTotalLabel = totalRow.createCell(0);
                cellTotalLabel.setCellValue("TOTAL");
                cellTotalLabel.setCellStyle(styleHeader);
                totalRow.createCell(1).setCellValue(totalFacturasMensual);
                totalRow.getCell(1).setCellStyle(styleHeader);
                totalRow.createCell(2).setCellValue(totalBaseMensual.doubleValue());
                totalRow.getCell(2).setCellStyle(styleHeader);
                totalRow.createCell(3).setCellValue(totalIvaMensual.doubleValue());
                totalRow.getCell(3).setCellStyle(styleHeader);
                totalRow.createCell(4).setCellValue(totalGeneralMensual.doubleValue());
                totalRow.getCell(4).setCellStyle(styleHeader);

                for (int i = 0; i < headersMensual.length; i++) {
                    sheetMensual.autoSizeColumn(i);
                }

                // ========================================
                // HOJA 3: FACTURACIÓN POR CLIENTE
                // ========================================
                if (tablaPorCliente != null && !tablaPorCliente.getItems().isEmpty()) {
                    Sheet sheetClientes = workbook.createSheet("Por Cliente");
                    rowNum = 0;

                    Row headerClientes = sheetClientes.createRow(rowNum++);
                    String[] headersClientes = {"Cliente", "Facturas", "Total"};
                    for (int i = 0; i < headersClientes.length; i++) {
                        org.apache.poi.ss.usermodel.Cell cell = headerClientes.createCell(i);
                        cell.setCellValue(headersClientes[i]);
                        cell.setCellStyle(styleHeader);
                    }

                    BigDecimal totalClientesAmount = BigDecimal.ZERO;
                    int totalClientesFacturas = 0;

                    for (ResumenCliente rc : tablaPorCliente.getItems()) {
                        Row row = sheetClientes.createRow(rowNum++);
                        row.createCell(0).setCellValue(rc.getNombreCliente());
                        row.createCell(1).setCellValue(rc.getNumFacturas());
                        row.createCell(2).setCellValue(rc.getTotal().doubleValue());
                        row.getCell(2).setCellStyle(styleCurrency);

                        totalClientesAmount = totalClientesAmount.add(rc.getTotal());
                        totalClientesFacturas += rc.getNumFacturas();
                    }

                    // Fila de totales
                    Row totalClientesRow = sheetClientes.createRow(rowNum++);
                    org.apache.poi.ss.usermodel.Cell cellTotalClientesLabel = totalClientesRow.createCell(0);
                    cellTotalClientesLabel.setCellValue("TOTAL");
                    cellTotalClientesLabel.setCellStyle(styleHeader);
                    totalClientesRow.createCell(1).setCellValue(totalClientesFacturas);
                    totalClientesRow.getCell(1).setCellStyle(styleHeader);
                    totalClientesRow.createCell(2).setCellValue(totalClientesAmount.doubleValue());
                    totalClientesRow.getCell(2).setCellStyle(styleHeader);

                    for (int i = 0; i < headersClientes.length; i++) {
                        sheetClientes.autoSizeColumn(i);
                    }
                }

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
                log.info("Reporte Excel generado: {}", file.getAbsolutePath());
                mostrarAlerta("Éxito", "Reporte exportado correctamente", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                log.error("Error exportando a Excel", e);
                mostrarAlerta("Error", "No se pudo exportar el reporte: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.initOwner(getStage());
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Clases internas
    public static class ResumenMensual {
        private final String mes;
        private final YearMonth yearMonth;
        private final int numFacturas;
        private final BigDecimal base;
        private final BigDecimal iva;
        private final BigDecimal total;

        public ResumenMensual(String mes, YearMonth yearMonth, int numFacturas, BigDecimal base, BigDecimal iva, BigDecimal total) {
            this.mes = mes;
            this.yearMonth = yearMonth;
            this.numFacturas = numFacturas;
            this.base = base;
            this.iva = iva;
            this.total = total;
        }

        public String getMes() {
            return mes;
        }

        public YearMonth getYearMonth() {
            return yearMonth;
        }

        public int getNumFacturas() {
            return numFacturas;
        }

        public BigDecimal getBase() {
            return base;
        }

        public BigDecimal getIva() {
            return iva;
        }

        public BigDecimal getTotal() {
            return total;
        }
    }

    public static class ResumenCliente {
        private final String nombreCliente;
        private final int numFacturas;
        private final BigDecimal total;

        public ResumenCliente(String nombreCliente, int numFacturas, BigDecimal total) {
            this.nombreCliente = nombreCliente;
            this.numFacturas = numFacturas;
            this.total = total;
        }

        public String getNombreCliente() {
            return nombreCliente;
        }

        public int getNumFacturas() {
            return numFacturas;
        }

        public BigDecimal getTotal() {
            return total;
        }
    }

    public static class ResumenServicio {
        private final String nombreServicio;
        private final int cantidad;
        private final BigDecimal total;

        public ResumenServicio(String nombreServicio, int cantidad, BigDecimal total) {
            this.nombreServicio = nombreServicio;
            this.cantidad = cantidad;
            this.total = total;
        }

        public String getNombreServicio() {
            return nombreServicio;
        }

        public int getCantidad() {
            return cantidad;
        }

        public BigDecimal getTotal() {
            return total;
        }
    }
}

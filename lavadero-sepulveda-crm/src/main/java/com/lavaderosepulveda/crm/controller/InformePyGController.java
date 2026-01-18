package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.service.InformePyGService;
import com.lavaderosepulveda.crm.service.InformePyGService.InformePyG;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Controlador para el Informe de Pérdidas y Ganancias
 */
@Slf4j
public class InformePyGController implements Initializable {

    // Filtros
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private Label lblPeriodo;
    @FXML private Label lblEjercicio;

    // Resumen
    @FXML private Label lblTotalIngresos;
    @FXML private Label lblTotalGastos;
    @FXML private Label lblResultadoFinal;
    @FXML private Label lblTituloResultado;
    @FXML private Label lblMargen;
    @FXML private Label lblNumIngresos;
    @FXML private Label lblNumGastos;
    @FXML private VBox cardResultado;

    // Detalle ingresos
    @FXML private Label lblVentasNetas;
    @FXML private Label lblVentasDetalle;
    @FXML private Label lblOtrosIngresos;

    // Detalle gastos
    @FXML private Label lblAprovisionamientos;
    @FXML private Label lblGastosPersonal;
    @FXML private Label lblSueldos;
    @FXML private Label lblCargasSociales;
    @FXML private Label lblOtrosGastosExplotacion;
    @FXML private Label lblServiciosExteriores;
    @FXML private Label lblTributos;
    @FXML private Label lblOtrosGastosGestion;
    @FXML private Label lblAmortizacion;

    // Resultados
    @FXML private Label lblResultadoExplotacion;
    @FXML private Label lblIngresosFinancieros;
    @FXML private Label lblGastosFinancieros;
    @FXML private Label lblResultadoFinanciero;
    @FXML private Label lblResultadoAntesImpuestos;
    @FXML private Label lblImpuestoBeneficios;
    @FXML private Label lblResultadoEjercicio;
    @FXML private HBox boxResultadoEjercicio;

    // Desglose
    @FXML private TableView<Map.Entry<String, BigDecimal>> tablaGastosPorCategoria;
    @FXML private TableColumn<Map.Entry<String, BigDecimal>, String> colCategoria;
    @FXML private TableColumn<Map.Entry<String, BigDecimal>, String> colImporteCategoria;
    @FXML private TableColumn<Map.Entry<String, BigDecimal>, String> colPorcentajeCategoria;
    @FXML private PieChart chartGastos;

    // Servicios
    private InformePyGService informeService;
    private InformePyG informeActual;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public InformePyGController() {
        // Constructor vacío requerido para FXML
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Inicializando InformePyGController...");

        informeService = InformePyGService.getInstance();

        configurarFiltros();
        configurarTabla();

        // Generar informe inicial (año actual)
        generarInforme();

        log.info("InformePyGController inicializado");
    }

    private void configurarFiltros() {
        int año = LocalDate.now().getYear();
        dpDesde.setValue(LocalDate.of(año, 1, 1));
        dpHasta.setValue(LocalDate.now());
    }

    private void configurarTabla() {
        if (colCategoria != null) {
            colCategoria.setCellValueFactory(data -> 
                    new SimpleStringProperty(formatearCategoria(data.getValue().getKey())));
        }
        
        if (colImporteCategoria != null) {
            colImporteCategoria.setCellValueFactory(data -> 
                    new SimpleStringProperty(currencyFormat.format(data.getValue().getValue())));
            colImporteCategoria.setStyle("-fx-alignment: CENTER-RIGHT;");
        }

        if (colPorcentajeCategoria != null) {
            colPorcentajeCategoria.setCellValueFactory(data -> {
                if (informeActual != null && informeActual.getTotalGastos().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal porcentaje = data.getValue().getValue()
                            .multiply(new BigDecimal("100"))
                            .divide(informeActual.getTotalGastos(), 1, RoundingMode.HALF_UP);
                    return new SimpleStringProperty(porcentaje + "%");
                }
                return new SimpleStringProperty("0%");
            });
            colPorcentajeCategoria.setStyle("-fx-alignment: CENTER-RIGHT;");
        }
    }

    // ========================================
    // GENERACIÓN DE INFORME
    // ========================================

    @FXML
    private void generarInforme() {
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

        if (desde == null || hasta == null) {
            mostrarAlerta("Aviso", "Seleccione las fechas del período", Alert.AlertType.WARNING);
            return;
        }

        if (desde.isAfter(hasta)) {
            mostrarAlerta("Aviso", "La fecha de inicio debe ser anterior a la fecha fin", Alert.AlertType.WARNING);
            return;
        }

        // Actualizar etiqueta de período
        lblPeriodo.setText("Período: " + desde.format(dateFormatter) + " - " + hasta.format(dateFormatter));
        if (lblEjercicio != null) {
            lblEjercicio.setText("Ejercicio " + desde.getYear());
        }

        CompletableFuture.supplyAsync(() -> informeService.generarInforme(desde, hasta))
                .thenAcceptAsync(informe -> Platform.runLater(() -> mostrarInforme(informe)))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        log.error("Error generando informe", e);
                        mostrarAlerta("Error", "Error al generar el informe: " + e.getMessage(), Alert.AlertType.ERROR);
                    });
                    return null;
                });
    }

    private void mostrarInforme(InformePyG informe) {
        this.informeActual = informe;

        // Resumen
        lblTotalIngresos.setText(currencyFormat.format(informe.getTotalIngresos()));
        lblTotalGastos.setText(currencyFormat.format(informe.getTotalGastos()));
        lblResultadoFinal.setText(currencyFormat.format(informe.getResultadoEjercicio()));
        lblNumIngresos.setText(informe.getNumFacturasEmitidas() + " facturas");
        lblNumGastos.setText(informe.getNumFacturasRecibidas() + " facturas + " + informe.getNumGastos() + " gastos");

        // Margen
        lblMargen.setText("Margen: " + informe.getMargenBeneficio().setScale(1, RoundingMode.HALF_UP) + "%");

        // Estilo según resultado
        if (informe.hayBeneficio()) {
            lblTituloResultado.setText("BENEFICIO");
            lblTituloResultado.setStyle("-fx-font-weight: bold; -fx-text-fill: #2E7D32;");
            lblResultadoFinal.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
            cardResultado.setStyle("-fx-background-color: #E8F5E9; -fx-padding: 20; -fx-background-radius: 8;");
            if (boxResultadoEjercicio != null) {
                boxResultadoEjercicio.setStyle("-fx-background-color: #E8F5E9; -fx-padding: 15; -fx-background-radius: 5; -fx-border-color: #4CAF50; -fx-border-radius: 5;");
            }
        } else if (informe.hayPerdida()) {
            lblTituloResultado.setText("PÉRDIDA");
            lblTituloResultado.setStyle("-fx-font-weight: bold; -fx-text-fill: #C62828;");
            lblResultadoFinal.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #C62828;");
            cardResultado.setStyle("-fx-background-color: #FFEBEE; -fx-padding: 20; -fx-background-radius: 8;");
            if (boxResultadoEjercicio != null) {
                boxResultadoEjercicio.setStyle("-fx-background-color: #FFEBEE; -fx-padding: 15; -fx-background-radius: 5; -fx-border-color: #EF5350; -fx-border-radius: 5;");
            }
        } else {
            lblTituloResultado.setText("RESULTADO");
            lblTituloResultado.setStyle("-fx-font-weight: bold; -fx-text-fill: #1565C0;");
            lblResultadoFinal.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1565C0;");
            cardResultado.setStyle("-fx-background-color: #E3F2FD; -fx-padding: 20; -fx-background-radius: 8;");
        }

        // Detalle ingresos
        lblVentasNetas.setText(currencyFormat.format(informe.getVentasNetas()));
        if (lblVentasDetalle != null) {
            lblVentasDetalle.setText(currencyFormat.format(informe.getVentasNetas()));
        }
        if (lblOtrosIngresos != null) {
            lblOtrosIngresos.setText(currencyFormat.format(informe.getOtrosIngresosExplotacion()));
        }

        // Detalle gastos
        lblAprovisionamientos.setText(formatearGasto(informe.getAprovisionamientos()));
        lblGastosPersonal.setText(formatearGasto(informe.getGastosPersonal()));
        
        // Desglose personal
        BigDecimal sueldos = informe.getGastosPorCategoria().getOrDefault("PERSONAL", BigDecimal.ZERO);
        BigDecimal ss = informe.getGastosPorCategoria().getOrDefault("SEGURIDAD_SOCIAL", BigDecimal.ZERO);
        if (lblSueldos != null) lblSueldos.setText(formatearGasto(sueldos));
        if (lblCargasSociales != null) lblCargasSociales.setText(formatearGasto(ss));

        lblOtrosGastosExplotacion.setText(formatearGasto(informe.getOtrosGastosExplotacion()));
        if (lblServiciosExteriores != null) lblServiciosExteriores.setText(formatearGasto(informe.getServiciosExteriores()));
        if (lblTributos != null) lblTributos.setText(formatearGasto(informe.getTributos()));
        if (lblOtrosGastosGestion != null) lblOtrosGastosGestion.setText(formatearGasto(informe.getOtrosGastosGestion()));
        if (lblAmortizacion != null) lblAmortizacion.setText(formatearGasto(informe.getAmortizacion()));

        // Resultados
        lblResultadoExplotacion.setText(currencyFormat.format(informe.getResultadoExplotacion()));
        colorearResultado(lblResultadoExplotacion, informe.getResultadoExplotacion());

        if (lblIngresosFinancieros != null) lblIngresosFinancieros.setText(currencyFormat.format(informe.getIngresosFinancieros()));
        if (lblGastosFinancieros != null) lblGastosFinancieros.setText(formatearGasto(informe.getGastosFinancieros()));

        lblResultadoFinanciero.setText(currencyFormat.format(informe.getResultadoFinanciero()));
        colorearResultado(lblResultadoFinanciero, informe.getResultadoFinanciero());

        if (lblResultadoAntesImpuestos != null) {
            lblResultadoAntesImpuestos.setText(currencyFormat.format(informe.getResultadoAntesImpuestos()));
            colorearResultado(lblResultadoAntesImpuestos, informe.getResultadoAntesImpuestos());
        }

        if (lblImpuestoBeneficios != null) {
            lblImpuestoBeneficios.setText(currencyFormat.format(informe.getImpuestoBeneficios()));
        }

        lblResultadoEjercicio.setText(currencyFormat.format(informe.getResultadoEjercicio()));
        colorearResultado(lblResultadoEjercicio, informe.getResultadoEjercicio());

        // Tabla de gastos por categoría
        cargarTablaGastos(informe);

        // Gráfico
        cargarGraficoGastos(informe);

        log.info("Informe mostrado - Resultado: {}", informe.getResultadoEjercicio());
    }

    private void cargarTablaGastos(InformePyG informe) {
        if (tablaGastosPorCategoria == null) return;
        
        List<Map.Entry<String, BigDecimal>> entries = new ArrayList<>(informe.getGastosPorCategoria().entrySet());
        entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        tablaGastosPorCategoria.setItems(FXCollections.observableArrayList(entries));
    }

    private void cargarGraficoGastos(InformePyG informe) {
        if (chartGastos == null) return;
        
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        
        List<Map.Entry<String, BigDecimal>> entries = new ArrayList<>(informe.getGastosPorCategoria().entrySet());
        entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        BigDecimal otros = BigDecimal.ZERO;
        int count = 0;
        
        for (Map.Entry<String, BigDecimal> entry : entries) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                if (count < 6) {
                    pieData.add(new PieChart.Data(
                            formatearCategoria(entry.getKey()), 
                            entry.getValue().doubleValue()));
                } else {
                    otros = otros.add(entry.getValue());
                }
                count++;
            }
        }
        
        if (otros.compareTo(BigDecimal.ZERO) > 0) {
            pieData.add(new PieChart.Data("Otros", otros.doubleValue()));
        }
        
        chartGastos.setData(pieData);
        chartGastos.setTitle("Distribución de Gastos");
    }

    // ========================================
    // FILTROS RÁPIDOS
    // ========================================

    @FXML
    private void filtrarEsteMes() {
        LocalDate hoy = LocalDate.now();
        dpDesde.setValue(hoy.withDayOfMonth(1));
        dpHasta.setValue(hoy);
        generarInforme();
    }

    @FXML
    private void filtrarT1() {
        int año = LocalDate.now().getYear();
        dpDesde.setValue(LocalDate.of(año, 1, 1));
        dpHasta.setValue(LocalDate.of(año, 3, 31));
        generarInforme();
    }

    @FXML
    private void filtrarT2() {
        int año = LocalDate.now().getYear();
        dpDesde.setValue(LocalDate.of(año, 4, 1));
        dpHasta.setValue(LocalDate.of(año, 6, 30));
        generarInforme();
    }

    @FXML
    private void filtrarT3() {
        int año = LocalDate.now().getYear();
        dpDesde.setValue(LocalDate.of(año, 7, 1));
        dpHasta.setValue(LocalDate.of(año, 9, 30));
        generarInforme();
    }

    @FXML
    private void filtrarT4() {
        int año = LocalDate.now().getYear();
        dpDesde.setValue(LocalDate.of(año, 10, 1));
        dpHasta.setValue(LocalDate.of(año, 12, 31));
        generarInforme();
    }

    @FXML
    private void filtrarAnio() {
        int año = LocalDate.now().getYear();
        dpDesde.setValue(LocalDate.of(año, 1, 1));
        dpHasta.setValue(LocalDate.of(año, 12, 31));
        generarInforme();
    }

    // ========================================
    // EXPORTACIÓN
    // ========================================

    @FXML
    private void exportarExcel() {
        if (informeActual == null) {
            mostrarAlerta("Aviso", "Genere primero el informe", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Informe PyG");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        fileChooser.setInitialFileName("PyG_" + informeActual.getEjercicio() + ".xlsx");

        Stage stage = (Stage) lblPeriodo.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Pérdidas y Ganancias");

                // Estilos
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setFontHeightInPoints((short) 14);
                headerStyle.setFont(headerFont);

                CellStyle sectionStyle = workbook.createCellStyle();
                Font sectionFont = workbook.createFont();
                sectionFont.setBold(true);
                sectionStyle.setFont(sectionFont);
                sectionStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
                sectionStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                CellStyle currencyStyle = workbook.createCellStyle();
                DataFormat format = workbook.createDataFormat();
                currencyStyle.setDataFormat(format.getFormat("#,##0.00 €"));
                currencyStyle.setAlignment(HorizontalAlignment.RIGHT);

                int rowNum = 0;

                // Título
                Row titleRow = sheet.createRow(rowNum++);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("CUENTA DE PÉRDIDAS Y GANANCIAS");
                titleCell.setCellStyle(headerStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

                // Período
                Row periodoRow = sheet.createRow(rowNum++);
                periodoRow.createCell(0).setCellValue("Período: " + 
                        informeActual.getFechaDesde().format(dateFormatter) + " - " + 
                        informeActual.getFechaHasta().format(dateFormatter));
                rowNum++;

                // Ingresos
                addRowPyG(sheet, rowNum++, "1. Importe neto cifra de negocios", informeActual.getVentasNetas(), sectionStyle, currencyStyle);
                addRowPyG(sheet, rowNum++, "4. Aprovisionamientos", informeActual.getAprovisionamientos().negate(), null, currencyStyle);
                addRowPyG(sheet, rowNum++, "5. Otros ingresos de explotación", informeActual.getOtrosIngresosExplotacion(), null, currencyStyle);
                addRowPyG(sheet, rowNum++, "6. Gastos de personal", informeActual.getGastosPersonal().negate(), null, currencyStyle);
                addRowPyG(sheet, rowNum++, "7. Otros gastos de explotación", informeActual.getOtrosGastosExplotacion().negate(), null, currencyStyle);
                addRowPyG(sheet, rowNum++, "8. Amortización del inmovilizado", informeActual.getAmortizacion().negate(), null, currencyStyle);
                rowNum++;
                addRowPyG(sheet, rowNum++, "A.1) RESULTADO DE EXPLOTACIÓN", informeActual.getResultadoExplotacion(), sectionStyle, currencyStyle);
                rowNum++;
                addRowPyG(sheet, rowNum++, "12. Ingresos financieros", informeActual.getIngresosFinancieros(), null, currencyStyle);
                addRowPyG(sheet, rowNum++, "13. Gastos financieros", informeActual.getGastosFinancieros().negate(), null, currencyStyle);
                rowNum++;
                addRowPyG(sheet, rowNum++, "A.2) RESULTADO FINANCIERO", informeActual.getResultadoFinanciero(), sectionStyle, currencyStyle);
                rowNum++;
                addRowPyG(sheet, rowNum++, "A.3) RESULTADO ANTES DE IMPUESTOS", informeActual.getResultadoAntesImpuestos(), sectionStyle, currencyStyle);
                addRowPyG(sheet, rowNum++, "17. Impuesto sobre beneficios", informeActual.getImpuestoBeneficios().negate(), null, currencyStyle);
                rowNum++;
                addRowPyG(sheet, rowNum++, "A.4) RESULTADO DEL EJERCICIO", informeActual.getResultadoEjercicio(), sectionStyle, currencyStyle);

                // Desglose gastos
                rowNum += 2;
                Row desgloseHeader = sheet.createRow(rowNum++);
                desgloseHeader.createCell(0).setCellValue("DESGLOSE POR CATEGORÍA");
                desgloseHeader.getCell(0).setCellStyle(headerStyle);

                for (Map.Entry<String, BigDecimal> entry : informeActual.getGastosPorCategoria().entrySet()) {
                    if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                        addRowPyG(sheet, rowNum++, formatearCategoria(entry.getKey()), entry.getValue(), null, currencyStyle);
                    }
                }

                // Ajustar anchos
                sheet.setColumnWidth(0, 12000);
                sheet.setColumnWidth(1, 4000);

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }

                mostrarAlerta("Éxito", "Informe exportado correctamente", Alert.AlertType.INFORMATION);

            } catch (Exception e) {
                log.error("Error exportando informe", e);
                mostrarAlerta("Error", "Error al exportar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void addRowPyG(Sheet sheet, int rowNum, String concepto, BigDecimal importe, 
                           CellStyle conceptoStyle, CellStyle importeStyle) {
        Row row = sheet.createRow(rowNum);
        Cell conceptoCell = row.createCell(0);
        conceptoCell.setCellValue(concepto);
        if (conceptoStyle != null) {
            conceptoCell.setCellStyle(conceptoStyle);
        }

        Cell importeCell = row.createCell(1);
        importeCell.setCellValue(importe.doubleValue());
        importeCell.setCellStyle(importeStyle);
    }

    @FXML
    private void imprimir() {
        if (informeActual == null) {
            mostrarAlerta("Aviso", "Genere primero el informe", Alert.AlertType.WARNING);
            return;
        }

        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null && printerJob.showPrintDialog(lblPeriodo.getScene().getWindow())) {
            mostrarAlerta("Información", "Funcionalidad de impresión en desarrollo.\n\nPor ahora, exporte a Excel e imprima desde allí.", Alert.AlertType.INFORMATION);
        }
    }

    // ========================================
    // UTILIDADES
    // ========================================

    private String formatearGasto(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) == 0) {
            return "0,00 €";
        }
        return "(" + currencyFormat.format(valor) + ")";
    }

    private void colorearResultado(Label label, BigDecimal valor) {
        String baseStyle = "-fx-font-weight: bold; -fx-font-size: 14px;";
        if (valor.compareTo(BigDecimal.ZERO) > 0) {
            label.setStyle(baseStyle + " -fx-text-fill: #2E7D32;");
        } else if (valor.compareTo(BigDecimal.ZERO) < 0) {
            label.setStyle(baseStyle + " -fx-text-fill: #C62828;");
        } else {
            label.setStyle(baseStyle);
        }
    }

    private String formatearCategoria(String categoria) {
        if (categoria == null) return "Otros";
        
        return switch (categoria) {
            case "AGUA" -> "Agua";
            case "LUZ" -> "Electricidad";
            case "GAS" -> "Gas";
            case "ALQUILER" -> "Alquiler";
            case "SEGUROS" -> "Seguros";
            case "SUMINISTROS" -> "Suministros";
            case "PRODUCTOS" -> "Productos limpieza";
            case "MANTENIMIENTO" -> "Mantenimiento";
            case "REPARACIONES" -> "Reparaciones";
            case "COMBUSTIBLE" -> "Combustible";
            case "PERSONAL" -> "Personal";
            case "SEGURIDAD_SOCIAL" -> "Seguridad Social";
            case "IMPUESTOS" -> "Impuestos";
            case "TELEFONIA" -> "Telefonía/Internet";
            case "PUBLICIDAD" -> "Publicidad";
            case "MATERIAL_OFICINA" -> "Material oficina";
            case "GESTORIA" -> "Gestoría";
            case "BANCARIOS" -> "Gastos bancarios";
            case "VEHICULOS" -> "Vehículos";
            case "MAQUINARIA" -> "Maquinaria";
            case "OTROS" -> "Otros gastos";
            default -> categoria;
        };
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.model.dto.*;
import com.lavaderosepulveda.crm.service.ExportacionBOEService;
import com.lavaderosepulveda.crm.service.ModelosFiscalesService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * Controlador para la vista de Modelos Fiscales
 * Permite generar y visualizar los modelos 303, 130, 390 y 347
 */
@Slf4j
public class ModelosFiscalesController implements Initializable {

    // ========================================
    // FILTROS Y SELECCIÓN
    // ========================================
    @FXML
    private ComboBox<String> comboModelo;
    @FXML
    private ComboBox<Integer> comboAno;
    @FXML
    private ComboBox<String> comboTrimestre;
    @FXML
    private Button btnGenerar;
    @FXML
    private Button btnExportarExcel;
    @FXML
    private Button btnExportarPDF;
    @FXML
    private Button btnExportarBOE;
    @FXML
    private Label lblTrimestreLabel;

    // ========================================
    // CONTENEDOR DE RESULTADOS
    // ========================================
    @FXML
    private ScrollPane scrollResultados;
    @FXML
    private VBox contenedorResultados;
    @FXML
    private Label lblTituloModelo;
    @FXML
    private Label lblPeriodo;

    // ========================================
    // SERVICIOS Y DATOS
    // ========================================
    private ModelosFiscalesService modelosService;
    private ExportacionBOEService exportacionBOEService;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

    // Constructor vacío requerido por JavaFX FXML
    public ModelosFiscalesController() {
        // Vacío - la inicialización se hace en initialize()
    }

    private Modelo303DTO modelo303Actual;
    private Modelo130DTO modelo130Actual;
    private Modelo390DTO modelo390Actual;
    private Modelo347DTO modelo347Actual;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Inicializando ModelosFiscalesController...");

        // Inicializar los servicios usando Singleton
        this.modelosService = ModelosFiscalesService.getInstance();
        this.exportacionBOEService = ExportacionBOEService.getInstance();

        configurarCombos();
        configurarListeners();

        log.info("ModelosFiscalesController inicializado correctamente");
    }

    private void configurarCombos() {
        // Modelos disponibles
        comboModelo.setItems(FXCollections.observableArrayList(
                "Modelo 303 - IVA Trimestral",
                "Modelo 130 - IRPF Trimestral",
                "Modelo 390 - Resumen Anual IVA",
                "Modelo 347 - Operaciones con Terceros"));
        comboModelo.getSelectionModel().selectFirst();

        // Años (últimos 5 años)
        int anoActual = LocalDate.now().getYear();
        comboAno.setItems(FXCollections.observableArrayList(
                anoActual, anoActual - 1, anoActual - 2, anoActual - 3, anoActual - 4));
        comboAno.getSelectionModel().selectFirst();

        // Trimestres
        comboTrimestre.setItems(FXCollections.observableArrayList(
                "1T (Ene-Mar)", "2T (Abr-Jun)", "3T (Jul-Sep)", "4T (Oct-Dic)"));

        // Seleccionar trimestre actual
        int mesActual = LocalDate.now().getMonthValue();
        int trimestreActual = (mesActual - 1) / 3;
        comboTrimestre.getSelectionModel().select(trimestreActual);
    }

    private void configurarListeners() {
        comboModelo.setOnAction(e -> actualizarVisibilidadTrimestre());
        actualizarVisibilidadTrimestre();
    }

    private void actualizarVisibilidadTrimestre() {
        String modeloSeleccionado = comboModelo.getValue();
        boolean esTrimestral = modeloSeleccionado != null &&
                (modeloSeleccionado.contains("303") || modeloSeleccionado.contains("130"));

        comboTrimestre.setVisible(esTrimestral);
        comboTrimestre.setManaged(esTrimestral);
        if (lblTrimestreLabel != null) {
            lblTrimestreLabel.setVisible(esTrimestral);
            lblTrimestreLabel.setManaged(esTrimestral);
        }

        // Mostrar/ocultar botón BOE según el modelo (347 no tiene formato BOE)
        if (btnExportarBOE != null) {
            boolean tieneBOE = modeloSeleccionado != null && !modeloSeleccionado.contains("347");
            btnExportarBOE.setVisible(tieneBOE);
            btnExportarBOE.setManaged(tieneBOE);
        }
    }

    // ========================================
    // GENERACIÓN DE MODELOS
    // ========================================

    @FXML
    private void generarModelo() {
        String modeloSeleccionado = comboModelo.getValue();
        Integer ano = comboAno.getValue();

        if (modeloSeleccionado == null || ano == null) {
            mostrarAlerta("Error", "Seleccione el modelo y el año", Alert.AlertType.WARNING);
            return;
        }

        btnGenerar.setDisable(true);
        contenedorResultados.getChildren().clear();

        // Mostrar indicador de carga
        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(50, 50);
        contenedorResultados.getChildren().add(progress);

        CompletableFuture.runAsync(() -> {
            try {
                if (modeloSeleccionado.contains("303")) {
                    int trimestre = comboTrimestre.getSelectionModel().getSelectedIndex() + 1;
                    modelo303Actual = modelosService.generarModelo303(ano, trimestre);
                    Platform.runLater(() -> mostrarModelo303(modelo303Actual));

                } else if (modeloSeleccionado.contains("130")) {
                    int trimestre = comboTrimestre.getSelectionModel().getSelectedIndex() + 1;
                    modelo130Actual = modelosService.generarModelo130(ano, trimestre);
                    Platform.runLater(() -> mostrarModelo130(modelo130Actual));

                } else if (modeloSeleccionado.contains("390")) {
                    modelo390Actual = modelosService.generarModelo390(ano);
                    Platform.runLater(() -> mostrarModelo390(modelo390Actual));

                } else if (modeloSeleccionado.contains("347")) {
                    modelo347Actual = modelosService.generarModelo347(ano);
                    Platform.runLater(() -> mostrarModelo347(modelo347Actual));
                }

            } catch (Exception e) {
                log.error("Error generando modelo", e);
                Platform.runLater(() -> {
                    contenedorResultados.getChildren().clear();
                    mostrarAlerta("Error", "No se pudo generar el modelo: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            } finally {
                Platform.runLater(() -> btnGenerar.setDisable(false));
            }
        });
    }

    // ========================================
    // VISUALIZACIÓN MODELO 303
    // ========================================

    private void mostrarModelo303(Modelo303DTO modelo) {
        contenedorResultados.getChildren().clear();

        // Título
        Label titulo = crearTitulo("MODELO 303 - Autoliquidación IVA");
        Label subtitulo = crearSubtitulo("Ejercicio " + modelo.getEjercicio() + " - Período " + modelo.getPeriodo());

        // Datos identificativos
        VBox datosId = crearSeccion("DATOS IDENTIFICATIVOS",
                crearFila("NIF:", modelo.getNif()),
                crearFila("Nombre/Razón Social:", modelo.getNombreRazonSocial()));

        // IVA Devengado
        VBox ivaDevengado = crearSeccion("IVA DEVENGADO (Repercutido)",
                crearFilaConImporte("Base imponible al 21%:", modelo.getBaseImponible21()),
                crearFilaConImporte("Cuota al 21%:", modelo.getCuotaDevengada21()),
                crearFilaSeparador(),
                crearFilaConImporte("TOTAL CUOTA DEVENGADA:", modelo.getTotalCuotaDevengada(), true));

        // IVA Deducible
        VBox ivaDeducible = crearSeccion("IVA DEDUCIBLE (Soportado)",
                crearFilaConImporte("Base deducible (operaciones interiores):", modelo.getBaseDeducibleInteriores()),
                crearFilaConImporte("Cuota deducible:", modelo.getCuotaDeducibleInteriores()),
                crearFilaSeparador(),
                crearFilaConImporte("TOTAL CUOTA DEDUCIBLE:", modelo.getTotalCuotaDeducible(), true));

        // Resultado
        String estiloResultado = modelo.getResultado().compareTo(BigDecimal.ZERO) > 0
                ? "-fx-text-fill: #d32f2f; -fx-font-weight: bold;"
                : "-fx-text-fill: #388e3c; -fx-font-weight: bold;";

        VBox resultado = crearSeccion("RESULTADO",
                crearFilaConImporte("Diferencia (Devengado - Deducible):", modelo.getDiferencia()),
                crearFilaConImporte("Cuotas a compensar períodos anteriores:", modelo.getCuotasCompensar()),
                crearFilaSeparador(),
                crearFilaConImporte("RESULTADO DE LA DECLARACIÓN:", modelo.getResultado(), true, estiloResultado),
                crearFila("Tipo:", modelo.getTipoResultado()));

        // Información adicional
        VBox info = crearSeccion("INFORMACIÓN ADICIONAL",
                crearFila("Nº Facturas emitidas:", String.valueOf(modelo.getNumFacturasEmitidas())),
                crearFila("Nº Facturas recibidas:", String.valueOf(modelo.getNumFacturasRecibidas())));

        contenedorResultados.getChildren().addAll(titulo, subtitulo, datosId, ivaDevengado, ivaDeducible, resultado,
                info);
    }

    // ========================================
    // VISUALIZACIÓN MODELO 130
    // ========================================

    private void mostrarModelo130(Modelo130DTO modelo) {
        contenedorResultados.getChildren().clear();

        Label titulo = crearTitulo("MODELO 130 - Pago Fraccionado IRPF");
        Label subtitulo = crearSubtitulo("Ejercicio " + modelo.getEjercicio() + " - Período " + modelo.getPeriodo() +
                " (Datos acumulados desde 1 de enero)");

        VBox datosId = crearSeccion("DATOS IDENTIFICATIVOS",
                crearFila("NIF:", modelo.getNif()),
                crearFila("Nombre y Apellidos:", modelo.getNombreApellidos()));

        VBox actividad = crearSeccion("ACTIVIDADES ECONÓMICAS EN ESTIMACIÓN DIRECTA",
                crearFilaConImporte("[01] Ingresos computables (acumulados):", modelo.getIngresosComputables()),
                crearFilaConImporte("[02] Gastos deducibles (acumulados):", modelo.getGastosDeducibles()),
                crearFilaSeparador(),
                crearFilaConImporte("[03] Rendimiento neto (01-02):", modelo.getRendimientoNeto(), true),
                crearFilaConImporte("[04] 20% del rendimiento neto:", modelo.getPagoCuenta20()),
                crearFilaConImporte("[05] Retenciones soportadas:", modelo.getRetencionesIngresoCuenta()),
                crearFilaConImporte("[06] Pagos fraccionados anteriores:", modelo.getPagosFraccionadosAnteriores()),
                crearFilaSeparador(),
                crearFilaConImporte("[07] Resultado previo:", modelo.getResultadoPrevio(), true));

        String estiloResultado = modelo.getTotal().compareTo(BigDecimal.ZERO) > 0
                ? "-fx-text-fill: #d32f2f; -fx-font-weight: bold;"
                : "-fx-text-fill: #388e3c; -fx-font-weight: bold;";

        VBox resultado = crearSeccion("RESULTADO",
                crearFilaConImporte("A deducir:", modelo.getADeducir()),
                crearFilaSeparador(),
                crearFilaConImporte("TOTAL A INGRESAR:", modelo.getTotal(), true, estiloResultado),
                crearFila("Tipo:", modelo.getTipoResultado()));

        VBox info = crearSeccion("INFORMACIÓN ADICIONAL",
                crearFila("Ingresos este trimestre:", currencyFormat.format(modelo.getIngresosTrimestre())),
                crearFila("Nº Facturas emitidas:", String.valueOf(modelo.getNumFacturasEmitidas())),
                crearFila("Nº Gastos registrados:", String.valueOf(modelo.getNumGastosRegistrados())));

        contenedorResultados.getChildren().addAll(titulo, subtitulo, datosId, actividad, resultado, info);
    }

    // ========================================
    // VISUALIZACIÓN MODELO 390
    // ========================================

    private void mostrarModelo390(Modelo390DTO modelo) {
        contenedorResultados.getChildren().clear();

        Label titulo = crearTitulo("MODELO 390 - Resumen Anual IVA");
        Label subtitulo = crearSubtitulo("Ejercicio " + modelo.getEjercicio());

        VBox datosId = crearSeccion("DATOS IDENTIFICATIVOS",
                crearFila("NIF:", modelo.getNif()),
                crearFila("Nombre/Razón Social:", modelo.getNombreRazonSocial()),
                crearFila("CNAE:", modelo.getCnae()));

        VBox devengado = crearSeccion("IVA DEVENGADO - RÉGIMEN GENERAL",
                crearFilaConImporte("Base imponible al 21%:", modelo.getBaseDevengada21()),
                crearFilaConImporte("Cuota al 21%:", modelo.getCuotaDevengada21()),
                crearFilaSeparador(),
                crearFilaConImporte("TOTAL BASE DEVENGADA:", modelo.getTotalBaseDevengada(), true),
                crearFilaConImporte("TOTAL CUOTA DEVENGADA:", modelo.getTotalCuotaDevengada(), true));

        VBox deducible = crearSeccion("IVA DEDUCIBLE",
                crearFilaConImporte("Base deducible (interiores):", modelo.getBaseDeducibleInteriores()),
                crearFilaConImporte("Cuota deducible (interiores):", modelo.getCuotaDeducibleInteriores()),
                crearFilaSeparador(),
                crearFilaConImporte("TOTAL CUOTA DEDUCIBLE:", modelo.getTotalCuotaDeducible(), true));

        VBox resultado = crearSeccion("RESULTADO ANUAL",
                crearFilaConImporte("Diferencia:", modelo.getDiferencia(), true),
                crearFilaConImporte("Volumen de operaciones:", modelo.getVolumenOperaciones(), true));

        // Desglose trimestral
        VBox desglose = crearSeccion("DESGLOSE POR TRIMESTRES");
        for (Modelo390DTO.ResumenTrimestral rt : modelo.getDesgloseTrimestral()) {
            HBox fila = new HBox(20);
            fila.getChildren().addAll(
                    new Label(rt.getTrimestre() + ":"),
                    new Label("Devengado: " + currencyFormat.format(rt.getCuotaDevengada())),
                    new Label("Deducible: " + currencyFormat.format(rt.getCuotaDeducible())),
                    new Label("Resultado: " + currencyFormat.format(rt.getResultado())));
            desglose.getChildren().add(fila);
        }

        VBox info = crearSeccion("TOTALES",
                crearFila("Total facturas emitidas:", String.valueOf(modelo.getNumFacturasEmitidas())),
                crearFila("Total facturas recibidas:", String.valueOf(modelo.getNumFacturasRecibidas())));

        contenedorResultados.getChildren().addAll(titulo, subtitulo, datosId, devengado, deducible, resultado, desglose,
                info);
    }

    // ========================================
    // VISUALIZACIÓN MODELO 347
    // ========================================

    private void mostrarModelo347(Modelo347DTO modelo) {
        contenedorResultados.getChildren().clear();

        Label titulo = crearTitulo("MODELO 347 - Operaciones con Terceras Personas");
        Label subtitulo = crearSubtitulo("Ejercicio " + modelo.getEjercicio() +
                " (Operaciones superiores a 3.005,06 €)");

        VBox datosId = crearSeccion("DATOS DEL DECLARANTE",
                crearFila("NIF:", modelo.getNif()),
                crearFila("Nombre/Razón Social:", modelo.getNombreRazonSocial()));

        VBox resumen = crearSeccion("RESUMEN DE LA DECLARACIÓN",
                crearFila("Total personas/entidades declaradas:", String.valueOf(modelo.getNumDeclarados())),
                crearFilaConImporte("Importe total operaciones:", modelo.getImporteTotal()),
                crearFilaSeparador(),
                crearFila("Clientes declarados:", String.valueOf(modelo.getNumClientes())),
                crearFilaConImporte("Total ventas:", modelo.getImporteTotalVentas()),
                crearFila("Proveedores declarados:", String.valueOf(modelo.getNumProveedores())),
                crearFilaConImporte("Total compras:", modelo.getImporteTotalCompras()));

        // Listado de declarados
        VBox listado = crearSeccion("DETALLE DE DECLARADOS");

        if (modelo.getDeclarados().isEmpty()) {
            listado.getChildren().add(new Label("No hay operaciones que superen el umbral de 3.005,06 €"));
        } else {
            for (Modelo347DTO.Declarado347 d : modelo.getDeclarados()) {
                VBox declarado = new VBox(5);
                declarado.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");
                declarado.getChildren().addAll(
                        new Label(d.getNombreRazonSocial()
                                + (d.getNif() != null && !d.getNif().isEmpty() ? " (" + d.getNif() + ")" : "")),
                        crearFilaConImporte("Importe anual:", d.getImporteAnual()),
                        crearFila("Tipo:",
                                "B".equals(d.getClaveOperacion()) ? "CLIENTE (Ventas)" : "PROVEEDOR (Compras)"),
                        crearFila("Nº Operaciones:", String.valueOf(d.getNumOperaciones())));
                listado.getChildren().add(declarado);
            }
        }

        contenedorResultados.getChildren().addAll(titulo, subtitulo, datosId, resumen, listado);
    }

    // ========================================
    // EXPORTACIÓN EXCEL
    // ========================================

    @FXML
    private void exportarExcel() {
        String modeloSeleccionado = comboModelo.getValue();

        if (modeloSeleccionado == null) {
            mostrarAlerta("Aviso", "Primero debe generar un modelo", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Modelo Fiscal");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));

        String nombreArchivo = modeloSeleccionado.substring(0, 10).replace(" ", "_") + "_" +
                comboAno.getValue() + ".xlsx";
        fileChooser.setInitialFileName(nombreArchivo);

        Stage stage = (Stage) btnExportarExcel.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                if (modeloSeleccionado.contains("303") && modelo303Actual != null) {
                    exportarModelo303Excel(workbook, modelo303Actual);
                } else if (modeloSeleccionado.contains("130") && modelo130Actual != null) {
                    exportarModelo130Excel(workbook, modelo130Actual);
                } else if (modeloSeleccionado.contains("390") && modelo390Actual != null) {
                    exportarModelo390Excel(workbook, modelo390Actual);
                } else if (modeloSeleccionado.contains("347") && modelo347Actual != null) {
                    exportarModelo347Excel(workbook, modelo347Actual);
                } else {
                    mostrarAlerta("Aviso", "Primero genere el modelo", Alert.AlertType.WARNING);
                    return;
                }

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }

                mostrarAlerta("Éxito", "Modelo exportado correctamente", Alert.AlertType.INFORMATION);

            } catch (Exception e) {
                log.error("Error exportando a Excel", e);
                mostrarAlerta("Error", "No se pudo exportar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void exportarModelo303Excel(Workbook workbook, Modelo303DTO modelo) {
        Sheet sheet = workbook.createSheet("Modelo 303");
        int row = 0;

        crearFilaExcel(sheet, row++, "MODELO 303 - AUTOLIQUIDACIÓN IVA", "");
        crearFilaExcel(sheet, row++, "Ejercicio:", modelo.getEjercicio());
        crearFilaExcel(sheet, row++, "Período:", modelo.getPeriodo());
        crearFilaExcel(sheet, row++, "NIF:", modelo.getNif());
        crearFilaExcel(sheet, row++, "Nombre:", modelo.getNombreRazonSocial());
        row++;

        crearFilaExcel(sheet, row++, "IVA DEVENGADO", "");
        crearFilaExcel(sheet, row++, "Base imponible 21%:", modelo.getBaseImponible21().toString());
        crearFilaExcel(sheet, row++, "Cuota 21%:", modelo.getCuotaDevengada21().toString());
        crearFilaExcel(sheet, row++, "TOTAL DEVENGADO:", modelo.getTotalCuotaDevengada().toString());
        row++;

        crearFilaExcel(sheet, row++, "IVA DEDUCIBLE", "");
        crearFilaExcel(sheet, row++, "Base deducible:", modelo.getBaseDeducibleInteriores().toString());
        crearFilaExcel(sheet, row++, "Cuota deducible:", modelo.getCuotaDeducibleInteriores().toString());
        crearFilaExcel(sheet, row++, "TOTAL DEDUCIBLE:", modelo.getTotalCuotaDeducible().toString());
        row++;

        crearFilaExcel(sheet, row++, "RESULTADO", "");
        crearFilaExcel(sheet, row++, "Diferencia:", modelo.getDiferencia().toString());
        crearFilaExcel(sheet, row++, "RESULTADO:", modelo.getResultado().toString());
        crearFilaExcel(sheet, row++, "Tipo:", modelo.getTipoResultado());

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void exportarModelo130Excel(Workbook workbook, Modelo130DTO modelo) {
        Sheet sheet = workbook.createSheet("Modelo 130");
        int row = 0;

        crearFilaExcel(sheet, row++, "MODELO 130 - PAGO FRACCIONADO IRPF", "");
        crearFilaExcel(sheet, row++, "Ejercicio:", modelo.getEjercicio());
        crearFilaExcel(sheet, row++, "Período:", modelo.getPeriodo());
        crearFilaExcel(sheet, row++, "NIF:", modelo.getNif());
        crearFilaExcel(sheet, row++, "Nombre:", modelo.getNombreApellidos());
        row++;

        crearFilaExcel(sheet, row++, "[01] Ingresos:", modelo.getIngresosComputables().toString());
        crearFilaExcel(sheet, row++, "[02] Gastos:", modelo.getGastosDeducibles().toString());
        crearFilaExcel(sheet, row++, "[03] Rendimiento neto:", modelo.getRendimientoNeto().toString());
        crearFilaExcel(sheet, row++, "[04] 20% Rdto:", modelo.getPagoCuenta20().toString());
        crearFilaExcel(sheet, row++, "[07] Resultado:", modelo.getResultadoPrevio().toString());
        row++;

        crearFilaExcel(sheet, row++, "TOTAL A INGRESAR:", modelo.getTotal().toString());

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void exportarModelo390Excel(Workbook workbook, Modelo390DTO modelo) {
        Sheet sheet = workbook.createSheet("Modelo 390");
        int row = 0;

        crearFilaExcel(sheet, row++, "MODELO 390 - RESUMEN ANUAL IVA", "");
        crearFilaExcel(sheet, row++, "Ejercicio:", modelo.getEjercicio());
        row++;

        crearFilaExcel(sheet, row++, "Total base devengada:", modelo.getTotalBaseDevengada().toString());
        crearFilaExcel(sheet, row++, "Total cuota devengada:", modelo.getTotalCuotaDevengada().toString());
        crearFilaExcel(sheet, row++, "Total cuota deducible:", modelo.getTotalCuotaDeducible().toString());
        crearFilaExcel(sheet, row++, "Diferencia:", modelo.getDiferencia().toString());
        crearFilaExcel(sheet, row++, "Volumen operaciones:", modelo.getVolumenOperaciones().toString());

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void exportarModelo347Excel(Workbook workbook, Modelo347DTO modelo) {
        Sheet sheet = workbook.createSheet("Modelo 347");
        int row = 0;

        crearFilaExcel(sheet, row++, "MODELO 347 - OPERACIONES CON TERCEROS", "");
        crearFilaExcel(sheet, row++, "Ejercicio:", modelo.getEjercicio());
        crearFilaExcel(sheet, row++, "Total declarados:", String.valueOf(modelo.getNumDeclarados()));
        crearFilaExcel(sheet, row++, "Importe total:", modelo.getImporteTotal().toString());
        row++;

        // Cabecera de tabla
        Row headerRow = sheet.createRow(row++);
        headerRow.createCell(0).setCellValue("NIF");
        headerRow.createCell(1).setCellValue("Nombre");
        headerRow.createCell(2).setCellValue("Tipo");
        headerRow.createCell(3).setCellValue("Importe");

        for (Modelo347DTO.Declarado347 d : modelo.getDeclarados()) {
            Row dataRow = sheet.createRow(row++);
            dataRow.createCell(0).setCellValue(d.getNif() != null ? d.getNif() : "");
            dataRow.createCell(1).setCellValue(d.getNombreRazonSocial());
            dataRow.createCell(2).setCellValue("B".equals(d.getClaveOperacion()) ? "Cliente" : "Proveedor");
            dataRow.createCell(3).setCellValue(d.getImporteAnual().doubleValue());
        }

        for (int i = 0; i < 4; i++)
            sheet.autoSizeColumn(i);
    }

    private void crearFilaExcel(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    // ========================================
    // EXPORTACIÓN BOE (AGENCIA TRIBUTARIA)
    // ========================================

    @FXML
    private void exportarBOE() {
        String modeloSeleccionado = comboModelo.getValue();

        if (modeloSeleccionado == null) {
            mostrarAlerta("Aviso", "Primero debe seleccionar un modelo", Alert.AlertType.WARNING);
            return;
        }

        // El modelo 347 no tiene formato BOE en esta implementación
        if (modeloSeleccionado.contains("347")) {
            mostrarAlerta("Información",
                    "El Modelo 347 se presenta de forma diferente.\n\n" +
                            "Utilice la exportación a Excel como borrador y presente\n" +
                            "los datos directamente en la sede electrónica de la AEAT.",
                    Alert.AlertType.INFORMATION);
            return;
        }

        // Verificar que el modelo está generado
        String contenidoBOE = null;
        String nombreFichero = null;

        try {
            if (modeloSeleccionado.contains("303")) {
                if (modelo303Actual == null) {
                    mostrarAlerta("Aviso", "Primero debe generar el Modelo 303", Alert.AlertType.WARNING);
                    return;
                }
                contenidoBOE = exportacionBOEService.generarBOE303(modelo303Actual);
                nombreFichero = exportacionBOEService.generarNombreFichero("303",
                        modelo303Actual.getEjercicio(), modelo303Actual.getPeriodo());

            } else if (modeloSeleccionado.contains("130")) {
                if (modelo130Actual == null) {
                    mostrarAlerta("Aviso", "Primero debe generar el Modelo 130", Alert.AlertType.WARNING);
                    return;
                }
                contenidoBOE = exportacionBOEService.generarBOE130(modelo130Actual);
                nombreFichero = exportacionBOEService.generarNombreFichero("130",
                        modelo130Actual.getEjercicio(), modelo130Actual.getPeriodo());

            } else if (modeloSeleccionado.contains("390")) {
                if (modelo390Actual == null) {
                    mostrarAlerta("Aviso", "Primero debe generar el Modelo 390", Alert.AlertType.WARNING);
                    return;
                }
                contenidoBOE = exportacionBOEService.generarBOE390(modelo390Actual);
                nombreFichero = exportacionBOEService.generarNombreFichero("390",
                        modelo390Actual.getEjercicio(), null);
            }

            if (contenidoBOE == null) {
                mostrarAlerta("Error", "No se pudo generar el fichero BOE", Alert.AlertType.ERROR);
                return;
            }

            // Mostrar diálogo para guardar
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar fichero BOE para Agencia Tributaria");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichero AEAT (*.txt)", "*.txt"));
            fileChooser.setInitialFileName(nombreFichero);

            Stage stage = (Stage) btnExportarBOE.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                // Guardar con codificación ISO-8859-1 (requerido por AEAT)
                try (FileWriter writer = new FileWriter(file, StandardCharsets.ISO_8859_1)) {
                    writer.write(contenidoBOE);
                }

                mostrarAlerta("Éxito",
                        "Fichero BOE generado correctamente.\n\n" +
                                "Archivo: " + file.getName() + "\n\n" +
                                "Puede importar este fichero en:\n" +
                                "• Sede electrónica de la AEAT\n" +
                                "• Programa de ayuda de la AEAT\n\n" +
                                "IMPORTANTE: Revise los datos antes de presentar.",
                        Alert.AlertType.INFORMATION);

                log.info("Fichero BOE exportado: {}", file.getAbsolutePath());
            }

        } catch (IOException e) {
            log.error("Error al guardar fichero BOE", e);
            mostrarAlerta("Error", "No se pudo guardar el fichero: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            log.error("Error al generar fichero BOE", e);
            mostrarAlerta("Error", "Error al generar el fichero BOE: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void exportarPDF() {
        mostrarAlerta("Información", "Exportación a PDF en desarrollo.\n\nPor ahora, use la exportación a Excel o BOE.",
                Alert.AlertType.INFORMATION);
    }

    // ========================================
    // MÉTODOS AUXILIARES UI
    // ========================================

    private Label crearTitulo(String texto) {
        Label label = new Label(texto);
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1976D2;");
        VBox.setMargin(label, new Insets(0, 0, 5, 0));
        return label;
    }

    private Label crearSubtitulo(String texto) {
        Label label = new Label(texto);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
        VBox.setMargin(label, new Insets(0, 0, 15, 0));
        return label;
    }

    private VBox crearSeccion(String titulo, HBox... filas) {
        VBox seccion = new VBox(8);
        seccion.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5; " +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 5;");
        VBox.setMargin(seccion, new Insets(0, 0, 15, 0));

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");
        seccion.getChildren().add(lblTitulo);

        for (HBox fila : filas) {
            seccion.getChildren().add(fila);
        }

        return seccion;
    }

    private HBox crearFila(String label, String valor) {
        HBox fila = new HBox(10);
        fila.setAlignment(Pos.CENTER_LEFT);

        Label lblLabel = new Label(label);
        lblLabel.setMinWidth(250);
        lblLabel.setStyle("-fx-text-fill: #666;");

        Label lblValor = new Label(valor != null ? valor : "");
        lblValor.setStyle("-fx-font-weight: bold;");

        fila.getChildren().addAll(lblLabel, lblValor);
        return fila;
    }

    private HBox crearFilaConImporte(String label, BigDecimal valor) {
        return crearFilaConImporte(label, valor, false, null);
    }

    private HBox crearFilaConImporte(String label, BigDecimal valor, boolean destacado) {
        return crearFilaConImporte(label, valor, destacado, null);
    }

    private HBox crearFilaConImporte(String label, BigDecimal valor, boolean destacado, String estiloExtra) {
        HBox fila = new HBox(10);
        fila.setAlignment(Pos.CENTER_LEFT);

        Label lblLabel = new Label(label);
        lblLabel.setMinWidth(250);
        lblLabel.setStyle(destacado ? "-fx-font-weight: bold;" : "-fx-text-fill: #666;");

        String valorFormateado = valor != null ? currencyFormat.format(valor) : "0,00 €";
        Label lblValor = new Label(valorFormateado);

        String estilo = "-fx-font-weight: bold;";
        if (estiloExtra != null) {
            estilo += estiloExtra;
        }
        lblValor.setStyle(estilo);

        fila.getChildren().addAll(lblLabel, lblValor);
        return fila;
    }

    private HBox crearFilaSeparador() {
        HBox fila = new HBox();
        Separator sep = new Separator();
        sep.setPrefWidth(500);
        fila.getChildren().add(sep);
        VBox.setMargin(fila, new Insets(5, 0, 5, 0));
        return fila;
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
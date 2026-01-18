package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.model.dto.*;
import com.lavaderosepulveda.crm.api.service.FacturacionApiService;
import com.lavaderosepulveda.crm.config.StageManager;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.print.PageLayout;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Parent;

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

import java.awt.Desktop;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ResumenFinancieroController implements Initializable {

    // Filtros
    @FXML
    private DatePicker dpDesde;
    @FXML
    private DatePicker dpHasta;
    @FXML
    private Label lblPeriodo;

    // Tarjetas principales
    @FXML
    private Label lblTotalIngresos;
    @FXML
    private Label lblFacturasEmitidas;
    @FXML
    private Label lblBaseIngresos;
    @FXML
    private Label lblIvaRepercutido;

    @FXML
    private Label lblTotalGastos;
    @FXML
    private Label lblNumGastos;
    @FXML
    private Label lblBaseGastos;
    @FXML
    private Label lblIvaSoportado;

    @FXML
    private Label lblBeneficio;
    @FXML
    private Label lblMargen;
    @FXML
    private Label lblLiquidacionIva;

    // Tabla categorías
    @FXML
    private TableView<Map<String, Object>> tablaCategorias;
    @FXML
    private TableColumn<Map<String, Object>, String> colCategoria;
    @FXML
    private TableColumn<Map<String, Object>, String> colCategoriaImporte;
    @FXML
    private TableColumn<Map<String, Object>, String> colCategoriaPorcentaje;
    @FXML
    private TableColumn<Map<String, Object>, String> colCategoriaNumero;

    // Gráficos
    @FXML
    private PieChart chartCategorias;
    @FXML
    private LineChart<String, Number> chartEvolucion;
    @FXML
    private CategoryAxis ejeX;
    @FXML
    private NumberAxis ejeY;

    // Pendientes de cobro
    @FXML
    private TableView<FacturaEmitidaDTO> tablaPendientesCobro;
    @FXML
    private TableColumn<FacturaEmitidaDTO, String> colCobroNumero;
    @FXML
    private TableColumn<FacturaEmitidaDTO, String> colCobroCliente;
    @FXML
    private TableColumn<FacturaEmitidaDTO, String> colCobroFecha;
    @FXML
    private TableColumn<FacturaEmitidaDTO, String> colCobroImporte;
    @FXML
    private Label lblTotalPendienteCobro;

    // Pendientes de pago
    @FXML
    private TableView<FacturaRecibidaDTO> tablaPendientesPago;
    @FXML
    private TableColumn<FacturaRecibidaDTO, String> colPagoNumero;
    @FXML
    private TableColumn<FacturaRecibidaDTO, String> colPagoProveedor;
    @FXML
    private TableColumn<FacturaRecibidaDTO, String> colPagoVencimiento;
    @FXML
    private TableColumn<FacturaRecibidaDTO, String> colPagoImporte;
    @FXML
    private Label lblTotalPendientePago;

    // IVA trimestral
    @FXML
    private TableView<Map<String, Object>> tablaIvaTrimestral;
    @FXML
    private TableColumn<Map<String, Object>, String> colTrimestre;
    @FXML
    private TableColumn<Map<String, Object>, String> colIvaRepercutidoTrim;
    @FXML
    private TableColumn<Map<String, Object>, String> colIvaSoportadoTrim;
    @FXML
    private TableColumn<Map<String, Object>, String> colLiquidacionTrim;
    @FXML
    private Label lblIvaRepercutidoAnual;
    @FXML
    private Label lblIvaSoportadoAnual;
    @FXML
    private Label lblLiquidacionAnual;

    private final FacturacionApiService apiService = FacturacionApiService.getInstance();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Datos cargados
    private List<FacturaEmitidaDTO> facturasEmitidas = new ArrayList<>();
    private List<FacturaRecibidaDTO> facturasRecibidas = new ArrayList<>();
    private List<GastoDTO> gastos = new ArrayList<>();

    // Colores para PDF (iText 7/8)
    private static final DeviceRgb COLOR_VERDE = new DeviceRgb(46, 125, 50);
    private static final DeviceRgb COLOR_ROJO = new DeviceRgb(198, 40, 40);
    private static final DeviceRgb COLOR_AZUL = new DeviceRgb(21, 101, 192);
    private static final DeviceRgb COLOR_VERDE_CLARO = new DeviceRgb(232, 245, 233);
    private static final DeviceRgb COLOR_ROJO_CLARO = new DeviceRgb(255, 235, 238);
    private static final DeviceRgb COLOR_GRIS = new DeviceRgb(100, 100, 100);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarFiltros();
        configurarTablaCategorias();
        configurarTablaPendientesCobro();
        configurarTablaPendientesPago();
        configurarTablaIvaTrimestral();

        // Establecer período por defecto (año actual)
        filtrarEsteAnio();
    }

    private void configurarFiltros() {
        dpDesde.valueProperty().addListener((obs, old, nuevo) -> actualizarLabelPeriodo());
        dpHasta.valueProperty().addListener((obs, old, nuevo) -> actualizarLabelPeriodo());
    }

    private void actualizarLabelPeriodo() {
        if (dpDesde.getValue() != null && dpHasta.getValue() != null) {
            lblPeriodo.setText("Período: " + dpDesde.getValue().format(dateFormatter) +
                    " - " + dpHasta.getValue().format(dateFormatter));
        }
    }

    private void configurarTablaCategorias() {
        colCategoria.setCellValueFactory(
                data -> new SimpleStringProperty(formatearCategoria((String) data.getValue().get("categoria"))));
        colCategoriaImporte.setCellValueFactory(
                data -> new SimpleStringProperty(formatearMoneda(getBigDecimal(data.getValue().get("total")))));
        colCategoriaPorcentaje.setCellValueFactory(data -> new SimpleStringProperty(
                String.format("%.1f%%", getDouble(data.getValue().get("porcentaje")))));
        colCategoriaNumero
                .setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get("cantidad"))));

        colCategoriaImporte.setStyle("-fx-alignment: CENTER-RIGHT;");
        colCategoriaPorcentaje.setStyle("-fx-alignment: CENTER-RIGHT;");
        colCategoriaNumero.setStyle("-fx-alignment: CENTER;");
    }

    private void configurarTablaPendientesCobro() {
        colCobroNumero.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNumeroFactura()));
        colCobroCliente.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClienteNombre()));
        colCobroFecha.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFechaEmision()));
        colCobroImporte
                .setCellValueFactory(data -> new SimpleStringProperty(formatearMoneda(data.getValue().getTotal())));

        colCobroImporte.setStyle("-fx-alignment: CENTER-RIGHT;");
    }

    private void configurarTablaPendientesPago() {
        colPagoNumero.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNumeroFactura()));
        colPagoProveedor.setCellValueFactory(data -> {
            FacturaRecibidaDTO f = data.getValue();
            String nombre = f.getProveedorNombre();
            if (nombre == null || nombre.isEmpty()) {
                nombre = "Sin proveedor";
            }
            return new SimpleStringProperty(nombre);
        });
        colPagoVencimiento.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFechaVencimiento()));
        colPagoImporte
                .setCellValueFactory(data -> new SimpleStringProperty(formatearMoneda(data.getValue().getTotal())));

        colPagoImporte.setStyle("-fx-alignment: CENTER-RIGHT;");

        // Resaltar facturas vencidas
        tablaPendientesPago.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(FacturaRecibidaDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    try {
                        LocalDate vencimiento = LocalDate.parse(item.getFechaVencimiento(), dateFormatter);
                        if (vencimiento.isBefore(LocalDate.now())) {
                            setStyle("-fx-background-color: #ffebee;");
                        } else if (vencimiento.isBefore(LocalDate.now().plusDays(7))) {
                            setStyle("-fx-background-color: #fff3e0;");
                        } else {
                            setStyle("");
                        }
                    } catch (Exception e) {
                        setStyle("");
                    }
                }
            }
        });
    }

    private void configurarTablaIvaTrimestral() {
        colTrimestre.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("trimestre")));
        colIvaRepercutidoTrim.setCellValueFactory(data -> new SimpleStringProperty(
                formatearMoneda(getBigDecimal(data.getValue().get("ivaRepercutido")))));
        colIvaSoportadoTrim.setCellValueFactory(
                data -> new SimpleStringProperty(formatearMoneda(getBigDecimal(data.getValue().get("ivaSoportado")))));
        colLiquidacionTrim.setCellValueFactory(
                data -> new SimpleStringProperty(formatearMoneda(getBigDecimal(data.getValue().get("liquidacion")))));

        colIvaRepercutidoTrim.setStyle("-fx-alignment: CENTER-RIGHT;");
        colIvaSoportadoTrim.setStyle("-fx-alignment: CENTER-RIGHT;");
        colLiquidacionTrim.setStyle("-fx-alignment: CENTER-RIGHT;");
    }

    @FXML
    private void actualizarResumen() {
        if (dpDesde.getValue() == null || dpHasta.getValue() == null) {
            mostrarError("Selecciona un período válido");
            return;
        }

        cargarDatos();
    }

    @FXML
    private void filtrarEsteMes() {
        LocalDate hoy = LocalDate.now();
        dpDesde.setValue(hoy.withDayOfMonth(1));
        dpHasta.setValue(hoy.with(TemporalAdjusters.lastDayOfMonth()));
        cargarDatos();
    }

    @FXML
    private void filtrarEsteTrimestre() {
        LocalDate hoy = LocalDate.now();
        int trimestre = (hoy.getMonthValue() - 1) / 3;
        LocalDate inicioTrimestre = LocalDate.of(hoy.getYear(), trimestre * 3 + 1, 1);
        LocalDate finTrimestre = inicioTrimestre.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth());

        dpDesde.setValue(inicioTrimestre);
        dpHasta.setValue(finTrimestre);
        cargarDatos();
    }

    @FXML
    private void filtrarEsteAnio() {
        LocalDate hoy = LocalDate.now();
        dpDesde.setValue(LocalDate.of(hoy.getYear(), 1, 1));
        dpHasta.setValue(LocalDate.of(hoy.getYear(), 12, 31));
        cargarDatos();
    }

    private void cargarDatos() {
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

        CompletableFuture.runAsync(() -> {
            try {
                // Cargar facturas emitidas
                facturasEmitidas = apiService.obtenerFacturasEmitidasPorPeriodo(desde, hasta);

                // Cargar facturas recibidas
                facturasRecibidas = apiService.obtenerFacturasRecibidasPorPeriodo(desde, hasta);

                // Cargar gastos
                gastos = apiService.obtenerGastosPorPeriodo(desde, hasta);

                Platform.runLater(this::actualizarVista);

            } catch (Exception e) {
                log.error("Error al cargar datos financieros", e);
                Platform.runLater(() -> mostrarError("Error al cargar datos: " + e.getMessage()));
            }
        });
    }

    private void actualizarVista() {
        calcularTotales();
        actualizarTablaCategorias();
        actualizarGraficoCategorias();
        actualizarGraficoEvolucion();
        actualizarPendientes();
        actualizarIvaTrimestral();
    }

    private void calcularTotales() {
        // Totales de ingresos
        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal baseIngresos = BigDecimal.ZERO;
        BigDecimal ivaRepercutido = BigDecimal.ZERO;

        for (FacturaEmitidaDTO f : facturasEmitidas) {
            if (f.getTotal() != null)
                totalIngresos = totalIngresos.add(f.getTotal());
            if (f.getBaseImponible() != null)
                baseIngresos = baseIngresos.add(f.getBaseImponible());
            if (f.getCuotaIva() != null)
                ivaRepercutido = ivaRepercutido.add(f.getCuotaIva());
        }

        lblTotalIngresos.setText(formatearMoneda(totalIngresos));
        lblFacturasEmitidas.setText(facturasEmitidas.size() + " facturas emitidas");
        lblBaseIngresos.setText(formatearMoneda(baseIngresos));
        lblIvaRepercutido.setText(formatearMoneda(ivaRepercutido));

        // Totales de gastos (facturas recibidas + gastos)
        BigDecimal totalGastos = BigDecimal.ZERO;
        BigDecimal baseGastos = BigDecimal.ZERO;
        BigDecimal ivaSoportado = BigDecimal.ZERO;

        for (FacturaRecibidaDTO f : facturasRecibidas) {
            if (f.getTotal() != null)
                totalGastos = totalGastos.add(f.getTotal());
            if (f.getBaseImponible() != null)
                baseGastos = baseGastos.add(f.getBaseImponible());
            if (f.getCuotaIva() != null)
                ivaSoportado = ivaSoportado.add(f.getCuotaIva());
        }

        for (GastoDTO g : gastos) {
            if (g.getImporte() != null)
                totalGastos = totalGastos.add(g.getImporte());
            if (g.getBaseImponible() != null)
                baseGastos = baseGastos.add(g.getBaseImponible());
            if (g.getCuotaIva() != null)
                ivaSoportado = ivaSoportado.add(g.getCuotaIva());
        }

        lblTotalGastos.setText(formatearMoneda(totalGastos));
        lblNumGastos.setText(facturasRecibidas.size() + " facturas + " + gastos.size() + " gastos");
        lblBaseGastos.setText(formatearMoneda(baseGastos));
        lblIvaSoportado.setText(formatearMoneda(ivaSoportado));

        // Beneficio
        BigDecimal beneficio = totalIngresos.subtract(totalGastos);
        lblBeneficio.setText(formatearMoneda(beneficio));

        // Colorear según beneficio
        if (beneficio.compareTo(BigDecimal.ZERO) >= 0) {
            lblBeneficio.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        } else {
            lblBeneficio.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #c62828;");
        }

        // Margen
        if (totalIngresos.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal margen = beneficio.multiply(new BigDecimal("100")).divide(totalIngresos, 1,
                    RoundingMode.HALF_UP);
            lblMargen.setText("Margen: " + margen + "%");
        } else {
            lblMargen.setText("Margen: N/A");
        }

        // Liquidación IVA
        BigDecimal liquidacionIva = ivaRepercutido.subtract(ivaSoportado);
        lblLiquidacionIva.setText(formatearMoneda(liquidacionIva));

        if (liquidacionIva.compareTo(BigDecimal.ZERO) >= 0) {
            lblLiquidacionIva.setStyle("-fx-font-weight: bold; -fx-text-fill: #c62828;"); // A pagar
        } else {
            lblLiquidacionIva.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;"); // A compensar
        }
    }

    private void actualizarTablaCategorias() {
        Map<String, BigDecimal[]> porCategoria = new LinkedHashMap<>();

        // Agregar facturas recibidas por categoría
        for (FacturaRecibidaDTO f : facturasRecibidas) {
            String cat = f.getCategoria() != null ? f.getCategoria() : "OTROS";
            porCategoria.computeIfAbsent(cat, k -> new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
            porCategoria.get(cat)[0] = porCategoria.get(cat)[0]
                    .add(f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO);
            porCategoria.get(cat)[1] = porCategoria.get(cat)[1].add(BigDecimal.ONE);
        }

        // Agregar gastos por categoría
        for (GastoDTO g : gastos) {
            String cat = g.getCategoria() != null ? g.getCategoria() : "OTROS";
            porCategoria.computeIfAbsent(cat, k -> new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
            porCategoria.get(cat)[0] = porCategoria.get(cat)[0]
                    .add(g.getImporte() != null ? g.getImporte() : BigDecimal.ZERO);
            porCategoria.get(cat)[1] = porCategoria.get(cat)[1].add(BigDecimal.ONE);
        }

        // Calcular total para porcentajes
        BigDecimal total = porCategoria.values().stream()
                .map(arr -> arr[0])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Crear lista de datos
        ObservableList<Map<String, Object>> datos = FXCollections.observableArrayList();

        for (Map.Entry<String, BigDecimal[]> entry : porCategoria.entrySet()) {
            Map<String, Object> fila = new HashMap<>();
            fila.put("categoria", entry.getKey());
            fila.put("total", entry.getValue()[0]);
            fila.put("cantidad", entry.getValue()[1].intValue());

            double porcentaje = 0;
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                porcentaje = entry.getValue()[0].multiply(new BigDecimal("100"))
                        .divide(total, 2, RoundingMode.HALF_UP).doubleValue();
            }
            fila.put("porcentaje", porcentaje);

            datos.add(fila);
        }

        // Ordenar por importe descendente
        datos.sort((a, b) -> getBigDecimal(b.get("total")).compareTo(getBigDecimal(a.get("total"))));

        tablaCategorias.setItems(datos);
    }

    private void actualizarGraficoCategorias() {
        chartCategorias.getData().clear();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Map<String, Object> item : tablaCategorias.getItems()) {
            String categoria = formatearCategoria((String) item.get("categoria"));
            double valor = getBigDecimal(item.get("total")).doubleValue();
            if (valor > 0) {
                pieData.add(new PieChart.Data(categoria, valor));
            }
        }

        chartCategorias.setData(pieData);
        chartCategorias.setLegendVisible(true);
    }

    private void actualizarGraficoEvolucion() {
        chartEvolucion.getData().clear();

        // Crear series para ingresos y gastos
        XYChart.Series<String, Number> serieIngresos = new XYChart.Series<>();
        serieIngresos.setName("Ingresos");

        XYChart.Series<String, Number> serieGastos = new XYChart.Series<>();
        serieGastos.setName("Gastos");

        XYChart.Series<String, Number> serieBeneficio = new XYChart.Series<>();
        serieBeneficio.setName("Beneficio");

        // Agrupar por mes
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

        LocalDate mes = desde.withDayOfMonth(1);
        while (!mes.isAfter(hasta)) {
            String nombreMes = mes.format(DateTimeFormatter.ofPattern("MMM yy", new Locale("es", "ES")));

            final LocalDate mesActual = mes;

            // Sumar ingresos del mes
            BigDecimal ingresosMes = facturasEmitidas.stream()
                    .filter(f -> perteneceAlMes(f.getFechaEmision(), mesActual))
                    .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Sumar gastos del mes
            BigDecimal gastosMes = facturasRecibidas.stream()
                    .filter(f -> perteneceAlMes(f.getFechaFactura(), mesActual))
                    .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            gastosMes = gastosMes.add(gastos.stream()
                    .filter(g -> perteneceAlMes(g.getFecha(), mesActual))
                    .map(g -> g.getImporte() != null ? g.getImporte() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));

            serieIngresos.getData().add(new XYChart.Data<>(nombreMes, ingresosMes));
            serieGastos.getData().add(new XYChart.Data<>(nombreMes, gastosMes));
            serieBeneficio.getData().add(new XYChart.Data<>(nombreMes, ingresosMes.subtract(gastosMes)));

            mes = mes.plusMonths(1);
        }

        chartEvolucion.getData().addAll(serieIngresos, serieGastos, serieBeneficio);
    }

    private boolean perteneceAlMes(String fechaStr, LocalDate mes) {
        if (fechaStr == null || fechaStr.isEmpty())
            return false;
        try {
            LocalDate fecha = LocalDate.parse(fechaStr, dateFormatter);
            return fecha.getYear() == mes.getYear() && fecha.getMonth() == mes.getMonth();
        } catch (Exception e) {
            return false;
        }
    }

    private void actualizarPendientes() {
        // Pendientes de cobro
        List<FacturaEmitidaDTO> pendientesCobro = facturasEmitidas.stream()
                .filter(f -> "PENDIENTE".equals(f.getEstado()))
                .toList();

        tablaPendientesCobro.setItems(FXCollections.observableArrayList(pendientesCobro));

        BigDecimal totalPendienteCobro = pendientesCobro.stream()
                .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotalPendienteCobro.setText("Total: " + formatearMoneda(totalPendienteCobro));

        // Pendientes de pago (solo facturas recibidas)
        List<FacturaRecibidaDTO> pendientesPago = facturasRecibidas.stream()
                .filter(f -> "PENDIENTE".equals(f.getEstado()))
                .toList();

        tablaPendientesPago.setItems(FXCollections.observableArrayList(pendientesPago));

        BigDecimal totalPendientePago = pendientesPago.stream()
                .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotalPendientePago.setText("Total: " + formatearMoneda(totalPendientePago));
    }

    private void actualizarIvaTrimestral() {
        int year = dpDesde.getValue().getYear();

        ObservableList<Map<String, Object>> datosTrimestre = FXCollections.observableArrayList();

        BigDecimal totalRepercutido = BigDecimal.ZERO;
        BigDecimal totalSoportado = BigDecimal.ZERO;

        for (int t = 1; t <= 4; t++) {
            LocalDate inicioTrim = LocalDate.of(year, (t - 1) * 3 + 1, 1);
            LocalDate finTrim = inicioTrim.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth());

            // IVA repercutido del trimestre
            final int trimestre = t;
            BigDecimal ivaRep = facturasEmitidas.stream()
                    .filter(f -> perteneceATrimestre(f.getFechaEmision(), year, trimestre))
                    .map(f -> f.getCuotaIva() != null ? f.getCuotaIva() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // IVA soportado del trimestre (facturas recibidas + gastos)
            BigDecimal ivaSop = facturasRecibidas.stream()
                    .filter(f -> perteneceATrimestre(f.getFechaFactura(), year, trimestre))
                    .map(f -> f.getCuotaIva() != null ? f.getCuotaIva() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            ivaSop = ivaSop.add(gastos.stream()
                    .filter(g -> perteneceATrimestre(g.getFecha(), year, trimestre))
                    .map(g -> g.getCuotaIva() != null ? g.getCuotaIva() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));

            BigDecimal liquidacion = ivaRep.subtract(ivaSop);

            Map<String, Object> fila = new HashMap<>();
            fila.put("trimestre", t + "T " + year);
            fila.put("ivaRepercutido", ivaRep);
            fila.put("ivaSoportado", ivaSop);
            fila.put("liquidacion", liquidacion);

            datosTrimestre.add(fila);

            totalRepercutido = totalRepercutido.add(ivaRep);
            totalSoportado = totalSoportado.add(ivaSop);
        }

        tablaIvaTrimestral.setItems(datosTrimestre);

        // Totales anuales
        lblIvaRepercutidoAnual.setText(formatearMoneda(totalRepercutido));
        lblIvaSoportadoAnual.setText(formatearMoneda(totalSoportado));

        BigDecimal liquidacionAnual = totalRepercutido.subtract(totalSoportado);
        lblLiquidacionAnual.setText(formatearMoneda(liquidacionAnual));

        if (liquidacionAnual.compareTo(BigDecimal.ZERO) >= 0) {
            lblLiquidacionAnual.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #c62828;");
        } else {
            lblLiquidacionAnual.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #2e7d32;");
        }
    }

    private boolean perteneceATrimestre(String fechaStr, int year, int trimestre) {
        if (fechaStr == null || fechaStr.isEmpty())
            return false;
        try {
            LocalDate fecha = LocalDate.parse(fechaStr, dateFormatter);
            if (fecha.getYear() != year)
                return false;
            int trimestreFecha = (fecha.getMonthValue() - 1) / 3 + 1;
            return trimestreFecha == trimestre;
        } catch (Exception e) {
            return false;
        }
    }

    @FXML
    private void exportarExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Resumen Financiero");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        fileChooser.setInitialFileName("resumen_financiero_" +
                dpDesde.getValue().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" +
                dpHasta.getValue().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");

        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            // TODO: Implementar exportación a Excel usando Apache POI
            mostrarInfo("Exportación a Excel pendiente de implementar");
        }
    }

    @FXML
    private void abrirInformePyG() {
        try {
            BorderPane rootPane = (BorderPane) StageManager.getInstance()
                    .getPrimaryStage().getScene().getRoot();
            BorderPane mainBorderPane = (BorderPane) rootPane.getCenter();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/informe-pyg.fxml"));
            Parent vista = loader.load();
            mainBorderPane.setCenter(vista);

        } catch (Exception e) {
            log.error("Error al cargar informe PyG", e);
            mostrarError("Error al cargar el informe: " + e.getMessage());
        }
    }

    // ========================================
    // IMPRESIÓN Y GENERACIÓN DE PDF
    // ========================================

    @FXML
    private void imprimir() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(getStage());
        alert.setTitle("Imprimir Resumen Financiero");
        alert.setHeaderText("Seleccione el método de impresión");
        alert.setContentText("¿Cómo desea imprimir el resumen?");

        ButtonType btnPDF = new ButtonType("Generar PDF");
        ButtonType btnDirecto = new ButtonType("Imprimir directo");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnPDF, btnDirecto, btnCancelar);

        Optional<ButtonType> resultado = alert.showAndWait();

        if (resultado.isPresent()) {
            if (resultado.get() == btnPDF) {
                generarPDFResumen();
            } else if (resultado.get() == btnDirecto) {
                imprimirDirecto();
            }
        }
    }

    private void generarPDFResumen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Resumen Financiero como PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fileChooser.setInitialFileName("resumen_financiero_" +
                dpDesde.getValue().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" +
                dpHasta.getValue().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

        File file = fileChooser.showSaveDialog(getStage());

        if (file != null) {
            try {
                crearPDFResumen(file);

                Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                confirmacion.initOwner(getStage());
                confirmacion.setTitle("PDF Generado");
                confirmacion.setHeaderText("Resumen financiero guardado correctamente");
                confirmacion.setContentText("¿Desea abrir el PDF ahora?");

                Optional<ButtonType> respuesta = confirmacion.showAndWait();
                if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file);
                    }
                }

            } catch (Exception e) {
                log.error("Error generando PDF", e);
                mostrarError("Error al generar el PDF: " + e.getMessage());
            }
        }
    }

    /**
     * Crea el documento PDF con el resumen financiero (iText 7/8)
     */
    private void crearPDFResumen(File file) throws Exception {
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // ========================================
        // TÍTULO
        // ========================================
        document.add(new Paragraph("RESUMEN FINANCIERO")
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
        // CALCULAR TOTALES
        // ========================================
        BigDecimal totalIngresos = facturasEmitidas.stream()
                .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGastos = facturasRecibidas.stream()
                .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalGastos = totalGastos.add(gastos.stream()
                .map(g -> g.getImporte() != null ? g.getImporte() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        BigDecimal beneficio = totalIngresos.subtract(totalGastos);

        // ========================================
        // TABLA RESUMEN PRINCIPAL
        // ========================================
        Table tablaResumen = new Table(UnitValue.createPercentArray(3)).useAllAvailableWidth();

        tablaResumen.addCell(crearCeldaResumen("INGRESOS", formatearMoneda(totalIngresos), COLOR_VERDE_CLARO, COLOR_VERDE));
        tablaResumen.addCell(crearCeldaResumen("GASTOS", formatearMoneda(totalGastos), COLOR_ROJO_CLARO, COLOR_ROJO));
        tablaResumen.addCell(crearCeldaResumen(
                beneficio.compareTo(BigDecimal.ZERO) >= 0 ? "BENEFICIO" : "PÉRDIDA",
                formatearMoneda(beneficio),
                beneficio.compareTo(BigDecimal.ZERO) >= 0 ? COLOR_VERDE_CLARO : COLOR_ROJO_CLARO,
                beneficio.compareTo(BigDecimal.ZERO) >= 0 ? COLOR_VERDE : COLOR_ROJO));

        document.add(tablaResumen);
        document.add(new Paragraph(" "));

        // ========================================
        // DETALLE INGRESOS
        // ========================================
        document.add(new Paragraph("DETALLE DE INGRESOS")
                .setFontSize(12)
                .setBold()
                .setFontColor(ColorConstants.DARK_GRAY));

        Table tablaIngresos = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 2})).useAllAvailableWidth();

        tablaIngresos.addHeaderCell(crearCeldaCabecera("Nº Factura"));
        tablaIngresos.addHeaderCell(crearCeldaCabecera("Cliente"));
        tablaIngresos.addHeaderCell(crearCeldaCabecera("Fecha"));
        tablaIngresos.addHeaderCell(crearCeldaCabecera("Importe"));

        for (FacturaEmitidaDTO f : facturasEmitidas) {
            tablaIngresos.addCell(new Cell().add(new Paragraph(f.getNumeroFactura() != null ? f.getNumeroFactura() : "").setFontSize(9)));
            tablaIngresos.addCell(new Cell().add(new Paragraph(f.getClienteNombre() != null ? f.getClienteNombre() : "").setFontSize(9)));
            tablaIngresos.addCell(new Cell().add(new Paragraph(f.getFechaEmision() != null ? f.getFechaEmision() : "").setFontSize(9)));
            tablaIngresos.addCell(new Cell().add(new Paragraph(formatearMoneda(f.getTotal())).setFontSize(9).setTextAlignment(TextAlignment.RIGHT)));
        }

        Cell celdaTotalLabel = new Cell(1, 3)
                .add(new Paragraph("TOTAL INGRESOS").setFontSize(9).setBold())
                .setBackgroundColor(COLOR_VERDE_CLARO);
        tablaIngresos.addCell(celdaTotalLabel);

        Cell celdaTotalValor = new Cell()
                .add(new Paragraph(formatearMoneda(totalIngresos)).setFontSize(9).setBold().setTextAlignment(TextAlignment.RIGHT))
                .setBackgroundColor(COLOR_VERDE_CLARO);
        tablaIngresos.addCell(celdaTotalValor);

        document.add(tablaIngresos);
        document.add(new Paragraph(" "));

        // ========================================
        // GASTOS POR CATEGORÍA
        // ========================================
        document.add(new Paragraph("GASTOS POR CATEGORÍA")
                .setFontSize(12)
                .setBold()
                .setFontColor(ColorConstants.DARK_GRAY));

        Table tablaCat = new Table(UnitValue.createPercentArray(new float[]{4, 2, 1})).useAllAvailableWidth();

        tablaCat.addHeaderCell(crearCeldaCabecera("Categoría"));
        tablaCat.addHeaderCell(crearCeldaCabecera("Importe"));
        tablaCat.addHeaderCell(crearCeldaCabecera("%"));

        for (Map<String, Object> item : this.tablaCategorias.getItems()) {
            tablaCat.addCell(new Cell().add(new Paragraph(formatearCategoria((String) item.get("categoria"))).setFontSize(9)));
            tablaCat.addCell(new Cell().add(new Paragraph(formatearMoneda(getBigDecimal(item.get("total")))).setFontSize(9).setTextAlignment(TextAlignment.RIGHT)));
            tablaCat.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", getDouble(item.get("porcentaje")))).setFontSize(9).setTextAlignment(TextAlignment.RIGHT)));
        }

        document.add(tablaCat);
        document.add(new Paragraph(" "));

        // ========================================
        // IVA TRIMESTRAL
        // ========================================
        document.add(new Paragraph("RESUMEN IVA TRIMESTRAL")
                .setFontSize(12)
                .setBold()
                .setFontColor(ColorConstants.DARK_GRAY));

        Table tablaIva = new Table(UnitValue.createPercentArray(4)).useAllAvailableWidth();

        tablaIva.addHeaderCell(crearCeldaCabecera("Trimestre"));
        tablaIva.addHeaderCell(crearCeldaCabecera("IVA Repercutido"));
        tablaIva.addHeaderCell(crearCeldaCabecera("IVA Soportado"));
        tablaIva.addHeaderCell(crearCeldaCabecera("Liquidación"));

        for (Map<String, Object> item : this.tablaIvaTrimestral.getItems()) {
            tablaIva.addCell(new Cell().add(new Paragraph((String) item.get("trimestre")).setFontSize(9)));
            tablaIva.addCell(new Cell().add(new Paragraph(formatearMoneda(getBigDecimal(item.get("ivaRepercutido")))).setFontSize(9).setTextAlignment(TextAlignment.RIGHT)));
            tablaIva.addCell(new Cell().add(new Paragraph(formatearMoneda(getBigDecimal(item.get("ivaSoportado")))).setFontSize(9).setTextAlignment(TextAlignment.RIGHT)));

            BigDecimal liquidacion = getBigDecimal(item.get("liquidacion"));
            DeviceRgb colorLiq = liquidacion.compareTo(BigDecimal.ZERO) >= 0 ? COLOR_ROJO : COLOR_VERDE;
            tablaIva.addCell(new Cell().add(new Paragraph(formatearMoneda(liquidacion)).setFontSize(9).setTextAlignment(TextAlignment.RIGHT).setFontColor(colorLiq)));
        }

        document.add(tablaIva);

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
        log.info("PDF de resumen financiero generado: {}", file.getAbsolutePath());
    }

    /**
     * Crea una celda de resumen con estilo (iText 7/8)
     */
    private Cell crearCeldaResumen(String titulo, String valor, DeviceRgb bgColor, DeviceRgb fontColor) {
        Cell celda = new Cell()
                .setBackgroundColor(bgColor)
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER);

        celda.add(new Paragraph(titulo)
                .setFontSize(10)
                .setBold()
                .setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        celda.add(new Paragraph(valor)
                .setFontSize(12)
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

    /**
     * Imprime directamente usando el sistema de impresión de JavaFX
     */
    private void imprimirDirecto() {
        Printer printer = Printer.getDefaultPrinter();
        if (printer == null) {
            mostrarError("No se encontró ninguna impresora configurada");
            return;
        }

        PrinterJob job = PrinterJob.createPrinterJob(printer);
        if (job == null) {
            mostrarError("No se pudo crear el trabajo de impresión");
            return;
        }

        boolean continuar = job.showPrintDialog(getStage());
        if (!continuar) {
            job.cancelJob();
            return;
        }

        Node contenido = lblPeriodo.getParent().getParent();
        PageLayout pageLayout = job.getJobSettings().getPageLayout();

        double scaleX = pageLayout.getPrintableWidth() / contenido.getBoundsInParent().getWidth();
        double scaleY = pageLayout.getPrintableHeight() / contenido.getBoundsInParent().getHeight();
        double scale = Math.min(scaleX, scaleY) * 0.9;

        Scale scaleTransform = new Scale(scale, scale);
        contenido.getTransforms().add(scaleTransform);

        boolean exito = job.printPage(pageLayout, contenido);

        contenido.getTransforms().remove(scaleTransform);

        if (exito) {
            job.endJob();
            mostrarInfo("Documento enviado a la impresora");
        } else {
            mostrarError("Error al imprimir el documento");
        }
    }

    @FXML
    private void cerrar() {
        getStage().close();
    }

    private Stage getStage() {
        return (Stage) lblPeriodo.getScene().getWindow();
    }

    // Utilidades

    private String formatearMoneda(BigDecimal valor) {
        if (valor == null)
            return "0,00 €";
        return currencyFormat.format(valor);
    }

    private String formatearCategoria(String categoria) {
        if (categoria == null)
            return "Otros";
        return switch (categoria) {
            case "AGUA" -> "Agua";
            case "LUZ" -> "Electricidad";
            case "GAS" -> "Gas";
            case "ALQUILER" -> "Alquiler";
            case "SEGUROS" -> "Seguros";
            case "SUMINISTROS" -> "Suministros";
            case "PRODUCTOS" -> "Productos de limpieza";
            case "MANTENIMIENTO" -> "Mantenimiento";
            case "REPARACIONES" -> "Reparaciones";
            case "COMBUSTIBLE" -> "Combustible";
            case "PERSONAL" -> "Personal";
            case "SEGURIDAD_SOCIAL" -> "Seguridad Social";
            case "IMPUESTOS" -> "Impuestos";
            case "TELEFONIA" -> "Telefonía/Internet";
            case "PUBLICIDAD" -> "Publicidad";
            case "MATERIAL_OFICINA" -> "Material de oficina";
            case "GESTORIA" -> "Gestoría";
            case "BANCARIOS" -> "Gastos bancarios";
            case "VEHICULOS" -> "Vehículos";
            case "MAQUINARIA" -> "Maquinaria";
            case "OTROS" -> "Otros";
            default -> categoria;
        };
    }

    private BigDecimal getBigDecimal(Object obj) {
        if (obj == null)
            return BigDecimal.ZERO;
        if (obj instanceof BigDecimal)
            return (BigDecimal) obj;
        if (obj instanceof Number)
            return new BigDecimal(obj.toString());
        return BigDecimal.ZERO;
    }

    private double getDouble(Object obj) {
        if (obj == null)
            return 0;
        if (obj instanceof Number)
            return ((Number) obj).doubleValue();
        return 0;
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(getStage());
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(getStage());
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

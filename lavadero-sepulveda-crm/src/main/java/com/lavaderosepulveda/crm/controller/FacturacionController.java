package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.api.service.FacturacionApiService;
import com.lavaderosepulveda.crm.model.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class FacturacionController {

    // Componentes generales
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabEmitidas;
    @FXML
    private Tab tabRecibidas;
    @FXML
    private Tab tabGastos;

    // === FACTURAS EMITIDAS ===
    @FXML
    private TextField txtBuscarEmitida;
    @FXML
    private ComboBox<String> cmbEstadoEmitida;
    @FXML
    private DatePicker dpDesdeEmitida;
    @FXML
    private DatePicker dpHastaEmitida;
    @FXML
    private TableView<FacturaEmitidaDTO> tablaEmitidas;
    @FXML
    private TableColumn<FacturaEmitidaDTO, String> colNumeroEmitida;
    @FXML
    private TableColumn<FacturaEmitidaDTO, String> colFechaEmitida;
    @FXML
    private TableColumn<FacturaEmitidaDTO, String> colClienteEmitida;
    @FXML
    private TableColumn<FacturaEmitidaDTO, String> colConceptoEmitida;
    @FXML
    private TableColumn<FacturaEmitidaDTO, String> colBaseEmitida;
    @FXML
    private TableColumn<FacturaEmitidaDTO, String> colIvaEmitida;
    @FXML
    private TableColumn<FacturaEmitidaDTO, String> colTotalEmitida;
    @FXML
    private TableColumn<FacturaEmitidaDTO, String> colEstadoEmitida;
    @FXML
    private TableColumn<FacturaEmitidaDTO, Void> colAccionesEmitida;
    @FXML
    private Label lblTotalBaseEmitidas;
    @FXML
    private Label lblTotalIvaEmitidas;
    @FXML
    private Label lblTotalEmitidas;

    // === FACTURAS RECIBIDAS ===
    @FXML
    private TextField txtBuscarRecibida;
    @FXML
    private ComboBox<String> cmbCategoriaRecibida;
    @FXML
    private ComboBox<String> cmbEstadoRecibida;
    @FXML
    private DatePicker dpDesdeRecibida;
    @FXML
    private DatePicker dpHastaRecibida;
    @FXML
    private TableView<FacturaRecibidaDTO> tablaRecibidas;
    @FXML
    private TableColumn<FacturaRecibidaDTO, String> colNumeroRecibida;
    @FXML
    private TableColumn<FacturaRecibidaDTO, String> colFechaRecibida;
    @FXML
    private TableColumn<FacturaRecibidaDTO, String> colProveedorRecibida;
    @FXML
    private TableColumn<FacturaRecibidaDTO, String> colCategoriaRecibida;
    @FXML
    private TableColumn<FacturaRecibidaDTO, String> colConceptoRecibida;
    @FXML
    private TableColumn<FacturaRecibidaDTO, String> colBaseRecibida;
    @FXML
    private TableColumn<FacturaRecibidaDTO, String> colIvaRecibida;
    @FXML
    private TableColumn<FacturaRecibidaDTO, String> colTotalRecibida;
    @FXML
    private TableColumn<FacturaRecibidaDTO, String> colEstadoRecibida;
    @FXML
    private TableColumn<FacturaRecibidaDTO, String> colVencimientoRecibida;
    @FXML
    private TableColumn<FacturaRecibidaDTO, Void> colAccionesRecibida;
    @FXML
    private Label lblPendientesRecibidas;
    @FXML
    private Label lblTotalBaseRecibidas;
    @FXML
    private Label lblTotalIvaRecibidas;
    @FXML
    private Label lblTotalRecibidas;

    // === GASTOS ===
    @FXML
    private TextField txtBuscarGasto;
    @FXML
    private ComboBox<String> cmbCategoriaGasto;
    @FXML
    private DatePicker dpDesdeGasto;
    @FXML
    private DatePicker dpHastaGasto;
    @FXML
    private CheckBox chkSoloRecurrentes;
    @FXML
    private TableView<GastoDTO> tablaGastos;
    @FXML
    private TableColumn<GastoDTO, String> colFechaGasto;
    @FXML
    private TableColumn<GastoDTO, String> colConceptoGasto;
    @FXML
    private TableColumn<GastoDTO, String> colCategoriaGasto;
    @FXML
    private TableColumn<GastoDTO, String> colImporteGasto;
    @FXML
    private TableColumn<GastoDTO, String> colBaseGasto;
    @FXML
    private TableColumn<GastoDTO, String> colIvaGasto;
    @FXML
    private TableColumn<GastoDTO, String> colMetodoPagoGasto;
    @FXML
    private TableColumn<GastoDTO, String> colRecurrenteGasto;
    @FXML
    private TableColumn<GastoDTO, String> colPagadoGasto;
    @FXML
    private TableColumn<GastoDTO, Void> colAccionesGasto;
    @FXML
    private Label lblNumGastos;
    @FXML
    private Label lblTotalBaseGastos;
    @FXML
    private Label lblTotalIvaGastos;
    @FXML
    private Label lblTotalGastos;

    private FacturacionApiService apiService;
    private ObservableList<FacturaEmitidaDTO> listaEmitidas = FXCollections.observableArrayList();
    private ObservableList<FacturaRecibidaDTO> listaRecibidas = FXCollections.observableArrayList();
    private ObservableList<GastoDTO> listaGastos = FXCollections.observableArrayList();

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat FORMATO_MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

    @FXML
    public void initialize() {
        apiService = FacturacionApiService.getInstance();

        configurarCombos();
        configurarTablaEmitidas();
        configurarTablaRecibidas();
        configurarTablaGastos();

        // Cargar datos iniciales
        cargarFacturasEmitidas();
        cargarFacturasRecibidas();
        cargarGastos();
    }

    private void configurarCombos() {
        // Estados de factura
        cmbEstadoEmitida.setItems(FXCollections.observableArrayList("Todos", "PENDIENTE", "PAGADA"));
        cmbEstadoEmitida.setValue("Todos");

        cmbEstadoRecibida.setItems(FXCollections.observableArrayList("Todos", "PENDIENTE", "PAGADA"));
        cmbEstadoRecibida.setValue("Todos");

        // Categorías de gasto
        ObservableList<String> categorias = FXCollections.observableArrayList(
                "Todas", "AGUA", "LUZ", "GAS", "ALQUILER", "SEGUROS", "SUMINISTROS",
                "PRODUCTOS", "MANTENIMIENTO", "REPARACIONES", "COMBUSTIBLE", "PERSONAL",
                "SEGURIDAD_SOCIAL", "IMPUESTOS", "TELEFONIA", "PUBLICIDAD",
                "MATERIAL_OFICINA", "GESTORIA", "BANCARIOS", "VEHICULOS", "MAQUINARIA", "OTROS");
        cmbCategoriaRecibida.setItems(categorias);
        cmbCategoriaRecibida.setValue("Todas");
        cmbCategoriaGasto.setItems(categorias);
        cmbCategoriaGasto.setValue("Todas");
    }

    // === CONFIGURACIÓN TABLAS ===

    private void configurarTablaEmitidas() {
        // CORREGIDO: Usar lambdas con los nombres correctos del DTO
        colNumeroEmitida.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getNumeroFactura()));
        colFechaEmitida.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getFechaEmision()));
        colClienteEmitida.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getClienteNombre()));
        colConceptoEmitida.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getConcepto()));
        colBaseEmitida.setCellValueFactory(
                cellData -> new SimpleStringProperty(formatearMoneda(cellData.getValue().getBaseImponible())));
        colIvaEmitida.setCellValueFactory(
                cellData -> new SimpleStringProperty(formatearMoneda(cellData.getValue().getCuotaIva())));
        colTotalEmitida.setCellValueFactory(
                cellData -> new SimpleStringProperty(formatearMoneda(cellData.getValue().getTotal())));
        colEstadoEmitida.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getEstado()));

        configurarAccionesEmitidas();
        tablaEmitidas.setItems(listaEmitidas);
    }

    private void configurarTablaRecibidas() {
        colNumeroRecibida.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getNumeroFactura()));
        colFechaRecibida.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getFechaFactura()));
        colProveedorRecibida.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getProveedorNombre()));
        colCategoriaRecibida.setCellValueFactory(
                cellData -> new SimpleStringProperty(obtenerDescripcionCategoria(cellData.getValue().getCategoria())));
        colConceptoRecibida.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getConcepto()));
        colBaseRecibida.setCellValueFactory(
                cellData -> new SimpleStringProperty(formatearMoneda(cellData.getValue().getBaseImponible())));
        colIvaRecibida.setCellValueFactory(
                cellData -> new SimpleStringProperty(formatearMoneda(cellData.getValue().getCuotaIva())));
        colTotalRecibida.setCellValueFactory(
                cellData -> new SimpleStringProperty(formatearMoneda(cellData.getValue().getTotal())));
        colEstadoRecibida.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getEstado()));
        colVencimientoRecibida.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getFechaVencimiento()));

        configurarAccionesRecibidas();
        tablaRecibidas.setItems(listaRecibidas);
    }

    private void configurarTablaGastos() {
        colFechaGasto.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getFecha()));
        colConceptoGasto.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getConcepto()));
        colCategoriaGasto.setCellValueFactory(
                cellData -> new SimpleStringProperty(obtenerDescripcionCategoria(cellData.getValue().getCategoria())));
        colImporteGasto.setCellValueFactory(
                cellData -> new SimpleStringProperty(formatearMoneda(cellData.getValue().getImporte())));
        colBaseGasto.setCellValueFactory(
                cellData -> new SimpleStringProperty(formatearMoneda(cellData.getValue().getBaseImponible())));
        colIvaGasto.setCellValueFactory(
                cellData -> new SimpleStringProperty(formatearMoneda(cellData.getValue().getCuotaIva())));
        colMetodoPagoGasto.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getMetodoPago()));
        colRecurrenteGasto.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getRecurrente() != null && cellData.getValue().getRecurrente() ? "Sí"
                                : "No"));
        colPagadoGasto.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getPagado() != null && cellData.getValue().getPagado() ? "✅" : "⏳"));

        configurarAccionesGastos();
        tablaGastos.setItems(listaGastos);
    }

    private void configurarAccionesEmitidas() {
        colAccionesEmitida.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button("👁");
            private final Button btnPdf = new Button("📄");
            private final Button btnPagar = new Button("💰");
            private final HBox contenedor = new HBox(5, btnVer, btnPdf, btnPagar);

            {
                btnVer.setOnAction(e -> verFacturaEmitida(getTableView().getItems().get(getIndex())));
                btnPdf.setOnAction(e -> descargarPdfEmitida(getTableView().getItems().get(getIndex())));
                btnPagar.setOnAction(e -> marcarPagadaEmitida(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }

    private void configurarAccionesRecibidas() {
        colAccionesRecibida.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnPagar = new Button("💰");
            private final Button btnEliminar = new Button("🗑️");
            private final HBox contenedor = new HBox(5, btnEditar, btnPagar, btnEliminar);

            {
                btnEditar.setOnAction(e -> editarFacturaRecibida(getTableView().getItems().get(getIndex())));
                btnPagar.setOnAction(e -> marcarPagadaRecibida(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> eliminarFacturaRecibida(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }

    private void configurarAccionesGastos() {
        colAccionesGasto.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnPagar = new Button("💰");
            private final Button btnEliminar = new Button("🗑️");
            private final HBox contenedor = new HBox(5, btnEditar, btnPagar, btnEliminar);

            {
                btnEditar.setOnAction(e -> editarGasto(getTableView().getItems().get(getIndex())));
                btnPagar.setOnAction(e -> marcarPagadoGasto(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> eliminarGasto(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }

    // === CARGA DE DATOS ===

    private void cargarFacturasEmitidas() {
        try {
            List<FacturaEmitidaDTO> facturas = apiService.obtenerFacturasEmitidas();
            // Debug: imprimir datos recibidos
            System.out.println("=== DEBUG: Facturas emitidas cargadas: " + facturas.size() + " ===");
            if (!facturas.isEmpty()) {
                FacturaEmitidaDTO primera = facturas.get(0);
                System.out.println("Primera factura - Número: " + primera.getNumeroFactura()
                        + ", Fecha: " + primera.getFechaEmision()
                        + ", Cliente: " + primera.getClienteNombre()
                        + ", Concepto: " + primera.getConcepto()
                        + ", Total: " + primera.getTotal());
            }
            listaEmitidas.setAll(facturas);
            actualizarTotalesEmitidas();
        } catch (Exception e) {
            System.err.println("Error cargando facturas: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al cargar facturas emitidas", e.getMessage());
        }
    }

    private void cargarFacturasRecibidas() {
        try {
            List<FacturaRecibidaDTO> facturas = apiService.obtenerFacturasRecibidas();
            listaRecibidas.setAll(facturas);
            actualizarTotalesRecibidas();
        } catch (Exception e) {
            mostrarError("Error al cargar facturas recibidas", e.getMessage());
        }
    }

    private void cargarGastos() {
        try {
            List<GastoDTO> gastos = apiService.obtenerGastos();
            listaGastos.setAll(gastos);
            actualizarTotalesGastos();
        } catch (Exception e) {
            mostrarError("Error al cargar gastos", e.getMessage());
        }
    }

    // === ACCIONES FACTURAS EMITIDAS ===

    @FXML
    private void buscarFacturaEmitida() {
        String termino = txtBuscarEmitida.getText();
        if (termino == null || termino.isEmpty()) {
            cargarFacturasEmitidas();
        } else {
            try {
                List<FacturaEmitidaDTO> resultado = apiService.buscarFacturasEmitidas(termino);
                listaEmitidas.setAll(resultado);
                actualizarTotalesEmitidas();
            } catch (Exception e) {
                mostrarError("Error en búsqueda", e.getMessage());
            }
        }
    }

    @FXML
    private void filtrarFacturasEmitidas() {
        String estado = cmbEstadoEmitida.getValue();
        LocalDate desde = dpDesdeEmitida.getValue();
        LocalDate hasta = dpHastaEmitida.getValue();

        try {
            List<FacturaEmitidaDTO> resultado = apiService.filtrarFacturasEmitidas(estado, desde, hasta);
            listaEmitidas.setAll(resultado);
            actualizarTotalesEmitidas();
        } catch (Exception e) {
            mostrarError("Error al filtrar", e.getMessage());
        }
    }

    @FXML
    private void nuevaFacturaEmitida() {
        // TODO: Abrir diálogo de nueva factura emitida
        mostrarInfo("Nueva Factura", "Funcionalidad en desarrollo");
    }

    private void verFacturaEmitida(FacturaEmitidaDTO factura) {
        // TODO: Mostrar detalle de factura
    }

    private void descargarPdfEmitida(FacturaEmitidaDTO factura) {
        try {
            apiService.descargarPdfFacturaEmitida(factura.getId());
            mostrarInfo("PDF Generado", "La factura se ha descargado correctamente");
        } catch (Exception e) {
            mostrarError("Error al generar PDF", e.getMessage());
        }
    }

    private void marcarPagadaEmitida(FacturaEmitidaDTO factura) {
        if ("PAGADA".equals(factura.getEstado())) {
            mostrarInfo("Factura Pagada", "Esta factura ya está marcada como pagada");
            return;
        }

        Optional<String> metodo = mostrarDialogoMetodoPago();
        metodo.ifPresent(m -> {
            try {
                apiService.marcarFacturaEmitidaPagada(factura.getId(), m);
                cargarFacturasEmitidas();
                mostrarInfo("Éxito", "Factura marcada como pagada");
            } catch (Exception e) {
                mostrarError("Error", e.getMessage());
            }
        });
    }

    // === ACCIONES FACTURAS RECIBIDAS ===

    @FXML
    private void buscarFacturaRecibida() {
        String termino = txtBuscarRecibida.getText();
        if (termino == null || termino.isEmpty()) {
            cargarFacturasRecibidas();
        } else {
            try {
                List<FacturaRecibidaDTO> resultado = apiService.buscarFacturasRecibidas(termino);
                listaRecibidas.setAll(resultado);
                actualizarTotalesRecibidas();
            } catch (Exception e) {
                mostrarError("Error en búsqueda", e.getMessage());
            }
        }
    }

    @FXML
    private void filtrarFacturasRecibidas() {
        String categoria = cmbCategoriaRecibida.getValue();
        String estado = cmbEstadoRecibida.getValue();
        LocalDate desde = dpDesdeRecibida.getValue();
        LocalDate hasta = dpHastaRecibida.getValue();

        try {
            List<FacturaRecibidaDTO> resultado = apiService.filtrarFacturasRecibidas(categoria, estado, desde, hasta);
            listaRecibidas.setAll(resultado);
            actualizarTotalesRecibidas();
        } catch (Exception e) {
            mostrarError("Error al filtrar", e.getMessage());
        }
    }

    @FXML
    private void nuevaFacturaRecibida() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formulario-factura-recibida.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Nueva Factura Recibida");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarFacturasRecibidas();
        } catch (Exception e) {
            mostrarError("Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    @FXML
    private void gestionarProveedores() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/proveedores.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Gestión de Proveedores");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            mostrarError("Error", "No se pudo abrir la gestión de proveedores: " + e.getMessage());
        }
    }

    private void editarFacturaRecibida(FacturaRecibidaDTO factura) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formulario-factura-recibida.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Editar Factura Recibida");
            stage.setScene(new Scene(loader.load()));

            FormularioFacturaRecibidaController controller = loader.getController();
            controller.setFactura(factura);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarFacturasRecibidas();
        } catch (Exception e) {
            mostrarError("Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    private void marcarPagadaRecibida(FacturaRecibidaDTO factura) {
        if ("PAGADA".equals(factura.getEstado())) {
            mostrarInfo("Factura Pagada", "Esta factura ya está marcada como pagada");
            return;
        }

        Optional<String> metodo = mostrarDialogoMetodoPago();
        metodo.ifPresent(m -> {
            try {
                apiService.marcarFacturaRecibidaPagada(factura.getId(), m);
                cargarFacturasRecibidas();
                mostrarInfo("Éxito", "Factura marcada como pagada");
            } catch (Exception e) {
                mostrarError("Error", e.getMessage());
            }
        });
    }

    private void eliminarFacturaRecibida(FacturaRecibidaDTO factura) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar factura " + factura.getNumeroFactura() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    apiService.eliminarFacturaRecibida(factura.getId());
                    cargarFacturasRecibidas();
                    mostrarInfo("Éxito", "Factura eliminada correctamente");
                } catch (Exception e) {
                    mostrarError("Error", e.getMessage());
                }
            }
        });
    }

    // === ACCIONES GASTOS ===

    @FXML
    private void buscarGasto() {
        String termino = txtBuscarGasto.getText();
        if (termino == null || termino.isEmpty()) {
            cargarGastos();
        } else {
            try {
                List<GastoDTO> resultado = apiService.buscarGastos(termino);
                listaGastos.setAll(resultado);
                actualizarTotalesGastos();
            } catch (Exception e) {
                mostrarError("Error en búsqueda", e.getMessage());
            }
        }
    }

    @FXML
    private void filtrarGastos() {
        String categoria = cmbCategoriaGasto.getValue();
        LocalDate desde = dpDesdeGasto.getValue();
        LocalDate hasta = dpHastaGasto.getValue();
        boolean soloRecurrentes = chkSoloRecurrentes.isSelected();

        try {
            List<GastoDTO> resultado = apiService.filtrarGastos(categoria, desde, hasta, soloRecurrentes);
            listaGastos.setAll(resultado);
            actualizarTotalesGastos();
        } catch (Exception e) {
            mostrarError("Error al filtrar", e.getMessage());
        }
    }

    @FXML
    private void nuevoGasto() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formulario-gasto.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Nuevo Gasto");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarGastos();
        } catch (Exception e) {
            mostrarError("Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    private void editarGasto(GastoDTO gasto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formulario-gasto.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Editar Gasto");
            stage.setScene(new Scene(loader.load()));

            FormularioGastoController controller = loader.getController();
            controller.setGasto(gasto);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarGastos();
        } catch (Exception e) {
            mostrarError("Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    private void marcarPagadoGasto(GastoDTO gasto) {
        if (gasto.getPagado() != null && gasto.getPagado()) {
            mostrarInfo("Gasto Pagado", "Este gasto ya está marcado como pagado");
            return;
        }

        Optional<String> metodo = mostrarDialogoMetodoPago();
        metodo.ifPresent(m -> {
            try {
                apiService.marcarGastoPagado(gasto.getId(), m);
                cargarGastos();
                mostrarInfo("Éxito", "Gasto marcado como pagado");
            } catch (Exception e) {
                mostrarError("Error", e.getMessage());
            }
        });
    }

    private void eliminarGasto(GastoDTO gasto) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar gasto: " + gasto.getConcepto() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    apiService.eliminarGasto(gasto.getId());
                    cargarGastos();
                    mostrarInfo("Éxito", "Gasto eliminado correctamente");
                } catch (Exception e) {
                    mostrarError("Error", e.getMessage());
                }
            }
        });
    }

    // === RESUMEN FINANCIERO ===

    @FXML
    private void mostrarResumenFinanciero() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/resumen-financiero.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Resumen Financiero");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) {
            mostrarError("Error", "No se pudo abrir el resumen: " + e.getMessage());
        }
    }

    // === TOTALES ===

    private void actualizarTotalesEmitidas() {
        BigDecimal base = listaEmitidas.stream()
                .map(FacturaEmitidaDTO::getBaseImponible)
                .filter(b -> b != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal iva = listaEmitidas.stream()
                .map(FacturaEmitidaDTO::getCuotaIva)
                .filter(i -> i != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = listaEmitidas.stream()
                .map(FacturaEmitidaDTO::getTotal)
                .filter(t -> t != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblTotalBaseEmitidas.setText("Base: " + formatearMoneda(base));
        lblTotalIvaEmitidas.setText("IVA: " + formatearMoneda(iva));
        lblTotalEmitidas.setText("Total: " + formatearMoneda(total));
    }

    private void actualizarTotalesRecibidas() {
        long pendientes = listaRecibidas.stream()
                .filter(f -> "PENDIENTE".equals(f.getEstado()))
                .count();

        BigDecimal base = listaRecibidas.stream()
                .map(FacturaRecibidaDTO::getBaseImponible)
                .filter(b -> b != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal iva = listaRecibidas.stream()
                .map(FacturaRecibidaDTO::getCuotaIva)
                .filter(i -> i != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = listaRecibidas.stream()
                .map(FacturaRecibidaDTO::getTotal)
                .filter(t -> t != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblPendientesRecibidas.setText("Pendientes: " + pendientes);
        lblTotalBaseRecibidas.setText("Base: " + formatearMoneda(base));
        lblTotalIvaRecibidas.setText("IVA Soportado: " + formatearMoneda(iva));
        lblTotalRecibidas.setText("Total: " + formatearMoneda(total));
    }

    private void actualizarTotalesGastos() {
        lblNumGastos.setText("Gastos: " + listaGastos.size());

        BigDecimal base = listaGastos.stream()
                .map(GastoDTO::getBaseImponible)
                .filter(b -> b != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal iva = listaGastos.stream()
                .map(GastoDTO::getCuotaIva)
                .filter(i -> i != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = listaGastos.stream()
                .map(GastoDTO::getImporte)
                .filter(t -> t != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblTotalBaseGastos.setText("Base: " + formatearMoneda(base));
        lblTotalIvaGastos.setText("IVA Soportado: " + formatearMoneda(iva));
        lblTotalGastos.setText("Total: " + formatearMoneda(total));
    }

    // === UTILIDADES ===

    private String formatearMoneda(BigDecimal cantidad) {
        if (cantidad == null)
            return "0,00 €";
        return FORMATO_MONEDA.format(cantidad);
    }

    private String obtenerDescripcionCategoria(String categoria) {
        if (categoria == null)
            return "";
        switch (categoria) {
            case "AGUA":
                return "Agua";
            case "LUZ":
                return "Electricidad";
            case "GAS":
                return "Gas";
            case "ALQUILER":
                return "Alquiler";
            case "SEGUROS":
                return "Seguros";
            case "SUMINISTROS":
                return "Suministros";
            case "PRODUCTOS":
                return "Productos químicos";
            case "MANTENIMIENTO":
                return "Mantenimiento";
            case "REPARACIONES":
                return "Reparaciones";
            case "COMBUSTIBLE":
                return "Combustible";
            case "PERSONAL":
                return "Personal";
            case "SEGURIDAD_SOCIAL":
                return "Seg. Social";
            case "IMPUESTOS":
                return "Impuestos";
            case "TELEFONIA":
                return "Telefonía";
            case "PUBLICIDAD":
                return "Publicidad";
            case "MATERIAL_OFICINA":
                return "Material oficina";
            case "GESTORIA":
                return "Gestoría";
            case "BANCARIOS":
                return "Gastos bancarios";
            case "VEHICULOS":
                return "Vehículos";
            case "MAQUINARIA":
                return "Maquinaria";
            default:
                return "Otros";
        }
    }

    private Optional<String> mostrarDialogoMetodoPago() {
        ChoiceDialog<String> dialogo = new ChoiceDialog<>("EFECTIVO", "EFECTIVO", "TARJETA", "BIZUM", "TRANSFERENCIA");
        dialogo.setTitle("Método de Pago");
        dialogo.setHeaderText("Selecciona el método de pago");
        dialogo.setContentText("Método:");
        return dialogo.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
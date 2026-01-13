package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.api.service.FacturacionApiService;
import com.lavaderosepulveda.crm.model.dto.*;
import com.lavaderosepulveda.crm.model.entity.*;
import com.lavaderosepulveda.crm.model.enums.*;
import com.lavaderosepulveda.crm.model.dto.ClienteDTO;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
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
            log.info("=== DEBUG: Facturas emitidas cargadas: {} ===", facturas.size());
            if (!facturas.isEmpty()) {
                FacturaEmitidaDTO primera = facturas.get(0);
                log.info("Primera factura - Número: {}, Fecha: {}, Cliente: {}, Concepto: {}, Total: {}",
                        primera.getNumeroFactura(), primera.getFechaEmision(), primera.getClienteNombre(),
                        primera.getConcepto(), primera.getTotal());
            }
            listaEmitidas.setAll(facturas);
            actualizarTotalesEmitidas();
        } catch (Exception e) {
            log.error("Error cargando facturas", e);
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
        Dialog<FacturaEmitidaDTO> dialog = new Dialog<>();
        dialog.setTitle("Nueva Factura Emitida");
        dialog.setHeaderText("Crear nueva factura");
        dialog.initOwner(tablaEmitidas.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        ButtonType btnCrear = new ButtonType("Crear Factura", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnCrear, ButtonType.CANCEL);
        
        // Contenedor principal
        VBox contenido = new VBox(15);
        contenido.setPadding(new Insets(20));
        
        // === TIPO DE FACTURA ===
        HBox boxTipo = new HBox(10);
        boxTipo.setAlignment(Pos.CENTER_LEFT);
        Label lblTipo = new Label("Tipo de Factura:");
        lblTipo.setStyle("-fx-font-weight: bold;");
        ComboBox<String> cmbTipo = new ComboBox<>();
        cmbTipo.setItems(FXCollections.observableArrayList("SIMPLIFICADA", "COMPLETA"));
        cmbTipo.setValue("SIMPLIFICADA");
        cmbTipo.setPrefWidth(200);
        boxTipo.getChildren().addAll(lblTipo, cmbTipo);
        
        // === DATOS DEL CLIENTE ===
        TitledPane panelCliente = new TitledPane();
        panelCliente.setText("Datos del Cliente");
        panelCliente.setCollapsible(false);
        
        VBox vboxCliente = new VBox(10);
        vboxCliente.setPadding(new Insets(10));
        
        // Selector de cliente existente
        HBox boxSelectorCliente = new HBox(10);
        boxSelectorCliente.setAlignment(Pos.CENTER_LEFT);
        Label lblSeleccionar = new Label("Cliente:");
        ComboBox<ClienteDTO> cmbCliente = new ComboBox<>();
        cmbCliente.setPromptText("Seleccionar cliente existente...");
        cmbCliente.setPrefWidth(350);
        
        // Cargar clientes
        try {
            List<ClienteDTO> clientes = apiService.obtenerClientes();
            cmbCliente.setItems(FXCollections.observableArrayList(clientes));
        } catch (Exception ex) {
            log.error("Error al cargar clientes", ex);
        }
        
        // Converter para mostrar nombre del cliente
        cmbCliente.setConverter(new javafx.util.StringConverter<ClienteDTO>() {
            @Override
            public String toString(ClienteDTO cliente) {
                if (cliente == null) return "";
                String nif = cliente.getNif() != null && !cliente.getNif().isEmpty() 
                    ? " (" + cliente.getNif() + ")" : "";
                return cliente.getNombreCompleto() + nif;
            }
            @Override
            public ClienteDTO fromString(String string) { return null; }
        });
        
        Button btnLimpiarCliente = new Button("✖ Limpiar");
        btnLimpiarCliente.setOnAction(e -> {
            cmbCliente.setValue(null);
        });
        
        boxSelectorCliente.getChildren().addAll(lblSeleccionar, cmbCliente, btnLimpiarCliente);
        
        // Separador
        Label lblManual = new Label("O introducir manualmente:");
        lblManual.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
        
        GridPane gridCliente = new GridPane();
        gridCliente.setHgap(10);
        gridCliente.setVgap(10);
        
        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre del cliente");
        txtNombre.setPrefWidth(300);
        
        TextField txtNif = new TextField();
        txtNif.setPromptText("NIF/CIF");
        txtNif.setPrefWidth(150);
        
        TextField txtDireccion = new TextField();
        txtDireccion.setPromptText("Dirección completa");
        txtDireccion.setPrefWidth(300);
        
        TextField txtTelefono = new TextField();
        txtTelefono.setPromptText("Teléfono");
        txtTelefono.setPrefWidth(150);
        
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email");
        txtEmail.setPrefWidth(200);
        
        gridCliente.add(new Label("Nombre:"), 0, 0);
        gridCliente.add(txtNombre, 1, 0, 2, 1);
        gridCliente.add(new Label("NIF:"), 0, 1);
        gridCliente.add(txtNif, 1, 1);
        gridCliente.add(new Label("Teléfono:"), 2, 1);
        gridCliente.add(txtTelefono, 3, 1);
        gridCliente.add(new Label("Dirección:"), 0, 2);
        gridCliente.add(txtDireccion, 1, 2, 3, 1);
        gridCliente.add(new Label("Email:"), 0, 3);
        gridCliente.add(txtEmail, 1, 3, 2, 1);
        
        vboxCliente.getChildren().addAll(boxSelectorCliente, lblManual, gridCliente);
        panelCliente.setContent(vboxCliente);
        
        // Listener para rellenar campos al seleccionar cliente
        cmbCliente.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtNombre.setText(newVal.getNombreCompleto() != null ? newVal.getNombreCompleto() : "");
                txtNif.setText(newVal.getNif() != null ? newVal.getNif() : "");
                txtDireccion.setText(newVal.getDireccionCompleta() != null ? newVal.getDireccionCompleta() : "");
                txtTelefono.setText(newVal.getTelefono() != null ? newVal.getTelefono() : "");
                txtEmail.setText(newVal.getEmail() != null ? newVal.getEmail() : "");
                // Desactivar edición si hay cliente seleccionado
                txtNombre.setDisable(true);
                txtNif.setDisable(true);
                txtDireccion.setDisable(true);
                txtTelefono.setDisable(true);
                txtEmail.setDisable(true);
            } else {
                // Limpiar y habilitar campos
                txtNombre.clear();
                txtNif.clear();
                txtDireccion.clear();
                txtTelefono.clear();
                txtEmail.clear();
                txtNombre.setDisable(false);
                boolean esCompleta = "COMPLETA".equals(cmbTipo.getValue());
                txtNif.setDisable(!esCompleta);
                txtDireccion.setDisable(!esCompleta);
                txtTelefono.setDisable(false);
                txtEmail.setDisable(false);
            }
        });
        
        // Mostrar/ocultar campos según tipo (solo si no hay cliente seleccionado)
        txtNif.setDisable(true);
        txtDireccion.setDisable(true);
        cmbTipo.setOnAction(e -> {
            if (cmbCliente.getValue() == null) {
                boolean esCompleta = "COMPLETA".equals(cmbTipo.getValue());
                txtNif.setDisable(!esCompleta);
                txtDireccion.setDisable(!esCompleta);
                if (!esCompleta) {
                    txtNif.clear();
                    txtDireccion.clear();
                }
            }
        });
        
        // === LÍNEAS DE FACTURA ===
        TitledPane panelLineas = new TitledPane();
        panelLineas.setText("Líneas de Factura");
        panelLineas.setCollapsible(false);
        
        VBox boxLineas = new VBox(10);
        boxLineas.setPadding(new Insets(10));
        
        // Tabla de líneas
        TableView<LineaFacturaTemp> tablaLineas = new TableView<>();
        tablaLineas.setPrefHeight(150);
        
        ObservableList<LineaFacturaTemp> lineas = FXCollections.observableArrayList();
        
        TableColumn<LineaFacturaTemp, String> colConceptoLinea = new TableColumn<>("Concepto");
        colConceptoLinea.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getConcepto()));
        colConceptoLinea.setPrefWidth(250);
        
        TableColumn<LineaFacturaTemp, String> colCantidadLinea = new TableColumn<>("Cant.");
        colCantidadLinea.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCantidad())));
        colCantidadLinea.setPrefWidth(50);
        
        TableColumn<LineaFacturaTemp, String> colPrecioLinea = new TableColumn<>("Precio");
        colPrecioLinea.setCellValueFactory(c -> new SimpleStringProperty(formatearMoneda(c.getValue().getPrecio())));
        colPrecioLinea.setPrefWidth(80);
        
        TableColumn<LineaFacturaTemp, String> colSubtotalLinea = new TableColumn<>("Subtotal");
        colSubtotalLinea.setCellValueFactory(c -> new SimpleStringProperty(formatearMoneda(c.getValue().getSubtotal())));
        colSubtotalLinea.setPrefWidth(80);
        
        TableColumn<LineaFacturaTemp, Void> colEliminarLinea = new TableColumn<>("");
        colEliminarLinea.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("🗑");
            {
                btn.setOnAction(ev -> {
                    lineas.remove(getIndex());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        colEliminarLinea.setPrefWidth(40);
        
        tablaLineas.getColumns().addAll(colConceptoLinea, colCantidadLinea, colPrecioLinea, colSubtotalLinea, colEliminarLinea);
        tablaLineas.setItems(lineas);
        
        // Formulario para añadir línea
        HBox boxNuevaLinea = new HBox(10);
        boxNuevaLinea.setAlignment(Pos.CENTER_LEFT);
        
        TextField txtConcepto = new TextField();
        txtConcepto.setPromptText("Concepto");
        txtConcepto.setPrefWidth(200);
        
        Spinner<Integer> spnCantidad = new Spinner<>(1, 100, 1);
        spnCantidad.setPrefWidth(70);
        spnCantidad.setEditable(true);
        
        TextField txtPrecio = new TextField();
        txtPrecio.setPromptText("Precio");
        txtPrecio.setPrefWidth(80);
        
        Button btnAnadir = new Button("+ Añadir");
        btnAnadir.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        boxNuevaLinea.getChildren().addAll(
            new Label("Concepto:"), txtConcepto,
            new Label("Cant:"), spnCantidad,
            new Label("Precio:"), txtPrecio,
            btnAnadir
        );
        
        boxLineas.getChildren().addAll(tablaLineas, boxNuevaLinea);
        panelLineas.setContent(boxLineas);
        
        // === TOTALES ===
        TitledPane panelTotales = new TitledPane();
        panelTotales.setText("Totales");
        panelTotales.setCollapsible(false);
        
        GridPane gridTotales = new GridPane();
        gridTotales.setHgap(20);
        gridTotales.setVgap(8);
        gridTotales.setPadding(new Insets(10));
        
        Label lblBase = new Label("0,00 €");
        Label lblIva = new Label("0,00 €");
        Label lblTotalDialog = new Label("0,00 €");
        lblTotalDialog.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        
        gridTotales.add(new Label("Base Imponible:"), 0, 0);
        gridTotales.add(lblBase, 1, 0);
        gridTotales.add(new Label("IVA (21%):"), 0, 1);
        gridTotales.add(lblIva, 1, 1);
        gridTotales.add(new Label("TOTAL:"), 0, 2);
        gridTotales.add(lblTotalDialog, 1, 2);
        
        panelTotales.setContent(gridTotales);
        
        // === MÉTODO DE PAGO ===
        HBox boxPago = new HBox(10);
        boxPago.setAlignment(Pos.CENTER_LEFT);
        Label lblPago = new Label("Método de Pago:");
        lblPago.setStyle("-fx-font-weight: bold;");
        ComboBox<String> cmbMetodoPago = new ComboBox<>();
        cmbMetodoPago.setItems(FXCollections.observableArrayList(
            "PENDIENTE", "EFECTIVO", "TARJETA", "BIZUM", "TRANSFERENCIA"));
        cmbMetodoPago.setValue("PENDIENTE");
        cmbMetodoPago.setPrefWidth(150);
        boxPago.getChildren().addAll(lblPago, cmbMetodoPago);
        
        // Acción añadir línea
        btnAnadir.setOnAction(e -> {
            String concepto = txtConcepto.getText().trim();
            String precioStr = txtPrecio.getText().trim().replace(",", ".");
            
            if (concepto.isEmpty()) {
                mostrarError("Error", "El concepto es obligatorio");
                return;
            }
            
            try {
                BigDecimal precio = new BigDecimal(precioStr);
                int cantidad = spnCantidad.getValue();
                
                LineaFacturaTemp linea = new LineaFacturaTemp(concepto, cantidad, precio);
                lineas.add(linea);
                
                // Limpiar campos
                txtConcepto.clear();
                txtPrecio.clear();
                spnCantidad.getValueFactory().setValue(1);
                
                // Actualizar totales
                actualizarTotalesDialog(lineas, lblBase, lblIva, lblTotalDialog);
                
            } catch (NumberFormatException ex) {
                mostrarError("Error", "El precio debe ser un número válido");
            }
        });
        
        // Añadir todo al contenido
        contenido.getChildren().addAll(boxTipo, panelCliente, panelLineas, panelTotales, boxPago);
        
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setPrefSize(600, 550);
        
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().setPrefSize(650, 600);
        
        // Convertir resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnCrear) {
                if (lineas.isEmpty()) {
                    mostrarError("Error", "Debe añadir al menos una línea");
                    return null;
                }
                
                FacturaEmitidaDTO factura = new FacturaEmitidaDTO();
                factura.setTipoFactura(cmbTipo.getValue());
                factura.setClienteNombre(txtNombre.getText().trim().isEmpty() ? "Mostrador" : txtNombre.getText().trim());
                factura.setClienteNif(txtNif.getText().trim());
                factura.setClienteDireccion(txtDireccion.getText().trim());
                factura.setMetodoPago(cmbMetodoPago.getValue());
                
                return factura;
            }
            return null;
        });
        
        Optional<FacturaEmitidaDTO> result = dialog.showAndWait();
        
        result.ifPresent(facturaDTO -> {
            try {
                // Construir JSON
                StringBuilder jsonBuilder = new StringBuilder();
                jsonBuilder.append("{");
                jsonBuilder.append("\"tipo\":\"").append(facturaDTO.getTipoFactura()).append("\",");
                jsonBuilder.append("\"clienteNombre\":\"").append(escapeJson(facturaDTO.getClienteNombre())).append("\",");
                jsonBuilder.append("\"clienteNif\":\"").append(escapeJson(facturaDTO.getClienteNif() != null ? facturaDTO.getClienteNif() : "")).append("\",");
                jsonBuilder.append("\"clienteDireccion\":\"").append(escapeJson(facturaDTO.getClienteDireccion() != null ? facturaDTO.getClienteDireccion() : "")).append("\",");
                jsonBuilder.append("\"lineas\":[");
                
                for (int i = 0; i < lineas.size(); i++) {
                    LineaFacturaTemp l = lineas.get(i);
                    if (i > 0) jsonBuilder.append(",");
                    jsonBuilder.append("{");
                    jsonBuilder.append("\"concepto\":\"").append(escapeJson(l.getConcepto())).append("\",");
                    jsonBuilder.append("\"cantidad\":").append(l.getCantidad()).append(",");
                    jsonBuilder.append("\"precioUnitario\":").append(l.getPrecio());
                    jsonBuilder.append("}");
                }
                
                jsonBuilder.append("]}");
                
                String json = jsonBuilder.toString();
                log.info("Enviando factura: {}", json);
                
                // Llamar a la API
                String response = enviarFacturaManual(json);
                
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                FacturaEmitidaDTO nuevaFactura = mapper.readValue(response, FacturaEmitidaDTO.class);
                
                // Si no es pendiente, marcar como pagada
                String metodoPago = facturaDTO.getMetodoPago();
                if (!"PENDIENTE".equals(metodoPago)) {
                    apiService.marcarFacturaEmitidaPagada(nuevaFactura.getId(), metodoPago);
                }
                
                mostrarInfo("Factura Creada", 
                    "Factura " + nuevaFactura.getNumeroFactura() + " creada correctamente\n" +
                    "Estado: " + ("PENDIENTE".equals(metodoPago) ? "Pendiente de cobro" : "Pagada con " + metodoPago));
                cargarFacturasEmitidas();
                
            } catch (Exception e) {
                log.error("Error al crear factura", e);
                mostrarError("Error", "No se pudo crear la factura: " + e.getMessage());
            }
        });
    }

    private void verFacturaEmitida(FacturaEmitidaDTO factura) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detalle de Factura");
        dialog.setHeaderText("Factura " + factura.getNumeroFactura());
        dialog.initOwner(tablaEmitidas.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        // Contenedor principal
        VBox contenido = new VBox(15);
        contenido.setPadding(new Insets(20));
        contenido.setStyle("-fx-background-color: white;");
        
        // === CABECERA ===
        HBox cabecera = new HBox(20);
        cabecera.setAlignment(Pos.CENTER_LEFT);
        
        // Info factura
        VBox infoFactura = new VBox(5);
        Label lblNumero = new Label("Factura: " + factura.getNumeroFactura());
        lblNumero.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label lblFecha = new Label("Fecha: " + factura.getFechaEmision());
        Label lblTipoFact = new Label("Tipo: " + (factura.getTipoFactura() != null ? factura.getTipoFactura() : "SIMPLIFICADA"));
        infoFactura.getChildren().addAll(lblNumero, lblFecha, lblTipoFact);
        
        // Estado
        Label lblEstado = new Label(factura.getEstado());
        lblEstado.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 5 15; -fx-background-radius: 15; " +
            ("PAGADA".equals(factura.getEstado()) ? 
                "-fx-background-color: #d4edda; -fx-text-fill: #155724;" : 
                "-fx-background-color: #fff3cd; -fx-text-fill: #856404;"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        cabecera.getChildren().addAll(infoFactura, spacer, lblEstado);
        
        // === DATOS CLIENTE ===
        TitledPane panelCliente = new TitledPane();
        panelCliente.setText("Datos del Cliente");
        panelCliente.setCollapsible(false);
        
        GridPane gridCliente = new GridPane();
        gridCliente.setHgap(15);
        gridCliente.setVgap(8);
        gridCliente.setPadding(new Insets(10));
        
        gridCliente.add(new Label("Nombre:"), 0, 0);
        gridCliente.add(new Label(factura.getClienteNombre() != null ? factura.getClienteNombre() : "Mostrador"), 1, 0);
        
        gridCliente.add(new Label("NIF:"), 0, 1);
        gridCliente.add(new Label(factura.getClienteNif() != null && !factura.getClienteNif().isEmpty() ? factura.getClienteNif() : "-"), 1, 1);
        
        gridCliente.add(new Label("Dirección:"), 0, 2);
        gridCliente.add(new Label(factura.getClienteDireccion() != null && !factura.getClienteDireccion().isEmpty() ? factura.getClienteDireccion() : "-"), 1, 2);
        
        panelCliente.setContent(gridCliente);
        
        // === CONCEPTO / LÍNEAS ===
        TitledPane panelConcepto = new TitledPane();
        panelConcepto.setText("Concepto");
        panelConcepto.setCollapsible(false);
        
        VBox contenidoConcepto = new VBox(10);
        contenidoConcepto.setPadding(new Insets(10));
        
        String concepto = factura.getConcepto();
        if (concepto != null && !concepto.isEmpty()) {
            Label lblConcepto = new Label(concepto);
            lblConcepto.setWrapText(true);
            lblConcepto.setStyle("-fx-font-size: 13px;");
            contenidoConcepto.getChildren().add(lblConcepto);
        } else {
            contenidoConcepto.getChildren().add(new Label("Sin concepto especificado"));
        }
        
        panelConcepto.setContent(contenidoConcepto);
        
        // === IMPORTES ===
        TitledPane panelImportes = new TitledPane();
        panelImportes.setText("Importes");
        panelImportes.setCollapsible(false);
        
        GridPane gridImportes = new GridPane();
        gridImportes.setHgap(30);
        gridImportes.setVgap(8);
        gridImportes.setPadding(new Insets(10));
        
        gridImportes.add(new Label("Base Imponible:"), 0, 0);
        Label lblBaseImp = new Label(formatearMoneda(factura.getBaseImponible()));
        lblBaseImp.setStyle("-fx-font-weight: bold;");
        gridImportes.add(lblBaseImp, 1, 0);
        
        String tipoIva = factura.getTipoIva() != null ? factura.getTipoIva().toString() + "%" : "21%";
        gridImportes.add(new Label("IVA (" + tipoIva + "):"), 0, 1);
        Label lblIvaImp = new Label(formatearMoneda(factura.getCuotaIva()));
        gridImportes.add(lblIvaImp, 1, 1);
        
        Separator sep = new Separator();
        gridImportes.add(sep, 0, 2, 2, 1);
        
        gridImportes.add(new Label("TOTAL:"), 0, 3);
        Label lblTotalImp = new Label(formatearMoneda(factura.getTotal()));
        lblTotalImp.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        gridImportes.add(lblTotalImp, 1, 3);
        
        panelImportes.setContent(gridImportes);
        
        // === DATOS DE PAGO ===
        TitledPane panelPago = new TitledPane();
        panelPago.setText("Información de Pago");
        panelPago.setCollapsible(false);
        
        GridPane gridPago = new GridPane();
        gridPago.setHgap(15);
        gridPago.setVgap(8);
        gridPago.setPadding(new Insets(10));
        
        gridPago.add(new Label("Estado:"), 0, 0);
        gridPago.add(new Label(factura.getEstado()), 1, 0);
        
        gridPago.add(new Label("Método de Pago:"), 0, 1);
        gridPago.add(new Label(factura.getMetodoPago() != null ? factura.getMetodoPago() : "-"), 1, 1);
        
        gridPago.add(new Label("Fecha de Pago:"), 0, 2);
        gridPago.add(new Label(factura.getFechaPago() != null && !factura.getFechaPago().isEmpty() ? factura.getFechaPago() : "-"), 1, 2);
        
        panelPago.setContent(gridPago);
        
        // Añadir todo al contenido
        contenido.getChildren().addAll(cabecera, panelCliente, panelConcepto, panelImportes, panelPago);
        
        // Configurar scroll
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setPrefSize(500, 500);
        scroll.setStyle("-fx-background-color: white;");
        
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().setPrefSize(550, 550);
        
        dialog.showAndWait();
    }

    private void descargarPdfEmitida(FacturaEmitidaDTO factura) {
        try {
            byte[] pdfBytes = apiService.descargarPdfFacturaEmitida(factura.getId());
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Factura PDF");
            fileChooser.setInitialFileName("Factura_" + factura.getNumeroFactura().replace("/", "-") + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            
            File archivo = fileChooser.showSaveDialog(tablaEmitidas.getScene().getWindow());
            if (archivo != null) {
                try (FileOutputStream fos = new FileOutputStream(archivo)) {
                    fos.write(pdfBytes);
                }
                mostrarInfo("PDF Generado", "La factura se ha guardado en:\n" + archivo.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Error al descargar PDF", e);
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

    // === MÉTODOS AUXILIARES ===

    private String enviarFacturaManual(String json) throws IOException {
        java.net.URL url = new java.net.URL("https://lavadero-sepulveda-production.up.railway.app/api/facturas/manual");
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        
        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        
        int responseCode = conn.getResponseCode();
        java.io.BufferedReader reader;
        if (responseCode >= 400) {
            reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getErrorStream(), java.nio.charset.StandardCharsets.UTF_8));
        } else {
            reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
        }
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        if (responseCode >= 400) {
            throw new IOException("Error en la API (" + responseCode + "): " + response);
        }
        
        return response.toString();
    }
    
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    private void actualizarTotalesDialog(ObservableList<LineaFacturaTemp> lineas, Label lblBase, Label lblIva, Label lblTotal) {
        BigDecimal base = lineas.stream()
            .map(LineaFacturaTemp::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal iva = base.multiply(new BigDecimal("0.21")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = base.add(iva);
        
        if (lblBase != null) lblBase.setText(formatearMoneda(base));
        if (lblIva != null) lblIva.setText(formatearMoneda(iva));
        if (lblTotal != null) lblTotal.setText(formatearMoneda(total));
    }

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

    // === CLASE INTERNA PARA LÍNEAS TEMPORALES ===
    
    public static class LineaFacturaTemp {
        private String concepto;
        private int cantidad;
        private BigDecimal precio;
        
        public LineaFacturaTemp(String concepto, int cantidad, BigDecimal precio) {
            this.concepto = concepto;
            this.cantidad = cantidad;
            this.precio = precio;
        }
        
        public String getConcepto() { return concepto; }
        public int getCantidad() { return cantidad; }
        public BigDecimal getPrecio() { return precio; }
        public BigDecimal getSubtotal() { 
            return precio.multiply(new BigDecimal(cantidad)); 
        }
    }
}

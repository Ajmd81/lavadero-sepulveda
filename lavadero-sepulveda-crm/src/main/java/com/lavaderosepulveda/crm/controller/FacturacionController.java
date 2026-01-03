package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.api.dto.CitaDTO;
import com.lavaderosepulveda.crm.api.dto.ClienteDTO;
import com.lavaderosepulveda.crm.api.dto.FacturaDTO;
import com.lavaderosepulveda.crm.api.service.CitaApiService;
import com.lavaderosepulveda.crm.api.service.ClienteApiService;
import com.lavaderosepulveda.crm.api.service.FacturaApiService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FacturacionController {

    private static final Logger log = LoggerFactory.getLogger(FacturacionController.class);

    // Componentes de la cabecera
    @FXML
    private Label lblTotalFacturado;
    @FXML
    private Label lblPendienteCobro;
    @FXML
    private Label lblFacturasPendientes;

    // Filtros
    @FXML
    private DatePicker dpDesde;
    @FXML
    private DatePicker dpHasta;
    @FXML
    private CheckBox chkSoloPendientes;
    @FXML
    private TextField txtBuscar;

    // Tabla de facturas
    @FXML
    private TableView<FacturaDTO> tblFacturas;
    @FXML
    private TableColumn<FacturaDTO, String> colNumero;
    @FXML
    private TableColumn<FacturaDTO, String> colFecha;
    @FXML
    private TableColumn<FacturaDTO, String> colCliente;
    @FXML
    private TableColumn<FacturaDTO, String> colTipo;
    @FXML
    private TableColumn<FacturaDTO, String> colBase;
    @FXML
    private TableColumn<FacturaDTO, String> colIva;
    @FXML
    private TableColumn<FacturaDTO, String> colTotal;
    @FXML
    private TableColumn<FacturaDTO, String> colEstado;
    @FXML
    private TableColumn<FacturaDTO, Void> colAcciones;

    private final FacturaApiService facturaApiService = FacturaApiService.getInstance();
    private final CitaApiService citaApiService = CitaApiService.getInstance();
    private final ClienteApiService clienteApiService = ClienteApiService.getInstance();

    @FXML
    public void initialize() {
        log.info("Inicializando FacturacionController...");

        configurarFiltros();
        configurarTabla();
        cargarResumen();
        cargarFacturas();
    }

    private void configurarFiltros() {
        // Fechas por defecto: mes actual
        dpDesde.setValue(LocalDate.now().withDayOfMonth(1));
        dpHasta.setValue(LocalDate.now());

        // Listener para búsqueda en tiempo real
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.length() >= 2) {
                    buscarFacturas(newVal);
                } else if (newVal == null || newVal.isEmpty()) {
                    cargarFacturas();
                }
            });
        }
    }

    private void configurarTabla() {
        // Configurar columnas
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNombre"));
        colTipo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipoFormateado()));
        colBase.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getBaseImponibleFormateada()));
        colIva.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getImporteIvaFormateado()));
        colTotal.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTotalFormateado()));
        colEstado.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstadoFormateado()));

        // Estilo para columna de estado
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Pendiente".equals(item)) {
                        setStyle("-fx-background-color: #FFF3E0; -fx-text-fill: #E65100; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Columna de acciones
        colAcciones.setCellFactory(column -> new TableCell<>() {
            private final Button btnPagar = new Button("Cobrar");
            private final Button btnPdf = new Button("PDF");
            private final Button btnEliminar = new Button("🗑");
            private final HBox hbox = new HBox(5, btnPagar, btnPdf, btnEliminar);

            {
                btnPagar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
                btnPdf.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
                btnEliminar.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");

                btnPagar.setOnAction(e -> {
                    FacturaDTO factura = getTableView().getItems().get(getIndex());
                    cobrarFactura(factura);
                });

                btnPdf.setOnAction(e -> {
                    FacturaDTO factura = getTableView().getItems().get(getIndex());
                    descargarPdf(factura);
                });

                btnEliminar.setOnAction(e -> {
                    FacturaDTO factura = getTableView().getItems().get(getIndex());
                    eliminarFactura(factura);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    FacturaDTO factura = getTableView().getItems().get(getIndex());
                    // Ocultar botón cobrar si ya está pagada
                    btnPagar.setVisible("PENDIENTE".equals(factura.getEstado()));
                    // No permitir eliminar facturas pagadas
                    btnEliminar.setDisable("PAGADA".equals(factura.getEstado()));
                    setGraphic(hbox);
                }
            }
        });

        // Doble clic para ver detalles
        tblFacturas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                FacturaDTO factura = tblFacturas.getSelectionModel().getSelectedItem();
                if (factura != null) {
                    mostrarDetalleFactura(factura);
                }
            }
        });
    }

    private void cargarResumen() {
        new Thread(() -> {
            Map<String, Object> resumen = facturaApiService.getResumen();

            Platform.runLater(() -> {
                // Total facturado este mes
                Object totalMes = resumen.get("totalMes");
                lblTotalFacturado.setText(formatearImporte(totalMes));

                // Pendiente de cobro
                Object totalPendiente = resumen.get("totalPendiente");
                lblPendienteCobro.setText(formatearImporte(totalPendiente));

                // Facturas pendientes
                Object facturasPendientes = resumen.get("facturasPendientes");
                lblFacturasPendientes.setText(
                        facturasPendientes != null ? String.valueOf(((Number) facturasPendientes).intValue()) : "0");
            });
        }).start();
    }

    private void cargarFacturas() {
        log.info("Cargando facturas...");

        new Thread(() -> {
            List<FacturaDTO> facturas;

            if (chkSoloPendientes != null && chkSoloPendientes.isSelected()) {
                facturas = facturaApiService.findPendientes();
            } else {
                facturas = facturaApiService.findAll();
            }

            log.info("Facturas obtenidas: {}", facturas.size());

            Platform.runLater(() -> {
                ObservableList<FacturaDTO> data = FXCollections.observableArrayList(facturas);
                tblFacturas.setItems(data);
            });
        }).start();
    }

    @FXML
    private void filtrar() {
        log.info("Aplicando filtros...");

        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

        if (desde == null || hasta == null) {
            mostrarError("Debes seleccionar ambas fechas");
            return;
        }

        new Thread(() -> {
            List<FacturaDTO> facturas = facturaApiService.findByFechas(desde, hasta);

            // Filtrar por pendientes si está marcado
            if (chkSoloPendientes != null && chkSoloPendientes.isSelected()) {
                facturas = facturas.stream()
                        .filter(f -> "PENDIENTE".equals(f.getEstado()))
                        .collect(Collectors.toList());
            }

            List<FacturaDTO> facturasFinal = facturas;
            Platform.runLater(() -> {
                tblFacturas.setItems(FXCollections.observableArrayList(facturasFinal));
            });
        }).start();
    }

    private void buscarFacturas(String texto) {
        new Thread(() -> {
            List<FacturaDTO> facturas = facturaApiService.buscar(texto);
            Platform.runLater(() -> {
                tblFacturas.setItems(FXCollections.observableArrayList(facturas));
            });
        }).start();
    }

    @FXML
    private void nuevaFactura() {
        log.info("Abriendo diálogo de nueva factura...");

        // Crear diálogo para elegir tipo de factura
        ChoiceDialog<String> tipoDialog = new ChoiceDialog<>("Desde cita completada",
                "Desde cita completada", "Factura manual simplificada", "Factura manual completa");
        tipoDialog.setTitle("Nueva Factura");
        tipoDialog.setHeaderText("¿Qué tipo de factura deseas crear?");
        tipoDialog.setContentText("Tipo:");

        Optional<String> tipoResult = tipoDialog.showAndWait();

        tipoResult.ifPresent(tipo -> {
            switch (tipo) {
                case "Desde cita completada":
                    crearFacturaDesdeCita();
                    break;
                case "Factura manual simplificada":
                    crearFacturaManual("SIMPLIFICADA");
                    break;
                case "Factura manual completa":
                    crearFacturaManual("COMPLETA");
                    break;
            }
        });
    }

    private void crearFacturaDesdeCita() {
        // Cargar citas completadas sin facturar
        new Thread(() -> {
            List<CitaDTO> todasCitas = citaApiService.findAll();
            List<CitaDTO> citasSinFacturar = todasCitas.stream()
                    .filter(c -> "COMPLETADA".equals(c.getEstado().name()))
                    .filter(c -> !Boolean.TRUE.equals(c.getFacturada()))
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                if (citasSinFacturar.isEmpty()) {
                    mostrarInfo("Sin citas", "No hay citas completadas pendientes de facturar");
                    return;
                }

                // Diálogo para seleccionar cita
                Dialog<CitaDTO> dialog = new Dialog<>();
                dialog.setTitle("Seleccionar Cita");
                dialog.setHeaderText("Selecciona la cita a facturar");

                ButtonType crearButtonType = new ButtonType("Crear Factura", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(crearButtonType, ButtonType.CANCEL);

                VBox content = new VBox(15);
                content.setPadding(new Insets(10));

                ListView<CitaDTO> listView = new ListView<>();
                listView.setItems(FXCollections.observableArrayList(citasSinFacturar));
                listView.setCellFactory(lv -> new ListCell<>() {
                    @Override
                    protected void updateItem(CitaDTO cita, boolean empty) {
                        super.updateItem(cita, empty);
                        if (empty || cita == null) {
                            setText(null);
                        } else {
                            String cliente = cita.getCliente() != null ? cita.getCliente().getNombre() : "Sin nombre";
                            String servicio = cita.getServicios() != null && !cita.getServicios().isEmpty()
                                    ? cita.getServicios().get(0).getNombre()
                                    : "";
                            setText(String.format("#%d - %s - %s - %s",
                                    cita.getId(), cliente, servicio, cita.getFechaHora().toLocalDate()));
                        }
                    }
                });
                listView.setPrefHeight(250);

                // Selector de método de pago
                Label lblMetodoPago = new Label("Método de pago:");
                lblMetodoPago.setStyle("-fx-font-weight: bold;");
                ComboBox<String> cmbMetodoPago = new ComboBox<>();
                cmbMetodoPago.setItems(FXCollections.observableArrayList(
                        "PENDIENTE", "EFECTIVO", "TARJETA", "BIZUM", "TRANSFERENCIA"));
                cmbMetodoPago.setValue("PENDIENTE");
                cmbMetodoPago.setPrefWidth(200);

                HBox hboxPago = new HBox(10, lblMetodoPago, cmbMetodoPago);
                hboxPago.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                content.getChildren().addAll(
                        new Label("Selecciona la cita:"),
                        listView,
                        new Separator(),
                        hboxPago);

                dialog.getDialogPane().setContent(content);
                dialog.getDialogPane().setPrefWidth(500);

                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == crearButtonType) {
                        return listView.getSelectionModel().getSelectedItem();
                    }
                    return null;
                });

                Optional<CitaDTO> result = dialog.showAndWait();

                result.ifPresent(cita -> {
                    String metodoPago = cmbMetodoPago.getValue();
                    new Thread(() -> {
                        try {
                            FacturaDTO factura = facturaApiService.crearSimplificadaDesdeCita(cita.getId());

                            // Si se seleccionó un método de pago distinto de PENDIENTE, marcar como pagada
                            if (!"PENDIENTE".equals(metodoPago)) {
                                facturaApiService.marcarComoPagada(factura.getId(), metodoPago);
                            }

                            Platform.runLater(() -> {
                                String estado = "PENDIENTE".equals(metodoPago) ? "pendiente de cobro"
                                        : "pagada con " + metodoPago;
                                mostrarInfo("Factura Creada",
                                        "Factura " + factura.getNumero() + " creada correctamente\n" +
                                                "Total: " + factura.getTotalFormateado() + "\n" +
                                                "Estado: " + estado);
                                cargarFacturas();
                                cargarResumen();
                            });
                        } catch (Exception e) {
                            log.error("Error al crear factura", e);
                            Platform.runLater(() -> mostrarError("Error al crear factura: " + e.getMessage()));
                        }
                    }).start();
                });
            });
        }).start();
    }

    private void crearFacturaManual(String tipo) {
        Dialog<FacturaDTO> dialog = new Dialog<>();
        dialog.setTitle("Nueva Factura " + ("COMPLETA".equals(tipo) ? "Completa" : "Simplificada"));
        dialog.setHeaderText("Introduce los datos de la factura");
        dialog.getDialogPane().setPrefWidth(550);

        ButtonType crearButtonType = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(crearButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        // ========================================
        // SELECTOR DE CLIENTE EXISTENTE
        // ========================================
        ComboBox<ClienteDTO> cmbClientes = new ComboBox<>();
        cmbClientes.setPromptText("Buscar cliente existente...");
        cmbClientes.setPrefWidth(350);
        cmbClientes.setEditable(true);

        // Cargar clientes en segundo plano
        new Thread(() -> {
            try {
                List<ClienteDTO> clientes = clienteApiService.obtenerTodosLosClientes();
                Platform.runLater(() -> {
                    cmbClientes.setItems(FXCollections.observableArrayList(clientes));
                });
            } catch (Exception e) {
                log.error("Error cargando clientes", e);
            }
        }).start();

        // Convertidor para mostrar nombre del cliente
        cmbClientes.setConverter(new StringConverter<ClienteDTO>() {
            @Override
            public String toString(ClienteDTO cliente) {
                if (cliente == null)
                    return "";
                String display = cliente.getNombreCompleto();
                if (cliente.getNif() != null && !cliente.getNif().isEmpty()) {
                    display += " (" + cliente.getNif() + ")";
                }
                return display;
            }

            @Override
            public ClienteDTO fromString(String string) {
                // Buscar cliente por texto
                if (string == null || string.isEmpty())
                    return null;
                return cmbClientes.getItems().stream()
                        .filter(c -> toString(c).toLowerCase().contains(string.toLowerCase()))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Filtro de búsqueda mientras escribe
        cmbClientes.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty() && cmbClientes.getItems() != null) {
                // No filtrar si ya se seleccionó un cliente
                if (cmbClientes.getValue() != null)
                    return;

                Platform.runLater(() -> {
                    if (!cmbClientes.isShowing()) {
                        cmbClientes.show();
                    }
                });
            }
        });

        // ========================================
        // CAMPOS DEL CLIENTE
        // ========================================
        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre del cliente");
        txtNombre.setPrefWidth(350);

        TextField txtNif = new TextField();
        txtNif.setPromptText("NIF/CIF");

        TextField txtDireccion = new TextField();
        txtDireccion.setPromptText("Dirección completa");
        txtDireccion.setPrefWidth(350);

        TextField txtTelefono = new TextField();
        txtTelefono.setPromptText("Teléfono");

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email");

        // Auto-rellenar campos cuando se selecciona un cliente
        cmbClientes.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtNombre.setText(newVal.getNombreCompleto());
                txtNif.setText(newVal.getNif() != null ? newVal.getNif() : "");

                // Construir dirección completa
                StringBuilder direccion = new StringBuilder();
                if (newVal.getDireccion() != null && !newVal.getDireccion().isEmpty()) {
                    direccion.append(newVal.getDireccion());
                }
                if (newVal.getCodigoPostal() != null && !newVal.getCodigoPostal().isEmpty()) {
                    if (direccion.length() > 0)
                        direccion.append(", ");
                    direccion.append(newVal.getCodigoPostal());
                }
                if (newVal.getCiudad() != null && !newVal.getCiudad().isEmpty()) {
                    if (direccion.length() > 0)
                        direccion.append(" ");
                    direccion.append(newVal.getCiudad());
                }
                if (newVal.getProvincia() != null && !newVal.getProvincia().isEmpty()
                        && !newVal.getProvincia().equals(newVal.getCiudad())) {
                    direccion.append(" (").append(newVal.getProvincia()).append(")");
                }
                txtDireccion.setText(direccion.toString());

                txtTelefono.setText(newVal.getTelefono() != null ? newVal.getTelefono() : "");
                txtEmail.setText(newVal.getEmail() != null ? newVal.getEmail() : "");

                log.info("Cliente seleccionado: {} - {}", newVal.getId(), newVal.getNombreCompleto());
            }
        });

        // Botón para limpiar selección
        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setOnAction(e -> {
            cmbClientes.setValue(null);
            cmbClientes.getEditor().clear();
            txtNombre.clear();
            txtNif.clear();
            txtDireccion.clear();
            txtTelefono.clear();
            txtEmail.clear();
        });

        // ========================================
        // DATOS DE LA FACTURA
        // ========================================
        TextField txtConcepto = new TextField();
        txtConcepto.setPromptText("Concepto del servicio");
        txtConcepto.setPrefWidth(350);

        TextField txtPrecio = new TextField();
        txtPrecio.setPromptText("Precio sin IVA (ej: 19.01)");

        // Selector de método de pago
        ComboBox<String> cmbMetodoPago = new ComboBox<>();
        cmbMetodoPago.setItems(FXCollections.observableArrayList(
                "PENDIENTE", "EFECTIVO", "TARJETA", "BIZUM", "TRANSFERENCIA"));
        cmbMetodoPago.setValue("PENDIENTE");
        cmbMetodoPago.setPrefWidth(200);

        // ========================================
        // CONSTRUIR LAYOUT
        // ========================================
        int row = 0;

        // Selector de cliente
        Label lblSelector = new Label("Buscar cliente:");
        lblSelector.setStyle("-fx-font-weight: bold;");
        grid.add(lblSelector, 0, row);
        HBox hboxSelector = new HBox(10, cmbClientes, btnLimpiar);
        grid.add(hboxSelector, 1, row++);

        // Separador
        Separator sep1 = new Separator();
        sep1.setPadding(new Insets(5, 0, 5, 0));
        grid.add(sep1, 0, row++, 2, 1);

        // Datos del cliente
        Label lblDatosCliente = new Label("Datos del cliente:");
        lblDatosCliente.setStyle("-fx-font-weight: bold; -fx-text-fill: #1976D2;");
        grid.add(lblDatosCliente, 0, row++, 2, 1);

        grid.add(new Label("Nombre:"), 0, row);
        grid.add(txtNombre, 1, row++);

        if ("COMPLETA".equals(tipo)) {
            grid.add(new Label("NIF/CIF:"), 0, row);
            grid.add(txtNif, 1, row++);
            grid.add(new Label("Dirección:"), 0, row);
            grid.add(txtDireccion, 1, row++);
        }

        grid.add(new Label("Teléfono:"), 0, row);
        grid.add(txtTelefono, 1, row++);
        grid.add(new Label("Email:"), 0, row);
        grid.add(txtEmail, 1, row++);

        // Separador
        Separator sep2 = new Separator();
        sep2.setPadding(new Insets(5, 0, 5, 0));
        grid.add(sep2, 0, row++, 2, 1);

        // Datos de la factura
        Label lblDatosFactura = new Label("Datos de la factura:");
        lblDatosFactura.setStyle("-fx-font-weight: bold; -fx-text-fill: #1976D2;");
        grid.add(lblDatosFactura, 0, row++, 2, 1);

        grid.add(new Label("Concepto:"), 0, row);
        grid.add(txtConcepto, 1, row++);
        grid.add(new Label("Precio (sin IVA):"), 0, row);
        grid.add(txtPrecio, 1, row++);
        grid.add(new Label("Método de pago:"), 0, row);
        grid.add(cmbMetodoPago, 1, row++);

        dialog.getDialogPane().setContent(grid);

        // Validación antes de crear
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == crearButtonType) {
                // Validar campos obligatorios
                if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
                    mostrarError("El nombre del cliente es obligatorio");
                    return null;
                }
                if (txtConcepto.getText() == null || txtConcepto.getText().trim().isEmpty()) {
                    mostrarError("El concepto es obligatorio");
                    return null;
                }
                if (txtPrecio.getText() == null || txtPrecio.getText().trim().isEmpty()) {
                    mostrarError("El precio es obligatorio");
                    return null;
                }
                if ("COMPLETA".equals(tipo) && (txtNif.getText() == null || txtNif.getText().trim().isEmpty())) {
                    mostrarError("El NIF/CIF es obligatorio para facturas completas");
                    return null;
                }

                try {
                    FacturaDTO factura = new FacturaDTO();
                    factura.setTipo(tipo);
                    factura.setClienteNombre(txtNombre.getText().trim());
                    factura.setClienteNif(txtNif.getText() != null ? txtNif.getText().trim() : "");
                    factura.setClienteDireccion(txtDireccion.getText() != null ? txtDireccion.getText().trim() : "");
                    factura.setClienteTelefono(txtTelefono.getText() != null ? txtTelefono.getText().trim() : "");
                    factura.setClienteEmail(txtEmail.getText() != null ? txtEmail.getText().trim() : "");

                    // Si se seleccionó un cliente existente, guardar su ID
                    if (cmbClientes.getValue() != null) {
                        factura.setClienteId(cmbClientes.getValue().getId());
                    }

                    // Crear línea de factura
                    FacturaDTO.LineaFacturaDTO linea = new FacturaDTO.LineaFacturaDTO();
                    linea.setConcepto(txtConcepto.getText().trim());
                    linea.setCantidad(1);
                    linea.setPrecioUnitario(new BigDecimal(txtPrecio.getText().trim().replace(",", ".")));
                    factura.setLineas(List.of(linea));

                    return factura;
                } catch (NumberFormatException e) {
                    mostrarError("El precio debe ser un número válido (ej: 19.01)");
                    return null;
                } catch (Exception e) {
                    mostrarError("Error en los datos: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<FacturaDTO> result = dialog.showAndWait();

        // Capturar el método de pago seleccionado
        String metodoPagoSeleccionado = cmbMetodoPago.getValue();

        result.ifPresent(facturaDTO -> {
            new Thread(() -> {
                try {
                    FacturaDTO factura = facturaApiService.crearManual(facturaDTO);

                    // Si se seleccionó un método de pago distinto de PENDIENTE, marcar como pagada
                    if (!"PENDIENTE".equals(metodoPagoSeleccionado)) {
                        facturaApiService.marcarComoPagada(factura.getId(), metodoPagoSeleccionado);
                    }

                    Platform.runLater(() -> {
                        String estado = "PENDIENTE".equals(metodoPagoSeleccionado) ? "pendiente de cobro"
                                : "pagada con " + metodoPagoSeleccionado;
                        mostrarInfo("Factura Creada",
                                "Factura " + factura.getNumero() + " creada correctamente\n" +
                                        "Total: " + factura.getTotalFormateado() + "\n" +
                                        "Estado: " + estado);
                        cargarFacturas();
                        cargarResumen();
                    });
                } catch (Exception e) {
                    log.error("Error al crear factura", e);
                    Platform.runLater(() -> mostrarError("Error al crear factura: " + e.getMessage()));
                }
            }).start();
        });
    }

    private void cobrarFactura(FacturaDTO factura) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("EFECTIVO",
                "EFECTIVO", "TARJETA", "BIZUM", "TRANSFERENCIA");
        dialog.setTitle("Cobrar Factura");
        dialog.setHeaderText("Factura " + factura.getNumero() + " - " + factura.getTotalFormateado());
        dialog.setContentText("Método de pago:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(metodoPago -> {
            new Thread(() -> {
                try {
                    facturaApiService.marcarComoPagada(factura.getId(), metodoPago);
                    Platform.runLater(() -> {
                        mostrarInfo("Factura Cobrada",
                                "Factura " + factura.getNumero() + " marcada como pagada");
                        cargarFacturas();
                        cargarResumen();
                    });
                } catch (Exception e) {
                    log.error("Error al cobrar factura", e);
                    Platform.runLater(() -> mostrarError("Error al cobrar factura: " + e.getMessage()));
                }
            }).start();
        });
    }

    private void descargarPdf(FacturaDTO factura) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Factura PDF");
        fileChooser.setInitialFileName("Factura_" + factura.getNumero().replace("/", "-") + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        File file = fileChooser.showSaveDialog(tblFacturas.getScene().getWindow());

        if (file != null) {
            new Thread(() -> {
                try {
                    byte[] pdfBytes = facturaApiService.descargarPdf(factura.getId());

                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(pdfBytes);
                    }

                    Platform.runLater(() -> {
                        mostrarInfo("PDF Guardado", "Factura guardada en:\n" + file.getAbsolutePath());

                        // Preguntar si abrir el PDF
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Abrir PDF");
                        alert.setHeaderText("¿Deseas abrir el PDF?");
                        alert.setContentText(file.getName());

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            try {
                                Desktop.getDesktop().open(file);
                            } catch (Exception e) {
                                log.error("Error al abrir PDF", e);
                            }
                        }
                    });
                } catch (Exception e) {
                    log.error("Error al descargar PDF", e);
                    Platform.runLater(() -> mostrarError("Error al descargar PDF: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void eliminarFactura(FacturaDTO factura) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar Factura");
        alert.setHeaderText("¿Estás seguro de eliminar la factura " + factura.getNumero() + "?");
        alert.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    facturaApiService.delete(factura.getId());
                    Platform.runLater(() -> {
                        mostrarInfo("Factura Eliminada", "Factura " + factura.getNumero() + " eliminada");
                        cargarFacturas();
                        cargarResumen();
                    });
                } catch (Exception e) {
                    log.error("Error al eliminar factura", e);
                    Platform.runLater(() -> mostrarError("Error al eliminar factura: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void mostrarDetalleFactura(FacturaDTO factura) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detalle de Factura");
        dialog.setHeaderText("Factura " + factura.getNumero());

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // Datos generales
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(8);

        int row = 0;
        grid.add(new Label("Fecha:"), 0, row);
        grid.add(new Label(factura.getFecha()), 1, row++);

        grid.add(new Label("Tipo:"), 0, row);
        grid.add(new Label(factura.getTipoFormateado()), 1, row++);

        grid.add(new Label("Estado:"), 0, row);
        Label lblEstado = new Label(factura.getEstadoFormateado());
        lblEstado.setStyle("PENDIENTE".equals(factura.getEstado()) ? "-fx-text-fill: #E65100; -fx-font-weight: bold;"
                : "-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
        grid.add(lblEstado, 1, row++);

        if (factura.getMetodoPago() != null) {
            grid.add(new Label("Método de pago:"), 0, row);
            grid.add(new Label(factura.getMetodoPagoFormateado()), 1, row++);
        }

        grid.add(new Label("Cliente:"), 0, row);
        grid.add(new Label(factura.getClienteNombre()), 1, row++);

        if (factura.getClienteNif() != null && !factura.getClienteNif().isEmpty()) {
            grid.add(new Label("NIF:"), 0, row);
            grid.add(new Label(factura.getClienteNif()), 1, row++);
        }

        content.getChildren().add(grid);

        // Separador
        content.getChildren().add(new Separator());

        // Líneas
        if (factura.getLineas() != null && !factura.getLineas().isEmpty()) {
            Label lblLineas = new Label("Conceptos:");
            lblLineas.setStyle("-fx-font-weight: bold;");
            content.getChildren().add(lblLineas);

            for (FacturaDTO.LineaFacturaDTO linea : factura.getLineas()) {
                HBox lineaBox = new HBox(10);
                lineaBox.getChildren().addAll(
                        new Label(linea.getConcepto()),
                        new Label("x" + linea.getCantidad()),
                        new Label(linea.getSubtotalFormateado()));
                content.getChildren().add(lineaBox);
            }
        }

        // Separador
        content.getChildren().add(new Separator());

        // Totales
        GridPane totales = new GridPane();
        totales.setHgap(20);
        totales.setVgap(5);

        totales.add(new Label("Base Imponible:"), 0, 0);
        totales.add(new Label(factura.getBaseImponibleFormateada()), 1, 0);

        totales.add(new Label("IVA (21%):"), 0, 1);
        totales.add(new Label(factura.getImporteIvaFormateado()), 1, 1);

        Label lblTotal = new Label("TOTAL:");
        lblTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label lblTotalValor = new Label(factura.getTotalFormateado());
        lblTotalValor.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2196F3;");

        totales.add(lblTotal, 0, 2);
        totales.add(lblTotalValor, 1, 2);

        content.getChildren().add(totales);

        // Pie de factura - Observaciones
        if (factura.getObservaciones() != null && !factura.getObservaciones().trim().isEmpty()) {
            content.getChildren().add(new Separator());

            Label lblObservacionesTitulo = new Label("Observaciones:");
            lblObservacionesTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
            content.getChildren().add(lblObservacionesTitulo);

            Label lblObservaciones = new Label(factura.getObservaciones());
            lblObservaciones.setWrapText(true);
            lblObservaciones.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");
            lblObservaciones.setMaxWidth(360);
            content.getChildren().add(lblObservaciones);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(400);

        dialog.showAndWait();
    }

    @FXML
    private void exportar() {
        mostrarInfo("Exportar", "Funcionalidad de exportación en desarrollo");
    }

    private String formatearImporte(Object valor) {
        if (valor == null)
            return "0,00 €";
        double importe = ((Number) valor).doubleValue();
        return String.format("%,.2f €", importe).replace(",", "X").replace(".", ",").replace("X", ".");
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
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
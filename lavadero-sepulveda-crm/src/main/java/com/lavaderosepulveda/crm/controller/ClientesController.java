package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.api.dto.ClienteDTO;
import com.lavaderosepulveda.crm.api.service.ClienteApiService;
import com.lavaderosepulveda.crm.util.ExcelClienteHandler;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClientesController {

    private static final Logger log = LoggerFactory.getLogger(ClientesController.class);

    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnNuevoCliente;
    @FXML private Button btnImportar;
    @FXML private Button btnExportar;

    @FXML private TableView<ClienteDTO> tblClientes;
    @FXML private TableColumn<ClienteDTO, Long> colId;
    @FXML private TableColumn<ClienteDTO, String> colNombreCompleto;
    @FXML private TableColumn<ClienteDTO, String> colTelefono;
    @FXML private TableColumn<ClienteDTO, String> colEmail;
    @FXML private TableColumn<ClienteDTO, String> colVehiculo;
    @FXML private TableColumn<ClienteDTO, Integer> colTotalCitas;
    @FXML private TableColumn<ClienteDTO, Double> colFacturado;
    @FXML private TableColumn<ClienteDTO, Void> colAcciones;

    @FXML private Label lblTotalClientes;
    @FXML private Label lblClientesActivos;

    private final ClienteApiService clienteApiService = ClienteApiService.getInstance();
    private List<ClienteDTO> todosLosClientes;

    @FXML
    public void initialize() {
        log.info("Inicializando ClientesController...");
        configurarTabla();
        cargarClientes();
    }

    private void configurarTabla() {
        // Configurar columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        // Nombre completo (nombre + apellidos)
        colNombreCompleto.setCellValueFactory(cellData -> {
            ClienteDTO cliente = cellData.getValue();
            String nombreCompleto = cliente.getNombre();
            if (cliente.getApellidos() != null && !cliente.getApellidos().isEmpty()) {
                nombreCompleto += " " + cliente.getApellidos();
            }
            String finalNombre = nombreCompleto;
            return javafx.beans.binding.Bindings.createStringBinding(() -> finalNombre);
        });

        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colVehiculo.setCellValueFactory(new PropertyValueFactory<>("vehiculoHabitual"));
        colTotalCitas.setCellValueFactory(new PropertyValueFactory<>("totalCitas"));
        colFacturado.setCellValueFactory(new PropertyValueFactory<>("totalFacturado"));
        
        // Formatear columna de facturado
        colFacturado.setCellFactory(column -> new TableCell<ClienteDTO, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", item));
                }
            }
        });

        // Configurar columna de acciones
        colAcciones.setCellFactory(column -> new TableCell<ClienteDTO, Void>() {
            private final Button btnVer = new Button("Ver");
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox hbox = new HBox(5, btnVer, btnEditar, btnEliminar);

            {
                btnEditar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                btnEliminar.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                
                btnVer.setOnAction(event -> {
                    ClienteDTO cliente = getTableView().getItems().get(getIndex());
                    verDetalleCliente(cliente);
                });
                
                btnEditar.setOnAction(event -> {
                    ClienteDTO cliente = getTableView().getItems().get(getIndex());
                    editarCliente(cliente);
                });
                
                btnEliminar.setOnAction(event -> {
                    ClienteDTO cliente = getTableView().getItems().get(getIndex());
                    eliminarCliente(cliente);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });
    }

    private void cargarClientes() {
        log.info("Cargando clientes desde la API...");
        
        // Ejecutar en hilo separado para no bloquear UI
        new Thread(() -> {
            try {
                todosLosClientes = clienteApiService.obtenerTodosLosClientes();
                log.info("Clientes cargados: {}", todosLosClientes.size());
                
                // Actualizar UI en el hilo de JavaFX
                Platform.runLater(() -> {
                    actualizarTabla(todosLosClientes);
                    actualizarEstadisticas();
                });
            } catch (Exception e) {
                log.error("Error al cargar clientes", e);
                Platform.runLater(() -> {
                    mostrarError("Error al cargar los clientes: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void buscarClientes() {
        String textoBusqueda = txtBuscar.getText();
        
        if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
            actualizarTabla(todosLosClientes);
            return;
        }
        
        String busqueda = textoBusqueda.toLowerCase().trim();
        log.info("Buscando clientes con: {}", busqueda);
        
        List<ClienteDTO> clientesFiltrados = todosLosClientes.stream()
            .filter(cliente -> {
                String nombreCompleto = cliente.getNombre();
                if (cliente.getApellidos() != null) {
                    nombreCompleto += " " + cliente.getApellidos();
                }
                if (nombreCompleto.toLowerCase().contains(busqueda)) {
                    return true;
                }
                
                if (cliente.getTelefono() != null && 
                    cliente.getTelefono().toLowerCase().contains(busqueda)) {
                    return true;
                }
                
                if (cliente.getEmail() != null && 
                    cliente.getEmail().toLowerCase().contains(busqueda)) {
                    return true;
                }
                
                return false;
            })
            .collect(Collectors.toList());
        
        log.info("Clientes encontrados: {}", clientesFiltrados.size());
        actualizarTabla(clientesFiltrados);
        actualizarEstadisticas(clientesFiltrados.size(), 
                              clientesFiltrados.stream().filter(ClienteDTO::getActivo).count());
    }

    @FXML
    private void limpiarBusqueda() {
        txtBuscar.clear();
        actualizarTabla(todosLosClientes);
        actualizarEstadisticas();
        log.info("Búsqueda limpiada");
    }

    @FXML
    private void importarExcel() {
        log.info("Importando clientes desde Excel...");
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo Excel");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx", "*.xls")
        );
        
        File archivo = fileChooser.showOpenDialog(tblClientes.getScene().getWindow());
        
        if (archivo != null) {
            new Thread(() -> {
                try {
                    List<ClienteDTO> clientesImportados = ExcelClienteHandler.importarDesdeExcel(archivo);
                    
                    Platform.runLater(() -> {
                        mostrarInfo("Importación Exitosa", 
                            "Se han importado " + clientesImportados.size() + " clientes.\n\n" +
                            "NOTA: Los clientes importados se muestran en la tabla pero NO se han " +
                            "guardado en la base de datos.\n\n" +
                            "Para guardarlos, necesitas implementar el endpoint POST /api/clientes " +
                            "en tu API de Spring Boot.");
                        
                        // Agregar a la lista local (no se guardan en DB todavía)
                        todosLosClientes.addAll(clientesImportados);
                        actualizarTabla(todosLosClientes);
                        actualizarEstadisticas();
                    });
                    
                } catch (Exception e) {
                    log.error("Error al importar Excel", e);
                    Platform.runLater(() -> {
                        mostrarError("Error al importar: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    @FXML
    private void exportarExcel() {
        log.info("Exportando clientes a Excel...");
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar archivo Excel");
        fileChooser.setInitialFileName("clientes_" + java.time.LocalDate.now() + ".xlsx");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx")
        );
        
        File archivo = fileChooser.showSaveDialog(tblClientes.getScene().getWindow());
        
        if (archivo != null) {
            new Thread(() -> {
                try {
                    // Obtener clientes actuales de la tabla
                    List<ClienteDTO> clientesAExportar = tblClientes.getItems();
                    
                    ExcelClienteHandler.exportarAExcel(clientesAExportar, archivo);
                    
                    Platform.runLater(() -> {
                        mostrarInfo("Exportación Exitosa", 
                            "Se han exportado " + clientesAExportar.size() + " clientes a:\n" +
                            archivo.getAbsolutePath());
                    });
                    
                } catch (Exception e) {
                    log.error("Error al exportar Excel", e);
                    Platform.runLater(() -> {
                        mostrarError("Error al exportar: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    private void actualizarTabla(List<ClienteDTO> clientes) {
        ObservableList<ClienteDTO> data = FXCollections.observableArrayList(clientes);
        tblClientes.setItems(data);
        log.info("Tabla actualizada con {} clientes", clientes.size());
    }

    private void actualizarEstadisticas() {
        if (todosLosClientes != null) {
            long activos = todosLosClientes.stream().filter(ClienteDTO::getActivo).count();
            actualizarEstadisticas(todosLosClientes.size(), activos);
        }
    }

    private void actualizarEstadisticas(long total, long activos) {
        lblTotalClientes.setText("Total clientes: " + total);
        lblClientesActivos.setText("Activos: " + activos);
    }

    @FXML
    private void nuevoCliente() {
        log.info("Abriendo formulario de nuevo cliente...");
        
        Dialog<ClienteDTO> dialog = crearDialogoCliente(null);
        Optional<ClienteDTO> resultado = dialog.showAndWait();
        
        resultado.ifPresent(cliente -> {
            log.info("Nuevo cliente: {}", cliente.getNombre());
            mostrarInfo("Funcionalidad en desarrollo", 
                "Para guardar clientes nuevos, necesitas implementar el endpoint:\n" +
                "POST /api/clientes\n\n" +
                "en tu API de Spring Boot.");
        });
    }

    private void editarCliente(ClienteDTO cliente) {
        log.info("Editando cliente: {}", cliente.getId());
        
        Dialog<ClienteDTO> dialog = crearDialogoCliente(cliente);
        Optional<ClienteDTO> resultado = dialog.showAndWait();
        
        resultado.ifPresent(clienteEditado -> {
            log.info("Cliente editado: {}", clienteEditado.getNombre());
            mostrarInfo("Funcionalidad en desarrollo", 
                "Para actualizar clientes, necesitas implementar el endpoint:\n" +
                "PUT /api/clientes/{id}\n\n" +
                "en tu API de Spring Boot.");
            
            // Actualizar en la lista local (no se guarda en DB todavía)
            int index = todosLosClientes.indexOf(cliente);
            if (index >= 0) {
                todosLosClientes.set(index, clienteEditado);
                actualizarTabla(todosLosClientes);
            }
        });
    }

    private void eliminarCliente(ClienteDTO cliente) {
        log.info("Solicitando eliminar cliente: {}", cliente.getId());
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar cliente?");
        confirmacion.setContentText(
            "¿Estás seguro de que quieres eliminar a:\n" +
            cliente.getNombre() + " " + (cliente.getApellidos() != null ? cliente.getApellidos() : "") + "\n\n" +
            "Esta acción no se puede deshacer."
        );
        
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            log.info("Confirmada eliminación de cliente: {}", cliente.getId());
            
            mostrarInfo("Funcionalidad en desarrollo", 
                "Para eliminar clientes, necesitas implementar el endpoint:\n" +
                "DELETE /api/clientes/{id}\n\n" +
                "en tu API de Spring Boot.");
            
            // Eliminar de la lista local (no se elimina de DB todavía)
            todosLosClientes.remove(cliente);
            actualizarTabla(todosLosClientes);
            actualizarEstadisticas();
        }
    }

    private Dialog<ClienteDTO> crearDialogoCliente(ClienteDTO clienteExistente) {
        Dialog<ClienteDTO> dialog = new Dialog<>();
        dialog.setTitle(clienteExistente == null ? "Nuevo Cliente" : "Editar Cliente");
        dialog.setHeaderText(clienteExistente == null ? 
            "Ingresa los datos del nuevo cliente" : 
            "Modifica los datos del cliente");

        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre");
        TextField txtApellidos = new TextField();
        txtApellidos.setPromptText("Apellidos");
        TextField txtTelefono = new TextField();
        txtTelefono.setPromptText("Teléfono");
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email");
        TextField txtVehiculo = new TextField();
        txtVehiculo.setPromptText("Vehículo habitual");

        if (clienteExistente != null) {
            txtNombre.setText(clienteExistente.getNombre());
            txtApellidos.setText(clienteExistente.getApellidos());
            txtTelefono.setText(clienteExistente.getTelefono());
            txtEmail.setText(clienteExistente.getEmail());
            txtVehiculo.setText(clienteExistente.getVehiculoHabitual());
        }

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Apellidos:"), 0, 1);
        grid.add(txtApellidos, 1, 1);
        grid.add(new Label("Teléfono:"), 0, 2);
        grid.add(txtTelefono, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(txtEmail, 1, 3);
        grid.add(new Label("Vehículo:"), 0, 4);
        grid.add(txtVehiculo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> txtNombre.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                ClienteDTO cliente = clienteExistente != null ? clienteExistente : new ClienteDTO();
                cliente.setNombre(txtNombre.getText());
                cliente.setApellidos(txtApellidos.getText());
                cliente.setTelefono(txtTelefono.getText());
                cliente.setEmail(txtEmail.getText());
                cliente.setVehiculoHabitual(txtVehiculo.getText());
                return cliente;
            }
            return null;
        });

        return dialog;
    }

    private void verDetalleCliente(ClienteDTO cliente) {
        log.info("Viendo detalle de cliente: {}", cliente.getId());
        
        StringBuilder detalle = new StringBuilder();
        detalle.append("ID: ").append(cliente.getId()).append("\n");
        detalle.append("Nombre: ").append(cliente.getNombre());
        if (cliente.getApellidos() != null && !cliente.getApellidos().isEmpty()) {
            detalle.append(" ").append(cliente.getApellidos());
        }
        detalle.append("\n");
        detalle.append("Teléfono: ").append(cliente.getTelefono()).append("\n");
        detalle.append("Email: ").append(cliente.getEmail()).append("\n");
        detalle.append("Vehículo habitual: ").append(cliente.getVehiculoHabitual()).append("\n");
        detalle.append("\n--- Estadísticas ---\n");
        detalle.append("Total citas: ").append(cliente.getTotalCitas()).append("\n");
        detalle.append("Citas completadas: ").append(cliente.getCitasCompletadas()).append("\n");
        detalle.append("Citas canceladas: ").append(cliente.getCitasCanceladas()).append("\n");
        detalle.append("No presentadas: ").append(cliente.getCitasNoPresentadas()).append("\n");
        detalle.append("Total facturado: ").append(String.format("%.2f €", cliente.getTotalFacturado())).append("\n");
        detalle.append("Estado: ").append(cliente.getActivo() ? "Activo" : "Inactivo");
        
        mostrarInfo("Detalle de Cliente", detalle.toString());
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
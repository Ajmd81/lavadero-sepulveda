package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.api.dto.CitaDTO;
import com.lavaderosepulveda.crm.api.dto.ClienteDTO;
import com.lavaderosepulveda.crm.api.dto.ServicioDTO;
import com.lavaderosepulveda.crm.api.service.CitaApiService;
import com.lavaderosepulveda.crm.model.EstadoCita;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CitasController {

    private static final Logger log = LoggerFactory.getLogger(CitasController.class);

    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Button btnFiltrar;
    @FXML private Button btnNuevaCita;
    @FXML private Button btnCalendario;

    @FXML private TableView<CitaDTO> tblCitas;
    @FXML private TableColumn<CitaDTO, Long> colId;
    @FXML private TableColumn<CitaDTO, String> colFechaHora;
    @FXML private TableColumn<CitaDTO, String> colCliente;
    @FXML private TableColumn<CitaDTO, String> colServicios;
    @FXML private TableColumn<CitaDTO, String> colEstado;
    @FXML private TableColumn<CitaDTO, Double> colImporte;
    @FXML private TableColumn<CitaDTO, Void> colAcciones;

    private final CitaApiService citaApiService = CitaApiService.getInstance();
    private List<CitaDTO> todasLasCitas;

    @FXML
    public void initialize() {
        log.info("Inicializando CitasController...");
        configurarTabla();
        configurarComboEstado();
        cargarCitas();
    }

    private void configurarTabla() {
        // Configurar columnas básicas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        // Fecha y hora formateada
        colFechaHora.setCellValueFactory(cellData -> {
            LocalDateTime fechaHora = cellData.getValue().getFechaHora();
            String formatted = fechaHora != null ? 
                fechaHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
            return javafx.beans.binding.Bindings.createStringBinding(() -> formatted);
        });

        // Cliente (nombre del primer cliente)
        colCliente.setCellValueFactory(cellData -> {
            CitaDTO cita = cellData.getValue();
            String cliente = cita.getCliente() != null ? 
                cita.getCliente().getNombre() : "Sin cliente";
            return javafx.beans.binding.Bindings.createStringBinding(() -> cliente);
        });

        // Servicios (nombre del primer servicio)
        colServicios.setCellValueFactory(cellData -> {
            CitaDTO cita = cellData.getValue();
            String servicio = "";
            if (cita.getServicios() != null && !cita.getServicios().isEmpty()) {
                servicio = cita.getServicios().get(0).getNombre();
            }
            String finalServicio = servicio;
            return javafx.beans.binding.Bindings.createStringBinding(() -> finalServicio);
        });

        // Estado
        colEstado.setCellValueFactory(cellData -> {
            EstadoCita estado = cellData.getValue().getEstado();
            String estadoStr = estado != null ? formatearEstado(estado) : "";
            return javafx.beans.binding.Bindings.createStringBinding(() -> estadoStr);
        });

        // Importe formateado
        colImporte.setCellValueFactory(new PropertyValueFactory<>("importeTotal"));
        colImporte.setCellFactory(column -> new TableCell<CitaDTO, Double>() {
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

        // Columna de acciones
        colAcciones.setCellFactory(column -> new TableCell<CitaDTO, Void>() {
            private final Button btnCambiarEstado = new Button("Cambiar Estado");
            private final HBox hbox = new HBox(5, btnCambiarEstado);

            {
                btnCambiarEstado.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                
                btnCambiarEstado.setOnAction(event -> {
                    CitaDTO cita = getTableView().getItems().get(getIndex());
                    cambiarEstadoCita(cita);
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

    private void configurarComboEstado() {
        cmbEstado.setItems(FXCollections.observableArrayList(
            "Todos",
            "Pendiente",
            "Confirmada",
            "Completada",
            "Cancelada",
            "No Presentado"
        ));
        cmbEstado.setValue("Todos");
    }

    private void cargarCitas() {
        log.info("Cargando citas desde la API...");
        
        new Thread(() -> {
            try {
                todasLasCitas = citaApiService.findAll();
                log.info("Citas cargadas: {}", todasLasCitas.size());
                
                Platform.runLater(() -> {
                    actualizarTabla(todasLasCitas);
                });
            } catch (Exception e) {
                log.error("Error al cargar citas", e);
                Platform.runLater(() -> {
                    mostrarError("Error al cargar las citas: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void filtrarCitas() {
        LocalDate fecha = dpFecha.getValue();
        String estado = cmbEstado.getValue();
        
        List<CitaDTO> citasFiltradas = todasLasCitas;
        
        // Filtrar por fecha
        if (fecha != null) {
            citasFiltradas = citasFiltradas.stream()
                .filter(cita -> cita.getFechaHora() != null && 
                               cita.getFechaHora().toLocalDate().equals(fecha))
                .collect(Collectors.toList());
        }
        
        // Filtrar por estado
        if (estado != null && !estado.equals("Todos")) {
            EstadoCita estadoEnum = convertirEstado(estado);
            citasFiltradas = citasFiltradas.stream()
                .filter(cita -> cita.getEstado() == estadoEnum)
                .collect(Collectors.toList());
        }
        
        actualizarTabla(citasFiltradas);
        log.info("Citas filtradas: {}", citasFiltradas.size());
    }

    @FXML
    private void nuevaCita() {
        log.info("Abriendo formulario de nueva cita...");
        
        Dialog<CitaDTO> dialog = crearDialogoNuevaCita();
        Optional<CitaDTO> resultado = dialog.showAndWait();
        
        resultado.ifPresent(cita -> {
            log.info("Creando nueva cita en la API...");
            
            new Thread(() -> {
                try {
                    // Llamar al endpoint POST /api/citas
                    CitaDTO citaCreada = citaApiService.create(cita);
                    
                    Platform.runLater(() -> {
                        mostrarInfo("Cita Creada", 
                            "La cita ha sido creada exitosamente.\n" +
                            "ID: " + citaCreada.getId());
                        cargarCitas(); // Recargar tabla
                    });
                    
                } catch (Exception e) {
                    log.error("Error al crear cita", e);
                    Platform.runLater(() -> {
                        mostrarError("Error al crear la cita: " + e.getMessage());
                    });
                }
            }).start();
        });
    }

    @FXML
    private void abrirCalendario() {
        log.info("Abriendo calendario...");
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/calendario.fxml"));
            Parent root = loader.load();
            
            CalendarioController controller = loader.getController();
            
            Stage stage = new Stage();
            stage.setTitle("Calendario de Citas");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 900, 700));
            stage.show();
            
        } catch (Exception e) {
            log.error("Error al abrir calendario", e);
            mostrarError("Error al abrir el calendario: " + e.getMessage());
        }
    }

    private void cambiarEstadoCita(CitaDTO cita) {
        log.info("Cambiando estado de cita: {}", cita.getId());
        
        Dialog<EstadoCita> dialog = new Dialog<>();
        dialog.setTitle("Cambiar Estado de Cita");
        dialog.setHeaderText("Cita #" + cita.getId() + " - " + 
            cita.getCliente().getNombre() + "\n" +
            "Estado actual: " + formatearEstado(cita.getEstado()));

        ButtonType cambiarButtonType = new ButtonType("Cambiar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(cambiarButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> cmbNuevoEstado = new ComboBox<>();
        cmbNuevoEstado.setItems(FXCollections.observableArrayList(
            "Pendiente",
            "Confirmada",
            "Completada",
            "Cancelada",
            "No Presentado"
        ));
        cmbNuevoEstado.setValue(formatearEstado(cita.getEstado()));

        TextArea txtNotas = new TextArea();
        txtNotas.setPromptText("Notas adicionales (opcional)");
        txtNotas.setPrefRowCount(3);

        grid.add(new Label("Nuevo Estado:"), 0, 0);
        grid.add(cmbNuevoEstado, 1, 0);
        grid.add(new Label("Notas:"), 0, 1);
        grid.add(txtNotas, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == cambiarButtonType) {
                return convertirEstado(cmbNuevoEstado.getValue());
            }
            return null;
        });

        Optional<EstadoCita> resultado = dialog.showAndWait();
        
        resultado.ifPresent(nuevoEstado -> {
            log.info("Cambiando estado de {} a {}", cita.getEstado(), nuevoEstado);
            
            new Thread(() -> {
                try {
                    // Usar endpoint específico PUT /api/citas/{id}/estado/{estado}
                    CitaDTO citaActualizada = citaApiService.cambiarEstado(cita.getId(), nuevoEstado);
                    
                    Platform.runLater(() -> {
                        mostrarInfo("Estado Actualizado", 
                            "El estado de la cita ha sido actualizado a: " + formatearEstado(nuevoEstado));
                        cargarCitas(); // Recargar tabla
                    });
                    
                } catch (Exception e) {
                    log.error("Error al actualizar estado de cita", e);
                    Platform.runLater(() -> {
                        mostrarError("Error al actualizar el estado: " + e.getMessage());
                    });
                }
            }).start();
        });
    }

    private Dialog<CitaDTO> crearDialogoNuevaCita() {
        Dialog<CitaDTO> dialog = new Dialog<>();
        dialog.setTitle("Nueva Cita");
        dialog.setHeaderText("Registrar nueva cita");

        ButtonType crearButtonType = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(crearButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Campos del formulario
        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre del cliente");
        
        TextField txtTelefono = new TextField();
        txtTelefono.setPromptText("Teléfono");
        
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email");
        
        DatePicker dpFechaCita = new DatePicker();
        dpFechaCita.setValue(LocalDate.now().plusDays(1));
        
        ComboBox<String> cmbHora = new ComboBox<>();
        cmbHora.setPromptText("Selecciona fecha primero");
        
        // Label para mostrar estado de carga
        Label lblHorariosStatus = new Label("Selecciona una fecha para ver horarios disponibles");
        lblHorariosStatus.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");
        
        // Listener para cargar horarios cuando cambie la fecha
        dpFechaCita.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                lblHorariosStatus.setText("Cargando horarios...");
                lblHorariosStatus.setStyle("-fx-text-fill: #2196F3; -fx-font-size: 10px;");
                cmbHora.setDisable(true);
                cmbHora.getItems().clear();
                
                // Cargar horarios en hilo separado
                new Thread(() -> {
                    List<String> horarios = citaApiService.obtenerHorariosDisponibles(newValue);
                    
                    Platform.runLater(() -> {
                        if (horarios != null && !horarios.isEmpty()) {
                            cmbHora.setItems(FXCollections.observableArrayList(horarios));
                            cmbHora.setValue(horarios.get(0));
                            cmbHora.setDisable(false);
                            lblHorariosStatus.setText(horarios.size() + " horarios disponibles");
                            lblHorariosStatus.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 10px;");
                        } else {
                            cmbHora.setItems(FXCollections.observableArrayList());
                            cmbHora.setDisable(true);
                            lblHorariosStatus.setText("No hay horarios disponibles para esta fecha");
                            lblHorariosStatus.setStyle("-fx-text-fill: #f44336; -fx-font-size: 10px;");
                        }
                    });
                }).start();
            }
        });
        
        // Disparar carga inicial de horarios
        Platform.runLater(() -> {
            dpFechaCita.fireEvent(new javafx.event.ActionEvent());
            // Forzar actualización de horarios para la fecha inicial
            LocalDate fechaInicial = dpFechaCita.getValue();
            if (fechaInicial != null) {
                new Thread(() -> {
                    List<String> horarios = citaApiService.obtenerHorariosDisponibles(fechaInicial);
                    Platform.runLater(() -> {
                        if (horarios != null && !horarios.isEmpty()) {
                            cmbHora.setItems(FXCollections.observableArrayList(horarios));
                            cmbHora.setValue(horarios.get(0));
                            cmbHora.setDisable(false);
                            lblHorariosStatus.setText(horarios.size() + " horarios disponibles");
                            lblHorariosStatus.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 10px;");
                        }
                    });
                }).start();
            }
        });
        
        ComboBox<String> cmbServicio = new ComboBox<>();
        cmbServicio.setItems(FXCollections.observableArrayList(
            "LAVADO_COMPLETO_TURISMO",
            "LAVADO_INTERIOR_TURISMO",
            "LAVADO_EXTERIOR_TURISMO",
            "LAVADO_COMPLETO_RANCHERA",
            "LAVADO_INTERIOR_RANCHERA",
            "LAVADO_EXTERIOR_RANCHERA",
            "LAVADO_COMPLETO_MONOVOLUMEN",
            "LAVADO_INTERIOR_MONOVOLUMEN",
            "LAVADO_EXTERIOR_MONOVOLUMEN",
            "LAVADO_COMPLETO_TODOTERRENO",
            "LAVADO_INTERIOR_TODOTERRENO",
            "LAVADO_EXTERIOR_TODOTERRENO",
            "LAVADO_COMPLETO_FURGONETA_PEQUEÑA",
            "LAVADO_INTERIOR_FURGONETA_PEQUEÑA",
            "LAVADO_EXTERIOR_FURGONETA_PEQUEÑA",
            "LAVADO_COMPLETO_FURGONETA_GRANDE",
            "LAVADO_INTERIOR_FURGONETA_GRANDE",
            "LAVADO_EXTERIOR_FURGONETA_GRANDE",
            "TRATAMIENTO_OZONO",
            "ENCERADO",
            "TAPICERIA_SIN_DESMONTAR",
            "TAPICERIA_DESMONTANDO"
        ));
        cmbServicio.setValue("LAVADO_COMPLETO_TURISMO");
        
        TextField txtVehiculo = new TextField();
        txtVehiculo.setPromptText("Modelo de vehículo");

        // Agregar campos al grid
        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Teléfono:"), 0, 1);
        grid.add(txtTelefono, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(txtEmail, 1, 2);
        grid.add(new Label("Fecha:"), 0, 3);
        grid.add(dpFechaCita, 1, 3);
        grid.add(new Label("Hora:"), 0, 4);
        grid.add(cmbHora, 1, 4);
        grid.add(lblHorariosStatus, 1, 5);
        grid.add(new Label("Servicio:"), 0, 6);
        grid.add(cmbServicio, 1, 6);
        grid.add(new Label("Vehículo:"), 0, 7);
        grid.add(txtVehiculo, 1, 7);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> txtNombre.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == crearButtonType) {
                // Validar que haya un horario seleccionado
                if (cmbHora.getValue() == null || cmbHora.getValue().isEmpty()) {
                    mostrarError("Debes seleccionar un horario disponible");
                    return null;
                }
                
                // Construir CitaDTO
                CitaDTO cita = new CitaDTO();
                
                // Cliente
                ClienteDTO cliente = new ClienteDTO();
                cliente.setNombre(txtNombre.getText());
                cliente.setTelefono(txtTelefono.getText());
                cliente.setEmail(txtEmail.getText());
                cita.setCliente(cliente);
                
                // Fecha y hora
                LocalDate fecha = dpFechaCita.getValue();
                LocalTime hora = LocalTime.parse(cmbHora.getValue());
                cita.setFechaHora(LocalDateTime.of(fecha, hora));
                
                // Servicio
                ServicioDTO servicio = new ServicioDTO();
                servicio.setNombre(formatearNombreServicio(cmbServicio.getValue()));
                cita.setServicios(Arrays.asList(servicio));
                
                // Estado inicial
                cita.setEstado(EstadoCita.PENDIENTE);
                
                // Vehículo
                // TODO: Agregar modeloVehiculo al CitaDTO si es necesario
                
                return cita;
            }
            return null;
        });

        return dialog;
    }

    private String formatearNombreServicio(String servicio) {
        return Arrays.stream(servicio.split("_"))
            .map(palabra -> palabra.substring(0, 1).toUpperCase() + palabra.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }

    private void actualizarTabla(List<CitaDTO> citas) {
        ObservableList<CitaDTO> data = FXCollections.observableArrayList(citas);
        tblCitas.setItems(data);
    }

    private String formatearEstado(EstadoCita estado) {
        if (estado == null) return "";
        switch (estado) {
            case PENDIENTE: return "Pendiente";
            case CONFIRMADA: return "Confirmada";
            case COMPLETADA: return "Completada";
            case CANCELADA: return "Cancelada";
            case NO_PRESENTADO: return "No Presentado";
            default: return estado.name();
        }
    }

    private EstadoCita convertirEstado(String estado) {
        switch (estado) {
            case "Pendiente": return EstadoCita.PENDIENTE;
            case "Confirmada": return EstadoCita.CONFIRMADA;
            case "Completada": return EstadoCita.COMPLETADA;
            case "Cancelada": return EstadoCita.CANCELADA;
            case "No Presentado": return EstadoCita.NO_PRESENTADO;
            default: return EstadoCita.PENDIENTE;
        }
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
package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.model.dto.CitaDTO;
import com.lavaderosepulveda.crm.api.service.CitaApiService;
import com.lavaderosepulveda.crm.model.enums.EstadoCita;
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
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox hbox = new HBox(5, btnCambiarEstado, btnEliminar);

            {
                btnCambiarEstado.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnEliminar.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                
                btnCambiarEstado.setOnAction(event -> {
                    CitaDTO cita = getTableView().getItems().get(getIndex());
                    cambiarEstadoCita(cita);
                });

                btnEliminar.setOnAction(event -> {
                    CitaDTO cita = getTableView().getItems().get(getIndex());
                    eliminarCita(cita);
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
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formulario-nueva-cita.fxml"));
            Parent root = loader.load();
            
            FormularioNuevaCitaController controller = loader.getController();
            
            // Callback para recargar la tabla cuando se guarde
            controller.setOnCitaGuardada(() -> {
                cargarCitas();
            });
            
            Stage stage = new Stage();
            stage.setTitle("Nueva Cita");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(tblCitas.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
            
        } catch (Exception e) {
            log.error("Error al abrir formulario de nueva cita", e);
            mostrarError("Error al abrir el formulario: " + e.getMessage());
        }
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
            stage.initOwner(tblCitas.getScene().getWindow()); 
            stage.setScene(new Scene(root, 900, 700));
            stage.setResizable(false); 
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
            "En Proceso",
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

    private void eliminarCita(CitaDTO cita) {
        log.info("Solicitando eliminar cita: {}", cita.getId());
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar cita?");
        confirmacion.setContentText(
            "¿Estás seguro de que quieres eliminar esta cita?\n\n" +
            "Cita #" + cita.getId() + "\n" +
            "Cliente: " + cita.getCliente().getNombre() + "\n" +
            "Fecha: " + cita.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n\n" +
            "Esta acción no se puede deshacer."
        );
        
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            log.info("Confirmada eliminación de cita: {}", cita.getId());
            
            new Thread(() -> {
                try {
                    citaApiService.delete(cita.getId());
                    
                    Platform.runLater(() -> {
                        log.info("Cita eliminada exitosamente: {}", cita.getId());
                        mostrarInfo("Cita Eliminada", 
                            "La cita se ha eliminado correctamente.");
                        
                        // Recargar la lista de citas
                        cargarCitas();
                    });
                } catch (Exception e) {
                    log.error("Error al eliminar cita", e);
                    Platform.runLater(() -> {
                        mostrarError("Error al eliminar la cita: " + e.getMessage());
                    });
                }
            }).start();
        }
    }


    private String formatearNombreServicio(String servicio) {
        return Arrays.stream(servicio.split("_"))
            .map(palabra -> palabra.substring(0, 1).toUpperCase() + palabra.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }

    private void actualizarTabla(List<CitaDTO> citas) {
        // Ordenar por fecha y hora (ascendente)
        List<CitaDTO> citasOrdenadas = citas.stream()
            .sorted((c1, c2) -> {
                if (c1.getFechaHora() == null && c2.getFechaHora() == null) return 0;
                if (c1.getFechaHora() == null) return 1;
                if (c2.getFechaHora() == null) return -1;
                return c1.getFechaHora().compareTo(c2.getFechaHora());
            })
            .collect(Collectors.toList());
        
        ObservableList<CitaDTO> data = FXCollections.observableArrayList(citasOrdenadas);
        tblCitas.setItems(data);
    }

    private String formatearEstado(EstadoCita estado) {
        if (estado == null) return "";
        switch (estado) {
            case PENDIENTE: return "Pendiente";
            case CONFIRMADA: return "Confirmada";
            case EN_PROCESO: return "En Proceso";
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
            case "En Proceso": return EstadoCita.EN_PROCESO;
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
package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.api.dto.CitaDTO;
import com.lavaderosepulveda.crm.api.service.CitaApiService;
import com.lavaderosepulveda.crm.model.EstadoCita;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class CitasController {

    private static final Logger log = LoggerFactory.getLogger(CitasController.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
        configurarComboEstado();
        configurarTabla();
        cargarCitas();
    }

    private void configurarComboEstado() {
        ObservableList<String> estados = FXCollections.observableArrayList(
            "Todos",
            "PENDIENTE",
            "CONFIRMADA",
            "COMPLETADA",
            "CANCELADA",
            "NO_PRESENTADO"
        );
        cmbEstado.setItems(estados);
        cmbEstado.setValue("Todos");
    }

    private void configurarTabla() {
        // Configurar columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        colFechaHora.setCellValueFactory(cellData -> {
            CitaDTO cita = cellData.getValue();
            if (cita.getFechaHora() != null) {
                String fechaFormateada = cita.getFechaHora().format(DATE_TIME_FORMATTER);
                return javafx.beans.binding.Bindings.createStringBinding(() -> fechaFormateada);
            }
            return javafx.beans.binding.Bindings.createStringBinding(() -> "");
        });

        colCliente.setCellValueFactory(cellData -> {
            CitaDTO cita = cellData.getValue();
            String nombreCliente = "";
            if (cita.getCliente() != null) {
                nombreCliente = cita.getCliente().getNombre() + " " + 
                               (cita.getCliente().getApellidos() != null ? cita.getCliente().getApellidos() : "");
            }
            String finalNombre = nombreCliente;
            return javafx.beans.binding.Bindings.createStringBinding(() -> finalNombre);
        });

        colServicios.setCellValueFactory(cellData -> {
            CitaDTO cita = cellData.getValue();
            String servicios = "";
            if (cita.getServicios() != null && !cita.getServicios().isEmpty()) {
                servicios = cita.getServicios().stream()
                    .map(s -> s.getNombre())
                    .collect(Collectors.joining(", "));
            }
            String finalServicios = servicios;
            return javafx.beans.binding.Bindings.createStringBinding(() -> finalServicios);
        });

        colEstado.setCellValueFactory(cellData -> {
            CitaDTO cita = cellData.getValue();
            String estado = cita.getEstado() != null ? cita.getEstado().name() : "";
            return javafx.beans.binding.Bindings.createStringBinding(() -> estado);
        });

        colImporte.setCellValueFactory(new PropertyValueFactory<>("importeTotal"));
        
        // Formatear columna de importe
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

        // Configurar columna de acciones
        colAcciones.setCellFactory(column -> new TableCell<CitaDTO, Void>() {
            private final Button btnVer = new Button("Ver");
            private final HBox hbox = new HBox(5, btnVer);

            {
                btnVer.setOnAction(event -> {
                    CitaDTO cita = getTableView().getItems().get(getIndex());
                    verDetalleCita(cita);
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

    private void cargarCitas() {
        log.info("Cargando citas desde la API...");
        
        // Ejecutar en hilo separado para no bloquear UI
        new Thread(() -> {
            try {
                todasLasCitas = citaApiService.findAll();
                log.info("Citas cargadas: {}", todasLasCitas.size());
                
                // Actualizar UI en el hilo de JavaFX
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
        log.info("Filtrando citas...");
        
        if (todasLasCitas == null || todasLasCitas.isEmpty()) {
            log.warn("No hay citas para filtrar");
            return;
        }

        List<CitaDTO> citasFiltradas = todasLasCitas;

        // Filtrar por fecha
        LocalDate fechaSeleccionada = dpFecha.getValue();
        if (fechaSeleccionada != null) {
            log.info("Filtrando por fecha: {}", fechaSeleccionada);
            citasFiltradas = citasFiltradas.stream()
                .filter(cita -> cita.getFechaHora() != null && 
                               cita.getFechaHora().toLocalDate().equals(fechaSeleccionada))
                .collect(Collectors.toList());
        }

        // Filtrar por estado
        String estadoSeleccionado = cmbEstado.getValue();
        if (estadoSeleccionado != null && !estadoSeleccionado.equals("Todos")) {
            log.info("Filtrando por estado: {}", estadoSeleccionado);
            EstadoCita estado = EstadoCita.valueOf(estadoSeleccionado);
            citasFiltradas = citasFiltradas.stream()
                .filter(cita -> cita.getEstado() == estado)
                .collect(Collectors.toList());
        }

        log.info("Citas después de filtrar: {}", citasFiltradas.size());
        actualizarTabla(citasFiltradas);
    }

    private void actualizarTabla(List<CitaDTO> citas) {
        ObservableList<CitaDTO> data = FXCollections.observableArrayList(citas);
        tblCitas.setItems(data);
        log.info("Tabla actualizada con {} citas", citas.size());
    }

    @FXML
    private void nuevaCita() {
        log.info("Abriendo formulario de nueva cita...");
        mostrarInfo("Funcionalidad en desarrollo", "La creación de citas estará disponible próximamente.");
    }

    @FXML
    private void abrirCalendario() {
        log.info("Abriendo calendario...");
        mostrarInfo("Funcionalidad en desarrollo", "El calendario estará disponible próximamente.");
    }

    private void verDetalleCita(CitaDTO cita) {
        log.info("Viendo detalle de cita: {}", cita.getId());
        mostrarInfo("Detalle de Cita", 
            "ID: " + cita.getId() + "\n" +
            "Fecha: " + (cita.getFechaHora() != null ? cita.getFechaHora().format(DATE_TIME_FORMATTER) : "") + "\n" +
            "Estado: " + (cita.getEstado() != null ? cita.getEstado().name() : "") + "\n" +
            "Importe: " + String.format("%.2f €", cita.getImporteTotal())
        );
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
package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.api.service.CitaApiService;
import com.lavaderosepulveda.crm.model.dto.CitaDTO;
import com.lavaderosepulveda.crm.model.dto.ClienteDTO;
import com.lavaderosepulveda.crm.model.dto.ServicioDTO;
import com.lavaderosepulveda.crm.model.enums.EstadoCita;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class FormularioNuevaCitaController {

    private static final Logger log = LoggerFactory.getLogger(FormularioNuevaCitaController.class);

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cmbHora;
    @FXML private Label lblHorariosStatus;
    @FXML private ComboBox<String> cmbServicio;
    @FXML private TextField txtVehiculo;
    @FXML private TextArea txtObservaciones;

    private CitaApiService citaApiService;
    private CitaDTO citaActual;
    private boolean modoEdicion = false;
    private Runnable onCitaGuardada;

    @FXML
    public void initialize() {
        citaApiService = CitaApiService.getInstance();
        
        configurarServicios();
        configurarFecha();
        configurarHorarios();
    }

    private void configurarServicios() {
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
    }

    private void configurarFecha() {
        // Fecha por defecto: mañana
        dpFecha.setValue(LocalDate.now().plusDays(1));
        
        // Listener para cargar horarios cuando cambie la fecha
        dpFecha.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                cargarHorariosDisponibles(newValue);
            }
        });
    }

    private void configurarHorarios() {
        cmbHora.setDisable(true);
        
        // Cargar horarios para la fecha inicial
        Platform.runLater(() -> {
            LocalDate fechaInicial = dpFecha.getValue();
            if (fechaInicial != null) {
                cargarHorariosDisponibles(fechaInicial);
            }
        });
    }

    private void cargarHorariosDisponibles(LocalDate fecha) {
        log.info("Cargando horarios para: {}", fecha);
        lblHorariosStatus.setText("Cargando horarios...");
        lblHorariosStatus.setStyle("-fx-text-fill: #2196F3; -fx-font-size: 10px;");
        cmbHora.setDisable(true);
        cmbHora.getItems().clear();
        
        new Thread(() -> {
            List<String> horarios = citaApiService.obtenerHorariosDisponibles(fecha);
            
            Platform.runLater(() -> {
                if (horarios != null && !horarios.isEmpty()) {
                    cmbHora.setItems(FXCollections.observableArrayList(horarios));
                    cmbHora.setValue(horarios.get(0));
                    cmbHora.setDisable(false);
                    lblHorariosStatus.setText(horarios.size() + " horarios disponibles");
                    lblHorariosStatus.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 10px;");
                    log.info("Horarios cargados: {}", horarios.size());
                } else {
                    cmbHora.setItems(FXCollections.observableArrayList());
                    cmbHora.setDisable(true);
                    lblHorariosStatus.setText("No hay horarios disponibles (puede ser festivo o domingo)");
                    lblHorariosStatus.setStyle("-fx-text-fill: #f44336; -fx-font-size: 10px;");
                    log.warn("Sin horarios para: {}", fecha);
                }
            });
        }).start();
    }

    @FXML
    private void guardar() {
        if (!validar()) {
            return;
        }

        try {
            CitaDTO cita = construirCitaDTO();
            
            log.info("Guardando cita...");
            
            new Thread(() -> {
                try {
                    CitaDTO citaGuardada;
                    if (modoEdicion) {
                        citaGuardada = citaApiService.update(citaActual.getId(), cita);
                    } else {
                        citaGuardada = citaApiService.create(cita);
                    }
                    
                    Platform.runLater(() -> {
                        mostrarInfo("Éxito", 
                            modoEdicion ? "Cita actualizada correctamente" : "Cita creada correctamente");
                        
                        if (onCitaGuardada != null) {
                            onCitaGuardada.run();
                        }
                        
                        cerrarVentana();
                    });
                    
                } catch (Exception e) {
                    log.error("Error al guardar cita", e);
                    Platform.runLater(() -> {
                        mostrarError("Error al guardar", e.getMessage());
                    });
                }
            }).start();
            
        } catch (Exception e) {
            log.error("Error al construir DTO", e);
            mostrarError("Error", e.getMessage());
        }
    }

    private CitaDTO construirCitaDTO() {
        CitaDTO cita = new CitaDTO();
        
        if (modoEdicion) {
            cita.setId(citaActual.getId());
        }
        
        // Cliente con datos directos
        ClienteDTO cliente = new ClienteDTO();
        cliente.setNombre(txtNombre.getText().trim());
        cliente.setTelefono(txtTelefono.getText().trim());
        
        String email = txtEmail.getText().trim();
        if (!email.isEmpty()) {
            cliente.setEmail(email);
        }
        
        cita.setCliente(cliente);
        
        // Fecha y hora
        LocalDate fecha = dpFecha.getValue();
        LocalTime hora = LocalTime.parse(cmbHora.getValue());
        cita.setFechaHora(LocalDateTime.of(fecha, hora));
        
        // Servicio
        ServicioDTO servicio = new ServicioDTO();
        servicio.setNombre(cmbServicio.getValue());
        cita.setServicios(Arrays.asList(servicio));
        
        // Estado
        cita.setEstado(EstadoCita.PENDIENTE);
        
        // Vehículo
        String vehiculo = txtVehiculo.getText().trim();
        if (!vehiculo.isEmpty()) {
            cita.setMarcaModelo(vehiculo);
        }
        
        // Observaciones
        String observaciones = txtObservaciones.getText().trim();
        if (!observaciones.isEmpty()) {
            cita.setObservaciones(observaciones);
        }
        
        return cita;
    }

    private boolean validar() {
        StringBuilder errores = new StringBuilder();

        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("- El nombre es obligatorio\n");
        }
        if (txtTelefono.getText().trim().isEmpty()) {
            errores.append("- El teléfono es obligatorio\n");
        }
        if (dpFecha.getValue() == null) {
            errores.append("- La fecha es obligatoria\n");
        }
        if (cmbHora.getValue() == null || cmbHora.getValue().isEmpty()) {
            errores.append("- Debe seleccionar un horario\n");
        }
        if (cmbServicio.getValue() == null) {
            errores.append("- Debe seleccionar un servicio\n");
        }

        if (errores.length() > 0) {
            mostrarError("Datos incompletos", errores.toString());
            return false;
        }
        
        return true;
    }

    @FXML
    private void cancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.initOwner(txtNombre.getScene().getWindow());
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.initOwner(txtNombre.getScene().getWindow());
        alert.showAndWait();
    }

    // Setters para modo edición y callback
    public void setCita(CitaDTO cita) {
        this.citaActual = cita;
        this.modoEdicion = true;
        lblTitulo.setText("Editar Cita");
        
        // TODO: Cargar datos de la cita en los campos
    }

    public void setOnCitaGuardada(Runnable callback) {
        this.onCitaGuardada = callback;
    }
}
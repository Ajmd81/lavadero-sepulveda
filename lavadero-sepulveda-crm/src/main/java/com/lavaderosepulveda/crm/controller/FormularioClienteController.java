package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.api.service.ClienteApiService;
import com.lavaderosepulveda.crm.model.dto.ClienteDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormularioClienteController {

    private static final Logger log = LoggerFactory.getLogger(FormularioClienteController.class);

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtNif;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtCodigoPostal;
    @FXML private TextField txtProvincia;
    @FXML private TextArea txtNotas;

    private ClienteApiService clienteApiService;
    private ClienteDTO clienteActual;
    private boolean modoEdicion = false;
    private Runnable onClienteGuardado;

    @FXML
    public void initialize() {
        clienteApiService = ClienteApiService.getInstance();
        log.info("FormularioClienteController inicializado");
    }

    @FXML
    private void guardar() {
        if (!validar()) {
            return;
        }

        try {
            ClienteDTO cliente = construirClienteDTO();
            
            log.info("Guardando cliente...");
            
            new Thread(() -> {
                try {
                    ClienteDTO clienteGuardado;
                    if (modoEdicion) {
                        clienteGuardado = clienteApiService.actualizar(clienteActual.getId(), cliente);
                        log.info("Cliente actualizado: {}", clienteGuardado.getId());
                    } else {
                        clienteGuardado = clienteApiService.crear(cliente);
                        log.info("Cliente creado: {}", clienteGuardado.getId());
                    }
                    
                    Platform.runLater(() -> {
                        mostrarInfo("Éxito", 
                            modoEdicion ? "Cliente actualizado correctamente" : "Cliente creado correctamente");
                        
                        if (onClienteGuardado != null) {
                            onClienteGuardado.run();
                        }
                        
                        cerrarVentana();
                    });
                    
                } catch (Exception e) {
                    log.error("Error al guardar cliente", e);
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

    private ClienteDTO construirClienteDTO() {
        ClienteDTO cliente = new ClienteDTO();
        
        if (modoEdicion) {
            cliente.setId(clienteActual.getId());
        }
        
        // Datos personales obligatorios
        cliente.setNombre(txtNombre.getText().trim());
        cliente.setTelefono(txtTelefono.getText().trim());
        
        // Datos personales opcionales
        String apellidos = txtApellidos.getText().trim();
        if (!apellidos.isEmpty()) {
            cliente.setApellidos(apellidos);
        }
        
        String email = txtEmail.getText().trim();
        if (!email.isEmpty()) {
            cliente.setEmail(email);
        }
        
        String nif = txtNif.getText().trim();
        if (!nif.isEmpty()) {
            cliente.setNif(nif);
        }
        
        // Dirección
        String direccion = txtDireccion.getText().trim();
        if (!direccion.isEmpty()) {
            cliente.setDireccion(direccion);
        }
        
        String ciudad = txtCiudad.getText().trim();
        if (!ciudad.isEmpty()) {
            cliente.setCiudad(ciudad);
        }
        
        String codigoPostal = txtCodigoPostal.getText().trim();
        if (!codigoPostal.isEmpty()) {
            cliente.setCodigoPostal(codigoPostal);
        }
        
        String provincia = txtProvincia.getText().trim();
        if (!provincia.isEmpty()) {
            cliente.setProvincia(provincia);
        }
        
        // Notas
        String notas = txtNotas.getText().trim();
        if (!notas.isEmpty()) {
            cliente.setNotas(notas);
        }
        
        // Cliente activo por defecto
        cliente.setActivo(true);
        
        return cliente;
    }

    private boolean validar() {
        StringBuilder errores = new StringBuilder();

        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("- El nombre es obligatorio\n");
        }
        if (txtTelefono.getText().trim().isEmpty()) {
            errores.append("- El teléfono es obligatorio\n");
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
    public void setCliente(ClienteDTO cliente) {
        this.clienteActual = cliente;
        this.modoEdicion = true;
        lblTitulo.setText("Editar Cliente");
        cargarDatosCliente();
    }

    private void cargarDatosCliente() {
        if (clienteActual != null) {
            txtNombre.setText(clienteActual.getNombre());
            txtApellidos.setText(clienteActual.getApellidos());
            txtTelefono.setText(clienteActual.getTelefono());
            txtEmail.setText(clienteActual.getEmail());
            txtNif.setText(clienteActual.getNif());
            txtDireccion.setText(clienteActual.getDireccion());
            txtCiudad.setText(clienteActual.getCiudad());
            txtCodigoPostal.setText(clienteActual.getCodigoPostal());
            txtProvincia.setText(clienteActual.getProvincia());
            txtNotas.setText(clienteActual.getNotas());
        }
    }

    public void setOnClienteGuardado(Runnable callback) {
        this.onClienteGuardado = callback;
    }
}

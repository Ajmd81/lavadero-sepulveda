package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.api.service.FacturacionApiService;
import com.lavaderosepulveda.crm.model.ProveedorDTO;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class FormularioProveedorController {

    @FXML
    private Label lblTitulo;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtNif;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtContacto;
    @FXML
    private TextField txtIban;
    @FXML
    private TextArea txtNotas;
    @FXML
    private CheckBox chkActivo;

    private FacturacionApiService apiService;
    private ProveedorDTO proveedorActual;
    private boolean modoEdicion = false;

    @FXML
    public void initialize() {
        apiService = FacturacionApiService.getInstance();
    }

    public void setProveedor(ProveedorDTO proveedor) {
        this.proveedorActual = proveedor;
        this.modoEdicion = true;
        lblTitulo.setText("Editar Proveedor");

        // Cargar datos
        txtNombre.setText(proveedor.getNombre());
        txtNif.setText(proveedor.getNif());
        txtDireccion.setText(proveedor.getDireccion());
        txtTelefono.setText(proveedor.getTelefono());
        txtEmail.setText(proveedor.getEmail());
        txtContacto.setText(proveedor.getContacto());
        txtIban.setText(proveedor.getIban());
        txtNotas.setText(proveedor.getNotas());
        chkActivo.setSelected(proveedor.getActivo() != null ? proveedor.getActivo() : true);
    }

    @FXML
    private void guardar() {
        if (!validar()) {
            return;
        }

        try {
            ProveedorDTO dto = modoEdicion ? proveedorActual : new ProveedorDTO();

            dto.setNombre(txtNombre.getText().trim());
            dto.setNif(txtNif.getText().trim());
            dto.setDireccion(txtDireccion.getText().trim());
            dto.setTelefono(txtTelefono.getText().trim());
            dto.setEmail(txtEmail.getText().trim());
            dto.setContacto(txtContacto.getText().trim());
            dto.setIban(txtIban.getText().trim());
            dto.setNotas(txtNotas.getText().trim());
            dto.setActivo(chkActivo.isSelected());

            if (modoEdicion) {
                apiService.actualizarProveedor(dto.getId(), dto);
                mostrarInfo("Éxito", "Proveedor actualizado correctamente");
            } else {
                apiService.crearProveedor(dto);
                mostrarInfo("Éxito", "Proveedor creado correctamente");
            }

            cerrarVentana();

        } catch (Exception e) {
            mostrarError("Error al guardar", e.getMessage());
        }
    }

    private boolean validar() {
        StringBuilder errores = new StringBuilder();

        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("- El nombre es obligatorio\n");
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
}

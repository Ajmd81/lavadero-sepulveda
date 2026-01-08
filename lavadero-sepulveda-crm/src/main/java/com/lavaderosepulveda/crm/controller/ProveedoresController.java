package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.model.ProveedorDTO;
import com.lavaderosepulveda.crm.api.service.FacturacionApiService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class ProveedoresController {

    @FXML
    private TextField txtBuscar;
    @FXML
    private CheckBox chkMostrarInactivos;
    @FXML
    private TableView<ProveedorDTO> tablaProveedores;
    @FXML
    private TableColumn<ProveedorDTO, String> colNombre;
    @FXML
    private TableColumn<ProveedorDTO, String> colNif;
    @FXML
    private TableColumn<ProveedorDTO, String> colTelefono;
    @FXML
    private TableColumn<ProveedorDTO, String> colEmail;
    @FXML
    private TableColumn<ProveedorDTO, String> colActivo;

    @FXML
    private VBox panelEdicion;
    @FXML
    private Label lblTituloEdicion;
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
    private Button btnDesactivar;

    private FacturacionApiService apiService;
    private ObservableList<ProveedorDTO> listaProveedores = FXCollections.observableArrayList();
    private ProveedorDTO proveedorSeleccionado;
    private boolean modoNuevo = false;

    @FXML
    public void initialize() {
        apiService = FacturacionApiService.getInstance();

        configurarTabla();
        configurarListeners();
        cargarProveedores();
        limpiarFormulario();
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNif.setCellValueFactory(new PropertyValueFactory<>("nif"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colActivo.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getActivo() ? "✅ Activo" : "❌ Inactivo"));

        tablaProveedores.setItems(listaProveedores);
    }

    private void configurarListeners() {
        // Selección de proveedor
        tablaProveedores.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null) {
                seleccionarProveedor(nuevo);
            }
        });

        // Checkbox mostrar inactivos
        chkMostrarInactivos.selectedProperty().addListener((obs, old, nuevo) -> cargarProveedores());
    }

    private void cargarProveedores() {
        try {
            List<ProveedorDTO> proveedores;
            if (chkMostrarInactivos.isSelected()) {
                proveedores = apiService.obtenerProveedores();
            } else {
                proveedores = apiService.obtenerProveedoresActivos();
            }
            listaProveedores.setAll(proveedores);
        } catch (Exception e) {
            mostrarError("Error", "No se pudieron cargar los proveedores: " + e.getMessage());
        }
    }

    private void seleccionarProveedor(ProveedorDTO proveedor) {
        this.proveedorSeleccionado = proveedor;
        this.modoNuevo = false;

        lblTituloEdicion.setText("Editar Proveedor");
        txtNombre.setText(proveedor.getNombre());
        txtNif.setText(proveedor.getNif());
        txtDireccion.setText(proveedor.getDireccion());
        txtTelefono.setText(proveedor.getTelefono());
        txtEmail.setText(proveedor.getEmail());
        txtContacto.setText(proveedor.getContacto());
        txtIban.setText(proveedor.getIban());
        txtNotas.setText(proveedor.getNotas());

        // Actualizar botón activar/desactivar
        if (proveedor.getActivo()) {
            btnDesactivar.setText("🚫 Desactivar");
        } else {
            btnDesactivar.setText("✅ Activar");
        }
        btnDesactivar.setVisible(true);
    }

    @FXML
    private void buscar() {
        String termino = txtBuscar.getText();
        if (termino == null || termino.trim().isEmpty()) {
            cargarProveedores();
        } else {
            try {
                List<ProveedorDTO> resultado = apiService.buscarProveedores(termino);
                if (!chkMostrarInactivos.isSelected()) {
                    resultado = resultado.stream()
                            .filter(p -> p.getActivo())
                            .collect(Collectors.toList());
                }
                listaProveedores.setAll(resultado);
            } catch (Exception e) {
                mostrarError("Error", "Error en la búsqueda: " + e.getMessage());
            }
        }
    }

    @FXML
    private void nuevoProveedor() {
        limpiarFormulario();
        modoNuevo = true;
        lblTituloEdicion.setText("Nuevo Proveedor");
        btnDesactivar.setVisible(false);
        txtNombre.requestFocus();
    }

    @FXML
    private void guardar() {
        if (!validar())
            return;

        try {
            ProveedorDTO dto = new ProveedorDTO();

            if (!modoNuevo && proveedorSeleccionado != null) {
                dto.setId(proveedorSeleccionado.getId());
                dto.setActivo(proveedorSeleccionado.getActivo());
            } else {
                dto.setActivo(true);
            }

            dto.setNombre(txtNombre.getText().trim());
            dto.setNif(txtNif.getText().trim());
            dto.setDireccion(txtDireccion.getText().trim());
            dto.setTelefono(txtTelefono.getText().trim());
            dto.setEmail(txtEmail.getText().trim());
            dto.setContacto(txtContacto.getText().trim());
            dto.setIban(txtIban.getText().trim());
            dto.setNotas(txtNotas.getText());

            if (modoNuevo) {
                apiService.crearProveedor(dto);
                mostrarInfo("Éxito", "Proveedor creado correctamente");
            } else {
                apiService.actualizarProveedor(dto.getId(), dto);
                mostrarInfo("Éxito", "Proveedor actualizado correctamente");
            }

            cargarProveedores();
            limpiarFormulario();
        } catch (Exception e) {
            mostrarError("Error al guardar", e.getMessage());
        }
    }

    @FXML
    private void toggleActivar() {
        if (proveedorSeleccionado == null)
            return;

        try {
            if (proveedorSeleccionado.getActivo()) {
                apiService.desactivarProveedor(proveedorSeleccionado.getId());
                mostrarInfo("Éxito", "Proveedor desactivado correctamente");
            } else {
                apiService.activarProveedor(proveedorSeleccionado.getId());
                mostrarInfo("Éxito", "Proveedor activado correctamente");
            }
            cargarProveedores();
            limpiarFormulario();
        } catch (Exception e) {
            mostrarError("Error", e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        if (proveedorSeleccionado == null)
            return;

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar proveedor " + proveedorSeleccionado.getNombre() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer. Se recomienda desactivar en lugar de eliminar.");

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    apiService.eliminarProveedor(proveedorSeleccionado.getId());
                    cargarProveedores();
                    limpiarFormulario();
                    mostrarInfo("Éxito", "Proveedor eliminado correctamente");
                } catch (Exception e) {
                    mostrarError("Error", "No se pudo eliminar: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void cancelarEdicion() {
        limpiarFormulario();
        tablaProveedores.getSelectionModel().clearSelection();
    }

    private void limpiarFormulario() {
        proveedorSeleccionado = null;
        modoNuevo = false;
        lblTituloEdicion.setText("Selecciona un proveedor");

        txtNombre.clear();
        txtNif.clear();
        txtDireccion.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtContacto.clear();
        txtIban.clear();
        txtNotas.clear();

        btnDesactivar.setVisible(false);
    }

    private boolean validar() {
        StringBuilder errores = new StringBuilder();

        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("- El nombre es obligatorio\n");
        }
        if (txtNif.getText().trim().isEmpty()) {
            errores.append("- El NIF/CIF es obligatorio\n");
        }

        if (errores.length() > 0) {
            mostrarError("Datos incompletos", errores.toString());
            return false;
        }
        return true;
    }

    @FXML
    private void cerrar() {
        Stage stage = (Stage) tablaProveedores.getScene().getWindow();
        stage.close();
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

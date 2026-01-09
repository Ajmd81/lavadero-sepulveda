package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.api.service.FacturacionApiService;
import com.lavaderosepulveda.crm.model.FacturaRecibidaDTO;
import com.lavaderosepulveda.crm.model.ProveedorDTO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class FormularioFacturaRecibidaController {

    @FXML
    private Label lblTitulo;
    @FXML
    private ComboBox<ProveedorDTO> cmbProveedor;
    @FXML
    private TextField txtProveedorNombre;
    @FXML
    private TextField txtProveedorNif;
    @FXML
    private TextField txtNumeroFactura;
    @FXML
    private DatePicker dpFechaFactura;
    @FXML
    private ComboBox<String> cmbCategoria;
    @FXML
    private DatePicker dpFechaVencimiento;
    @FXML
    private TextField txtConcepto;
    @FXML
    private TextField txtBaseImponible;
    @FXML
    private ComboBox<BigDecimal> cmbTipoIva;
    @FXML
    private Label lblCuotaIva;
    @FXML
    private ComboBox<BigDecimal> cmbTipoIrpf;
    @FXML
    private Label lblCuotaIrpf;
    @FXML
    private Label lblTotal;
    @FXML
    private ComboBox<String> cmbEstado;
    @FXML
    private ComboBox<String> cmbMetodoPago;
    @FXML
    private TextArea txtNotas;

    private FacturacionApiService apiService;
    private FacturaRecibidaDTO facturaActual;
    private boolean modoEdicion = false;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat FORMATO_MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

    @FXML
    public void initialize() {
        apiService = FacturacionApiService.getInstance();

        configurarCombos();
        configurarListeners();
        cargarProveedores();

        // Valores por defecto
        dpFechaFactura.setValue(LocalDate.now());
        cmbTipoIva.setValue(new BigDecimal("21"));
        cmbTipoIrpf.setValue(BigDecimal.ZERO);
        cmbEstado.setValue("PENDIENTE");
    }

    private void configurarCombos() {
        // Categorías
        cmbCategoria.setItems(FXCollections.observableArrayList(
                "AGUA", "LUZ", "GAS", "ALQUILER", "SEGUROS", "SUMINISTROS",
                "PRODUCTOS", "MANTENIMIENTO", "REPARACIONES", "COMBUSTIBLE",
                "PERSONAL", "SEGURIDAD_SOCIAL", "IMPUESTOS", "TELEFONIA",
                "PUBLICIDAD", "MATERIAL_OFICINA", "GESTORIA", "BANCARIOS",
                "VEHICULOS", "MAQUINARIA", "OTROS"));

        // Tipos de IVA
        cmbTipoIva.setItems(FXCollections.observableArrayList(
                BigDecimal.ZERO, new BigDecimal("4"), new BigDecimal("10"), new BigDecimal("21")));
        cmbTipoIva.setConverter(new StringConverter<>() {
            @Override
            public String toString(BigDecimal value) {
                return value == null ? "" : value.intValue() + "%";
            }

            @Override
            public BigDecimal fromString(String string) {
                return new BigDecimal(string.replace("%", ""));
            }
        });

        // Tipos de IRPF
        cmbTipoIrpf.setItems(FXCollections.observableArrayList(
                BigDecimal.ZERO, new BigDecimal("7"), new BigDecimal("15"), new BigDecimal("19")));
        cmbTipoIrpf.setConverter(new StringConverter<>() {
            @Override
            public String toString(BigDecimal value) {
                return value == null ? "" : value.intValue() + "%";
            }

            @Override
            public BigDecimal fromString(String string) {
                return new BigDecimal(string.replace("%", ""));
            }
        });

        // Estados
        cmbEstado.setItems(FXCollections.observableArrayList("PENDIENTE", "PAGADA"));

        // Métodos de pago
        cmbMetodoPago.setItems(FXCollections.observableArrayList(
                "EFECTIVO", "TARJETA", "BIZUM", "TRANSFERENCIA"));

        // Converter para proveedores
        cmbProveedor.setConverter(new StringConverter<>() {
            @Override
            public String toString(ProveedorDTO proveedor) {
                return proveedor == null ? "" : proveedor.getNombre() + " (" + proveedor.getNif() + ")";
            }

            @Override
            public ProveedorDTO fromString(String string) {
                return null;
            }
        });
    }

    private void configurarListeners() {
        // Listener para proveedor seleccionado
        cmbProveedor.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtProveedorNombre.setText(newVal.getNombre());
                txtProveedorNif.setText(newVal.getNif());
                txtProveedorNombre.setDisable(true);
                txtProveedorNif.setDisable(true);
            } else {
                txtProveedorNombre.setDisable(false);
                txtProveedorNif.setDisable(false);
            }
        });

        // Listeners para calcular totales
        txtBaseImponible.textProperty().addListener((obs, old, nuevo) -> calcularTotales());
        cmbTipoIva.valueProperty().addListener((obs, old, nuevo) -> calcularTotales());
        cmbTipoIrpf.valueProperty().addListener((obs, old, nuevo) -> calcularTotales());
    }

    private void cargarProveedores() {
        try {
            List<ProveedorDTO> proveedores = apiService.obtenerProveedores();
            cmbProveedor.setItems(FXCollections.observableArrayList(proveedores));
        } catch (Exception e) {
            mostrarError("Error", "No se pudieron cargar los proveedores: " + e.getMessage());
        }
    }

    private void calcularTotales() {
        try {
            String baseText = txtBaseImponible.getText().replace(",", ".");
            if (baseText.isEmpty()) {
                lblCuotaIva.setText("0,00 €");
                lblCuotaIrpf.setText("0,00 €");
                lblTotal.setText("0,00 €");
                return;
            }

            BigDecimal base = new BigDecimal(baseText);
            BigDecimal tipoIva = cmbTipoIva.getValue() != null ? cmbTipoIva.getValue() : BigDecimal.ZERO;
            BigDecimal tipoIrpf = cmbTipoIrpf.getValue() != null ? cmbTipoIrpf.getValue() : BigDecimal.ZERO;

            BigDecimal cuotaIva = base.multiply(tipoIva).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal cuotaIrpf = base.multiply(tipoIrpf).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal total = base.add(cuotaIva).subtract(cuotaIrpf);

            lblCuotaIva.setText(FORMATO_MONEDA.format(cuotaIva));
            lblCuotaIrpf.setText(FORMATO_MONEDA.format(cuotaIrpf));
            lblTotal.setText(FORMATO_MONEDA.format(total));
        } catch (NumberFormatException e) {
            // Ignorar mientras se escribe
        }
    }

    public void setFactura(FacturaRecibidaDTO factura) {
        this.facturaActual = factura;
        this.modoEdicion = true;
        lblTitulo.setText("Editar Factura Recibida");

        // Cargar datos
        txtNumeroFactura.setText(factura.getNumeroFactura());
        txtConcepto.setText(factura.getConcepto());

        if (factura.getProveedorId() != null) {
            cmbProveedor.getItems().stream()
                    .filter(p -> p.getId().equals(factura.getProveedorId()))
                    .findFirst()
                    .ifPresent(cmbProveedor::setValue);
        } else {
            txtProveedorNombre.setText(factura.getProveedorNombre());
            txtProveedorNif.setText(factura.getProveedorNif());
        }

        if (factura.getFechaFactura() != null) {
            dpFechaFactura.setValue(LocalDate.parse(factura.getFechaFactura(), FORMATO_FECHA));
        }
        if (factura.getFechaVencimiento() != null) {
            dpFechaVencimiento.setValue(LocalDate.parse(factura.getFechaVencimiento(), FORMATO_FECHA));
        }

        cmbCategoria.setValue(factura.getCategoria());

        if (factura.getBaseImponible() != null) {
            txtBaseImponible.setText(factura.getBaseImponible().toString());
        }
        if (factura.getTipoIva() != null) {
            cmbTipoIva.setValue(factura.getTipoIva());
        }
        if (factura.getTipoIrpf() != null) {
            cmbTipoIrpf.setValue(factura.getTipoIrpf());
        }

        cmbEstado.setValue(factura.getEstado());
        cmbMetodoPago.setValue(factura.getMetodoPago());
        txtNotas.setText(factura.getNotas());

        calcularTotales();
    }

    @FXML
    private void nuevoProveedor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formulario-proveedor.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Nuevo Proveedor");
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarProveedores();
        } catch (Exception e) {
            mostrarError("Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    @FXML
    private void guardar() {
        if (!validar())
            return;

        try {
            FacturaRecibidaDTO dto = new FacturaRecibidaDTO();

            if (modoEdicion) {
                dto.setId(facturaActual.getId());
            }

            // Proveedor
            ProveedorDTO proveedor = cmbProveedor.getValue();
            if (proveedor != null) {
                dto.setProveedorId(proveedor.getId());
            } else {
                dto.setProveedorNombre(txtProveedorNombre.getText());
                dto.setProveedorNif(txtProveedorNif.getText());
            }

            dto.setNumeroFactura(txtNumeroFactura.getText());
            dto.setFechaFactura(dpFechaFactura.getValue().format(FORMATO_FECHA));

            if (dpFechaVencimiento.getValue() != null) {
                dto.setFechaVencimiento(dpFechaVencimiento.getValue().format(FORMATO_FECHA));
            }

            dto.setCategoria(cmbCategoria.getValue());
            dto.setConcepto(txtConcepto.getText());

            String baseText = txtBaseImponible.getText().replace(",", ".");
            dto.setBaseImponible(new BigDecimal(baseText));
            dto.setTipoIva(cmbTipoIva.getValue());
            dto.setTipoIrpf(cmbTipoIrpf.getValue());

            dto.setEstado(cmbEstado.getValue());
            dto.setMetodoPago(cmbMetodoPago.getValue());
            dto.setNotas(txtNotas.getText());

            if (modoEdicion) {
                apiService.actualizarFacturaRecibida(dto.getId(), dto);
            } else {
                apiService.crearFacturaRecibida(dto);
            }

            cerrarVentana();
        } catch (Exception e) {
            mostrarError("Error al guardar", e.getMessage());
        }
    }

    private boolean validar() {
        StringBuilder errores = new StringBuilder();

        if (txtNumeroFactura.getText().trim().isEmpty()) {
            errores.append("- El número de factura es obligatorio\n");
        }
        if (dpFechaFactura.getValue() == null) {
            errores.append("- La fecha de factura es obligatoria\n");
        }
        if (cmbCategoria.getValue() == null) {
            errores.append("- La categoría es obligatoria\n");
        }
        if (txtConcepto.getText().trim().isEmpty()) {
            errores.append("- El concepto es obligatorio\n");
        }
        if (txtBaseImponible.getText().trim().isEmpty()) {
            errores.append("- La base imponible es obligatoria\n");
        }

        // Validar que haya proveedor (seleccionado o manual)
        if (cmbProveedor.getValue() == null &&
                txtProveedorNombre.getText().trim().isEmpty()) {
            errores.append("- Debe seleccionar un proveedor o introducir los datos manualmente\n");
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
        Stage stage = (Stage) txtNumeroFactura.getScene().getWindow();
        stage.close();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

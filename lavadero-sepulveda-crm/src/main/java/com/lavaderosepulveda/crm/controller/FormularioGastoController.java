package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.api.service.FacturacionApiService;
import com.lavaderosepulveda.crm.model.dto.GastoDTO;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FormularioGastoController {

    @FXML
    private Label lblTitulo;
    @FXML
    private DatePicker dpFecha;
    @FXML
    private ComboBox<String> cmbCategoria;
    @FXML
    private TextField txtConcepto;
    @FXML
    private TextField txtBaseImponible;
    @FXML
    private ComboBox<BigDecimal> cmbTipoIva;
    @FXML
    private Label lblCuotaIva;
    @FXML
    private Label lblTotal;
    @FXML
    private CheckBox chkPagado;
    @FXML
    private ComboBox<String> cmbMetodoPago;
    @FXML
    private CheckBox chkRecurrente;
    @FXML
    private Label lblPeriodicidad;
    @FXML
    private ComboBox<String> cmbPeriodicidad;
    @FXML
    private TextArea txtNotas;

    private FacturacionApiService apiService;
    private GastoDTO gastoActual;
    private boolean modoEdicion = false;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat FORMATO_MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

    @FXML
    public void initialize() {
        apiService = FacturacionApiService.getInstance();

        configurarCombos();
        configurarListeners();

        // Valores por defecto
        dpFecha.setValue(LocalDate.now());
        cmbTipoIva.setValue(new BigDecimal("21"));
        cmbMetodoPago.setDisable(true);
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
        cmbTipoIva.setValue(new BigDecimal("21"));

        // Métodos de pago
        cmbMetodoPago.setItems(FXCollections.observableArrayList(
                "EFECTIVO", "TARJETA", "BIZUM", "TRANSFERENCIA", "DOMICILIACION"));

        // Periodicidad
        cmbPeriodicidad.setItems(FXCollections.observableArrayList(
                "MENSUAL", "BIMESTRAL", "TRIMESTRAL", "SEMESTRAL", "ANUAL"));
    }

    private void configurarListeners() {
        // Calcular totales al cambiar valores
        txtBaseImponible.textProperty().addListener((obs, old, nuevo) -> calcularTotales());
        cmbTipoIva.valueProperty().addListener((obs, old, nuevo) -> calcularTotales());

        // Habilitar método de pago si está pagado
        chkPagado.selectedProperty().addListener((obs, old, nuevo) -> {
            cmbMetodoPago.setDisable(!nuevo);
            if (!nuevo) {
                cmbMetodoPago.setValue(null);
            }
        });

        // Mostrar periodicidad si es recurrente
        chkRecurrente.selectedProperty().addListener((obs, old, nuevo) -> {
            lblPeriodicidad.setVisible(nuevo);
            cmbPeriodicidad.setVisible(nuevo);
            if (nuevo && cmbPeriodicidad.getValue() == null) {
                cmbPeriodicidad.setValue("MENSUAL");
            }
        });
    }

    private void calcularTotales() {
        try {
            String baseText = txtBaseImponible.getText().replace(",", ".");
            if (baseText.isEmpty()) {
                lblCuotaIva.setText("0,00 €");
                lblTotal.setText("0,00 €");
                return;
            }

            BigDecimal base = new BigDecimal(baseText);
            BigDecimal tipoIva = cmbTipoIva.getValue() != null ? cmbTipoIva.getValue() : BigDecimal.ZERO;

            BigDecimal cuotaIva = base.multiply(tipoIva).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal total = base.add(cuotaIva);

            lblCuotaIva.setText(FORMATO_MONEDA.format(cuotaIva));
            lblTotal.setText(FORMATO_MONEDA.format(total));
        } catch (NumberFormatException e) {
            // Ignorar mientras se escribe
        }
    }

    public void setGasto(GastoDTO gasto) {
        this.gastoActual = gasto;
        this.modoEdicion = true;
        lblTitulo.setText("Editar Gasto");

        // Cargar datos
        if (gasto.getFecha() != null && !gasto.getFecha().isEmpty()) {
            try {
                dpFecha.setValue(LocalDate.parse(gasto.getFecha(), FORMATO_FECHA));
            } catch (Exception e) {
                dpFecha.setValue(LocalDate.now());
            }
        }

        cmbCategoria.setValue(gasto.getCategoria());
        txtConcepto.setText(gasto.getConcepto());

        if (gasto.getBaseImponible() != null) {
            txtBaseImponible.setText(gasto.getBaseImponible().toString());
        }
        if (gasto.getTipoIva() != null) {
            cmbTipoIva.setValue(gasto.getTipoIva());
        }

        chkPagado.setSelected(gasto.getPagado() != null && gasto.getPagado());
        cmbMetodoPago.setValue(gasto.getMetodoPago());

        chkRecurrente.setSelected(gasto.getRecurrente() != null && gasto.getRecurrente());
        cmbPeriodicidad.setValue(gasto.getPeriodicidad());

        txtNotas.setText(gasto.getNotas());

        calcularTotales();
    }

    @FXML
    private void guardar() {
        if (!validar()) {
            return;
        }

        try {
            GastoDTO dto = modoEdicion ? gastoActual : new GastoDTO();

            dto.setFecha(dpFecha.getValue().format(FORMATO_FECHA));
            dto.setCategoria(cmbCategoria.getValue());
            dto.setConcepto(txtConcepto.getText().trim());

            String baseText = txtBaseImponible.getText().replace(",", ".");
            BigDecimal base = new BigDecimal(baseText);
            BigDecimal tipoIva = cmbTipoIva.getValue() != null ? cmbTipoIva.getValue() : BigDecimal.ZERO;
            BigDecimal cuotaIva = base.multiply(tipoIva).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal total = base.add(cuotaIva);

            dto.setBaseImponible(base);
            dto.setTipoIva(tipoIva);
            dto.setCuotaIva(cuotaIva);
            dto.setImporte(total);

            dto.setPagado(chkPagado.isSelected());
            dto.setMetodoPago(cmbMetodoPago.getValue());

            dto.setRecurrente(chkRecurrente.isSelected());
            if (chkRecurrente.isSelected()) {
                dto.setPeriodicidad(cmbPeriodicidad.getValue());
            }

            dto.setNotas(txtNotas.getText().trim());

            if (modoEdicion) {
                apiService.actualizarGasto(dto.getId(), dto);
                mostrarInfo("Éxito", "Gasto actualizado correctamente");
            } else {
                apiService.crearGasto(dto);
                mostrarInfo("Éxito", "Gasto registrado correctamente");
            }

            cerrarVentana();

        } catch (Exception e) {
            mostrarError("Error al guardar", e.getMessage());
        }
    }

    private boolean validar() {
        StringBuilder errores = new StringBuilder();

        if (dpFecha.getValue() == null) {
            errores.append("- La fecha es obligatoria\n");
        }
        if (cmbCategoria.getValue() == null) {
            errores.append("- La categoría es obligatoria\n");
        }
        if (txtConcepto.getText().trim().isEmpty()) {
            errores.append("- El concepto es obligatorio\n");
        }
        if (txtBaseImponible.getText().trim().isEmpty()) {
            errores.append("- El importe es obligatorio\n");
        } else {
            try {
                new BigDecimal(txtBaseImponible.getText().replace(",", "."));
            } catch (NumberFormatException e) {
                errores.append("- El importe debe ser un número válido\n");
            }
        }
        if (chkPagado.isSelected() && cmbMetodoPago.getValue() == null) {
            errores.append("- Debe seleccionar un método de pago\n");
        }
        if (chkRecurrente.isSelected() && cmbPeriodicidad.getValue() == null) {
            errores.append("- Debe seleccionar la periodicidad\n");
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
        Stage stage = (Stage) txtConcepto.getScene().getWindow();
        stage.close();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.initOwner(txtConcepto.getScene().getWindow());
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.initOwner(txtConcepto.getScene().getWindow());
        alert.showAndWait();
    }
}

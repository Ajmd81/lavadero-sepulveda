package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.api.service.FacturacionApiService;
import com.lavaderosepulveda.crm.model.GastoDTO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

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
    private TextField txtConcepto;
    @FXML
    private DatePicker dpFecha;
    @FXML
    private ComboBox<String> cmbCategoria;
    @FXML
    private TextField txtImporte;
    @FXML
    private CheckBox chkIvaIncluido;
    @FXML
    private Label lblBase;
    @FXML
    private Label lblIva;
    @FXML
    private ComboBox<String> cmbMetodoPago;
    @FXML
    private CheckBox chkPagado;
    @FXML
    private CheckBox chkRecurrente;
    @FXML
    private Spinner<Integer> spnDiaRecurrencia;
    @FXML
    private TextArea txtNotas;

    private FacturacionApiService apiService;
    private GastoDTO gastoActual;
    private boolean modoEdicion = false;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat FORMATO_MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    private static final BigDecimal IVA = new BigDecimal("21");

    @FXML
    public void initialize() {
        apiService = FacturacionApiService.getInstance();

        configurarCombos();
        configurarSpinner();
        configurarListeners();

        // Valores por defecto
        dpFecha.setValue(LocalDate.now());
        chkIvaIncluido.setSelected(true);
    }

    private void configurarCombos() {
        // Categorías
        cmbCategoria.setItems(FXCollections.observableArrayList(
                "AGUA", "LUZ", "GAS", "ALQUILER", "SEGUROS", "SUMINISTROS",
                "PRODUCTOS", "MANTENIMIENTO", "REPARACIONES", "COMBUSTIBLE",
                "PERSONAL", "SEGURIDAD_SOCIAL", "IMPUESTOS", "TELEFONIA",
                "PUBLICIDAD", "MATERIAL_OFICINA", "GESTORIA", "BANCARIOS",
                "VEHICULOS", "MAQUINARIA", "OTROS"));

        // Métodos de pago
        cmbMetodoPago.setItems(FXCollections.observableArrayList(
                "EFECTIVO", "TARJETA", "BIZUM", "TRANSFERENCIA"));
        cmbMetodoPago.setValue("EFECTIVO");
    }

    private void configurarSpinner() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31, 1);
        spnDiaRecurrencia.setValueFactory(valueFactory);
        spnDiaRecurrencia.setDisable(true);
    }

    private void configurarListeners() {
        // Calcular desglose al cambiar importe o checkbox IVA
        txtImporte.textProperty().addListener((obs, old, nuevo) -> calcularDesglose());
        chkIvaIncluido.selectedProperty().addListener((obs, old, nuevo) -> calcularDesglose());

        // Habilitar/deshabilitar día recurrencia
        chkRecurrente.selectedProperty().addListener((obs, old, nuevo) -> {
            spnDiaRecurrencia.setDisable(!nuevo);
        });
    }

    private void calcularDesglose() {
        try {
            String importeText = txtImporte.getText().replace(",", ".");
            if (importeText.isEmpty()) {
                lblBase.setText("0,00 €");
                lblIva.setText("0,00 €");
                return;
            }

            BigDecimal importe = new BigDecimal(importeText);
            BigDecimal base;
            BigDecimal iva;

            if (chkIvaIncluido.isSelected()) {
                // IVA incluido: Base = Importe / 1.21, IVA = Importe - Base
                base = importe.divide(new BigDecimal("1.21"), 2, RoundingMode.HALF_UP);
                iva = importe.subtract(base);
            } else {
                // IVA no incluido: Base = Importe, IVA = Base * 0.21
                base = importe;
                iva = base.multiply(IVA).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }

            lblBase.setText(FORMATO_MONEDA.format(base));
            lblIva.setText(FORMATO_MONEDA.format(iva));
        } catch (NumberFormatException e) {
            // Ignorar mientras se escribe
        }
    }

    public void setGasto(GastoDTO gasto) {
        this.gastoActual = gasto;
        this.modoEdicion = true;
        lblTitulo.setText("Editar Gasto");

        // Cargar datos
        txtConcepto.setText(gasto.getConcepto());

        if (gasto.getFecha() != null) {
            dpFecha.setValue(LocalDate.parse(gasto.getFecha(), FORMATO_FECHA));
        }

        cmbCategoria.setValue(gasto.getCategoria());

        if (gasto.getImporte() != null) {
            txtImporte.setText(gasto.getImporte().toString());
        }

        if (gasto.getIvaIncluido() != null) {
            chkIvaIncluido.setSelected(gasto.getIvaIncluido());
        }

        cmbMetodoPago.setValue(gasto.getMetodoPago());

        if (gasto.getPagado() != null) {
            chkPagado.setSelected(gasto.getPagado());
        }

        if (gasto.getRecurrente() != null) {
            chkRecurrente.setSelected(gasto.getRecurrente());
        }

        if (gasto.getDiaRecurrencia() != null) {
            spnDiaRecurrencia.getValueFactory().setValue(gasto.getDiaRecurrencia());
        }

        txtNotas.setText(gasto.getNotas());

        calcularDesglose();
    }

    @FXML
    private void guardar() {
        if (!validar())
            return;

        try {
            GastoDTO dto = new GastoDTO();

            if (modoEdicion) {
                dto.setId(gastoActual.getId());
            }

            dto.setConcepto(txtConcepto.getText());
            dto.setFecha(dpFecha.getValue().format(FORMATO_FECHA));
            dto.setCategoria(cmbCategoria.getValue());

            String importeText = txtImporte.getText().replace(",", ".");
            dto.setImporte(new BigDecimal(importeText));
            dto.setIvaIncluido(chkIvaIncluido.isSelected());

            dto.setMetodoPago(cmbMetodoPago.getValue());
            dto.setPagado(chkPagado.isSelected());
            dto.setRecurrente(chkRecurrente.isSelected());

            if (chkRecurrente.isSelected()) {
                dto.setDiaRecurrencia(spnDiaRecurrencia.getValue());
            }

            dto.setNotas(txtNotas.getText());

            if (modoEdicion) {
                apiService.actualizarGasto(dto.getId(), dto);
            } else {
                apiService.crearGasto(dto);
            }

            cerrarVentana();
        } catch (Exception e) {
            mostrarError("Error al guardar", e.getMessage());
        }
    }

    private boolean validar() {
        StringBuilder errores = new StringBuilder();

        if (txtConcepto.getText().trim().isEmpty()) {
            errores.append("- El concepto es obligatorio\n");
        }
        if (dpFecha.getValue() == null) {
            errores.append("- La fecha es obligatoria\n");
        }
        if (cmbCategoria.getValue() == null) {
            errores.append("- La categoría es obligatoria\n");
        }
        if (txtImporte.getText().trim().isEmpty()) {
            errores.append("- El importe es obligatorio\n");
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
        alert.showAndWait();
    }
}

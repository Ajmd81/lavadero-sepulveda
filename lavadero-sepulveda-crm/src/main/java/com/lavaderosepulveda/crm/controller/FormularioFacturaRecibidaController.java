package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.api.service.FacturacionApiService;
import com.lavaderosepulveda.crm.model.dto.FacturaRecibidaDTO;
import com.lavaderosepulveda.crm.model.dto.ProveedorDTO;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
import java.util.stream.Collectors;

public class FormularioFacturaRecibidaController {

    @FXML private Label lblTitulo;
    @FXML private ComboBox<ProveedorDTO> cmbProveedor;
    @FXML private TextField txtProveedorNombre;
    @FXML private TextField txtProveedorNif;
    @FXML private TextField txtNumeroFactura;
    @FXML private DatePicker dpFechaFactura;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private DatePicker dpFechaVencimiento;
    
    // Líneas de factura
    @FXML private TableView<LineaFactura> tablaLineas;
    @FXML private TableColumn<LineaFactura, String> colConcepto;
    @FXML private TableColumn<LineaFactura, String> colCantidad;
    @FXML private TableColumn<LineaFactura, String> colPrecioUnitario;
    @FXML private TableColumn<LineaFactura, String> colSubtotal;
    @FXML private TableColumn<LineaFactura, Void> colEliminar;
    @FXML private TextField txtConcepto;
    @FXML private Spinner<Integer> spnCantidad;
    @FXML private TextField txtPrecioUnitario;
    
    // Importes
    @FXML private Label lblBaseImponible;
    @FXML private ComboBox<BigDecimal> cmbTipoIva;
    @FXML private Label lblCuotaIva;
    @FXML private ComboBox<BigDecimal> cmbTipoIrpf;
    @FXML private Label lblCuotaIrpf;
    @FXML private Label lblTotal;
    
    @FXML private ComboBox<String> cmbEstado;
    @FXML private ComboBox<String> cmbMetodoPago;
    @FXML private TextArea txtNotas;

    private FacturacionApiService apiService;
    private FacturaRecibidaDTO facturaActual;
    private boolean modoEdicion = false;
    private ObservableList<LineaFactura> lineas = FXCollections.observableArrayList();

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat FORMATO_MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

    @FXML
    public void initialize() {
        apiService = FacturacionApiService.getInstance();

        configurarCombos();
        configurarTablaLineas();
        configurarListeners();
        cargarProveedores();

        // Valores por defecto
        dpFechaFactura.setValue(LocalDate.now());
        dpFechaVencimiento.setValue(LocalDate.now().plusDays(30));
        cmbTipoIva.setValue(new BigDecimal("21"));
        cmbTipoIrpf.setValue(BigDecimal.ZERO);
        cmbEstado.setValue("PENDIENTE");
        
        // Configurar spinner
        spnCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1));
        spnCantidad.setEditable(true);
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
                "", "EFECTIVO", "TARJETA", "BIZUM", "TRANSFERENCIA", "DOMICILIACION"));

        // Converter para proveedores
        cmbProveedor.setConverter(new StringConverter<>() {
            @Override
            public String toString(ProveedorDTO proveedor) {
                if (proveedor == null) return "";
                String nif = proveedor.getNif() != null && !proveedor.getNif().isEmpty() 
                    ? " (" + proveedor.getNif() + ")" : "";
                return proveedor.getNombre() + nif;
            }

            @Override
            public ProveedorDTO fromString(String string) {
                return null;
            }
        });
    }

    private void configurarTablaLineas() {
        colConcepto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getConcepto()));
        colCantidad.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCantidad())));
        colPrecioUnitario.setCellValueFactory(c -> new SimpleStringProperty(
                FORMATO_MONEDA.format(c.getValue().getPrecioUnitario())));
        colSubtotal.setCellValueFactory(c -> new SimpleStringProperty(
                FORMATO_MONEDA.format(c.getValue().getSubtotal())));
        
        // Botón eliminar
        colEliminar.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("🗑");
            {
                btn.setOnAction(e -> {
                    LineaFactura linea = getTableView().getItems().get(getIndex());
                    lineas.remove(linea);
                    calcularTotales();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        
        tablaLineas.setItems(lineas);
    }

    private void configurarListeners() {
        // Listener para proveedor seleccionado
        cmbProveedor.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtProveedorNombre.setText(newVal.getNombre());
                txtProveedorNif.setText(newVal.getNif() != null ? newVal.getNif() : "");
                txtProveedorNombre.setDisable(true);
                txtProveedorNif.setDisable(true);
            } else {
                txtProveedorNombre.setDisable(false);
                txtProveedorNif.setDisable(false);
            }
        });

        // Listeners para recalcular totales al cambiar IVA/IRPF
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

    @FXML
    private void agregarLinea() {
        String concepto = txtConcepto.getText().trim();
        String precioStr = txtPrecioUnitario.getText().trim().replace(",", ".");
        
        if (concepto.isEmpty()) {
            mostrarError("Error", "El concepto es obligatorio");
            return;
        }
        
        if (precioStr.isEmpty()) {
            mostrarError("Error", "El precio es obligatorio");
            return;
        }
        
        try {
            BigDecimal precio = new BigDecimal(precioStr);
            int cantidad = spnCantidad.getValue();
            
            LineaFactura linea = new LineaFactura(concepto, cantidad, precio);
            lineas.add(linea);
            
            // Limpiar campos
            txtConcepto.clear();
            txtPrecioUnitario.clear();
            spnCantidad.getValueFactory().setValue(1);
            
            calcularTotales();
            
        } catch (NumberFormatException e) {
            mostrarError("Error", "El precio debe ser un número válido");
        }
    }

    private void calcularTotales() {
        BigDecimal base = lineas.stream()
                .map(LineaFactura::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal tipoIva = cmbTipoIva.getValue() != null ? cmbTipoIva.getValue() : BigDecimal.ZERO;
        BigDecimal tipoIrpf = cmbTipoIrpf.getValue() != null ? cmbTipoIrpf.getValue() : BigDecimal.ZERO;

        BigDecimal cuotaIva = base.multiply(tipoIva).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal cuotaIrpf = base.multiply(tipoIrpf).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal total = base.add(cuotaIva).subtract(cuotaIrpf);

        lblBaseImponible.setText(FORMATO_MONEDA.format(base));
        lblCuotaIva.setText(FORMATO_MONEDA.format(cuotaIva));
        lblCuotaIrpf.setText(FORMATO_MONEDA.format(cuotaIrpf));
        lblTotal.setText(FORMATO_MONEDA.format(total));
    }

    public void setFactura(FacturaRecibidaDTO factura) {
        this.facturaActual = factura;
        this.modoEdicion = true;
        lblTitulo.setText("Editar Factura Recibida");

        // Cargar datos básicos
        txtNumeroFactura.setText(factura.getNumeroFactura());

        if (factura.getProveedorId() != null) {
            cmbProveedor.getItems().stream()
                    .filter(p -> p.getId().equals(factura.getProveedorId()))
                    .findFirst()
                    .ifPresent(cmbProveedor::setValue);
        } else {
            txtProveedorNombre.setText(factura.getProveedorNombre());
            txtProveedorNif.setText(factura.getProveedorNif());
        }

        if (factura.getFechaFactura() != null && !factura.getFechaFactura().isEmpty()) {
            try {
                dpFechaFactura.setValue(LocalDate.parse(factura.getFechaFactura(), FORMATO_FECHA));
            } catch (Exception e) {
                // Ignorar
            }
        }
        if (factura.getFechaVencimiento() != null && !factura.getFechaVencimiento().isEmpty()) {
            try {
                dpFechaVencimiento.setValue(LocalDate.parse(factura.getFechaVencimiento(), FORMATO_FECHA));
            } catch (Exception e) {
                // Ignorar
            }
        }

        cmbCategoria.setValue(factura.getCategoria());

        // Cargar línea única con el concepto existente
        if (factura.getConcepto() != null && !factura.getConcepto().isEmpty() && factura.getBaseImponible() != null) {
            LineaFactura linea = new LineaFactura(factura.getConcepto(), 1, factura.getBaseImponible());
            lineas.add(linea);
        }

        if (factura.getTipoIva() != null) {
            cmbTipoIva.setValue(factura.getTipoIva());
        }
        if (factura.getTipoIrpf() != null) {
            cmbTipoIrpf.setValue(factura.getTipoIrpf());
        }

        cmbEstado.setValue(factura.getEstado());
        cmbMetodoPago.setValue(factura.getMetodoPago() != null ? factura.getMetodoPago() : "");
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
            stage.initOwner(txtNumeroFactura.getScene().getWindow());
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
                dto.setProveedorNombre(proveedor.getNombre());
                dto.setProveedorNif(proveedor.getNif());
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
            
            // Concatenar conceptos de las líneas
            String conceptosConcatenados = lineas.stream()
                    .map(l -> l.getCantidad() + "x " + l.getConcepto())
                    .collect(Collectors.joining(", "));
            dto.setConcepto(conceptosConcatenados);

            // Calcular totales
            BigDecimal base = lineas.stream()
                    .map(LineaFactura::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal tipoIva = cmbTipoIva.getValue() != null ? cmbTipoIva.getValue() : BigDecimal.ZERO;
            BigDecimal tipoIrpf = cmbTipoIrpf.getValue() != null ? cmbTipoIrpf.getValue() : BigDecimal.ZERO;
            BigDecimal cuotaIva = base.multiply(tipoIva).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal cuotaIrpf = base.multiply(tipoIrpf).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal total = base.add(cuotaIva).subtract(cuotaIrpf);

            dto.setBaseImponible(base);
            dto.setTipoIva(tipoIva);
            dto.setCuotaIva(cuotaIva);
            dto.setTipoIrpf(tipoIrpf);
            dto.setCuotaIrpf(cuotaIrpf);
            dto.setTotal(total);

            dto.setEstado(cmbEstado.getValue());
            String metodoPago = cmbMetodoPago.getValue();
            dto.setMetodoPago(metodoPago != null && !metodoPago.isEmpty() ? metodoPago : null);
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
        if (lineas.isEmpty()) {
            errores.append("- Debe añadir al menos una línea de factura\n");
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
        alert.initOwner(txtNumeroFactura.getScene().getWindow());
        alert.showAndWait();
    }

    // ============ CLASE INTERNA PARA LÍNEAS DE FACTURA ============
    
    public static class LineaFactura {
        private String concepto;
        private int cantidad;
        private BigDecimal precioUnitario;
        
        public LineaFactura(String concepto, int cantidad, BigDecimal precioUnitario) {
            this.concepto = concepto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
        }
        
        public String getConcepto() { return concepto; }
        public int getCantidad() { return cantidad; }
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        
        public BigDecimal getSubtotal() {
            return precioUnitario.multiply(new BigDecimal(cantidad));
        }
    }
}

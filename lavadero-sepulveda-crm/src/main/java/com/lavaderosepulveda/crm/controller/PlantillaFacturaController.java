package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.model.entity.PlantillaFacturaConfig;
import com.lavaderosepulveda.crm.service.PlantillaFacturaService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Base64;

/**
 * Controlador para el editor de plantilla de facturas
 */
@Slf4j
public class PlantillaFacturaController {

    // ========================================
    // DATOS DEL EMISOR
    // ========================================
    @FXML
    private TextField txtEmisorNombre;
    @FXML
    private TextField txtEmisorNif;
    @FXML
    private TextField txtEmisorDireccion;
    @FXML
    private TextField txtEmisorCodigoPostal;
    @FXML
    private TextField txtEmisorCiudad;
    @FXML
    private TextField txtEmisorProvincia;
    @FXML
    private TextField txtEmisorTelefono;
    @FXML
    private TextField txtEmisorEmail;
    @FXML
    private TextField txtEmisorWeb;

    // ========================================
    // LOGO
    // ========================================
    @FXML
    private ImageView imgLogo;
    @FXML
    private Button btnCargarLogo;
    @FXML
    private Button btnEliminarLogo;
    @FXML
    private Spinner<Integer> spnLogoAncho;
    @FXML
    private Spinner<Integer> spnLogoAlto;
    @FXML
    private CheckBox chkMostrarLogo;

    // ========================================
    // COLORES
    // ========================================
    @FXML
    private ColorPicker cpColorPrimario;
    @FXML
    private ColorPicker cpColorSecundario;
    @FXML
    private ColorPicker cpColorTexto;
    @FXML
    private ColorPicker cpColorFondo;
    @FXML
    private ColorPicker cpColorBorde;

    // ========================================
    // TEXTOS
    // ========================================
    @FXML
    private TextField txtTituloFactura;
    @FXML
    private TextArea txtPieFactura;
    @FXML
    private TextField txtCondicionesPago;
    @FXML
    private TextField txtCuentaBancaria;
    @FXML
    private TextField txtTextoGracias;

    // ========================================
    // OPCIONES
    // ========================================
    @FXML
    private CheckBox chkMostrarDatosContacto;
    @FXML
    private CheckBox chkMostrarCuentaBancaria;
    @FXML
    private CheckBox chkMostrarCondicionesPago;
    @FXML
    private CheckBox chkMostrarTextoGracias;
    @FXML
    private CheckBox chkMostrarMarcaAgua;
    @FXML
    private CheckBox chkFilasAlternas;

    // ========================================
    // VISTA PREVIA
    // ========================================
    @FXML
    private WebView webVistaPrevia;
    @FXML
    private HBox panelPrincipal;

    // ========================================
    // BOTONES
    // ========================================
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnRestablecer;
    @FXML
    private Button btnExportar;
    @FXML
    private Button btnImportar;

    private final PlantillaFacturaService plantillaService = PlantillaFacturaService.getInstance();
    private PlantillaFacturaConfig config;
    private String logoBase64Actual = "";

    @FXML
    public void initialize() {
        log.info("Inicializando editor de plantilla de facturas...");

        config = plantillaService.getConfiguracion();

        // Configurar spinners
        configurarSpinners();

        // Cargar datos actuales
        cargarDatosEnFormulario();

        // Listeners para actualizar vista previa
        configurarListeners();

        // Vista previa inicial
        actualizarVistaPrevia();
    }

    private void configurarSpinners() {
        if (spnLogoAncho != null) {
            spnLogoAncho.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(50, 300, 150, 10));
        }
        if (spnLogoAlto != null) {
            spnLogoAlto.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(30, 150, 60, 5));
        }
    }

    private void cargarDatosEnFormulario() {
        // Emisor
        if (txtEmisorNombre != null)
            txtEmisorNombre.setText(config.getEmisorNombre());
        if (txtEmisorNif != null)
            txtEmisorNif.setText(config.getEmisorNif());
        if (txtEmisorDireccion != null)
            txtEmisorDireccion.setText(config.getEmisorDireccion());
        if (txtEmisorCodigoPostal != null)
            txtEmisorCodigoPostal.setText(config.getEmisorCodigoPostal());
        if (txtEmisorCiudad != null)
            txtEmisorCiudad.setText(config.getEmisorCiudad());
        if (txtEmisorProvincia != null)
            txtEmisorProvincia.setText(config.getEmisorProvincia());
        if (txtEmisorTelefono != null)
            txtEmisorTelefono.setText(config.getEmisorTelefono());
        if (txtEmisorEmail != null)
            txtEmisorEmail.setText(config.getEmisorEmail());
        if (txtEmisorWeb != null)
            txtEmisorWeb.setText(config.getEmisorWeb());

        // Logo
        logoBase64Actual = config.getLogoBase64();
        if (logoBase64Actual != null && !logoBase64Actual.isEmpty()) {
            cargarImagenEnVisor(logoBase64Actual);
        }
        if (spnLogoAncho != null)
            spnLogoAncho.getValueFactory().setValue(config.getLogoAncho());
        if (spnLogoAlto != null)
            spnLogoAlto.getValueFactory().setValue(config.getLogoAlto());
        if (chkMostrarLogo != null)
            chkMostrarLogo.setSelected(config.isMostrarLogo());

        // Colores
        if (cpColorPrimario != null)
            cpColorPrimario.setValue(Color.web(config.getColorPrimario()));
        if (cpColorSecundario != null)
            cpColorSecundario.setValue(Color.web(config.getColorSecundario()));
        if (cpColorTexto != null)
            cpColorTexto.setValue(Color.web(config.getColorTexto()));
        if (cpColorFondo != null)
            cpColorFondo.setValue(Color.web(config.getColorFondo()));
        if (cpColorBorde != null)
            cpColorBorde.setValue(Color.web(config.getColorBorde()));

        // Textos
        if (txtTituloFactura != null)
            txtTituloFactura.setText(config.getTituloFactura());
        if (txtPieFactura != null)
            txtPieFactura.setText(config.getPieFactura());
        if (txtCondicionesPago != null)
            txtCondicionesPago.setText(config.getCondicionesPago());
        if (txtCuentaBancaria != null)
            txtCuentaBancaria.setText(config.getCuentaBancaria());
        if (txtTextoGracias != null)
            txtTextoGracias.setText(config.getTextoGracias());

        // Opciones
        if (chkMostrarDatosContacto != null)
            chkMostrarDatosContacto.setSelected(config.isMostrarDatosContacto());
        if (chkMostrarCuentaBancaria != null)
            chkMostrarCuentaBancaria.setSelected(config.isMostrarCuentaBancaria());
        if (chkMostrarCondicionesPago != null)
            chkMostrarCondicionesPago.setSelected(config.isMostrarCondicionesPago());
        if (chkMostrarTextoGracias != null)
            chkMostrarTextoGracias.setSelected(config.isMostrarTextoGracias());
        if (chkMostrarMarcaAgua != null)
            chkMostrarMarcaAgua.setSelected(config.isMostrarMarcaAgua());
        if (chkFilasAlternas != null)
            chkFilasAlternas.setSelected(config.isUsarFilasAlternas());
    }

    private void configurarListeners() {
        // Listener para actualizar vista previa cuando cambian los campos
        if (txtEmisorNombre != null)
            txtEmisorNombre.textProperty().addListener((o, old, newVal) -> actualizarVistaPreviaConRetraso());
        if (txtEmisorNif != null)
            txtEmisorNif.textProperty().addListener((o, old, newVal) -> actualizarVistaPreviaConRetraso());
        if (txtEmisorDireccion != null)
            txtEmisorDireccion.textProperty().addListener((o, old, newVal) -> actualizarVistaPreviaConRetraso());

        if (cpColorPrimario != null)
            cpColorPrimario.valueProperty().addListener((o, old, newVal) -> actualizarVistaPrevia());
        if (cpColorSecundario != null)
            cpColorSecundario.valueProperty().addListener((o, old, newVal) -> actualizarVistaPrevia());
        if (cpColorTexto != null)
            cpColorTexto.valueProperty().addListener((o, old, newVal) -> actualizarVistaPrevia());

        if (chkMostrarLogo != null)
            chkMostrarLogo.selectedProperty().addListener((o, old, newVal) -> actualizarVistaPrevia());
        if (chkMostrarDatosContacto != null)
            chkMostrarDatosContacto.selectedProperty().addListener((o, old, newVal) -> actualizarVistaPrevia());
        if (chkMostrarCuentaBancaria != null)
            chkMostrarCuentaBancaria.selectedProperty().addListener((o, old, newVal) -> actualizarVistaPrevia());
    }

    private void actualizarVistaPreviaConRetraso() {
        // Pequeño retraso para no actualizar en cada tecla
        new Thread(() -> {
            try {
                Thread.sleep(500);
                Platform.runLater(this::actualizarVistaPrevia);
            } catch (InterruptedException ignored) {
            }
        }).start();
    }

    @FXML
    private void cargarLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Logo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"));

        File archivo = fileChooser.showOpenDialog(btnCargarLogo.getScene().getWindow());
        if (archivo != null) {
            try {
                logoBase64Actual = plantillaService.cargarLogoDesdeArchivo(archivo);
                cargarImagenEnVisor(logoBase64Actual);
                actualizarVistaPrevia();
                log.info("Logo cargado: {}", archivo.getName());
            } catch (Exception e) {
                log.error("Error cargando logo", e);
                mostrarError("Error al cargar el logo: " + e.getMessage());
            }
        }
    }

    @FXML
    private void eliminarLogo() {
        logoBase64Actual = "";
        if (imgLogo != null) {
            imgLogo.setImage(null);
        }
        actualizarVistaPrevia();
    }

    private void cargarImagenEnVisor(String base64) {
        if (imgLogo == null || base64 == null || base64.isEmpty())
            return;

        try {
            String datos = base64;
            if (base64.contains(",")) {
                datos = base64.substring(base64.indexOf(",") + 1);
            }
            byte[] bytes = Base64.getDecoder().decode(datos);
            Image imagen = new Image(new ByteArrayInputStream(bytes));
            imgLogo.setImage(imagen);
        } catch (Exception e) {
            log.error("Error cargando imagen en visor", e);
        }
    }

    @FXML
    private void guardarConfiguracion() {
        actualizarConfigDesdeFormulario();

        boolean guardado = plantillaService.guardarConfiguracion(config);

        if (guardado) {
            mostrarInfo("Configuración Guardada", "La plantilla de factura se ha guardado correctamente.");
        } else {
            mostrarError("Error al guardar la configuración.");
        }
    }

    private void actualizarConfigDesdeFormulario() {
        // Emisor
        if (txtEmisorNombre != null)
            config.setEmisorNombre(txtEmisorNombre.getText());
        if (txtEmisorNif != null)
            config.setEmisorNif(txtEmisorNif.getText());
        if (txtEmisorDireccion != null)
            config.setEmisorDireccion(txtEmisorDireccion.getText());
        if (txtEmisorCodigoPostal != null)
            config.setEmisorCodigoPostal(txtEmisorCodigoPostal.getText());
        if (txtEmisorCiudad != null)
            config.setEmisorCiudad(txtEmisorCiudad.getText());
        if (txtEmisorProvincia != null)
            config.setEmisorProvincia(txtEmisorProvincia.getText());
        if (txtEmisorTelefono != null)
            config.setEmisorTelefono(txtEmisorTelefono.getText());
        if (txtEmisorEmail != null)
            config.setEmisorEmail(txtEmisorEmail.getText());
        if (txtEmisorWeb != null)
            config.setEmisorWeb(txtEmisorWeb.getText());

        // Logo
        config.setLogoBase64(logoBase64Actual);
        if (spnLogoAncho != null)
            config.setLogoAncho(spnLogoAncho.getValue());
        if (spnLogoAlto != null)
            config.setLogoAlto(spnLogoAlto.getValue());
        if (chkMostrarLogo != null)
            config.setMostrarLogo(chkMostrarLogo.isSelected());

        // Colores
        if (cpColorPrimario != null)
            config.setColorPrimario(colorToHex(cpColorPrimario.getValue()));
        if (cpColorSecundario != null)
            config.setColorSecundario(colorToHex(cpColorSecundario.getValue()));
        if (cpColorTexto != null)
            config.setColorTexto(colorToHex(cpColorTexto.getValue()));
        if (cpColorFondo != null)
            config.setColorFondo(colorToHex(cpColorFondo.getValue()));
        if (cpColorBorde != null)
            config.setColorBorde(colorToHex(cpColorBorde.getValue()));

        // Textos
        if (txtTituloFactura != null)
            config.setTituloFactura(txtTituloFactura.getText());
        if (txtPieFactura != null)
            config.setPieFactura(txtPieFactura.getText());
        if (txtCondicionesPago != null)
            config.setCondicionesPago(txtCondicionesPago.getText());
        if (txtCuentaBancaria != null)
            config.setCuentaBancaria(txtCuentaBancaria.getText());
        if (txtTextoGracias != null)
            config.setTextoGracias(txtTextoGracias.getText());

        // Opciones
        if (chkMostrarDatosContacto != null)
            config.setMostrarDatosContacto(chkMostrarDatosContacto.isSelected());
        if (chkMostrarCuentaBancaria != null)
            config.setMostrarCuentaBancaria(chkMostrarCuentaBancaria.isSelected());
        if (chkMostrarCondicionesPago != null)
            config.setMostrarCondicionesPago(chkMostrarCondicionesPago.isSelected());
        if (chkMostrarTextoGracias != null)
            config.setMostrarTextoGracias(chkMostrarTextoGracias.isSelected());
        if (chkMostrarMarcaAgua != null)
            config.setMostrarMarcaAgua(chkMostrarMarcaAgua.isSelected());
        if (chkFilasAlternas != null)
            config.setUsarFilasAlternas(chkFilasAlternas.isSelected());
    }

    @FXML
    private void restablecerPorDefecto() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Restablecer Configuración");
        alert.setHeaderText("¿Estás seguro?");
        alert.setContentText("Se restablecerán todos los valores a la configuración por defecto.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            plantillaService.restablecerPorDefecto();
            config = plantillaService.getConfiguracion();
            cargarDatosEnFormulario();
            actualizarVistaPrevia();
            mostrarInfo("Configuración Restablecida", "Se han restaurado los valores por defecto.");
        }
    }

    @FXML
    private void exportarConfiguracion() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Configuración");
        fileChooser.setInitialFileName("plantilla-factura.json");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON", "*.json"));

        File archivo = fileChooser.showSaveDialog(btnExportar.getScene().getWindow());
        if (archivo != null) {
            try {
                actualizarConfigDesdeFormulario();
                plantillaService.exportarConfiguracion(archivo);
                mostrarInfo("Exportación Completada", "Configuración exportada a: " + archivo.getName());
            } catch (Exception e) {
                log.error("Error exportando configuración", e);
                mostrarError("Error al exportar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void importarConfiguracion() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importar Configuración");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON", "*.json"));

        File archivo = fileChooser.showOpenDialog(btnImportar.getScene().getWindow());
        if (archivo != null) {
            try {
                config = plantillaService.importarConfiguracion(archivo);
                cargarDatosEnFormulario();
                actualizarVistaPrevia();
                mostrarInfo("Importación Completada", "Configuración importada correctamente.");
            } catch (Exception e) {
                log.error("Error importando configuración", e);
                mostrarError("Error al importar: " + e.getMessage());
            }
        }
    }

    private void actualizarVistaPrevia() {
        if (webVistaPrevia == null)
            return;

        actualizarConfigDesdeFormulario();
        String html = generarHtmlVistaPrevia();
        webVistaPrevia.getEngine().loadContent(html);
    }

    private String generarHtmlVistaPrevia() {
        String colorPrimario = config.getColorPrimario();
        String colorTexto = config.getColorTexto();
        String colorFondo = config.getColorFondo();
        String colorBorde = config.getColorBorde();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append(
                "body { font-family: 'Helvetica', sans-serif; font-size: 10px; margin: 15px; background: #f0f0f0; }");
        html.append(".factura { background: ").append(colorFondo)
                .append("; padding: 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); max-width: 500px; margin: auto; }");
        html.append(".cabecera { display: flex; justify-content: space-between; border-bottom: 2px solid ")
                .append(colorPrimario).append("; padding-bottom: 10px; margin-bottom: 15px; }");
        html.append(".logo-zona { }");
        html.append(".logo { max-width: ").append(config.getLogoAncho()).append("px; max-height: ")
                .append(config.getLogoAlto()).append("px; }");
        html.append(".titulo-zona { text-align: right; }");
        html.append(".titulo { font-size: 18px; font-weight: bold; color: ").append(colorPrimario)
                .append("; margin: 0; }");
        html.append(".numero { font-size: 11px; color: ").append(colorTexto).append("; }");
        html.append(".emisor { font-size: 9px; color: #666; margin-bottom: 15px; }");
        html.append(".emisor-nombre { font-weight: bold; font-size: 11px; color: ").append(colorTexto).append("; }");
        html.append(".cliente-box { background: #f9f9f9; padding: 10px; border-left: 3px solid ").append(colorPrimario)
                .append("; margin-bottom: 15px; }");
        html.append(".cliente-titulo { font-weight: bold; font-size: 9px; color: #999; margin-bottom: 5px; }");
        html.append(".tabla { width: 100%; border-collapse: collapse; margin-bottom: 15px; }");
        html.append(".tabla th { background: ").append(colorPrimario)
                .append("; color: white; padding: 8px; text-align: left; font-size: 9px; }");
        html.append(".tabla td { padding: 8px; border-bottom: 1px solid ").append(colorBorde)
                .append("; font-size: 9px; }");
        if (config.isUsarFilasAlternas()) {
            html.append(".tabla tr:nth-child(even) { background: #f9f9f9; }");
        }
        html.append(".totales { text-align: right; margin-top: 10px; }");
        html.append(
                ".total-linea { display: flex; justify-content: flex-end; gap: 30px; padding: 3px 0; font-size: 9px; }");
        html.append(".total-final { font-weight: bold; font-size: 12px; color: ").append(colorPrimario)
                .append("; border-top: 2px solid ").append(colorPrimario)
                .append("; padding-top: 5px; margin-top: 5px; }");
        html.append(".pie { text-align: center; margin-top: 20px; padding-top: 10px; border-top: 1px solid ")
                .append(colorBorde).append("; font-size: 9px; color: #666; }");
        html.append("</style></head><body>");

        html.append("<div class='factura'>");

        // Cabecera
        html.append("<div class='cabecera'>");
        html.append("<div class='logo-zona'>");
        if (config.isMostrarLogo() && logoBase64Actual != null && !logoBase64Actual.isEmpty()) {
            html.append("<img src='").append(logoBase64Actual).append("' class='logo'/>");
        }
        html.append("</div>");
        html.append("<div class='titulo-zona'>");
        html.append("<h1 class='titulo'>").append(escapeHtml(config.getTituloFactura())).append("</h1>");
        html.append("<div class='numero'>Nº: 2026/001</div>");
        html.append("<div class='numero'>Fecha: 01/01/2026</div>");
        html.append("</div>");
        html.append("</div>");

        // Datos emisor
        html.append("<div class='emisor'>");
        html.append("<div class='emisor-nombre'>").append(escapeHtml(config.getEmisorNombre())).append("</div>");
        html.append("<div>NIF: ").append(escapeHtml(config.getEmisorNif())).append("</div>");
        html.append("<div>").append(escapeHtml(config.getDireccionCompleta())).append("</div>");
        if (config.isMostrarDatosContacto()) {
            if (config.getEmisorTelefono() != null && !config.getEmisorTelefono().isEmpty()) {
                html.append("<div>Tel: ").append(escapeHtml(config.getEmisorTelefono())).append("</div>");
            }
            if (config.getEmisorEmail() != null && !config.getEmisorEmail().isEmpty()) {
                html.append("<div>").append(escapeHtml(config.getEmisorEmail())).append("</div>");
            }
        }
        html.append("</div>");

        // Cliente
        html.append("<div class='cliente-box'>");
        html.append("<div class='cliente-titulo'>CLIENTE</div>");
        html.append("<div><strong>Cliente Ejemplo S.L.</strong></div>");
        html.append("<div>B12345678</div>");
        html.append("<div>Calle Ejemplo, 123</div>");
        html.append("<div>14001 Córdoba</div>");
        html.append("</div>");

        // Tabla de conceptos
        html.append("<table class='tabla'>");
        html.append("<tr><th>Concepto</th><th>Cant.</th><th>Precio</th><th>Importe</th></tr>");
        html.append("<tr><td>Lavado Exterior Completo</td><td>1</td><td>15,00 €</td><td>15,00 €</td></tr>");
        html.append("<tr><td>Lavado Interior</td><td>1</td><td>20,00 €</td><td>20,00 €</td></tr>");
        html.append("<tr><td>Tratamiento de Tapicería</td><td>1</td><td>25,00 €</td><td>25,00 €</td></tr>");
        html.append("</table>");

        // Totales
        html.append("<div class='totales'>");
        html.append("<div class='total-linea'><span>Base Imponible:</span><span>60,00 €</span></div>");
        html.append("<div class='total-linea'><span>IVA (21%):</span><span>12,60 €</span></div>");
        html.append("<div class='total-linea total-final'><span>TOTAL:</span><span>72,60 €</span></div>");
        html.append("</div>");

        // Pie
        html.append("<div class='pie'>");
        if (config.isMostrarCondicionesPago() && config.getCondicionesPago() != null
                && !config.getCondicionesPago().isEmpty()) {
            html.append("<div>").append(escapeHtml(config.getCondicionesPago())).append("</div>");
        }
        if (config.isMostrarCuentaBancaria() && config.getCuentaBancaria() != null
                && !config.getCuentaBancaria().isEmpty()) {
            html.append("<div>Cuenta: ").append(escapeHtml(config.getCuentaBancaria())).append("</div>");
        }
        if (config.isMostrarTextoGracias() && config.getTextoGracias() != null && !config.getTextoGracias().isEmpty()) {
            html.append("<div style='margin-top:10px; font-style:italic;'>")
                    .append(escapeHtml(config.getTextoGracias())).append("</div>");
        }
        html.append("</div>");

        html.append("</div>");
        html.append("</body></html>");

        return html.toString();
    }

    private String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private String escapeHtml(String text) {
        if (text == null)
            return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
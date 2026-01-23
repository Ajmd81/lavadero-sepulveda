package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.config.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class MainController {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    public void initialize() {
        // Cargar dashboard por defecto
        cargarDashboard();
    }

    @FXML
    private void cargarDashboard() {
        cargarVista("/fxml/dashboard.fxml");
    }

    @FXML
    private void cargarClientes() {
        cargarVista("/fxml/clientes.fxml");
    }

    @FXML
    private void cargarProveedores(){ cargarVista("/fxml/proveedores.fxml");}

    @FXML
    private void cargarCitas() {
        cargarVista("/fxml/citas.fxml");
    }

    @FXML
    private void cargarFacturacion() {
        cargarVista("/fxml/facturacion.fxml");
    }

    @FXML
    private void cargarContabilidad() {
        cargarVista("/fxml/contabilidad.fxml");
    }

    @FXML
    private void cargarResumenFinanciero() {
        cargarVista("/fxml/resumen-financiero.fxml");
    }

    @FXML
    private void cargarModelosFiscales() {
        cargarVista("/fxml/modelos-fiscales.fxml");
    }

    @FXML
    private void cargarPlantillaFactura() {
        cargarVista("/fxml/plantilla_factura.fxml");
    }

    @FXML
    private void abrirConfiguracion() {
        // TODO: Implementar ventana de configuración
        log.info("Abriendo configuración...");
    }

    @FXML
    private void cargarInformePyG() {
        cargarVista("/fxml/informe-pyg.fxml");
    }

    @FXML
    private void salir() {
        StageManager.getInstance().getPrimaryStage().close();
    }

    private void cargarVista(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent vista = loader.load();
            mainBorderPane.setCenter(vista);
            log.debug("Vista cargada: {}", fxmlPath);
        } catch (IOException e) {
            log.error("Error al cargar vista: " + fxmlPath, e);
        }
    }
}

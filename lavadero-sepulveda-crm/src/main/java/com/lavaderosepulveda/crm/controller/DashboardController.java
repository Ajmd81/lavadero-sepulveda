package com.lavaderosepulveda.crm.controller;

import com.lavaderosepulveda.crm.api.dto.ClienteDTO;
import com.lavaderosepulveda.crm.service.DashboardService;
import com.lavaderosepulveda.crm.service.DashboardService.DashboardMetrics;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DashboardController {

    @FXML private Label lblCitasHoy;
    @FXML private Label lblCitasPendientes;
    @FXML private Label lblFacturadoHoy;
    @FXML private Label lblFacturadoMes;
    @FXML private Label lblPendienteCobro;
    @FXML private Label lblClientesActivos;

    @FXML private TableView<ClienteDTO> tblTopClientes;
    @FXML private TableColumn<ClienteDTO, String> colNombreTop;
    @FXML private TableColumn<ClienteDTO, Integer> colCitasTop;
    @FXML private TableColumn<ClienteDTO, Double> colFacturadoTop;

    @FXML private TableView<ClienteDTO> tblNoPresentados;
    @FXML private TableColumn<ClienteDTO, String> colNombreNo;
    @FXML private TableColumn<ClienteDTO, Integer> colNoPresentaciones;
    @FXML private TableColumn<ClienteDTO, Double> colTasaNo;

    private final DashboardService dashboardService = DashboardService.getInstance();

    @FXML
    public void initialize() {
        configurarTablas();
        cargarDatos();
    }

    private void configurarTablas() {
        // Tabla top clientes
        colNombreTop.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colCitasTop.setCellValueFactory(new PropertyValueFactory<>("citasCompletadas"));
        colFacturadoTop.setCellValueFactory(new PropertyValueFactory<>("totalFacturado"));

        // Tabla no presentados
        colNombreNo.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colNoPresentaciones.setCellValueFactory(new PropertyValueFactory<>("citasNoPresentadas"));
        colTasaNo.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createObjectBinding(
                () -> cellData.getValue().getTasaNoPresentacion()
            )
        );
    }

    private void cargarDatos() {
        // Ejecutar en hilo separado para no bloquear UI
        new Thread(() -> {
            try {
                DashboardMetrics metrics = dashboardService.obtenerMetricsHoy();
                List<ClienteDTO> topClientes = dashboardService.obtenerTopClientesPorFacturacion(10);
                List<ClienteDTO> noPresentados = dashboardService.obtenerClientesConMasNoPresentaciones(10);

                // Actualizar UI en el hilo de JavaFX
                Platform.runLater(() -> {
                    actualizarMetricas(metrics);
                    actualizarTopClientes(topClientes);
                    actualizarNoPresentados(noPresentados);
                });
            } catch (Exception e) {
                log.error("Error al cargar datos del dashboard", e);
                Platform.runLater(() -> {
                    // Mostrar error en UI
                    lblCitasHoy.setText("Error");
                });
            }
        }).start();
    }

    private void actualizarMetricas(DashboardMetrics metrics) {
        lblCitasHoy.setText(String.valueOf(metrics.getCitasHoy()));
        lblCitasPendientes.setText(String.valueOf(metrics.getCitasPendientes()));
        lblFacturadoHoy.setText(String.format("%.2f €", metrics.getFacturadoHoy()));
        lblFacturadoMes.setText(String.format("%.2f €", metrics.getFacturadoMes()));
        lblPendienteCobro.setText(String.format("%.2f €", metrics.getPendienteCobro()));
        lblClientesActivos.setText(String.valueOf(metrics.getClientesActivos()));
    }

    private void actualizarTopClientes(List<ClienteDTO> clientes) {
        ObservableList<ClienteDTO> data = FXCollections.observableArrayList(clientes);
        tblTopClientes.setItems(data);
    }

    private void actualizarNoPresentados(List<ClienteDTO> clientes) {
        ObservableList<ClienteDTO> data = FXCollections.observableArrayList(clientes);
        tblNoPresentados.setItems(data);
    }

    @FXML
    private void refrescar() {
        cargarDatos();
    }
}

package com.lavaderosepulveda.crm;

import com.lavaderosepulveda.crm.api.ApiClient;
import com.lavaderosepulveda.crm.config.ConfigManager;
import com.lavaderosepulveda.crm.config.StageManager;
import com.lavaderosepulveda.crm.util.AlertUtil;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LavaderoSepulvedaCRMApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            log.info("Iniciando Lavadero Sepúlveda CRM...");
            
            // Inicializar configuración
            ConfigManager config = ConfigManager.getInstance();
            log.info("URL de la API: {}", config.getApiBaseUrl());
            
            // Probar conexión a la API
            ApiClient apiClient = ApiClient.getInstance();
            boolean conexionOk = apiClient.testConnection();
            
            if (!conexionOk) {
                log.warn("No se pudo conectar a la API. Verifica la URL en api-config.properties");
                AlertUtil.mostrarAdvertencia(
                    "Advertencia de Conexión",
                    "No se pudo conectar a la API en: " + config.getApiBaseUrl() + "\n\n" +
                    "Verifica que:\n" +
                    "1. La API esté ejecutándose\n" +
                    "2. La URL esté configurada correctamente en api-config.properties\n" +
                    "3. No haya problemas de red o firewall\n\n" +
                    "La aplicación iniciará pero algunas funciones pueden no estar disponibles."
                );
            } else {
                log.info("Conexión exitosa con la API");
            }
            
            // Configurar StageManager
            StageManager stageManager = StageManager.getInstance();
            stageManager.setPrimaryStage(primaryStage);
            
            // Configurar ventana principal
            primaryStage.setTitle("Lavadero Sepúlveda - CRM");
            primaryStage.setWidth(1200);
            primaryStage.setHeight(800);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(600);
            
            // Cargar vista principal
            stageManager.switchScene("/fxml/main.fxml", "Lavadero Sepúlveda - CRM");
            
            log.info("Aplicación iniciada correctamente");
            
        } catch (Exception e) {
            log.error("Error al iniciar la aplicación", e);
            AlertUtil.mostrarError("Error Fatal", 
                "No se pudo iniciar la aplicación: " + e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        log.info("Cerrando aplicación...");
        log.info("Aplicación cerrada");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

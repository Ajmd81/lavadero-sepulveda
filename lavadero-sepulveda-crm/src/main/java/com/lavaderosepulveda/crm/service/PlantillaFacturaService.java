package com.lavaderosepulveda.crm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lavaderosepulveda.crm.api.ApiClient;
import com.lavaderosepulveda.crm.config.ConfigManager;
import com.lavaderosepulveda.crm.model.PlantillaFacturaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Servicio para gestionar la configuración de plantilla de facturas
 */
public class PlantillaFacturaService {

    private static final Logger log = LoggerFactory.getLogger(PlantillaFacturaService.class);
    private static PlantillaFacturaService instance;

    private final ObjectMapper objectMapper;
    private final String configPath;
    private final ApiClient apiClient;
    private final String backendUrl;

    private PlantillaFacturaConfig configActual;

    private PlantillaFacturaService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Directorio de configuración
        String userHome = System.getProperty("user.home");
        String configDir = userHome + File.separator + ".lavadero-sepulveda";
        this.configPath = configDir + File.separator + "plantilla-factura.json";

        // Crear directorio si no existe
        try {
            Files.createDirectories(Paths.get(configDir));
        } catch (IOException e) {
            log.error("Error creando directorio de configuración", e);
        }

        this.apiClient = ApiClient.getInstance();
        this.backendUrl = ConfigManager.getInstance().getApiBaseUrl() + "/api/config/plantilla-factura";

        // Cargar configuración
        cargarConfiguracion();
    }

    public static PlantillaFacturaService getInstance() {
        if (instance == null) {
            instance = new PlantillaFacturaService();
        }
        return instance;
    }

    /**
     * Obtener configuración actual
     */
    public PlantillaFacturaConfig getConfiguracion() {
        if (configActual == null) {
            configActual = new PlantillaFacturaConfig();
        }
        return configActual;
    }

    /**
     * Guardar configuración localmente y en el backend
     */
    public boolean guardarConfiguracion(PlantillaFacturaConfig config) {
        this.configActual = config;

        // Guardar localmente
        boolean guardadoLocal = guardarLocal(config);

        // Sincronizar con backend
        boolean guardadoBackend = sincronizarConBackend(config);

        return guardadoLocal && guardadoBackend;
    }

    /**
     * Cargar configuración desde archivo local o crear por defecto
     */
    private void cargarConfiguracion() {
        File configFile = new File(configPath);

        if (configFile.exists()) {
            try {
                configActual = objectMapper.readValue(configFile, PlantillaFacturaConfig.class);
                log.info("Configuración de plantilla cargada desde: {}", configPath);
            } catch (IOException e) {
                log.error("Error cargando configuración, usando valores por defecto", e);
                configActual = new PlantillaFacturaConfig();
            }
        } else {
            log.info("No existe archivo de configuración, usando valores por defecto");
            configActual = new PlantillaFacturaConfig();
            guardarLocal(configActual);
        }
    }

    /**
     * Guardar configuración en archivo local
     */
    private boolean guardarLocal(PlantillaFacturaConfig config) {
        try {
            objectMapper.writeValue(new File(configPath), config);
            log.info("Configuración guardada en: {}", configPath);
            return true;
        } catch (IOException e) {
            log.error("Error guardando configuración local", e);
            return false;
        }
    }

    /**
     * Sincronizar configuración con el backend
     */
    private boolean sincronizarConBackend(PlantillaFacturaConfig config) {
        try {
            apiClient.post(backendUrl, config, PlantillaFacturaConfig.class);
            log.info("Configuración sincronizada con backend");
            return true;
        } catch (Exception e) {
            log.warn("No se pudo sincronizar con backend (puede no estar disponible): {}", e.getMessage());
            return false;
        }
    }

    /**
     * Cargar logo desde archivo y convertir a Base64
     */
    public String cargarLogoDesdeArchivo(File archivo) throws IOException {
        byte[] bytes = Files.readAllBytes(archivo.toPath());
        String base64 = Base64.getEncoder().encodeToString(bytes);

        // Detectar tipo de imagen
        String extension = getExtension(archivo.getName()).toLowerCase();
        String mimeType;
        switch (extension) {
            case "png":
                mimeType = "image/png";
                break;
            case "jpg":
            case "jpeg":
                mimeType = "image/jpeg";
                break;
            case "gif":
                mimeType = "image/gif";
                break;
            default:
                mimeType = "image/png";
        }

        return "data:" + mimeType + ";base64," + base64;
    }

    /**
     * Guardar logo Base64 a archivo temporal
     */
    public File guardarLogoTemporal(String logoBase64) throws IOException {
        if (logoBase64 == null || logoBase64.isEmpty()) {
            return null;
        }

        // Extraer datos del Base64
        String base64Data = logoBase64;
        String extension = "png";

        if (logoBase64.startsWith("data:")) {
            int commaIndex = logoBase64.indexOf(",");
            if (commaIndex > 0) {
                String header = logoBase64.substring(0, commaIndex);
                base64Data = logoBase64.substring(commaIndex + 1);

                if (header.contains("jpeg") || header.contains("jpg")) {
                    extension = "jpg";
                } else if (header.contains("gif")) {
                    extension = "gif";
                }
            }
        }

        byte[] bytes = Base64.getDecoder().decode(base64Data);

        Path tempFile = Files.createTempFile("logo_factura_", "." + extension);
        Files.write(tempFile, bytes);

        return tempFile.toFile();
    }

    /**
     * Obtener extensión de un nombre de archivo
     */
    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot + 1);
        }
        return "";
    }

    /**
     * Restablecer configuración a valores por defecto
     */
    public void restablecerPorDefecto() {
        configActual = new PlantillaFacturaConfig();
        guardarConfiguracion(configActual);
    }

    /**
     * Exportar configuración a archivo
     */
    public void exportarConfiguracion(File archivo) throws IOException {
        objectMapper.writeValue(archivo, configActual);
    }

    /**
     * Importar configuración desde archivo
     */
    public PlantillaFacturaConfig importarConfiguracion(File archivo) throws IOException {
        PlantillaFacturaConfig config = objectMapper.readValue(archivo, PlantillaFacturaConfig.class);
        guardarConfiguracion(config);
        return config;
    }
}
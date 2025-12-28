package com.lavaderosepulveda.crm.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class ConfigManager {
    
    private static ConfigManager instance;
    private final Properties properties;
    
    private ConfigManager() {
        properties = new Properties();
        cargarConfiguracion();
    }
    
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    private void cargarConfiguracion() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("api-config.properties")) {
            
            if (input == null) {
                log.warn("No se encontró api-config.properties, usando valores por defecto");
                cargarValoresPorDefecto();
                return;
            }
            
            properties.load(input);
            log.info("Configuración cargada exitosamente");
            
        } catch (IOException e) {
            log.error("Error al cargar configuración", e);
            cargarValoresPorDefecto();
        }
    }
    
    private void cargarValoresPorDefecto() {
        properties.setProperty("api.base.url", "http://localhost:8080");
        properties.setProperty("api.endpoints.clientes", "/api/clientes");
        properties.setProperty("api.endpoints.citas", "/api/citas");
        properties.setProperty("api.endpoints.servicios", "/api/servicios");
        properties.setProperty("api.endpoints.facturas", "/api/facturas");
        properties.setProperty("api.timeout.connect", "10");
        properties.setProperty("api.timeout.read", "30");
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(defaultValue)));
    }
    
    // Métodos de acceso rápido para configuración de API
    public String getApiBaseUrl() {
        return getProperty("api.base.url", "http://localhost:8080");
    }
    
    public String getClientesEndpoint() {
        return getApiBaseUrl() + getProperty("api.endpoints.clientes", "/api/clientes");
    }
    
    public String getCitasEndpoint() {
        return getApiBaseUrl() + getProperty("api.endpoints.citas", "/api/citas");
    }
    
    public String getServiciosEndpoint() {
        return getApiBaseUrl() + getProperty("api.endpoints.servicios", "/api/servicios");
    }
    
    public String getFacturasEndpoint() {
        return getApiBaseUrl() + getProperty("api.endpoints.facturas", "/api/facturas");
    }
    
    public int getConnectTimeout() {
        return getIntProperty("api.timeout.connect", 10);
    }
    
    public int getReadTimeout() {
        return getIntProperty("api.timeout.read", 30);
    }
    
    public int getWriteTimeout() {
        return getIntProperty("api.timeout.write", 30);
    }
    
    public boolean isAuthEnabled() {
        return getBooleanProperty("api.auth.enabled", false);
    }
    
    public String getAuthToken() {
        return getProperty("api.auth.token", "");
    }
}

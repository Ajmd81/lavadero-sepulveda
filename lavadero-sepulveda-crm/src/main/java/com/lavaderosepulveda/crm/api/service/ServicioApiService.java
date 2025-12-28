package com.lavaderosepulveda.crm.api.service;

import com.google.gson.reflect.TypeToken;
import com.lavaderosepulveda.crm.api.ApiClient;
import com.lavaderosepulveda.crm.api.dto.ServicioDTO;
import com.lavaderosepulveda.crm.config.ConfigManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ServicioApiService {
    
    private static ServicioApiService instance;
    private final ApiClient apiClient;
    private final ConfigManager config;
    private final String baseUrl;
    
    private ServicioApiService() {
        this.apiClient = ApiClient.getInstance();
        this.config = ConfigManager.getInstance();
        this.baseUrl = config.getServiciosEndpoint();
    }
    
    public static synchronized ServicioApiService getInstance() {
        if (instance == null) {
            instance = new ServicioApiService();
        }
        return instance;
    }
    
    /**
     * Obtener todos los servicios
     */
    public List<ServicioDTO> findAll() {
        try {
            String response = apiClient.getRaw(baseUrl);
            Type listType = new TypeToken<ArrayList<ServicioDTO>>(){}.getType();
            List<ServicioDTO> servicios = apiClient.getGson().fromJson(response, listType);
            log.info("Obtenidos {} servicios de la API", servicios != null ? servicios.size() : 0);
            return servicios != null ? servicios : new ArrayList<>();
        } catch (IOException e) {
            log.error("Error al obtener servicios", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener servicio por ID
     */
    public ServicioDTO findById(Long id) {
        try {
            ServicioDTO servicio = apiClient.get(baseUrl + "/" + id, ServicioDTO.class);
            log.info("Servicio obtenido: {}", id);
            return servicio;
        } catch (IOException e) {
            log.error("Error al obtener servicio: " + id, e);
            return null;
        }
    }
    
    /**
     * Obtener servicios activos
     */
    public List<ServicioDTO> findActivos() {
        try {
            String url = baseUrl + "/activos";
            String response = apiClient.getRaw(url);
            Type listType = new TypeToken<ArrayList<ServicioDTO>>(){}.getType();
            return apiClient.getGson().fromJson(response, listType);
        } catch (IOException e) {
            log.error("Error al obtener servicios activos", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener servicios por categoría
     */
    public List<ServicioDTO> findByCategoria(String categoria) {
        try {
            String url = baseUrl + "/categoria/" + categoria;
            String response = apiClient.getRaw(url);
            Type listType = new TypeToken<ArrayList<ServicioDTO>>(){}.getType();
            return apiClient.getGson().fromJson(response, listType);
        } catch (IOException e) {
            log.error("Error al obtener servicios por categoría", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Buscar servicios por nombre
     */
    public List<ServicioDTO> buscarPorNombre(String nombre) {
        try {
            String url = baseUrl + "/buscar?nombre=" + nombre;
            String response = apiClient.getRaw(url);
            Type listType = new TypeToken<ArrayList<ServicioDTO>>(){}.getType();
            return apiClient.getGson().fromJson(response, listType);
        } catch (IOException e) {
            log.error("Error al buscar servicios por nombre", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener todas las categorías
     */
    public List<String> findAllCategorias() {
        try {
            String url = baseUrl + "/categorias";
            String response = apiClient.getRaw(url);
            Type listType = new TypeToken<ArrayList<String>>(){}.getType();
            return apiClient.getGson().fromJson(response, listType);
        } catch (IOException e) {
            log.error("Error al obtener categorías", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Crear nuevo servicio
     */
    public ServicioDTO crear(ServicioDTO servicio) {
        try {
            ServicioDTO nuevoServicio = apiClient.post(baseUrl, servicio, ServicioDTO.class);
            log.info("Servicio creado: {}", nuevoServicio.getId());
            return nuevoServicio;
        } catch (IOException e) {
            log.error("Error al crear servicio", e);
            return null;
        }
    }
    
    /**
     * Actualizar servicio
     */
    public ServicioDTO actualizar(Long id, ServicioDTO servicio) {
        try {
            ServicioDTO servicioActualizado = apiClient.put(baseUrl + "/" + id, servicio, ServicioDTO.class);
            log.info("Servicio actualizado: {}", id);
            return servicioActualizado;
        } catch (IOException e) {
            log.error("Error al actualizar servicio: " + id, e);
            return null;
        }
    }
    
    /**
     * Eliminar servicio
     */
    public boolean eliminar(Long id) {
        try {
            apiClient.delete(baseUrl + "/" + id);
            log.info("Servicio eliminado: {}", id);
            return true;
        } catch (IOException e) {
            log.error("Error al eliminar servicio: " + id, e);
            return false;
        }
    }
}

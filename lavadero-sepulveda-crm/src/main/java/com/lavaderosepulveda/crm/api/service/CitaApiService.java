package com.lavaderosepulveda.crm.api.service;

import com.google.gson.reflect.TypeToken;
import com.lavaderosepulveda.crm.api.ApiClient;
import com.lavaderosepulveda.crm.api.dto.CitaDTO;
import com.lavaderosepulveda.crm.config.ConfigManager;
import com.lavaderosepulveda.crm.mapper.CitaMapper;
import com.lavaderosepulveda.crm.model.EstadoCita;
import com.lavaderosepulveda.crm.api.dto.CitaApiResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CitaApiService {

    private static final Logger log = LoggerFactory.getLogger(CitaApiService.class);

    private static CitaApiService instance;
    private final ApiClient apiClient;
    private final ConfigManager config;
    private final String baseUrl;
    private final CitaMapper citaMapper;

    private CitaApiService() {
        this.apiClient = ApiClient.getInstance();
        this.config = ConfigManager.getInstance();
        this.baseUrl = config.getCitasEndpoint();
        this.citaMapper = new CitaMapper();
    }

    public static synchronized CitaApiService getInstance() {
        if (instance == null) {
            instance = new CitaApiService();
        }
        return instance;
    }

    /**
     * Obtener todas las citas
     */
    public List<CitaDTO> findAll() {
        try {
            String response = apiClient.getRaw(baseUrl);
            Type listType = new TypeToken<ArrayList<CitaApiResponseDTO>>(){}.getType();
            List<CitaApiResponseDTO> apiResponse = apiClient.getGson().fromJson(response, listType);
            
            List<CitaDTO> citas = citaMapper.toDTOList(apiResponse);
            log.info("Obtenidas {} citas de la API", citas.size());
            return citas;
        } catch (IOException e) {
            log.error("Error al obtener citas", e);
            return new ArrayList<>();
        }
    }

    /**
     * Crear nueva cita
     */
    public CitaDTO create(CitaDTO cita) throws IOException {
        log.info("Creando nueva cita en la API...");
        
        CitaDTO citaCreada = apiClient.post(baseUrl, cita, CitaDTO.class);
        log.info("Cita creada con ID: {}", citaCreada.getId());
        
        return citaCreada;
    }

    /**
     * Actualizar cita existente
     */
    public CitaDTO update(Long id, CitaDTO cita) throws IOException {
        log.info("Actualizando cita ID: {}", id);
        
        String url = baseUrl + "/" + id;
        CitaDTO citaActualizada = apiClient.put(url, cita, CitaDTO.class);
        log.info("Cita actualizada: {}", id);
        
        return citaActualizada;
    }

    /**
     * Eliminar cita
     */
    public void delete(Long id) throws IOException {
        log.info("Eliminando cita ID: {}", id);
        
        String url = baseUrl + "/" + id;
        apiClient.delete(url);
        log.info("Cita eliminada: {}", id);
    }

    /**
     * Obtener cita por ID
     */
    public CitaDTO findById(Long id) {
        try {
            String url = baseUrl + "/" + id;
            String response = apiClient.getRaw(url);
            CitaDTO cita = apiClient.getGson().fromJson(response, CitaDTO.class);
            return cita;
        } catch (IOException e) {
            log.error("Error al obtener cita por ID: " + id, e);
            return null;
        }
    }

    /**
     * Filtrar citas por fecha
     */
    public List<CitaDTO> findByFecha(LocalDate fecha) {
        List<CitaDTO> todasLasCitas = findAll();
        return todasLasCitas.stream()
            .filter(cita -> cita.getFechaHora() != null)
            .filter(cita -> cita.getFechaHora().toLocalDate().equals(fecha))
            .collect(Collectors.toList());
    }

    /**
     * Filtrar citas por estado
     */
    public List<CitaDTO> findByEstado(EstadoCita estado) {
        List<CitaDTO> todasLasCitas = findAll();
        return todasLasCitas.stream()
            .filter(cita -> cita.getEstado() == estado)
            .collect(Collectors.toList());
    }

    /**
     * Obtener citas de hoy
     */
    public List<CitaDTO> findCitasHoy() {
        return findByFecha(LocalDate.now());
    }

    /**
     * Obtener citas pendientes
     */
    public List<CitaDTO> findCitasPendientes() {
        return findByEstado(EstadoCita.PENDIENTE);
    }

    /**
     * Obtener citas confirmadas
     */
    public List<CitaDTO> findCitasConfirmadas() {
        return findByEstado(EstadoCita.CONFIRMADA);
    }
}
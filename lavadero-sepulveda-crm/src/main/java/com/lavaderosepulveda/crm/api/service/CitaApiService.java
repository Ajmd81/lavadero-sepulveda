package com.lavaderosepulveda.crm.api.service;

import com.google.gson.reflect.TypeToken;
import com.lavaderosepulveda.crm.api.ApiClient;
import com.lavaderosepulveda.crm.api.dto.CitaDTO;
import com.lavaderosepulveda.crm.config.ConfigManager;
import com.lavaderosepulveda.crm.model.EstadoCita;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CitaApiService {
    
    private static CitaApiService instance;
    private final ApiClient apiClient;
    private final ConfigManager config;
    private final String baseUrl;
    
    private CitaApiService() {
        this.apiClient = ApiClient.getInstance();
        this.config = ConfigManager.getInstance();
        this.baseUrl = config.getCitasEndpoint();
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
            Type listType = new TypeToken<ArrayList<CitaDTO>>(){}.getType();
            List<CitaDTO> citas = apiClient.getGson().fromJson(response, listType);
            log.info("Obtenidas {} citas de la API", citas != null ? citas.size() : 0);
            return citas != null ? citas : new ArrayList<>();
        } catch (IOException e) {
            log.error("Error al obtener citas", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener cita por ID
     */
    public CitaDTO findById(Long id) {
        try {
            CitaDTO cita = apiClient.get(baseUrl + "/" + id, CitaDTO.class);
            log.info("Cita obtenida: {}", id);
            return cita;
        } catch (IOException e) {
            log.error("Error al obtener cita: " + id, e);
            return null;
        }
    }
    
    /**
     * Obtener citas por fecha (filtrado local)
     */
    public List<CitaDTO> findByFecha(LocalDate fecha) {
        try {
            List<CitaDTO> todasLasCitas = findAll();
            return todasLasCitas.stream()
                .filter(cita -> cita.getFechaHora() != null && 
                               cita.getFechaHora().toLocalDate().equals(fecha))
                .toList();
        } catch (Exception e) {
            log.error("Error al obtener citas por fecha", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener citas por rango de fechas (filtrado local)
     */
    public List<CitaDTO> findByFechaRange(LocalDate inicio, LocalDate fin) {
        try {
            List<CitaDTO> todasLasCitas = findAll();
            return todasLasCitas.stream()
                .filter(cita -> {
                    if (cita.getFechaHora() == null) return false;
                    LocalDate fecha = cita.getFechaHora().toLocalDate();
                    return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
                })
                .toList();
        } catch (Exception e) {
            log.error("Error al obtener citas por rango", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener citas por cliente (filtrado local)
     */
    public List<CitaDTO> findByClienteId(Long clienteId) {
        try {
            List<CitaDTO> todasLasCitas = findAll();
            return todasLasCitas.stream()
                .filter(cita -> cita.getClienteId() != null && cita.getClienteId().equals(clienteId))
                .toList();
        } catch (Exception e) {
            log.error("Error al obtener citas por cliente", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener citas por estado (filtrado local)
     */
    public List<CitaDTO> findByEstado(EstadoCita estado) {
        try {
            List<CitaDTO> todasLasCitas = findAll();
            return todasLasCitas.stream()
                .filter(cita -> cita.getEstado() == estado)
                .toList();
        } catch (Exception e) {
            log.error("Error al obtener citas por estado", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener citas de hoy (filtrado local)
     */
    public List<CitaDTO> findCitasHoy() {
        return findByFecha(LocalDate.now());
    }
    
    /**
     * Obtener citas pendientes (filtrado local)
     */
    public List<CitaDTO> findCitasPendientes() {
        try {
            List<CitaDTO> todasLasCitas = findAll();
            LocalDate hoy = LocalDate.now();
            return todasLasCitas.stream()
                .filter(cita -> cita.getEstado() == EstadoCita.PENDIENTE || 
                               cita.getEstado() == EstadoCita.CONFIRMADA)
                .filter(cita -> cita.getFechaHora() != null && 
                               !cita.getFechaHora().toLocalDate().isBefore(hoy))
                .toList();
        } catch (Exception e) {
            log.error("Error al obtener citas pendientes", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener citas próxima semana (filtrado local)
     */
    public List<CitaDTO> findCitasProximaSemana() {
        LocalDate hoy = LocalDate.now();
        LocalDate finSemana = hoy.plusDays(7);
        return findByFechaRange(hoy, finSemana);
    }
    
    /**
     * Obtener citas no facturadas (filtrado local)
     */
    public List<CitaDTO> findCitasNoFacturadas() {
        try {
            List<CitaDTO> todasLasCitas = findAll();
            return todasLasCitas.stream()
                .filter(cita -> cita.getEstado() == EstadoCita.COMPLETADA)
                .filter(cita -> cita.getFacturada() == null || !cita.getFacturada())
                .toList();
        } catch (Exception e) {
            log.error("Error al obtener citas no facturadas", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Crear nueva cita
     */
    public CitaDTO crear(CitaDTO cita) {
        try {
            CitaDTO nuevaCita = apiClient.post(baseUrl, cita, CitaDTO.class);
            log.info("Cita creada: {}", nuevaCita.getId());
            return nuevaCita;
        } catch (IOException e) {
            log.error("Error al crear cita", e);
            return null;
        }
    }
    
    /**
     * Actualizar cita
     */
    public CitaDTO actualizar(Long id, CitaDTO cita) {
        try {
            CitaDTO citaActualizada = apiClient.put(baseUrl + "/" + id, cita, CitaDTO.class);
            log.info("Cita actualizada: {}", id);
            return citaActualizada;
        } catch (IOException e) {
            log.error("Error al actualizar cita: " + id, e);
            return null;
        }
    }
    
    /**
     * Cambiar estado de cita
     */
    public CitaDTO cambiarEstado(Long id, EstadoCita nuevoEstado) {
        try {
            String url = baseUrl + "/" + id + "/estado/" + nuevoEstado.name();
            CitaDTO citaActualizada = apiClient.put(url, null, CitaDTO.class);
            log.info("Estado de cita cambiado: {} a {}", id, nuevoEstado);
            return citaActualizada;
        } catch (IOException e) {
            log.error("Error al cambiar estado de cita: " + id, e);
            return null;
        }
    }
    
    /**
     * Cancelar cita
     */
    public CitaDTO cancelar(Long id, String motivo) {
        try {
            String url = baseUrl + "/" + id + "/cancelar";
            CitaDTO citaCancelada = apiClient.post(url, motivo, CitaDTO.class);
            log.info("Cita cancelada: {}", id);
            return citaCancelada;
        } catch (IOException e) {
            log.error("Error al cancelar cita: " + id, e);
            return null;
        }
    }
    
    /**
     * Eliminar cita
     */
    public boolean eliminar(Long id) {
        try {
            apiClient.delete(baseUrl + "/" + id);
            log.info("Cita eliminada: {}", id);
            return true;
        } catch (IOException e) {
            log.error("Error al eliminar cita: " + id, e);
            return false;
        }
    }
    
    /**
     * Contar citas por estado (calculado localmente)
     */
    public long countByEstado(EstadoCita estado) {
        try {
            List<CitaDTO> todasLasCitas = findAll();
            return todasLasCitas.stream()
                .filter(cita -> cita.getEstado() == estado)
                .count();
        } catch (Exception e) {
            log.error("Error al contar citas por estado", e);
            return 0;
        }
    }
    
    /**
     * Contar citas de hoy (calculado localmente)
     */
    public long countCitasHoy() {
        try {
            LocalDate hoy = LocalDate.now();
            List<CitaDTO> citasHoy = findByFecha(hoy);
            return citasHoy.size();
        } catch (Exception e) {
            log.error("Error al contar citas de hoy", e);
            return 0;
        }
    }
}

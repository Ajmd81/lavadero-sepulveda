package com.lavaderosepulveda.crm.api.service;

import com.google.gson.reflect.TypeToken;
import com.lavaderosepulveda.crm.api.client.ApiClient;
import com.lavaderosepulveda.crm.model.dto.CitaDTO;
import com.lavaderosepulveda.crm.model.dto.CitaApiResponseDTO;
import com.lavaderosepulveda.crm.config.ConfigManager;
import com.lavaderosepulveda.crm.mapper.CitaMapper;
import com.lavaderosepulveda.crm.model.enums.EstadoCita;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CitaApiService {

    private static final Logger log = LoggerFactory.getLogger(CitaApiService.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

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
     * Crear nueva cita - MODIFICADO para usar CitaApiResponseDTO
     */
    public CitaDTO create(CitaDTO cita) throws IOException {
        log.info("Creando nueva cita en la API...");

        // Convertir CitaDTO a CitaApiResponseDTO (formato que espera el backend)
        CitaApiResponseDTO request = convertirACitaApiRequest(cita);

        log.info("JSON a enviar: {}", apiClient.getGson().toJson(request));

        // Enviar el request en el formato correcto
        String response = apiClient.postRaw(baseUrl, request);

        // La respuesta viene en formato CitaApiResponseDTO
        CitaApiResponseDTO apiResponse = apiClient.getGson().fromJson(response, CitaApiResponseDTO.class);

        // Convertir la respuesta a CitaDTO usando el mapper
        CitaDTO citaCreada = citaMapper.toDTO(apiResponse);
        log.info("Cita creada con ID: {}", citaCreada.getId());

        return citaCreada;
    }

    /**
     * Convierte CitaDTO (formato CRM) a CitaApiResponseDTO (formato backend)
     */
    private CitaApiResponseDTO convertirACitaApiRequest(CitaDTO cita) {
        CitaApiResponseDTO request = new CitaApiResponseDTO();

        // Datos del cliente (directos, no como objeto)
        if (cita.getCliente() != null) {
            request.setNombre(cita.getCliente().getNombre());
            request.setEmail(cita.getCliente().getEmail());
            request.setTelefono(cita.getCliente().getTelefono());
        }

        // Fecha y hora SEPARADAS como Strings
        if (cita.getFechaHora() != null) {
            request.setFecha(cita.getFechaHora().format(FORMATO_FECHA));
            request.setHora(cita.getFechaHora().format(FORMATO_HORA));
        }

        // Tipo de lavado como String (primer servicio)
        if (cita.getServicios() != null && !cita.getServicios().isEmpty()) {
            request.setTipoLavado(cita.getServicios().get(0).getNombre());
        }

        // Estado como String
        if (cita.getEstado() != null) {
            request.setEstado(cita.getEstado().name());
        } else {
            request.setEstado("PENDIENTE");
        }

        // Modelo de vehículo
        request.setModeloVehiculo(cita.getMarcaModelo());

        // Observaciones
        request.setObservaciones(cita.getObservaciones());

        // Pago adelantado (por defecto false)
        request.setPagoAdelantado(false);

        return request;
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
            CitaApiResponseDTO apiResponse = apiClient.getGson().fromJson(response, CitaApiResponseDTO.class);
            return citaMapper.toDTO(apiResponse);
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

    /**
     * Cambiar estado de una cita usando el endpoint específico
     */
    public CitaDTO cambiarEstado(Long id, EstadoCita nuevoEstado) throws IOException {
        String estadoStr = nuevoEstado.name();
        String url = baseUrl + "/" + id + "/estado/" + estadoStr;

        log.info("Cambiando estado de cita {} a {}", id, estadoStr);

        // PUT sin body - el estado va en la URL
        CitaDTO citaActualizada = apiClient.put(url, null, CitaDTO.class);
        log.info("Estado de cita {} cambiado exitosamente a {}", id, estadoStr);

        return citaActualizada;
    }

    /**
     * Obtener horarios disponibles para una fecha desde la API
     */
    public List<String> obtenerHorariosDisponibles(LocalDate fecha) {
        try {
            // Formato dd/MM/yyyy que espera el backend
            String fechaStr = fecha.format(FORMATO_FECHA);
            String url = baseUrl + "/horarios-disponibles?fecha=" + fechaStr;

            log.info("Obteniendo horarios disponibles para: {}", fechaStr);
            String response = apiClient.getRaw(url);

            Type listType = new TypeToken<ArrayList<String>>(){}.getType();
            List<String> horarios = apiClient.getGson().fromJson(response, listType);

            log.info("Horarios disponibles obtenidos: {}", horarios != null ? horarios.size() : 0);
            return horarios != null ? horarios : new ArrayList<>();
        } catch (IOException e) {
            log.error("Error al obtener horarios disponibles para: " + fecha, e);
            return new ArrayList<>();
        }
    }
}
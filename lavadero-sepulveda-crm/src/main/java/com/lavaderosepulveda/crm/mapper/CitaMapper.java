package com.lavaderosepulveda.crm.mapper;

import com.lavaderosepulveda.crm.model.dto.CitaApiResponseDTO;
import com.lavaderosepulveda.crm.model.dto.CitaDTO;
import com.lavaderosepulveda.crm.model.dto.ClienteDTO;
import com.lavaderosepulveda.crm.model.dto.ServicioDTO;
import com.lavaderosepulveda.crm.model.enums.EstadoCita;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper que convierte CitaApiResponseDTO (JSON de la API) a CitaDTO (usado por
 * el CRM)
 */
public class CitaMapper {

    private static final Logger log = LoggerFactory.getLogger(CitaMapper.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Mapa de precios por tipo de lavado (debe coincidir con TipoLavado del
    // backend)
    private static final Map<String, Double> PRECIOS_LAVADO = new HashMap<>();

    static {
        PRECIOS_LAVADO.put("LAVADO_COMPLETO_TURISMO", 23.0);
        PRECIOS_LAVADO.put("LAVADO_INTERIOR_TURISMO", 16.0);
        PRECIOS_LAVADO.put("LAVADO_EXTERIOR_TURISMO", 12.0);
        PRECIOS_LAVADO.put("LAVADO_COMPLETO_RANCHERA", 26.0);
        PRECIOS_LAVADO.put("LAVADO_INTERIOR_RANCHERA", 18.0);
        PRECIOS_LAVADO.put("LAVADO_EXTERIOR_RANCHERA", 13.0);
        PRECIOS_LAVADO.put("LAVADO_COMPLETO_MONOVOLUMEN", 28.0);
        PRECIOS_LAVADO.put("LAVADO_INTERIOR_MONOVOLUMEN", 19.0);
        PRECIOS_LAVADO.put("LAVADO_EXTERIOR_MONOVOLUMEN", 14.0);
        PRECIOS_LAVADO.put("LAVADO_COMPLETO_TODOTERRENO", 31.0);
        PRECIOS_LAVADO.put("LAVADO_INTERIOR_TODOTERRENO", 20.0);
        PRECIOS_LAVADO.put("LAVADO_EXTERIOR_TODOTERRENO", 16.0);
        PRECIOS_LAVADO.put("LAVADO_COMPLETO_FURGONETA_PEQUEÑA", 30.0);
        PRECIOS_LAVADO.put("LAVADO_INTERIOR_FURGONETA_PEQUEÑA", 20.0);
        PRECIOS_LAVADO.put("LAVADO_EXTERIOR_FURGONETA_PEQUEÑA", 15.0);
        PRECIOS_LAVADO.put("LAVADO_COMPLETO_FURGONETA_GRANDE", 35.0);
        PRECIOS_LAVADO.put("LAVADO_INTERIOR_FURGONETA_GRANDE", 25.0);
        PRECIOS_LAVADO.put("LAVADO_EXTERIOR_FURGONETA_GRANDE", 20.0);
        PRECIOS_LAVADO.put("TRATAMIENTO_OZONO", 15.0);
        PRECIOS_LAVADO.put("ENCERADO", 25.0);
        PRECIOS_LAVADO.put("TAPICERIA_SIN_DESMONTAR", 100.0);
        PRECIOS_LAVADO.put("TAPICERIA_DESMONTANDO", 150.0);
    }

    /**
     * Convierte CitaApiResponseDTO a CitaDTO
     */
    public static CitaDTO toDTO(CitaApiResponseDTO apiResponse) {
        if (apiResponse == null) {
            return null;
        }

        CitaDTO cita = new CitaDTO();

        try {
            // ID
            cita.setId(apiResponse.getId());

            // Fecha y hora combinadas
            LocalDateTime fechaHora = combinarFechaHora(apiResponse.getFecha(), apiResponse.getHora());
            cita.setFechaHora(fechaHora);

            // Cliente (crear objeto a partir de campos separados)
            ClienteDTO cliente = crearClienteDTO(apiResponse);
            cita.setCliente(cliente);

            // Estado
            EstadoCita estado = convertirEstado(apiResponse.getEstado());
            cita.setEstado(estado);

            // Servicios (crear a partir del tipoLavado)
            List<ServicioDTO> servicios = crearServiciosDTO(apiResponse.getTipoLavado());
            cita.setServicios(servicios);

            // Otros campos
            cita.setMarcaModelo(apiResponse.getModeloVehiculo());
            cita.setObservaciones(apiResponse.getObservaciones());
            cita.setFacturada(apiResponse.getPagoAdelantado());

            log.debug("Cita mapeada correctamente: ID {}", cita.getId());

        } catch (Exception e) {
            log.error("Error al mapear cita: {}", e.getMessage(), e);
        }

        return cita;
    }

    /**
     * Convierte una lista de CitaApiResponseDTO a CitaDTO
     */
    public static List<CitaDTO> toDTOList(List<CitaApiResponseDTO> apiResponseList) {
        List<CitaDTO> result = new ArrayList<>();

        if (apiResponseList != null) {
            for (CitaApiResponseDTO apiResponse : apiResponseList) {
                CitaDTO cita = toDTO(apiResponse);
                if (cita != null) {
                    result.add(cita);
                }
            }
        }

        return result;
    }

    /**
     * Combina fecha (dd/MM/yyyy) y hora (HH:mm:ss) en LocalDateTime
     */
    private static LocalDateTime combinarFechaHora(String fechaStr, String horaStr) {
        try {
            LocalDate fecha = LocalDate.parse(fechaStr, DATE_FORMATTER);
            LocalTime hora = LocalTime.parse(horaStr, TIME_FORMATTER);
            return LocalDateTime.of(fecha, hora);
        } catch (Exception e) {
            log.error("Error al parsear fecha '{}' y hora '{}': {}", fechaStr, horaStr, e.getMessage());
            return null;
        }
    }

    /**
     * Crea un ClienteDTO a partir de los campos separados de la API
     */
    private static ClienteDTO crearClienteDTO(CitaApiResponseDTO apiResponse) {
        ClienteDTO cliente = new ClienteDTO();

        // Separar nombre y apellidos
        String nombreCompleto = apiResponse.getNombre();
        if (nombreCompleto != null && nombreCompleto.contains(" ")) {
            String[] partes = nombreCompleto.split(" ", 2);
            cliente.setNombre(partes[0]);
            cliente.setApellidos(partes[1]);
        } else {
            cliente.setNombre(nombreCompleto);
            cliente.setApellidos("");
        }

        cliente.setTelefono(apiResponse.getTelefono());
        cliente.setEmail(apiResponse.getEmail());

        return cliente;
    }

    /**
     * Convierte el string del estado a enum EstadoCita
     */
    private static EstadoCita convertirEstado(String estadoStr) {
        if (estadoStr == null || estadoStr.isEmpty()) {
            return EstadoCita.PENDIENTE;
        }

        try {
            return EstadoCita.valueOf(estadoStr);
        } catch (IllegalArgumentException e) {
            log.warn("Estado desconocido: {}, usando PENDIENTE", estadoStr);
            return EstadoCita.PENDIENTE;
        }
    }

    /**
 * Crea una lista de ServicioDTO a partir del tipoLavado
 */
private static List<ServicioDTO> crearServiciosDTO(String tipoLavado) {
    List<ServicioDTO> servicios = new ArrayList<>();
    
    if (tipoLavado != null && !tipoLavado.isEmpty()) {
        ServicioDTO servicio = new ServicioDTO();
        servicio.setId(0L);
        servicio.setNombre(formatearNombreLavado(tipoLavado));
        
        // Obtener precio CON IVA del mapa
        Double precioConIva = PRECIOS_LAVADO.getOrDefault(tipoLavado, 0.0);
        
        // Calcular precio BASE (sin IVA)
        // Si precio con IVA es 23.0€, precio base = 23.0 / 1.21 = 19.01€
        Double precioBase = precioConIva / 1.21;
        
        servicio.setPrecio(precioBase);
        servicio.setIva(21.0); // IVA del 21%
        
        servicios.add(servicio);
    }
    
    return servicios;
}

    /**
     * Formatea el nombre del tipo de lavado para mostrar
     */
    private static String formatearNombreLavado(String tipoLavado) {
        if (tipoLavado == null || tipoLavado.isEmpty()) {
            return "";
        }

        // Convertir de LAVADO_COMPLETO_TURISMO a "Lavado Completo Turismo"
        String[] palabras = tipoLavado.toLowerCase().split("_");
        StringBuilder resultado = new StringBuilder();

        for (String palabra : palabras) {
            if (palabra.length() > 0) {
                // Capitalizar primera letra de cada palabra
                resultado.append(Character.toUpperCase(palabra.charAt(0)));
                if (palabra.length() > 1) {
                    resultado.append(palabra.substring(1));
                }
                resultado.append(" ");
            }
        }

        return resultado.toString().trim();
    }
}
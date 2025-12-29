package com.lavaderosepulveda.crm.mapper;

import com.lavaderosepulveda.crm.api.dto.CitaApiResponseDTO;
import com.lavaderosepulveda.crm.api.dto.CitaDTO;
import com.lavaderosepulveda.crm.api.dto.ClienteDTO;
import com.lavaderosepulveda.crm.api.dto.ServicioDTO;
import com.lavaderosepulveda.crm.model.EstadoCita;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CitaMapper {

    private static final Logger log = LoggerFactory.getLogger(CitaMapper.class);

    private static final DateTimeFormatter FECHA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Mapa de precios con IVA incluido (21%)
    private static final Map<String, Double> PRECIOS_LAVADO = new HashMap<>();

    static {
        // Turismo
        PRECIOS_LAVADO.put("LAVADO_COMPLETO_TURISMO", 23.0);
        PRECIOS_LAVADO.put("LAVADO_INTERIOR_TURISMO", 16.0);
        PRECIOS_LAVADO.put("LAVADO_EXTERIOR_TURISMO", 12.0);

        // Ranchera
        PRECIOS_LAVADO.put("LAVADO_COMPLETO_RANCHERA", 26.0);
        PRECIOS_LAVADO.put("LAVADO_INTERIOR_RANCHERA", 18.0);
        PRECIOS_LAVADO.put("LAVADO_EXTERIOR_RANCHERA", 13.0);

        // Monovolumen
        PRECIOS_LAVADO.put("LAVADO_COMPLETO_MONOVOLUMEN", 28.0);
        PRECIOS_LAVADO.put("LAVADO_INTERIOR_MONOVOLUMEN", 19.0);
        PRECIOS_LAVADO.put("LAVADO_EXTERIOR_MONOVOLUMEN", 14.0);

        // Todoterreno
        PRECIOS_LAVADO.put("LAVADO_COMPLETO_TODOTERRENO", 31.0);
        PRECIOS_LAVADO.put("LAVADO_INTERIOR_TODOTERRENO", 20.0);
        PRECIOS_LAVADO.put("LAVADO_EXTERIOR_TODOTERRENO", 16.0);

        // Furgoneta pequeña
        PRECIOS_LAVADO.put("LAVADO_COMPLETO_FURGONETA_PEQUEÑA", 30.0);
        PRECIOS_LAVADO.put("LAVADO_INTERIOR_FURGONETA_PEQUEÑA", 20.0);
        PRECIOS_LAVADO.put("LAVADO_EXTERIOR_FURGONETA_PEQUEÑA", 15.0);

        // Furgoneta grande
        PRECIOS_LAVADO.put("LAVADO_COMPLETO_FURGONETA_GRANDE", 35.0);
        PRECIOS_LAVADO.put("LAVADO_INTERIOR_FURGONETA_GRANDE", 25.0);
        PRECIOS_LAVADO.put("LAVADO_EXTERIOR_FURGONETA_GRANDE", 20.0);

        // Adicionales
        PRECIOS_LAVADO.put("TRATAMIENTO_OZONO", 15.0);
        PRECIOS_LAVADO.put("ENCERADO", 25.0);
        PRECIOS_LAVADO.put("TAPICERIA_SIN_DESMONTAR", 100.0);
        PRECIOS_LAVADO.put("TAPICERIA_DESMONTANDO", 150.0);
    }

    /**
     * Convertir de CitaApiResponseDTO a CitaDTO
     */
    public CitaDTO toDTO(CitaApiResponseDTO apiResponse) {
        if (apiResponse == null) {
            return null;
        }

        CitaDTO dto = new CitaDTO();

        try {
            // ID
            dto.setId(apiResponse.getId());

            // Fecha y hora
            dto.setFechaHora(combinarFechaHora(apiResponse.getFecha(), apiResponse.getHora()));

            // Cliente
            dto.setCliente(crearClienteDTO(apiResponse));

            // Servicios
            dto.setServicios(crearServiciosDTO(apiResponse.getTipoLavado()));

            // Estado
            dto.setEstado(convertirEstado(apiResponse.getEstado()));

            // NOTA: No se establece importeTotal porque es un método calculado
            // El importe total se calcula automáticamente sumando los servicios:
            // dto.getImporteTotal() =
            // servicios.stream().mapToDouble(ServicioDTO::getPrecioConIva).sum()

        } catch (Exception e) {
            log.error("Error al mapear CitaApiResponseDTO a CitaDTO", e);
        }

        return dto;
    }

    /**
     * Convertir lista de CitaApiResponseDTO a lista de CitaDTO
     */
    public List<CitaDTO> toDTOList(List<CitaApiResponseDTO> apiResponseList) {
        if (apiResponseList == null) {
            return new ArrayList<>();
        }

        return apiResponseList.stream()
                .map(this::toDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Combinar fecha y hora en LocalDateTime
     */
    private LocalDateTime combinarFechaHora(String fechaStr, String horaStr) {
        try {
            LocalDate fecha = LocalDate.parse(fechaStr, FECHA_FORMATTER);
            LocalTime hora = LocalTime.parse(horaStr, HORA_FORMATTER);
            return LocalDateTime.of(fecha, hora);
        } catch (Exception e) {
            log.error("Error al parsear fecha/hora: {} {}", fechaStr, horaStr, e);
            return LocalDateTime.now();
        }
    }

    /**
     * Crear ClienteDTO desde los datos de la API
     */
    private ClienteDTO crearClienteDTO(CitaApiResponseDTO apiResponse) {
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
        cliente.setVehiculoHabitual(apiResponse.getModeloVehiculo());

        return cliente;
    }

    /**
     * Crear lista de ServicioDTO desde tipo de lavado
     */
    private List<ServicioDTO> crearServiciosDTO(String tipoLavado) {
        ServicioDTO servicio = new ServicioDTO();

        // Nombre formateado
        servicio.setNombre(formatearNombreLavado(tipoLavado));

        // Precio con IVA (según tabla de precios)
        Double precioConIva = PRECIOS_LAVADO.getOrDefault(tipoLavado, 0.0);

        // Calcular precio base (sin IVA): precioConIva / 1.21
        Double precioBase = precioConIva / 1.21;
        servicio.setPrecio(precioBase);

        // IVA como porcentaje (21.0 = 21%)
        servicio.setIva(21.0);

        // Activo por defecto
        servicio.setActivo(true);

        return Arrays.asList(servicio);
    }

    /**
     * Formatear nombre de lavado: "LAVADO_COMPLETO_TURISMO" → "Lavado Completo
     * Turismo"
     */
    private String formatearNombreLavado(String tipoLavado) {
        if (tipoLavado == null || tipoLavado.isEmpty()) {
            return "";
        }

        String[] palabras = tipoLavado.split("_");
        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < palabras.length; i++) {
            String palabra = palabras[i];
            if (!palabra.isEmpty()) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)));
                resultado.append(palabra.substring(1).toLowerCase());

                if (i < palabras.length - 1) {
                    resultado.append(" ");
                }
            }
        }

        return resultado.toString();
    }

    /**
     * Convertir String de estado a enum EstadoCita
     */
    private EstadoCita convertirEstado(String estado) {
        if (estado == null) {
            return EstadoCita.PENDIENTE;
        }

        try {
            return EstadoCita.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Estado desconocido: {}, usando PENDIENTE por defecto", estado);
            return EstadoCita.PENDIENTE;
        }
    }
}
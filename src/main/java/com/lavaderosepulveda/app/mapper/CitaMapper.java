package com.lavaderosepulveda.app.mapper;

import com.lavaderosepulveda.app.dto.CitaDTO;
import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.enums.EstadoCita;
import com.lavaderosepulveda.app.util.DateTimeFormatUtils;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Cita (Entity) y CitaDTO
 * Usa DateTimeFormatUtils para formateo consistente de fechas
 */
@Component
public class CitaMapper {

    /**
     * Convierte una entidad Cita a CitaDTO
     */
    public CitaDTO toDTO(Cita cita) {
        if (cita == null) {
            return null;
        }

        CitaDTO dto = new CitaDTO();
        dto.setId(cita.getId());
        dto.setNombre(cita.getNombre());
        dto.setEmail(cita.getEmail());
        dto.setTelefono(cita.getTelefono());
        dto.setModeloVehiculo(cita.getModeloVehiculo());
        dto.setTipoLavado(cita.getTipoLavado());
        dto.setFecha(cita.getFecha());
        dto.setHora(cita.getHora());

        // Campos adicionales
        dto.setEstado(cita.getEstado() != null ? cita.getEstado().name() : "PENDIENTE");
        dto.setPagoAdelantado(cita.isPagoAdelantado());
        dto.setReferenciaPago(cita.getReferenciaPago());
        dto.setObservaciones(cita.getObservaciones());

        return dto;
    }

    /**
     * Convierte un CitaDTO a entidad Cita
     */
    public Cita toEntity(CitaDTO dto) {
        if (dto == null) {
            return null;
        }

        Cita cita = new Cita();
        cita.setId(dto.getId());
        cita.setNombre(dto.getNombre());
        cita.setEmail(dto.getEmail());
        cita.setTelefono(dto.getTelefono());
        cita.setModeloVehiculo(dto.getModeloVehiculo());
        cita.setTipoLavado(dto.getTipoLavado());
        cita.setFecha(dto.getFecha());
        cita.setHora(dto.getHora());

        // Campos adicionales
        if (dto.getEstado() != null) {
            try {
                cita.setEstado(EstadoCita.valueOf(dto.getEstado()));
            } catch (IllegalArgumentException e) {
                cita.setEstado(EstadoCita.PENDIENTE);
            }
        } else {
            cita.setEstado(EstadoCita.PENDIENTE);
        }

        cita.setPagoAdelantado(dto.getPagoAdelantado() != null ? dto.getPagoAdelantado() : false);
        cita.setReferenciaPago(dto.getReferenciaPago());
        cita.setObservaciones(dto.getObservaciones());

        return cita;
    }

    /**
     * Actualiza una entidad Cita existente con datos del DTO
     * Preserva el ID y otros campos que no deben ser modificados
     */
    public void updateEntityFromDTO(CitaDTO dto, Cita cita) {
        if (dto == null || cita == null) {
            return;
        }

        // Actualizar solo campos modificables
        cita.setNombre(dto.getNombre());
        cita.setEmail(dto.getEmail());
        cita.setTelefono(dto.getTelefono());
        cita.setModeloVehiculo(dto.getModeloVehiculo());
        cita.setTipoLavado(dto.getTipoLavado());
        cita.setFecha(dto.getFecha());
        cita.setHora(dto.getHora());

        // Actualizar campos adicionales si están presentes
        if (dto.getEstado() != null) {
            try {
                cita.setEstado(EstadoCita.valueOf(dto.getEstado()));
            } catch (IllegalArgumentException e) {
                // Mantener el estado actual si el valor no es válido
            }
        }

        if (dto.getPagoAdelantado() != null) {
            cita.setPagoAdelantado(dto.getPagoAdelantado());
        }

        if (dto.getReferenciaPago() != null) {
            cita.setReferenciaPago(dto.getReferenciaPago());
        }

        if (dto.getObservaciones() != null) {
            cita.setObservaciones(dto.getObservaciones());
        }
    }

    /**
     * Crea un CitaDTO con fechas formateadas para la vista
     */
    public CitaDTO toDTOWithFormattedDates(Cita cita) {
        CitaDTO dto = toDTO(cita);

        if (dto != null && dto.getFecha() != null && dto.getHora() != null) {
            // Aquí podrías agregar campos adicionales formateados si el DTO los necesita
            // Por ahora, el formateo se hace directamente en los templates con DateTimeFormatUtils
        }

        return dto;
    }
}
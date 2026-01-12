package com.lavaderosepulveda.app.mapper;

import com.lavaderosepulveda.app.dto.GastoDTO;
import com.lavaderosepulveda.app.model.Gasto;
import com.lavaderosepulveda.app.model.enums.CategoriaGasto;
import com.lavaderosepulveda.app.util.DateTimeFormatUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre Gasto (Entity) y GastoDTO
 */
@Component
public class GastoMapper {

    /**
     * Convierte una entidad Gasto a GastoDTO
     */
    public GastoDTO toDTO(Gasto gasto) {
        if (gasto == null) {
            return null;
        }

        GastoDTO dto = new GastoDTO();
        dto.setId(gasto.getId());
        dto.setConcepto(gasto.getConcepto());
        dto.setImporte(gasto.getImporte());
        
        // Formatear fecha
        if (gasto.getFecha() != null) {
            dto.setFecha(DateTimeFormatUtils.formatDate(gasto.getFecha()));
        }
        
        // Enum a String
        if (gasto.getCategoria() != null) {
            dto.setCategoria(gasto.getCategoria().name());
        }
        
        dto.setProveedor(gasto.getProveedor());
        dto.setNumeroFactura(gasto.getNumeroFactura());
        dto.setDeducible(gasto.getDeducible());
        dto.setRecurrente(gasto.getRecurrente());
        dto.setPeriodicidad(gasto.getPeriodicidad());
        dto.setNotas(gasto.getNotas());

        return dto;
    }

    /**
     * Convierte un GastoDTO a entidad Gasto
     */
    public Gasto toEntity(GastoDTO dto) {
        if (dto == null) {
            return null;
        }

        Gasto gasto = new Gasto();
        gasto.setId(dto.getId());
        gasto.setConcepto(dto.getConcepto());
        gasto.setImporte(dto.getImporte());
        
        // Parsear fecha
        if (dto.getFecha() != null && !dto.getFecha().isEmpty()) {
            gasto.setFecha(DateTimeFormatUtils.parseDate(dto.getFecha()));
        }
        
        // String a Enum
        if (dto.getCategoria() != null && !dto.getCategoria().isEmpty()) {
            try {
                gasto.setCategoria(CategoriaGasto.valueOf(dto.getCategoria()));
            } catch (IllegalArgumentException e) {
                // Si no coincide con ningún enum, dejarlo null
            }
        }
        
        gasto.setProveedor(dto.getProveedor());
        gasto.setNumeroFactura(dto.getNumeroFactura());
        gasto.setDeducible(dto.getDeducible() != null ? dto.getDeducible() : true);
        gasto.setRecurrente(dto.getRecurrente() != null ? dto.getRecurrente() : false);
        gasto.setPeriodicidad(dto.getPeriodicidad());
        gasto.setNotas(dto.getNotas());

        return gasto;
    }

    /**
     * Actualiza una entidad Gasto existente con datos del DTO
     */
    public void updateEntity(Gasto gasto, GastoDTO dto) {
        if (dto == null || gasto == null) {
            return;
        }

        if (dto.getConcepto() != null) gasto.setConcepto(dto.getConcepto());
        if (dto.getImporte() != null) gasto.setImporte(dto.getImporte());
        if (dto.getFecha() != null && !dto.getFecha().isEmpty()) {
            gasto.setFecha(DateTimeFormatUtils.parseDate(dto.getFecha()));
        }
        if (dto.getCategoria() != null && !dto.getCategoria().isEmpty()) {
            try {
                gasto.setCategoria(CategoriaGasto.valueOf(dto.getCategoria()));
            } catch (IllegalArgumentException e) {
                // Ignorar si no es válido
            }
        }
        if (dto.getProveedor() != null) gasto.setProveedor(dto.getProveedor());
        if (dto.getNumeroFactura() != null) gasto.setNumeroFactura(dto.getNumeroFactura());
        if (dto.getDeducible() != null) gasto.setDeducible(dto.getDeducible());
        if (dto.getRecurrente() != null) gasto.setRecurrente(dto.getRecurrente());
        if (dto.getPeriodicidad() != null) gasto.setPeriodicidad(dto.getPeriodicidad());
        if (dto.getNotas() != null) gasto.setNotas(dto.getNotas());
    }

    /**
     * Convierte lista de entidades a lista de DTOs
     */
    public List<GastoDTO> toDTOList(List<Gasto> gastos) {
        if (gastos == null) {
            return new ArrayList<>();
        }
        return gastos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}

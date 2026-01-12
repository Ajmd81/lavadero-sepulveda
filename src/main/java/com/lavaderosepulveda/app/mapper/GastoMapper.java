package com.lavaderosepulveda.app.mapper;

import com.lavaderosepulveda.app.dto.GastoDTO;
import com.lavaderosepulveda.app.model.Gasto;
import com.lavaderosepulveda.app.model.enums.CategoriaGasto;
import com.lavaderosepulveda.app.model.enums.MetodoPago;
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
            dto.setFecha(DateTimeFormatUtils.formatearFechaCorta(gasto.getFecha()));
        }
        
        // Enum a String
        if (gasto.getCategoria() != null) {
            dto.setCategoria(gasto.getCategoria().name());
        }
        if (gasto.getMetodoPago() != null) {
            dto.setMetodoPago(gasto.getMetodoPago().name());
        }
        
        dto.setIvaIncluido(gasto.getIvaIncluido());
        dto.setBaseImponible(gasto.getBaseImponible());
        dto.setCuotaIva(gasto.getCuotaIva());
        
        // Factura recibida vinculada
        if (gasto.getFacturaRecibida() != null) {
            dto.setFacturaRecibidaId(gasto.getFacturaRecibida().getId());
        }
        
        dto.setRecurrente(gasto.getRecurrente());
        dto.setDiaRecurrencia(gasto.getDiaRecurrencia());
        dto.setNotas(gasto.getNotas());
        dto.setPagado(gasto.getPagado());

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
            gasto.setFecha(DateTimeFormatUtils.parsearFechaCorta(dto.getFecha()));
        }
        
        // String a Enum
        if (dto.getCategoria() != null && !dto.getCategoria().isEmpty()) {
            try {
                gasto.setCategoria(CategoriaGasto.valueOf(dto.getCategoria()));
            } catch (IllegalArgumentException e) {
                // Si no coincide con ningún enum, dejarlo null
            }
        }
        if (dto.getMetodoPago() != null && !dto.getMetodoPago().isEmpty()) {
            try {
                gasto.setMetodoPago(MetodoPago.valueOf(dto.getMetodoPago()));
            } catch (IllegalArgumentException e) {
                // Ignorar
            }
        }
        
        gasto.setIvaIncluido(dto.getIvaIncluido() != null ? dto.getIvaIncluido() : true);
        gasto.setBaseImponible(dto.getBaseImponible());
        gasto.setCuotaIva(dto.getCuotaIva());
        
        // facturaRecibida se setea en el Service
        
        gasto.setRecurrente(dto.getRecurrente() != null ? dto.getRecurrente() : false);
        gasto.setDiaRecurrencia(dto.getDiaRecurrencia());
        gasto.setNotas(dto.getNotas());
        gasto.setPagado(dto.getPagado() != null ? dto.getPagado() : true);

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
            gasto.setFecha(DateTimeFormatUtils.parsearFechaCorta(dto.getFecha()));
        }
        if (dto.getCategoria() != null && !dto.getCategoria().isEmpty()) {
            try {
                gasto.setCategoria(CategoriaGasto.valueOf(dto.getCategoria()));
            } catch (IllegalArgumentException e) {
                // Ignorar si no es válido
            }
        }
        if (dto.getMetodoPago() != null && !dto.getMetodoPago().isEmpty()) {
            try {
                gasto.setMetodoPago(MetodoPago.valueOf(dto.getMetodoPago()));
            } catch (IllegalArgumentException e) {
                // Ignorar
            }
        }
        if (dto.getIvaIncluido() != null) gasto.setIvaIncluido(dto.getIvaIncluido());
        if (dto.getBaseImponible() != null) gasto.setBaseImponible(dto.getBaseImponible());
        if (dto.getCuotaIva() != null) gasto.setCuotaIva(dto.getCuotaIva());
        if (dto.getRecurrente() != null) gasto.setRecurrente(dto.getRecurrente());
        if (dto.getDiaRecurrencia() != null) gasto.setDiaRecurrencia(dto.getDiaRecurrencia());
        if (dto.getNotas() != null) gasto.setNotas(dto.getNotas());
        if (dto.getPagado() != null) gasto.setPagado(dto.getPagado());
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

package com.lavaderosepulveda.app.mapper;

import com.lavaderosepulveda.app.dto.FacturaRecibidaDTO;
import com.lavaderosepulveda.app.model.FacturaRecibida;
import com.lavaderosepulveda.app.model.Proveedor;
import com.lavaderosepulveda.app.model.enums.CategoriaGasto;
import com.lavaderosepulveda.app.model.enums.EstadoFactura;
import com.lavaderosepulveda.app.model.enums.MetodoPago;
import com.lavaderosepulveda.app.util.DateTimeFormatUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre FacturaRecibida (Entity) y FacturaRecibidaDTO
 */
@Component
public class FacturaRecibidaMapper {

    /**
     * Convierte una entidad FacturaRecibida a FacturaRecibidaDTO
     */
    public FacturaRecibidaDTO toDTO(FacturaRecibida factura) {
        if (factura == null) {
            return null;
        }

        FacturaRecibidaDTO dto = new FacturaRecibidaDTO();
        dto.setId(factura.getId());
        dto.setNumeroFactura(factura.getNumeroFactura());
        
        // Formatear fechas
        if (factura.getFechaFactura() != null) {
            dto.setFechaFactura(DateTimeFormatUtils.formatDate(factura.getFechaFactura()));
        }
        if (factura.getFechaRecepcion() != null) {
            dto.setFechaRecepcion(DateTimeFormatUtils.formatDate(factura.getFechaRecepcion()));
        }
        if (factura.getFechaVencimiento() != null) {
            dto.setFechaVencimiento(DateTimeFormatUtils.formatDate(factura.getFechaVencimiento()));
        }
        if (factura.getFechaPago() != null) {
            dto.setFechaPago(DateTimeFormatUtils.formatDate(factura.getFechaPago()));
        }
        
        // Proveedor
        Proveedor proveedor = factura.getProveedor();
        if (proveedor != null) {
            dto.setProveedorId(proveedor.getId());
            dto.setProveedorNombre(proveedor.getNombre());
        }
        
        // Enums a String
        if (factura.getCategoria() != null) {
            dto.setCategoria(factura.getCategoria().name());
        }
        if (factura.getEstado() != null) {
            dto.setEstado(factura.getEstado().name());
        }
        if (factura.getMetodoPago() != null) {
            dto.setMetodoPago(factura.getMetodoPago().name());
        }
        
        // Importes
        dto.setBaseImponible(factura.getBaseImponible());
        dto.setTipoIva(factura.getTipoIva());
        dto.setImporteIva(factura.getImporteIva());
        dto.setTipoIrpf(factura.getTipoIrpf());
        dto.setImporteIrpf(factura.getImporteIrpf());
        dto.setTotal(factura.getTotal());
        
        // Otros
        dto.setConcepto(factura.getConcepto());
        dto.setNotas(factura.getNotas());

        return dto;
    }

    /**
     * Convierte un FacturaRecibidaDTO a entidad FacturaRecibida (sin proveedor)
     */
    public FacturaRecibida toEntity(FacturaRecibidaDTO dto) {
        if (dto == null) {
            return null;
        }

        FacturaRecibida factura = new FacturaRecibida();
        factura.setId(dto.getId());
        factura.setNumeroFactura(dto.getNumeroFactura());
        
        // Parsear fechas
        if (dto.getFechaFactura() != null && !dto.getFechaFactura().isEmpty()) {
            factura.setFechaFactura(DateTimeFormatUtils.parseDate(dto.getFechaFactura()));
        }
        if (dto.getFechaRecepcion() != null && !dto.getFechaRecepcion().isEmpty()) {
            factura.setFechaRecepcion(DateTimeFormatUtils.parseDate(dto.getFechaRecepcion()));
        }
        if (dto.getFechaVencimiento() != null && !dto.getFechaVencimiento().isEmpty()) {
            factura.setFechaVencimiento(DateTimeFormatUtils.parseDate(dto.getFechaVencimiento()));
        }
        if (dto.getFechaPago() != null && !dto.getFechaPago().isEmpty()) {
            factura.setFechaPago(DateTimeFormatUtils.parseDate(dto.getFechaPago()));
        }
        
        // String a Enums
        if (dto.getCategoria() != null && !dto.getCategoria().isEmpty()) {
            try {
                factura.setCategoria(CategoriaGasto.valueOf(dto.getCategoria()));
            } catch (IllegalArgumentException e) {
                // Ignorar
            }
        }
        if (dto.getEstado() != null && !dto.getEstado().isEmpty()) {
            factura.setEstado(EstadoFactura.valueOf(dto.getEstado()));
        }
        if (dto.getMetodoPago() != null && !dto.getMetodoPago().isEmpty()) {
            factura.setMetodoPago(MetodoPago.valueOf(dto.getMetodoPago()));
        }
        
        // Importes
        factura.setBaseImponible(dto.getBaseImponible());
        factura.setTipoIva(dto.getTipoIva());
        factura.setImporteIva(dto.getImporteIva());
        factura.setTipoIrpf(dto.getTipoIrpf());
        factura.setImporteIrpf(dto.getImporteIrpf());
        factura.setTotal(dto.getTotal());
        
        // Otros
        factura.setConcepto(dto.getConcepto());
        factura.setNotas(dto.getNotas());

        return factura;
    }

    /**
     * Actualiza una entidad FacturaRecibida existente con datos del DTO
     */
    public void updateEntity(FacturaRecibida factura, FacturaRecibidaDTO dto) {
        if (dto == null || factura == null) {
            return;
        }

        if (dto.getNumeroFactura() != null) factura.setNumeroFactura(dto.getNumeroFactura());
        if (dto.getFechaFactura() != null && !dto.getFechaFactura().isEmpty()) {
            factura.setFechaFactura(DateTimeFormatUtils.parseDate(dto.getFechaFactura()));
        }
        if (dto.getFechaRecepcion() != null && !dto.getFechaRecepcion().isEmpty()) {
            factura.setFechaRecepcion(DateTimeFormatUtils.parseDate(dto.getFechaRecepcion()));
        }
        if (dto.getFechaVencimiento() != null && !dto.getFechaVencimiento().isEmpty()) {
            factura.setFechaVencimiento(DateTimeFormatUtils.parseDate(dto.getFechaVencimiento()));
        }
        if (dto.getCategoria() != null && !dto.getCategoria().isEmpty()) {
            try {
                factura.setCategoria(CategoriaGasto.valueOf(dto.getCategoria()));
            } catch (IllegalArgumentException e) {
                // Ignorar
            }
        }
        if (dto.getBaseImponible() != null) factura.setBaseImponible(dto.getBaseImponible());
        if (dto.getTipoIva() != null) factura.setTipoIva(dto.getTipoIva());
        if (dto.getImporteIva() != null) factura.setImporteIva(dto.getImporteIva());
        if (dto.getTipoIrpf() != null) factura.setTipoIrpf(dto.getTipoIrpf());
        if (dto.getImporteIrpf() != null) factura.setImporteIrpf(dto.getImporteIrpf());
        if (dto.getTotal() != null) factura.setTotal(dto.getTotal());
        if (dto.getConcepto() != null) factura.setConcepto(dto.getConcepto());
        if (dto.getNotas() != null) factura.setNotas(dto.getNotas());
    }

    /**
     * Convierte lista de entidades a lista de DTOs
     */
    public List<FacturaRecibidaDTO> toDTOList(List<FacturaRecibida> facturas) {
        if (facturas == null) {
            return new ArrayList<>();
        }
        return facturas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}

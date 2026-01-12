package com.lavaderosepulveda.app.mapper;

import com.lavaderosepulveda.app.dto.FacturaDTO;
import com.lavaderosepulveda.app.model.Cliente;
import com.lavaderosepulveda.app.model.Factura;
import com.lavaderosepulveda.app.model.LineaFactura;
import com.lavaderosepulveda.app.model.enums.EstadoFactura;
import com.lavaderosepulveda.app.model.enums.MetodoPago;
import com.lavaderosepulveda.app.model.enums.TipoFactura;
import com.lavaderosepulveda.app.util.DateTimeFormatUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre Factura (Entity) y FacturaDTO
 */
@Component
public class FacturaMapper {

    /**
     * Convierte una entidad Factura a FacturaDTO
     */
    public FacturaDTO toDTO(Factura factura) {
        if (factura == null) {
            return null;
        }

        FacturaDTO dto = new FacturaDTO();
        dto.setId(factura.getId());
        dto.setNumero(factura.getNumero());
        
        // Formatear fecha
        if (factura.getFecha() != null) {
            dto.setFecha(DateTimeFormatUtils.formatearFechaCorta(factura.getFecha()));
        }
        
        // Enums a String
        if (factura.getTipo() != null) {
            dto.setTipo(factura.getTipo().name());
        }
        if (factura.getEstado() != null) {
            dto.setEstado(factura.getEstado().name());
        }
        if (factura.getMetodoPago() != null) {
            dto.setMetodoPago(factura.getMetodoPago().name());
        }
        
        // Datos del cliente
        Cliente cliente = factura.getCliente();
        if (cliente != null) {
            dto.setClienteId(cliente.getId());
            dto.setClienteNombre(cliente.getNombre());
            dto.setClienteNif(cliente.getNif());
            dto.setClienteDireccion(cliente.getDireccion());
            dto.setClienteEmail(cliente.getEmail());
            dto.setClienteTelefono(cliente.getTelefono());
        } else {
            // Datos directos de la factura (factura simplificada)
            dto.setClienteNombre(factura.getClienteNombre());
            dto.setClienteNif(factura.getClienteNif());
            dto.setClienteDireccion(factura.getClienteDireccion());
        }
        
        // Líneas de factura
        if (factura.getLineas() != null) {
            dto.setLineas(factura.getLineas().stream()
                    .map(this::toLineaDTO)
                    .collect(Collectors.toList()));
        }
        
        // Importes
        dto.setBaseImponible(factura.getBaseImponible());
        dto.setTipoIva(factura.getTipoIva());
        dto.setImporteIva(factura.getImporteIva());
        dto.setTotal(factura.getTotal());
        
        // Otros
        if (factura.getFechaPago() != null) {
            dto.setFechaPago(DateTimeFormatUtils.formatearFechaCorta(factura.getFechaPago()));
        }
        dto.setObservaciones(factura.getObservaciones());

        return dto;
    }

    /**
     * Convierte una LineaFactura a LineaFacturaDTO
     */
    private FacturaDTO.LineaFacturaDTO toLineaDTO(LineaFactura linea) {
        if (linea == null) {
            return null;
        }

        FacturaDTO.LineaFacturaDTO dto = new FacturaDTO.LineaFacturaDTO();
        dto.setId(linea.getId());
        dto.setCitaId(linea.getCitaId());
        dto.setConcepto(linea.getConcepto());
        dto.setCantidad(linea.getCantidad());
        dto.setPrecioUnitario(linea.getPrecioUnitario());
        dto.setSubtotal(linea.getSubtotal());

        return dto;
    }

    /**
     * Convierte un FacturaDTO a entidad Factura (básico, sin cliente)
     */
    public Factura toEntity(FacturaDTO dto) {
        if (dto == null) {
            return null;
        }

        Factura factura = new Factura();
        factura.setId(dto.getId());
        factura.setNumero(dto.getNumero());
        
        // Parsear fecha
        if (dto.getFecha() != null && !dto.getFecha().isEmpty()) {
            factura.setFecha(DateTimeFormatUtils.parsearFechaCorta(dto.getFecha()));
        }
        
        // String a Enums
        if (dto.getTipo() != null) {
            factura.setTipo(TipoFactura.valueOf(dto.getTipo()));
        }
        if (dto.getEstado() != null) {
            factura.setEstado(EstadoFactura.valueOf(dto.getEstado()));
        }
        if (dto.getMetodoPago() != null && !dto.getMetodoPago().isEmpty()) {
            factura.setMetodoPago(MetodoPago.valueOf(dto.getMetodoPago()));
        }
        
        // Datos del cliente directos
        factura.setClienteNombre(dto.getClienteNombre());
        factura.setClienteNif(dto.getClienteNif());
        factura.setClienteDireccion(dto.getClienteDireccion());
        
        // Importes
        factura.setBaseImponible(dto.getBaseImponible());
        factura.setTipoIva(dto.getTipoIva());
        factura.setImporteIva(dto.getImporteIva());
        factura.setTotal(dto.getTotal());
        
        // Otros
        if (dto.getFechaPago() != null && !dto.getFechaPago().isEmpty()) {
            factura.setFechaPago(DateTimeFormatUtils.parsearFechaCorta(dto.getFechaPago()));
        }
        factura.setObservaciones(dto.getObservaciones());

        return factura;
    }

    /**
     * Convierte lista de entidades a lista de DTOs
     */
    public List<FacturaDTO> toDTOList(List<Factura> facturas) {
        if (facturas == null) {
            return new ArrayList<>();
        }
        return facturas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}

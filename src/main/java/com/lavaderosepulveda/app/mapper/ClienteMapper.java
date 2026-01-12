package com.lavaderosepulveda.app.mapper;

import com.lavaderosepulveda.app.dto.ClienteDTO;
import com.lavaderosepulveda.app.model.Cliente;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Cliente (Entity) y ClienteDTO
 */
@Component
public class ClienteMapper {

    /**
     * Convierte una entidad Cliente a ClienteDTO
     */
    public ClienteDTO toDTO(Cliente cliente) {
        if (cliente == null) {
            return null;
        }

        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setNif(cliente.getNif());
        dto.setDireccion(cliente.getDireccion());
        dto.setCodigoPostal(cliente.getCodigoPostal());
        dto.setPoblacion(cliente.getPoblacion());
        dto.setProvincia(cliente.getProvincia());
        dto.setTelefono(cliente.getTelefono());
        dto.setEmail(cliente.getEmail());
        dto.setObservaciones(cliente.getObservaciones());
        dto.setActivo(cliente.getActivo());
        dto.setTotalVisitas(cliente.getTotalVisitas());
        dto.setTotalGastado(cliente.getTotalGastado());
        dto.setTotalNoPresentaciones(cliente.getTotalNoPresentaciones());
        
        // Formatear fechas si existen
        if (cliente.getCreatedAt() != null) {
            dto.setCreatedAt(cliente.getCreatedAt().toString());
        }
        if (cliente.getUpdatedAt() != null) {
            dto.setUpdatedAt(cliente.getUpdatedAt().toString());
        }
        if (cliente.getUltimaVisita() != null) {
            dto.setUltimaVisita(cliente.getUltimaVisita().toString());
        }

        return dto;
    }

    /**
     * Convierte un ClienteDTO a entidad Cliente
     */
    public Cliente toEntity(ClienteDTO dto) {
        if (dto == null) {
            return null;
        }

        Cliente cliente = new Cliente();
        cliente.setId(dto.getId());
        cliente.setNombre(dto.getNombre());
        cliente.setNif(dto.getNif());
        cliente.setDireccion(dto.getDireccion());
        cliente.setCodigoPostal(dto.getCodigoPostal());
        cliente.setPoblacion(dto.getPoblacion());
        cliente.setProvincia(dto.getProvincia());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());
        cliente.setObservaciones(dto.getObservaciones());
        cliente.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        cliente.setTotalVisitas(dto.getTotalVisitas() != null ? dto.getTotalVisitas() : 0);
        cliente.setTotalGastado(dto.getTotalGastado());
        cliente.setTotalNoPresentaciones(dto.getTotalNoPresentaciones() != null ? dto.getTotalNoPresentaciones() : 0);

        return cliente;
    }

    /**
     * Actualiza una entidad Cliente existente con datos del DTO
     */
    public void updateEntity(Cliente cliente, ClienteDTO dto) {
        if (dto == null || cliente == null) {
            return;
        }

        if (dto.getNombre() != null) cliente.setNombre(dto.getNombre());
        if (dto.getNif() != null) cliente.setNif(dto.getNif());
        if (dto.getDireccion() != null) cliente.setDireccion(dto.getDireccion());
        if (dto.getCodigoPostal() != null) cliente.setCodigoPostal(dto.getCodigoPostal());
        if (dto.getPoblacion() != null) cliente.setPoblacion(dto.getPoblacion());
        if (dto.getProvincia() != null) cliente.setProvincia(dto.getProvincia());
        if (dto.getTelefono() != null) cliente.setTelefono(dto.getTelefono());
        if (dto.getEmail() != null) cliente.setEmail(dto.getEmail());
        if (dto.getObservaciones() != null) cliente.setObservaciones(dto.getObservaciones());
        if (dto.getActivo() != null) cliente.setActivo(dto.getActivo());
    }
}

package com.lavaderosepulveda.app.mapper;

import com.lavaderosepulveda.app.dto.ProveedorDTO;
import com.lavaderosepulveda.app.model.Proveedor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre Proveedor (Entity) y ProveedorDTO
 */
@Component
public class ProveedorMapper {

    /**
     * Convierte una entidad Proveedor a ProveedorDTO
     */
    public ProveedorDTO toDTO(Proveedor proveedor) {
        if (proveedor == null) {
            return null;
        }

        ProveedorDTO dto = new ProveedorDTO();
        dto.setId(proveedor.getId());
        dto.setNombre(proveedor.getNombre());
        dto.setNif(proveedor.getNif());
        dto.setDireccion(proveedor.getDireccion());
        dto.setTelefono(proveedor.getTelefono());
        dto.setEmail(proveedor.getEmail());
        dto.setContacto(proveedor.getContacto());
        dto.setIban(proveedor.getIban());
        dto.setNotas(proveedor.getNotas());
        dto.setActivo(proveedor.getActivo());

        return dto;
    }

    /**
     * Convierte un ProveedorDTO a entidad Proveedor
     */
    public Proveedor toEntity(ProveedorDTO dto) {
        if (dto == null) {
            return null;
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setId(dto.getId());
        proveedor.setNombre(dto.getNombre());
        proveedor.setNif(dto.getNif());
        proveedor.setDireccion(dto.getDireccion());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setEmail(dto.getEmail());
        proveedor.setContacto(dto.getContacto());
        proveedor.setIban(dto.getIban());
        proveedor.setNotas(dto.getNotas());
        proveedor.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        return proveedor;
    }

    /**
     * Actualiza una entidad Proveedor existente con datos del DTO
     */
    public void updateEntity(Proveedor proveedor, ProveedorDTO dto) {
        if (dto == null || proveedor == null) {
            return;
        }

        if (dto.getNombre() != null) proveedor.setNombre(dto.getNombre());
        if (dto.getNif() != null) proveedor.setNif(dto.getNif());
        if (dto.getDireccion() != null) proveedor.setDireccion(dto.getDireccion());
        if (dto.getTelefono() != null) proveedor.setTelefono(dto.getTelefono());
        if (dto.getEmail() != null) proveedor.setEmail(dto.getEmail());
        if (dto.getContacto() != null) proveedor.setContacto(dto.getContacto());
        if (dto.getIban() != null) proveedor.setIban(dto.getIban());
        if (dto.getNotas() != null) proveedor.setNotas(dto.getNotas());
        if (dto.getActivo() != null) proveedor.setActivo(dto.getActivo());
    }

    /**
     * Convierte lista de entidades a lista de DTOs
     */
    public List<ProveedorDTO> toDTOList(List<Proveedor> proveedores) {
        if (proveedores == null) {
            return new ArrayList<>();
        }
        return proveedores.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}

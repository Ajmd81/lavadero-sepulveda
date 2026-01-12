package com.lavaderosepulveda.app.mapper;

import com.lavaderosepulveda.app.dto.ClienteDTO;
import com.lavaderosepulveda.app.model.Cliente;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        dto.setApellidos(cliente.getApellidos());
        dto.setTelefono(cliente.getTelefono());
        dto.setEmail(cliente.getEmail());
        dto.setActivo(cliente.getActivo());
        dto.setVehiculoHabitual(cliente.getVehiculoHabitual());
        
        // Campos CRM
        dto.setNif(cliente.getNif());
        dto.setDireccion(cliente.getDireccion());
        dto.setCodigoPostal(cliente.getCodigoPostal());
        dto.setCiudad(cliente.getCiudad());
        dto.setProvincia(cliente.getProvincia());
        dto.setMatricula(cliente.getMatricula());
        dto.setMarca(cliente.getMarca());
        dto.setModelo(cliente.getModelo());
        dto.setColor(cliente.getColor());
        dto.setNotas(cliente.getNotas());
        
        // Estadísticas se calculan en el Service, no aquí
        // totalCitas, citasCompletadas, etc. vienen de consultas

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
        cliente.setApellidos(dto.getApellidos());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());
        cliente.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        cliente.setVehiculoHabitual(dto.getVehiculoHabitual());
        
        // Campos CRM
        cliente.setNif(dto.getNif());
        cliente.setDireccion(dto.getDireccion());
        cliente.setCodigoPostal(dto.getCodigoPostal());
        cliente.setCiudad(dto.getCiudad());
        cliente.setProvincia(dto.getProvincia());
        cliente.setMatricula(dto.getMatricula());
        cliente.setMarca(dto.getMarca());
        cliente.setModelo(dto.getModelo());
        cliente.setColor(dto.getColor());
        cliente.setNotas(dto.getNotas());

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
        if (dto.getApellidos() != null) cliente.setApellidos(dto.getApellidos());
        if (dto.getTelefono() != null) cliente.setTelefono(dto.getTelefono());
        if (dto.getEmail() != null) cliente.setEmail(dto.getEmail());
        if (dto.getActivo() != null) cliente.setActivo(dto.getActivo());
        if (dto.getVehiculoHabitual() != null) cliente.setVehiculoHabitual(dto.getVehiculoHabitual());
        
        // Campos CRM
        if (dto.getNif() != null) cliente.setNif(dto.getNif());
        if (dto.getDireccion() != null) cliente.setDireccion(dto.getDireccion());
        if (dto.getCodigoPostal() != null) cliente.setCodigoPostal(dto.getCodigoPostal());
        if (dto.getCiudad() != null) cliente.setCiudad(dto.getCiudad());
        if (dto.getProvincia() != null) cliente.setProvincia(dto.getProvincia());
        if (dto.getMatricula() != null) cliente.setMatricula(dto.getMatricula());
        if (dto.getMarca() != null) cliente.setMarca(dto.getMarca());
        if (dto.getModelo() != null) cliente.setModelo(dto.getModelo());
        if (dto.getColor() != null) cliente.setColor(dto.getColor());
        if (dto.getNotas() != null) cliente.setNotas(dto.getNotas());
    }

    /**
     * Convierte lista de entidades a lista de DTOs
     */
    public List<ClienteDTO> toDTOList(List<Cliente> clientes) {
        if (clientes == null) {
            return new ArrayList<>();
        }
        return clientes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}

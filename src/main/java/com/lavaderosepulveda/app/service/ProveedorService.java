package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.dto.ProveedorDTO;
import com.lavaderosepulveda.app.model.Proveedor;
import com.lavaderosepulveda.app.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    public List<ProveedorDTO> listarActivos() {
        return proveedorRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProveedorDTO> listarTodos() {
        return proveedorRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProveedorDTO obtenerPorId(Long id) {
        return proveedorRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    public List<ProveedorDTO> buscar(String termino) {
        return proveedorRepository.buscarActivos(termino)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProveedorDTO crear(ProveedorDTO dto) {
        Proveedor proveedor = new Proveedor();
        actualizarDesdeDTO(proveedor, dto);
        proveedor.setActivo(true);
        return toDTO(proveedorRepository.save(proveedor));
    }

    public ProveedorDTO actualizar(Long id, ProveedorDTO dto) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        actualizarDesdeDTO(proveedor, dto);
        return toDTO(proveedorRepository.save(proveedor));
    }

    public void desactivar(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
    }

    public void activar(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        proveedor.setActivo(true);
        proveedorRepository.save(proveedor);
    }

    public void eliminar(Long id) {
        proveedorRepository.deleteById(id);
    }

    private void actualizarDesdeDTO(Proveedor proveedor, ProveedorDTO dto) {
        proveedor.setNombre(dto.getNombre());
        proveedor.setNif(dto.getNif());
        proveedor.setDireccion(dto.getDireccion());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setEmail(dto.getEmail());
        proveedor.setContacto(dto.getContacto());
        proveedor.setIban(dto.getIban());
        proveedor.setNotas(dto.getNotas());
        if (dto.getActivo() != null) {
            proveedor.setActivo(dto.getActivo());
        }
    }

    private ProveedorDTO toDTO(Proveedor proveedor) {
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
}

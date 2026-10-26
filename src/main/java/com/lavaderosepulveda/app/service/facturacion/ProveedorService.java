package com.lavaderosepulveda.app.service.facturacion;

import com.lavaderosepulveda.app.dto.facturacion.ProveedorDTO;

import com.lavaderosepulveda.app.model.facturacion.Proveedor;
import com.lavaderosepulveda.app.repository.facturacion.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    /**
    * Obtener proveedores activos con paginación
    */
    public Page<ProveedorDTO> listarActivosPaginados(Pageable pageable) {
        return proveedorRepository.findByActivoTrue(pageable)
                .map(this::toDTO);
    }

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
        validar(dto);
        Proveedor proveedor = new Proveedor();
        actualizarDesdeDTO(proveedor, dto);
        // Respeta el check "Activo" del formulario; por defecto, activo
        proveedor.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        return toDTO(proveedorRepository.save(proveedor));
    }

    public ProveedorDTO actualizar(Long id, ProveedorDTO dto) {
        validar(dto);
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

    /** El nombre es el único dato imprescindible para poder facturar */
    private void validar(ProveedorDTO dto) {
        if (dto == null || dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del proveedor es obligatorio");
        }
    }

    private void actualizarDesdeDTO(Proveedor proveedor, ProveedorDTO dto) {
        proveedor.setNombre(dto.getNombre().trim());
        proveedor.setNif(dto.getNif() != null ? dto.getNif().trim().toUpperCase() : null);
        proveedor.setDireccion(dto.getDireccion());
        proveedor.setCiudad(dto.getCiudad());
        proveedor.setCodigoPostal(dto.getCodigoPostal());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setEmail(dto.getEmail());
        proveedor.setContacto(dto.getContacto());
        proveedor.setIban(dto.getIban());
        proveedor.setNotas(dto.getNotas());
        proveedor.setCategoria(dto.getCategoria());
        proveedor.setFormaPago(dto.getFormaPago());
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
        dto.setCiudad(proveedor.getCiudad());
        dto.setCodigoPostal(proveedor.getCodigoPostal());
        dto.setTelefono(proveedor.getTelefono());
        dto.setEmail(proveedor.getEmail());
        dto.setContacto(proveedor.getContacto());
        dto.setIban(proveedor.getIban());
        dto.setNotas(proveedor.getNotas());
        dto.setCategoria(proveedor.getCategoria());
        dto.setFormaPago(proveedor.getFormaPago());
        dto.setActivo(proveedor.getActivo());
        return dto;
    }
}

package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.dto.AlbaranDTO;
import com.lavaderosepulveda.app.dto.LineaAlbaranDTO;
import com.lavaderosepulveda.app.model.Albaran;
import com.lavaderosepulveda.app.model.Cliente;
import com.lavaderosepulveda.app.model.LineaAlbaran;
import com.lavaderosepulveda.app.repository.AlbaranRepository;
import com.lavaderosepulveda.app.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbaranService {
    
    private final AlbaranRepository albaranRepository;
    private final ClienteRepository clienteRepository;
    
    @Transactional(readOnly = true)
    public List<AlbaranDTO> findAll() {
        return albaranRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public AlbaranDTO findById(Long id) {
        return albaranRepository.findById(id)
            .map(this::convertToDTO)
            .orElseThrow(() -> new RuntimeException("Albarán no encontrado: " + id));
    }
    
    @Transactional(readOnly = true)
    public List<AlbaranDTO> findByCliente(Long clienteId) {
        return albaranRepository.findByClienteIdOrderByFechaDesc(clienteId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AlbaranDTO> findByEstado(Albaran.EstadoAlbaran estado) {
        return albaranRepository.findByEstado(estado).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AlbaranDTO> findPendientesParaFacturar(Long clienteId) {
        return albaranRepository.findByEstadoAndClienteId(
            Albaran.EstadoAlbaran.ENTREGADO, clienteId
        ).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public AlbaranDTO create(AlbaranDTO dto) {
        Albaran albaran = new Albaran();
        albaran.setNumero(generarNumeroAlbaran());
        albaran.setFecha(dto.getFecha());
        
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + dto.getClienteId()));
        albaran.setCliente(cliente);
        
        albaran.setEstado(Albaran.EstadoAlbaran.PENDIENTE);
        
        // Crear líneas
        for (LineaAlbaranDTO lineaDTO : dto.getLineas()) {
            LineaAlbaran linea = new LineaAlbaran();
            linea.setConcepto(lineaDTO.getConcepto());
            linea.setCantidad(lineaDTO.getCantidad());
            linea.setPrecioUnitario(lineaDTO.getPrecioUnitario());
            linea.setTipoIva(lineaDTO.getTipoIva());
            linea.setAlbaran(albaran);
            albaran.getLineas().add(linea);
        }
        
        albaran.calcularTotales();
        Albaran saved = albaranRepository.save(albaran);
        return convertToDTO(saved);
    }
    
    @Transactional
    public AlbaranDTO update(Long id, AlbaranDTO dto) {
        Albaran albaran = albaranRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Albarán no encontrado: " + id));
        
        if (albaran.getEstado() == Albaran.EstadoAlbaran.FACTURADO) {
            throw new RuntimeException("No se puede modificar un albarán ya facturado");
        }
        
        albaran.setFecha(dto.getFecha());
        
        // Actualizar cliente si cambió
        if (!albaran.getCliente().getId().equals(dto.getClienteId())) {
            Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            albaran.setCliente(cliente);
        }
        
        // Reemplazar líneas
        albaran.getLineas().clear();
        for (LineaAlbaranDTO lineaDTO : dto.getLineas()) {
            LineaAlbaran linea = new LineaAlbaran();
            linea.setConcepto(lineaDTO.getConcepto());
            linea.setCantidad(lineaDTO.getCantidad());
            linea.setPrecioUnitario(lineaDTO.getPrecioUnitario());
            linea.setTipoIva(lineaDTO.getTipoIva());
            linea.setAlbaran(albaran);
            albaran.getLineas().add(linea);
        }
        
        albaran.calcularTotales();
        Albaran updated = albaranRepository.save(albaran);
        return convertToDTO(updated);
    }
    
    @Transactional
    public void cambiarEstado(Long id, Albaran.EstadoAlbaran nuevoEstado) {
        Albaran albaran = albaranRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Albarán no encontrado: " + id));
        
        if (albaran.getEstado() == Albaran.EstadoAlbaran.FACTURADO && 
            nuevoEstado != Albaran.EstadoAlbaran.FACTURADO) {
            throw new RuntimeException("No se puede cambiar el estado de un albarán facturado");
        }
        
        albaran.setEstado(nuevoEstado);
        albaranRepository.save(albaran);
    }
    
    @Transactional
    public void delete(Long id) {
        Albaran albaran = albaranRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Albarán no encontrado: " + id));
        
        if (albaran.getEstado() == Albaran.EstadoAlbaran.FACTURADO) {
            throw new RuntimeException("No se puede eliminar un albarán facturado");
        }
        
        albaranRepository.delete(albaran);
    }
    
    private String generarNumeroAlbaran() {
        String year = String.valueOf(LocalDate.now().getYear());
        Integer maxNumero = albaranRepository.findMaxNumeroByYear(year);
        int siguiente = (maxNumero != null ? maxNumero : 0) + 1;
        return String.format("ALB-%s-%04d", year, siguiente);
    }
    
    private AlbaranDTO convertToDTO(Albaran albaran) {
        AlbaranDTO dto = new AlbaranDTO();
        dto.setId(albaran.getId());
        dto.setNumero(albaran.getNumero());
        dto.setClienteId(albaran.getCliente().getId());
        dto.setClienteNombre(albaran.getCliente().getNombre());
        dto.setFecha(albaran.getFecha());
        dto.setBaseImponible(albaran.getBaseImponible());
        dto.setIva(albaran.getIva());
        dto.setTotal(albaran.getTotal());
        dto.setEstado(albaran.getEstado().name());
        dto.setFacturaId(albaran.getFactura() != null ? albaran.getFactura().getId() : null);
        
        List<LineaAlbaranDTO> lineasDTO = albaran.getLineas().stream()
            .map(this::convertLineaToDTO)
            .collect(Collectors.toList());
        dto.setLineas(lineasDTO);
        
        return dto;
    }
    
    private LineaAlbaranDTO convertLineaToDTO(LineaAlbaran linea) {
        LineaAlbaranDTO dto = new LineaAlbaranDTO();
        dto.setId(linea.getId());
        dto.setConcepto(linea.getConcepto());
        dto.setCantidad(linea.getCantidad());
        dto.setPrecioUnitario(linea.getPrecioUnitario());
        dto.setTipoIva(linea.getTipoIva());
        dto.setSubtotal(linea.getSubtotal());
        dto.setIva(linea.getIva());
        dto.setTotal(linea.getTotal());
        return dto;
    }
}
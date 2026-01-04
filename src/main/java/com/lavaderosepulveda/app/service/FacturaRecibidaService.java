package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.dto.FacturaRecibidaDTO;
import com.lavaderosepulveda.app.model.*;
import com.lavaderosepulveda.app.repository.FacturaRecibidaRepository;
import com.lavaderosepulveda.app.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacturaRecibidaService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private FacturaRecibidaRepository facturaRecibidaRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    public List<FacturaRecibidaDTO> listarTodas() {
        return facturaRecibidaRepository.findByOrderByFechaFacturaDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<FacturaRecibidaDTO> listarPorEstado(String estado) {
        EstadoFactura estadoEnum = EstadoFactura.valueOf(estado);
        return facturaRecibidaRepository.findByEstadoOrderByFechaFacturaDesc(estadoEnum)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<FacturaRecibidaDTO> listarPendientes() {
        return facturaRecibidaRepository.findPendientes()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<FacturaRecibidaDTO> listarVencidas() {
        return facturaRecibidaRepository.findVencidas(LocalDate.now())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<FacturaRecibidaDTO> listarPorProveedor(Long proveedorId) {
        return facturaRecibidaRepository.findByProveedorIdOrderByFechaFacturaDesc(proveedorId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<FacturaRecibidaDTO> listarPorCategoria(String categoria) {
        CategoriaGasto categoriaEnum = CategoriaGasto.valueOf(categoria);
        return facturaRecibidaRepository.findByCategoriaOrderByFechaFacturaDesc(categoriaEnum)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<FacturaRecibidaDTO> listarPorPeriodo(String fechaInicio, String fechaFin) {
        LocalDate inicio = LocalDate.parse(fechaInicio, FORMATO_FECHA);
        LocalDate fin = LocalDate.parse(fechaFin, FORMATO_FECHA);
        return facturaRecibidaRepository.findByFechaFacturaBetween(inicio, fin)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<FacturaRecibidaDTO> listarPorMes(int year, int month) {
        return facturaRecibidaRepository.findByMes(year, month)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<FacturaRecibidaDTO> buscar(String termino) {
        return facturaRecibidaRepository.buscar(termino)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public FacturaRecibidaDTO obtenerPorId(Long id) {
        return facturaRecibidaRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    public FacturaRecibidaDTO crear(FacturaRecibidaDTO dto) {
        FacturaRecibida factura = new FacturaRecibida();
        actualizarDesdeDTO(factura, dto);
        return toDTO(facturaRecibidaRepository.save(factura));
    }

    public FacturaRecibidaDTO actualizar(Long id, FacturaRecibidaDTO dto) {
        FacturaRecibida factura = facturaRecibidaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        actualizarDesdeDTO(factura, dto);
        return toDTO(facturaRecibidaRepository.save(factura));
    }

    public FacturaRecibidaDTO marcarPagada(Long id, String metodoPago) {
        FacturaRecibida factura = facturaRecibidaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        factura.setEstado(EstadoFactura.PAGADA);
        factura.setFechaPago(LocalDate.now());
        if (metodoPago != null && !metodoPago.isEmpty()) {
            factura.setMetodoPago(MetodoPago.valueOf(metodoPago));
        }
        return toDTO(facturaRecibidaRepository.save(factura));
    }

    public void eliminar(Long id) {
        facturaRecibidaRepository.deleteById(id);
    }

    // Resúmenes
    public BigDecimal totalPorPeriodo(String fechaInicio, String fechaFin) {
        LocalDate inicio = LocalDate.parse(fechaInicio, FORMATO_FECHA);
        LocalDate fin = LocalDate.parse(fechaFin, FORMATO_FECHA);
        BigDecimal total = facturaRecibidaRepository.totalPorPeriodo(inicio, fin);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal totalIvaSoportado(String fechaInicio, String fechaFin) {
        LocalDate inicio = LocalDate.parse(fechaInicio, FORMATO_FECHA);
        LocalDate fin = LocalDate.parse(fechaFin, FORMATO_FECHA);
        BigDecimal total = facturaRecibidaRepository.totalIvaSoportado(inicio, fin);
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<Object[]> resumenPorCategoria(String fechaInicio, String fechaFin) {
        LocalDate inicio = LocalDate.parse(fechaInicio, FORMATO_FECHA);
        LocalDate fin = LocalDate.parse(fechaFin, FORMATO_FECHA);
        return facturaRecibidaRepository.resumenPorCategoria(inicio, fin);
    }

    public List<Object[]> resumenPorProveedor(String fechaInicio, String fechaFin) {
        LocalDate inicio = LocalDate.parse(fechaInicio, FORMATO_FECHA);
        LocalDate fin = LocalDate.parse(fechaFin, FORMATO_FECHA);
        return facturaRecibidaRepository.resumenPorProveedor(inicio, fin);
    }

    private void actualizarDesdeDTO(FacturaRecibida factura, FacturaRecibidaDTO dto) {
        factura.setNumeroFactura(dto.getNumeroFactura());
        
        // Proveedor
        if (dto.getProveedorId() != null) {
            Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId()).orElse(null);
            factura.setProveedor(proveedor);
        } else {
            factura.setProveedorNombre(dto.getProveedorNombre());
            factura.setProveedorNif(dto.getProveedorNif());
        }
        
        // Fechas
        if (dto.getFechaFactura() != null && !dto.getFechaFactura().isEmpty()) {
            factura.setFechaFactura(LocalDate.parse(dto.getFechaFactura(), FORMATO_FECHA));
        }
        if (dto.getFechaVencimiento() != null && !dto.getFechaVencimiento().isEmpty()) {
            factura.setFechaVencimiento(LocalDate.parse(dto.getFechaVencimiento(), FORMATO_FECHA));
        }
        if (dto.getFechaPago() != null && !dto.getFechaPago().isEmpty()) {
            factura.setFechaPago(LocalDate.parse(dto.getFechaPago(), FORMATO_FECHA));
        }
        
        // Categoría
        if (dto.getCategoria() != null && !dto.getCategoria().isEmpty()) {
            factura.setCategoria(CategoriaGasto.valueOf(dto.getCategoria()));
        }
        
        factura.setConcepto(dto.getConcepto());
        factura.setBaseImponible(dto.getBaseImponible());
        
        if (dto.getTipoIva() != null) {
            factura.setTipoIva(dto.getTipoIva());
        }
        if (dto.getTipoIrpf() != null) {
            factura.setTipoIrpf(dto.getTipoIrpf());
        }
        
        // Estado
        if (dto.getEstado() != null && !dto.getEstado().isEmpty()) {
            factura.setEstado(EstadoFactura.valueOf(dto.getEstado()));
        }
        
        // Método de pago
        if (dto.getMetodoPago() != null && !dto.getMetodoPago().isEmpty()) {
            factura.setMetodoPago(MetodoPago.valueOf(dto.getMetodoPago()));
        }
        
        factura.setDocumentoAdjunto(dto.getDocumentoAdjunto());
        factura.setNotas(dto.getNotas());
    }

    private FacturaRecibidaDTO toDTO(FacturaRecibida factura) {
        FacturaRecibidaDTO dto = new FacturaRecibidaDTO();
        dto.setId(factura.getId());
        dto.setNumeroFactura(factura.getNumeroFactura());
        
        if (factura.getProveedor() != null) {
            dto.setProveedorId(factura.getProveedor().getId());
            dto.setProveedorNombre(factura.getProveedor().getNombre());
            dto.setProveedorNif(factura.getProveedor().getNif());
        } else {
            dto.setProveedorNombre(factura.getProveedorNombre());
            dto.setProveedorNif(factura.getProveedorNif());
        }
        
        if (factura.getFechaFactura() != null) {
            dto.setFechaFactura(factura.getFechaFactura().format(FORMATO_FECHA));
        }
        if (factura.getFechaVencimiento() != null) {
            dto.setFechaVencimiento(factura.getFechaVencimiento().format(FORMATO_FECHA));
        }
        if (factura.getFechaPago() != null) {
            dto.setFechaPago(factura.getFechaPago().format(FORMATO_FECHA));
        }
        
        if (factura.getCategoria() != null) {
            dto.setCategoria(factura.getCategoria().name());
        }
        
        dto.setConcepto(factura.getConcepto());
        dto.setBaseImponible(factura.getBaseImponible());
        dto.setTipoIva(factura.getTipoIva());
        dto.setCuotaIva(factura.getCuotaIva());
        dto.setTipoIrpf(factura.getTipoIrpf());
        dto.setCuotaIrpf(factura.getCuotaIrpf());
        dto.setTotal(factura.getTotal());
        
        if (factura.getEstado() != null) {
            dto.setEstado(factura.getEstado().name());
        }
        if (factura.getMetodoPago() != null) {
            dto.setMetodoPago(factura.getMetodoPago().name());
        }
        
        dto.setDocumentoAdjunto(factura.getDocumentoAdjunto());
        dto.setNotas(factura.getNotas());
        
        return dto;
    }
}

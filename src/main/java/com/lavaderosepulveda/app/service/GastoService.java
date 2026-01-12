package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.dto.GastoDTO;
import com.lavaderosepulveda.app.model.*;
import com.lavaderosepulveda.app.model.enums.CategoriaGasto;
import com.lavaderosepulveda.app.model.enums.MetodoPago;
import com.lavaderosepulveda.app.repository.FacturaRecibidaRepository;
import com.lavaderosepulveda.app.repository.GastoRepository;
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
public class GastoService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private GastoRepository gastoRepository;

    @Autowired
    private FacturaRecibidaRepository facturaRecibidaRepository;

    public List<GastoDTO> listarTodos() {
        return gastoRepository.findByOrderByFechaDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<GastoDTO> listarPorCategoria(String categoria) {
        CategoriaGasto categoriaEnum = CategoriaGasto.valueOf(categoria);
        return gastoRepository.findByCategoriaOrderByFechaDesc(categoriaEnum)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<GastoDTO> listarRecurrentes() {
        return gastoRepository.findByRecurrenteTrueOrderByConceptoAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<GastoDTO> listarPorPeriodo(String fechaInicio, String fechaFin) {
        LocalDate inicio = LocalDate.parse(fechaInicio, FORMATO_FECHA);
        LocalDate fin = LocalDate.parse(fechaFin, FORMATO_FECHA);
        return gastoRepository.findByFechaBetween(inicio, fin)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<GastoDTO> listarPorMes(int year, int month) {
        return gastoRepository.findByMes(year, month)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<GastoDTO> listarPendientesPago() {
        return gastoRepository.findPendientesPago()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<GastoDTO> buscar(String termino) {
        return gastoRepository.buscar(termino)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public GastoDTO obtenerPorId(Long id) {
        return gastoRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    public GastoDTO crear(GastoDTO dto) {
        Gasto gasto = new Gasto();
        actualizarDesdeDTO(gasto, dto);
        return toDTO(gastoRepository.save(gasto));
    }

    public GastoDTO actualizar(Long id, GastoDTO dto) {
        Gasto gasto = gastoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto no encontrado"));
        actualizarDesdeDTO(gasto, dto);
        return toDTO(gastoRepository.save(gasto));
    }

    public GastoDTO marcarPagado(Long id, String metodoPago) {
        Gasto gasto = gastoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto no encontrado"));
        gasto.setPagado(true);
        if (metodoPago != null && !metodoPago.isEmpty()) {
            gasto.setMetodoPago(MetodoPago.valueOf(metodoPago));
        }
        return toDTO(gastoRepository.save(gasto));
    }

    public void eliminar(Long id) {
        gastoRepository.deleteById(id);
    }

    // Generar gastos recurrentes para el mes actual
    public List<GastoDTO> generarRecurrentesMes() {
        LocalDate hoy = LocalDate.now();
        int diaActual = hoy.getDayOfMonth();
        
        List<Gasto> recurrentes = gastoRepository.findRecurrentesPorDia(diaActual);
        
        return recurrentes.stream()
                .map(recurrente -> {
                    Gasto nuevoGasto = new Gasto();
                    nuevoGasto.setConcepto(recurrente.getConcepto());
                    nuevoGasto.setFecha(hoy);
                    nuevoGasto.setCategoria(recurrente.getCategoria());
                    nuevoGasto.setImporte(recurrente.getImporte());
                    nuevoGasto.setIvaIncluido(recurrente.getIvaIncluido());
                    nuevoGasto.setMetodoPago(recurrente.getMetodoPago());
                    nuevoGasto.setNotas("Generado automáticamente desde gasto recurrente #" + recurrente.getId());
                    nuevoGasto.setPagado(false);
                    nuevoGasto.setRecurrente(false);
                    return toDTO(gastoRepository.save(nuevoGasto));
                })
                .collect(Collectors.toList());
    }

    // Resúmenes
    public BigDecimal totalPorPeriodo(String fechaInicio, String fechaFin) {
        LocalDate inicio = LocalDate.parse(fechaInicio, FORMATO_FECHA);
        LocalDate fin = LocalDate.parse(fechaFin, FORMATO_FECHA);
        BigDecimal total = gastoRepository.totalPorPeriodo(inicio, fin);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal totalIvaSoportado(String fechaInicio, String fechaFin) {
        LocalDate inicio = LocalDate.parse(fechaInicio, FORMATO_FECHA);
        LocalDate fin = LocalDate.parse(fechaFin, FORMATO_FECHA);
        BigDecimal total = gastoRepository.totalIvaSoportado(inicio, fin);
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<Object[]> resumenPorCategoria(String fechaInicio, String fechaFin) {
        LocalDate inicio = LocalDate.parse(fechaInicio, FORMATO_FECHA);
        LocalDate fin = LocalDate.parse(fechaFin, FORMATO_FECHA);
        return gastoRepository.resumenPorCategoria(inicio, fin);
    }

    public List<Object[]> evolucionMensual(String fechaInicio, String fechaFin) {
        LocalDate inicio = LocalDate.parse(fechaInicio, FORMATO_FECHA);
        LocalDate fin = LocalDate.parse(fechaFin, FORMATO_FECHA);
        return gastoRepository.evolucionMensual(inicio, fin);
    }

    private void actualizarDesdeDTO(Gasto gasto, GastoDTO dto) {
        gasto.setConcepto(dto.getConcepto());
        
        if (dto.getFecha() != null && !dto.getFecha().isEmpty()) {
            gasto.setFecha(LocalDate.parse(dto.getFecha(), FORMATO_FECHA));
        }
        
        if (dto.getCategoria() != null && !dto.getCategoria().isEmpty()) {
            gasto.setCategoria(CategoriaGasto.valueOf(dto.getCategoria()));
        }
        
        gasto.setImporte(dto.getImporte());
        
        if (dto.getIvaIncluido() != null) {
            gasto.setIvaIncluido(dto.getIvaIncluido());
        }
        
        // Vinculación con factura recibida
        if (dto.getFacturaRecibidaId() != null) {
            FacturaRecibida factura = facturaRecibidaRepository.findById(dto.getFacturaRecibidaId()).orElse(null);
            gasto.setFacturaRecibida(factura);
        }
        
        if (dto.getMetodoPago() != null && !dto.getMetodoPago().isEmpty()) {
            gasto.setMetodoPago(MetodoPago.valueOf(dto.getMetodoPago()));
        }
        
        if (dto.getRecurrente() != null) {
            gasto.setRecurrente(dto.getRecurrente());
        }
        
        gasto.setDiaRecurrencia(dto.getDiaRecurrencia());
        gasto.setNotas(dto.getNotas());
        
        if (dto.getPagado() != null) {
            gasto.setPagado(dto.getPagado());
        }
    }

    private GastoDTO toDTO(Gasto gasto) {
        GastoDTO dto = new GastoDTO();
        dto.setId(gasto.getId());
        dto.setConcepto(gasto.getConcepto());
        
        if (gasto.getFecha() != null) {
            dto.setFecha(gasto.getFecha().format(FORMATO_FECHA));
        }
        
        if (gasto.getCategoria() != null) {
            dto.setCategoria(gasto.getCategoria().name());
        }
        
        dto.setImporte(gasto.getImporte());
        dto.setIvaIncluido(gasto.getIvaIncluido());
        dto.setBaseImponible(gasto.getBaseImponible());
        dto.setCuotaIva(gasto.getCuotaIva());
        
        if (gasto.getFacturaRecibida() != null) {
            dto.setFacturaRecibidaId(gasto.getFacturaRecibida().getId());
        }
        
        if (gasto.getMetodoPago() != null) {
            dto.setMetodoPago(gasto.getMetodoPago().name());
        }
        
        dto.setRecurrente(gasto.getRecurrente());
        dto.setDiaRecurrencia(gasto.getDiaRecurrencia());
        dto.setNotas(gasto.getNotas());
        dto.setPagado(gasto.getPagado());
        
        return dto;
    }
}

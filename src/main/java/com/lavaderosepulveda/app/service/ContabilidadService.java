package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.dto.ContabilidadResumenDTO;
import com.lavaderosepulveda.app.model.Factura;
import com.lavaderosepulveda.app.repository.FacturaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContabilidadService {
    private static final Logger log = LoggerFactory.getLogger(ContabilidadService.class);

    @Autowired
    private FacturaRepository facturaRepository;

    /**
     * Genera el resumen contable para un rango de fechas
     */
    public ContabilidadResumenDTO generarResumen(LocalDate desde, LocalDate hasta) {
        try {
            log.info("Generando resumen contable: {} a {}", desde, hasta);
            
            // Obtener facturas del período
            List<Factura> facturas = facturaRepository.findByFechaBetweenOrderByFechaDesc(desde, hasta);
            
            if (facturas.isEmpty()) {
                log.warn("No hay facturas en el período {} a {}", desde, hasta);
                ContabilidadResumenDTO resultado = new ContabilidadResumenDTO();
                resultado.setIngresosTotales(BigDecimal.ZERO);
                resultado.setBaseImponible(BigDecimal.ZERO);
                resultado.setIvaRepercutido(BigDecimal.ZERO);
                resultado.setNumFacturas(0);
                resultado.setResumenMensual(new ArrayList<>());
                resultado.setResumenCliente(new ArrayList<>());
                return resultado;
            }

            // Calcular totales
            BigDecimal ingresosTotales = facturas.stream()
                    .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal baseImponible = facturas.stream()
                    .map(f -> f.getBaseImponible() != null ? f.getBaseImponible() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal ivaRepercutido = facturas.stream()
                    .map(f -> f.getImporteIva() != null ? f.getImporteIva() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Agrupar por mes
            Map<YearMonth, List<Factura>> facturasPorMes = facturas.stream()
                    .collect(Collectors.groupingBy(f -> YearMonth.from(f.getFecha())));

            // Generar resumen mensual
            List<ContabilidadResumenDTO.ResumenMensualDTO> resumenMensual = facturasPorMes.entrySet()
                    .stream()
                    .sorted((a, b) -> b.getKey().compareTo(a.getKey()))
                    .map(entry -> {
                        YearMonth mes = entry.getKey();
                        List<Factura> facturasDelMes = entry.getValue();
                        
                        BigDecimal base = facturasDelMes.stream()
                                .map(f -> f.getBaseImponible() != null ? f.getBaseImponible() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        
                        BigDecimal iva = facturasDelMes.stream()
                                .map(f -> f.getImporteIva() != null ? f.getImporteIva() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        
                        BigDecimal total = facturasDelMes.stream()
                                .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        
                        ContabilidadResumenDTO.ResumenMensualDTO item = new ContabilidadResumenDTO.ResumenMensualDTO();
                        item.setMes(mes.atDay(1).toString());
                        item.setBase(base);
                        item.setIva(iva);
                        item.setTotal(total);
                        item.setNumFacturas(facturasDelMes.size());
                        
                        return item;
                    })
                    .collect(Collectors.toList());

            // Agrupar por cliente
            Map<String, List<Factura>> facturasPorCliente = facturas.stream()
                    .collect(Collectors.groupingBy(f -> f.getCliente() != null && f.getCliente().getNombre() != null 
                            ? f.getCliente().getNombre() : "Sin especificar"));

            List<ContabilidadResumenDTO.ResumenClienteDTO> resumenCliente = facturasPorCliente.entrySet()
                    .stream()
                    .map(entry -> {
                        String cliente = entry.getKey();
                        List<Factura> facturasDelCliente = entry.getValue();
                        
                        BigDecimal total = facturasDelCliente.stream()
                                .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        
                        ContabilidadResumenDTO.ResumenClienteDTO item = new ContabilidadResumenDTO.ResumenClienteDTO();
                        item.setNombreCliente(cliente);
                        item.setTotal(total);
                        item.setNumFacturas(facturasDelCliente.size());
                        
                        return item;
                    })
                    .sorted((a, b) -> b.getTotal().compareTo(a.getTotal()))
                    .collect(Collectors.toList());

            ContabilidadResumenDTO resultado = new ContabilidadResumenDTO();
            resultado.setIngresosTotales(ingresosTotales);
            resultado.setBaseImponible(baseImponible);
            resultado.setIvaRepercutido(ivaRepercutido);
            resultado.setNumFacturas(facturas.size());
            resultado.setResumenMensual(resumenMensual);
            resultado.setResumenCliente(resumenCliente);
            
            return resultado;
        } catch (Exception e) {
            log.error("Error generando resumen contable", e);
            throw new RuntimeException("Error al generar resumen contable: " + e.getMessage());
        }
    }

    /**
     * Genera archivo Excel con el resumen
     */
    public byte[] generarExcel(LocalDate desde, LocalDate hasta) throws IOException {
        log.info("Generando Excel de contabilidad");
        return "Excel export pending".getBytes();
    }

    /**
     * Genera archivo PDF con el resumen
     */
    public byte[] generarPdf(LocalDate desde, LocalDate hasta) throws IOException {
        log.info("Generando PDF de contabilidad");
        return "PDF export pending".getBytes();
    }
}

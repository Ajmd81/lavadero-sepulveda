package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.dto.Modelo130DTO;
import com.lavaderosepulveda.app.dto.Modelo303DTO;
import com.lavaderosepulveda.app.model.Factura;
import com.lavaderosepulveda.app.model.FacturaRecibida;
import com.lavaderosepulveda.app.model.Gasto;
import com.lavaderosepulveda.app.repository.FacturaRecibidaRepository;
import com.lavaderosepulveda.app.repository.FacturaRepository;
import com.lavaderosepulveda.app.repository.GastoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Servicio para calcular Modelos Fiscales (303, 130).
 * Lógica portada desde CRM Desktop (ModelosFiscalesService.java).
 */
@Service
public class ModelosFiscalesService {

    private static final Logger log = LoggerFactory.getLogger(ModelosFiscalesService.class);

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private FacturaRecibidaRepository facturaRecibidaRepository;

    @Autowired
    private GastoRepository gastoRepository;

    // Datos Emisor desde configuración
    @Value("${app.fiscal.nif}")
    private String nifEmisor;

    @Value("${app.fiscal.nombre}")
    private String nombreEmisor;

    // ========================================
    // MODELO 303 - IVA TRIMESTRAL
    // ========================================
    public Modelo303DTO generarModelo303(int year, int trimestre) {
        log.info("Generando Modelo 303 para {}T{}", trimestre, year);

        LocalDate inicio = getInicioTrimestre(year, trimestre);
        LocalDate fin = getFinTrimestre(year, trimestre);

        // Obtener datos
        List<Factura> ventas = facturaRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin);
        List<Gasto> gastos = gastoRepository.findByFechaBetween(inicio, fin);
        List<FacturaRecibida> compras = facturaRecibidaRepository.findByFechaFacturaBetween(inicio, fin);

        // Calcular IVA Devengado (Ventas)
        BigDecimal baseImponible21 = BigDecimal.ZERO;
        BigDecimal cuotaDevengada21 = BigDecimal.ZERO;

        for (Factura f : ventas) {
            if (f.getBaseImponible() != null) {
                baseImponible21 = baseImponible21.add(f.getBaseImponible());
            }
            // En Backend Factura tiene 'importeIva'
            if (f.getImporteIva() != null) {
                cuotaDevengada21 = cuotaDevengada21.add(f.getImporteIva());
            }
        }

        // Calcular IVA Deducible (Gastos + Compras)
        BigDecimal baseDeducible = BigDecimal.ZERO;
        BigDecimal cuotaDeducible = BigDecimal.ZERO;

        for (Gasto g : gastos) {
            if (g.getBaseImponible() != null) {
                baseDeducible = baseDeducible.add(g.getBaseImponible());
            }
            if (g.getCuotaIva() != null) {
                cuotaDeducible = cuotaDeducible.add(g.getCuotaIva());
            }
        }

        for (FacturaRecibida fr : compras) {
            if (fr.getBaseImponible() != null) {
                baseDeducible = baseDeducible.add(fr.getBaseImponible());
            }
            if (fr.getCuotaIva() != null) {
                cuotaDeducible = cuotaDeducible.add(fr.getCuotaIva());
            }
        }

        Modelo303DTO modelo = Modelo303DTO.builder()
                .ejercicio(String.valueOf(year))
                .periodo(trimestre + "T")
                .nif(nifEmisor)
                .nombreRazonSocial(nombreEmisor)
                .baseImponible21(baseImponible21)
                .cuotaDevengada21(cuotaDevengada21)
                .baseImponible10(BigDecimal.ZERO)
                .cuotaDevengada10(BigDecimal.ZERO)
                .baseImponible4(BigDecimal.ZERO)
                .cuotaDevengada4(BigDecimal.ZERO)
                .baseDeducibleInteriores(baseDeducible)
                .cuotaDeducibleInteriores(cuotaDeducible)
                .baseDeducibleInversion(BigDecimal.ZERO)
                .cuotaDeducibleInversion(BigDecimal.ZERO)
                .baseDeducibleImportaciones(BigDecimal.ZERO)
                .cuotaDeducibleImportaciones(BigDecimal.ZERO)
                .cuotasCompensar(BigDecimal.ZERO)
                .numFacturasEmitidas(ventas.size())
                .numFacturasRecibidas(gastos.size() + compras.size())
                .build();

        modelo.calcularResultado();
        return modelo;
    }

    // ========================================
    // MODELO 130 - IRPF TRIMESTRAL
    // ========================================
    public Modelo130DTO generarModelo130(int year, int trimestre) {
        log.info("Generando Modelo 130 para {}T{}", trimestre, year);

        // ACUMULADO desde 1 de enero
        LocalDate inicioAno = LocalDate.of(year, 1, 1);
        LocalDate finTrimestre = getFinTrimestre(year, trimestre);

        List<Factura> ventas = facturaRepository.findByFechaBetweenOrderByFechaDesc(inicioAno, finTrimestre);
        List<Gasto> gastos = gastoRepository.findByFechaBetween(inicioAno, finTrimestre);
        List<FacturaRecibida> compras = facturaRecibidaRepository.findByFechaFacturaBetween(inicioAno, finTrimestre);

        // Ingresos acumulados
        BigDecimal ingresos = ventas.stream()
                .map(f -> f.getBaseImponible() != null ? f.getBaseImponible() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Gastos acumulados
        BigDecimal gastosTotal = gastos.stream()
                .map(g -> g.getImporte() != null ? g.getImporte() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal comprasTotal = compras.stream()
                .map(fr -> fr.getTotal() != null ? fr.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gastosDeducibles = gastosTotal.add(comprasTotal);

        // Datos solo trimestre actual (para info)
        LocalDate inicioTrimestre = getInicioTrimestre(year, trimestre);
        List<Factura> ventasTrimestre = facturaRepository.findByFechaBetweenOrderByFechaDesc(inicioTrimestre,
                finTrimestre);
        BigDecimal ingresosTrimestre = ventasTrimestre.stream()
                .map(f -> f.getBaseImponible() != null ? f.getBaseImponible() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Modelo130DTO modelo = Modelo130DTO.builder()
                .ejercicio(String.valueOf(year))
                .periodo(trimestre + "T")
                .nif(nifEmisor)
                .nombreApellidos(nombreEmisor)
                .ingresosComputables(ingresos)
                .gastosDeducibles(gastosDeducibles)
                .retencionesIngresoCuenta(BigDecimal.ZERO)
                .pagosFraccionadosAnteriores(BigDecimal.ZERO) // TODO: Calcular anteriores
                .aDeducir(BigDecimal.ZERO)
                .ingresosTrimestre(ingresosTrimestre)
                .numFacturasEmitidas(ventas.size())
                .numGastosRegistrados(gastos.size() + compras.size())
                .build();

        modelo.calcularPagoFraccionado();
        return modelo;
    }

    // ========================================
    // MÉTODOS AUXILIARES
    // ========================================

    private LocalDate getInicioTrimestre(int year, int trimestre) {
        int mes = (trimestre - 1) * 3 + 1;
        return LocalDate.of(year, mes, 1);
    }

    private LocalDate getFinTrimestre(int year, int trimestre) {
        int mes = trimestre * 3;
        return YearMonth.of(year, mes).atEndOfMonth();
    }
}
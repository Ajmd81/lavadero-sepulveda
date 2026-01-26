package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.dto.ResumenFinancieroDTO;
import com.lavaderosepulveda.app.dto.ResumenFinancieroDTO.CategoriaResumenDTO;
import com.lavaderosepulveda.app.dto.ResumenFinancieroDTO.MesResumenDTO;
import com.lavaderosepulveda.app.model.Factura;
import com.lavaderosepulveda.app.model.FacturaRecibida;
import com.lavaderosepulveda.app.model.Gasto;
import com.lavaderosepulveda.app.model.enums.EstadoFactura;
import com.lavaderosepulveda.app.repository.FacturaRepository;
import com.lavaderosepulveda.app.repository.FacturaRecibidaRepository;
import com.lavaderosepulveda.app.repository.GastoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResumenFinancieroService {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private FacturaRecibidaRepository facturaRecibidaRepository;

    @Autowired
    private GastoRepository gastoRepository;

    public ResumenFinancieroDTO generarResumen(LocalDate desde, LocalDate hasta) {
        ResumenFinancieroDTO resumen = new ResumenFinancieroDTO();

        // Obtener datos
        List<Factura> facturasEmitidas = facturaRepository.findByFechaBetween(desde, hasta);
        List<FacturaRecibida> facturasRecibidas = facturaRecibidaRepository
                .findByFechaFacturaBetweenOrderByFechaFacturaDesc(desde, hasta);
        List<Gasto> gastos = gastoRepository.findByFechaBetween(desde, hasta);

        // ========== INGRESOS (Facturas Emitidas) ==========
        BigDecimal totalIngresos = facturasEmitidas.stream()
                .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ivaRepercutido = facturasEmitidas.stream()
                .map(f -> f.getImporteIva() != null ? f.getImporteIva() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendientesCobro = facturasEmitidas.stream()
                .filter(f -> f.getEstado() == EstadoFactura.PENDIENTE)
                .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        resumen.setTotalFacturasEmitidas(totalIngresos);
        resumen.setNumFacturasEmitidas(facturasEmitidas.size());
        resumen.setTotalIvaRepercutido(ivaRepercutido);
        resumen.setFacturasPendientesCobro(pendientesCobro);
        resumen.setIvaRepercutido(ivaRepercutido);

        // ========== GASTOS (Facturas Recibidas + Gastos) ==========
        BigDecimal totalFacturasRecibidas = facturasRecibidas.stream()
                .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGastos = gastos.stream()
                .map(g -> g.getImporte() != null ? g.getImporte() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ivaSoportadoFacturas = facturasRecibidas.stream()
                .map(f -> f.getCuotaIva() != null ? f.getCuotaIva() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ivaSoportadoGastos = gastos.stream()
                .map(g -> g.getCuotaIva() != null ? g.getCuotaIva() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ivaSoportado = ivaSoportadoFacturas.add(ivaSoportadoGastos);

        BigDecimal pendientesPago = facturasRecibidas.stream()
                .filter(f -> "PENDIENTE".equals(f.getEstado()))
                .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        resumen.setTotalFacturasRecibidas(totalFacturasRecibidas);
        resumen.setTotalGastos(totalGastos);
        resumen.setNumFacturasRecibidas(facturasRecibidas.size());
        resumen.setNumGastos(gastos.size());
        resumen.setTotalIvaSoportado(ivaSoportado);
        resumen.setFacturasPendientesPago(pendientesPago);
        resumen.setIvaSoportado(ivaSoportado);

        // ========== RESULTADO ==========
        resumen.setTotalIngresos(totalIngresos);
        BigDecimal totalGastosGeneral = totalFacturasRecibidas.add(totalGastos);
        resumen.setTotalGastosGeneral(totalGastosGeneral);

        BigDecimal beneficio = totalIngresos.subtract(totalGastosGeneral);
        resumen.setBeneficioBruto(beneficio);

        // Liquidación IVA
        BigDecimal resultadoIva = ivaRepercutido.subtract(ivaSoportado);
        resumen.setResultadoIva(resultadoIva);

        // ========== GASTOS POR CATEGORÍA ==========
        Map<String, BigDecimal[]> porCategoria = new LinkedHashMap<>();

        // Facturas recibidas
        for (FacturaRecibida f : facturasRecibidas) {
            String cat = f.getCategoria() != null ? f.getCategoria().name() : "OTROS";
            porCategoria.computeIfAbsent(cat, k -> new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
            porCategoria.get(cat)[0] = porCategoria.get(cat)[0]
                    .add(f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO);
            porCategoria.get(cat)[1] = porCategoria.get(cat)[1].add(BigDecimal.ONE);
        }

        // Gastos
        for (Gasto g : gastos) {
            String cat = g.getCategoria() != null ? g.getCategoria().name() : "OTROS";
            porCategoria.computeIfAbsent(cat, k -> new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
            porCategoria.get(cat)[0] = porCategoria.get(cat)[0]
                    .add(g.getImporte() != null ? g.getImporte() : BigDecimal.ZERO);
            porCategoria.get(cat)[1] = porCategoria.get(cat)[1].add(BigDecimal.ONE);
        }

        List<CategoriaResumenDTO> categoriasDTO = porCategoria.entrySet().stream()
                .map(entry -> new CategoriaResumenDTO(
                        entry.getKey(),
                        formatearCategoria(entry.getKey()),
                        entry.getValue()[1].intValue(),
                        entry.getValue()[0]))
                .sorted((a, b) -> b.getTotal().compareTo(a.getTotal()))
                .collect(Collectors.toList());

        resumen.setGastosPorCategoria(categoriasDTO);

        // ========== EVOLUCIÓN MENSUAL ==========
        Map<YearMonth, BigDecimal[]> porMes = new TreeMap<>();

        LocalDate mes = desde.withDayOfMonth(1);
        while (!mes.isAfter(hasta)) {
            YearMonth ym = YearMonth.from(mes);
            porMes.put(ym, new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
            mes = mes.plusMonths(1);
        }

        // Ingresos por mes
        for (Factura f : facturasEmitidas) {
            YearMonth ym = YearMonth.from(f.getFecha());
            if (porMes.containsKey(ym)) {
                porMes.get(ym)[0] = porMes.get(ym)[0].add(f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO);
            }
        }

        // Gastos por mes
        for (FacturaRecibida f : facturasRecibidas) {
            YearMonth ym = YearMonth.from(f.getFechaFactura());
            if (porMes.containsKey(ym)) {
                porMes.get(ym)[1] = porMes.get(ym)[1].add(f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO);
            }
        }

        for (Gasto g : gastos) {
            YearMonth ym = YearMonth.from(g.getFecha());
            if (porMes.containsKey(ym)) {
                porMes.get(ym)[1] = porMes.get(ym)[1].add(g.getImporte() != null ? g.getImporte() : BigDecimal.ZERO);
            }
        }

        List<MesResumenDTO> evolucion = porMes.entrySet().stream()
                .map(entry -> {
                    MesResumenDTO dto = new MesResumenDTO();
                    dto.setYear(entry.getKey().getYear());
                    dto.setMes(entry.getKey().getMonthValue());
                    dto.setMesNombre(entry.getKey().getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "ES")));
                    dto.setIngresos(entry.getValue()[0]);
                    dto.setGastos(entry.getValue()[1]);
                    dto.setBeneficio(entry.getValue()[0].subtract(entry.getValue()[1]));
                    return dto;
                })
                .collect(Collectors.toList());

        resumen.setEvolucionMensual(evolucion);

        return resumen;
    }

    private String formatearCategoria(String categoria) {
        if (categoria == null)
            return "Otros";
        return switch (categoria) {
            case "AGUA" -> "Agua";
            case "LUZ" -> "Electricidad";
            case "GAS" -> "Gas";
            case "ALQUILER" -> "Alquiler";
            case "SEGUROS" -> "Seguros";
            case "SUMINISTROS" -> "Suministros";
            case "PRODUCTOS" -> "Productos de limpieza";
            case "MANTENIMIENTO" -> "Mantenimiento";
            case "REPARACIONES" -> "Reparaciones";
            case "COMBUSTIBLE" -> "Combustible";
            case "PERSONAL" -> "Personal";
            case "SEGURIDAD_SOCIAL" -> "Seguridad Social";
            case "IMPUESTOS" -> "Impuestos";
            case "TELEFONIA" -> "Telefonía/Internet";
            case "PUBLICIDAD" -> "Publicidad";
            case "MATERIAL_OFICINA" -> "Material de oficina";
            case "GESTORIA" -> "Gestoría";
            case "BANCARIOS" -> "Gastos bancarios";
            case "VEHICULOS" -> "Vehículos";
            case "MAQUINARIA" -> "Maquinaria";
            case "OTROS" -> "Otros";
            default -> categoria;
        };
    }
}

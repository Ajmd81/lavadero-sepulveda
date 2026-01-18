package com.lavaderosepulveda.crm.service;

import com.lavaderosepulveda.crm.api.service.FacturacionApiService;
import com.lavaderosepulveda.crm.model.dto.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Servicio para generar el Informe de Pérdidas y Ganancias
 */
@Slf4j
public class InformePyGService {

    private static InformePyGService instance;
    private final FacturacionApiService facturacionService;

    public static synchronized InformePyGService getInstance() {
        if (instance == null) {
            instance = new InformePyGService();
        }
        return instance;
    }

    private InformePyGService() {
        this.facturacionService = FacturacionApiService.getInstance();
    }

    /**
     * Genera el Informe de Pérdidas y Ganancias para un período
     */
    public InformePyG generarInforme(LocalDate desde, LocalDate hasta) {
        log.info("Generando Informe PyG del {} al {}", desde, hasta);

        InformePyG informe = new InformePyG();
        informe.setFechaDesde(desde);
        informe.setFechaHasta(hasta);
        informe.setEjercicio(desde.getYear());

        try {
            // Obtener datos usando los métodos existentes en FacturacionApiService
            List<FacturaEmitidaDTO> facturasEmitidas = facturacionService.obtenerFacturasEmitidasPorPeriodo(desde, hasta);
            List<FacturaRecibidaDTO> facturasRecibidas = facturacionService.obtenerFacturasRecibidasPorPeriodo(desde, hasta);
            List<GastoDTO> gastos = facturacionService.obtenerGastosPorPeriodo(desde, hasta);

            // 1. INGRESOS
            BigDecimal ventasBruto = facturasEmitidas.stream()
                    .map(f -> f.getBaseImponible() != null ? f.getBaseImponible() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            informe.setVentasNetas(ventasBruto);
            informe.setNumFacturasEmitidas(facturasEmitidas.size());
            informe.setOtrosIngresosExplotacion(BigDecimal.ZERO);

            // 2. GASTOS - Clasificar por categoría
            Map<String, BigDecimal> gastosPorCategoria = new HashMap<>();
            
            for (FacturaRecibidaDTO factura : facturasRecibidas) {
                String categoria = factura.getCategoria() != null ? factura.getCategoria() : "OTROS";
                BigDecimal base = factura.getBaseImponible() != null ? factura.getBaseImponible() : BigDecimal.ZERO;
                gastosPorCategoria.merge(categoria, base, BigDecimal::add);
            }

            for (GastoDTO gasto : gastos) {
                String categoria = gasto.getCategoria() != null ? gasto.getCategoria() : "OTROS";
                BigDecimal base = gasto.getBaseImponible() != null ? gasto.getBaseImponible() : 
                                  gasto.getImporte() != null ? gasto.getImporte() : BigDecimal.ZERO;
                gastosPorCategoria.merge(categoria, base, BigDecimal::add);
            }

            informe.setGastosPorCategoria(gastosPorCategoria);

            // Aprovisionamientos
            BigDecimal aprovisionamientos = sumarCategorias(gastosPorCategoria, 
                    "PRODUCTOS", "SUMINISTROS", "COMERCIALES");
            informe.setAprovisionamientos(aprovisionamientos);

            // Gastos de personal
            BigDecimal gastosPersonal = sumarCategorias(gastosPorCategoria, 
                    "PERSONAL", "SEGURIDAD_SOCIAL", "NOMINAS", "SALARIOS");
            informe.setGastosPersonal(gastosPersonal);

            // Servicios exteriores
            BigDecimal serviciosExteriores = sumarCategorias(gastosPorCategoria,
                    "AGUA", "LUZ", "GAS", "ALQUILER", "SEGUROS", "MANTENIMIENTO", 
                    "REPARACIONES", "TELEFONIA", "PUBLICIDAD", "GESTORIA", 
                    "MATERIAL_OFICINA", "COMBUSTIBLE", "VEHICULOS");
            informe.setServiciosExteriores(serviciosExteriores);

            // Tributos
            BigDecimal tributos = sumarCategorias(gastosPorCategoria, "IMPUESTOS");
            informe.setTributos(tributos);

            // Otros gastos
            BigDecimal otrosGastos = sumarCategorias(gastosPorCategoria, "BANCARIOS", "OTROS", "MAQUINARIA");
            informe.setOtrosGastosGestion(otrosGastos);

            informe.setOtrosGastosExplotacion(serviciosExteriores.add(tributos).add(otrosGastos));

            // Amortización
            BigDecimal amortizacion = sumarCategorias(gastosPorCategoria, "AMORTIZACION");
            informe.setAmortizacion(amortizacion);

            // RESULTADO DE EXPLOTACIÓN
            BigDecimal totalIngresosExplotacion = informe.getVentasNetas()
                    .add(informe.getOtrosIngresosExplotacion());

            BigDecimal totalGastosExplotacion = informe.getAprovisionamientos()
                    .add(informe.getGastosPersonal())
                    .add(informe.getOtrosGastosExplotacion())
                    .add(informe.getAmortizacion());

            informe.setResultadoExplotacion(totalIngresosExplotacion.subtract(totalGastosExplotacion));

            // RESULTADO FINANCIERO
            BigDecimal ingresosFinancieros = sumarCategorias(gastosPorCategoria, "INGRESOS_FINANCIEROS");
            informe.setIngresosFinancieros(ingresosFinancieros);

            BigDecimal gastosFinancieros = gastosPorCategoria.getOrDefault("INTERESES", BigDecimal.ZERO);
            informe.setGastosFinancieros(gastosFinancieros);

            informe.setResultadoFinanciero(ingresosFinancieros.subtract(gastosFinancieros));

            // RESULTADO ANTES DE IMPUESTOS
            informe.setResultadoAntesImpuestos(
                    informe.getResultadoExplotacion().add(informe.getResultadoFinanciero()));

            // IMPUESTO SOBRE BENEFICIOS
            informe.setImpuestoBeneficios(BigDecimal.ZERO);

            // RESULTADO DEL EJERCICIO
            informe.setResultadoEjercicio(
                    informe.getResultadoAntesImpuestos().subtract(informe.getImpuestoBeneficios()));

            // TOTALES
            informe.setTotalIngresos(totalIngresosExplotacion.add(ingresosFinancieros));
            informe.setTotalGastos(totalGastosExplotacion.add(gastosFinancieros));
            informe.setNumFacturasRecibidas(facturasRecibidas.size());
            informe.setNumGastos(gastos.size());

            // Margen
            if (informe.getTotalIngresos().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal margen = informe.getResultadoEjercicio()
                        .divide(informe.getTotalIngresos(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                informe.setMargenBeneficio(margen);
            }

            log.info("Informe PyG generado - Resultado: {}", informe.getResultadoEjercicio());

        } catch (Exception e) {
            log.error("Error generando informe PyG", e);
            throw new RuntimeException("Error al generar el informe: " + e.getMessage(), e);
        }

        return informe;
    }

    private BigDecimal sumarCategorias(Map<String, BigDecimal> gastos, String... categorias) {
        BigDecimal total = BigDecimal.ZERO;
        for (String cat : categorias) {
            total = total.add(gastos.getOrDefault(cat, BigDecimal.ZERO));
        }
        return total;
    }

    // ========================================
    // CLASE DE DATOS DEL INFORME
    // ========================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InformePyG {
        private LocalDate fechaDesde;
        private LocalDate fechaHasta;
        private int ejercicio;

        private BigDecimal ventasNetas = BigDecimal.ZERO;
        private BigDecimal otrosIngresosExplotacion = BigDecimal.ZERO;

        private BigDecimal aprovisionamientos = BigDecimal.ZERO;
        private BigDecimal gastosPersonal = BigDecimal.ZERO;
        private BigDecimal serviciosExteriores = BigDecimal.ZERO;
        private BigDecimal tributos = BigDecimal.ZERO;
        private BigDecimal otrosGastosGestion = BigDecimal.ZERO;
        private BigDecimal otrosGastosExplotacion = BigDecimal.ZERO;
        private BigDecimal amortizacion = BigDecimal.ZERO;

        private BigDecimal resultadoExplotacion = BigDecimal.ZERO;
        
        private BigDecimal ingresosFinancieros = BigDecimal.ZERO;
        private BigDecimal gastosFinancieros = BigDecimal.ZERO;
        private BigDecimal resultadoFinanciero = BigDecimal.ZERO;

        private BigDecimal resultadoAntesImpuestos = BigDecimal.ZERO;
        private BigDecimal impuestoBeneficios = BigDecimal.ZERO;
        private BigDecimal resultadoEjercicio = BigDecimal.ZERO;

        private BigDecimal totalIngresos = BigDecimal.ZERO;
        private BigDecimal totalGastos = BigDecimal.ZERO;

        private int numFacturasEmitidas;
        private int numFacturasRecibidas;
        private int numGastos;
        private BigDecimal margenBeneficio = BigDecimal.ZERO;

        private Map<String, BigDecimal> gastosPorCategoria = new HashMap<>();

        public boolean hayBeneficio() {
            return resultadoEjercicio.compareTo(BigDecimal.ZERO) > 0;
        }

        public boolean hayPerdida() {
            return resultadoEjercicio.compareTo(BigDecimal.ZERO) < 0;
        }
    }
}

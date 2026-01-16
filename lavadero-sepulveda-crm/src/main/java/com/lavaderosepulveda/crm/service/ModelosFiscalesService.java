package com.lavaderosepulveda.crm.service;

import com.lavaderosepulveda.crm.api.service.FacturacionApiService;
import com.lavaderosepulveda.crm.model.dto.*;
import com.lavaderosepulveda.crm.util.FechaUtils;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para la generación de modelos fiscales españoles
 * Genera datos pre-calculados para los modelos 303, 130, 390 y 347
 */
@Slf4j
public class ModelosFiscalesService {

    // ========================================
    // SINGLETON
    // ========================================
    private static ModelosFiscalesService instance;

    public static synchronized ModelosFiscalesService getInstance() {
        if (instance == null) {
            instance = new ModelosFiscalesService();
        }
        return instance;
    }

    // ========================================
    // DEPENDENCIAS Y CONFIGURACIÓN
    // ========================================
    private final FacturacionApiService facturacionService;

    // Datos del emisor (configurables)
    private String nifEmisor = "44372838L";
    private String nombreEmisor = "ANTONIO JESUS MARTINEZ DÍAZ";
    private String domicilioEmisor = "C/ Ingeniero Ruiz de Azua s/n Local 8, 14006 Córdoba";

    // Tipos de IVA en España
    private static final BigDecimal IVA_GENERAL = new BigDecimal("21");
    private static final BigDecimal IVA_REDUCIDO = new BigDecimal("10");
    private static final BigDecimal IVA_SUPERREDUCIDO = new BigDecimal("4");

    // Constructor privado para Singleton
    private ModelosFiscalesService() {
        this.facturacionService = FacturacionApiService.getInstance();
    }

    /**
     * Configura los datos del emisor
     */
    public void configurarEmisor(String nif, String nombre, String domicilio) {
        this.nifEmisor = nif;
        this.nombreEmisor = nombre;
        this.domicilioEmisor = domicilio;
    }

    // ========================================
    // MODELO 303 - IVA TRIMESTRAL
    // ========================================

    /**
     * Genera el Modelo 303 para un trimestre específico
     */
    public Modelo303DTO generarModelo303(int year, int trimestre) {
        log.info("Generando Modelo 303 para {}T{}", trimestre, year);

        // Obtener fechas del trimestre
        LocalDate inicioTrimestre = getInicioTrimestre(year, trimestre);
        LocalDate finTrimestre = getFinTrimestre(year, trimestre);

        // Obtener facturas y gastos del período
        List<FacturaEmitidaDTO> facturas = obtenerFacturasEmitidas(inicioTrimestre, finTrimestre);
        List<GastoDTO> gastos = obtenerGastos(inicioTrimestre, finTrimestre);
        List<FacturaRecibidaDTO> facturasRecibidas = obtenerFacturasRecibidas(inicioTrimestre, finTrimestre);

        // Calcular IVA devengado (ventas)
        BigDecimal baseImponible21 = BigDecimal.ZERO;
        BigDecimal cuotaDevengada21 = BigDecimal.ZERO;

        for (FacturaEmitidaDTO factura : facturas) {
            if (factura.getBaseImponible() != null) {
                baseImponible21 = baseImponible21.add(factura.getBaseImponible());
            }
            if (factura.getCuotaIva() != null) {
                cuotaDevengada21 = cuotaDevengada21.add(factura.getCuotaIva());
            }

        }

        // Calcular IVA deducible (compras/gastos)
        BigDecimal baseDeducible = BigDecimal.ZERO;
        BigDecimal cuotaDeducible = BigDecimal.ZERO;

        for (GastoDTO gasto : gastos) {
            if (gasto.getBaseImponible() != null) {
                baseDeducible = baseDeducible.add(gasto.getBaseImponible());
            }
            if (gasto.getCuotaIva() != null) {
                cuotaDeducible = cuotaDeducible.add(gasto.getCuotaIva());
            }
        }

        for (FacturaRecibidaDTO fr : facturasRecibidas) {
            if (fr.getBaseImponible() != null) {
                baseDeducible = baseDeducible.add(fr.getBaseImponible());
            }
            if (fr.getCuotaIva() != null) {
                cuotaDeducible = cuotaDeducible.add(fr.getCuotaIva());
            }
        }

        // Crear DTO
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
                .numFacturasEmitidas(facturas.size())
                .numFacturasRecibidas(gastos.size() + facturasRecibidas.size())
                .tributacionComun(true)
                .regimenSimplificado(false)
                .build();

        // Calcular resultado
        modelo.calcularResultado();

        log.info("Modelo 303 generado - Resultado: {} € ({})",
                modelo.getResultado(), modelo.getTipoResultado());

        return modelo;
    }

    // ========================================
    // MODELO 130 - IRPF TRIMESTRAL
    // ========================================

    /**
     * Genera el Modelo 130 para un trimestre específico
     * Los datos son acumulados desde el inicio del año
     */
    public Modelo130DTO generarModelo130(int year, int trimestre) {
        log.info("Generando Modelo 130 para {}T{}", trimestre, year);

        // Para el 130, los datos son ACUMULADOS desde 1 de enero
        LocalDate inicioAno = LocalDate.of(year, 1, 1);
        LocalDate finTrimestre = getFinTrimestre(year, trimestre);

        // Obtener datos acumulados
        List<FacturaEmitidaDTO> facturas = obtenerFacturasEmitidas(inicioAno, finTrimestre);
        List<GastoDTO> gastos = obtenerGastos(inicioAno, finTrimestre);
        List<FacturaRecibidaDTO> facturasRecibidas = obtenerFacturasRecibidas(inicioAno, finTrimestre);

        // Calcular ingresos acumulados
        BigDecimal ingresos = facturas.stream()
                .map(f -> f.getBaseImponible() != null ? f.getBaseImponible() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular gastos acumulados
        BigDecimal gastosTotal = gastos.stream()
                .map(g -> g.getImporte() != null ? g.getImporte() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal facturasRecibidasTotal = facturasRecibidas.stream()
                .map(fr -> fr.getTotal() != null ? fr.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gastosDeducibles = gastosTotal.add(facturasRecibidasTotal);

        // Obtener datos solo del trimestre actual para info
        LocalDate inicioTrimestreActual = getInicioTrimestre(year, trimestre);
        List<FacturaEmitidaDTO> facturasTrimestre = obtenerFacturasEmitidas(inicioTrimestreActual, finTrimestre);

        // Crear DTO
        Modelo130DTO modelo = Modelo130DTO.builder()
                .ejercicio(String.valueOf(year))
                .periodo(trimestre + "T")
                .nif(nifEmisor)
                .nombreApellidos(nombreEmisor)
                .ingresosComputables(ingresos)
                .gastosDeducibles(gastosDeducibles)
                .retencionesIngresoCuenta(BigDecimal.ZERO) // Normalmente 0 en lavadero
                .pagosFraccionadosAnteriores(BigDecimal.ZERO) // Hay que obtenerlo de trimestres anteriores
                .aDeducir(BigDecimal.ZERO)
                .ingresosTrimestre(facturasTrimestre.stream()
                        .map(f -> f.getBaseImponible() != null ? f.getBaseImponible() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .primerAnoActividad(false)
                .numFacturasEmitidas(facturas.size())
                .numGastosRegistrados(gastos.size() + facturasRecibidas.size())
                .build();

        // Calcular pago fraccionado
        modelo.calcularPagoFraccionado();

        log.info("Modelo 130 generado - Resultado: {} € ({})",
                modelo.getTotal(), modelo.getTipoResultado());

        return modelo;
    }

    // ========================================
    // MODELO 390 - RESUMEN ANUAL IVA
    // ========================================

    /**
     * Genera el Modelo 390 para un año completo
     */
    public Modelo390DTO generarModelo390(int year) {
        log.info("Generando Modelo 390 para año {}", year);

        LocalDate inicioAno = LocalDate.of(year, 1, 1);
        LocalDate finAno = LocalDate.of(year, 12, 31);

        // Obtener todos los datos del año
        List<FacturaEmitidaDTO> facturas = obtenerFacturasEmitidas(inicioAno, finAno);
        List<GastoDTO> gastos = obtenerGastos(inicioAno, finAno);
        List<FacturaRecibidaDTO> facturasRecibidas = obtenerFacturasRecibidas(inicioAno, finAno);

        // Totales devengados
        BigDecimal baseDevengada21 = BigDecimal.ZERO;
        BigDecimal cuotaDevengada21 = BigDecimal.ZERO;

        for (FacturaEmitidaDTO f : facturas) {
            if (f.getBaseImponible() != null)
                baseDevengada21 = baseDevengada21.add(f.getBaseImponible());
            if (f.getCuotaIva() != null)
                cuotaDevengada21 = cuotaDevengada21.add(f.getCuotaIva());
        }

        // Totales deducibles
        BigDecimal baseDeducible = BigDecimal.ZERO;
        BigDecimal cuotaDeducible = BigDecimal.ZERO;

        for (GastoDTO g : gastos) {
            if (g.getBaseImponible() != null)
                baseDeducible = baseDeducible.add(g.getBaseImponible());
            if (g.getCuotaIva() != null)
                cuotaDeducible = cuotaDeducible.add(g.getCuotaIva());
        }
        for (FacturaRecibidaDTO fr : facturasRecibidas) {
            if (fr.getBaseImponible() != null)
                baseDeducible = baseDeducible.add(fr.getBaseImponible());
            if (fr.getCuotaIva() != null)
                cuotaDeducible = cuotaDeducible.add(fr.getCuotaIva());
        }

        // Generar desglose trimestral
        List<Modelo390DTO.ResumenTrimestral> desglose = new ArrayList<>();
        for (int t = 1; t <= 4; t++) {
            Modelo303DTO m303 = generarModelo303(year, t);
            desglose.add(Modelo390DTO.ResumenTrimestral.builder()
                    .trimestre(t + "T")
                    .baseDevengada(m303.getBaseImponible21())
                    .cuotaDevengada(m303.getTotalCuotaDevengada())
                    .baseDeducible(m303.getBaseDeducibleInteriores())
                    .cuotaDeducible(m303.getTotalCuotaDeducible())
                    .resultado(m303.getResultado())
                    .numFacturasEmitidas(m303.getNumFacturasEmitidas())
                    .numFacturasRecibidas(m303.getNumFacturasRecibidas())
                    .build());
        }

        // Crear DTO
        Modelo390DTO modelo = Modelo390DTO.builder()
                .ejercicio(String.valueOf(year))
                .nif(nifEmisor)
                .nombreRazonSocial(nombreEmisor)
                .domicilioFiscal(domicilioEmisor)
                .cnae("4520") // Mantenimiento y reparación de vehículos
                .baseDevengada21(baseDevengada21)
                .cuotaDevengada21(cuotaDevengada21)
                .baseDevengada10(BigDecimal.ZERO)
                .cuotaDevengada10(BigDecimal.ZERO)
                .baseDevengada4(BigDecimal.ZERO)
                .cuotaDevengada4(BigDecimal.ZERO)
                .baseDeducibleInteriores(baseDeducible)
                .cuotaDeducibleInteriores(cuotaDeducible)
                .baseDeducibleInversion(BigDecimal.ZERO)
                .cuotaDeducibleInversion(BigDecimal.ZERO)
                .baseDeducibleImportaciones(BigDecimal.ZERO)
                .cuotaDeducibleImportaciones(BigDecimal.ZERO)
                .operacionesExentas(BigDecimal.ZERO)
                .operacionesNoSujetas(BigDecimal.ZERO)
                .numFacturasEmitidas(facturas.size())
                .numFacturasRecibidas(gastos.size() + facturasRecibidas.size())
                .desgloseTrimestral(desglose)
                .sectoresDiferenciados(false)
                .build();

        modelo.calcularTotales();

        log.info("Modelo 390 generado - Volumen operaciones: {} €", modelo.getVolumenOperaciones());

        return modelo;
    }

    // ========================================
    // MODELO 347 - OPERACIONES CON TERCEROS
    // ========================================

    /**
     * Genera el Modelo 347 para un año
     * Incluye clientes y proveedores con operaciones > 3.005,06 €
     */
    public Modelo347DTO generarModelo347(int year) {
        log.info("Generando Modelo 347 para año {}", year);

        LocalDate inicioAno = LocalDate.of(year, 1, 1);
        LocalDate finAno = LocalDate.of(year, 12, 31);

        Modelo347DTO modelo = Modelo347DTO.builder()
                .ejercicio(String.valueOf(year))
                .nif(nifEmisor)
                .nombreRazonSocial(nombreEmisor)
                .domicilioFiscal(domicilioEmisor)
                .tipoDeclaracion("")
                .declarados(new ArrayList<>())
                .build();

        // Obtener facturas emitidas y agrupar por cliente
        List<FacturaEmitidaDTO> facturas = obtenerFacturasEmitidas(inicioAno, finAno);

        // Agrupar por cliente (usando NIF si está disponible, si no por nombre)
        Map<String, List<FacturaEmitidaDTO>> facturasPorCliente = facturas.stream()
                .filter(f -> f.getClienteNombre() != null && !f.getClienteNombre().isEmpty())
                .collect(Collectors.groupingBy(f -> f.getClienteNif() != null && !f.getClienteNif().isEmpty()
                        ? f.getClienteNif()
                        : f.getClienteNombre()));

        // Procesar cada cliente
        for (Map.Entry<String, List<FacturaEmitidaDTO>> entry : facturasPorCliente.entrySet()) {
            String identificador = entry.getKey();
            List<FacturaEmitidaDTO> facturasCliente = entry.getValue();

            // Calcular total anual (con IVA)
            BigDecimal totalAnual = facturasCliente.stream()
                    .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calcular por trimestre
            BigDecimal[] trimestres = new BigDecimal[4];
            Arrays.fill(trimestres, BigDecimal.ZERO);

            for (FacturaEmitidaDTO f : facturasCliente) {
                LocalDate fecha = FechaUtils.parseFecha(f.getFechaEmision());
                if (fecha != null && f.getTotal() != null) {
                    int t = (fecha.getMonthValue() - 1) / 3;
                    trimestres[t] = trimestres[t].add(f.getTotal());
                }
            }

            // Crear declarado
            FacturaEmitidaDTO primeraFactura = facturasCliente.get(0);
            Modelo347DTO.Declarado347 declarado = Modelo347DTO.Declarado347.builder()
                    .nif(primeraFactura.getClienteNif() != null ? primeraFactura.getClienteNif() : "")
                    .nombreRazonSocial(primeraFactura.getClienteNombre())
                    .codigoProvincia("14") // Córdoba
                    .claveOperacion("B") // Ventas
                    .importeAnual(totalAnual)
                    .importeTrimestre1(trimestres[0])
                    .importeTrimestre2(trimestres[1])
                    .importeTrimestre3(trimestres[2])
                    .importeTrimestre4(trimestres[3])
                    .numOperaciones(facturasCliente.size())
                    .operacionSeguro(false)
                    .arrendamientoLocal(false)
                    .build();

            // Añadir solo si supera umbral
            modelo.addDeclaradoSiSupera(declarado);
        }

        // Obtener gastos y facturas recibidas y agrupar por proveedor
        List<GastoDTO> gastos = obtenerGastos(inicioAno, finAno);
        List<FacturaRecibidaDTO> facturasRecibidas = obtenerFacturasRecibidas(inicioAno, finAno);

        // Agrupar proveedores por NIF/nombre
        Map<String, BigDecimal> totalesPorProveedor = new HashMap<>();
        Map<String, String> nombresProveedores = new HashMap<>();
        Map<String, Integer> operacionesPorProveedor = new HashMap<>();

        for (GastoDTO gasto : gastos) {
            String id = gasto.getConcepto() != null && !gasto.getConcepto().isEmpty()
                    ? gasto.getConcepto()
                    : "Sin proveedor";

            BigDecimal total = gasto.getImporte() != null ? gasto.getImporte() : BigDecimal.ZERO;
            totalesPorProveedor.merge(id, total, BigDecimal::add);
            nombresProveedores.putIfAbsent(id, gasto.getConcepto());
            operacionesPorProveedor.merge(id, 1, Integer::sum);
        }

        for (FacturaRecibidaDTO fr : facturasRecibidas) {
            String id = fr.getProveedorNif() != null && !fr.getProveedorNif().isEmpty()
                    ? fr.getProveedorNif()
                    : (fr.getProveedorNombre() != null ? fr.getProveedorNombre() : "Sin proveedor");

            BigDecimal total = fr.getTotal() != null ? fr.getTotal() : BigDecimal.ZERO;
            totalesPorProveedor.merge(id, total, BigDecimal::add);
            nombresProveedores.putIfAbsent(id, fr.getProveedorNombre());
            operacionesPorProveedor.merge(id, 1, Integer::sum);
        }

        // Crear declarados para proveedores
        for (Map.Entry<String, BigDecimal> entry : totalesPorProveedor.entrySet()) {
            String id = entry.getKey();
            BigDecimal total = entry.getValue();

            Modelo347DTO.Declarado347 declarado = Modelo347DTO.Declarado347.builder()
                    .nif(id.matches("\\d{8}[A-Z]|[A-Z]\\d{8}|[A-Z]\\d{7}[A-Z]") ? id : "")
                    .nombreRazonSocial(nombresProveedores.get(id))
                    .codigoProvincia("14")
                    .claveOperacion("A") // Compras
                    .importeAnual(total)
                    .numOperaciones(operacionesPorProveedor.getOrDefault(id, 0))
                    .operacionSeguro(false)
                    .arrendamientoLocal(false)
                    .build();

            modelo.addDeclaradoSiSupera(declarado);
        }

        modelo.calcularTotales();

        log.info("Modelo 347 generado - {} declarados, importe total: {} €",
                modelo.getNumDeclarados(), modelo.getImporteTotal());

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

    private List<FacturaEmitidaDTO> obtenerFacturasEmitidas(LocalDate desde, LocalDate hasta) {
        try {
            List<FacturaEmitidaDTO> todas = facturacionService.obtenerFacturasEmitidas();
            return todas.stream()
                    .filter(f -> {
                        LocalDate fecha = FechaUtils.parseFecha(f.getFechaEmision());
                        return fecha != null && !fecha.isBefore(desde) && !fecha.isAfter(hasta);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error obteniendo facturas emitidas", e);
            return new ArrayList<>();
        }
    }

    private List<GastoDTO> obtenerGastos(LocalDate desde, LocalDate hasta) {
        try {
            List<GastoDTO> todos = facturacionService.obtenerGastos();
            return todos.stream()
                    .filter(g -> {
                        LocalDate fecha = FechaUtils.parseFecha(g.getFecha());
                        return fecha != null && !fecha.isBefore(desde) && !fecha.isAfter(hasta);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error obteniendo gastos", e);
            return new ArrayList<>();
        }
    }

    private List<FacturaRecibidaDTO> obtenerFacturasRecibidas(LocalDate desde, LocalDate hasta) {
        try {
            List<FacturaRecibidaDTO> todas = facturacionService.obtenerFacturasRecibidas();
            return todas.stream()
                    .filter(fr -> {
                        LocalDate fecha = FechaUtils.parseFecha(fr.getFechaFactura());
                        return fecha != null && !fecha.isBefore(desde) && !fecha.isAfter(hasta);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error obteniendo facturas recibidas", e);
            return new ArrayList<>();
        }
    }

}
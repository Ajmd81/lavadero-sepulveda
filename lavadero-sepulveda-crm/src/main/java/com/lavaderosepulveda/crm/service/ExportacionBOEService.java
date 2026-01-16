package com.lavaderosepulveda.crm.service;

import com.lavaderosepulveda.crm.model.dto.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Servicio para generar ficheros en formato BOE (Boletín Oficial del Estado)
 * para su importación en la Agencia Tributaria.
 * 
 * Formatos según especificaciones técnicas de la AEAT:
 * - Modelo 303: Autoliquidación IVA
 * - Modelo 130: Pago fraccionado IRPF
 * - Modelo 390: Resumen anual IVA
 */
@Slf4j
public class ExportacionBOEService {

    private static ExportacionBOEService instance;

    public static synchronized ExportacionBOEService getInstance() {
        if (instance == null) {
            instance = new ExportacionBOEService();
        }
        return instance;
    }

    private ExportacionBOEService() {
    }

    // ========================================
    // MODELO 303 - IVA TRIMESTRAL
    // ========================================

    /**
     * Genera el fichero BOE para el Modelo 303
     * Formato según Orden HAP/2194/2013 y actualizaciones
     */
    public String generarBOE303(Modelo303DTO modelo) {
        log.info("Generando fichero BOE para Modelo 303 - {} {}", modelo.getPeriodo(), modelo.getEjercicio());

        StringBuilder sb = new StringBuilder();

        // ========================================
        // REGISTRO TIPO 1 - DECLARANTE
        // ========================================
        sb.append("<T3030").append(modelo.getEjercicio()).append(modelo.getPeriodo()).append("0000>");
        sb.append("<AUX>");
        sb.append(completarConEspacios("", 70)); // Reservado
        sb.append(formatearVersion("2.0")); // Versión del programa
        sb.append(completarConEspacios("", 4)); // Reservado
        sb.append(formatearNIF(modelo.getNif())); // NIF
        sb.append(completarConEspacios("", 213)); // Reservado
        sb.append("</AUX>");

        // Datos identificativos
        sb.append("<T30301000>");
        sb.append(formatearCampo("01", modelo.getEjercicio())); // Ejercicio
        sb.append(formatearCampo("02", modelo.getPeriodo())); // Período
        sb.append(formatearCampo("03", modelo.getNif())); // NIF
        sb.append(formatearCampo("04", modelo.getNombreRazonSocial())); // Apellidos y nombre o razón social
        sb.append("</T30301000>");

        // ========================================
        // PÁGINA 1 - LIQUIDACIÓN
        // ========================================
        sb.append("<T30302000>");

        // IVA Devengado - Régimen general
        // Casilla 01: Base imponible al tipo general (21%)
        sb.append(formatearCampoNumerico("01", modelo.getBaseImponible21()));
        // Casilla 02: Tipo (21)
        sb.append(formatearCampo("02", "21,00"));
        // Casilla 03: Cuota
        sb.append(formatearCampoNumerico("03", modelo.getCuotaDevengada21()));

        // Casilla 04: Base imponible al tipo reducido (10%)
        sb.append(formatearCampoNumerico("04", modelo.getBaseImponible10()));
        // Casilla 05: Tipo (10)
        sb.append(formatearCampo("05", "10,00"));
        // Casilla 06: Cuota
        sb.append(formatearCampoNumerico("06", modelo.getCuotaDevengada10()));

        // Casilla 07: Base imponible al tipo superreducido (4%)
        sb.append(formatearCampoNumerico("07", modelo.getBaseImponible4()));
        // Casilla 08: Tipo (4)
        sb.append(formatearCampo("08", "4,00"));
        // Casilla 09: Cuota
        sb.append(formatearCampoNumerico("09", modelo.getCuotaDevengada4()));

        // Casilla 21: Total cuota devengada
        sb.append(formatearCampoNumerico("21", modelo.getTotalCuotaDevengada()));

        // IVA Deducible
        // Casilla 22: Base imponible operaciones interiores corrientes
        sb.append(formatearCampoNumerico("22", modelo.getBaseDeducibleInteriores()));
        // Casilla 23: Cuota deducible operaciones interiores corrientes
        sb.append(formatearCampoNumerico("23", modelo.getCuotaDeducibleInteriores()));

        // Casilla 24: Base imponible bienes de inversión
        sb.append(formatearCampoNumerico("24", modelo.getBaseDeducibleInversion()));
        // Casilla 25: Cuota deducible bienes de inversión
        sb.append(formatearCampoNumerico("25", modelo.getCuotaDeducibleInversion()));

        // Casilla 26: Base imponible importaciones
        sb.append(formatearCampoNumerico("26", modelo.getBaseDeducibleImportaciones()));
        // Casilla 27: Cuota deducible importaciones
        sb.append(formatearCampoNumerico("27", modelo.getCuotaDeducibleImportaciones()));

        // Casilla 28: Total cuota deducible
        sb.append(formatearCampoNumerico("28", modelo.getTotalCuotaDeducible()));

        // Casilla 29: Diferencia (21 - 28)
        sb.append(formatearCampoNumerico("29", modelo.getDiferencia()));

        // Casilla 30: Atribuible a la Administración del Estado (100%)
        sb.append(formatearCampo("30", "100"));

        // Casilla 31: Cuotas a compensar de períodos anteriores
        sb.append(formatearCampoNumerico("31", modelo.getCuotasCompensar()));

        // Casilla 32: Regularización cuotas art. 80.cinco.5ª LIVA
        sb.append(formatearCampoNumerico("32", BigDecimal.ZERO));

        // Casilla 33: Regularización por aplicación del porcentaje definitivo de
        // prorrata
        sb.append(formatearCampoNumerico("33", BigDecimal.ZERO));

        sb.append("</T30302000>");

        // ========================================
        // PÁGINA 3 - RESULTADO
        // ========================================
        sb.append("<T30303000>");

        // Casilla 64: Suma de resultados
        sb.append(formatearCampoNumerico("64", modelo.getResultado()));

        // Casilla 65: Atribuible a la Administración del Estado
        sb.append(formatearCampoNumerico("65", modelo.getResultado()));

        // Casilla 66: IVA a la importación liquidado por la Aduana pendiente de ingreso
        sb.append(formatearCampoNumerico("66", BigDecimal.ZERO));

        // Casilla 67: Cuotas a compensar pendientes de períodos anteriores
        sb.append(formatearCampoNumerico("67", BigDecimal.ZERO));

        // Casilla 68: Cuotas a compensar generadas en el período
        BigDecimal cuotasACompensar = modelo.getResultado().compareTo(BigDecimal.ZERO) < 0
                ? modelo.getResultado().abs()
                : BigDecimal.ZERO;
        sb.append(formatearCampoNumerico("68", cuotasACompensar));

        // Casilla 69: Resultado de la autoliquidación
        sb.append(formatearCampoNumerico("69", modelo.getResultado().max(BigDecimal.ZERO)));

        // Casilla 70: A compensar
        sb.append(formatearCampoNumerico("70", cuotasACompensar));

        // Casilla 71: Resultado
        sb.append(formatearCampoNumerico("71", modelo.getResultado()));

        // Tipo de declaración
        String tipoDeclaracion = determinarTipoDeclaracion303(modelo);
        sb.append(formatearCampo("TYPE", tipoDeclaracion));

        sb.append("</T30303000>");

        // Cierre del fichero
        sb.append("</T3030").append(modelo.getEjercicio()).append(modelo.getPeriodo()).append("0000>");

        log.info("Fichero BOE 303 generado correctamente");
        return sb.toString();
    }

    private String determinarTipoDeclaracion303(Modelo303DTO modelo) {
        if (modelo.getResultado().compareTo(BigDecimal.ZERO) > 0) {
            return "I"; // Ingreso
        } else if (modelo.getResultado().compareTo(BigDecimal.ZERO) < 0) {
            return "C"; // A compensar
        } else {
            return "N"; // Sin actividad/cero
        }
    }

    // ========================================
    // MODELO 130 - IRPF TRIMESTRAL
    // ========================================

    /**
     * Genera el fichero BOE para el Modelo 130
     * Pago fraccionado IRPF - Estimación directa
     */
    public String generarBOE130(Modelo130DTO modelo) {
        log.info("Generando fichero BOE para Modelo 130 - {} {}", modelo.getPeriodo(), modelo.getEjercicio());

        StringBuilder sb = new StringBuilder();

        // ========================================
        // REGISTRO TIPO 1 - DECLARANTE
        // ========================================
        sb.append("<T1300").append(modelo.getEjercicio()).append(modelo.getPeriodo()).append("0000>");
        sb.append("<AUX>");
        sb.append(completarConEspacios("", 70));
        sb.append(formatearVersion("1.0"));
        sb.append(completarConEspacios("", 4));
        sb.append(formatearNIF(modelo.getNif()));
        sb.append(completarConEspacios("", 213));
        sb.append("</AUX>");

        // Datos identificativos
        sb.append("<T13001000>");
        sb.append(formatearCampo("01", modelo.getEjercicio())); // Ejercicio
        sb.append(formatearCampo("02", modelo.getPeriodo())); // Período
        sb.append(formatearCampo("03", modelo.getNif())); // NIF
        sb.append(formatearCampo("04", modelo.getNombreApellidos())); // Apellidos y nombre
        sb.append("</T13001000>");

        // ========================================
        // ACTIVIDADES ECONÓMICAS EN ESTIMACIÓN DIRECTA
        // ========================================
        sb.append("<T13002000>");

        // I. ACTIVIDADES ECONÓMICAS EN ESTIMACIÓN DIRECTA, MODALIDAD NORMAL O
        // SIMPLIFICADA

        // Casilla 01: Ingresos computables del trimestre (acumulados)
        sb.append(formatearCampoNumerico("01", modelo.getIngresosComputables()));

        // Casilla 02: Gastos fiscalmente deducibles (acumulados)
        sb.append(formatearCampoNumerico("02", modelo.getGastosDeducibles()));

        // Casilla 03: Rendimiento neto (01 - 02)
        sb.append(formatearCampoNumerico("03", modelo.getRendimientoNeto()));

        // Casilla 04: 20% del rendimiento neto (si positivo)
        sb.append(formatearCampoNumerico("04", modelo.getPagoCuenta20()));

        // Casilla 05: A deducir: Retenciones e ingresos a cuenta soportados
        sb.append(formatearCampoNumerico("05", modelo.getRetencionesIngresoCuenta()));

        // Casilla 06: A deducir: Pagos fraccionados ingresados en trimestres anteriores
        sb.append(formatearCampoNumerico("06", modelo.getPagosFraccionadosAnteriores()));

        // Casilla 07: Pago fraccionado previo (04 - 05 - 06)
        sb.append(formatearCampoNumerico("07", modelo.getResultadoPrevio()));

        sb.append("</T13002000>");

        // ========================================
        // RESULTADO
        // ========================================
        sb.append("<T13003000>");

        // Casilla 12: Suma de los pagos fraccionados previos
        sb.append(formatearCampoNumerico("12", modelo.getResultadoPrevio()));

        // Casilla 13: Minoración por aplicación de la deducción art. 80 bis Ley IRPF
        // (primer año actividad)
        BigDecimal minoracionPrimerAno = BigDecimal.ZERO;
        if (modelo.isPrimerAnoActividad()) {
            // Reducción del 50% con límite
            minoracionPrimerAno = modelo.getResultadoPrevio()
                    .multiply(new BigDecimal("0.5"))
                    .min(new BigDecimal("400"));
        }
        sb.append(formatearCampoNumerico("13", minoracionPrimerAno));

        // Casilla 14: A deducir
        sb.append(formatearCampoNumerico("14", modelo.getADeducir()));

        // Casilla 15: Total liquidación (12 - 13 - 14)
        sb.append(formatearCampoNumerico("15", modelo.getTotal()));

        // Casilla 16: A deducir: Resultado a ingresar de las anteriores
        // autoliquidaciones
        sb.append(formatearCampoNumerico("16", BigDecimal.ZERO));

        // Casilla 17: Resultado de la autoliquidación
        sb.append(formatearCampoNumerico("17", modelo.getTotal()));

        // Tipo de declaración
        String tipoDeclaracion = determinarTipoDeclaracion130(modelo);
        sb.append(formatearCampo("TYPE", tipoDeclaracion));

        sb.append("</T13003000>");

        // Cierre del fichero
        sb.append("</T1300").append(modelo.getEjercicio()).append(modelo.getPeriodo()).append("0000>");

        log.info("Fichero BOE 130 generado correctamente");
        return sb.toString();
    }

    private String determinarTipoDeclaracion130(Modelo130DTO modelo) {
        if (modelo.getTotal().compareTo(BigDecimal.ZERO) > 0) {
            return "I"; // Ingreso
        } else if (modelo.getTotal().compareTo(BigDecimal.ZERO) < 0) {
            return "N"; // Negativa
        } else {
            return "B"; // En blanco / sin actividad
        }
    }

    // ========================================
    // MODELO 390 - RESUMEN ANUAL IVA
    // ========================================

    /**
     * Genera el fichero BOE para el Modelo 390
     * Declaración resumen anual del IVA
     */
    public String generarBOE390(Modelo390DTO modelo) {
        log.info("Generando fichero BOE para Modelo 390 - Ejercicio {}", modelo.getEjercicio());

        StringBuilder sb = new StringBuilder();

        // ========================================
        // REGISTRO TIPO 1 - DECLARANTE
        // ========================================
        sb.append("<T3900").append(modelo.getEjercicio()).append("0000>");
        sb.append("<AUX>");
        sb.append(completarConEspacios("", 70));
        sb.append(formatearVersion("2.0"));
        sb.append(completarConEspacios("", 4));
        sb.append(formatearNIF(modelo.getNif()));
        sb.append(completarConEspacios("", 213));
        sb.append("</AUX>");

        // ========================================
        // PÁGINA 1 - SUJETO PASIVO
        // ========================================
        sb.append("<T39001000>");
        sb.append(formatearCampo("01", modelo.getEjercicio())); // Ejercicio
        sb.append(formatearCampo("02", modelo.getNif())); // NIF
        sb.append(formatearCampo("03", modelo.getNombreRazonSocial())); // Apellidos y nombre o razón social

        // Datos estadísticos
        sb.append(formatearCampo("04", modelo.getCnae())); // CNAE actividad principal

        // Indicadores
        sb.append(formatearCampo("05", "N")); // ¿Sujeto pasivo inscrito en el registro de devolución mensual?
        sb.append(formatearCampo("06", "N")); // ¿Ha optado por la tributación en destino en ventas a distancia?
        sb.append(formatearCampo("07", "N")); // ¿Ha sido declarado en concurso de acreedores?
        sb.append(formatearCampo("08", "N")); // ¿Es autoliquidación conjunta?
        sb.append(formatearCampo("09", modelo.isSectoresDiferenciados() ? "S" : "N")); // Sectores diferenciados

        sb.append("</T39001000>");

        // ========================================
        // PÁGINA 3 - IVA DEVENGADO
        // ========================================
        sb.append("<T39003000>");

        // Régimen General
        // Base y cuota al 21%
        sb.append(formatearCampoNumerico("01", modelo.getBaseDevengada21()));
        sb.append(formatearCampo("02", "21,00"));
        sb.append(formatearCampoNumerico("03", modelo.getCuotaDevengada21()));

        // Base y cuota al 10%
        sb.append(formatearCampoNumerico("04", modelo.getBaseDevengada10()));
        sb.append(formatearCampo("05", "10,00"));
        sb.append(formatearCampoNumerico("06", modelo.getCuotaDevengada10()));

        // Base y cuota al 4%
        sb.append(formatearCampoNumerico("07", modelo.getBaseDevengada4()));
        sb.append(formatearCampo("08", "4,00"));
        sb.append(formatearCampoNumerico("09", modelo.getCuotaDevengada4()));

        // Total bases y cuotas IVA devengado
        sb.append(formatearCampoNumerico("33", modelo.getTotalBaseDevengada())); // Total bases
        sb.append(formatearCampoNumerico("34", modelo.getTotalCuotaDevengada())); // Total cuotas

        sb.append("</T39003000>");

        // ========================================
        // PÁGINA 4 - IVA DEDUCIBLE
        // ========================================
        sb.append("<T39004000>");

        // Operaciones interiores corrientes
        sb.append(formatearCampoNumerico("35", modelo.getBaseDeducibleInteriores()));
        sb.append(formatearCampoNumerico("36", modelo.getCuotaDeducibleInteriores()));

        // Operaciones interiores bienes de inversión
        sb.append(formatearCampoNumerico("37", modelo.getBaseDeducibleInversion()));
        sb.append(formatearCampoNumerico("38", modelo.getCuotaDeducibleInversion()));

        // Importaciones
        sb.append(formatearCampoNumerico("39", modelo.getBaseDeducibleImportaciones()));
        sb.append(formatearCampoNumerico("40", modelo.getCuotaDeducibleImportaciones()));

        // Total cuotas deducibles
        sb.append(formatearCampoNumerico("49", modelo.getTotalCuotaDeducible()));

        sb.append("</T39004000>");

        // ========================================
        // PÁGINA 5 - RESULTADO
        // ========================================
        sb.append("<T39005000>");

        // Resultado régimen general
        sb.append(formatearCampoNumerico("50", modelo.getDiferencia())); // Diferencia

        // Resultado régimen simplificado (no aplica normalmente a lavaderos)
        sb.append(formatearCampoNumerico("51", BigDecimal.ZERO));

        // Suma de resultados
        sb.append(formatearCampoNumerico("52", modelo.getDiferencia()));

        // Regularizaciones
        sb.append(formatearCampoNumerico("53", BigDecimal.ZERO)); // % Prorrata general
        sb.append(formatearCampoNumerico("54", BigDecimal.ZERO)); // Regularización bienes de inversión
        sb.append(formatearCampoNumerico("55", BigDecimal.ZERO)); // Regularización por aplicación % definitivo prorrata

        // Resultado de las autoliquidaciones
        sb.append(formatearCampoNumerico("56", modelo.getDiferencia())); // Suma de resultados

        // Entregas intracomunitarias exentas
        sb.append(formatearCampoNumerico("57", BigDecimal.ZERO));

        // Exportaciones y operaciones asimiladas
        sb.append(formatearCampoNumerico("58", BigDecimal.ZERO));

        // Operaciones no sujetas con derecho a deducción
        sb.append(formatearCampoNumerico("59", BigDecimal.ZERO));

        sb.append("</T39005000>");

        // ========================================
        // PÁGINA 6 - VOLUMEN DE OPERACIONES
        // ========================================
        sb.append("<T39006000>");

        // Operaciones en régimen general
        sb.append(formatearCampoNumerico("80", modelo.getTotalBaseDevengada()));

        // Operaciones en régimen especial (agricultura, ganadería, bienes usados, etc.)
        sb.append(formatearCampoNumerico("81", BigDecimal.ZERO));
        sb.append(formatearCampoNumerico("82", BigDecimal.ZERO));
        sb.append(formatearCampoNumerico("83", BigDecimal.ZERO));
        sb.append(formatearCampoNumerico("84", BigDecimal.ZERO));

        // Entregas intracomunitarias exentas
        sb.append(formatearCampoNumerico("85", BigDecimal.ZERO));

        // Exportaciones y operaciones asimiladas
        sb.append(formatearCampoNumerico("86", BigDecimal.ZERO));

        // Operaciones no sujetas o con inversión del sujeto pasivo
        sb.append(formatearCampoNumerico("87", BigDecimal.ZERO));

        // Total volumen de operaciones
        sb.append(formatearCampoNumerico("88", modelo.getVolumenOperaciones()));

        sb.append("</T39006000>");

        // ========================================
        // PÁGINA 8 - RESULTADO AUTOLIQUIDACIONES
        // ========================================
        sb.append("<T39008000>");

        // Desglose por trimestres
        int casilla = 95;
        for (Modelo390DTO.ResumenTrimestral rt : modelo.getDesgloseTrimestral()) {
            // Por cada trimestre: IVA devengado, IVA deducible, resultado
            sb.append(formatearCampoNumerico(String.valueOf(casilla++), rt.getCuotaDevengada()));
            sb.append(formatearCampoNumerico(String.valueOf(casilla++), rt.getCuotaDeducible()));
            sb.append(formatearCampoNumerico(String.valueOf(casilla++), rt.getResultado()));
        }

        // Totales
        BigDecimal totalDevengado = modelo.getDesgloseTrimestral().stream()
                .map(Modelo390DTO.ResumenTrimestral::getCuotaDevengada)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDeducible = modelo.getDesgloseTrimestral().stream()
                .map(Modelo390DTO.ResumenTrimestral::getCuotaDeducible)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalResultado = modelo.getDesgloseTrimestral().stream()
                .map(Modelo390DTO.ResumenTrimestral::getResultado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        sb.append(formatearCampoNumerico("107", totalDevengado));
        sb.append(formatearCampoNumerico("108", totalDeducible));
        sb.append(formatearCampoNumerico("109", totalResultado));

        // Resultado de las autoliquidaciones: A ingresar / A devolver
        sb.append(formatearCampoNumerico("110", totalResultado.max(BigDecimal.ZERO))); // Total ingresado
        sb.append(formatearCampoNumerico("111", totalResultado.min(BigDecimal.ZERO).abs())); // Total a
                                                                                             // devolver/compensar

        sb.append("</T39008000>");

        // Cierre del fichero
        sb.append("</T3900").append(modelo.getEjercicio()).append("0000>");

        log.info("Fichero BOE 390 generado correctamente");
        return sb.toString();
    }

    // ========================================
    // MÉTODOS AUXILIARES DE FORMATO
    // ========================================

    /**
     * Formatea un campo con su etiqueta
     */
    private String formatearCampo(String casilla, String valor) {
        if (valor == null)
            valor = "";
        return "<" + casilla + ">" + valor + "</" + casilla + ">";
    }

    /**
     * Formatea un campo numérico (importe)
     * Los importes van con 2 decimales, sin separador de miles
     * Los negativos se indican con signo
     */
    private String formatearCampoNumerico(String casilla, BigDecimal valor) {
        if (valor == null)
            valor = BigDecimal.ZERO;

        // Redondear a 2 decimales
        valor = valor.setScale(2, RoundingMode.HALF_UP);

        // Formatear sin separador de miles, con punto como separador decimal
        String valorStr = valor.toPlainString();

        return "<" + casilla + ">" + valorStr + "</" + casilla + ">";
    }

    /**
     * Formatea el NIF a 9 caracteres
     */
    private String formatearNIF(String nif) {
        if (nif == null)
            nif = "";
        nif = nif.toUpperCase().replaceAll("[^A-Z0-9]", "");
        return completarConEspacios(nif, 9);
    }

    /**
     * Formatea la versión del programa
     */
    private String formatearVersion(String version) {
        return completarConEspacios(version, 4);
    }

    /**
     * Completa una cadena con espacios a la derecha hasta la longitud indicada
     */
    private String completarConEspacios(String texto, int longitud) {
        if (texto == null)
            texto = "";
        if (texto.length() >= longitud) {
            return texto.substring(0, longitud);
        }
        return texto + " ".repeat(longitud - texto.length());
    }

    /**
     * Completa una cadena con ceros a la izquierda hasta la longitud indicada
     */
    private String completarConCeros(String texto, int longitud) {
        if (texto == null)
            texto = "";
        texto = texto.replaceAll("[^0-9]", "");
        if (texto.length() >= longitud) {
            return texto.substring(0, longitud);
        }
        return "0".repeat(longitud - texto.length()) + texto;
    }

    /**
     * Genera el nombre de fichero estándar para el modelo
     */
    public String generarNombreFichero(String modelo, String ejercicio, String periodo) {
        // Formato: MODELOejercicioperiodo.txt
        // Ejemplo: 3032024T1.txt, 1302024T2.txt, 3902024.txt
        StringBuilder nombre = new StringBuilder();
        nombre.append(modelo);
        nombre.append(ejercicio);
        if (periodo != null && !periodo.isEmpty()) {
            nombre.append(periodo.replace("T", "T"));
        }
        nombre.append(".txt");
        return nombre.toString();
    }
}
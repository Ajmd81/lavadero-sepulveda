package com.lavaderosepulveda.crm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para el Modelo 390 - Declaración-Resumen anual de IVA
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Modelo390DTO {

    // ========================================
    // IDENTIFICACIÓN
    // ========================================
    private String ejercicio;           // Año (ej: "2024")
    private String nif;
    private String nombreRazonSocial;
    private String domicilioFiscal;
    
    // ========================================
    // DATOS ESTADÍSTICOS
    // ========================================
    private String cnae;                // Código CNAE principal
    private String epigrafe;            // Epígrafe IAE
    
    // ========================================
    // RÉGIMEN GENERAL - IVA DEVENGADO
    // ========================================
    
    // Operaciones al 21%
    private BigDecimal baseDevengada21;
    private BigDecimal cuotaDevengada21;
    
    // Operaciones al 10%
    private BigDecimal baseDevengada10;
    private BigDecimal cuotaDevengada10;
    
    // Operaciones al 4%
    private BigDecimal baseDevengada4;
    private BigDecimal cuotaDevengada4;
    
    // Total bases y cuotas devengadas
    private BigDecimal totalBaseDevengada;
    private BigDecimal totalCuotaDevengada;
    
    // ========================================
    // RÉGIMEN GENERAL - IVA DEDUCIBLE
    // ========================================
    
    // Operaciones interiores corrientes
    private BigDecimal baseDeducibleInteriores;
    private BigDecimal cuotaDeducibleInteriores;
    
    // Bienes de inversión
    private BigDecimal baseDeducibleInversion;
    private BigDecimal cuotaDeducibleInversion;
    
    // Importaciones
    private BigDecimal baseDeducibleImportaciones;
    private BigDecimal cuotaDeducibleImportaciones;
    
    // Total deducible
    private BigDecimal totalBaseDeducible;
    private BigDecimal totalCuotaDeducible;
    
    // ========================================
    // RESULTADO ANUAL
    // ========================================
    
    // Suma de resultados 303 (positivos y negativos)
    private BigDecimal sumaResultados303;
    
    // Total cuotas ingresadas
    private BigDecimal cuotasIngresadas;
    
    // Total cuotas a compensar
    private BigDecimal cuotasACompensar;
    
    // Resultado anual
    private BigDecimal resultadoAnual;
    
    // Diferencia (Devengado - Deducible)
    private BigDecimal diferencia;
    
    // ========================================
    // OPERACIONES ESPECÍFICAS
    // ========================================
    
    // Operaciones exentas
    private BigDecimal operacionesExentas;
    
    // Operaciones no sujetas
    private BigDecimal operacionesNoSujetas;
    
    // Operaciones con inversión del sujeto pasivo
    private BigDecimal operacionesInversionSujetoPasivo;
    
    // ========================================
    // VOLUMEN DE OPERACIONES
    // ========================================
    
    // Total volumen de operaciones
    private BigDecimal volumenOperaciones;
    
    // Número total de facturas emitidas
    private int numFacturasEmitidas;
    
    // Número total de facturas recibidas
    private int numFacturasRecibidas;
    
    // ========================================
    // DESGLOSE POR TRIMESTRES
    // ========================================
    
    @Builder.Default
    private List<ResumenTrimestral> desgloseTrimestral = new ArrayList<>();
    
    // ========================================
    // DATOS ADICIONALES
    // ========================================
    
    // Prorrata aplicable (%)
    private BigDecimal prorrata;
    
    // Regularización bienes de inversión
    private BigDecimal regularizacionInversion;
    
    // Sectores diferenciados
    private boolean sectoresDiferenciados;
    
    // Fecha de presentación
    private String fechaPresentacion;
    
    /**
     * Calcula los totales del modelo 390
     */
    public void calcularTotales() {
        // Total devengado
        this.totalBaseDevengada = BigDecimal.ZERO;
        this.totalCuotaDevengada = BigDecimal.ZERO;
        
        if (baseDevengada21 != null) totalBaseDevengada = totalBaseDevengada.add(baseDevengada21);
        if (baseDevengada10 != null) totalBaseDevengada = totalBaseDevengada.add(baseDevengada10);
        if (baseDevengada4 != null) totalBaseDevengada = totalBaseDevengada.add(baseDevengada4);
        
        if (cuotaDevengada21 != null) totalCuotaDevengada = totalCuotaDevengada.add(cuotaDevengada21);
        if (cuotaDevengada10 != null) totalCuotaDevengada = totalCuotaDevengada.add(cuotaDevengada10);
        if (cuotaDevengada4 != null) totalCuotaDevengada = totalCuotaDevengada.add(cuotaDevengada4);
        
        // Total deducible
        this.totalBaseDeducible = BigDecimal.ZERO;
        this.totalCuotaDeducible = BigDecimal.ZERO;
        
        if (baseDeducibleInteriores != null) totalBaseDeducible = totalBaseDeducible.add(baseDeducibleInteriores);
        if (baseDeducibleInversion != null) totalBaseDeducible = totalBaseDeducible.add(baseDeducibleInversion);
        if (baseDeducibleImportaciones != null) totalBaseDeducible = totalBaseDeducible.add(baseDeducibleImportaciones);
        
        if (cuotaDeducibleInteriores != null) totalCuotaDeducible = totalCuotaDeducible.add(cuotaDeducibleInteriores);
        if (cuotaDeducibleInversion != null) totalCuotaDeducible = totalCuotaDeducible.add(cuotaDeducibleInversion);
        if (cuotaDeducibleImportaciones != null) totalCuotaDeducible = totalCuotaDeducible.add(cuotaDeducibleImportaciones);
        
        // Diferencia
        this.diferencia = totalCuotaDevengada.subtract(totalCuotaDeducible);
        
        // Volumen de operaciones
        this.volumenOperaciones = totalBaseDevengada;
        if (operacionesExentas != null) volumenOperaciones = volumenOperaciones.add(operacionesExentas);
    }
    
    /**
     * Clase interna para resumen trimestral
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenTrimestral {
        private String trimestre;           // "1T", "2T", "3T", "4T"
        private BigDecimal baseDevengada;
        private BigDecimal cuotaDevengada;
        private BigDecimal baseDeducible;
        private BigDecimal cuotaDeducible;
        private BigDecimal resultado;
        private int numFacturasEmitidas;
        private int numFacturasRecibidas;
    }
}

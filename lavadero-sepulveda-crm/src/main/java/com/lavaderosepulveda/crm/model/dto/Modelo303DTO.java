package com.lavaderosepulveda.crm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para el Modelo 303 - Declaración trimestral de IVA
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Modelo303DTO {

    // ========================================
    // IDENTIFICACIÓN
    // ========================================
    private String ejercicio;           // Año (ej: "2024")
    private String periodo;             // Trimestre: "1T", "2T", "3T", "4T"
    private String nif;
    private String nombreRazonSocial;
    
    // ========================================
    // IVA DEVENGADO (Ventas/Ingresos)
    // ========================================
    
    // Régimen general - Base imponible y cuota al 21%
    private BigDecimal baseImponible21;
    private BigDecimal cuotaDevengada21;
    
    // Régimen general - Base imponible y cuota al 10%
    private BigDecimal baseImponible10;
    private BigDecimal cuotaDevengada10;
    
    // Régimen general - Base imponible y cuota al 4%
    private BigDecimal baseImponible4;
    private BigDecimal cuotaDevengada4;
    
    // Total IVA devengado (suma de cuotas)
    private BigDecimal totalCuotaDevengada;
    
    // ========================================
    // IVA DEDUCIBLE (Compras/Gastos)
    // ========================================
    
    // Cuotas soportadas en operaciones interiores corrientes
    private BigDecimal baseDeducibleInteriores;
    private BigDecimal cuotaDeducibleInteriores;
    
    // Cuotas soportadas en bienes de inversión
    private BigDecimal baseDeducibleInversion;
    private BigDecimal cuotaDeducibleInversion;
    
    // Cuotas soportadas en importaciones
    private BigDecimal baseDeducibleImportaciones;
    private BigDecimal cuotaDeducibleImportaciones;
    
    // Total IVA deducible
    private BigDecimal totalCuotaDeducible;
    
    // ========================================
    // RESULTADO
    // ========================================
    
    // Diferencia (Devengado - Deducible)
    private BigDecimal diferencia;
    
    // Cuotas a compensar de períodos anteriores
    private BigDecimal cuotasCompensar;
    
    // Resultado de la declaración
    private BigDecimal resultado;
    
    // A ingresar / A compensar / A devolver
    private String tipoResultado; // "INGRESAR", "COMPENSAR", "DEVOLVER"
    
    // ========================================
    // DATOS ADICIONALES
    // ========================================
    
    // Número de facturas emitidas
    private int numFacturasEmitidas;
    
    // Número de facturas recibidas (gastos)
    private int numFacturasRecibidas;
    
    // Tributación exclusiva en territorio común
    private boolean tributacionComun;
    
    // Está en régimen simplificado
    private boolean regimenSimplificado;
    
    // Fecha de presentación
    private String fechaPresentacion;
    
    /**
     * Calcula el resultado de la declaración
     */
    public void calcularResultado() {
        // Total devengado
        this.totalCuotaDevengada = BigDecimal.ZERO;
        if (cuotaDevengada21 != null) totalCuotaDevengada = totalCuotaDevengada.add(cuotaDevengada21);
        if (cuotaDevengada10 != null) totalCuotaDevengada = totalCuotaDevengada.add(cuotaDevengada10);
        if (cuotaDevengada4 != null) totalCuotaDevengada = totalCuotaDevengada.add(cuotaDevengada4);
        
        // Total deducible
        this.totalCuotaDeducible = BigDecimal.ZERO;
        if (cuotaDeducibleInteriores != null) totalCuotaDeducible = totalCuotaDeducible.add(cuotaDeducibleInteriores);
        if (cuotaDeducibleInversion != null) totalCuotaDeducible = totalCuotaDeducible.add(cuotaDeducibleInversion);
        if (cuotaDeducibleImportaciones != null) totalCuotaDeducible = totalCuotaDeducible.add(cuotaDeducibleImportaciones);
        
        // Diferencia
        this.diferencia = totalCuotaDevengada.subtract(totalCuotaDeducible);
        
        // Resultado (considerando compensaciones)
        if (cuotasCompensar == null) cuotasCompensar = BigDecimal.ZERO;
        this.resultado = diferencia.subtract(cuotasCompensar);
        
        // Tipo de resultado
        if (resultado.compareTo(BigDecimal.ZERO) > 0) {
            this.tipoResultado = "INGRESAR";
        } else if (resultado.compareTo(BigDecimal.ZERO) < 0) {
            this.tipoResultado = "COMPENSAR";
        } else {
            this.tipoResultado = "CERO";
        }
    }
}

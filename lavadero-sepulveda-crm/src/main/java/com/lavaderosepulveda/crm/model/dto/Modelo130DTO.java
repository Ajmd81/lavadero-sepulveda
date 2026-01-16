package com.lavaderosepulveda.crm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DTO para el Modelo 130 - Pago fraccionado IRPF (Estimación Directa)
 * Para autónomos en estimación directa normal o simplificada
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Modelo130DTO {

    // ========================================
    // IDENTIFICACIÓN
    // ========================================
    private String ejercicio;           // Año (ej: "2024")
    private String periodo;             // Trimestre: "1T", "2T", "3T", "4T"
    private String nif;
    private String nombreApellidos;
    
    // ========================================
    // ACTIVIDADES ECONÓMICAS EN ESTIMACIÓN DIRECTA
    // ========================================
    
    // I. Actividades económicas en estimación directa, modalidad normal o simplificada
    
    // Casilla 01: Ingresos computables del trimestre (acumulados)
    private BigDecimal ingresosComputables;
    
    // Casilla 02: Gastos fiscalmente deducibles (acumulados)
    private BigDecimal gastosDeducibles;
    
    // Casilla 03: Rendimiento neto (01 - 02)
    private BigDecimal rendimientoNeto;
    
    // Casilla 04: 20% del rendimiento neto (03 x 0.20)
    private BigDecimal pagoCuenta20;
    
    // Casilla 05: Retenciones e ingresos a cuenta soportados (acumulados)
    private BigDecimal retencionesIngresoCuenta;
    
    // Casilla 06: Pagos fraccionados ingresados en trimestres anteriores
    private BigDecimal pagosFraccionadosAnteriores;
    
    // Casilla 07: Resultado (04 - 05 - 06)
    private BigDecimal resultadoPrevio;
    
    // ========================================
    // RESULTADO FINAL
    // ========================================
    
    // Casilla 12: Resultado de la autoliquidación (suma de actividades)
    private BigDecimal resultado;
    
    // Casilla 13: A deducir (si resultado negativo de períodos anteriores)
    private BigDecimal aDeducir;
    
    // Casilla 14: Total (12 - 13)
    private BigDecimal total;
    
    // Tipo de resultado
    private String tipoResultado; // "INGRESAR", "NEGATIVO", "CERO"
    
    // ========================================
    // DATOS ADICIONALES PARA CÁLCULO
    // ========================================
    
    // Ingresos del trimestre actual (no acumulado)
    private BigDecimal ingresosTrimestre;
    
    // Gastos del trimestre actual (no acumulado)
    private BigDecimal gastosTrimestre;
    
    // Porcentaje de reducción por inicio de actividad (si aplica)
    private BigDecimal reduccionInicioActividad;
    
    // Es el primer año de actividad
    private boolean primerAnoActividad;
    
    // Fecha de presentación
    private String fechaPresentacion;
    
    // ========================================
    // INFORMACIÓN ADICIONAL
    // ========================================
    
    // Número de facturas emitidas en el período
    private int numFacturasEmitidas;
    
    // Número de gastos registrados
    private int numGastosRegistrados;
    
    /**
     * Calcula el pago fraccionado
     */
    public void calcularPagoFraccionado() {
        // Rendimiento neto = Ingresos - Gastos
        if (ingresosComputables == null) ingresosComputables = BigDecimal.ZERO;
        if (gastosDeducibles == null) gastosDeducibles = BigDecimal.ZERO;
        
        this.rendimientoNeto = ingresosComputables.subtract(gastosDeducibles);
        
        // Si hay rendimiento positivo, calculamos el 20%
        if (rendimientoNeto.compareTo(BigDecimal.ZERO) > 0) {
            this.pagoCuenta20 = rendimientoNeto.multiply(new BigDecimal("0.20"))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            this.pagoCuenta20 = BigDecimal.ZERO;
        }
        
        // Resultado previo
        if (retencionesIngresoCuenta == null) retencionesIngresoCuenta = BigDecimal.ZERO;
        if (pagosFraccionadosAnteriores == null) pagosFraccionadosAnteriores = BigDecimal.ZERO;
        
        this.resultadoPrevio = pagoCuenta20
                .subtract(retencionesIngresoCuenta)
                .subtract(pagosFraccionadosAnteriores);
        
        // Resultado final
        this.resultado = resultadoPrevio;
        
        if (aDeducir == null) aDeducir = BigDecimal.ZERO;
        this.total = resultado.subtract(aDeducir);
        
        // Tipo de resultado
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            this.tipoResultado = "INGRESAR";
        } else if (total.compareTo(BigDecimal.ZERO) < 0) {
            this.tipoResultado = "NEGATIVO";
        } else {
            this.tipoResultado = "CERO";
        }
    }
    
    /**
     * Obtiene el trimestre como número
     */
    public int getNumeroTrimestre() {
        if (periodo == null) return 1;
        return switch (periodo) {
            case "1T" -> 1;
            case "2T" -> 2;
            case "3T" -> 3;
            case "4T" -> 4;
            default -> 1;
        };
    }
}

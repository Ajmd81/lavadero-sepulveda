package com.lavaderosepulveda.crm.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ResumenFinancieroDTO {
    
    // Totales generales
    private BigDecimal totalIngresos;
    private BigDecimal totalGastos;
    private BigDecimal beneficioBruto;
    
    // Facturas emitidas
    private int numeroFacturasEmitidas;
    private BigDecimal baseFacturasEmitidas;
    private BigDecimal ivaRepercutido;
    
    // Facturas recibidas
    private int numeroFacturasRecibidas;
    private BigDecimal baseFacturasRecibidas;
    private BigDecimal ivaSoportadoFacturas;
    
    // Gastos
    private int numeroGastos;
    private BigDecimal totalGastosOperativos;
    private BigDecimal ivaSoportadoGastos;
    
    // Liquidación IVA
    private BigDecimal liquidacionIva;
    
    // Desglose por categoría
    private List<Map<String, Object>> desglosePorCategoria;
    
    // Evolución mensual
    private List<Map<String, Object>> evolucionMensual;
    
    // Pendientes
    private BigDecimal pendienteCobro;
    private BigDecimal pendientePago;
    
    // Getters y Setters
    
    public BigDecimal getTotalIngresos() {
        return totalIngresos;
    }
    
    public void setTotalIngresos(BigDecimal totalIngresos) {
        this.totalIngresos = totalIngresos;
    }
    
    public BigDecimal getTotalGastos() {
        return totalGastos;
    }
    
    public void setTotalGastos(BigDecimal totalGastos) {
        this.totalGastos = totalGastos;
    }
    
    public BigDecimal getBeneficioBruto() {
        return beneficioBruto;
    }
    
    public void setBeneficioBruto(BigDecimal beneficioBruto) {
        this.beneficioBruto = beneficioBruto;
    }
    
    public int getNumeroFacturasEmitidas() {
        return numeroFacturasEmitidas;
    }
    
    public void setNumeroFacturasEmitidas(int numeroFacturasEmitidas) {
        this.numeroFacturasEmitidas = numeroFacturasEmitidas;
    }
    
    public BigDecimal getBaseFacturasEmitidas() {
        return baseFacturasEmitidas;
    }
    
    public void setBaseFacturasEmitidas(BigDecimal baseFacturasEmitidas) {
        this.baseFacturasEmitidas = baseFacturasEmitidas;
    }
    
    public BigDecimal getIvaRepercutido() {
        return ivaRepercutido;
    }
    
    public void setIvaRepercutido(BigDecimal ivaRepercutido) {
        this.ivaRepercutido = ivaRepercutido;
    }
    
    public int getNumeroFacturasRecibidas() {
        return numeroFacturasRecibidas;
    }
    
    public void setNumeroFacturasRecibidas(int numeroFacturasRecibidas) {
        this.numeroFacturasRecibidas = numeroFacturasRecibidas;
    }
    
    public BigDecimal getBaseFacturasRecibidas() {
        return baseFacturasRecibidas;
    }
    
    public void setBaseFacturasRecibidas(BigDecimal baseFacturasRecibidas) {
        this.baseFacturasRecibidas = baseFacturasRecibidas;
    }
    
    public BigDecimal getIvaSoportadoFacturas() {
        return ivaSoportadoFacturas;
    }
    
    public void setIvaSoportadoFacturas(BigDecimal ivaSoportadoFacturas) {
        this.ivaSoportadoFacturas = ivaSoportadoFacturas;
    }
    
    public int getNumeroGastos() {
        return numeroGastos;
    }
    
    public void setNumeroGastos(int numeroGastos) {
        this.numeroGastos = numeroGastos;
    }
    
    public BigDecimal getTotalGastosOperativos() {
        return totalGastosOperativos;
    }
    
    public void setTotalGastosOperativos(BigDecimal totalGastosOperativos) {
        this.totalGastosOperativos = totalGastosOperativos;
    }
    
    public BigDecimal getIvaSoportadoGastos() {
        return ivaSoportadoGastos;
    }
    
    public void setIvaSoportadoGastos(BigDecimal ivaSoportadoGastos) {
        this.ivaSoportadoGastos = ivaSoportadoGastos;
    }
    
    public BigDecimal getLiquidacionIva() {
        return liquidacionIva;
    }
    
    public void setLiquidacionIva(BigDecimal liquidacionIva) {
        this.liquidacionIva = liquidacionIva;
    }
    
    public List<Map<String, Object>> getDesglosePorCategoria() {
        return desglosePorCategoria;
    }
    
    public void setDesglosePorCategoria(List<Map<String, Object>> desglosePorCategoria) {
        this.desglosePorCategoria = desglosePorCategoria;
    }
    
    public List<Map<String, Object>> getEvolucionMensual() {
        return evolucionMensual;
    }
    
    public void setEvolucionMensual(List<Map<String, Object>> evolucionMensual) {
        this.evolucionMensual = evolucionMensual;
    }
    
    public BigDecimal getPendienteCobro() {
        return pendienteCobro;
    }
    
    public void setPendienteCobro(BigDecimal pendienteCobro) {
        this.pendienteCobro = pendienteCobro;
    }
    
    public BigDecimal getPendientePago() {
        return pendientePago;
    }
    
    public void setPendientePago(BigDecimal pendientePago) {
        this.pendientePago = pendientePago;
    }
    
    // Métodos de utilidad
    
    public BigDecimal getIvaSoportadoTotal() {
        BigDecimal total = BigDecimal.ZERO;
        if (ivaSoportadoFacturas != null) total = total.add(ivaSoportadoFacturas);
        if (ivaSoportadoGastos != null) total = total.add(ivaSoportadoGastos);
        return total;
    }
    
    public BigDecimal getMargenPorcentaje() {
        if (totalIngresos == null || totalIngresos.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return beneficioBruto.multiply(new BigDecimal("100")).divide(totalIngresos, 2, java.math.RoundingMode.HALF_UP);
    }
}

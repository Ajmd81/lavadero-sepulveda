package com.lavaderosepulveda.crm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    // Métodos de utilidad

    public BigDecimal getIvaSoportadoTotal() {
        BigDecimal total = BigDecimal.ZERO;
        if (ivaSoportadoFacturas != null)
            total = total.add(ivaSoportadoFacturas);
        if (ivaSoportadoGastos != null)
            total = total.add(ivaSoportadoGastos);
        return total;
    }

    public BigDecimal getMargenPorcentaje() {
        if (totalIngresos == null || totalIngresos.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return beneficioBruto.multiply(new BigDecimal("100")).divide(totalIngresos, 2, java.math.RoundingMode.HALF_UP);
    }
}

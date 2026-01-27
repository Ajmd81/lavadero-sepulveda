package com.lavaderosepulveda.app.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ResumenFinancieroDTO {

    // Ingresos (Facturas Emitidas)
    private BigDecimal totalFacturasEmitidas;
    private BigDecimal totalIvaRepercutido;
    private Integer numFacturasEmitidas;
    private BigDecimal facturasPendientesCobro;

    // Gastos (Facturas Recibidas + Gastos)
    private BigDecimal totalFacturasRecibidas;
    private BigDecimal totalGastos;
    private BigDecimal totalIvaSoportado;
    private Integer numFacturasRecibidas;
    private Integer numGastos;
    private BigDecimal facturasPendientesPago;

    // Liquidación IVA
    private BigDecimal ivaRepercutido;
    private BigDecimal ivaSoportado;
    private BigDecimal resultadoIva; // Repercutido - Soportado

    // Resultado
    private BigDecimal totalIngresos;
    private BigDecimal baseImponible; // Total Ingresos - IVA Repercutido
    private BigDecimal totalGastosGeneral;
    private BigDecimal baseGastos; // Total Gastos - IVA Soportado
    private BigDecimal beneficioBruto;

    // Desglose por categoría
    private List<CategoriaResumenDTO> gastosPorCategoria;

    // Evolución mensual
    private List<MesResumenDTO> evolucionMensual;

    // Constructores
    public ResumenFinancieroDTO() {
    }

    // Getters y Setters
    public BigDecimal getTotalFacturasEmitidas() {
        return totalFacturasEmitidas;
    }

    public void setTotalFacturasEmitidas(BigDecimal totalFacturasEmitidas) {
        this.totalFacturasEmitidas = totalFacturasEmitidas;
    }

    public BigDecimal getTotalIvaRepercutido() {
        return totalIvaRepercutido;
    }

    public void setTotalIvaRepercutido(BigDecimal totalIvaRepercutido) {
        this.totalIvaRepercutido = totalIvaRepercutido;
    }

    public Integer getNumFacturasEmitidas() {
        return numFacturasEmitidas;
    }

    public void setNumFacturasEmitidas(Integer numFacturasEmitidas) {
        this.numFacturasEmitidas = numFacturasEmitidas;
    }

    public BigDecimal getFacturasPendientesCobro() {
        return facturasPendientesCobro;
    }

    public void setFacturasPendientesCobro(BigDecimal facturasPendientesCobro) {
        this.facturasPendientesCobro = facturasPendientesCobro;
    }

    public BigDecimal getTotalFacturasRecibidas() {
        return totalFacturasRecibidas;
    }

    public void setTotalFacturasRecibidas(BigDecimal totalFacturasRecibidas) {
        this.totalFacturasRecibidas = totalFacturasRecibidas;
    }

    public BigDecimal getTotalGastos() {
        return totalGastos;
    }

    public void setTotalGastos(BigDecimal totalGastos) {
        this.totalGastos = totalGastos;
    }

    public BigDecimal getTotalIvaSoportado() {
        return totalIvaSoportado;
    }

    public void setTotalIvaSoportado(BigDecimal totalIvaSoportado) {
        this.totalIvaSoportado = totalIvaSoportado;
    }

    public Integer getNumFacturasRecibidas() {
        return numFacturasRecibidas;
    }

    public void setNumFacturasRecibidas(Integer numFacturasRecibidas) {
        this.numFacturasRecibidas = numFacturasRecibidas;
    }

    public Integer getNumGastos() {
        return numGastos;
    }

    public void setNumGastos(Integer numGastos) {
        this.numGastos = numGastos;
    }

    public BigDecimal getFacturasPendientesPago() {
        return facturasPendientesPago;
    }

    public void setFacturasPendientesPago(BigDecimal facturasPendientesPago) {
        this.facturasPendientesPago = facturasPendientesPago;
    }

    public BigDecimal getIvaRepercutido() {
        return ivaRepercutido;
    }

    public void setIvaRepercutido(BigDecimal ivaRepercutido) {
        this.ivaRepercutido = ivaRepercutido;
    }

    public BigDecimal getIvaSoportado() {
        return ivaSoportado;
    }

    public void setIvaSoportado(BigDecimal ivaSoportado) {
        this.ivaSoportado = ivaSoportado;
    }

    public BigDecimal getResultadoIva() {
        return resultadoIva;
    }

    public void setResultadoIva(BigDecimal resultadoIva) {
        this.resultadoIva = resultadoIva;
    }

    public BigDecimal getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(BigDecimal totalIngresos) {
        this.totalIngresos = totalIngresos;
    }

    public BigDecimal getTotalGastosGeneral() {
        return totalGastosGeneral;
    }

    public void setTotalGastosGeneral(BigDecimal totalGastosGeneral) {
        this.totalGastosGeneral = totalGastosGeneral;
    }

    public BigDecimal getBaseImponible() {
        return baseImponible;
    }

    public void setBaseImponible(BigDecimal baseImponible) {
        this.baseImponible = baseImponible;
    }

    public BigDecimal getBaseGastos() {
        return baseGastos;
    }

    public void setBaseGastos(BigDecimal baseGastos) {
        this.baseGastos = baseGastos;
    }

    public BigDecimal getBeneficioBruto() {
        return beneficioBruto;
    }

    public void setBeneficioBruto(BigDecimal beneficioBruto) {
        this.beneficioBruto = beneficioBruto;
    }

    public List<CategoriaResumenDTO> getGastosPorCategoria() {
        return gastosPorCategoria;
    }

    public void setGastosPorCategoria(List<CategoriaResumenDTO> gastosPorCategoria) {
        this.gastosPorCategoria = gastosPorCategoria;
    }

    public List<MesResumenDTO> getEvolucionMensual() {
        return evolucionMensual;
    }

    public void setEvolucionMensual(List<MesResumenDTO> evolucionMensual) {
        this.evolucionMensual = evolucionMensual;
    }

    // DTOs internos
    public static class CategoriaResumenDTO {
        private String categoria;
        private String descripcion;
        private Integer cantidad;
        private BigDecimal total;

        public CategoriaResumenDTO() {
        }

        public CategoriaResumenDTO(String categoria, String descripcion, Integer cantidad, BigDecimal total) {
            this.categoria = categoria;
            this.descripcion = descripcion;
            this.cantidad = cantidad;
            this.total = total;
        }

        public String getCategoria() {
            return categoria;
        }

        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }
    }

    public static class MesResumenDTO {
        private Integer year;
        private Integer mes;
        private String mesNombre;
        private BigDecimal ingresos;
        private BigDecimal gastos;
        private BigDecimal beneficio;

        public MesResumenDTO() {
        }

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public Integer getMes() {
            return mes;
        }

        public void setMes(Integer mes) {
            this.mes = mes;
        }

        public String getMesNombre() {
            return mesNombre;
        }

        public void setMesNombre(String mesNombre) {
            this.mesNombre = mesNombre;
        }

        public BigDecimal getIngresos() {
            return ingresos;
        }

        public void setIngresos(BigDecimal ingresos) {
            this.ingresos = ingresos;
        }

        public BigDecimal getGastos() {
            return gastos;
        }

        public void setGastos(BigDecimal gastos) {
            this.gastos = gastos;
        }

        public BigDecimal getBeneficio() {
            return beneficio;
        }

        public void setBeneficio(BigDecimal beneficio) {
            this.beneficio = beneficio;
        }
    }
}

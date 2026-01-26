package com.lavaderosepulveda.app.dto;

import java.math.BigDecimal;
import java.util.List;

public class ContabilidadResumenDTO {
    private BigDecimal ingresosTotales;
    private BigDecimal baseImponible;
    private BigDecimal ivaRepercutido;
    private Integer numFacturas;
    private List<ResumenMensualDTO> resumenMensual;
    private List<ResumenClienteDTO> resumenCliente;

    // Getters y Setters
    public BigDecimal getIngresosTotales() {
        return ingresosTotales;
    }

    public void setIngresosTotales(BigDecimal ingresosTotales) {
        this.ingresosTotales = ingresosTotales;
    }

    public BigDecimal getBaseImponible() {
        return baseImponible;
    }

    public void setBaseImponible(BigDecimal baseImponible) {
        this.baseImponible = baseImponible;
    }

    public BigDecimal getIvaRepercutido() {
        return ivaRepercutido;
    }

    public void setIvaRepercutido(BigDecimal ivaRepercutido) {
        this.ivaRepercutido = ivaRepercutido;
    }

    public Integer getNumFacturas() {
        return numFacturas;
    }

    public void setNumFacturas(Integer numFacturas) {
        this.numFacturas = numFacturas;
    }

    public List<ResumenMensualDTO> getResumenMensual() {
        return resumenMensual;
    }

    public void setResumenMensual(List<ResumenMensualDTO> resumenMensual) {
        this.resumenMensual = resumenMensual;
    }

    public List<ResumenClienteDTO> getResumenCliente() {
        return resumenCliente;
    }

    public void setResumenCliente(List<ResumenClienteDTO> resumenCliente) {
        this.resumenCliente = resumenCliente;
    }

    // DTOs internos
    public static class ResumenMensualDTO {
        private String mes;
        private BigDecimal total;
        private BigDecimal base;
        private BigDecimal iva;
        private Integer numFacturas; // ← CAMPO AÑADIDO

        public String getMes() {
            return mes;
        }

        public void setMes(String mes) {
            this.mes = mes;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }

        public BigDecimal getBase() {
            return base;
        }

        public void setBase(BigDecimal base) {
            this.base = base;
        }

        public BigDecimal getIva() {
            return iva;
        }

        public void setIva(BigDecimal iva) {
            this.iva = iva;
        }

        public Integer getNumFacturas() {
            return numFacturas;
        } // ← GETTER AÑADIDO

        public void setNumFacturas(Integer numFacturas) {
            this.numFacturas = numFacturas;
        } // ← SETTER AÑADIDO
    }

    public static class ResumenClienteDTO {
        private String nombreCliente;
        private BigDecimal total;
        private Integer numFacturas;

        public String getNombreCliente() {
            return nombreCliente;
        }

        public void setNombreCliente(String nombreCliente) {
            this.nombreCliente = nombreCliente;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }

        public Integer getNumFacturas() {
            return numFacturas;
        }

        public void setNumFacturas(Integer numFacturas) {
            this.numFacturas = numFacturas;
        }
    }
}
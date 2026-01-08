package com.lavaderosepulveda.crm.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/**
 * DTO para Factura Emitida
 * Incluye @JsonAlias para compatibilidad con diferentes nombres de campos de la
 * API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacturaEmitidaDTO {
    private Long id;

    @JsonAlias({ "numero", "numeroFactura", "numero_factura" })
    private String numeroFactura;

    @JsonAlias({ "fecha", "fechaEmision", "fecha_emision" })
    private String fechaEmision;

    private Long clienteId;

    @JsonAlias({ "clienteNombre", "cliente_nombre", "nombreCliente" })
    private String clienteNombre;

    @JsonAlias({ "clienteNif", "cliente_nif", "nifCliente" })
    private String clienteNif;

    @JsonAlias({ "clienteDireccion", "cliente_direccion", "direccionCliente" })
    private String clienteDireccion;

    @JsonAlias({ "concepto", "descripcion" })
    private String concepto;

    @JsonAlias({ "tipoFactura", "tipo_factura", "tipo" })
    private String tipoFactura;

    @JsonAlias({ "baseImponible", "base_imponible", "base" })
    private BigDecimal baseImponible;

    @JsonAlias({ "tipoIva", "tipo_iva", "porcentajeIva" })
    private BigDecimal tipoIva;

    @JsonAlias({ "cuotaIva", "cuota_iva", "iva" })
    private BigDecimal cuotaIva;

    private BigDecimal total;
    private String estado;

    @JsonAlias({ "metodoPago", "metodo_pago", "formaPago" })
    private String metodoPago;

    @JsonAlias({ "fechaPago", "fecha_pago" })
    private String fechaPago;

    @JsonAlias({ "citaId", "cita_id" })
    private Long citaId;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public String getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(String fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteNif() {
        return clienteNif;
    }

    public void setClienteNif(String clienteNif) {
        this.clienteNif = clienteNif;
    }

    public String getClienteDireccion() {
        return clienteDireccion;
    }

    public void setClienteDireccion(String clienteDireccion) {
        this.clienteDireccion = clienteDireccion;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public String getTipoFactura() {
        return tipoFactura;
    }

    public void setTipoFactura(String tipoFactura) {
        this.tipoFactura = tipoFactura;
    }

    public BigDecimal getBaseImponible() {
        return baseImponible;
    }

    public void setBaseImponible(BigDecimal baseImponible) {
        this.baseImponible = baseImponible;
    }

    public BigDecimal getTipoIva() {
        return tipoIva;
    }

    public void setTipoIva(BigDecimal tipoIva) {
        this.tipoIva = tipoIva;
    }

    public BigDecimal getCuotaIva() {
        return cuotaIva;
    }

    public void setCuotaIva(BigDecimal cuotaIva) {
        this.cuotaIva = cuotaIva;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(String fechaPago) {
        this.fechaPago = fechaPago;
    }

    public Long getCitaId() {
        return citaId;
    }

    public void setCitaId(Long citaId) {
        this.citaId = citaId;
    }

    @Override
    public String toString() {
        return "FacturaEmitidaDTO{" +
                "id=" + id +
                ", numeroFactura='" + numeroFactura + '\'' +
                ", fechaEmision='" + fechaEmision + '\'' +
                ", clienteNombre='" + clienteNombre + '\'' +
                ", concepto='" + concepto + '\'' +
                ", total=" + total +
                ", estado='" + estado + '\'' +
                '}';
    }
}
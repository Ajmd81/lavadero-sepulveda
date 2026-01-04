package com.lavaderosepulveda.app.dto;

import java.math.BigDecimal;

public class FacturaRecibidaDTO {
    private Long id;
    private String numeroFactura;
    private Long proveedorId;
    private String proveedorNombre;
    private String proveedorNif;
    private String fechaFactura;      // dd/MM/yyyy
    private String fechaVencimiento;  // dd/MM/yyyy
    private String fechaPago;         // dd/MM/yyyy
    private String categoria;
    private String concepto;
    private BigDecimal baseImponible;
    private BigDecimal tipoIva;
    private BigDecimal cuotaIva;
    private BigDecimal tipoIrpf;
    private BigDecimal cuotaIrpf;
    private BigDecimal total;
    private String estado;
    private String metodoPago;
    private String documentoAdjunto;
    private String notas;

    // Constructores
    public FacturaRecibidaDTO() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public Long getProveedorId() { return proveedorId; }
    public void setProveedorId(Long proveedorId) { this.proveedorId = proveedorId; }

    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }

    public String getProveedorNif() { return proveedorNif; }
    public void setProveedorNif(String proveedorNif) { this.proveedorNif = proveedorNif; }

    public String getFechaFactura() { return fechaFactura; }
    public void setFechaFactura(String fechaFactura) { this.fechaFactura = fechaFactura; }

    public String getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(String fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public String getFechaPago() { return fechaPago; }
    public void setFechaPago(String fechaPago) { this.fechaPago = fechaPago; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public BigDecimal getBaseImponible() { return baseImponible; }
    public void setBaseImponible(BigDecimal baseImponible) { this.baseImponible = baseImponible; }

    public BigDecimal getTipoIva() { return tipoIva; }
    public void setTipoIva(BigDecimal tipoIva) { this.tipoIva = tipoIva; }

    public BigDecimal getCuotaIva() { return cuotaIva; }
    public void setCuotaIva(BigDecimal cuotaIva) { this.cuotaIva = cuotaIva; }

    public BigDecimal getTipoIrpf() { return tipoIrpf; }
    public void setTipoIrpf(BigDecimal tipoIrpf) { this.tipoIrpf = tipoIrpf; }

    public BigDecimal getCuotaIrpf() { return cuotaIrpf; }
    public void setCuotaIrpf(BigDecimal cuotaIrpf) { this.cuotaIrpf = cuotaIrpf; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getDocumentoAdjunto() { return documentoAdjunto; }
    public void setDocumentoAdjunto(String documentoAdjunto) { this.documentoAdjunto = documentoAdjunto; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}

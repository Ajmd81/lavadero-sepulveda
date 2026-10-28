package com.lavaderosepulveda.app.dto.facturacion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class FacturaRecibidaDTO {
    private Long id;
    @Size(max = 50)
    private String numeroFactura;
    @Positive
    private Long proveedorId;
    @Size(max = 150)
    private String proveedorNombre;
    @Size(max = 20)
    @Pattern(regexp = "^$|^[A-Za-z0-9\\-]{5,20}$", message = "NIF/CIF con formato inválido")
    private String proveedorNif;
    @Pattern(regexp = "^$|^\\d{2}/\\d{2}/\\d{4}$|^\\d{4}-\\d{2}-\\d{2}$", message = "Fecha con formato inválido (dd/MM/yyyy)")
    private String fechaFactura;      // dd/MM/yyyy
    @Pattern(regexp = "^$|^\\d{2}/\\d{2}/\\d{4}$|^\\d{4}-\\d{2}-\\d{2}$", message = "Fecha con formato inválido (dd/MM/yyyy)")
    private String fechaVencimiento;  // dd/MM/yyyy
    @Pattern(regexp = "^$|^\\d{2}/\\d{2}/\\d{4}$|^\\d{4}-\\d{2}-\\d{2}$", message = "Fecha con formato inválido (dd/MM/yyyy)")
    private String fechaPago;         // dd/MM/yyyy
    @Size(max = 50)
    private String categoria;
    @Size(max = 255)
    private String concepto;
    @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
    private BigDecimal baseImponible;
    @DecimalMin(value = "0", message = "No puede ser negativo")
    @DecimalMax(value = "100", message = "No puede superar 100")
    @Digits(integer = 3, fraction = 4)
    private BigDecimal tipoIva;
    @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
    private BigDecimal cuotaIva;
    @DecimalMin(value = "0", message = "No puede ser negativo")
    @DecimalMax(value = "100", message = "No puede superar 100")
    @Digits(integer = 3, fraction = 4)
    private BigDecimal tipoIrpf;
    @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
    private BigDecimal cuotaIrpf;
    @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
    private BigDecimal total;
    @Size(max = 30)
    private String estado;
    @Size(max = 30)
    private String metodoPago;
    @Size(max = 500)
    private String documentoAdjunto;
    @Size(max = 2000)
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

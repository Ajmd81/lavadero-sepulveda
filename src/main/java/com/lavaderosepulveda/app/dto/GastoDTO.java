package com.lavaderosepulveda.app.dto;

import java.math.BigDecimal;

public class GastoDTO {
    private Long id;
    private String concepto;
    private String fecha;          // dd/MM/yyyy
    private String categoria;
    private BigDecimal importe;
    private Boolean ivaIncluido;
    private BigDecimal baseImponible;
    private BigDecimal cuotaIva;
    private Long facturaRecibidaId;
    private String metodoPago;
    private Boolean recurrente;
    private Integer diaRecurrencia;
    private String notas;
    private Boolean pagado;

    // Constructores
    public GastoDTO() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }

    public Boolean getIvaIncluido() { return ivaIncluido; }
    public void setIvaIncluido(Boolean ivaIncluido) { this.ivaIncluido = ivaIncluido; }

    public BigDecimal getBaseImponible() { return baseImponible; }
    public void setBaseImponible(BigDecimal baseImponible) { this.baseImponible = baseImponible; }

    public BigDecimal getCuotaIva() { return cuotaIva; }
    public void setCuotaIva(BigDecimal cuotaIva) { this.cuotaIva = cuotaIva; }

    public Long getFacturaRecibidaId() { return facturaRecibidaId; }
    public void setFacturaRecibidaId(Long facturaRecibidaId) { this.facturaRecibidaId = facturaRecibidaId; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public Boolean getRecurrente() { return recurrente; }
    public void setRecurrente(Boolean recurrente) { this.recurrente = recurrente; }

    public Integer getDiaRecurrencia() { return diaRecurrencia; }
    public void setDiaRecurrencia(Integer diaRecurrencia) { this.diaRecurrencia = diaRecurrencia; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public Boolean getPagado() { return pagado; }
    public void setPagado(Boolean pagado) { this.pagado = pagado; }
}

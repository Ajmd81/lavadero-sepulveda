package com.lavaderosepulveda.app.dto.facturacion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class GastoDTO {
    private Long id;
    @NotBlank(message = "El concepto es obligatorio")
    @Size(max = 255)
    private String concepto;
    @Pattern(regexp = "^$|^\\d{2}/\\d{2}/\\d{4}$|^\\d{4}-\\d{2}-\\d{2}$", message = "Fecha con formato inválido (dd/MM/yyyy)")
    private String fecha;          // dd/MM/yyyy
    @Size(max = 50)
    private String categoria;
    @NotNull(message = "El importe es obligatorio")
    @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
    private BigDecimal importe;
    private Boolean ivaIncluido;
    @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
    private BigDecimal baseImponible;
    @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
    private BigDecimal cuotaIva;
    @Positive
    private Long facturaRecibidaId;
    @Size(max = 30)
    private String metodoPago;
    private Boolean recurrente;
    @Min(1)
    @Max(31)
    private Integer diaRecurrencia;
    @Size(max = 2000)
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

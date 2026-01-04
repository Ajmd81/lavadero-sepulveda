package com.lavaderosepulveda.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "gastos")
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String concepto;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaGasto categoria;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal importe;

    @Column(name = "iva_incluido")
    private Boolean ivaIncluido = true; // Si el importe incluye IVA

    @Column(name = "base_imponible", precision = 10, scale = 2)
    private BigDecimal baseImponible;

    @Column(name = "cuota_iva", precision = 10, scale = 2)
    private BigDecimal cuotaIva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_recibida_id")
    private FacturaRecibida facturaRecibida; // Vinculado a factura si existe

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    private MetodoPago metodoPago;

    private Boolean recurrente = false; // Gasto fijo mensual

    @Column(name = "dia_recurrencia")
    private Integer diaRecurrencia; // Día del mes para gastos recurrentes

    private String notas;

    private Boolean pagado = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calcularDesglose();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calcularDesglose();
    }

    public void calcularDesglose() {
        if (importe != null && ivaIncluido != null && ivaIncluido) {
            // Si el importe incluye IVA, calcular base
            // Base = Total / 1.21
            baseImponible = importe.divide(new BigDecimal("1.21"), 2, java.math.RoundingMode.HALF_UP);
            cuotaIva = importe.subtract(baseImponible);
        } else if (importe != null) {
            // El importe es la base
            baseImponible = importe;
            cuotaIva = BigDecimal.ZERO;
        }
    }

    // Constructores
    public Gasto() {}

    public Gasto(String concepto, LocalDate fecha, CategoriaGasto categoria, BigDecimal importe) {
        this.concepto = concepto;
        this.fecha = fecha;
        this.categoria = categoria;
        this.importe = importe;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public CategoriaGasto getCategoria() { return categoria; }
    public void setCategoria(CategoriaGasto categoria) { this.categoria = categoria; }

    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { 
        this.importe = importe;
        calcularDesglose();
    }

    public Boolean getIvaIncluido() { return ivaIncluido; }
    public void setIvaIncluido(Boolean ivaIncluido) { 
        this.ivaIncluido = ivaIncluido;
        calcularDesglose();
    }

    public BigDecimal getBaseImponible() { return baseImponible; }
    public void setBaseImponible(BigDecimal baseImponible) { this.baseImponible = baseImponible; }

    public BigDecimal getCuotaIva() { return cuotaIva; }
    public void setCuotaIva(BigDecimal cuotaIva) { this.cuotaIva = cuotaIva; }

    public FacturaRecibida getFacturaRecibida() { return facturaRecibida; }
    public void setFacturaRecibida(FacturaRecibida facturaRecibida) { this.facturaRecibida = facturaRecibida; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public Boolean getRecurrente() { return recurrente; }
    public void setRecurrente(Boolean recurrente) { this.recurrente = recurrente; }

    public Integer getDiaRecurrencia() { return diaRecurrencia; }
    public void setDiaRecurrencia(Integer diaRecurrencia) { this.diaRecurrencia = diaRecurrencia; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public Boolean getPagado() { return pagado; }
    public void setPagado(Boolean pagado) { this.pagado = pagado; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

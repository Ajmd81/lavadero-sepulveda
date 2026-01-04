package com.lavaderosepulveda.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "facturas_recibidas")
public class FacturaRecibida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_factura", nullable = false)
    private String numeroFactura; // Número de factura del proveedor

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    // Datos del proveedor (por si no está dado de alta)
    @Column(name = "proveedor_nombre")
    private String proveedorNombre;

    @Column(name = "proveedor_nif")
    private String proveedorNif;

    @Column(name = "fecha_factura", nullable = false)
    private LocalDate fechaFactura;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaGasto categoria;

    private String concepto;

    @Column(name = "base_imponible", precision = 10, scale = 2, nullable = false)
    private BigDecimal baseImponible;

    @Column(name = "tipo_iva", precision = 5, scale = 2)
    private BigDecimal tipoIva = new BigDecimal("21.00");

    @Column(name = "cuota_iva", precision = 10, scale = 2)
    private BigDecimal cuotaIva;

    @Column(name = "tipo_irpf", precision = 5, scale = 2)
    private BigDecimal tipoIrpf = BigDecimal.ZERO; // Retención IRPF si aplica

    @Column(name = "cuota_irpf", precision = 10, scale = 2)
    private BigDecimal cuotaIrpf = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoFactura estado = EstadoFactura.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    private MetodoPago metodoPago;

    @Column(name = "documento_adjunto")
    private String documentoAdjunto; // Ruta al PDF escaneado

    private String notas;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calcularTotales();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calcularTotales();
    }

    public void calcularTotales() {
        if (baseImponible != null) {
            // Calcular IVA
            if (tipoIva != null) {
                cuotaIva = baseImponible.multiply(tipoIva).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            } else {
                cuotaIva = BigDecimal.ZERO;
            }
            
            // Calcular IRPF (si aplica)
            if (tipoIrpf != null && tipoIrpf.compareTo(BigDecimal.ZERO) > 0) {
                cuotaIrpf = baseImponible.multiply(tipoIrpf).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            } else {
                cuotaIrpf = BigDecimal.ZERO;
            }
            
            // Total = Base + IVA - IRPF
            total = baseImponible.add(cuotaIva).subtract(cuotaIrpf);
        }
    }

    // Constructores
    public FacturaRecibida() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { 
        this.proveedor = proveedor;
        if (proveedor != null) {
            this.proveedorNombre = proveedor.getNombre();
            this.proveedorNif = proveedor.getNif();
        }
    }

    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }

    public String getProveedorNif() { return proveedorNif; }
    public void setProveedorNif(String proveedorNif) { this.proveedorNif = proveedorNif; }

    public LocalDate getFechaFactura() { return fechaFactura; }
    public void setFechaFactura(LocalDate fechaFactura) { this.fechaFactura = fechaFactura; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }

    public CategoriaGasto getCategoria() { return categoria; }
    public void setCategoria(CategoriaGasto categoria) { this.categoria = categoria; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public BigDecimal getBaseImponible() { return baseImponible; }
    public void setBaseImponible(BigDecimal baseImponible) { 
        this.baseImponible = baseImponible;
        calcularTotales();
    }

    public BigDecimal getTipoIva() { return tipoIva; }
    public void setTipoIva(BigDecimal tipoIva) { 
        this.tipoIva = tipoIva;
        calcularTotales();
    }

    public BigDecimal getCuotaIva() { return cuotaIva; }
    public void setCuotaIva(BigDecimal cuotaIva) { this.cuotaIva = cuotaIva; }

    public BigDecimal getTipoIrpf() { return tipoIrpf; }
    public void setTipoIrpf(BigDecimal tipoIrpf) { 
        this.tipoIrpf = tipoIrpf;
        calcularTotales();
    }

    public BigDecimal getCuotaIrpf() { return cuotaIrpf; }
    public void setCuotaIrpf(BigDecimal cuotaIrpf) { this.cuotaIrpf = cuotaIrpf; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public EstadoFactura getEstado() { return estado; }
    public void setEstado(EstadoFactura estado) { this.estado = estado; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public String getDocumentoAdjunto() { return documentoAdjunto; }
    public void setDocumentoAdjunto(String documentoAdjunto) { this.documentoAdjunto = documentoAdjunto; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

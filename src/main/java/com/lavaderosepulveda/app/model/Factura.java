package com.lavaderosepulveda.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facturas", indexes = {
    @Index(name = "idx_facturas_numero", columnList = "numero", unique = true),
    @Index(name = "idx_facturas_fecha", columnList = "fecha"),
    @Index(name = "idx_facturas_cliente_id", columnList = "cliente_id"),
    @Index(name = "idx_facturas_estado", columnList = "estado")
})
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Número de factura: 2025/001
    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    // Año de la factura (para generar numeración)
    @Column(nullable = false)
    private Integer anio;

    // Número secuencial dentro del año
    @Column(name = "numero_secuencial", nullable = false)
    private Integer numeroSecuencial;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoFactura tipo = TipoFactura.SIMPLIFICADA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoFactura estado = EstadoFactura.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", length = 15)
    private MetodoPago metodoPago;

    // Relación con cliente (opcional para factura simplificada)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // Datos del cliente en el momento de la factura (se guardan por si cambian después)
    @Column(name = "cliente_nombre")
    private String clienteNombre;

    @Column(name = "cliente_nif", length = 20)
    private String clienteNif;

    @Column(name = "cliente_direccion")
    private String clienteDireccion;

    @Column(name = "cliente_email")
    private String clienteEmail;

    @Column(name = "cliente_telefono", length = 20)
    private String clienteTelefono;

    // Líneas de la factura
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaFactura> lineas = new ArrayList<>();

    // Importes
    @Column(name = "base_imponible", precision = 10, scale = 2, nullable = false)
    private BigDecimal baseImponible = BigDecimal.ZERO;

    @Column(name = "tipo_iva", precision = 5, scale = 2, nullable = false)
    private BigDecimal tipoIva = new BigDecimal("21.00");

    @Column(name = "importe_iva", precision = 10, scale = 2, nullable = false)
    private BigDecimal importeIva = BigDecimal.ZERO;

    @Column(name = "total", precision = 10, scale = 2, nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    // Fecha de pago (cuando se marca como pagada)
    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    // Observaciones
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    // Auditoría
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructores
    public Factura() {
    }

    // Callbacks JPA
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (fecha == null) {
            fecha = LocalDate.now();
        }
        if (anio == null) {
            anio = fecha.getYear();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Métodos de utilidad
    public void addLinea(LineaFactura linea) {
        lineas.add(linea);
        linea.setFactura(this);
        recalcularTotales();
    }

    public void removeLinea(LineaFactura linea) {
        lineas.remove(linea);
        linea.setFactura(null);
        recalcularTotales();
    }

    public void recalcularTotales() {
        this.baseImponible = lineas.stream()
            .map(LineaFactura::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.importeIva = baseImponible.multiply(tipoIva).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        this.total = baseImponible.add(importeIva);
    }

    public void marcarComoPagada(MetodoPago metodoPago) {
        this.estado = EstadoFactura.PAGADA;
        this.metodoPago = metodoPago;
        this.fechaPago = LocalDate.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public Integer getNumeroSecuencial() {
        return numeroSecuencial;
    }

    public void setNumeroSecuencial(Integer numeroSecuencial) {
        this.numeroSecuencial = numeroSecuencial;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public TipoFactura getTipo() {
        return tipo;
    }

    public void setTipo(TipoFactura tipo) {
        this.tipo = tipo;
    }

    public EstadoFactura getEstado() {
        return estado;
    }

    public void setEstado(EstadoFactura estado) {
        this.estado = estado;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
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

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    public List<LineaFactura> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaFactura> lineas) {
        this.lineas = lineas;
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

    public BigDecimal getImporteIva() {
        return importeIva;
    }

    public void setImporteIva(BigDecimal importeIva) {
        this.importeIva = importeIva;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Factura{" +
                "id=" + id +
                ", numero='" + numero + '\'' +
                ", fecha=" + fecha +
                ", tipo=" + tipo +
                ", estado=" + estado +
                ", total=" + total +
                '}';
    }
}

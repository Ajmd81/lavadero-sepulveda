package com.lavaderosepulveda.app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "albaranes")
@Data
public class Albaran {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String numero; // ALB-2025-0001
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    
    @Column(nullable = false)
    private LocalDate fecha;
    
    @OneToMany(mappedBy = "albaran", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaAlbaran> lineas = new ArrayList<>();
    
    @Column(precision = 10, scale = 2)
    private BigDecimal baseImponible;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal iva;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal total;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoAlbaran estado = EstadoAlbaran.PENDIENTE;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private Factura factura; // Relación con factura cuando se facture
    
    @Column(updatable = false)
    private LocalDateTime fechaCreacion;
    
    private LocalDateTime fechaModificacion;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
    
    // Métodos auxiliares
    public void calcularTotales() {
        baseImponible = lineas.stream()
            .map(LineaAlbaran::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        iva = lineas.stream()
            .map(LineaAlbaran::getIva)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        total = baseImponible.add(iva);
    }
    
    public enum EstadoAlbaran {
        PENDIENTE,
        ENTREGADO,
        FACTURADO
    }
}
package com.lavaderosepulveda.app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "lineas_albaran")
@Data
public class LineaAlbaran {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "albaran_id", nullable = false)
    private Albaran albaran;
    
    @Column(nullable = false)
    private String concepto;
    
    @Column(nullable = false)
    private Integer cantidad;
    
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitario;
    
    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal tipoIva = new BigDecimal("21.00"); // IVA por defecto 21%
    
    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal iva;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal total;
    
    @PrePersist
    @PreUpdate
    public void calcular() {
        if (cantidad != null && precioUnitario != null && tipoIva != null) {
            subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
            iva = subtotal.multiply(tipoIva).divide(new BigDecimal("100"));
            total = subtotal.add(iva);
        }
    }
}
package com.lavaderosepulveda.crm.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "servicios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private Double precio;

    @Column(name = "duracion_estimada")
    private Integer duracionEstimada; // en minutos

    @Column(nullable = false)
    private Boolean activo = true;

    // Categorías de servicios
    @Column(nullable = false)
    private String categoria; // LAVADO_BASICO, LAVADO_COMPLETO, LAVADO_PREMIUM, ENCERADO, PULIDO, etc.

    // IVA aplicable
    @Column(nullable = false)
    private Double iva = 21.0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Double getPrecioConIva() {
        return precio * (1 + iva / 100);
    }

    public Double getImporteIva() {
        return precio * (iva / 100);
    }
}

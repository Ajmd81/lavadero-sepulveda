package com.lavaderosepulveda.crm.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellidos;

    @Column(unique = true, nullable = false)
    private String telefono;

    @Column(unique = true)
    private String email;

    @Column(length = 20)
    private String nif;

    private String direccion;
    
    private String codigoPostal;
    
    private String ciudad;
    
    private String provincia;

    // Datos del vehículo
    private String matricula;
    
    private String marca;
    
    private String modelo;
    
    private String color;

    // Estadísticas del cliente
    @Column(name = "total_citas")
    private Integer totalCitas = 0;

    @Column(name = "citas_completadas")
    private Integer citasCompletadas = 0;

    @Column(name = "citas_canceladas")
    private Integer citasCanceladas = 0;

    @Column(name = "citas_no_presentadas")
    private Integer citasNoPresentadas = 0;

    @Column(name = "total_facturado", precision = 10, scale = 2)
    private Double totalFacturado = 0.0;

    @Column(name = "fecha_primera_cita")
    private LocalDateTime fechaPrimeraCita;

    @Column(name = "fecha_ultima_cita")
    private LocalDateTime fechaUltimaCita;

    // Notas y observaciones
    @Column(length = 1000)
    private String notas;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relaciones
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cita> citas = new ArrayList<>();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Factura> facturas = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Métodos de utilidad
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    public Double getTicketMedio() {
        if (citasCompletadas == null || citasCompletadas == 0) {
            return 0.0;
        }
        return totalFacturado / citasCompletadas;
    }

    public Double getTasaCompletacion() {
        if (totalCitas == null || totalCitas == 0) {
            return 0.0;
        }
        return (citasCompletadas.doubleValue() / totalCitas) * 100;
    }

    public Double getTasaNoPresentacion() {
        if (totalCitas == null || totalCitas == 0) {
            return 0.0;
        }
        return (citasNoPresentadas.doubleValue() / totalCitas) * 100;
    }
}

package com.lavaderosepulveda.crm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "citas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "duracion_estimada")
    private Integer duracionEstimada; // en minutos

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado = EstadoCita.PENDIENTE;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "cita_servicios",
        joinColumns = @JoinColumn(name = "cita_id"),
        inverseJoinColumns = @JoinColumn(name = "servicio_id")
    )
    private List<Servicio> servicios = new ArrayList<>();

    @Column(length = 500)
    private String observaciones;

    @Column(name = "recordatorio_enviado")
    private Boolean recordatorioEnviado = false;

    @Column(name = "confirmacion_enviada")
    private Boolean confirmacionEnviada = false;

    // Datos del vehículo en el momento de la cita (puede cambiar)
    private String matricula;
    
    private String marcaModelo;

    // Control de tiempos
    @Column(name = "hora_llegada")
    private LocalDateTime horaLlegada;

    @Column(name = "hora_inicio")
    private LocalDateTime horaInicio;

    @Column(name = "hora_fin")
    private LocalDateTime horaFin;

    // Facturación
    @Column(name = "facturada")
    private Boolean facturada = false;

    @OneToOne(mappedBy = "cita")
    private Factura factura;

    @Column(name = "motivo_cancelacion")
    private String motivoCancelacion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (servicios != null && !servicios.isEmpty()) {
            duracionEstimada = servicios.stream()
                .mapToInt(s -> s.getDuracionEstimada() != null ? s.getDuracionEstimada() : 30)
                .sum();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Métodos de utilidad
    public Double getImporteTotal() {
        if (servicios == null || servicios.isEmpty()) {
            return 0.0;
        }
        return servicios.stream()
            .mapToDouble(Servicio::getPrecioConIva)
            .sum();
    }

    public Double getImporteBase() {
        if (servicios == null || servicios.isEmpty()) {
            return 0.0;
        }
        return servicios.stream()
            .mapToDouble(Servicio::getPrecio)
            .sum();
    }

    public Double getImporteIva() {
        return getImporteTotal() - getImporteBase();
    }

    public boolean isPendiente() {
        return estado == EstadoCita.PENDIENTE;
    }

    public boolean isCompletada() {
        return estado == EstadoCita.COMPLETADA;
    }

    public boolean isCancelada() {
        return estado == EstadoCita.CANCELADA;
    }

    public boolean isNoPresentado() {
        return estado == EstadoCita.NO_PRESENTADO;
    }

    public String getEstadoColor() {
        return estado.getColor();
    }
}

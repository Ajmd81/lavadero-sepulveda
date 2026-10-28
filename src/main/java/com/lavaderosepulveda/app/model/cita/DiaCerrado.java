package com.lavaderosepulveda.app.model.cita;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dias_cerrados", indexes = {
        @Index(name = "idx_dias_cerrados_fecha", columnList = "fecha", unique = true)
})
public class DiaCerrado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @jakarta.validation.constraints.NotNull(message = "La fecha es obligatoria")
    @Column(nullable = false, unique = true)
    private LocalDate fecha;

    /**
     * Tipo de cierre: FESTIVO, VACACIONES, MANTENIMIENTO, OTRO
     */
    @jakarta.validation.constraints.Size(max = 30, message = "El tipo no puede superar 30 caracteres")
    @Column(nullable = false, length = 30)
    private String tipo = "FESTIVO";

    @jakarta.validation.constraints.Size(max = 255, message = "El motivo no puede superar 255 caracteres")
    @Column(length = 255)
    private String motivo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────

    public DiaCerrado() {}

    public DiaCerrado(LocalDate fecha, String tipo, String motivo) {
        this.fecha  = fecha;
        this.tipo   = tipo;
        this.motivo = motivo;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public Long getId()                  { return id; }
    public void setId(Long id)           { this.id = id; }

    public LocalDate getFecha()          { return fecha; }
    public void setFecha(LocalDate f)    { this.fecha = f; }

    public String getTipo()              { return tipo; }
    public void setTipo(String t)        { this.tipo = t; }

    public String getMotivo()            { return motivo; }
    public void setMotivo(String m)      { this.motivo = m; }

    public LocalDateTime getCreatedAt()  { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}
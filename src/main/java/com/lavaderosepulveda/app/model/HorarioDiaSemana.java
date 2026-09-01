package com.lavaderosepulveda.app.model;

import com.lavaderosepulveda.app.model.enums.DiaSemana;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Horarios del lavadero por día de la semana
 * 
 * Permite definir dos franjas horarias por día:
 * - Franja Mañana: aperturaMañana - cierreMañana
 * - Franja Tarde: aperturaTarde - cierreTarde
 * 
 * Si una franja es NULL, se considera cerrado en ese horario
 * Ejemplo: Viernes solo abierto de mañana
 *   - aperturaMañana: 09:00, cierreMañana: 14:00
 *   - aperturaTarde: NULL, cierreTarde: NULL
 */
@Entity
@Table(name = "horario_dia_semana", uniqueConstraints = {
    @UniqueConstraint(columnNames = "dia_semana")
})
public class HorarioDiaSemana {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Día de la semana (LUNES, MARTES, ..., DOMINGO)
     */
    @NotNull(message = "El día de la semana es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, unique = true)
    private DiaSemana diaSemana;

    /**
     * Hora de apertura en la franja de mañana
     * NULL = Cerrado en la mañana
     */
    @Column(name = "apertura_manana")
    private LocalTime aperturaMañana;

    /**
     * Hora de cierre en la franja de mañana
     * NULL = Cerrado en la mañana
     */
    @Column(name = "cierre_manana")
    private LocalTime cierreMañana;

    /**
     * Hora de apertura en la franja de tarde
     * NULL = Cerrado en la tarde
     */
    @Column(name = "apertura_tarde")
    private LocalTime aperturaTarde;

    /**
     * Hora de cierre en la franja de tarde
     * NULL = Cerrado en la tarde
     */
    @Column(name = "cierre_tarde")
    private LocalTime cierreTarde;

    /**
     * Indica si este día está activo
     * false = Cierre total (como si fuera festivo)
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    /**
     * Fecha de última actualización
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Constructores ─────────────────────────────────────────────────────

    public HorarioDiaSemana() {
    }

    public HorarioDiaSemana(DiaSemana diaSemana, LocalTime aperturaMañana, LocalTime cierreMañana,
                            LocalTime aperturaTarde, LocalTime cierreTarde) {
        this.diaSemana = diaSemana;
        this.aperturaMañana = aperturaMañana;
        this.cierreMañana = cierreMañana;
        this.aperturaTarde = aperturaTarde;
        this.cierreTarde = cierreTarde;
        this.activo = true;
    }

    // ── Callbacks JPA ─────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Validaciones ──────────────────────────────────────────────────────

    /**
     * Valida que si hay apertura, hay cierre
     * Valida que apertura < cierre
     */
    public boolean isHorarioValido() {
        // Validar franja mañana
        if ((aperturaMañana != null && cierreMañana == null) ||
            (aperturaMañana == null && cierreMañana != null)) {
            return false; // Incompleta
        }
        if (aperturaMañana != null && cierreMañana != null &&
            !aperturaMañana.isBefore(cierreMañana)) {
            return false; // Apertura >= cierre
        }

        // Validar franja tarde
        if ((aperturaTarde != null && cierreTarde == null) ||
            (aperturaTarde == null && cierreTarde != null)) {
            return false; // Incompleta
        }
        if (aperturaTarde != null && cierreTarde != null &&
            !aperturaTarde.isBefore(cierreTarde)) {
            return false; // Apertura >= cierre
        }

        return true;
    }

    /**
     * Valida que si hay dos franjas, mañana < tarde
     */
    public boolean isSeparacionValida() {
        if (aperturaMañana != null && cierreMañana != null &&
            aperturaTarde != null && cierreTarde != null) {
            // Debe haber separación: cierre de mañana < apertura de tarde
            return cierreMañana.isBefore(aperturaTarde);
        }
        return true; // OK si no hay ambas franjas
    }

    /**
     * Comprueba si el día está abierto en alguna franja
     */
    public boolean estaAbierto() {
        if (!activo) {
            return false;
        }
        return (aperturaMañana != null && cierreMañana != null) ||
               (aperturaTarde != null && cierreTarde != null);
    }

    // ── Getters y Setters ─────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DiaSemana getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(DiaSemana diaSemana) {
        this.diaSemana = diaSemana;
    }

    public LocalTime getAperturaMañana() {
        return aperturaMañana;
    }

    public void setAperturaMañana(LocalTime aperturaMañana) {
        this.aperturaMañana = aperturaMañana;
    }

    public LocalTime getCierreMañana() {
        return cierreMañana;
    }

    public void setCierreMañana(LocalTime cierreMañana) {
        this.cierreMañana = cierreMañana;
    }

    public LocalTime getAperturaTarde() {
        return aperturaTarde;
    }

    public void setAperturaTarde(LocalTime aperturaTarde) {
        this.aperturaTarde = aperturaTarde;
    }

    public LocalTime getCierreTarde() {
        return cierreTarde;
    }

    public void setCierreTarde(LocalTime cierreTarde) {
        this.cierreTarde = cierreTarde;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "HorarioDiaSemana{" +
                "id=" + id +
                ", diaSemana=" + diaSemana +
                ", aperturaMañana=" + aperturaMañana +
                ", cierreMañana=" + cierreMañana +
                ", aperturaTarde=" + aperturaTarde +
                ", cierreTarde=" + cierreTarde +
                ", activo=" + activo +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
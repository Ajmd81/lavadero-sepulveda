package com.lavaderosepulveda.app.model;

import com.lavaderosepulveda.app.model.enums.ModoHorario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Configuración centralizada de horarios del lavadero
 * Entidad singleton: siempre tiene ID = 1
 * 
 * Campos editables desde el CRM:
 * - duracionCitaMinutos: Duración de cada cita (30, 45, 60 minutos)
 * - citasPorHora: Cantidad de citas que caben en 1 hora
 * - modoHorario: SOLO_MAÑANA, SOLO_TARDE, COMPLETO
 * - horaApertura: Hora de apertura (ej: 08:00)
 * - horaCierre: Hora de cierre (ej: 20:00)
 */
@Entity
@Table(name = "configuracion_horario")
public class ConfiguracionHorario {

    @Id
    @Column(nullable = false)
    private Long id = 1L; // Singleton: siempre ID = 1

    /**
     * Duración de cada cita en minutos: 30, 45 o 60
     */
    @NotNull(message = "La duración de la cita es obligatoria")
    @Min(value = 15, message = "La duración mínima es 15 minutos")
    @Column(name = "duracion_cita_minutos", nullable = false)
    private Integer duracionCitaMinutos = 60;

    /**
     * Cantidad de citas que pueden agendarse en 1 hora
     * Ejemplo: si duracion es 30 min, puede haber 2 citas por hora
     */
    @NotNull(message = "Las citas por hora son obligatorias")
    @Min(value = 1, message = "Mínimo 1 cita por hora")
    @Column(name = "citas_por_hora", nullable = false)
    private Integer citasPorHora = 2;

    /**
     * Modo de horario:
     * - SOLO_MAÑANA: Abierto solo en la mañana
     * - SOLO_TARDE: Abierto solo en la tarde
     * - COMPLETO: Abierto mañana y tarde
     */
    @NotNull(message = "El modo de horario es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "modo_horario", nullable = false, length = 30)
    private ModoHorario modoHorario = ModoHorario.COMPLETO;

    /**
     * Hora de apertura del lavadero (ej: 08:00)
     */
    @NotNull(message = "La hora de apertura es obligatoria")
    @Column(name = "hora_apertura", nullable = false)
    private LocalTime horaApertura = LocalTime.of(8, 0);

    /**
     * Hora de cierre del lavadero (ej: 20:00)
     */
    @NotNull(message = "La hora de cierre es obligatoria")
    @Column(name = "hora_cierre", nullable = false)
    private LocalTime horaCierre = LocalTime.of(20, 0);

    /**
     * Fecha de última actualización
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Constructores ─────────────────────────────────────────────────────

    public ConfiguracionHorario() {
    }

    public ConfiguracionHorario(Integer duracionCitaMinutos, Integer citasPorHora,
                                 ModoHorario modoHorario, LocalTime horaApertura, LocalTime horaCierre) {
        this.id = 1L;
        this.duracionCitaMinutos = duracionCitaMinutos;
        this.citasPorHora = citasPorHora;
        this.modoHorario = modoHorario;
        this.horaApertura = horaApertura;
        this.horaCierre = horaCierre;
    }

    // ── Callbacks JPA ─────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = 1L;
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Validaciones ──────────────────────────────────────────────────────

    /**
     * Valida que la duración de la cita sea coherente con citasPorHora
     * Ejemplo: 60 minutos / 2 citas = 30 minutos por cita ✓
     */
    public boolean isConfiguracionValida() {
        if (duracionCitaMinutos == null || citasPorHora == null) {
            return false;
        }

        int minutosPorCita = 60 / citasPorHora;
        return duracionCitaMinutos <= minutosPorCita;
    }

    /**
     * Valida que horaApertura < horaCierre
     */
    public boolean isHorarioValido() {
        return horaApertura != null && horaCierre != null && horaApertura.isBefore(horaCierre);
    }

    // ── Getters y Setters ─────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = (id == null) ? 1L : id;
    }

    public Integer getDuracionCitaMinutos() {
        return duracionCitaMinutos;
    }

    public void setDuracionCitaMinutos(Integer duracionCitaMinutos) {
        this.duracionCitaMinutos = duracionCitaMinutos;
    }

    public Integer getCitasPorHora() {
        return citasPorHora;
    }

    public void setCitasPorHora(Integer citasPorHora) {
        this.citasPorHora = citasPorHora;
    }

    public ModoHorario getModoHorario() {
        return modoHorario;
    }

    public void setModoHorario(ModoHorario modoHorario) {
        this.modoHorario = modoHorario;
    }

    public LocalTime getHoraApertura() {
        return horaApertura;
    }

    public void setHoraApertura(LocalTime horaApertura) {
        this.horaApertura = horaApertura;
    }

    public LocalTime getHoraCierre() {
        return horaCierre;
    }

    public void setHoraCierre(LocalTime horaCierre) {
        this.horaCierre = horaCierre;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ConfiguracionHorario{" +
                "id=" + id +
                ", duracionCitaMinutos=" + duracionCitaMinutos +
                ", citasPorHora=" + citasPorHora +
                ", modoHorario=" + modoHorario +
                ", horaApertura=" + horaApertura +
                ", horaCierre=" + horaCierre +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
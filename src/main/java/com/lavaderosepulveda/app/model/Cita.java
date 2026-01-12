package com.lavaderosepulveda.app.model;

import com.lavaderosepulveda.app.model.enums.EstadoCita;
import com.lavaderosepulveda.app.model.enums.TipoLavado;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "citas", indexes = {
        @Index(name = "idx_citas_fecha", columnList = "fecha"),
        @Index(name = "idx_citas_estado", columnList = "estado"),
        @Index(name = "idx_citas_telefono", columnList = "telefono"),
        @Index(name = "idx_citas_cliente_id", columnList = "cliente_id")
})
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email no válido")
    @Column(nullable = false)
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Column(nullable = false)
    private String telefono;

    @NotBlank(message = "El modelo del vehículo es obligatorio")
    @Column(name = "modelo_vehiculo", nullable = false)
    private String modeloVehiculo;

    @NotNull(message = "El tipo de lavado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_lavado", nullable = false)
    @Convert(converter = TipoLavadoConverter.class)
    private TipoLavado tipoLavado;

    @NotNull(message = "La fecha es obligatoria")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    @Column(nullable = false)
    private LocalTime hora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado = EstadoCita.PENDIENTE; // Valor por defecto

    @Column(name = "pago_adelantado", nullable = false)
    private Boolean pagoAdelantado = false; // Valor por defecto

    @Column(name = "referencia_pago")
    private String referenciaPago;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    // ========================================
    // CAMPOS ADICIONALES PARA CRM
    // ========================================

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "duracion_estimada")
    private Integer duracionEstimada; // en minutos

    @Column(name = "hora_llegada")
    private LocalTime horaLlegada;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Column(name = "recordatorio_enviado")
    private Boolean recordatorioEnviado = false;

    @Column(name = "confirmacion_enviada")
    private Boolean confirmacionEnviada = false;

    @Column(name = "facturada")
    private Boolean facturada = false;

    @Column(name = "factura_id")
    private Long facturaId;

    // Matrícula del vehículo (separada del modelo)
    private String matricula;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructores
    public Cita() {
    }

    public Cita(String nombre, String email, String telefono, String modeloVehiculo, TipoLavado tipoLavado, LocalDate fecha, LocalTime hora) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.modeloVehiculo = modeloVehiculo;
        this.tipoLavado = tipoLavado;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = EstadoCita.PENDIENTE; // Por defecto
        this.pagoAdelantado = false; // Por defecto
    }

    // Callbacks JPA
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (duracionEstimada == null) {
            duracionEstimada = 60; // 1 hora por defecto
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters y Setters existentes
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getModeloVehiculo() {
        return modeloVehiculo;
    }

    public void setModeloVehiculo(String modeloVehiculo) {
        this.modeloVehiculo = modeloVehiculo;
    }

    public TipoLavado getTipoLavado() {
        return tipoLavado;
    }

    public void setTipoLavado(TipoLavado tipoLavado) {
        this.tipoLavado = tipoLavado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    // NUEVOS GETTERS Y SETTERS
    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public Boolean isPagoAdelantado() {
        return pagoAdelantado;
    }

    public void setPagoAdelantado(Boolean pagoAdelantado) {
        this.pagoAdelantado = pagoAdelantado;
    }

    public String getReferenciaPago() {
        return referenciaPago;
    }

    public void setReferenciaPago(String referenciaPago) {
        this.referenciaPago = referenciaPago;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    // ========================================
    // GETTERS Y SETTERS CAMPOS CRM
    // ========================================

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Integer getDuracionEstimada() {
        return duracionEstimada;
    }

    public void setDuracionEstimada(Integer duracionEstimada) {
        this.duracionEstimada = duracionEstimada;
    }

    public LocalTime getHoraLlegada() {
        return horaLlegada;
    }

    public void setHoraLlegada(LocalTime horaLlegada) {
        this.horaLlegada = horaLlegada;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Boolean getRecordatorioEnviado() {
        return recordatorioEnviado;
    }

    public void setRecordatorioEnviado(Boolean recordatorioEnviado) {
        this.recordatorioEnviado = recordatorioEnviado;
    }

    public Boolean getConfirmacionEnviada() {
        return confirmacionEnviada;
    }

    public void setConfirmacionEnviada(Boolean confirmacionEnviada) {
        this.confirmacionEnviada = confirmacionEnviada;
    }

    public Boolean getFacturada() {
        return facturada;
    }

    public void setFacturada(Boolean facturada) {
        this.facturada = facturada;
    }

    public Long getFacturaId() {
        return facturaId;
    }

    public void setFacturaId(Long facturaId) {
        this.facturaId = facturaId;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
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
        return "Cita{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", telefono='" + telefono + '\'' +
                ", modeloVehiculo='" + modeloVehiculo + '\'' +
                ", tipoLavado=" + tipoLavado +
                ", fecha=" + fecha +
                ", hora=" + hora +
                ", estado=" + estado +
                ", pagoAdelantado=" + pagoAdelantado +
                ", clienteId=" + clienteId +
                ", facturada=" + facturada +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }
}
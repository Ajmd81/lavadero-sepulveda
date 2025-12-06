package com.lavaderosepulveda.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "citas")
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
                ", referenciaPago='" + referenciaPago + '\'' +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }
}
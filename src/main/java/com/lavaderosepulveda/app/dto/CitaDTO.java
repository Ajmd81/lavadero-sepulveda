package com.lavaderosepulveda.app.dto;

import com.lavaderosepulveda.app.model.enums.TipoLavado;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO completo para transferencia de datos de Cita
 * Incluye todos los campos necesarios para el sistema de pagos y observaciones
 */
public class CitaDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email no válido")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotBlank(message = "El modelo del vehículo es obligatorio")
    private String modeloVehiculo;

    @NotNull(message = "El tipo de lavado es obligatorio")
    private TipoLavado tipoLavado;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    // Campos adicionales para gestión de estado y pagos
    private String estado = "PENDIENTE";

    private Boolean pagoAdelantado = false;

    private String referenciaPago;

    private String numeroBizum;

    private String observaciones;

    // Constructores
    public CitaDTO() {
    }

    public CitaDTO(String nombre, String email, String telefono, String modeloVehiculo,
                   TipoLavado tipoLavado, LocalDate fecha, LocalTime hora) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.modeloVehiculo = modeloVehiculo;
        this.tipoLavado = tipoLavado;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = "PENDIENTE";
        this.pagoAdelantado = false;
    }

    // Getters y Setters
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Boolean getPagoAdelantado() {
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

    public String getNumeroBizum() {
        return numeroBizum;
    }

    public void setNumeroBizum(String numeroBizum) {
        this.numeroBizum = numeroBizum;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "CitaDTO{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", telefono='" + telefono + '\'' +
                ", modeloVehiculo='" + modeloVehiculo + '\'' +
                ", tipoLavado=" + tipoLavado +
                ", fecha=" + fecha +
                ", hora=" + hora +
                ", estado='" + estado + '\'' +
                ", pagoAdelantado=" + pagoAdelantado +
                ", referenciaPago='" + referenciaPago + '\'' +
                ", numeroBizum='" + numeroBizum + '\'' +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }
}
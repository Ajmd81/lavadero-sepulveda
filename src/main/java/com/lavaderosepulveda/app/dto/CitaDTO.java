package com.lavaderosepulveda.app.dto;

import com.lavaderosepulveda.app.model.enums.TipoLavado;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO completo para transferencia de datos de Cita.
 * Incluye validaciones defensivas contra bots y entradas maliciosas.
 */
public class CitaDTO {

    private Long id;

    // ─── NOMBRE ───────────────────────────────────────────────────────────────
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Pattern(
        regexp = "^[\\p{L}\\s'\\-\\.]+$",
        message = "El nombre solo puede contener letras, espacios, guiones y puntos"
    )
    private String nombre;

    // ─── EMAIL ────────────────────────────────────────────────────────────────
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email no válido")
    @Size(max = 150, message = "El email no puede superar 150 caracteres")
    private String email;

    // ─── TELÉFONO ─────────────────────────────────────────────────────────────
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(
        regexp = "^[6789]\\d{8}$",
        message = "El teléfono debe ser un número español válido (9 dígitos empezando por 6, 7, 8 o 9)"
    )
    private String telefono;

    // ─── MODELO VEHÍCULO ──────────────────────────────────────────────────────
    @NotBlank(message = "El modelo del vehículo es obligatorio")
    @Size(max = 100, message = "El modelo no puede superar 100 caracteres")
    @Pattern(
        regexp = "^[\\p{L}\\p{N}\\s\\-\\.]+$",
        message = "El modelo del vehículo contiene caracteres no permitidos"
    )
    private String modeloVehiculo;

    // ─── TIPO LAVADO ──────────────────────────────────────────────────────────
    @NotNull(message = "El tipo de lavado es obligatorio")
    private TipoLavado tipoLavado;

    // ─── FECHA ────────────────────────────────────────────────────────────────
    @NotNull(message = "La fecha es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Future(message = "La fecha debe ser futura")
    private LocalDate fecha;

    // ─── HORA ─────────────────────────────────────────────────────────────────
    @NotNull(message = "La hora es obligatoria")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime hora;

    // ─── CAMPOS OPCIONALES (sin restricciones estrictas pero sí de longitud) ──

    private String estado = "PENDIENTE";

    private Boolean pagoAdelantado = false;

    @Size(max = 100, message = "La referencia de pago no puede superar 100 caracteres")
    private String referenciaPago;

    @Pattern(
        regexp = "^[6789]\\d{8}$|^$",
        message = "El número de Bizum debe ser un teléfono español válido o estar vacío"
    )
    private String numeroBizum;

    @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres")
    private String observaciones;

    // ─── CONSTRUCTORES ────────────────────────────────────────────────────────

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

    // ─── GETTERS Y SETTERS ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getModeloVehiculo() { return modeloVehiculo; }
    public void setModeloVehiculo(String modeloVehiculo) { this.modeloVehiculo = modeloVehiculo; }

    public TipoLavado getTipoLavado() { return tipoLavado; }
    public void setTipoLavado(TipoLavado tipoLavado) { this.tipoLavado = tipoLavado; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Boolean getPagoAdelantado() { return pagoAdelantado; }
    public void setPagoAdelantado(Boolean pagoAdelantado) { this.pagoAdelantado = pagoAdelantado; }

    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }

    public String getNumeroBizum() { return numeroBizum; }
    public void setNumeroBizum(String numeroBizum) { this.numeroBizum = numeroBizum; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

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
                '}';
    }
}
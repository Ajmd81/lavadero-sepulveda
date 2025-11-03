package com.lavaderosepulveda.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
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

    // Campos para el control de asistencia
    @Column(name = "asistida")
    private Boolean asistida;  // true: asistió, false: faltó, null: pendiente

    @Column(name = "faltas")
    private Integer faltas;    // contador de faltas acumuladas

    // Campos para gestión de pagos
    @Column(name = "precio_total", precision = 10, scale = 2)
    private BigDecimal precioTotal;

    @Column(name = "deposito", precision = 10, scale = 2)
    private BigDecimal deposito;

    @Column(name = "deposito_pagado")
    private Boolean depositoPagado;

    @Column(name = "referencia_bizum", length = 50)
    private String referenciaBizum;

    @Column(name = "fecha_pago_deposito")
    private LocalDate fechaPagoDeposito;

    // Constructores
    public Cita() {
        this.faltas = 0;
        this.depositoPagado = false;
    }

    public Cita(String nombre, String email, String telefono, String modeloVehiculo, TipoLavado tipoLavado, LocalDate fecha, LocalTime hora) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.modeloVehiculo = modeloVehiculo;
        this.tipoLavado = tipoLavado;
        this.fecha = fecha;
        this.hora = hora;
        this.faltas = 0;
        this.depositoPagado = false;

        // Calcular precio total y depósito si el tipo de lavado está definido
        if (tipoLavado != null) {
            this.precioTotal = tipoLavado.getPrecio();
            this.deposito = this.precioTotal.multiply(new BigDecimal("0.5"));
        }
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

    public Boolean getAsistida() {
        return asistida;
    }

    public void setAsistida(Boolean asistida) {
        this.asistida = asistida;
    }

    public Integer getFaltas() {
        return faltas;
    }

    public void setFaltas(Integer faltas) {
        this.faltas = faltas;
    }

    public BigDecimal getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(BigDecimal precioTotal) {
        this.precioTotal = precioTotal;
    }

    public BigDecimal getDeposito() {
        return deposito;
    }

    public void setDeposito(BigDecimal deposito) {
        this.deposito = deposito;
    }

    public Boolean getDepositoPagado() {
        return depositoPagado;
    }

    public void setDepositoPagado(Boolean depositoPagado) {
        this.depositoPagado = depositoPagado;
    }

    public String getReferenciaBizum() {
        return referenciaBizum;
    }

    public void setReferenciaBizum(String referenciaBizum) {
        this.referenciaBizum = referenciaBizum;
    }

    public LocalDate getFechaPagoDeposito() {
        return fechaPagoDeposito;
    }

    public void setFechaPagoDeposito(LocalDate fechaPagoDeposito) {
        this.fechaPagoDeposito = fechaPagoDeposito;
    }

    // Método para calcular el saldo pendiente
    public BigDecimal getSaldoPendiente() {
        if (precioTotal == null) {
            return BigDecimal.ZERO;
        }

        if (depositoPagado != null && depositoPagado && deposito != null) {
            return precioTotal.subtract(deposito);
        }

        return precioTotal;
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
                ", asistida=" + asistida +
                ", faltas=" + faltas +
                ", precioTotal=" + precioTotal +
                ", deposito=" + deposito +
                ", depositoPagado=" + depositoPagado +
                ", referenciaBizum='" + referenciaBizum + '\'' +
                ", fechaPagoDeposito=" + fechaPagoDeposito +
                '}';
    }
}
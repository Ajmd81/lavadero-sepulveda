package com.lavaderosepulveda.app.dto;

import java.math.BigDecimal;

/**
 * DTO para mostrar estadísticas de clientes
 * con sus reservas y gastos totales
 */
public class ClienteEstadisticaDTO {

    private String nombre;
    private String telefono;
    private String email;
    private Long totalReservas;
    private BigDecimal totalGastado;
    private String servicioMasFrecuente;

    /**
     * Constructor utilizado por el query JPQL principal
     * que calcula nombre, teléfono, email, total reservas y total gastado
     */
    public ClienteEstadisticaDTO(String nombre, String telefono, String email,
                                 Long totalReservas, Double totalGastado) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.totalReservas = totalReservas;
        this.totalGastado = BigDecimal.valueOf(totalGastado);
        this.servicioMasFrecuente = ""; // Se llenará después
    }

    /**
     * Constructor completo para uso general
     */
    public ClienteEstadisticaDTO(String nombre, String telefono, String email,
                                 Long totalReservas, BigDecimal totalGastado,
                                 String servicioMasFrecuente) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.totalReservas = totalReservas;
        this.totalGastado = totalGastado;
        this.servicioMasFrecuente = servicioMasFrecuente;
    }

    // Getters y Setters

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getTotalReservas() {
        return totalReservas;
    }

    public void setTotalReservas(Long totalReservas) {
        this.totalReservas = totalReservas;
    }

    public BigDecimal getTotalGastado() {
        return totalGastado;
    }

    public void setTotalGastado(BigDecimal totalGastado) {
        this.totalGastado = totalGastado;
    }

    public String getServicioMasFrecuente() {
        return servicioMasFrecuente;
    }

    public void setServicioMasFrecuente(String servicioMasFrecuente) {
        this.servicioMasFrecuente = servicioMasFrecuente;
    }

    @Override
    public String toString() {
        return "ClienteEstadisticaDTO{" +
                "nombre='" + nombre + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                ", totalReservas=" + totalReservas +
                ", totalGastado=" + totalGastado +
                ", servicioMasFrecuente='" + servicioMasFrecuente + '\'' +
                '}';
    }
}
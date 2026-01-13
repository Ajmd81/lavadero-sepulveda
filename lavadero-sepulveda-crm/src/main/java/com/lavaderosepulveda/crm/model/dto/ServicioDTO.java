package com.lavaderosepulveda.crm.model.dto;

import java.time.LocalDateTime;

public class ServicioDTO {
    
    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer duracionEstimada; // en minutos
    private Boolean activo;
    private String categoria;
    private Double iva;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructores
    public ServicioDTO() {
    }

    public ServicioDTO(Long id, String nombre, String descripcion, Double precio, 
                       Integer duracionEstimada, Boolean activo, String categoria, 
                       Double iva, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.duracionEstimada = duracionEstimada;
        this.activo = activo;
        this.categoria = categoria;
        this.iva = iva;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Métodos calculados
    public Double getPrecioConIva() {
        return precio * (1 + (iva != null ? iva : 21.0) / 100);
    }

    public Double getImporteIva() {
        return precio * ((iva != null ? iva : 21.0) / 100);
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getDuracionEstimada() {
        return duracionEstimada;
    }

    public void setDuracionEstimada(Integer duracionEstimada) {
        this.duracionEstimada = duracionEstimada;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Double getIva() {
        return iva;
    }

    public void setIva(Double iva) {
        this.iva = iva;
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
        return "ServicioDTO{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio=" + precio +
                ", duracionEstimada=" + duracionEstimada +
                ", activo=" + activo +
                ", categoria='" + categoria + '\'' +
                ", iva=" + iva +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
package com.lavaderosepulveda.app.model.enums;

public enum EstadoCita {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    EN_PROCESO("En proceso"),
    COMPLETADA("Completada"),
    NO_PRESENTADO("No presentado"),
    CANCELADA("Cancelada");

    private final String descripcion;

    EstadoCita(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
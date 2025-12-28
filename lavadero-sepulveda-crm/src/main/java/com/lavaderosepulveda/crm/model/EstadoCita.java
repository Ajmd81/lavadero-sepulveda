package com.lavaderosepulveda.crm.model;

public enum EstadoCita {
    PENDIENTE("Pendiente", "#FFA500"),
    CONFIRMADA("Confirmada", "#4CAF50"),
    EN_PROCESO("En Proceso", "#2196F3"),
    COMPLETADA("Completada", "#8BC34A"),
    CANCELADA("Cancelada", "#F44336"),
    NO_PRESENTADO("No Presentado", "#9E9E9E");

    private final String nombre;
    private final String color;

    EstadoCita(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
    }

    public String getNombre() {
        return nombre;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

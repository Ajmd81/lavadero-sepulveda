package com.lavaderosepulveda.app.model;

public enum EstadoFactura {
    PENDIENTE("Pendiente"),
    PAGADA("Pagada");

    private final String descripcion;

    EstadoFactura(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

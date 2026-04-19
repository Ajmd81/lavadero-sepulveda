package com.lavaderosepulveda.app.model.enums;

public enum EstadoDeclaracion {
    GENERADO("Generado"),
    PRESENTADO("Presentado");

    private final String descripcion;

    EstadoDeclaracion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

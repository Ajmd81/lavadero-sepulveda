package com.lavaderosepulveda.app.model;

public enum MetodoPago {
    EFECTIVO("Efectivo"),
    TARJETA("Tarjeta"),
    BIZUM("Bizum"),
    TRANSFERENCIA("Transferencia"),
    DOMICILIACION("Domiciliacion");

    private final String descripcion;

    MetodoPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

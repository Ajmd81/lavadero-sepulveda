package com.lavaderosepulveda.app.model;

public enum TipoFactura {
    SIMPLIFICADA("Factura Simplificada"),  // Ticket
    COMPLETA("Factura Completa");           // Con datos completos del cliente

    private final String descripcion;

    TipoFactura(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

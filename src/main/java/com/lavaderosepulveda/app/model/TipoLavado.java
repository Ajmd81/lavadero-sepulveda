package com.lavaderosepulveda.app.model;

public enum TipoLavado {
    LAVADO_COMPLETO_TURISMO("Lavado Completo Turismo", 23.0),
    LAVADO_INTERIOR_TURISMO("Lavado Interior Turismo", 16.0),
    LAVADO_EXTERIOR_TURISMO("Lavado Exterior Turismo", 12.0),
    LAVADO_COMPLETO_RANCHERA("Lavado Completo Turismo Ranchera", 26.0),
    LAVADO_INTERIOR_RANCHERA("Lavado Interior Turismo Ranchera", 18.0),
    LAVADO_EXTERIOR_RANCHERA("Lavado Exterior Turismo Ranchera", 13.0),
    LAVADO_COMPLETO_MONOVOLUMEN("Lavado Completo Monovolumen/Todoterreno Pequeño", 28.0),
    LAVADO_INTERIOR_MONOVOLUMEN("Lavado Interior Monovolumen/Todoterreno Pequeño", 19.0),
    LAVADO_EXTERIOR_MONOVOLUMEN("Lavado Exterior Monovolumen/Todoterreno Pequeño", 14.0),
    LAVADO_COMPLETO_TODOTERRENO("Lavado Completo Todoterreno Grande", 31.0),
    LAVADO_INTERIOR_TODOTERRENO("Lavado Interior Todoterreno Grande", 20.0),
    LAVADO_EXTERIOR_TODOTERRENO("Lavado Exterior Todoterreno Grande", 16.0),
    LAVADO_COMPLETO_FURGONETA_PEQUEÑA("Lavado Completo Furgoneta Pequeña", 30.0),
    LAVADO_INTERIOR_FURGONETA_PEQUEÑA("Lavado Interior Furgoneta Pequeña", 20.0),
    LAVADO_EXTERIOR_FURGONETA_PEQUEÑA("Lavado Exterior Furgoneta Pequeña", 15.0),
    LAVADO_COMPLETO_FURGONETA_GRANDE("Lavado Completo Furgoneta Grande", 35.0),
    LAVADO_INTERIOR_FURGONETA_GRANDE("Lavado Interior Furgoneta Grande", 25.0),
    LAVADO_EXTERIOR_FURGONETA_GRANDE("Lavado Exterior Furgoneta Grande", 20.0),
    TRATAMIENTO_OZONO("Tratamiento de Ozono", 15.0),
    ENCERADO("Encerado de Vehículo a Mano", 25.0),
    TAPICERIA_SIN_DESMONTAR("Limpieza de tapicería sin desmontar asientos", 100.0),
    TAPICERIA_DESMONTANDO("Limpieza de tapicería desmontando asientos", 150.0);

    private final String descripcion;
    private final double precio;

    TipoLavado(String descripcion, double precio) {
        this.descripcion = descripcion;
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecio() {
        return precio;
    }
}
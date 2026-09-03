package com.lavaderosepulveda.app.model.enums;

public enum TipoLavado {
    LAVADO_COMPLETO_TURISMO("Lavado Completo Turismo", "Lavado Completo", 25.0, 60),
    LAVADO_INTERIOR_TURISMO("Lavado Interior Turismo", "Lavado Interior", 17.0, 45),
    LAVADO_EXTERIOR_TURISMO("Lavado Exterior Turismo", "Lavado Exterior", 13.0, 30),
    LAVADO_COMPLETO_RANCHERA("Lavado Completo Turismo Ranchera", "Lavado Completo", 26.0, 60),
    LAVADO_INTERIOR_RANCHERA("Lavado Interior Turismo Ranchera", "Lavado Interior", 18.0, 45),
    LAVADO_EXTERIOR_RANCHERA("Lavado Exterior Turismo Ranchera", "Lavado Exterior", 13.0, 30),
    LAVADO_COMPLETO_MONOVOLUMEN("Lavado Completo Monovolumen/Todoterreno Pequeño", "Lavado Completo", 30.0, 75),
    LAVADO_INTERIOR_MONOVOLUMEN("Lavado Interior Monovolumen/Todoterreno Pequeño", "Lavado Interior", 20.0, 50),
    LAVADO_EXTERIOR_MONOVOLUMEN("Lavado Exterior Monovolumen/Todoterreno Pequeño", "Lavado Exterior", 15.0, 35),
    LAVADO_COMPLETO_TODOTERRENO("Lavado Completo Todoterreno Grande", "Lavado Completo", 35.0, 90),
    LAVADO_INTERIOR_TODOTERRENO("Lavado Interior Todoterreno Grande", "Lavado Interior", 22.0, 60),
    LAVADO_EXTERIOR_TODOTERRENO("Lavado Exterior Todoterreno Grande", "Lavado Exterior", 18.0, 40),
    LAVADO_COMPLETO_FURGONETA_PEQUEÑA("Lavado Completo Furgoneta Pequeña", "Lavado Completo", 30.0, 75),
    LAVADO_INTERIOR_FURGONETA_PEQUEÑA("Lavado Interior Furgoneta Pequeña", "Lavado Interior", 20.0, 50),
    LAVADO_EXTERIOR_FURGONETA_PEQUEÑA("Lavado Exterior Furgoneta Pequeña", "Lavado Exterior", 15.0, 35),
    LAVADO_COMPLETO_FURGONETA_GRANDE("Lavado Completo Furgoneta Grande", "Lavado Completo", 35.0, 90),
    LAVADO_INTERIOR_FURGONETA_GRANDE("Lavado Interior Furgoneta Grande", "Lavado Interior", 25.0, 60),
    LAVADO_EXTERIOR_FURGONETA_GRANDE("Lavado Exterior Furgoneta Grande", "Lavado Exterior", 20.0, 45),
    TRATAMIENTO_OZONO("Tratamiento de Ozono", "Tratamiento", 15.0, 30),
    ENCERADO("Encerado de Vehículo a Mano", "Encerado", 25.0, 60),
    TAPICERIA_SIN_DESMONTAR("Limpieza de tapicería sin desmontar asientos", "Tapicería", 100.0, 120),
    TAPICERIA_DESMONTANDO("Limpieza de tapicería desmontando asientos", "Tapicería", 150.0, 180);

    private final String descripcion;
    private final String label;  // ✅ NUEVO: Etiqueta corta para UI
    private final double precio;
    private final int duracion;  // ✅ NUEVO: Duración en minutos

    TipoLavado(String descripcion, String label, double precio, int duracion) {
        this.descripcion = descripcion;
        this.label = label;
        this.precio = precio;
        this.duracion = duracion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getLabel() {
        return label;
    }

    public double getPrecio() {
        return precio;
    }

    public int getDuracion() {
        return duracion;
    }

    /**
     * Devuelve el nombre del enum (ej: "LAVADO_COMPLETO_TURISMO").
     * Necesario para que Jackson lo incluya en la serialización JSON como campo "name",
     * ya que por defecto los enums con getters se serializan como objeto sin incluir name().
     */
    public String getName() {
        return this.name();
    }
}
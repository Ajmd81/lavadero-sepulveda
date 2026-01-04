package com.lavaderosepulveda.app.model;

public enum CategoriaGasto {
    AGUA("Agua"),
    LUZ("Electricidad"),
    GAS("Gas"),
    ALQUILER("Alquiler"),
    SEGUROS("Seguros"),
    SUMINISTROS("Suministros limpieza"),
    PRODUCTOS("Productos químicos"),
    MANTENIMIENTO("Mantenimiento"),
    REPARACIONES("Reparaciones"),
    COMBUSTIBLE("Combustible"),
    PERSONAL("Personal/Nóminas"),
    SEGURIDAD_SOCIAL("Seguridad Social"),
    IMPUESTOS("Impuestos/Tasas"),
    TELEFONIA("Telefonía/Internet"),
    PUBLICIDAD("Publicidad/Marketing"),
    MATERIAL_OFICINA("Material oficina"),
    GESTORÍA("Gestoría/Asesoría"),
    BANCARIOS("Gastos bancarios"),
    VEHICULOS("Vehículos"),
    MAQUINARIA("Maquinaria/Equipos"),
    OTROS("Otros");

    private final String descripcion;

    CategoriaGasto(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

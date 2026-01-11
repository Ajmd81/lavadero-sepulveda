package com.lavaderosepulveda.app.model;

public enum CategoriaGasto {
    AGUA("Agua"),
    ALQUILER("Alquiler"),
    ASOCIACIONES("Asociaciones"),
    BANCARIOS("Gastos bancarios"),
    COMBUSTIBLE("Combustible"),
    GESTORÍA("Gestoría/Asesoría"),
    IMPUESTOS("Impuestos/Tasas"),
    LUZ("Electricidad"),
    MANTENIMIENTO("Mantenimiento"),
    MAQUINARIA("Maquinaria/Equipos"),
    MATERIAL_OFICINA("Material oficina"),
    OTROS("Otros"),
    PERSONAL("Personal/Nóminas"),
    PRODUCTOS("Productos"),
    PUBLICIDAD("Publicidad/Marketing"),
    REPARACIONES("Reparaciones"),
    SEGURIDAD_SOCIAL("Seguridad Social"),
    SEGURIDAD_SOCIAL_A_CARGO_EMPRESA("Autónomos"),
    SEGUROS("Seguros"),
    SUMINISTROS("Productos de limpieza"),
    TELEFONIA("Telefonía/Internet"),
    VEHICULOS("Vehículos");

    private final String descripcion;

    CategoriaGasto(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

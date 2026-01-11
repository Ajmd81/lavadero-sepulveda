package com.lavaderosepulveda.app.model;

public enum CategoriaGasto {
    AGUA("Agua"),
    LUZ("Electricidad"),
    ALQUILER("Alquiler"),
    SEGUROS("Seguros"),
    SUMINISTROS("Productos de limpieza"),
    PRODUCTOS("Productos"),
    MANTENIMIENTO("Mantenimiento"),
    REPARACIONES("Reparaciones"),
    COMBUSTIBLE("Combustible"),
    PERSONAL("Personal/Nóminas"),
    SEGURIDAD_SOCIAL("Seguridad Social"),
    SEGURIDAD_SOCIAL_A_CARGO_EMPRESA("Autónomos"),
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

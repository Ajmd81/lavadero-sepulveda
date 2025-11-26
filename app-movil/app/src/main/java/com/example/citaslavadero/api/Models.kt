package com.example.citaslavadero.api

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para citas que coincide con la estructura de Spring Boot
 */
data class CitaRemota(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("fecha")
    val fecha: String,

    @SerializedName("hora")
    val hora: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("email")
    val email: String = "",

    @SerializedName("telefono")
    val telefono: String,

    @SerializedName("modeloVehiculo")
    val modeloVehiculo: String,

    @SerializedName("tipoLavado")
    val tipoLavado: String,

    @SerializedName("estado")
    val estado: String = "PENDIENTE"
)
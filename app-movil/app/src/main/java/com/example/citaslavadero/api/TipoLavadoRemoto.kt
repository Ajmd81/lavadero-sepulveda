package com.example.citaslavadero.api

import com.google.gson.annotations.SerializedName

data class TipoLavadoRemoto(
    @SerializedName("id")
    val  id: String,

    @SerializedName("descripcion")
    val titulo: String,

    @SerializedName("precio")
    val precio: Double
)
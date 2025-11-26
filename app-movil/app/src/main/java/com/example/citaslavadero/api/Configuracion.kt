package com.example.citaslavadero.api

import com.google.gson.annotations.SerializedName

data class Configuracion(
    @SerializedName("horaAperturaMañana")
    val hora_apertura_mañana: Int = 9,

    @SerializedName("horaCierreMañana")
    val hora_cierre_mañana: Int = 14,

    @SerializedName("horaAperturaTarde")
    val hora_apertura_tarde: Int = 17,

    @SerializedName("horaCierreTarde")
    val hora_cierre_tarde: Int = 20,

    @SerializedName("intervaloMinutos")
    val intervalo_minutos: Int = 60,

    @SerializedName("diasTrabajo")
    val dias_trabajo: List<Int> = listOf(1, 2, 3, 4, 5, 6) // 1=Lunes a 6=Sábado
)
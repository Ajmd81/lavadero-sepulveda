package com.example.citaslavadero.api

import com.example.citaslavadero.database.Cita

/**
 * Funciones de extensión para convertir entre modelos locales y remotos
 */

fun CitaRemota.toCita(): Cita {
    return Cita(
        id = this.id?.toInt() ?: 0,
        fecha = this.fecha,
        hora = this.hora,
        nombreCliente = this.nombre,
        email = this.email ?: "", // Convertir null a string vacía
        modeloCoche = this.modeloVehiculo,
        telefono = this.telefono,
        tipoLavado = this.tipoLavado,

    )
}

fun Cita.toCitaRemota(): CitaRemota {
    return CitaRemota(
        id = if(this.id > 0) this.id.toLong() else null,
        fecha = this.fecha,
        hora = this.hora,
        nombre = this.nombreCliente,
        email = this.email,
        telefono = this.telefono,
        modeloVehiculo = this.modeloCoche,
        tipoLavado = this.tipoLavado,

    )
}

fun List<CitaRemota>.toCitaList(): List<Cita> {
    return this.map { it.toCita() }
}
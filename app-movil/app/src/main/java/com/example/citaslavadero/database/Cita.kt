package com.example.citaslavadero.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "citas",
    indices = [
        Index(value = ["fecha", "hora"], unique = true)
    ]
)
data class Cita (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fecha: String,
    val hora: String,
    val nombreCliente: String,
    val email: String = "", // Nuevo campo con valor por defecto vacío
    val modeloCoche: String,
    val telefono: String,
    val tipoLavado: String,
    val estado: String = "PENDIENTE"
)
package com.example.citaslavadero.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CitaDao {
    @Query("SELECT * FROM citas ORDER BY fecha DESC, hora DESC")
    fun obtenerTodasLasCitas(): Flow<List<Cita>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCita(cita: Cita)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCitas(citas: List<Cita>)

    @Delete
    suspend fun eliminarCita(cita: Cita)

    @Query("DELETE FROM citas")
    suspend fun eliminarTodasLasCitas()

    @Query("SELECT EXISTS(SELECT 1 FROM citas WHERE fecha = :fecha AND hora = :hora)")
    suspend fun existeCitaEnFechaHora(fecha: String, hora: String): Boolean
}
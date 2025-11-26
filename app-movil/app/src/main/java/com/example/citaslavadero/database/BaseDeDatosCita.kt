package com.example.citaslavadero.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Cita::class], version = 1)
abstract class BaseDeDatosCitas : RoomDatabase() {
    abstract fun citaDao(): CitaDao

    companion object {
        @Volatile
        private var INSTANCIA: BaseDeDatosCitas? = null

        fun obtenerBaseDeDatos(contexto: Context): BaseDeDatosCitas {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    contexto.applicationContext,
                    BaseDeDatosCitas::class.java,
                    "base_datos_citas"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}
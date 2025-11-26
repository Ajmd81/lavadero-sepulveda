package com.example.citaslavadero.repository

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.example.citaslavadero.api.RetrofitClient
import com.example.citaslavadero.api.toCita
import com.example.citaslavadero.api.toCitaList
import com.example.citaslavadero.api.toCitaRemota
import com.example.citaslavadero.database.BaseDeDatosCitas
import com.example.citaslavadero.database.Cita
import com.example.citaslavadero.database.TipoLavado
import com.example.citaslavadero.util.HorariosUtil
import com.example.citaslavadero.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Repositorio que maneja la sincronización entre la base de datos local (Room)
 * y el servidor de Spring Boot
 * MODIFICADO: Solo muestra citas creadas desde la aplicación Android
 */
class CitaRepository(private val context: Context) {

    private val database = BaseDeDatosCitas.obtenerBaseDeDatos(context)
    private val citaDao = database.citaDao()
    private val apiService = RetrofitClient.apiService

    // Configuración para manejar el modo offline
    private var modoOffline = false
    private val TAG = "CitaRepository"

    init {
        Log.d(TAG, "Inicializando CitaRepository con API: ${RetrofitClient.getServerUrl()}")
    }

    /**
     * Obtiene todas las citas desde la base de datos local
     */
    fun obtenerTodasLasCitas(): Flow<List<Cita>> {
        Log.d(TAG, "Obteniendo todas las citas desde la base de datos local (solo citas de Android)")
        return citaDao.obtenerTodasLasCitas()
    }

    /**
     * MODIFICADO: No sincronizar citas desde el servidor
     * Solo mostrar citas creadas desde esta aplicación Android
     */
    suspend fun sincronizarCitas() {
        Log.d(TAG, "MODIFICADO: Saltando sincronización desde servidor")
        Log.d(TAG, "Solo se mostrarán citas creadas desde esta aplicación Android")

        // COMENTADO: No descargar citas del servidor
        // Esto permite que solo se vean las citas creadas desde Android

        /*
        // CÓDIGO ORIGINAL COMENTADO:
        if (!modoOffline && NetworkUtils.isNetworkAvailable(context)) {
            try {
                Log.d(TAG, "Obteniendo citas del servidor: ${RetrofitClient.getServerUrl()}api/citas")
                val response = apiService.obtenerCitas()
                // ... resto del código de sincronización comentado
            } catch (e: Exception) {
                Log.e(TAG, "Error sincronizando citas", e)
                manejarExcepcionRed(e)
            }
        } else {
            Log.d(TAG, "No hay conexión, o en modo offline. No se pueden sincronizar citas.")
        }
        */
    }

    /**
     * Agrega una nueva cita, primero intentando en el servidor remoto
     * y luego en la base de datos local
     * MANTENIDO: Las citas se siguen enviando al servidor
     */
    suspend fun agregarCita(cita: Cita): Result<Cita> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Intentando agregar cita: $cita")
        Log.d(TAG, "Estado de red: ${if (NetworkUtils.isNetworkAvailable(context)) "Disponible" else "No disponible"}")
        Log.d(TAG, "Modo offline: $modoOffline")

        if (!modoOffline && NetworkUtils.isNetworkAvailable(context)) {
            try {
                // Convertir Cita a formato para API usando la función de extensión
                val citaRemota = cita.toCitaRemota()

                // Imprimir JSON exacto que se envía
                val gson = GsonBuilder().setPrettyPrinting().create()
                val jsonCita = gson.toJson(citaRemota)
                Log.d(TAG, "JSON enviado al servidor: $jsonCita")
                Log.d(TAG, "Tipo de lavado enviado: ${citaRemota.tipoLavado} (debe coincidir con un valor del enum en el servidor)")

                val response = apiService.crearCita(citaRemota)
                Log.d(TAG, "Respuesta del servidor: Código ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "Cita creada exitosamente en el servidor: ${response.body()}")
                    // MODIFICADO: Guardar la cita local con la información original de Android
                    // No usar la respuesta del servidor para evitar conflictos de ID
                    Log.d(TAG, "Guardando cita en base de datos local (versión Android): $cita")
                    citaDao.insertarCita(cita)
                    return@withContext Result.success(cita)
                } else {
                    // Si hay un conflicto (409), probablemente la cita ya existe
                    if (response.code() == 409) {
                        Log.e(TAG, "Conflicto: Ya existe una cita en esa fecha y hora")
                        return@withContext Result.failure(
                            Exception("Ya existe una cita programada en ese horario")
                        )
                    }

                    Log.e(TAG, "Error al crear cita en servidor. Código: ${response.code()}")
                    response.errorBody()?.string()?.let { error ->
                        Log.e(TAG, "Detalle del error: $error")
                    }
                    // Si falla el servidor pero no es por conflicto, intentar guardar localmente
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al comunicarse con el servidor", e)
                manejarExcepcionRed(e)
                // En caso de error de red, intentar guardar localmente
                modoOffline = true
            }
        }

        // Modo offline: guardar solo localmente
        try {
            Log.d(TAG, "Operando en modo offline o falló la conexión al servidor")
            // Verificar si ya existe una cita en esa fecha y hora
            val existeCita = citaDao.existeCitaEnFechaHora(cita.fecha, cita.hora)
            if (existeCita) {
                Log.e(TAG, "Ya existe una cita local en esa fecha y hora")
                return@withContext Result.failure(
                    Exception("Ya existe una cita programada en ese horario")
                )
            }

            Log.d(TAG, "Guardando cita solo localmente: $cita")
            citaDao.insertarCita(cita)
            return@withContext Result.success(cita)
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando cita localmente", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Elimina una cita, primero en el servidor remoto y luego en la base de datos local
     */
    suspend fun eliminarCita(cita: Cita): Result<Boolean> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Intentando eliminar cita: $cita")

        if (!modoOffline && cita.id > 0 && NetworkUtils.isNetworkAvailable(context)) {
            try {
                Log.d(TAG, "Enviando solicitud para eliminar cita del servidor. ID: ${cita.id}")
                val response = apiService.eliminarCita(cita.id.toLong())

                if (response.isSuccessful) {
                    Log.d(TAG, "Cita eliminada exitosamente del servidor")
                    citaDao.eliminarCita(cita)
                    return@withContext Result.success(true)
                } else {
                    Log.e(TAG, "Error eliminando cita del servidor: ${response.code()}")
                    response.errorBody()?.string()?.let { error ->
                        Log.e(TAG, "Detalle del error: $error")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar cita del servidor", e)
                manejarExcepcionRed(e)
                // En caso de error de red, eliminar localmente de todos modos
                modoOffline = true
            }
        }

        // Modo offline: eliminar solo localmente
        try {
            Log.d(TAG, "Eliminando cita localmente: $cita")
            citaDao.eliminarCita(cita)
            return@withContext Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando cita localmente", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * MODIFICADO: Verificación local únicamente
     * Solo verifica citas locales (creadas desde Android)
     */
    suspend fun existeCitaEnFechaHora(fecha: String, hora: String): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Verificando si existe cita LOCAL en fecha: $fecha, hora: $hora")

        // MODIFICADO: Solo verificación local, no consultar servidor
        // Esto asegura que solo se valide contra citas creadas desde Android
        val existeLocal = citaDao.existeCitaEnFechaHora(fecha, hora)
        Log.d(TAG, "Verificación local (solo Android): ${if (existeLocal) "Horario ocupado" else "Horario disponible"}")
        return@withContext existeLocal

        /*
        // CÓDIGO ORIGINAL COMENTADO:
        if (!modoOffline && NetworkUtils.isNetworkAvailable(context)) {
            try {
                Log.d(TAG, "Consultando disponibilidad al servidor")
                val response = apiService.verificarDisponibilidad(fecha, hora)
                // ... resto comentado
            } catch (e: Exception) {
                // ... manejo comentado
            }
        }
        */
    }

    /**
     * Obtiene horarios disponibles para una fecha
     * MANTENIDO: Sigue consultando al servidor para horarios disponibles
     */
    suspend fun obtenerHorariosDisponibles(fecha: String): Response<List<String>> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Solicitando horarios disponibles para fecha: $fecha")

        if (!modoOffline && NetworkUtils.isNetworkAvailable(context)) {
            try {
                Log.d(TAG, "Consultando horarios disponibles al servidor: ${RetrofitClient.getServerUrl()}api/citas/horarios-disponibles?fecha=$fecha")
                val response = apiService.obtenerHorariosDisponibles(fecha)

                Log.d(TAG, "Código de respuesta: ${response.code()}")

                if (response.isSuccessful) {
                    Log.d(TAG, "Horarios disponibles recibidos: ${response.body()}")
                    return@withContext response
                } else {
                    Log.e(TAG, "Error al obtener horarios disponibles. Código: ${response.code()}")
                    response.errorBody()?.string()?.let { error ->
                        Log.e(TAG, "Detalle del error: $error")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo horarios disponibles", e)
                manejarExcepcionRed(e)
            }
        }

        // Modo offline o error - devolver horarios por defecto
        Log.d(TAG, "Usando horarios por defecto")
        val horariosDefault = HorariosUtil.generarHorariosDisponibles()
        return@withContext Response.success(horariosDefault)
    }

    /**
     * Obtiene la configuración del horario
     */
    suspend fun obtenerConfiguracion(): Triple<Int, Int, Int> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Solicitando configuración de horarios")

        if (!modoOffline && NetworkUtils.isNetworkAvailable(context)) {
            try {
                Log.d(TAG, "Consultando configuración al servidor")
                val response = apiService.obtenerConfiguracion()

                if (response.isSuccessful && response.body() != null) {
                    val config = response.body()!!
                    Log.d(TAG, "Configuración recibida del servidor: $config")
                    return@withContext Triple(
                        config.hora_apertura_mañana ?: HorariosUtil.HORA_APERTURA_MAÑANA,
                        config.hora_cierre_tarde ?: HorariosUtil.HORA_CIERRE_TARDE,
                        config.intervalo_minutos ?: HorariosUtil.INTERVALO_MINUTOS
                    )
                } else {
                    Log.e(TAG, "Error al obtener configuración. Código: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener configuración del servidor", e)
                manejarExcepcionRed(e)
            }
        }

        // Valores por defecto de la aplicación
        Log.d(TAG, "Usando configuración por defecto")
        return@withContext Triple(
            HorariosUtil.HORA_APERTURA_MAÑANA,
            HorariosUtil.HORA_CIERRE_TARDE,
            HorariosUtil.INTERVALO_MINUTOS
        )
    }

    /**
     * Obtiene los tipos de lavado
     */
    suspend fun obtenerTiposLavado(): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Solicitando tipos de lavado")

        if (!modoOffline && NetworkUtils.isNetworkAvailable(context)) {
            try {
                Log.d(TAG, "Consultando tipos de lavado al servidor")
                val response = apiService.obtenerTiposLavado()

                if (response.isSuccessful && response.body() != null) {
                    val tipos = response.body()!!.map { Pair(it.id, it.titulo) }
                    Log.d(TAG, "Tipos de lavado recibidos del servidor: $tipos")
                    return@withContext tipos
                } else {
                    Log.e(TAG, "Error al obtener tipos de lavado. Código: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener tipos de lavado del servidor", e)
                manejarExcepcionRed(e)
            }
        }

        // Valores por defecto de la aplicación - Usando el nuevo enum
        Log.d(TAG, "Usando tipos de lavado por defecto")
        return@withContext TipoLavado.values().map {
            Pair(it.name, it.titulo)
        }
    }

    /**
     * MODIFICADO: Intenta conectar con el servidor pero NO sincroniza citas
     */
    suspend fun intentarSincronizacion(): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Intentando conexión con el servidor (sin sincronizar citas)")

        if (NetworkUtils.isNetworkAvailable(context)) {
            try {
                // Verificar conexión con una solicitud pequeña
                Log.d(TAG, "Verificando conexión con el servidor")
                val response = apiService.obtenerConfiguracion()

                if (response.isSuccessful) {
                    Log.d(TAG, "Conexión exitosa con el servidor")
                    modoOffline = false

                    // MODIFICADO: NO sincronizar citas
                    Log.d(TAG, "Conexión establecida pero NO se sincronizan citas (solo mostrar citas de Android)")

                    return@withContext true
                } else {
                    Log.e(TAG, "Error al verificar conexión. Código: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al intentar conectar con el servidor", e)
            }
        } else {
            Log.d(TAG, "No hay conexión de red disponible")
        }

        Log.d(TAG, "Estado de modo offline: $modoOffline")
        return@withContext !modoOffline
    }

    /**
     * Maneja las excepciones relacionadas con la red
     */
    private fun manejarExcepcionRed(e: Exception) {
        when (e) {
            is IOException, is SocketTimeoutException -> {
                Log.e(TAG, "Error de red, pasando a modo offline", e)
                modoOffline = true
            }
            is HttpException -> {
                Log.e(TAG, "Error HTTP: ${e.code()}", e)
            }
            else -> {
                Log.e(TAG, "Error desconocido", e)
            }
        }
    }
}
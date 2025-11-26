package com.example.citaslavadero.api

import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz que define los endpoints para la API REST de Spring Boot
 */
interface LavaderoApiService {
    /**
     * Obtiene todas las citas
     */
    @GET("api/citas")
    suspend fun obtenerCitas(): Response<List<CitaRemota>>

    /**
     * Obtiene una cita por su ID
     */
    @GET("api/citas/{id}")
    suspend fun obtenerCitaPorId(@Path("id") id: Long): Response<CitaRemota>


    /**
     * Crea una nueva cita
     */
    @POST("api/citas")
    suspend fun crearCita(@Body cita: CitaRemota): Response<CitaRemota>

    /**
     * Actualiza una cita existente
     */
    @PUT("api/citas/{id}")
    suspend fun actualizarCita(
        @Path("id") id: Long,
        @Body cita: CitaRemota
    ): Response<CitaRemota>

    /**
     * Elimina una cita
     */
    @DELETE("api/citas/{id}")
    suspend fun eliminarCita(@Path("id") id: Long): Response<Void>

    /**
     * Verifica si existe una cita en una fecha y hora específicas
    */
    @GET("api/citas/verificar-disponibilidad")
    suspend fun verificarDisponibilidad(
        @Query("fecha") fecha: String,
        @Query("hora") hora: String
    ): Response<Boolean>

    /**
     * Obtiene horarios disponibles para una fecha
    */
    @GET("api/citas/horarios-disponibles")
    suspend fun obtenerHorariosDisponibles(
        @Query("fecha") fecha: String
    ): Response<List<String>>


    /**
     * Obtiene la configuración del sistema (horarios de apertura, cierre, etc.)
     */
    @GET("api/configuracion")
    suspend fun obtenerConfiguracion(): Response<Configuracion>

    /**
     * Obtiene la lista de tipos de lavado disponibles
     */
    @GET("api/tipos-lavado")
    suspend fun obtenerTiposLavado(): Response<List<TipoLavadoRemoto>>
}
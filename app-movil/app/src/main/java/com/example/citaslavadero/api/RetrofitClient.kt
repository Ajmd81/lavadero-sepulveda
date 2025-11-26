package com.example.citaslavadero.api

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para acceder a la API del servidor Spring Boot
 * Soporta múltiples entornos: Ngrok, Producción, Local, etc.
 */
object RetrofitClient {

    // URLs para diferentes entornos
    private const val NGROK_URL = "https://c00e474ee81d.ngrok-free.app"
    private const val PRODUCTION_URL = "http://lavaderosepulveda.servehttp.com:8080/"


    // URL actual (por defecto ngrok para desarrollo)
    private var serverUrl = NGROK_URL

    private const val TIMEOUT = 30L
    private const val TAG = "RetrofitClient"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            try {
                chain.proceed(chain.request())
            } catch (e: Exception) {
                Log.e(TAG, "Error de conexión: ${e.message}")
                throw e
            }
        }
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private var retrofit: Retrofit? = null

    // Método para obtener la URL actual del servidor
    fun getServerUrl(): String {
        return serverUrl
    }

    // Método genérico para actualizar la URL del servidor
    fun updateServerUrl(url: String) {
        Log.d(TAG, "Actualizando URL del servidor de '$serverUrl' a '$url'")
        serverUrl = if (url.endsWith("/")) url else "$url/"
        retrofit = null
    }

    // Métodos específicos para cada entorno
    fun useNgrok(ngrokUrl: String? = null) {
        val url = ngrokUrl ?: NGROK_URL
        Log.d(TAG, "Cambiando a entorno Ngrok")
        updateServerUrl(url)
    }

    fun useProduction() {
        Log.d(TAG, "Cambiando a entorno Producción")
        updateServerUrl(PRODUCTION_URL)
    }

    // Método para cambiar entorno usando enum
    fun setEnvironment(env: Environment, customUrl: String? = null) {
        when(env) {
            Environment.NGROK -> useNgrok(customUrl)
            Environment.PRODUCTION -> useProduction()
            Environment.CUSTOM -> customUrl?.let { updateServerUrl(it) }
        }
    }

    // Método para verificar conexión
    fun isUsingHttps(): Boolean {
        return serverUrl.startsWith("https://")
    }

    // Método para obtener el dominio actual
    fun getCurrentDomain(): String {
        return serverUrl.replace("https://", "").replace("http://", "").split("/")[0]
    }

    val apiService: LavaderoApiService
        get() {
            if (retrofit == null) {
                Log.d(TAG, "Creando nueva instancia de Retrofit con URL: $serverUrl")
                retrofit = Retrofit.Builder()
                    .baseUrl(serverUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            }
            return retrofit!!.create(LavaderoApiService::class.java)
        }
}

/**
 * Enum para los diferentes entornos disponibles
 */
enum class Environment {
    NGROK,      // Para desarrollo con ngrok
    PRODUCTION, // Servidor de producción
    CUSTOM      // URL personalizada
}
package com.example.citaslavadero.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.citaslavadero.api.Environment
import com.example.citaslavadero.api.RetrofitClient

/**
 * Gestor de entornos para facilitar el cambio entre diferentes configuraciones
 * de servidor (ngrok, producción, local, etc.)
 */
object EnvironmentManager {
    private const val PREFS_NAME = "lavadero_environment"
    private const val KEY_CURRENT_ENV = "current_environment"
    private const val KEY_CUSTOM_URL = "custom_url"
    private const val KEY_LAST_NGROK_URL = "last_ngrok_url"
    private const val TAG = "EnvironmentManager"

    /**
     * Inicializa el entorno basado en las preferencias guardadas
     */
    fun initializeEnvironment(context: Context) {
        val prefs = getPreferences(context)
        val savedEnv = prefs.getString(KEY_CURRENT_ENV, Environment.NGROK.name)
        val customUrl = prefs.getString(KEY_CUSTOM_URL, null)

        try {
            val environment = Environment.valueOf(savedEnv ?: Environment.NGROK.name)
            RetrofitClient.setEnvironment(environment, customUrl)
            Log.d(TAG, "Entorno inicializado: $environment, URL: ${RetrofitClient.getServerUrl()}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar entorno, usando NGROK por defecto", e)
            RetrofitClient.setEnvironment(Environment.NGROK)
        }
    }

    /**
     * Cambia al entorno de ngrok y guarda la URL
     */
    fun setNgrokEnvironment(context: Context, ngrokUrl: String) {
        RetrofitClient.useNgrok(ngrokUrl)
        saveEnvironment(context, Environment.NGROK, ngrokUrl)
        saveLastNgrokUrl(context, ngrokUrl)
        Log.d(TAG, "Cambiado a entorno ngrok: $ngrokUrl")
    }

    /**
     * Cambia al entorno de producción
     */
    fun setProductionEnvironment(context: Context) {
        RetrofitClient.useProduction()
        saveEnvironment(context, Environment.PRODUCTION, null)
        Log.d(TAG, "Cambiado a entorno de producción")
    }

    /**
     * Cambia a una URL personalizada
     */
    fun setCustomEnvironment(context: Context, customUrl: String) {
        RetrofitClient.updateServerUrl(customUrl)
        saveEnvironment(context, Environment.CUSTOM, customUrl)
        Log.d(TAG, "Cambiado a entorno personalizado: $customUrl")
    }

    /**
     * Obtiene la última URL de ngrok utilizada
     */
    fun getLastNgrokUrl(context: Context): String? {
        return getPreferences(context).getString(KEY_LAST_NGROK_URL, null)
    }

    /**
     * Obtiene el entorno actual
     */
    fun getCurrentEnvironment(context: Context): Environment {
        val prefs = getPreferences(context)
        val savedEnv = prefs.getString(KEY_CURRENT_ENV, Environment.NGROK.name)
        return try {
            Environment.valueOf(savedEnv ?: Environment.NGROK.name)
        } catch (e: Exception) {
            Environment.NGROK
        }
    }

    /**
     * Obtiene información sobre el entorno actual
     */
    fun getCurrentEnvironmentInfo(context: Context): EnvironmentInfo {
        val currentEnv = getCurrentEnvironment(context)
        val currentUrl = RetrofitClient.getServerUrl()
        val isHttps = RetrofitClient.isUsingHttps()
        val domain = RetrofitClient.getCurrentDomain()

        return EnvironmentInfo(
            environment = currentEnv,
            url = currentUrl,
            isHttps = isHttps,
            domain = domain
        )
    }

    private fun saveEnvironment(context: Context, environment: Environment, customUrl: String?) {
        val prefs = getPreferences(context)
        prefs.edit()
            .putString(KEY_CURRENT_ENV, environment.name)
            .putString(KEY_CUSTOM_URL, customUrl)
            .apply()
    }

    private fun saveLastNgrokUrl(context: Context, ngrokUrl: String) {
        getPreferences(context)
            .edit()
            .putString(KEY_LAST_NGROK_URL, ngrokUrl)
            .apply()
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Verifica si la URL actual es válida (formato básico)
     */
    fun isValidUrl(url: String): Boolean {
        return url.matches(Regex("^https?://[a-zA-Z0-9.-]+(/.*)?$"))
    }
}

/**
 * Información sobre el entorno actual
 */
data class EnvironmentInfo(
    val environment: Environment,
    val url: String,
    val isHttps: Boolean,
    val domain: String
)
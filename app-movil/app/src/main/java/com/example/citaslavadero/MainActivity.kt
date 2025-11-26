package com.example.citaslavadero

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.citaslavadero.api.Environment
import com.example.citaslavadero.api.RetrofitClient
import com.example.citaslavadero.databinding.ActivityMainBinding
import com.example.citaslavadero.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CitaViewModel by viewModels()
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el entorno al iniciar la aplicación
        configurarEntornoInicial()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FragmentoNuevaCita())
                .commit()
        }

        configurarNavegacion()
        verificarConexion()
    }

    private fun configurarEntornoInicial() {
        val ngrokUrl = "https://c00e474ee81d.ngrok-free.app"

        // Cambiar entre entornos según necesites:

        // Para usar ngrok (desarrollo):
        RetrofitClient.useNgrok(ngrokUrl)

        // Para usar producción:
        // RetrofitClient.useProduction()

        // Usando el enum (alternativa):
        // RetrofitClient.setEnvironment(Environment.NGROK, ngrokUrl)

        Log.d(TAG, "Configurado para usar: ${RetrofitClient.getServerUrl()}")
        Log.d(TAG, "Usando HTTPS: ${RetrofitClient.isUsingHttps()}")
        Log.d(TAG, "Dominio actual: ${RetrofitClient.getCurrentDomain()}")
    }

    private fun configurarNavegacion() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.fragmentoNuevaCita -> FragmentoNuevaCita()
                R.id.fragmentoListaCitas -> FragmentoListaCitas()
                else -> return@setOnItemSelectedListener false
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()

            true
        }
    }

    private fun verificarConexion() {
        // Verificar disponibilidad de red
        val hayRed = NetworkUtils.isNetworkAvailable(this)
        Log.d(TAG, "Estado de red: ${if (hayRed) "Disponible" else "No disponible"}")

        // Probar conexión con el servidor configurado
        lifecycleScope.launch {
            try {
                val baseUrl = RetrofitClient.getServerUrl()
                // Usar un endpoint que SÍ existe en tu controlador
                val testUrl = "${baseUrl}api/citas"
                Log.d(TAG, "Probando conexión a: $testUrl")

                val resultado = withContext(Dispatchers.IO) {
                    val connection = URL(testUrl).openConnection() as HttpURLConnection
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000
                    connection.requestMethod = "GET"

                    try {
                        val responseCode = connection.responseCode
                        Log.d(TAG, "Código de respuesta: $responseCode")

                        when (responseCode) {
                            HttpURLConnection.HTTP_OK -> {
                                val response = connection.inputStream.bufferedReader().use { it.readText() }
                                "✅ Conexión exitosa ($responseCode)"
                            }
                            HttpURLConnection.HTTP_NOT_FOUND -> {
                                "⚠️ Servidor encontrado pero endpoint no disponible ($responseCode)"
                            }
                            else -> {
                                "❌ Error del servidor ($responseCode): ${connection.responseMessage}"
                            }
                        }
                    } finally {
                        connection.disconnect()
                    }
                }

                Log.d(TAG, "Resultado: $resultado")
                Toast.makeText(this@MainActivity, resultado, Toast.LENGTH_LONG).show()

                // Si la conexión falló, sugerir cambiar de entorno
                if (resultado.contains("❌")) {
                    Log.w(TAG, "Conexión fallida, considera cambiar el entorno de RetrofitClient")
                    Toast.makeText(
                        this@MainActivity,
                        "Tip: Verifica que ngrok esté ejecutándose y la URL sea correcta",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error en prueba de conexión", e)
                val mensajeError = when {
                    e.message?.contains("UnknownHostException") == true ->
                        "❌ No se puede resolver el dominio. Verifica la URL de ngrok."
                    e.message?.contains("ConnectException") == true ->
                        "❌ No se puede conectar al servidor. ¿Está ngrok ejecutándose?"
                    e.message?.contains("SocketTimeoutException") == true ->
                        "❌ Timeout de conexión. Verifica tu conexión a internet."
                    else -> "❌ Error de conexión: ${e.message}"
                }

                Toast.makeText(this@MainActivity, mensajeError, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Método público para cambiar el entorno desde otros lugares si es necesario
     */
    fun cambiarEntorno(entorno: Environment, urlPersonalizada: String? = null) {
        RetrofitClient.setEnvironment(entorno, urlPersonalizada)
        Log.d(TAG, "Entorno cambiado a: ${RetrofitClient.getServerUrl()}")

        // Verificar nueva conexión
        verificarConexion()

        // Opcional: recargar datos con el nuevo entorno
        viewModel.sincronizar()
    }
}
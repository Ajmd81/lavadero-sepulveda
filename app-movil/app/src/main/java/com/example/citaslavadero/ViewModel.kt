package com.example.citaslavadero

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.citaslavadero.database.Cita
import com.example.citaslavadero.repository.CitaRepository
import kotlinx.coroutines.launch

class CitaViewModel : ViewModel() {
    private lateinit var repository: CitaRepository
    private val TAG = "CitaViewModel"

    // Estados para la UI
    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _cargando = MutableLiveData<Boolean>(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _sincronizando = MutableLiveData<Boolean>(false)
    val sincronizando: LiveData<Boolean> = _sincronizando

    private val _tiposLavado = MutableLiveData<List<Pair<String, String>>>()
    val tiposLavado: LiveData<List<Pair<String, String>>> = _tiposLavado

    private val _configuracion = MutableLiveData<Triple<Int, Int, Int>>()
    val configuracion: LiveData<Triple<Int, Int, Int>> = _configuracion

    // Propiedad para las citas
    private var _todasLasCitas: LiveData<List<Cita>>? = null
    val todasLasCitas: LiveData<List<Cita>>
        get() {
            if (_todasLasCitas == null && ::repository.isInitialized) {
                _todasLasCitas = repository.obtenerTodasLasCitas().asLiveData()
            }
            return _todasLasCitas ?: MutableLiveData(emptyList())
        }

    fun inicializar(context: Context) {
        if (!::repository.isInitialized) {
            repository = CitaRepository(context)
            cargarConfiguracion()
            cargarTiposLavado()
            _todasLasCitas = repository.obtenerTodasLasCitas().asLiveData()
        }
    }

    /**
     * Carga las citas desde el servidor y las almacena en la base de datos local
     */
    fun cargarCitas() {
        Log.d(TAG, "Iniciando carga de citas")
        _cargando.value = true
        viewModelScope.launch {
            try {
                repository.sincronizarCitas()
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar citas", e)
                _errorMessage.postValue("Error al cargar citas: ${e.message}")
            } finally {
                _cargando.postValue(false)
            }
        }
    }

    // Método para agregar una cita
    fun agregarCita(cita: Cita) {
        _cargando.value = true
        viewModelScope.launch {
            try {
                val resultado = repository.agregarCita(cita)
                if (resultado.isFailure) {
                    _errorMessage.postValue(resultado.exceptionOrNull()?.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al guardar cita", e)
                _errorMessage.postValue("Error al guardar la cita: ${e.message}")
            } finally {
                _cargando.postValue(false)
            }
        }
    }

    suspend fun obtenerHorariosDisponibles(fecha: String): List<String> {
        return try {
            val response = repository.obtenerHorariosDisponibles(fecha)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("CitaViewModel", "Error obteniendo horarios", e)
            emptyList()
        }
    }

    // Método para eliminar una cita
    fun eliminarCita(cita: Cita) {
        _cargando.value = true
        viewModelScope.launch {
            try {
                val resultado = repository.eliminarCita(cita)
                if (resultado.isFailure) {
                    _errorMessage.postValue(resultado.exceptionOrNull()?.message ?: "Error al eliminar la cita")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar cita", e)
                _errorMessage.postValue("Error al eliminar la cita: ${e.message}")
            } finally {
                _cargando.postValue(false)
            }
        }
    }

    // Método para verificar si existe una cita en una fecha y hora específica
    suspend fun existeCitaEnFechaHora(fecha: String, hora: String): Boolean {
        return try {
            repository.existeCitaEnFechaHora(fecha, hora)
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar disponibilidad", e)
            _errorMessage.postValue("Error al verificar disponibilidad: ${e.message}")
            false
        }
    }

    // Método para sincronizar los datos con el servidor
    fun sincronizar() {
        _sincronizando.value = true
        viewModelScope.launch {
            try {
                val estaOnline = repository.intentarSincronizacion()
                if (!estaOnline) {
                    _errorMessage.postValue("No se pudo conectar con el servidor. Trabajando en modo offline.")
                }
                // Recargar datos independientemente del resultado
                cargarConfiguracion()
                cargarTiposLavado()
            } catch (e: Exception) {
                Log.e(TAG, "Error durante la sincronización", e)
                _errorMessage.postValue("Error durante la sincronización: ${e.message}")
            } finally {
                _sincronizando.postValue(false)
            }
        }
    }

    // Método para cargar la configuración
    private fun cargarConfiguracion() {
        viewModelScope.launch {
            try {
                val config = repository.obtenerConfiguracion()
                _configuracion.postValue(config)
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar configuración", e)
                _errorMessage.postValue("Error al cargar la configuración: ${e.message}")
            }
        }
    }

    // Método para cargar los tipos de lavado
    private fun cargarTiposLavado() {
        viewModelScope.launch {
            try {
                val tipos = repository.obtenerTiposLavado()
                _tiposLavado.postValue(tipos)
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar tipos de lavado", e)
                _errorMessage.postValue("Error al cargar los tipos de lavado: ${e.message}")
            }
        }
    }

    // Método para limpiar mensajes de error
    fun limpiarError() {
        _errorMessage.value = null
    }
}
package com.example.citaslavadero

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.citaslavadero.database.Cita
import com.example.citaslavadero.databinding.FragmentNuevaCitaBinding
import com.example.citaslavadero.util.HorariosUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

class FragmentoNuevaCita : Fragment() {

    private var _binding: FragmentNuevaCitaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CitaViewModel by viewModels({ requireActivity() })
    private var calendario = Calendar.getInstance()
    private var tipoLavadoSeleccionado: String = ""
    private val TAG = "FragmentoNuevaCita"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNuevaCitaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.inicializar(requireContext())
        configurarObservadores()
        configurarBotones()
        configurarSwipeRefresh()
        // Inicializamos fecha y hora después de configurar observadores para
        // asegurarnos de que la configuración se haya cargado
    }

    private fun configurarObservadores() {
        // Observar errores
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { mensaje ->
            mensaje?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.limpiarError()
            }
        })

        // Observar estado de carga
        viewModel.cargando.observe(viewLifecycleOwner, Observer { cargando ->
            binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
            binding.botonGuardar.isEnabled = !cargando
        })

        // Observar estado de sincronización
        viewModel.sincronizando.observe(viewLifecycleOwner, Observer { sincronizando ->
            binding.swipeRefresh.isRefreshing = sincronizando
        })

        // Observar tipos de lavado
        viewModel.tiposLavado.observe(viewLifecycleOwner, Observer { tipos ->
            // Pasamos la lista completa de pares al método
            configurarSpinnerTipoLavado(tipos)
        })

        // Inicializar fecha y hora después de que la configuración se haya cargado
        inicializarFechaHora()
    }

    private fun configurarBotones() {
        with(binding) {
            botonSelectorFecha.setOnClickListener { mostrarSelectorFecha() }
            botonSelectorHora.setOnClickListener { mostrarSelectorHora() }
            botonGuardar.setOnClickListener { guardarCita() }
        }
    }

    private fun configurarSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.sincronizar()
        }
    }

    private fun configurarSpinnerTipoLavado(tiposLavado: List<Pair<String, String>>) {
        // Aquí usamos el segundo valor del par (título) para mostrar al usuario
        val titulos = tiposLavado.map { it.second }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            titulos
        )

        (binding.spinnerTipoLavado as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            if (titulos.isNotEmpty() && tiposLavado.isNotEmpty()) {
                setText(titulos[0], false)
                // Guardamos el código (primer valor del par) que es el nombre del enum
                tipoLavadoSeleccionado = tiposLavado[0].first
                Log.d(TAG, "Tipo de lavado seleccionado inicialmente: ${tipoLavadoSeleccionado}")
            }
            setOnItemClickListener { _, _, position, _ ->
                // Guardamos el código (primer valor del par)
                if (position < tiposLavado.size) {
                    tipoLavadoSeleccionado = tiposLavado[position].first
                    Log.d(TAG, "Nuevo tipo de lavado seleccionado: ${tipoLavadoSeleccionado}")
                }
            }
        }
    }

    private fun inicializarFechaHora() {
        // Usar la versión centralizada para ajustar la fecha
        calendario = HorariosUtil.ajustarAProximaFechaValida(calendario)

        actualizarTextoFecha(
            calendario.get(Calendar.DAY_OF_MONTH),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.YEAR)
        )

        // Ajustar a la próxima hora válida
        calendario = HorariosUtil.ajustarAProximaHoraValida(calendario)

        actualizarTextoHora(
            calendario.get(Calendar.HOUR_OF_DAY),
            calendario.get(Calendar.MINUTE)
        )
    }

    private fun mostrarSelectorFecha() {
        val fechaMinima = Calendar.getInstance().apply {
            if (get(Calendar.HOUR_OF_DAY) >= HorariosUtil.HORA_CIERRE_TARDE) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
            while (get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val fechaSeleccionada = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }

                if (fechaSeleccionada.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    mostrarMensaje("Día no disponible", "Los Domingos estamos cerrados")
                    return@DatePickerDialog
                }

                if (fechaSeleccionada.before(fechaMinima)) {
                    mostrarMensaje("Fecha no válida", "No se pueden programar citas en fechas pasadas")
                    return@DatePickerDialog
                }

                calendario.set(Calendar.YEAR, year)
                calendario.set(Calendar.MONTH, month)
                calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Si seleccionamos un día diferente al actual, reiniciar la hora
                if (!HorariosUtil.esMismoDia(fechaSeleccionada, Calendar.getInstance())) {
                    calendario.set(Calendar.HOUR_OF_DAY, HorariosUtil.HORA_APERTURA_MAÑANA)
                    calendario.set(Calendar.MINUTE, 0)
                    actualizarTextoHora(HorariosUtil.HORA_APERTURA_MAÑANA, 0)
                } else {
                    // Si es el mismo día, ajustar a la próxima hora válida
                    calendario = HorariosUtil.ajustarAProximaHoraValida(calendario)
                    actualizarTextoHora(
                        calendario.get(Calendar.HOUR_OF_DAY),
                        calendario.get(Calendar.MINUTE)
                    )
                }

                actualizarTextoFecha(dayOfMonth, month, year)
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = fechaMinima.timeInMillis
            show()
        }
    }

    private fun mostrarSelectorHora() {
        val fecha = binding.textoFecha.text.toString()

        lifecycleScope.launch {
            try {
                // Mostrar indicador de carga
                binding.progressBar.visibility = View.VISIBLE

                // Obtener horarios del servidor
                val response = viewModel.obtenerHorariosDisponibles(fecha)

                // Ocultar indicador de carga
                binding.progressBar.visibility = View.GONE

                if (response.isNotEmpty()) {
                    // Mostrar diálogo con horarios disponibles
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Seleccione una hora")
                        .setItems(response.toTypedArray()) { _, which ->
                            val horaSeleccionada = response[which]
                            binding.textoHora.text = horaSeleccionada

                            // Actualizar el calendario interno
                            val partes = horaSeleccionada.split(":")
                            if (partes.size == 2) {
                                val hora = partes[0].toInt()
                                val minuto = partes[1].toInt()
                                calendario.set(Calendar.HOUR_OF_DAY, hora)
                                calendario.set(Calendar.MINUTE, minuto)
                            }
                        }
                        .show()
                } else {
                    // Mostrar mensaje si no hay horarios disponibles
                    Toast.makeText(
                        requireContext(),
                        "No hay horarios disponibles para esta fecha",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                // Ocultar indicador de carga y mostrar error
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Error al obtener horarios: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "Error al obtener horarios", e)
            }
        }
    }

    private fun validarFechaHora(): Boolean {
        return HorariosUtil.validarFechaHora(calendario)
    }

    private fun mostrarDialogoHorarioOcupado(fecha: String, hora: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Horario No Disponible")
            .setMessage("Ya existe una cita programada para el $fecha a las $hora. Por favor seleccione otro horario.")
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun actualizarTextoFecha(dia: Int, mes: Int, anio: Int) {
        binding.textoFecha.text = HorariosUtil.formatearFecha(dia, mes, anio)
    }

    private fun actualizarTextoHora(hora: Int, minuto: Int) {
        binding.textoHora.text = HorariosUtil.formatearHora(hora, minuto)
    }

    private fun guardarCita() {
        with(binding) {
            val fecha = textoFecha.text.toString()
            val hora = textoHora.text.toString()
            val nombreCliente = editNombreCliente.text.toString()
            val email = editEmail.text.toString() // Campo nuevo
            val modeloCoche = editModeloCoche.text.toString()
            val telefono = editTelefono.text.toString()

            // Validar campos obligatorios (email es opcional)
            if (!validarCampos(fecha, hora, nombreCliente, modeloCoche, telefono)) {
                mostrarMensaje("Campos incompletos", "Por favor complete todos los campos obligatorios")
                return
            }

            // Validar que la fecha y hora sean válidas
            if (!validarFechaHora()) {
                mostrarMensaje("Fecha u Hora no válida", "La fecha u hora seleccionada no es válida")
                return
            }

            // Verificar que el tipo de lavado esté seleccionado
            if (tipoLavadoSeleccionado.isEmpty()) {
                mostrarMensaje("Tipo de lavado no seleccionado", "Por favor seleccione un tipo de lavado")
                return
            }

            Log.d(TAG, "Guardando cita con tipo de lavado: $tipoLavadoSeleccionado")

            // Verificar disponibilidad del horario
            lifecycleScope.launch {
                if (viewModel.existeCitaEnFechaHora(fecha, hora)) {
                    mostrarDialogoHorarioOcupado(fecha, hora)
                    return@launch
                }

                // Guardar la cita
                guardarCitaEnBaseDeDatos(fecha, hora, nombreCliente, email, modeloCoche, telefono)
            }
        }
    }

    private fun validarCampos(
        fecha: String,
        hora: String,
        nombreCliente: String,
        modeloCoche: String,
        telefono: String
    ): Boolean {
        return fecha.isNotEmpty() &&
                hora.isNotEmpty() &&
                nombreCliente.isNotEmpty() &&
                modeloCoche.isNotEmpty() &&
                telefono.isNotEmpty() &&
                tipoLavadoSeleccionado.isNotEmpty()
    }

    private fun mostrarMensaje(titulo: String, mensaje: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun mostrarMensajeExito() {
        Toast.makeText(context, "Cita guardada con éxito", Toast.LENGTH_SHORT).show()
        limpiarCampos()
    }

    private fun guardarCitaEnBaseDeDatos(
        fecha: String,
        hora: String,
        nombreCliente: String,
        email: String, // Nuevo parámetro
        modeloCoche: String,
        telefono: String
    ) {
        try {
            Log.d(TAG, "Creando cita con fecha: $fecha, hora: $hora, tipo lavado: $tipoLavadoSeleccionado")

            val cita = Cita(
                fecha = fecha,
                hora = hora,
                nombreCliente = nombreCliente,
                modeloCoche = modeloCoche,
                telefono = telefono,
                tipoLavado = tipoLavadoSeleccionado,
                email = email // Nuevo campo
            )

            Log.d(TAG, "Enviando cita al ViewModel: $cita")
            viewModel.agregarCita(cita)
            mostrarMensajeExito()
        } catch (e: Exception) {
            // Manejar explícitamente cualquier error al guardar
            Log.e(TAG, "Error al guardar la cita", e)
            Snackbar.make(
                binding.root,
                "Error al guardar la cita: ${e.message}",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun limpiarCampos() {
        with(binding) {
            editNombreCliente.text?.clear()
            editModeloCoche.text?.clear()
            editTelefono.text?.clear()
            editEmail.text?.clear()

            // Reiniciar fecha y hora para la próxima cita
            inicializarFechaHora()

            // Reiniciar tipo de lavado al primer valor
            viewModel.tiposLavado.value?.let { tipos ->
                if (tipos.isNotEmpty()) {
                    (spinnerTipoLavado as AutoCompleteTextView).setText(tipos[0].second, false)
                    // CORREGIDO: Usar first en lugar de second para consistencia
                    tipoLavadoSeleccionado = tipos[0].first
                    Log.d(TAG, "Tipo de lavado reiniciado a: ${tipoLavadoSeleccionado}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
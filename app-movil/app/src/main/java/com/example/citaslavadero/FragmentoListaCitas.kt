package com.example.citaslavadero

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.citaslavadero.database.Cita
import com.example.citaslavadero.databinding.FragmentListaCitasBinding
import com.google.android.material.snackbar.Snackbar

class FragmentoListaCitas : Fragment() {

    private var _binding: FragmentListaCitasBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CitaViewModel by viewModels({ requireActivity() })
    private lateinit var citasAdapter: CitasAdapter
    private val TAG = "FragmentoListaCitas"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaCitasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        viewModel.inicializar(requireContext())
        configurarRecyclerView()
        configurarObservadores()
        configurarSwipeRefresh()

        // Cargar citas del servidor al iniciar
        cargarCitas()
    }

    private fun cargarCitas() {
        Log.d(TAG, "Cargando citas desde el servidor")
        viewModel.cargarCitas()
    }

    private fun configurarRecyclerView() {
        Log.d(TAG, "Configurando RecyclerView")
        citasAdapter = CitasAdapter(
            onCitaClick = { cita ->
                Log.d(TAG, "Cita seleccionada: $cita")
                mostrarDetallesCita(cita)
            },
            onCitaLongClick = { cita ->
                Log.d(TAG, "Cita seleccionada para eliminar: $cita")
                mostrarDialogoEliminar(cita)
                true
            }
        )

        binding.recyclerViewCitas.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = citasAdapter
        }
    }

    private fun configurarObservadores() {
        Log.d(TAG, "Configurando observadores")

        // Observar la lista de citas
        viewModel.todasLasCitas.observe(viewLifecycleOwner, Observer { citas ->
            Log.d(TAG, "Recibidas ${citas.size} citas para mostrar")
            citasAdapter.submitList(citas)
            actualizarVistaVacia(citas.isEmpty())
        })

        // Observar errores
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { mensaje ->
            mensaje?.let {
                Log.e(TAG, "Error recibido: $it")
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.limpiarError()
            }
        })

        // Observar estado de carga
        viewModel.cargando.observe(viewLifecycleOwner, Observer { cargando ->
            Log.d(TAG, "Estado de carga: $cargando")
            binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        })

        // Observar estado de sincronización
        viewModel.sincronizando.observe(viewLifecycleOwner, Observer { sincronizando ->
            Log.d(TAG, "Estado de sincronización: $sincronizando")
            binding.swipeRefreshLayout.isRefreshing = sincronizando
        })
    }

    private fun configurarSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            Log.d(TAG, "SwipeRefresh activado, sincronizando datos")
            viewModel.sincronizar()
        }
    }

    private fun actualizarVistaVacia(estaVacia: Boolean) {
        Log.d(TAG, "Actualizando vista vacía: $estaVacia")
        binding.textViewSinCitas.visibility = if (estaVacia) View.VISIBLE else View.GONE
        binding.recyclerViewCitas.visibility = if (estaVacia) View.GONE else View.VISIBLE
    }

    private fun mostrarDetallesCita(cita: Cita) {
        // Mostrar detalles de cita
        val mensaje = """
            Cliente: ${cita.nombreCliente}
            Email: ${if (cita.email.isBlank()) "No proporcionado" else cita.email}
            Vehículo: ${cita.modeloCoche}
            Teléfono: ${cita.telefono}
            Fecha: ${cita.fecha}
            Hora: ${cita.hora}
            Tipo de lavado: ${cita.tipoLavado}
            Estado: ${cita.estado}
        """.trimIndent()

        DialogoConfirmacion.mostrarDialogoConfirmacion(
            requireContext(),
            "Detalles de la Cita",
            mensaje
        )
    }

    private fun mostrarDialogoEliminar(cita: Cita) {
        DialogoConfirmacion.mostrarDialogoConfirmacion(
            requireContext(),
            "Eliminar Cita",
            "¿Está seguro de que desea eliminar esta cita de ${cita.nombreCliente} el ${cita.fecha} a las ${cita.hora}?"
        ) {
            Log.d(TAG, "Confirmada eliminación de cita: $cita")
            viewModel.eliminarCita(cita)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
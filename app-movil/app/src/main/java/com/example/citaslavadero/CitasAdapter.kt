package com.example.citaslavadero

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.citaslavadero.database.Cita
import com.example.citaslavadero.databinding.ItemCitaBinding

class CitasAdapter(
    private val onCitaClick: (Cita) -> Unit,
    private val onCitaLongClick: (Cita) -> Boolean
) : ListAdapter<Cita, CitasAdapter.CitaViewHolder>(CitaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        return CitaViewHolder(
            ItemCitaBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CitaViewHolder(
        private val binding: ItemCitaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCitaClick(getItem(position))
                }
            }

            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCitaLongClick(getItem(position))
                } else {
                    false
                }
            }
        }

        fun bind(cita: Cita) {
            binding.apply {
                textoFechaHora.text = "${cita.fecha} - ${cita.hora}"
                textoCliente.text = "Cliente: ${cita.nombreCliente}"
                textoCoche.text = "Vehículo: ${cita.modeloCoche}"
                textoTelefono.text = "Teléfono: ${cita.telefono}"
                textoTipoLavado.text = "Lavado: ${cita.tipoLavado}"
            }
        }
    }

    private class CitaDiffCallback : DiffUtil.ItemCallback<Cita>() {
        override fun areItemsTheSame(oldItem: Cita, newItem: Cita): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Cita, newItem: Cita): Boolean =
            oldItem == newItem
    }
}
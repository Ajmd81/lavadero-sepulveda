package com.example.citaslavadero

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DialogoConfirmacion {
    // Método original con callback requerido
    fun mostrarDialogoConfirmacion(
        context: Context,
        titulo: String,
        mensaje: String,
        onConfirmar: () -> Unit
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Confirmar") { dialog, _ ->
                onConfirmar()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Sobrecarga para cuando no se necesita acción al confirmar
    fun mostrarDialogoConfirmacion(
        context: Context,
        titulo: String,
        mensaje: String
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
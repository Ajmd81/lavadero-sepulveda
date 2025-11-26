package com.example.citaslavadero.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utilidades para manejar fechas y horas
 */
object DateTimeUtils {

    private const val DATE_FORMAT = "dd/MM/yyyy"
    private const val TIME_FORMAT = "HH:mm"

    /**
     * Convierte una fecha de String a Date
     */
    fun parseDate(dateString: String): Date? {
        return try {
            SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Formatea una fecha en formato dd/MM/yyyy
     */
    fun formatDate(date: Date): String {
        return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(date)
    }

    /**
     * Formatea una hora en formato HH:mm
     */
    fun formatTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        return SimpleDateFormat(TIME_FORMAT, Locale.getDefault()).format(calendar.time)
    }

    /**
     * Verifica si una fecha es hoy
     */
    fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val calendar = Calendar.getInstance()
        calendar.time = date

        return today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
    }
}
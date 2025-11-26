package com.example.citaslavadero.util

import java.util.*

/**
 * Utilidades para manejar los horarios del negocio
 */
object HorariosUtil {
    // Constantes de horario
    const val HORA_APERTURA_MAÑANA = 9
    const val HORA_CIERRE_MAÑANA = 14
    const val HORA_APERTURA_TARDE = 17
    const val HORA_CIERRE_TARDE = 20
    const val INTERVALO_MINUTOS = 60

    /**
     * Verifica si una hora está dentro del horario comercial
     */
    fun esHorarioComercial(hora: Int, minuto: Int): Boolean {
        return (hora in HORA_APERTURA_MAÑANA until HORA_CIERRE_MAÑANA) ||
                (hora in HORA_APERTURA_TARDE until HORA_CIERRE_TARDE)
    }

    /**
     * Genera los horarios disponibles para un día completo
     */
    fun generarHorariosDisponibles(): List<String> {
        val horarios = mutableListOf<String>()

        // Añadir horarios de mañana
        for (hora in HORA_APERTURA_MAÑANA until HORA_CIERRE_MAÑANA) {
            for (minuto in 0 until 60 step INTERVALO_MINUTOS) {
                horarios.add(String.format("%02d:%02d", hora, minuto))
            }
        }

        // Añadir horarios de tarde
        for (hora in HORA_APERTURA_TARDE until HORA_CIERRE_TARDE) {
            for (minuto in 0 until 60 step INTERVALO_MINUTOS) {
                horarios.add(String.format("%02d:%02d", hora, minuto))
            }
        }

        return horarios
    }

    /**
     * Ajusta a la próxima hora válida del horario comercial
     */
    fun ajustarAProximaHoraValida(calendario: Calendar): Calendar {
        val hora = calendario.get(Calendar.HOUR_OF_DAY)
        val cal = calendario.clone() as Calendar

        when {
            // Si estamos antes del horario de mañana
            hora < HORA_APERTURA_MAÑANA -> {
                cal.set(Calendar.HOUR_OF_DAY, HORA_APERTURA_MAÑANA)
                cal.set(Calendar.MINUTE, 0)
            }
            // Si estamos en el descanso del mediodía
            hora in HORA_CIERRE_MAÑANA until HORA_APERTURA_TARDE -> {
                cal.set(Calendar.HOUR_OF_DAY, HORA_APERTURA_TARDE)
                cal.set(Calendar.MINUTE, 0)
            }
            // Si estamos después del cierre
            hora >= HORA_CIERRE_TARDE -> {
                cal.add(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, HORA_APERTURA_MAÑANA)
                cal.set(Calendar.MINUTE, 0)

                // Si el día siguiente es domingo, avanzar al lunes
                if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            // Si estamos en horario comercial, ajustar al siguiente intervalo
            else -> {
                val minutoActual = cal.get(Calendar.MINUTE)
                val minutosExtra = minutoActual % INTERVALO_MINUTOS

                if (minutosExtra > 0) {
                    cal.add(Calendar.MINUTE, INTERVALO_MINUTOS - minutosExtra)
                }

                // Verificar si después del ajuste seguimos en horario comercial
                val horaAjustada = cal.get(Calendar.HOUR_OF_DAY)

                // Si estamos en la mañana pero el ajuste nos saca del horario
                if (hora < HORA_CIERRE_MAÑANA && horaAjustada >= HORA_CIERRE_MAÑANA) {
                    cal.set(Calendar.HOUR_OF_DAY, HORA_APERTURA_TARDE)
                    cal.set(Calendar.MINUTE, 0)
                }
                // Si estamos en la tarde pero el ajuste nos saca del horario
                else if (hora < HORA_CIERRE_TARDE && horaAjustada >= HORA_CIERRE_TARDE) {
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, HORA_APERTURA_MAÑANA)
                    cal.set(Calendar.MINUTE, 0)

                    // Si el día siguiente es domingo, avanzar al lunes
                    if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        cal.add(Calendar.DAY_OF_MONTH, 1)
                    }
                }
            }
        }

        return cal
    }

    /**
     * Ajusta a la próxima fecha válida (que no sea domingo)
     */
    fun ajustarAProximaFechaValida(calendario: Calendar): Calendar {
        val fechaActual = Calendar.getInstance()
        val cal = calendario.clone() as Calendar

        cal.set(Calendar.HOUR_OF_DAY, HORA_APERTURA_MAÑANA)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // Si ya pasó la hora de cierre, avanzar al día siguiente
        if (fechaActual.get(Calendar.HOUR_OF_DAY) >= HORA_CIERRE_TARDE) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Si es domingo, avanzar al lunes
        while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        return cal
    }

    /**
     * Verifica si dos calendarios representan el mismo día
     */
    fun esMismoDia(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * Valida si una fecha y hora son válidas para programar una cita
     */
    fun validarFechaHora(calendario: Calendar): Boolean {
        val ahora = Calendar.getInstance()

        // No se permiten citas en domingo
        if (calendario.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            return false
        }

        // No se permiten citas en el pasado
        if (calendario.before(ahora)) {
            return false
        }

        // Verificar que la hora esté dentro del horario comercial
        val hora = calendario.get(Calendar.HOUR_OF_DAY)
        val minuto = calendario.get(Calendar.MINUTE)

        if (!esHorarioComercial(hora, minuto)) {
            return false
        }

        // Verificaciones adicionales para el mismo día
        if (esMismoDia(calendario, ahora)) {
            val horaActual = ahora.get(Calendar.HOUR_OF_DAY)
            val minutosActuales = ahora.get(Calendar.MINUTE)

            // No se pueden programar citas en el pasado
            if (hora < horaActual || (hora == horaActual && minuto <= minutosActuales)) {
                return false
            }
        }

        return true
    }

    /**
     * Formatea la hora en formato HH:MM
     */
    fun formatearHora(hora: Int, minuto: Int): String {
        return String.format("%02d:%02d", hora, minuto)
    }

    /**
     * Formatea la fecha en formato dd/MM/yyyy
     */
    fun formatearFecha(dia: Int, mes: Int, anio: Int): String {
        return String.format("%02d/%02d/%d", dia, mes + 1, anio)
    }
}
package com.example.citaslavadero.util

import java.util.*

/**
 * Utilidades para manejar los horarios del negocio
 */
object HorariosUtil {
    // Constantes de horario Lunes a Viernes
    const val HORA_APERTURA_MAÑANA = 8
    const val HORA_CIERRE_MAÑANA = 15
    
    // Constantes de horario Sábados
    const val HORA_APERTURA_SABADO = 9
    const val HORA_CIERRE_SABADO = 13
    
    // Sin turno de tarde actualmente
    const val HORA_APERTURA_TARDE = 0
    const val HORA_CIERRE_TARDE = 0
    
    const val INTERVALO_MINUTOS = 60

    /**
     * Verifica si una hora está dentro del horario comercial según el día
     */
    fun esHorarioComercial(hora: Int, minuto: Int, diaSemana: Int): Boolean {
        // Domingo (Calendar.SUNDAY = 1) cerrado
        if (diaSemana == Calendar.SUNDAY) {
            return false
        }
        
        // Sábado (Calendar.SATURDAY = 7)
        if (diaSemana == Calendar.SATURDAY) {
            return hora in HORA_APERTURA_SABADO until HORA_CIERRE_SABADO
        }
        
        // Lunes a Viernes
        return hora in HORA_APERTURA_MAÑANA until HORA_CIERRE_MAÑANA
    }
    
    /**
     * Verifica si una hora está dentro del horario comercial (versión simple para L-V)
     */
    fun esHorarioComercial(hora: Int, minuto: Int): Boolean {
        return hora in HORA_APERTURA_MAÑANA until HORA_CIERRE_MAÑANA
    }

    /**
     * Genera los horarios disponibles según el día de la semana
     */
    fun generarHorariosDisponibles(diaSemana: Int): List<String> {
        val horarios = mutableListOf<String>()
        
        // Domingo cerrado
        if (diaSemana == Calendar.SUNDAY) {
            return horarios
        }
        
        val horaInicio: Int
        val horaFin: Int
        
        if (diaSemana == Calendar.SATURDAY) {
            // Sábado: 9:00 - 13:00
            horaInicio = HORA_APERTURA_SABADO
            horaFin = HORA_CIERRE_SABADO
        } else {
            // Lunes a Viernes: 8:00 - 15:00
            horaInicio = HORA_APERTURA_MAÑANA
            horaFin = HORA_CIERRE_MAÑANA
        }
        
        for (hora in horaInicio until horaFin) {
            for (minuto in 0 until 60 step INTERVALO_MINUTOS) {
                horarios.add(String.format("%02d:%02d", hora, minuto))
            }
        }
        
        return horarios
    }
    
    /**
     * Genera los horarios disponibles para un día completo (Lunes a Viernes)
     */
    fun generarHorariosDisponibles(): List<String> {
        val horarios = mutableListOf<String>()

        // Horarios de mañana (8:00 - 15:00)
        for (hora in HORA_APERTURA_MAÑANA until HORA_CIERRE_MAÑANA) {
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
        val diaSemana = calendario.get(Calendar.DAY_OF_WEEK)
        val cal = calendario.clone() as Calendar
        
        // Si es domingo, avanzar al lunes
        if (diaSemana == Calendar.SUNDAY) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, HORA_APERTURA_MAÑANA)
            cal.set(Calendar.MINUTE, 0)
            return cal
        }
        
        val horaApertura = if (diaSemana == Calendar.SATURDAY) HORA_APERTURA_SABADO else HORA_APERTURA_MAÑANA
        val horaCierre = if (diaSemana == Calendar.SATURDAY) HORA_CIERRE_SABADO else HORA_CIERRE_MAÑANA

        when {
            // Si estamos antes de la apertura
            hora < horaApertura -> {
                cal.set(Calendar.HOUR_OF_DAY, horaApertura)
                cal.set(Calendar.MINUTE, 0)
            }
            // Si estamos después del cierre
            hora >= horaCierre -> {
                cal.add(Calendar.DAY_OF_MONTH, 1)
                
                // Si el día siguiente es domingo, avanzar al lunes
                if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                }
                
                // Establecer hora de apertura según el nuevo día
                val nuevoDia = cal.get(Calendar.DAY_OF_WEEK)
                val nuevaHoraApertura = if (nuevoDia == Calendar.SATURDAY) HORA_APERTURA_SABADO else HORA_APERTURA_MAÑANA
                
                cal.set(Calendar.HOUR_OF_DAY, nuevaHoraApertura)
                cal.set(Calendar.MINUTE, 0)
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

                if (horaAjustada >= horaCierre) {
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                    
                    // Si el día siguiente es domingo, avanzar al lunes
                    if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        cal.add(Calendar.DAY_OF_MONTH, 1)
                    }
                    
                    val nuevoDia = cal.get(Calendar.DAY_OF_WEEK)
                    val nuevaHoraApertura = if (nuevoDia == Calendar.SATURDAY) HORA_APERTURA_SABADO else HORA_APERTURA_MAÑANA
                    
                    cal.set(Calendar.HOUR_OF_DAY, nuevaHoraApertura)
                    cal.set(Calendar.MINUTE, 0)
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
        val diaSemana = cal.get(Calendar.DAY_OF_WEEK)
        
        val horaApertura = if (diaSemana == Calendar.SATURDAY) HORA_APERTURA_SABADO else HORA_APERTURA_MAÑANA
        val horaCierre = if (diaSemana == Calendar.SATURDAY) HORA_CIERRE_SABADO else HORA_CIERRE_MAÑANA

        cal.set(Calendar.HOUR_OF_DAY, horaApertura)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // Si ya pasó la hora de cierre, avanzar al día siguiente
        if (fechaActual.get(Calendar.HOUR_OF_DAY) >= horaCierre) {
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
        val diaSemana = calendario.get(Calendar.DAY_OF_WEEK)

        // No se permiten citas en domingo
        if (diaSemana == Calendar.SUNDAY) {
            return false
        }

        // No se permiten citas en el pasado
        if (calendario.before(ahora)) {
            return false
        }

        // Verificar que la hora esté dentro del horario comercial
        val hora = calendario.get(Calendar.HOUR_OF_DAY)
        val minuto = calendario.get(Calendar.MINUTE)

        if (!esHorarioComercial(hora, minuto, diaSemana)) {
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
    
    /**
     * Obtiene la hora de apertura según el día de la semana
     */
    fun getHoraApertura(diaSemana: Int): Int {
        return if (diaSemana == Calendar.SATURDAY) HORA_APERTURA_SABADO else HORA_APERTURA_MAÑANA
    }
    
    /**
     * Obtiene la hora de cierre según el día de la semana
     */
    fun getHoraCierre(diaSemana: Int): Int {
        return if (diaSemana == Calendar.SATURDAY) HORA_CIERRE_SABADO else HORA_CIERRE_MAÑANA
    }
}
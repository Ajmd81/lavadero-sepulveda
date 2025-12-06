package com.lavaderosepulveda.app.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utilidades centralizadas para el formateo de fechas y horas
 * Elimina la duplicación de formatters en múltiples clases
 */
@Component
public class DateTimeFormatUtils {

    // Formatters estáticos reutilizables
    public static final DateTimeFormatter FECHA_COMPLETA_ES =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

    public static final DateTimeFormatter FECHA_CORTA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static final DateTimeFormatter HORA_CORTA =
            DateTimeFormatter.ofPattern("HH:mm");

    public static final DateTimeFormatter FECHA_ISO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Formatea una fecha en español con formato completo
     * Ejemplo: "Lunes, 15 de junio de 2025"
     */
    public static String formatearFechaCompleta(LocalDate fecha) {
        if (fecha == null) return "";

        String formatted = fecha.format(FECHA_COMPLETA_ES);
        // Capitalizar la primera letra del día de la semana
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
    }

    /**
     * Formatea una fecha en formato corto
     * Ejemplo: "15/06/2025"
     */
    public static String formatearFechaCorta(LocalDate fecha) {
        if (fecha == null) return "";
        return fecha.format(FECHA_CORTA);
    }

    /**
     * Formatea una hora en formato corto
     * Ejemplo: "14:30"
     */
    public static String formatearHoraCorta(LocalTime hora) {
        if (hora == null) return "";
        return hora.format(HORA_CORTA);
    }

    /**
     * Formatea una fecha en formato ISO
     * Ejemplo: "2025-06-15"
     */
    public static String formatearFechaISO(LocalDate fecha) {
        if (fecha == null) return "";
        return fecha.format(FECHA_ISO);
    }

    /**
     * Parsea una fecha desde formato corto
     * Ejemplo: "15/06/2025" -> LocalDate
     */
    public static LocalDate parsearFechaCorta(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            throw new IllegalArgumentException("La fecha no puede estar vacía");
        }
        return LocalDate.parse(fechaStr, FECHA_CORTA);
    }

    /**
     * Parsea una hora desde formato corto
     * Ejemplo: "14:30" -> LocalTime
     */
    public static LocalTime parsearHoraCorta(String horaStr) {
        if (horaStr == null || horaStr.trim().isEmpty()) {
            throw new IllegalArgumentException("La hora no puede estar vacía");
        }
        return LocalTime.parse(horaStr, HORA_CORTA);
    }

    /**
     * Valida si una cadena tiene formato de fecha válido (dd/MM/yyyy)
     */
    public static boolean esFechaValidaCorta(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return false;
        }
        try {
            parsearFechaCorta(fechaStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Valida si una cadena tiene formato de hora válido (HH:mm)
     */
    public static boolean esHoraValidaCorta(String horaStr) {
        if (horaStr == null || horaStr.trim().isEmpty()) {
            return false;
        }
        try {
            parsearHoraCorta(horaStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
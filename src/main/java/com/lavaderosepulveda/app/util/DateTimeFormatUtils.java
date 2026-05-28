package com.lavaderosepulveda.app.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Component
public class DateTimeFormatUtils {

    public static final DateTimeFormatter FECHA_COMPLETA_ES = DateTimeFormatter.ofPattern(
            "EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

    public static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static final DateTimeFormatter HORA_CORTA = DateTimeFormatter.ofPattern("HH:mm");

    public static final DateTimeFormatter FECHA_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String formatearFechaCompleta(LocalDate fecha) {
        if (fecha == null) return "";
        String formatted = fecha.format(FECHA_COMPLETA_ES);
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
    }

    public static String formatearFechaCorta(LocalDate fecha) {
        if (fecha == null) return "";
        return fecha.format(FECHA_CORTA);
    }

    public static String formatearHoraCorta(LocalTime hora) {
        if (hora == null) return "";
        return hora.format(HORA_CORTA);
    }

    public static String formatearFechaISO(LocalDate fecha) {
        if (fecha == null) return "";
        return fecha.format(FECHA_ISO);
    }

    /**
     * Parsea una fecha aceptando dos formatos:
     *  - ISO:   yyyy-MM-dd  (enviado por el CRM y la app móvil)
     *  - Corto: dd/MM/yyyy  (usado internamente en vistas Thymeleaf)
     */
    public static LocalDate parsearFechaCorta(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            throw new IllegalArgumentException("La fecha no puede estar vacía");
        }
        String valor = fechaStr.trim();

        // Intentar ISO primero (yyyy-MM-dd) — formato más común en la API
        try {
            return LocalDate.parse(valor, FECHA_ISO);
        } catch (DateTimeParseException e1) {
            // Fallback a formato corto (dd/MM/yyyy)
            try {
                return LocalDate.parse(valor, FECHA_CORTA);
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException(
                        "Formato de fecha no válido: '" + valor +
                        "'. Formatos aceptados: yyyy-MM-dd o dd/MM/yyyy");
            }
        }
    }

    public static LocalTime parsearHoraCorta(String horaStr) {
        if (horaStr == null || horaStr.trim().isEmpty()) {
            throw new IllegalArgumentException("La hora no puede estar vacía");
        }
        return LocalTime.parse(horaStr.trim(), HORA_CORTA);
    }

    public static boolean esFechaValidaCorta(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) return false;
        try {
            parsearFechaCorta(fechaStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean esHoraValidaCorta(String horaStr) {
        if (horaStr == null || horaStr.trim().isEmpty()) return false;
        try {
            parsearHoraCorta(horaStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
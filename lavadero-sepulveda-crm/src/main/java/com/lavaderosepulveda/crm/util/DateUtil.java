package com.lavaderosepulveda.crm.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Utilidades para trabajar con fechas y horas
 */
public class DateUtil {
    
    private static final Locale LOCALE_ES = new Locale("es", "ES");
    
    // Formateadores comunes
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", LOCALE_ES);
    
    /**
     * Formatea una fecha en formato dd/MM/yyyy
     */
    public static String formatearFecha(LocalDate fecha) {
        return fecha != null ? fecha.format(DATE_FORMATTER) : "";
    }
    
    /**
     * Formatea una fecha y hora en formato dd/MM/yyyy HH:mm
     */
    public static String formatearFechaHora(LocalDateTime fechaHora) {
        return fechaHora != null ? fechaHora.format(DATE_TIME_FORMATTER) : "";
    }
    
    /**
     * Formatea solo la hora en formato HH:mm
     */
    public static String formatearHora(LocalDateTime fechaHora) {
        return fechaHora != null ? fechaHora.format(TIME_FORMATTER) : "";
    }
    
    /**
     * Formatea mes y año (ej: "Diciembre 2024")
     */
    public static String formatearMesAnio(LocalDate fecha) {
        return fecha != null ? fecha.format(MONTH_YEAR_FORMATTER) : "";
    }
    
    /**
     * Convierte un LocalDate a LocalDateTime al inicio del día (00:00:00)
     */
    public static LocalDateTime inicioDelDia(LocalDate fecha) {
        return fecha.atStartOfDay();
    }
    
    /**
     * Convierte un LocalDate a LocalDateTime al final del día (23:59:59)
     */
    public static LocalDateTime finDelDia(LocalDate fecha) {
        return fecha.atTime(LocalTime.MAX);
    }
    
    /**
     * Obtiene el primer día del mes actual
     */
    public static LocalDate primerDiaDelMes() {
        return LocalDate.now().withDayOfMonth(1);
    }
    
    /**
     * Obtiene el último día del mes actual
     */
    public static LocalDate ultimoDiaDelMes() {
        LocalDate hoy = LocalDate.now();
        return hoy.withDayOfMonth(hoy.lengthOfMonth());
    }
    
    /**
     * Obtiene el primer día del año actual
     */
    public static LocalDate primerDiaDelAnio() {
        return LocalDate.now().withDayOfYear(1);
    }
    
    /**
     * Obtiene el último día del año actual
     */
    public static LocalDate ultimoDiaDelAnio() {
        return LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
    }
    
    /**
     * Calcula los días entre dos fechas
     */
    public static long diasEntre(LocalDate inicio, LocalDate fin) {
        return ChronoUnit.DAYS.between(inicio, fin);
    }
    
    /**
     * Calcula las horas entre dos fechas y horas
     */
    public static long horasEntre(LocalDateTime inicio, LocalDateTime fin) {
        return ChronoUnit.HOURS.between(inicio, fin);
    }
    
    /**
     * Calcula los minutos entre dos fechas y horas
     */
    public static long minutosEntre(LocalDateTime inicio, LocalDateTime fin) {
        return ChronoUnit.MINUTES.between(inicio, fin);
    }
    
    /**
     * Verifica si una fecha es hoy
     */
    public static boolean esHoy(LocalDate fecha) {
        return fecha != null && fecha.equals(LocalDate.now());
    }
    
    /**
     * Verifica si una fecha está en el pasado
     */
    public static boolean esPasado(LocalDate fecha) {
        return fecha != null && fecha.isBefore(LocalDate.now());
    }
    
    /**
     * Verifica si una fecha está en el futuro
     */
    public static boolean esFuturo(LocalDate fecha) {
        return fecha != null && fecha.isAfter(LocalDate.now());
    }
    
    /**
     * Obtiene el nombre del día de la semana
     */
    public static String obtenerNombreDia(LocalDate fecha) {
        if (fecha == null) return "";
        
        DayOfWeek dia = fecha.getDayOfWeek();
        return switch (dia) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }
    
    /**
     * Obtiene el nombre del mes
     */
    public static String obtenerNombreMes(int mes) {
        return switch (mes) {
            case 1 -> "Enero";
            case 2 -> "Febrero";
            case 3 -> "Marzo";
            case 4 -> "Abril";
            case 5 -> "Mayo";
            case 6 -> "Junio";
            case 7 -> "Julio";
            case 8 -> "Agosto";
            case 9 -> "Septiembre";
            case 10 -> "Octubre";
            case 11 -> "Noviembre";
            case 12 -> "Diciembre";
            default -> "";
        };
    }
    
    /**
     * Verifica si una fecha está dentro de un rango
     */
    public static boolean estaEnRango(LocalDate fecha, LocalDate inicio, LocalDate fin) {
        if (fecha == null || inicio == null || fin == null) {
            return false;
        }
        return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
    }
    
    /**
     * Añade días laborables a una fecha (excluyendo sábados y domingos)
     */
    public static LocalDate agregarDiasLaborables(LocalDate fecha, int dias) {
        LocalDate resultado = fecha;
        int diasAgregados = 0;
        
        while (diasAgregados < dias) {
            resultado = resultado.plusDays(1);
            if (resultado.getDayOfWeek() != DayOfWeek.SATURDAY && 
                resultado.getDayOfWeek() != DayOfWeek.SUNDAY) {
                diasAgregados++;
            }
        }
        
        return resultado;
    }
    
    /**
     * Verifica si una fecha es fin de semana
     */
    public static boolean esFinDeSemana(LocalDate fecha) {
        if (fecha == null) return false;
        DayOfWeek dia = fecha.getDayOfWeek();
        return dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY;
    }
}

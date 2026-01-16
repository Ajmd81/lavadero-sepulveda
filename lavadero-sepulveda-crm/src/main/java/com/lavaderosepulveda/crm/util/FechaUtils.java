package com.lavaderosepulveda.crm.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utilidades para el manejo de fechas
 */
@Slf4j
public class FechaUtils {

    private FechaUtils() {
        // Clase de utilidades, no instanciable
    }

    /**
     * Parsea una fecha desde String a LocalDate soportando múltiples formatos
     * 
     * @param fechaStr String con la fecha en formato ISO (con T), dd/MM/yyyy o
     *                 estándar
     * @return LocalDate parseado o null si no se puede parsear
     */
    public static LocalDate parseFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.isEmpty()) {
            return null;
        }

        try {
            // Formato ISO con timestamp (2024-01-15T10:30:00)
            if (fechaStr.contains("T")) {
                return LocalDate.parse(fechaStr.substring(0, 10));
            }
            // Formato dd/MM/yyyy
            else if (fechaStr.contains("/")) {
                return LocalDate.parse(fechaStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            // Formato estándar ISO (yyyy-MM-dd)
            else {
                return LocalDate.parse(fechaStr);
            }
        } catch (Exception e) {
            log.warn("No se pudo parsear fecha: {}", fechaStr);
            return null;
        }
    }
}

package com.lavaderosepulveda.app.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para toda la aplicación
 * Centraliza el manejo de errores que antes estaba duplicado en múltiples controladores
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja errores de parseo de fechas y horas
     */
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateTimeParseException(DateTimeParseException ex) {
        logger.warn("Error de formato de fecha/hora: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "FORMATO_FECHA_INVALIDO",
                "Error de formato de fecha u hora: " + ex.getMessage(),
                "Verifique que la fecha tenga formato dd/MM/yyyy y la hora formato HH:mm"
        );

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Maneja argumentos ilegales (como enum inválidos)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Argumento inválido: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "ARGUMENTO_INVALIDO",
                ex.getMessage(),
                "Verifique que todos los valores enviados sean correctos"
        );

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Maneja errores de runtime (lógica de negocio)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        logger.error("Error de lógica de negocio: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "ERROR_NEGOCIO",
                ex.getMessage(),
                "Error en la operación solicitada"
        );

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Maneja cualquier excepción no contemplada específicamente
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Error inesperado en la aplicación", ex);

        ErrorResponse error = new ErrorResponse(
                "ERROR_INTERNO",
                "Error interno del servidor",
                "Si el problema persiste, contacte al administrador"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Clase para estandarizar las respuestas de error
     */
    public static class ErrorResponse {
        private String codigo;
        private String mensaje;
        private String detalle;
        private long timestamp;

        public ErrorResponse(String codigo, String mensaje, String detalle) {
            this.codigo = codigo;
            this.mensaje = mensaje;
            this.detalle = detalle;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters y Setters
        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        public String getMensaje() {
            return mensaje;
        }

        public void setMensaje(String mensaje) {
            this.mensaje = mensaje;
        }

        public String getDetalle() {
            return detalle;
        }

        public void setDetalle(String detalle) {
            this.detalle = detalle;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        /**
         * Convierte la respuesta a un Map para compatibilidad con respuestas legacy
         */
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("codigo", codigo);
            map.put("mensaje", mensaje);
            map.put("detalle", detalle);
            map.put("timestamp", timestamp);
            return map;
        }
    }
}
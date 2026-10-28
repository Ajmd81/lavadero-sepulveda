package com.lavaderosepulveda.app.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manejador global de excepciones.
 *
 * Novedades:
 *  - Errores de validación (@Valid, parámetros, JSON mal formado) → 400 con el detalle por campo.
 *    Antes caían en el handler genérico y devolvían 500.
 *  - AccessDenied / Authentication → 403 / 401 (antes un @PreAuthorize denegado acababa en 500).
 *  - Errores de BD y excepciones técnicas → mensaje genérico (antes se devolvía e.getMessage(),
 *    que podía filtrar SQL, nombres de tablas, etc.).
 *
 * Todas las respuestas incluyen "error" (lo que lee el frontend) además de codigo/mensaje/detalle.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Excepciones de negocio cuyo mensaje lo escribimos nosotros y es seguro mostrar. */
    private static final Set<Class<?>> MENSAJE_SEGURO = Set.of(
            RuntimeException.class, IllegalStateException.class, IllegalArgumentException.class);

    // ─── VALIDACIÓN ───────────────────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleBodyNoValido(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(ge -> errores.putIfAbsent(ge.getObjectName(), ge.getDefaultMessage()));
        logger.debug("Validación fallida: {}", errores);
        return validacion(errores);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> handleParametrosNoValidos(HandlerMethodValidationException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getAllValidationResults().forEach(r -> r.getResolvableErrors().forEach(err ->
                errores.putIfAbsent(r.getMethodParameter().getParameterName(), err.getDefaultMessage())));
        return validacion(errores);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> errores.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
        return validacion(errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonIlegible(HttpMessageNotReadableException ex) {
        logger.debug("Body ilegible: {}", ex.getMessage());
        return respuesta(HttpStatus.BAD_REQUEST, "PETICION_INVALIDA",
                "El cuerpo de la petición no es válido (JSON mal formado o tipos incorrectos)", null);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            MissingRequestHeaderException.class})
    public ResponseEntity<Map<String, Object>> handleParametroIncorrecto(Exception ex) {
        String detalle = (ex instanceof MethodArgumentTypeMismatchException m)
                ? "Valor no válido para el parámetro '" + m.getName() + "'"
                : (ex instanceof MissingServletRequestParameterException p)
                    ? "Falta el parámetro obligatorio '" + p.getParameterName() + "'"
                    : "Falta una cabecera obligatoria";
        return respuesta(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO", detalle, null);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<Map<String, Object>> handleDateTimeParseException(DateTimeParseException ex) {
        logger.warn("Error de formato de fecha/hora: {}", ex.getParsedString());
        return respuesta(HttpStatus.BAD_REQUEST, "FORMATO_FECHA_INVALIDO",
                "Formato de fecha u hora no válido",
                "Verifique que la fecha tenga formato dd/MM/yyyy y la hora formato HH:mm");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Argumento inválido: {}", ex.getMessage());
        return respuesta(HttpStatus.BAD_REQUEST, "ARGUMENTO_INVALIDO", ex.getMessage(),
                "Verifique que todos los valores enviados sean correctos");
    }

    // ─── SEGURIDAD ────────────────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return respuesta(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", "Acceso denegado", null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        return respuesta(HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO", "No autenticado o sesión caducada", null);
    }

    // ─── HTTP ─────────────────────────────────────────────────────────────────

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMetodo(HttpRequestMethodNotSupportedException ex) {
        return respuesta(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NO_PERMITIDO", "Método HTTP no permitido", null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMediaType(HttpMediaTypeNotSupportedException ex) {
        return respuesta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "TIPO_NO_SOPORTADO", "Content-Type no soportado", null);
    }

    /**
     * Rutas o recursos estáticos inexistentes (bots buscando /.env, /.git, /wp-admin...).
     * Devuelve 404 y NO registra stacktrace para no saturar los logs de Railway.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(NoResourceFoundException ex) {
        logger.debug("Recurso no encontrado: {}", ex.getResourcePath());
        return respuesta(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", "Recurso no encontrado", "La ruta solicitada no existe");
    }

    // ─── BASE DE DATOS ────────────────────────────────────────────────────────

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegridad(DataIntegrityViolationException ex) {
        logger.warn("Violación de integridad: {}", ex.getMostSpecificCause().getMessage());
        return respuesta(HttpStatus.CONFLICT, "CONFLICTO_DATOS",
                "La operación entra en conflicto con datos existentes (duplicado o referencia en uso)", null);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccess(DataAccessException ex) {
        logger.error("Error de acceso a datos", ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Error interno del servidor", null);
    }

    // ─── NEGOCIO / GENÉRICO ───────────────────────────────────────────────────

    /**
     * Errores de lógica de negocio (los servicios lanzan RuntimeException("mensaje para el usuario")).
     * Solo se devuelve el mensaje si es una excepción "nuestra"; NPE, ClassCast, etc. → genérico.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        if (MENSAJE_SEGURO.contains(ex.getClass())) {
            logger.warn("Error de lógica de negocio: {}", ex.getMessage());
            return respuesta(HttpStatus.BAD_REQUEST, "ERROR_NEGOCIO", ex.getMessage(), "Error en la operación solicitada");
        }
        logger.error("Error inesperado", ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Error interno del servidor",
                "Si el problema persiste, contacte al administrador");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        logger.error("Error inesperado en la aplicación", ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Error interno del servidor",
                "Si el problema persiste, contacte al administrador");
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> validacion(Map<String, String> errores) {
        String primero = errores.isEmpty() ? "Datos no válidos" : errores.values().iterator().next();
        ResponseEntity<Map<String, Object>> r = respuesta(HttpStatus.BAD_REQUEST, "VALIDACION", primero,
                "Revise los campos marcados");
        r.getBody().put("errores", errores);
        return r;
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus status, String codigo, String mensaje, String detalle) {
        Map<String, Object> body = new HashMap<>();
        body.put("codigo", codigo);
        body.put("mensaje", mensaje);
        body.put("error", mensaje);
        if (detalle != null) body.put("detalle", detalle);
        body.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(status).body(body);
    }

    /**
     * Se mantiene por compatibilidad con código que construya ErrorResponse manualmente.
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

        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }
        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
        public String getDetalle() { return detalle; }
        public void setDetalle(String detalle) { this.detalle = detalle; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

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

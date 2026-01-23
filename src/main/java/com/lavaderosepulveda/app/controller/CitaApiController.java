package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.CitaDTO;
import com.lavaderosepulveda.app.mapper.CitaMapper;
import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.enums.EstadoCita;
import com.lavaderosepulveda.app.model.enums.TipoLavado;
import com.lavaderosepulveda.app.service.CitaService;
import com.lavaderosepulveda.app.service.EmailService;
import com.lavaderosepulveda.app.service.HorarioService;
import com.lavaderosepulveda.app.util.DateTimeFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * API REST refactorizada para gestión de citas
 * Usa utilities centralizadas y manejo de errores globalizado
 */
@RestController
@RequestMapping("/api")
public class CitaApiController {

    private static final Logger logger = LoggerFactory.getLogger(CitaApiController.class);

    @Autowired
    private CitaService citaService;

    @Autowired
    private HorarioService horarioService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CitaMapper citaMapper;

    /**
     * Obtener todas las citas
     */
    @GetMapping("/citas")
    public ResponseEntity<List<CitaDTO>> listarCitas() {
        List<Cita> citas = citaService.obtenerTodasLasCitas();
        List<CitaDTO> citasDTO = citas.stream()
                .map(citaMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(citasDTO);
    }

    /**
     * Obtener cita por ID
     */
    @GetMapping("/citas/{id}")
    public ResponseEntity<CitaDTO> obtenerCitaPorId(@PathVariable Long id) {
        Optional<Cita> cita = citaService.obtenerCitaPorId(id);

        if (cita.isPresent()) {
            CitaDTO citaDTO = citaMapper.toDTO(cita.get());
            return ResponseEntity.ok(citaDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Crear nueva cita - Simplificado usando CitaMapper
     */
    @PostMapping("/citas")
    public ResponseEntity<CitaDTO> crearCita(@RequestBody CitaDTO citaDTO) {
        logger.info("Recibida solicitud para crear cita: {}", citaDTO);

        // Mapeo automático usando CitaMapper (maneja validaciones)
        Cita cita = citaMapper.toEntity(citaDTO);

        // Guardar cita usando servicio
        Cita nuevaCita = citaService.crearCita(cita);
        logger.info("Cita creada exitosamente con ID: {}", nuevaCita.getId());

        // Enviar email de confirmación si está configurado y hay email
        enviarEmailConfirmacionSiEsPosible(nuevaCita);

        // Retornar DTO de respuesta
        CitaDTO respuestaDTO = citaMapper.toDTO(nuevaCita);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuestaDTO);
    }

    /**
     * Obtener horarios disponibles para una fecha - Simplificado
     */
    @GetMapping("/citas/horarios-disponibles")
    public ResponseEntity<List<String>> obtenerHorariosDisponibles(@RequestParam("fecha") String fechaStr) {
        logger.debug("Solicitando horarios disponibles para: {}", fechaStr);

        // Parsear fecha usando utility centralizada
        LocalDate fecha = DateTimeFormatUtils.parsearFechaCorta(fechaStr);

        // Obtener horarios usando servicio especializado
        List<LocalTime> horariosDisponibles = horarioService.obtenerHorariosDisponibles(fecha);

        // Aplicar filtro específico para API (excluir 15:00)
        List<LocalTime> horariosFiltrados = horariosDisponibles.stream()
                .filter(hora -> hora.getHour() != 15)
                .collect(Collectors.toList());

        // Convertir a strings usando utility
        List<String> horariosFormateados = horariosFiltrados.stream()
                .map(DateTimeFormatUtils::formatearHoraCorta)
                .collect(Collectors.toList());

        logger.debug("Horarios disponibles enviados: {}", horariosFormateados);
        return ResponseEntity.ok(horariosFormateados);
    }

    /**
     * Obtener días NO disponibles para un mes y servicio específico
     */
    @GetMapping("/citas/disponibilidad-mensual")
    public ResponseEntity<List<String>> obtenerDisponibilidadMensual(
            @RequestParam("mes") int mes,
            @RequestParam("anio") int anio,
            @RequestParam("tipoLavado") String tipoLavadoStr) {

        try {
            YearMonth yearMonth = YearMonth.of(anio, mes);
            TipoLavado tipoLavado = TipoLavado.valueOf(tipoLavadoStr);

            List<String> diasNoDisponibles = horarioService.obtenerDiasNoDisponibles(yearMonth, tipoLavado);
            return ResponseEntity.ok(diasNoDisponibles);
        } catch (Exception e) {
            logger.error("Error obteniendo disponibilidad mensual: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/tipos-lavado")
    public ResponseEntity<List<Map<String, Object>>> obtenerTiposLavado() {
        List<Map<String, Object>> tiposLavado = Arrays.stream(TipoLavado.values())
                .map(tipo -> {
                    Map<String, Object> tipoMap = new HashMap<>();
                    tipoMap.put("id", tipo.name());
                    tipoMap.put("nombre", tipo.name());
                    tipoMap.put("descripcion", tipo.getDescripcion());
                    tipoMap.put("precio", tipo.getPrecio());
                    return tipoMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(tiposLavado);
    }

    /**
     * Eliminar cita por ID
     */
    @DeleteMapping("/citas/{id}")
    public ResponseEntity<Void> eliminarCita(@PathVariable Long id) {
        citaService.eliminarCita(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verificar disponibilidad de un horario específico
     */
    @GetMapping("/citas/verificar-disponibilidad")
    public ResponseEntity<Boolean> verificarDisponibilidad(
            @RequestParam("fecha") String fechaStr,
            @RequestParam("hora") String horaStr) {

        LocalDate fecha = DateTimeFormatUtils.parsearFechaCorta(fechaStr);
        LocalTime hora = DateTimeFormatUtils.parsearHoraCorta(horaStr);
        boolean disponible = horarioService.esHorarioDisponible(fecha, hora);

        return ResponseEntity.ok(!disponible); // ← Devolver solo el boolean
    }

    /**
     * Obtener citas agrupadas por fecha - Mejorado
     */
    @GetMapping("/citas/por-fecha")
    public ResponseEntity<Map<String, List<CitaDTO>>> obtenerCitasPorFecha() {
        // Obtener citas agrupadas desde el servicio (ya formateadas)
        Map<String, List<Cita>> citasPorFecha = citaService.obtenerCitasAgrupadasPorFechaFormateada();

        // Convertir entidades a DTOs
        Map<String, List<CitaDTO>> citasPorFechaDTO = new LinkedHashMap<>();
        citasPorFecha.forEach((fecha, citas) -> {
            List<CitaDTO> citasDTO = citas.stream()
                    .map(citaMapper::toDTO)
                    .collect(Collectors.toList());
            citasPorFechaDTO.put(fecha, citasDTO);
        });

        return ResponseEntity.ok(citasPorFechaDTO);
    }

    /**
     * Actualizar cita existente
     */
    @PutMapping("/citas/{id}")
    public ResponseEntity<CitaDTO> actualizarCita(@PathVariable Long id, @RequestBody CitaDTO citaDTO) {
        logger.info("Actualizando cita ID: {} con datos: {}", id, citaDTO);

        // Mapear DTO a entidad
        Cita citaActualizada = citaMapper.toEntity(citaDTO);

        // Actualizar usando servicio
        Cita cita = citaService.actualizarCita(id, citaActualizada);

        // Retornar DTO actualizado
        CitaDTO respuestaDTO = citaMapper.toDTO(cita);
        return ResponseEntity.ok(respuestaDTO);
    }

    /**
     * Obtener estadísticas de ocupación para una fecha
     */
    @GetMapping("/citas/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(@RequestParam("fecha") String fechaStr) {
        LocalDate fecha = DateTimeFormatUtils.parsearFechaCorta(fechaStr);
        Map<String, Object> estadisticas = horarioService.obtenerEstadisticasOcupacion(fecha);

        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Obtener citas por teléfono (historial del cliente)
     */
    @GetMapping("/citas/cliente/{telefono}")
    public ResponseEntity<List<CitaDTO>> obtenerCitasPorTelefono(@PathVariable String telefono) {
        List<Cita> citas = citaService.obtenerCitasPorTelefono(telefono);
        List<CitaDTO> citasDTO = citas.stream()
                .map(citaMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(citasDTO);
    }

    /**
     * POST /api/citas/migrar-email
     * Permitir NULL en columna email
     */
    @PostMapping("/citas/migrar-email")
    public ResponseEntity<Map<String, String>> migrarColumnaEmail() {
        try (java.sql.Connection connection = dataSource.getConnection();
             java.sql.Statement statement = connection.createStatement()) {

            // Cambiar la columna para permitir NULL
            String sql = "ALTER TABLE citas MODIFY COLUMN email VARCHAR(255) NULL";
            statement.executeUpdate(sql);

            logger.info("Migración de columna email completada exitosamente");

            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Migración completada");
            response.put("detalle", "Columna 'email' ahora permite NULL");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error en migración de columna email: {}", e.getMessage());

            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================
    // NUEVOS ENDPOINTS PARA INTEGRACIÓN CRM
    // ========================================

    /**
     * GET /api/citas/fecha/{fecha}
     * Obtener citas por fecha específica
     */
    @GetMapping("/citas/fecha/{fecha}")
    public ResponseEntity<List<CitaDTO>> obtenerCitasPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        logger.info("Obteniendo citas para fecha: {}", fecha);
        List<Cita> citas = citaService.obtenerCitasPorFecha(fecha);
        List<CitaDTO> citasDTO = citas.stream()
                .map(citaMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(citasDTO);
    }

    /**
     * GET /api/citas/rango
     * Obtener citas por rango de fechas
     */
    @GetMapping("/citas/rango")
    public ResponseEntity<List<CitaDTO>> obtenerCitasPorRango(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        logger.info("Obteniendo citas desde {} hasta {}", inicio, fin);
        List<Cita> citas = citaService.obtenerCitasEnRango(inicio, fin);
        List<CitaDTO> citasDTO = citas.stream()
                .map(citaMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(citasDTO);
    }

    /**
     * GET /api/citas/estado/{estado}
     * Obtener citas por estado
     */
    @GetMapping("/citas/estado/{estado}")
    public ResponseEntity<List<CitaDTO>> obtenerCitasPorEstado(@PathVariable String estado) {
        try {
            EstadoCita estadoCita = EstadoCita.valueOf(estado.toUpperCase());
            List<Cita> citas = citaService.obtenerCitasPorEstado(estadoCita);
            List<CitaDTO> citasDTO = citas.stream()
                    .map(citaMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(citasDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Estado no válido: {}", estado);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/citas/pendientes
     * Obtener citas pendientes (PENDIENTE o CONFIRMADA)
     */
    @GetMapping("/citas/pendientes")
    public ResponseEntity<List<CitaDTO>> obtenerCitasPendientes() {
        List<Cita> citas = citaService.obtenerCitasPendientes();
        List<CitaDTO> citasDTO = citas.stream()
                .map(citaMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(citasDTO);
    }

    /**
     * GET /api/citas/no-facturadas
     * Obtener citas completadas sin facturar
     */
    @GetMapping("/citas/no-facturadas")
    public ResponseEntity<List<CitaDTO>> obtenerCitasNoFacturadas() {
        List<Cita> citas = citaService.obtenerCitasCompletadasSinFacturar();
        List<CitaDTO> citasDTO = citas.stream()
                .map(citaMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(citasDTO);
    }

    /**
     * GET /api/citas/hoy
     * Obtener citas de hoy
     */
    @GetMapping("/citas/hoy")
    public ResponseEntity<List<CitaDTO>> obtenerCitasHoy() {
        List<Cita> citas = citaService.obtenerCitasDeHoy();
        List<CitaDTO> citasDTO = citas.stream()
                .map(citaMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(citasDTO);
    }

    /**
     * GET /api/citas/en-proceso
     * Obtener citas en proceso
     */
    @GetMapping("/citas/en-proceso")
    public ResponseEntity<List<CitaDTO>> obtenerCitasEnProceso() {
        List<Cita> citas = citaService.obtenerCitasEnProceso();
        List<CitaDTO> citasDTO = citas.stream()
                .map(citaMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(citasDTO);
    }

    /**
     * GET /api/citas/cliente-id/{clienteId}
     * Obtener citas por ID de cliente
     */
    @GetMapping("/citas/cliente-id/{clienteId}")
    public ResponseEntity<List<CitaDTO>> obtenerCitasPorClienteId(@PathVariable Long clienteId) {
        List<Cita> citas = citaService.obtenerCitasPorClienteId(clienteId);
        List<CitaDTO> citasDTO = citas.stream()
                .map(citaMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(citasDTO);
    }

    // ========================================
    // ENDPOINTS DE CAMBIO DE ESTADO
    // ========================================

    /**
     * PUT /api/citas/{id}/estado/{estado}
     * Cambiar estado de una cita
     */
    @PutMapping("/citas/{id}/estado/{estado}")
    public ResponseEntity<CitaDTO> cambiarEstadoCita(
            @PathVariable Long id,
            @PathVariable String estado) {
        try {
            EstadoCita nuevoEstado = EstadoCita.valueOf(estado.toUpperCase());
            Cita cita = citaService.cambiarEstado(id, nuevoEstado);
            CitaDTO citaDTO = citaMapper.toDTO(cita);
            logger.info("Estado de cita {} cambiado a {}", id, estado);
            return ResponseEntity.ok(citaDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Estado no válido: {}", estado);
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            logger.error("Error al cambiar estado de cita {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/citas/{id}/cancelar
     * Cancelar una cita
     */
    @PostMapping("/citas/{id}/cancelar")
    public ResponseEntity<CitaDTO> cancelarCita(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String motivo = body != null ? body.get("motivo") : null;
            Cita cita = citaService.cancelarCita(id, motivo);
            CitaDTO citaDTO = citaMapper.toDTO(cita);
            logger.info("Cita {} cancelada", id);
            return ResponseEntity.ok(citaDTO);
        } catch (RuntimeException e) {
            logger.error("Error al cancelar cita {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * POST /api/citas/{id}/confirmar
     * Confirmar una cita
     */
    @PostMapping("/citas/{id}/confirmar")
    public ResponseEntity<CitaDTO> confirmarCita(@PathVariable Long id) {
        try {
            Cita cita = citaService.confirmarCita(id);
            CitaDTO citaDTO = citaMapper.toDTO(cita);
            return ResponseEntity.ok(citaDTO);
        } catch (RuntimeException e) {
            logger.error("Error al confirmar cita {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/citas/{id}/iniciar
     * Iniciar servicio (marcar en proceso)
     */
    @PostMapping("/citas/{id}/iniciar")
    public ResponseEntity<CitaDTO> iniciarServicio(@PathVariable Long id) {
        try {
            Cita cita = citaService.iniciarServicio(id);
            CitaDTO citaDTO = citaMapper.toDTO(cita);
            return ResponseEntity.ok(citaDTO);
        } catch (RuntimeException e) {
            logger.error("Error al iniciar servicio de cita {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/citas/{id}/completar
     * Completar una cita
     */
    @PostMapping("/citas/{id}/completar")
    public ResponseEntity<CitaDTO> completarCita(@PathVariable Long id) {
        try {
            Cita cita = citaService.completarCita(id);
            CitaDTO citaDTO = citaMapper.toDTO(cita);
            return ResponseEntity.ok(citaDTO);
        } catch (RuntimeException e) {
            logger.error("Error al completar cita {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/citas/{id}/no-presentado
     * Marcar como no presentado
     */
    @PostMapping("/citas/{id}/no-presentado")
    public ResponseEntity<CitaDTO> marcarNoPresentado(@PathVariable Long id) {
        try {
            Cita cita = citaService.marcarNoPresentado(id);
            CitaDTO citaDTO = citaMapper.toDTO(cita);
            return ResponseEntity.ok(citaDTO);
        } catch (RuntimeException e) {
            logger.error("Error al marcar no presentado cita {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/citas/{id}/llegada
     * Registrar llegada del cliente
     */
    @PostMapping("/citas/{id}/llegada")
    public ResponseEntity<CitaDTO> registrarLlegada(@PathVariable Long id) {
        try {
            Cita cita = citaService.registrarLlegada(id);
            CitaDTO citaDTO = citaMapper.toDTO(cita);
            return ResponseEntity.ok(citaDTO);
        } catch (RuntimeException e) {
            logger.error("Error al registrar llegada de cita {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/citas/{id}/facturar
     * Marcar cita como facturada
     */
    @PostMapping("/citas/{id}/facturar")
    public ResponseEntity<CitaDTO> marcarComoFacturada(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        try {
            Long facturaId = body.get("facturaId");
            Cita cita = citaService.marcarComoFacturada(id, facturaId);
            CitaDTO citaDTO = citaMapper.toDTO(cita);
            return ResponseEntity.ok(citaDTO);
        } catch (RuntimeException e) {
            logger.error("Error al marcar como facturada cita {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================
    // ENDPOINTS DE CONTEO (DASHBOARD)
    // ========================================

    /**
     * GET /api/citas/count/hoy
     * Contar citas de hoy
     */
    @GetMapping("/citas/count/hoy")
    public ResponseEntity<Map<String, Long>> contarCitasHoy() {
        long total = citaService.contarCitasHoy();
        return ResponseEntity.ok(Map.of("total", total));
    }

    /**
     * GET /api/citas/count/estado/{estado}
     * Contar citas por estado
     */
    @GetMapping("/citas/count/estado/{estado}")
    public ResponseEntity<Map<String, Long>> contarCitasPorEstado(@PathVariable String estado) {
        try {
            EstadoCita estadoCita = EstadoCita.valueOf(estado.toUpperCase());
            long count = citaService.contarCitasPorEstado(estadoCita);
            return ResponseEntity.ok(Map.of("count", count, "estado", (long) estadoCita.ordinal()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/citas/resumen/hoy
     * Resumen de citas de hoy por estado
     */
    @GetMapping("/citas/resumen/hoy")
    public ResponseEntity<Map<String, Object>> obtenerResumenHoy() {
        Map<String, Object> resumen = citaService.obtenerResumenCitasHoy();
        return ResponseEntity.ok(resumen);
    }

    // ========================================
    // ENDPOINT DE MIGRACIÓN
    // ========================================

    @Autowired
    private javax.sql.DataSource dataSource;

    /**
     * POST /api/citas/migrar-estado
     * Migrar columna estado de ENUM a VARCHAR para permitir nuevos valores
     */
    @PostMapping("/citas/migrar-estado")
    public ResponseEntity<Map<String, String>> migrarColumnaEstado() {
        try (java.sql.Connection connection = dataSource.getConnection();
                java.sql.Statement statement = connection.createStatement()) {

            // Cambiar la columna de ENUM a VARCHAR(20)
            String sql = "ALTER TABLE citas MODIFY COLUMN estado VARCHAR(20)";
            statement.executeUpdate(sql);

            logger.info("Migración de columna estado completada exitosamente");

            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Migración completada");
            response.put("detalle", "Columna 'estado' cambiada a VARCHAR(20)");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error en migración de columna estado: {}", e.getMessage());

            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Método privado para enviar email de confirmación
     */
    private void enviarEmailConfirmacionSiEsPosible(Cita cita) {
        if (emailService != null && cita.getEmail() != null && !cita.getEmail().trim().isEmpty()) {
            try {
                emailService.enviarEmailConfirmacion(cita);
                logger.info("Email de confirmación enviado a: {}", cita.getEmail());
            } catch (Exception emailError) {
                // Log del error pero no afecta la creación de la cita
                logger.warn("Error al enviar email de confirmación a {}: {}",
                        cita.getEmail(), emailError.getMessage());
            }
        }
    }
}
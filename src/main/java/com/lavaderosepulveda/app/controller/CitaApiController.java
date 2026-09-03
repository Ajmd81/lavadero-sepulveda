package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.CitaDTO;
import com.lavaderosepulveda.app.mapper.CitaMapper;
import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.HorarioDiaSemana;
import com.lavaderosepulveda.app.model.enums.EstadoCita;
import com.lavaderosepulveda.app.model.enums.TipoLavado;
import com.lavaderosepulveda.app.security.CitaRateLimiter;
import com.lavaderosepulveda.app.repository.HorarioDiaSemanaRepository;
import com.lavaderosepulveda.app.service.CitaService;
import com.lavaderosepulveda.app.service.EmailService;
import com.lavaderosepulveda.app.service.HorarioService;
import com.lavaderosepulveda.app.util.DateTimeFormatUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CitaApiController {

    private static final Logger logger = LoggerFactory.getLogger(CitaApiController.class);

    @Autowired private CitaService citaService;
    @Autowired private HorarioService horarioService;
    @Autowired private EmailService emailService;
    @Autowired private CitaMapper citaMapper;
    @Autowired private CitaRateLimiter citaRateLimiter;    // ← NUEVO
    @Autowired private javax.sql.DataSource dataSource;
    @Autowired private HorarioDiaSemanaRepository horarioDiaSemanaRepository;

    // ─── LISTAR CITAS ─────────────────────────────────────────────────────────

    @GetMapping("/citas")
    public ResponseEntity<List<CitaDTO>> listarCitas() {
        List<CitaDTO> citasDTO = citaService.obtenerTodasLasCitas().stream()
                .map(citaMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(citasDTO);
    }

    @GetMapping("/citas/paginado")
    public ResponseEntity<Page<CitaDTO>> listarCitasPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fecha") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("asc")
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<CitaDTO> citasDTOPage = citaService.obtenerCitasPaginadas(pageable).map(citaMapper::toDTO);
            return ResponseEntity.ok(citasDTOPage);
        } catch (Exception e) {
            logger.error("Error en paginación de citas: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/citas/{id}")
    public ResponseEntity<CitaDTO> obtenerCitaPorId(@PathVariable Long id) {
        return citaService.obtenerCitaPorId(id)
                .map(cita -> ResponseEntity.ok(citaMapper.toDTO(cita)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── CREAR CITA (API pública — usada por la app móvil) ───────────────────

    @PostMapping("/citas")
    public ResponseEntity<?> crearCita(
            @Valid @RequestBody CitaDTO citaDTO,
            HttpServletRequest httpRequest) {

        // ① Rate limiting por IP
        String ip = obtenerIpReal(httpRequest);
        if (!citaRateLimiter.intentoPermitido(ip)) {
            long espera = citaRateLimiter.segundosHastaReset(ip);
            logger.warn("Rate limit superado en POST /api/citas desde IP: {}", ip);
            return ResponseEntity.status(429).body(Map.of(
                    "error", "Demasiadas solicitudes. Espera " + Math.max(1, espera / 60) + " minuto(s).",
                    "retryAfter", espera
            ));
        }

        // ② Validación de rango de fecha
        LocalDate hoy = LocalDate.now();
        if (citaDTO.getFecha() != null) {
            if (citaDTO.getFecha().isBefore(hoy)) {
                return ResponseEntity.badRequest().body(Map.of("error", "La fecha no puede ser pasada."));
            }
            if (citaDTO.getFecha().isAfter(hoy.plusDays(60))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Máximo 60 días de antelación."));
            }
        }

        logger.info("Recibida solicitud para crear cita: {}", citaDTO);
        Cita cita = citaMapper.toEntity(citaDTO);
        Cita nuevaCita = citaService.crearCita(cita);
        logger.info("Cita creada exitosamente con ID: {}", nuevaCita.getId());

        enviarEmailConfirmacionSiEsPosible(nuevaCita);

        return ResponseEntity.status(HttpStatus.CREATED).body(citaMapper.toDTO(nuevaCita));
    }

    // ─── HORARIOS ─────────────────────────────────────────────────────────────

    @GetMapping("/citas/horarios-disponibles")
    public ResponseEntity<List<String>> obtenerHorariosDisponibles(@RequestParam("fecha") String fechaStr) {
        LocalDate fecha = DateTimeFormatUtils.parsearFechaCorta(fechaStr);
        List<String> horariosFormateados = horarioService.obtenerHorariosDisponibles(fecha).stream()
                .filter(hora -> hora.getHour() != 15)
                .map(DateTimeFormatUtils::formatearHoraCorta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(horariosFormateados);
    }

    @GetMapping("/horarios")
    public ResponseEntity<List<HorarioDiaSemana>> obtenerHorariosDiaSemana() {
        try {
            List<HorarioDiaSemana> horarios = horarioDiaSemanaRepository.findAllByOrderByDiaSemanaAsc();
            return ResponseEntity.ok(horarios);
        } catch (Exception e) {
            logger.error("Error obteniendo horarios por día: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/citas/disponibilidad-mensual")
    public ResponseEntity<List<String>> obtenerDisponibilidadMensual(
            @RequestParam("mes") int mes,
            @RequestParam("anio") int anio,
            @RequestParam("tipoLavado") String tipoLavadoStr) {
        try {
            YearMonth yearMonth = YearMonth.of(anio, mes);
            TipoLavado tipoLavado = TipoLavado.valueOf(tipoLavadoStr);
            return ResponseEntity.ok(horarioService.obtenerDiasNoDisponibles(yearMonth, tipoLavado));
        } catch (Exception e) {
            logger.error("Error disponibilidad mensual: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/citas/verificar-disponibilidad")
    public ResponseEntity<Boolean> verificarDisponibilidad(
            @RequestParam("fecha") String fechaStr,
            @RequestParam("hora") String horaStr) {
        LocalDate fecha = DateTimeFormatUtils.parsearFechaCorta(fechaStr);
        LocalTime hora = DateTimeFormatUtils.parsearHoraCorta(horaStr);
        return ResponseEntity.ok(!horarioService.esHorarioDisponible(fecha, hora));
    }

    // ─── TIPOS DE LAVADO ──────────────────────────────────────────────────────

    @GetMapping("/tipos-lavado")
    public ResponseEntity<List<Map<String, Object>>> obtenerTiposLavado() {
        List<Map<String, Object>> tipos = Arrays.stream(TipoLavado.values())
                .map(tipo -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", tipo.name());
                    m.put("nombre", tipo.name());
                    m.put("descripcion", tipo.getDescripcion());
                    m.put("precio", tipo.getPrecio());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(tipos);
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @DeleteMapping("/citas/{id}")
    public ResponseEntity<Void> eliminarCita(@PathVariable Long id) {
        citaService.eliminarCita(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/citas/{id}")
    public ResponseEntity<CitaDTO> actualizarCita(@PathVariable Long id, @RequestBody CitaDTO citaDTO) {
        Cita cita = citaService.actualizarCita(id, citaMapper.toEntity(citaDTO));
        return ResponseEntity.ok(citaMapper.toDTO(cita));
    }

    // ─── CONSULTAS ADICIONALES ────────────────────────────────────────────────

    @GetMapping("/citas/por-fecha")
    public ResponseEntity<Map<String, List<CitaDTO>>> obtenerCitasPorFechaAgrupadas() {
        Map<String, List<CitaDTO>> result = new LinkedHashMap<>();
        citaService.obtenerCitasAgrupadasPorFechaFormateada().forEach((fecha, citas) ->
                result.put(fecha, citas.stream().map(citaMapper::toDTO).collect(Collectors.toList())));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/citas/cliente/{telefono}")
    public ResponseEntity<List<CitaDTO>> obtenerCitasPorTelefono(@PathVariable String telefono) {
        try {
            List<Cita> citas = citaService.obtenerCitasPorTelefono(telefono);
            if (citas == null || citas.isEmpty()) return ResponseEntity.ok(Collections.emptyList());
            return ResponseEntity.ok(citas.stream().map(citaMapper::toDTO).collect(Collectors.toList()));
        } catch (Exception e) {
            logger.error("Error citas por teléfono {}: {}", telefono, e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/citas/fecha/{fecha}")
    public ResponseEntity<List<CitaDTO>> obtenerCitasPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(citaService.obtenerCitasPorFecha(fecha).stream()
                .map(citaMapper::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/citas/rango")
    public ResponseEntity<List<CitaDTO>> obtenerCitasPorRango(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(citaService.obtenerCitasEnRango(inicio, fin).stream()
                .map(citaMapper::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/citas/estado/{estado}")
    public ResponseEntity<List<CitaDTO>> obtenerCitasPorEstado(@PathVariable String estado) {
        try {
            EstadoCita e = EstadoCita.valueOf(estado.toUpperCase());
            return ResponseEntity.ok(citaService.obtenerCitasPorEstado(e).stream()
                    .map(citaMapper::toDTO).collect(Collectors.toList()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/citas/pendientes")
    public ResponseEntity<List<CitaDTO>> obtenerCitasPendientes() {
        return ResponseEntity.ok(citaService.obtenerCitasPendientes().stream()
                .map(citaMapper::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/citas/no-facturadas")
    public ResponseEntity<List<CitaDTO>> obtenerCitasNoFacturadas() {
        return ResponseEntity.ok(citaService.obtenerCitasCompletadasSinFacturar().stream()
                .map(citaMapper::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/citas/hoy")
    public ResponseEntity<List<CitaDTO>> obtenerCitasHoy() {
        return ResponseEntity.ok(citaService.obtenerCitasDeHoy().stream()
                .map(citaMapper::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/citas/en-proceso")
    public ResponseEntity<List<CitaDTO>> obtenerCitasEnProceso() {
        return ResponseEntity.ok(citaService.obtenerCitasEnProceso().stream()
                .map(citaMapper::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/citas/cliente-id/{clienteId}")
    public ResponseEntity<List<CitaDTO>> obtenerCitasPorClienteId(@PathVariable Long clienteId) {
        return ResponseEntity.ok(citaService.obtenerCitasPorClienteId(clienteId).stream()
                .map(citaMapper::toDTO).collect(Collectors.toList()));
    }

    // ─── CAMBIOS DE ESTADO ────────────────────────────────────────────────────

    @PutMapping("/citas/{id}/estado/{estado}")
    public ResponseEntity<CitaDTO> cambiarEstadoCita(@PathVariable Long id, @PathVariable String estado) {
        try {
            Cita cita = citaService.cambiarEstado(id, EstadoCita.valueOf(estado.toUpperCase()));
            return ResponseEntity.ok(citaMapper.toDTO(cita));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/citas/{id}/cancelar")
    public ResponseEntity<CitaDTO> cancelarCita(@PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            Cita cita = citaService.cancelarCita(id, body != null ? body.get("motivo") : null);
            return ResponseEntity.ok(citaMapper.toDTO(cita));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/citas/{id}/confirmar")
    public ResponseEntity<CitaDTO> confirmarCita(@PathVariable Long id) {
        try { return ResponseEntity.ok(citaMapper.toDTO(citaService.confirmarCita(id))); }
        catch (RuntimeException e) { return ResponseEntity.notFound().build(); }
    }

    @PostMapping("/citas/{id}/iniciar")
    public ResponseEntity<CitaDTO> iniciarServicio(@PathVariable Long id) {
        try { return ResponseEntity.ok(citaMapper.toDTO(citaService.iniciarServicio(id))); }
        catch (RuntimeException e) { return ResponseEntity.notFound().build(); }
    }

    @PostMapping("/citas/{id}/completar")
    public ResponseEntity<CitaDTO> completarCita(@PathVariable Long id) {
        try { return ResponseEntity.ok(citaMapper.toDTO(citaService.completarCita(id))); }
        catch (RuntimeException e) { return ResponseEntity.notFound().build(); }
    }

    @PostMapping("/citas/{id}/no-presentado")
    public ResponseEntity<CitaDTO> marcarNoPresentado(@PathVariable Long id) {
        try { return ResponseEntity.ok(citaMapper.toDTO(citaService.marcarNoPresentado(id))); }
        catch (RuntimeException e) { return ResponseEntity.notFound().build(); }
    }

    @PostMapping("/citas/{id}/llegada")
    public ResponseEntity<CitaDTO> registrarLlegada(@PathVariable Long id) {
        try { return ResponseEntity.ok(citaMapper.toDTO(citaService.registrarLlegada(id))); }
        catch (RuntimeException e) { return ResponseEntity.notFound().build(); }
    }

    @PostMapping("/citas/{id}/facturar")
    public ResponseEntity<CitaDTO> marcarComoFacturada(@PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        try {
            Cita cita = citaService.marcarComoFacturada(id, body.get("facturaId"));
            return ResponseEntity.ok(citaMapper.toDTO(cita));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ─── DASHBOARD / CONTEO ───────────────────────────────────────────────────

    @GetMapping("/citas/count/hoy")
    public ResponseEntity<Map<String, Long>> contarCitasHoy() {
        return ResponseEntity.ok(Map.of("total", citaService.contarCitasHoy()));
    }

    @GetMapping("/citas/count/estado/{estado}")
    public ResponseEntity<Map<String, Long>> contarCitasPorEstado(@PathVariable String estado) {
        try {
            EstadoCita e = EstadoCita.valueOf(estado.toUpperCase());
            return ResponseEntity.ok(Map.of("count", citaService.contarCitasPorEstado(e),
                    "estado", (long) e.ordinal()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/citas/resumen/hoy")
    public ResponseEntity<Map<String, Object>> obtenerResumenHoy() {
        return ResponseEntity.ok(citaService.obtenerResumenCitasHoy());
    }

    @GetMapping("/citas/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(@RequestParam("fecha") String fechaStr) {
        return ResponseEntity.ok(horarioService.obtenerEstadisticasOcupacion(
                DateTimeFormatUtils.parsearFechaCorta(fechaStr)));
    }

    @GetMapping("/horarios-configurados")
    public ResponseEntity<?> obtenerHorariosConfigurados() {
        try {
            LocalDate fecha = LocalDate.now();
            while (fecha.getDayOfWeek() == DayOfWeek.SATURDAY || fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
                fecha = fecha.plusDays(1);
            }
            List<String> horarios = horarioService.generarHorariosPorDia(fecha).stream()
                    .map(DateTimeFormatUtils::formatearHoraCorta)
                    .sorted()
                    .collect(Collectors.toList());
            return ResponseEntity.ok(horarios);
        } catch (Exception e) {
            logger.error("Error horarios configurados: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ─── MIGRACIONES (solo para uso puntual, no exponer en producción) ────────

    @PostMapping("/citas/migrar-email")
    public ResponseEntity<Map<String, String>> migrarColumnaEmail() {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE citas MODIFY COLUMN email VARCHAR(255) NULL");
            return ResponseEntity.ok(Map.of("mensaje", "Migración completada",
                    "detalle", "Columna 'email' ahora permite NULL"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/citas/migrar-estado")
    public ResponseEntity<Map<String, String>> migrarColumnaEstado() {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE citas MODIFY COLUMN estado VARCHAR(20)");
            return ResponseEntity.ok(Map.of("mensaje", "Migración completada",
                    "detalle", "Columna 'estado' cambiada a VARCHAR(20)"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ─── HELPERS PRIVADOS ─────────────────────────────────────────────────────

    private void enviarEmailConfirmacionSiEsPosible(Cita cita) {
        if (emailService != null && cita.getEmail() != null && !cita.getEmail().trim().isEmpty()) {
            try {
                emailService.enviarEmailConfirmacion(cita.getId());
            } catch (Exception e) {
                logger.warn("Error email confirmación {}: {}", cita.getEmail(), e.getMessage());
            }
        }
    }

    private String obtenerIpReal(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
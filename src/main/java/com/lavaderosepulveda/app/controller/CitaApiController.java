package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.CitaDTO;
import com.lavaderosepulveda.app.mapper.CitaMapper;
import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.HorarioDiaSemana;
import com.lavaderosepulveda.app.model.enums.DiaSemana;
import com.lavaderosepulveda.app.model.enums.EstadoCita;
import com.lavaderosepulveda.app.model.enums.TipoLavado;
import com.lavaderosepulveda.app.security.CitaRateLimiter;
import com.lavaderosepulveda.app.repository.HorarioDiaSemanaRepository;
import com.lavaderosepulveda.app.repository.DiaCerradoRepository;
import com.lavaderosepulveda.app.service.CitaService;
import com.lavaderosepulveda.app.service.EmailService;
import com.lavaderosepulveda.app.service.HorarioService;
import com.lavaderosepulveda.app.service.HorarioDiaSemanaService;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
    @Autowired private CitaRateLimiter citaRateLimiter;
    @Autowired private javax.sql.DataSource dataSource;
    @Autowired private HorarioDiaSemanaRepository horarioDiaSemanaRepository;
    @Autowired private HorarioDiaSemanaService horarioDiaSemanaService;
    @Autowired private DiaCerradoRepository diasCerradoRepository;

    // ═══════════════════════════════════════════════════════════════════════════════
    // ✅ ENDPOINTS ESPECÍFICOS - PRIMERO (Todos menos {id})
    // ═══════════════════════════════════════════════════════════════════════════════

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

    @GetMapping("/citas/horarios-disponibles")
    public ResponseEntity<List<String>> obtenerHorariosDisponibles(@RequestParam("fecha") String fechaStr) {
        LocalDate fecha = DateTimeFormatUtils.parsearFechaCorta(fechaStr);
        List<String> horariosFormateados = horarioService.obtenerHorariosDisponibles(fecha).stream()
                .filter(hora -> hora.getHour() != 15)
                .map(DateTimeFormatUtils::formatearHoraCorta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(horariosFormateados);
    }

    @GetMapping("/citas/disponibilidad-mes")
    public ResponseEntity<List<String>> obtenerDisponibilidadMensual(
            @RequestParam("mes") int mes,
            @RequestParam("anio") int anio,
            @RequestParam(value = "tipoLavado", required = false) String tipoLavadoStr) {
        try {
            YearMonth yearMonth = YearMonth.of(anio, mes);
            
            // Si tipoLavado no viene, pasar null
            TipoLavado tipoLavado = null;
            if (tipoLavadoStr != null && !tipoLavadoStr.isEmpty()) {
                try {
                    tipoLavado = TipoLavado.valueOf(tipoLavadoStr);
                } catch (IllegalArgumentException e) {
                    tipoLavado = null;
                }
            }
            
            List<String> diasNoDisponibles = horarioService.obtenerDiasNoDisponibles(yearMonth, tipoLavado);
            return ResponseEntity.ok(diasNoDisponibles);
        } catch (Exception e) {
            logger.error("Error disponibilidad mensual: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(List.of());
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

    @GetMapping("/tipos-lavado")
    public ResponseEntity<List<Map<String, Object>>> obtenerTiposLavado() {
        List<Map<String, Object>> tipos = Arrays.stream(TipoLavado.values())
                .map(tipo -> {
                    Map<String, Object> tipoMap = new HashMap<>();
                    tipoMap.put("id", tipo.ordinal());
                    tipoMap.put("nombre", tipo.getName());
                    tipoMap.put("label", tipo.getLabel());
                    tipoMap.put("descripcion", tipo.getDescripcion());
                    tipoMap.put("precio", tipo.getPrecio());
                    tipoMap.put("duracion", tipo.getDuracion());
                    return tipoMap;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(tipos);
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

    // ─── HORARIOS ────────────────────────────────────────────────────────────

    @GetMapping("/horarios")
    public ResponseEntity<List<String>> obtenerHorariosConCapacidad(
            @RequestParam(value = "fecha", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            if (fecha == null) {
                fecha = LocalDate.now();
            }
            
            List<String> resultado = new ArrayList<>();
            
            // Obtener horarios base del día (sin duplicar)
            List<LocalTime> horariosDelDia = horarioService.obtenerHorariosDisponibles(fecha);
            if (horariosDelDia == null || horariosDelDia.isEmpty()) {
                logger.info("No hay horarios para la fecha: {}", fecha);
                return ResponseEntity.ok(List.of());
            }
            
            // Repetir cada horario según su capacidad y disponibilidad
            for (LocalTime hora : horariosDelDia) {
                if (hora == null) continue;
                
                int capacidadMax = calcularCapacidad(fecha, hora.getHour());
                for (int i = 0; i < capacidadMax; i++) {
                    resultado.add(String.format("%d:00", hora.getHour()));
                }
                
                logger.debug("Hora {} - Capacidad: {}", hora.getHour(), capacidadMax);
            }
            
            logger.info("[UPDATED] Horarios disponibles para {}: {} slots", fecha, resultado.size());
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            logger.error("ERROR /api/horarios {}: {}", fecha, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }
    
    /**
     * Calcula la capacidad máxima según la hora y día de la semana
     * L-V: 8 (1), 9-13 (2), 14 (1)
     * Sábado: 9-13 (1)
     * Domingo: Cerrado (no aplica)
     */
    private int calcularCapacidad(LocalDate fecha, int hora) {
        DayOfWeek dayOfWeek = fecha.getDayOfWeek();
        
        // Sábado: 1 cita/hora (9-13)
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return 1;
        }
        
        // L-V: 8 (1), 9-13 (2), 14 (1)
        if (hora == 8 || hora == 14) {
            return 1;
        } else if (hora >= 9 && hora <= 13) {
            return 2;
        }
        return 1; // Por defecto
    }

    @GetMapping("/horarios-admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<HorarioDiaSemana>> obtenerHorariosDiaSemanaAdmin() {
        try {
            logger.info("GET /api/horarios-admin - Obteniendo todos los horarios");
            List<HorarioDiaSemana> horarios = horarioDiaSemanaRepository.findAllByOrderByDiaSemanaAsc();
            return ResponseEntity.ok(horarios);
        } catch (Exception e) {
            logger.error("Error obteniendo horarios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/horarios-admin/{diaSemana}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<HorarioDiaSemana> obtenerHorarioPorDia(
            @PathVariable DiaSemana diaSemana) {
        try {
            logger.info("GET /api/horarios-admin/{} - Obteniendo horario", diaSemana);
            HorarioDiaSemana horario = horarioDiaSemanaRepository.findByDiaSemana(diaSemana)
                    .orElse(null);
            return horario != null ? ResponseEntity.ok(horario) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error obteniendo horario: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/horarios-admin/{diaSemana}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HorarioDiaSemana> actualizarHorarioDia(
            @PathVariable DiaSemana diaSemana,
            @Valid @RequestBody HorarioDiaSemana horario) {
        try {
            logger.info("PUT /api/horarios-admin/{} - Actualizando horario", diaSemana);
            HorarioDiaSemana actualizado = horarioDiaSemanaService.actualizarHorarioDia(diaSemana, horario);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            logger.error("Error actualizando horario: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/horarios-del-dia")
    public ResponseEntity<?> obtenerHorariosDelDia(
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            List<LocalTime> horarios = horarioService.generarHorariosPorDia(fecha);
            return ResponseEntity.ok(horarios);
        } catch (Exception e) {
            logger.error("Error horarios del día: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/dias-no-disponibles")
    public ResponseEntity<?> obtenerDiasNoDisponibles(
            @RequestParam("anio") int anio,
            @RequestParam("mes") int mes) {
        try {
            YearMonth yearMonth = YearMonth.of(anio, mes);
            List<String> diasNoDisponibles = horarioService.obtenerDiasNoDisponibles(yearMonth, null);
            return ResponseEntity.ok(diasNoDisponibles);
        } catch (Exception e) {
            logger.error("Error días no disponibles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
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

    // ═══════════════════════════════════════════════════════════════════════════════
    // ✅ ENDPOINTS GENÉRICOS - DESPUÉS (Con {id})
    // ═══════════════════════════════════════════════════════════════════════════════

    @GetMapping("/citas/{id}")
    public ResponseEntity<CitaDTO> obtenerCitaPorId(@PathVariable Long id) {
        return citaService.obtenerCitaPorId(id)
                .map(cita -> ResponseEntity.ok(citaMapper.toDTO(cita)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/citas/{id}")
    public ResponseEntity<CitaDTO> actualizarCita(
            @PathVariable Long id,
            @Valid @RequestBody CitaDTO citaDTO) {
        try {
            Cita cita = citaMapper.toEntity(citaDTO);
            cita.setId(id);
            Cita actualizada = citaService.actualizarCita(cita);
            return ResponseEntity.ok(citaMapper.toDTO(actualizada));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/citas/{id}")
    public ResponseEntity<Map<String, String>> eliminarCita(@PathVariable Long id) {
        try {
            citaService.eliminarCita(id);
            return ResponseEntity.ok(Map.of("mensaje", "Cita eliminada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ─── CAMBIOS DE ESTADO ────────────────────────────────────────────────

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
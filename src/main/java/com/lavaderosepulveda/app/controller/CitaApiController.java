package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.CitaDTO;
import com.lavaderosepulveda.app.mapper.CitaMapper;
import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.HorarioDiaSemana;
import com.lavaderosepulveda.app.model.VehicleModel;
import com.lavaderosepulveda.app.model.enums.DiaSemana;
import com.lavaderosepulveda.app.model.enums.EstadoCita;
import com.lavaderosepulveda.app.model.enums.TipoLavado;
import com.lavaderosepulveda.app.repository.HorarioDiaSemanaRepository;
import com.lavaderosepulveda.app.repository.VehicleModelRepository;
import com.lavaderosepulveda.app.security.CitaRateLimiter;
import com.lavaderosepulveda.app.service.CitaService;
import com.lavaderosepulveda.app.service.EmailService;
import com.lavaderosepulveda.app.service.HorarioService;
import com.lavaderosepulveda.app.service.HorarioDiaSemanaService;
import com.lavaderosepulveda.app.service.VehicleClassificationService;
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
    @Autowired private VehicleModelRepository vehicleModelRepository;
    @Autowired private VehicleClassificationService vehicleClassificationService;

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
    public ResponseEntity<List<String>> obtenerHorariosDisponibles(
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        
        try {
            if (fecha.isBefore(LocalDate.now())) {
                return ResponseEntity.badRequest().build();
            }
            
            List<String> horariosDisponibles = horarioService.obtenerHorariosDisponiblesFormato(fecha);
            
            if (horariosDisponibles.isEmpty()) {
                logger.info("No hay horarios disponibles para la fecha: {}", fecha);
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            return ResponseEntity.ok(horariosDisponibles);
        } catch (Exception e) {
            logger.error("Error obteniendo horarios disponibles para fecha: {}", fecha, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("citasHoy", citaService.contarCitasHoy());
        resumen.put("citasConfirmadas", citaService.contarCitasPorEstado(EstadoCita.CONFIRMADA));
        resumen.put("citasPendientes", citaService.contarCitasPorEstado(EstadoCita.PENDIENTE));
        resumen.put("citasEnProceso", citaService.contarCitasPorEstado(EstadoCita.EN_PROCESO));
        return ResponseEntity.ok(resumen);
    }

    // ─── TIPOS DE LAVADO ──────────────────────────────────────────────────────────

    @GetMapping("/tipos-lavado")
    public ResponseEntity<List<Map<String, Object>>> listarTiposLavado() {
        try {
            List<Map<String, Object>> tiposLavado = Arrays.stream(TipoLavado.values())
                    .map(tipo -> Map.of(
                        "id", (Object) tipo.ordinal(),
                        "name", tipo.name(),
                        "descripcion", tipo.name().replace("_", " ")
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tiposLavado);
        } catch (Exception e) {
            logger.error("Error obteniendo tipos de lavado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ✅ NUEVO: Obtiene los tipos de lavado disponibles para un modelo específico
     * Filtra según la categoría del vehículo usando VehicleClassificationService
     */
    @GetMapping("/tipos-lavado/por-modelo/{modeloId}")
    public ResponseEntity<List<Map<String, Object>>> obtenerTiposLavadoPorModelo(@PathVariable Long modeloId) {
        try {
            // ① Obtener modelo desde BD
            VehicleModel modelo = vehicleModelRepository.findById(modeloId)
                    .orElseThrow(() -> new RuntimeException("Modelo de vehículo no encontrado con ID: " + modeloId));
            
            // ② Obtener categoría del modelo
            String categoria = modelo.getCategory().getName();
            logger.debug("Obteniendo tipos de lavado para modelo: {} (categoría: {})", modelo.getName(), categoria);
            
            // ③ Obtener tipos de lavado disponibles para esa categoría
            List<TipoLavado> servicios = vehicleClassificationService.getAvailableServices(categoria);
            
            // ④ Convertir a DTO con id, name y descripción
            List<Map<String, Object>> resultado = servicios.stream()
                    .map(tipo -> Map.of(
                        "id", (Object) tipo.ordinal(),
                        "name", tipo.name(),
                        "descripcion", tipo.name().replace("_", " ").toUpperCase()
                    ))
                    .collect(Collectors.toList());
            
            logger.info("Se encontraron {} tipos de lavado para modelo: {}", resultado.size(), modelo.getName());
            return ResponseEntity.ok(resultado);
            
        } catch (RuntimeException e) {
            logger.error("Error: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error obteniendo tipos de lavado para modelo {}: {}", modeloId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(Map.of("error", (Object) e.getMessage())));
        }
    }

    // ─── BÚSQUEDAS ────────────────────────────────────────────────────────────────

    @PostMapping("/citas")
    public ResponseEntity<CitaDTO> crearCita(@Valid @RequestBody CitaDTO citaDTO) {
        try {
            Cita cita = citaMapper.toEntity(citaDTO);
            Cita citaGuardada = citaService.crearCita(cita);
            enviarEmailConfirmacionSiEsPosible(citaGuardada);
            return ResponseEntity.status(HttpStatus.CREATED).body(citaMapper.toDTO(citaGuardada));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/citas/buscar")
    public ResponseEntity<List<CitaDTO>> buscarCitasPorTelefono(@RequestParam String telefono) {
        try {
            return ResponseEntity.ok(citaService.obtenerCitasPorTelefono(telefono).stream()
                    .map(citaMapper::toDTO).collect(Collectors.toList()));
        } catch (Exception e) {
            logger.error("Error buscando citas: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/citas/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(
            @RequestParam(value = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            LocalDate fechaBusqueda = fecha != null ? fecha : LocalDate.now();
            Map<String, Object> estadisticas = horarioService.obtenerEstadisticasOcupacion(fechaBusqueda);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/citas/dias-no-disponibles")
    public ResponseEntity<Map<String, Object>> obtenerDiasNoDisponibles(
            @RequestParam("anio") int anio,
            @RequestParam("mes") int mes) {
        try {
            YearMonth yearMonth = YearMonth.of(anio, mes);
            List<String> diasNoDisponibles = horarioService.obtenerDiasNoDisponibles(yearMonth, null);
            return ResponseEntity.ok(Map.of(
                "anio", anio,
                "mes", mes,
                "diasNoDisponibles", diasNoDisponibles
            ));
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
        try { 
            return ResponseEntity.ok(citaMapper.toDTO(citaService.confirmarCita(id))); 
        }
        catch (RuntimeException e) { 
            return ResponseEntity.notFound().build(); 
        }
    }

    @PostMapping("/citas/{id}/iniciar")
    public ResponseEntity<CitaDTO> iniciarServicio(@PathVariable Long id) {
        try { 
            return ResponseEntity.ok(citaMapper.toDTO(citaService.iniciarServicio(id))); 
        }
        catch (RuntimeException e) { 
            return ResponseEntity.notFound().build(); 
        }
    }

    @PostMapping("/citas/{id}/completar")
    public ResponseEntity<CitaDTO> completarCita(@PathVariable Long id) {
        try { 
            return ResponseEntity.ok(citaMapper.toDTO(citaService.completarCita(id))); 
        }
        catch (RuntimeException e) { 
            return ResponseEntity.notFound().build(); 
        }
    }

    @PostMapping("/citas/{id}/no-presentado")
    public ResponseEntity<CitaDTO> marcarNoPresentado(@PathVariable Long id) {
        try { 
            return ResponseEntity.ok(citaMapper.toDTO(citaService.marcarNoPresentado(id))); 
        }
        catch (RuntimeException e) { 
            return ResponseEntity.notFound().build(); 
        }
    }

    @PostMapping("/citas/{id}/llegada")
    public ResponseEntity<CitaDTO> registrarLlegada(@PathVariable Long id) {
        try { 
            return ResponseEntity.ok(citaMapper.toDTO(citaService.registrarLlegada(id))); 
        }
        catch (RuntimeException e) { 
            return ResponseEntity.notFound().build(); 
        }
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
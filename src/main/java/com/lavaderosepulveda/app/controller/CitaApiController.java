package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.CitaDTO;
import com.lavaderosepulveda.app.mapper.CitaMapper;
import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.TipoLavado;
import com.lavaderosepulveda.app.service.CitaService;
import com.lavaderosepulveda.app.service.EmailService;
import com.lavaderosepulveda.app.service.HorarioService;
import com.lavaderosepulveda.app.util.DateTimeFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
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

        return ResponseEntity.ok(!disponible);  // ← Devolver solo el boolean
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
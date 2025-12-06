package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.repository.CitaRepository;
import com.lavaderosepulveda.app.util.DateTimeFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio refactorizado para la gestión de citas
 * Se enfoca únicamente en la lógica de negocio de citas
 * La lógica de horarios se movió a HorarioService
 */
@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private HorarioService horarioService;

    /**
     * Crear una nueva cita con validaciones de negocio
     */
    public Cita crearCita(Cita cita) {
        if (cita == null) {
            throw new IllegalArgumentException("La cita no puede ser nula");
        }

        // Validar disponibilidad del horario
        validarDisponibilidadHorario(cita.getFecha(), cita.getHora());

        // Validar que la fecha no sea en el pasado
        validarFechaFutura(cita.getFecha());

        return citaRepository.save(cita);
    }

    /**
     * Obtener todas las citas ordenadas por fecha y hora
     */
    public List<Cita> obtenerTodasLasCitas() {
        return citaRepository.findAll().stream()
                .sorted(Comparator.comparing(Cita::getFecha).reversed().thenComparing(Cita::getHora).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Obtener cita por ID
     */
    public Optional<Cita> obtenerCitaPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        return citaRepository.findById(id);
    }

    /**
     * Actualizar cita existente con validaciones
     */
    public Cita actualizarCita(Long id, Cita citaActualizada) {
        if (id == null || citaActualizada == null) {
            throw new IllegalArgumentException("El ID y la cita no pueden ser nulos");
        }

        return citaRepository.findById(id)
                .map(citaExistente -> {
                    // Actualizar campos básicos
                    citaExistente.setNombre(citaActualizada.getNombre());
                    citaExistente.setEmail(citaActualizada.getEmail());
                    citaExistente.setTelefono(citaActualizada.getTelefono());
                    citaExistente.setModeloVehiculo(citaActualizada.getModeloVehiculo());
                    citaExistente.setTipoLavado(citaActualizada.getTipoLavado());

                    // Validar cambios de fecha/hora
                    boolean cambioFechaHora = !citaExistente.getFecha().equals(citaActualizada.getFecha()) ||
                            !citaExistente.getHora().equals(citaActualizada.getHora());

                    if (cambioFechaHora) {
                        validarDisponibilidadHorario(citaActualizada.getFecha(), citaActualizada.getHora());
                        validarFechaFutura(citaActualizada.getFecha());

                        citaExistente.setFecha(citaActualizada.getFecha());
                        citaExistente.setHora(citaActualizada.getHora());
                    }

                    return citaRepository.save(citaExistente);
                })
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
    }

    /**
     * Eliminar cita por ID
     */
    public void eliminarCita(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        if (!citaRepository.existsById(id)) {
            throw new RuntimeException("No se encontró la cita con ID: " + id);
        }

        citaRepository.deleteById(id);
    }

    /**
     * Obtener citas por fecha
     */
    public List<Cita> obtenerCitasPorFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }

        return citaRepository.findByFecha(fecha).stream()
                .sorted(Comparator.comparing(Cita::getHora))
                .collect(Collectors.toList());
    }

    /**
     * Obtener citas de un cliente por teléfono
     */
    public List<Cita> obtenerCitasPorTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono no puede estar vacío");
        }

        return citaRepository.findByTelefono(telefono.trim()).stream()
                .sorted(Comparator.comparing(Cita::getFecha).reversed().thenComparing(Cita::getHora).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Obtener citas agrupadas por fecha formateada para la vista
     * Usa DateTimeFormatUtils para formateo consistente
     */
    public Map<String, List<Cita>> obtenerCitasAgrupadasPorFechaFormateada() {
        List<Cita> todasLasCitas = obtenerTodasLasCitas();

        // Agrupar por fecha y ordenar
        Map<LocalDate, List<Cita>> citasPorFecha = todasLasCitas.stream()
                .collect(Collectors.groupingBy(
                        Cita::getFecha,
                        () -> new TreeMap<>(Collections.reverseOrder()),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                lista -> {
                                    lista.sort(Comparator.comparing(Cita::getHora));
                                    return lista;
                                }
                        )
                ));

        // Convertir a formato con fechas formateadas usando utility
        Map<String, List<Cita>> citasFormateadas = new LinkedHashMap<>();
        citasPorFecha.forEach((fecha, citas) -> {
            String fechaFormateada = DateTimeFormatUtils.formatearFechaCompleta(fecha);
            citasFormateadas.put(fechaFormateada, citas);
        });

        return citasFormateadas;
    }

    /**
     * Obtener horarios disponibles para una fecha - Delegado a HorarioService
     */
    public List<LocalTime> obtenerHorariosDisponibles(LocalDate fecha) {
        return horarioService.obtenerHorariosDisponibles(fecha);
    }

    /**
     * Verificar si existe una cita en fecha y hora específica
     */
    public boolean existeCitaEnFechaHora(LocalDate fecha, LocalTime hora) {
        if (fecha == null || hora == null) {
            return false;
        }
        return citaRepository.existsByFechaAndHora(fecha, hora);
    }

    /**
     * Obtener citas para un rango de fechas
     */
    public List<Cita> obtenerCitasEnRango(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin no pueden ser nulas");
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha fin");
        }

        return citaRepository.findCitasBetweenDates(fechaInicio, fechaFin);
    }

    /**
     * Obtener próximas citas (útil para recordatorios)
     */
    public List<Cita> obtenerProximasCitas(int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(dias);

        return obtenerCitasEnRango(hoy, fechaLimite);
    }

    /**
     * Validaciones privadas
     */
    private void validarDisponibilidadHorario(LocalDate fecha, LocalTime hora) {
        if (!horarioService.esHorarioDisponible(fecha, hora)) {
            throw new RuntimeException("El horario seleccionado no está disponible. " +
                    "Por favor, elija otro horario.");
        }
    }

    private void validarFechaFutura(LocalDate fecha) {
        if (fecha.isBefore(LocalDate.now())) {
            throw new RuntimeException("No se pueden crear citas en fechas pasadas");
        }
    }
}
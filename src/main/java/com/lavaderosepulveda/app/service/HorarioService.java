package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.config.HorariosConfig;
import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.HorarioDiaSemana;
import com.lavaderosepulveda.app.model.enums.DiaSemana;
import com.lavaderosepulveda.app.model.enums.TipoLavado;
import com.lavaderosepulveda.app.repository.CitaRepository;
import com.lavaderosepulveda.app.repository.HorarioDiaSemanaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de horarios del lavadero.
 * 
 * Refactorizado para usar la tabla HorarioDiaSemana en lugar de HorariosConfig.
 * Mantiene toda la lógica de ocupación, duraciones y disponibilidad.
 */
@Service
public class HorarioService {

    private static final Logger logger = LoggerFactory.getLogger(HorarioService.class);

    @Autowired
    private HorarioDiaSemanaRepository horarioDiaSemanaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private HorariosConfig horariosConfig;  // Para fallback si no hay BD

    // ─── HORARIOS DISPONIBLES ─────────────────────────────────────────────────

    /**
     * Obtiene los horarios disponibles para una fecha específica.
     * Filtra: horarios del día - horarios ocupados
     */
    public List<LocalTime> obtenerHorariosDisponibles(LocalDate fecha) {
        if (fecha == null) throw new IllegalArgumentException("La fecha no puede ser nula");

        // Verificar si está cerrado
        HorarioDiaSemana horario = obtenerHorarioDia(fecha);
        if (horario == null || !horario.getActivo()) {
            return Collections.emptyList();
        }

        // Obtener todos los horarios del día
        List<LocalTime> todosLosHorarios = generarHorariosPorDia(fecha);
        if (todosLosHorarios.isEmpty()) {
            return Collections.emptyList();
        }

        // Filtrar ocupados
        Set<LocalTime> horariosOcupados = obtenerHorariosOcupados(fecha);

        return todosLosHorarios.stream()
                .filter(h -> !horariosOcupados.contains(h))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Genera lista de horarios en horas completas para un día.
     * Por ejemplo: [09:00, 10:00, 11:00, 14:00, 17:00, 18:00, 19:00]
     */
    public List<LocalTime> generarHorariosPorDia(LocalDate fecha) {
        HorarioDiaSemana horario = obtenerHorarioDia(fecha);
        if (horario == null || !horario.getActivo()) {
            return Collections.emptyList();
        }

        List<LocalTime> horarios = new ArrayList<>();

        // Franja Mañana
        if (horario.getAperturaMañana() != null && horario.getCierreMañana() != null) {
            LocalTime inicio = horario.getAperturaMañana();
            LocalTime fin = horario.getCierreMañana();
            for (LocalTime h = inicio; h.isBefore(fin); h = h.plusHours(1)) {
                horarios.add(h);
            }
        }

        // Franja Tarde
        if (horario.getAperturaTarde() != null && horario.getCierreTarde() != null) {
            LocalTime inicio = horario.getAperturaTarde();
            LocalTime fin = horario.getCierreTarde();
            for (LocalTime h = inicio; h.isBefore(fin); h = h.plusHours(1)) {
                horarios.add(h);
            }
        }

        return horarios;
    }

    /**
     * Obtiene el horario configurado para un día de la semana.
     */
    private HorarioDiaSemana obtenerHorarioDia(LocalDate fecha) {
        if (fecha == null) return null;

        DayOfWeek dayOfWeek = fecha.getDayOfWeek();
        DiaSemana dia = convertirDayOfWeekADiaSemana(dayOfWeek);

        return horarioDiaSemanaRepository.findByDiaSemana(dia).orElse(null);
    }

    /**
     * Convierte DayOfWeek de Java a DiaSemana (enum nuestro)
     */
    private DiaSemana convertirDayOfWeekADiaSemana(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY    -> DiaSemana.LUNES;
            case TUESDAY   -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY  -> DiaSemana.JUEVES;
            case FRIDAY    -> DiaSemana.VIERNES;
            case SATURDAY  -> DiaSemana.SABADO;
            case SUNDAY    -> DiaSemana.DOMINGO;
        };
    }

    // ─── HORARIOS OCUPADOS ────────────────────────────────────────────────────

    /**
     * Obtiene el set de horas ocupadas para una fecha.
     */
    private Set<LocalTime> obtenerHorariosOcupados(LocalDate fecha) {
        return construirHorariosOcupados(citaRepository.findByFecha(fecha));
    }

    /**
     * Construye el set de horas ocupadas a partir de una lista de citas.
     * Tiene en cuenta duracionEstimada para bloquear slots adicionales:
     *   - 60 min (defecto) → bloquea 1 hora
     *   - 120 min          → bloquea 2 horas consecutivas
     *   - tapicería        → bloquea 3 horas
     */
    private Set<LocalTime> construirHorariosOcupados(List<Cita> citas) {
        Set<LocalTime> ocupados = new HashSet<>();
        for (Cita cita : citas) {
            LocalTime hora = cita.getHora();
            ocupados.add(hora);

            TipoLavado tipo = cita.getTipoLavado();

            // Tapicería — bloquea 3 horas
            if (tipo == TipoLavado.TAPICERIA_SIN_DESMONTAR || tipo == TipoLavado.TAPICERIA_DESMONTANDO) {
                ocupados.add(hora.plusHours(1));
                ocupados.add(hora.plusHours(2));
                continue;
            }

            // Cita de 2 horas — bloquea la hora siguiente
            Integer duracion = cita.getDuracionEstimada();
            if (duracion != null && duracion >= 120) {
                ocupados.add(hora.plusHours(1));
            }
        }
        return ocupados;
    }

    // ─── VERIFICACIÓN DE DISPONIBILIDAD ──────────────────────────────────────

    /**
     * Verifica si un horario específico está disponible.
     */
    public boolean esHorarioDisponible(LocalDate fecha, LocalTime hora) {
        if (fecha == null || hora == null) return false;

        // Verificar que el día está abierto
        HorarioDiaSemana horario = obtenerHorarioDia(fecha);
        if (horario == null || !horario.getActivo()) {
            return false;
        }

        // Verificar que la hora está dentro del horario
        if (!generarHorariosPorDia(fecha).contains(hora)) {
            return false;
        }

        // Verificar que no hay cita en ese horario
        return !citaRepository.existsByFechaAndHora(fecha, hora);
    }

    /**
     * Verifica si hay disponibilidad para una cita de duración específica.
     * Para citas de 2 horas comprueba que el slot siguiente también esté libre.
     */
    public boolean esHorarioDisponibleParaDuracion(LocalDate fecha, LocalTime hora, int duracionMinutos) {
        if (!esHorarioDisponible(fecha, hora)) return false;
        if (duracionMinutos >= 120) {
            LocalTime horaSiguiente = hora.plusHours(1);
            if (!generarHorariosPorDia(fecha).contains(horaSiguiente)) return false;
            return !citaRepository.existsByFechaAndHora(fecha, horaSiguiente);
        }
        return true;
    }

    /**
     * Obtiene el siguiente horario disponible después de la hora actual.
     */
    public Optional<LocalTime> siguienteHorarioDisponible(LocalDate fecha, LocalTime horaActual) {
        return obtenerHorariosDisponibles(fecha).stream()
                .filter(h -> h.isAfter(horaActual))
                .findFirst();
    }

    // ─── DISPONIBILIDAD MENSUAL (OPTIMIZADA) ─────────────────────────────────

    /**
     * Obtiene lista de días no disponibles en un mes para un tipo de servicio.
     */
    public List<String> obtenerDiasNoDisponibles(YearMonth mes, TipoLavado tipoServicio) {
        List<String> diasNoDisponibles = new ArrayList<>();
        LocalDate fechaInicio = mes.atDay(1);
        LocalDate fechaFin    = mes.atEndOfMonth();

        LocalDate hoy = LocalDate.now();
        if (mes.equals(YearMonth.now()) && hoy.isAfter(fechaInicio)) {
            for (LocalDate d = fechaInicio; d.isBefore(hoy); d = d.plusDays(1))
                diasNoDisponibles.add(d.toString());
            fechaInicio = hoy;
        }

        List<Cita> citasDelMes = citaRepository.findCitasBetweenDates(fechaInicio, fechaFin);
        Map<LocalDate, List<Cita>> citasPorFecha = citasDelMes.stream()
                .collect(Collectors.groupingBy(Cita::getFecha));

        boolean esTapiceria = (tipoServicio == TipoLavado.TAPICERIA_SIN_DESMONTAR
                || tipoServicio == TipoLavado.TAPICERIA_DESMONTANDO);

        for (LocalDate fecha = fechaInicio; !fecha.isAfter(fechaFin); fecha = fecha.plusDays(1)) {
            // Verificar si el día está cerrado en la BD
            HorarioDiaSemana horario = obtenerHorarioDia(fecha);
            if (horario == null || !horario.getActivo()) {
                diasNoDisponibles.add(fecha.toString());
                continue;
            }

            // Tapicería solo lunes-jueves
            if (esTapiceria && (fecha.getDayOfWeek() == DayOfWeek.FRIDAY
                    || fecha.getDayOfWeek() == DayOfWeek.SATURDAY)) {
                diasNoDisponibles.add(fecha.toString());
                continue;
            }

            List<Cita> citasDia = citasPorFecha.getOrDefault(fecha, Collections.emptyList());
            Set<LocalTime> ocupados = construirHorariosOcupados(citasDia);

            if (!hayHorarioDisponibleEnMemoria(fecha, tipoServicio, ocupados)) {
                diasNoDisponibles.add(fecha.toString());
            }
        }

        return diasNoDisponibles;
    }

    /**
     * Verifica si hay al menos un horario disponible para un tipo de servicio.
     */
    private boolean hayHorarioDisponibleEnMemoria(LocalDate fecha, TipoLavado tipoServicio,
                                                   Set<LocalTime> horariosOcupados) {
        List<LocalTime> todosLosHorarios = generarHorariosPorDia(fecha);

        if (tipoServicio == TipoLavado.TAPICERIA_SIN_DESMONTAR
                || tipoServicio == TipoLavado.TAPICERIA_DESMONTANDO) {
            LocalTime h08 = LocalTime.of(8, 0);
            if (!todosLosHorarios.contains(h08)) return false;
            return !horariosOcupados.contains(h08)
                    && !horariosOcupados.contains(h08.plusHours(1)) && esHorarioValido(fecha, h08.plusHours(1))
                    && !horariosOcupados.contains(h08.plusHours(2)) && esHorarioValido(fecha, h08.plusHours(2));
        }

        return todosLosHorarios.stream().anyMatch(h -> !horariosOcupados.contains(h));
    }

    /**
     * Verifica si una hora es válida para un día (está dentro del horario).
     */
    private boolean esHorarioValido(LocalDate fecha, LocalTime hora) {
        return generarHorariosPorDia(fecha).contains(hora);
    }

    // ─── ESTADÍSTICAS Y CONFIGURACIÓN ────────────────────────────────────────

    /**
     * Obtiene estadísticas de ocupación para una fecha.
     */
    public Map<String, Object> obtenerEstadisticasOcupacion(LocalDate fecha) {
        List<LocalTime> todos    = generarHorariosPorDia(fecha);
        Set<LocalTime>  ocupados = obtenerHorariosOcupados(fecha);
        int total = todos.size();
        double pct = total > 0 ? (double) ocupados.size() / total * 100 : 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("fecha", fecha);
        stats.put("totalHorarios", total);
        stats.put("horariosOcupados", ocupados.size());
        stats.put("horariosLibres", total - ocupados.size());
        stats.put("porcentajeOcupacion", Math.round(pct * 100.0) / 100.0);
        return stats;
    }

    /**
     * Obtiene la configuración de horarios actual (de BD).
     * Devuelve los horarios de toda la semana.
     */
    public Map<String, Object> obtenerConfiguracionHorarios() {
        List<HorarioDiaSemana> horarios = horarioDiaSemanaRepository.findAllByOrderByDiaSemanaAsc();

        Map<String, Object> config = new HashMap<>();

        for (HorarioDiaSemana h : horarios) {
            String dia = h.getDiaSemana().name();
            Map<String, Object> datoDia = new HashMap<>();
            datoDia.put("activo", h.getActivo());
            datoDia.put("aperturaMañana", h.getAperturaMañana());
            datoDia.put("cierreMañana", h.getCierreMañana());
            datoDia.put("aperturaTarde", h.getAperturaTarde());
            datoDia.put("cierreTarde", h.getCierreTarde());
            config.put(dia, datoDia);
        }

        return config;
    }

    /**
     * Valida la configuración de horarios en la BD.
     */
    public boolean validarConfiguracion() {
        List<HorarioDiaSemana> horarios = horarioDiaSemanaRepository.findAll();

        if (horarios.isEmpty()) {
            logger.warn("No hay horarios configurados en la BD. Usando defaults de HorariosConfig");
            return horariosConfig.isConfiguracionValida();
        }

        for (HorarioDiaSemana h : horarios) {
            if (!h.isHorarioValido() || !h.isSeparacionValida()) {
                logger.error("Horario inválido para {}: {}", h.getDiaSemana(), h);
                return false;
            }
        }

        logger.info("Configuración de horarios válida: {} días configurados", horarios.size());
        return true;
    }
}
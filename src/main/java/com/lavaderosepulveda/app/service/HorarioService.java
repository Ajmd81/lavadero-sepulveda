package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.config.HorariosConfig;
import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.DiaCerrado;
import com.lavaderosepulveda.app.model.HorarioDiaSemana;
import com.lavaderosepulveda.app.model.enums.DiaSemana;
import com.lavaderosepulveda.app.model.enums.EstadoCita;
import com.lavaderosepulveda.app.model.enums.TipoLavado;
import com.lavaderosepulveda.app.repository.CitaRepository;
import com.lavaderosepulveda.app.repository.DiaCerradoRepository;
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
 * Servicio para gestionar horarios, disponibilidad de citas y días cerrados.
 * Incluye integración con DiaCerrado para manejar festividades, vacaciones y mantenimiento.
 */
@Service
public class HorarioService {

    private static final Logger logger = LoggerFactory.getLogger(HorarioService.class);

    @Autowired private HorarioDiaSemanaRepository horarioDiaSemanaRepository;
    @Autowired private CitaRepository citaRepository;
    @Autowired private DiaCerradoRepository diaCerradoRepository;
    @Autowired private HorariosConfig horariosConfig;

    /**
     * Genera los horarios disponibles para un día específico.
     * Genera slots de 1 hora basados en la configuración de apertura/cierre.
     */
    public List<LocalTime> generarHorariosPorDia(LocalDate fecha) {
        List<LocalTime> horarios = new ArrayList<>();
        
        // ✅ INTEGRACIÓN CON DÍAS CERRADOS: Verificar si está cerrado
        if (diaCerradoRepository.existsByFecha(fecha)) {
            logger.info("Día {} está cerrado", fecha);
            return horarios; // Retornar lista vacía
        }

        DayOfWeek dayOfWeek = fecha.getDayOfWeek();
        DiaSemana diaSemana = convertirDayOfWeekADiaSemana(dayOfWeek);

        HorarioDiaSemana horarioDia = horarioDiaSemanaRepository.findByDiaSemana(diaSemana)
                .orElse(null);

        // ✅ CORREGIDO: Usar getActivo() con getter
        if (horarioDia == null || !horarioDia.getActivo()) {
            return horarios;
        }

        // Turno mañana
        if (horarioDia.getAperturaMañana() != null && horarioDia.getCierreMañana() != null) {
            LocalTime inicio = horarioDia.getAperturaMañana();
            LocalTime fin = horarioDia.getCierreMañana();
            for (LocalTime h = inicio; h.isBefore(fin); h = h.plusHours(1)) {
                horarios.add(h);
            }
        }

        // Turno tarde
        if (horarioDia.getAperturaTarde() != null && horarioDia.getCierreTarde() != null) {
            LocalTime inicio = horarioDia.getAperturaTarde();
            LocalTime fin = horarioDia.getCierreTarde();
            for (LocalTime h = inicio; h.isBefore(fin); h = h.plusHours(1)) {
                horarios.add(h);
            }
        }

        return horarios;
    }

    /**
     * Obtiene los horarios disponibles (sin citas) para un día específico.
     */
    public List<LocalTime> obtenerHorariosDisponibles(LocalDate fecha) {
        List<LocalTime> horariosDelDia = generarHorariosPorDia(fecha);
        List<LocalTime> horariosOcupados = citaRepository.findByFecha(fecha).stream()
                .map(Cita::getHora)
                .collect(Collectors.toList());

        return horariosDelDia.stream()
                .filter(h -> !horariosOcupados.contains(h))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Verifica si un horario específico está disponible.
     * Integrado con DiaCerrado para validar días cerrados.
     */
    public boolean esHorarioDisponible(LocalDate fecha, LocalTime hora) {
        // ✅ INTEGRACIÓN CON DÍAS CERRADOS
        if (diaCerradoRepository.existsByFecha(fecha)) {
            logger.debug("Fecha {} está en DiaCerrado", fecha);
            return false;
        }

        // Verificar que no haya cita en ese horario
        if (citaRepository.existsByFechaAndHora(fecha, hora)) {
            return false;
        }

        // Verificar que la hora esté dentro de los horarios configurados
        return obtenerHorariosDisponibles(fecha).contains(hora);
    }

    /**
     * Obtiene los horarios disponibles como strings (HH:mm)
     */
    public List<String> obtenerHorariosDisponiblesFormato(LocalDate fecha) {
        // Usar el método existente que ya funciona
        return obtenerHorariosDisponibles(fecha).stream()
                .map(hora -> String.format("%02d:%02d", hora.getHour(), hora.getMinute()))
                .collect(Collectors.toList());
    }

    /**
     * Genera lista de horarios entre dos horas
     */
    private List<String> generarHorarios(String horaInicio, String horaFin) {
        List<String> horarios = new ArrayList<>();
        
        int hInicio = Integer.parseInt(horaInicio.split(":")[0]);
        int hFin = Integer.parseInt(horaFin.split(":")[0]);
        
        for (int h = hInicio; h < hFin; h++) {
            horarios.add(String.format("%02d:00", h));
        }
        
        return horarios;
    }

    /**
     * Obtiene el día de la semana en español
     */
    private String obtenerDiaSemana(LocalDate fecha) {
        DayOfWeek dayOfWeek = fecha.getDayOfWeek();
        
        switch (dayOfWeek) {
            case MONDAY:    return "LUNES";
            case TUESDAY:   return "MARTES";
            case WEDNESDAY: return "MIERCOLES";
            case THURSDAY:  return "JUEVES";
            case FRIDAY:    return "VIERNES";
            case SATURDAY:  return "SABADO";
            case SUNDAY:    return "DOMINGO";
            default:        throw new IllegalArgumentException("Día inválido");
        }
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
     * Obtiene los días no disponibles (con citas) en un mes específico.
     */
    public List<String> obtenerDiasNoDisponibles(YearMonth yearMonth, TipoLavado tipoLavado) {
        LocalDate inicio = yearMonth.atDay(1);
        LocalDate fin = yearMonth.atEndOfMonth();

        // ✅ CORREGIDO: Usar findFechasByRango en lugar de findByFechaBetween
        Set<LocalDate> diasCerrados = diaCerradoRepository.findFechasByRango(inicio, fin)
                .stream()
                .collect(Collectors.toSet());

        // Obtener todas las citas en el rango
        Set<LocalDate> diasOcupados = new HashSet<>();
        for (LocalDate fecha = inicio; !fecha.isAfter(fin); fecha = fecha.plusDays(1)) {
            List<Cita> citasDelDia = citaRepository.findByFecha(fecha);
            if (citasDelDia != null && !citasDelDia.isEmpty()) {
                int citasEnDia = citasDelDia.size();
                int horariosDisponibles = generarHorariosPorDia(fecha).size();
                // Si está 50% ocupado o más
                if (horariosDisponibles > 0 && citasEnDia >= (horariosDisponibles * 0.5)) {
                    diasOcupados.add(fecha);
                }
            }
        }

        Set<LocalDate> todosLosNoDisponibles = new HashSet<>(diasCerrados);
        todosLosNoDisponibles.addAll(diasOcupados);

        return todosLosNoDisponibles.stream()
                .sorted()
                .map(LocalDate::toString)
                .collect(Collectors.toList());
    }

    /**
     * Valida si una hora está dentro del rango válido del día.
     */
    public boolean esHorarioValido(LocalDate fecha, LocalTime hora) {
        DayOfWeek dayOfWeek = fecha.getDayOfWeek();
        DiaSemana diaSemana = convertirDayOfWeekADiaSemana(dayOfWeek);

        HorarioDiaSemana horarioDia = horarioDiaSemanaRepository.findByDiaSemana(diaSemana)
                .orElse(null);

        if (horarioDia == null || !horarioDia.getActivo()) return false;

        // ✅ CORREGIDO: Usar métodos correctos de LocalTime
        // isSameOrAfter() no existe, usar !isBefore() en su lugar
        boolean enMañana = horarioDia.getAperturaMañana() != null && horarioDia.getCierreMañana() != null &&
                !hora.isBefore(horarioDia.getAperturaMañana()) &&
                hora.isBefore(horarioDia.getCierreMañana());
        
        boolean enTarde = horarioDia.getAperturaTarde() != null && horarioDia.getCierreTarde() != null &&
                !hora.isBefore(horarioDia.getAperturaTarde()) &&
                hora.isBefore(horarioDia.getCierreTarde());

        return enMañana || enTarde;
    }

    /**
     * Obtiene las horas ocupadas (con citas activas) para un día.
     */
    public Set<LocalTime> obtenerHorasOcupadasPorDia(LocalDate fecha) {
        Set<LocalTime> ocupadas = new HashSet<>();

        List<Cita> citasDelDia = citaRepository.findByFecha(fecha);
        if (citasDelDia != null) {
            for (Cita cita : citasDelDia) {
                ocupadas.add(cita.getHora());
                
                // Si la cita es de 2 horas, ocupar el slot siguiente también
                if (cita.getDuracionEstimada() != null && cita.getDuracionEstimada() >= 120) {
                    ocupadas.add(cita.getHora().plusHours(1));
                    ocupadas.add(cita.getHora().plusHours(2));
                }
            }
        }

        return ocupadas;
    }

    /**
     * Obtiene estadísticas de ocupación para un día.
     */
    public Map<String, Object> obtenerEstadisticasOcupacion(LocalDate fecha) {
        Map<String, Object> estadisticas = new HashMap<>();

        List<LocalTime> horariosDelDia = generarHorariosPorDia(fecha);
        Set<LocalTime> horasOcupadas = obtenerHorasOcupadasPorDia(fecha);
        int totalSlots = horariosDelDia.size();
        int slotsOcupados = (int) horariosDelDia.stream().filter(horasOcupadas::contains).count();

        estadisticas.put("fecha", fecha.toString());
        estadisticas.put("totalSlots", totalSlots);
        estadisticas.put("slotsOcupados", slotsOcupados);
        estadisticas.put("slotsDisponibles", totalSlots - slotsOcupados);
        estadisticas.put("porcentajeOcupacion", totalSlots > 0 ? (slotsOcupados * 100.0 / totalSlots) : 0);

        return estadisticas;
    }

    /**
     * Convierte DayOfWeek de Java a DiaSemana del enum.
     */
    private DiaSemana convertirDayOfWeekADiaSemana(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> DiaSemana.LUNES;
            case TUESDAY -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY -> DiaSemana.JUEVES;
            case FRIDAY -> DiaSemana.VIERNES;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }

    /**
     * Obtiene los horarios configurados para un día específico de la semana.
     */
    public HorarioDiaSemana obtenerHorarioConfiguracion(DiaSemana diaSemana) {
        return horarioDiaSemanaRepository.findByDiaSemana(diaSemana)
                .orElse(null);
    }

    /**
     * Verifica si todas las citas del día pueden caber en el horario disponible.
     */
    public boolean cabenTodasLasCitasDelDia(LocalDate fecha) {
        List<LocalTime> horariosDelDia = generarHorariosPorDia(fecha);
        long citasDelDia = citaRepository.countByFecha(fecha);
        return citasDelDia <= horariosDelDia.size();
    }

    /**
     * Valida que una cita propuesta no cause conflictos.
     */
    public boolean esValidaProposicionCita(LocalDate fecha, LocalTime hora, int duracionMinutos) {
        // ① Verificar día cerrado
        if (diaCerradoRepository.existsByFecha(fecha)) {
            return false;
        }

        // ② Verificar que la hora esté en rango
        if (!esHorarioValido(fecha, hora)) {
            return false;
        }

        // ③ Verificar disponibilidad según duración
        return esHorarioDisponibleParaDuracion(fecha, hora, duracionMinutos);
    }

    /**
     * Obtiene el próximo horario disponible después de una fecha/hora dada.
     */
    public LocalTime obtenerProximoHorarioDisponible(LocalDate fechaInicio, LocalTime horaInicio) {
        LocalDate fechaActual = fechaInicio;

        // Buscar en los próximos 30 días
        for (int i = 0; i < 30; i++) {
            List<LocalTime> disponibles = obtenerHorariosDisponibles(fechaActual);
            
            for (LocalTime h : disponibles) {
                // En el primer día, buscar después de horaInicio
                // En los siguientes días, cualquier hora válida
                if (fechaActual.equals(fechaInicio)) {
                    if (h.isAfter(horaInicio)) {
                        return h;
                    }
                } else {
                    // Primer horario del día
                    return disponibles.get(0);
                }
            }

            fechaActual = fechaActual.plusDays(1);
        }

        return null; // No hay disponibilidad en los próximos 30 días
    }

    /**
     * Obtiene la configuración completa de horarios para el dashboard.
     * Incluye horarios de todos los días y configuración general.
     */
    public Map<String, Object> obtenerConfiguracionHorarios() {
        Map<String, Object> configuracion = new HashMap<>();
        
        // Obtener todos los horarios por día de semana
        List<Map<String, Object>> horarios = new ArrayList<>();
        for (DiaSemana dia : DiaSemana.values()) {
            HorarioDiaSemana horarioDia = horarioDiaSemanaRepository.findByDiaSemana(dia)
                    .orElse(null);
            
            if (horarioDia != null) {
                Map<String, Object> horarioMap = new HashMap<>();
                horarioMap.put("diaSemana", dia.toString());
                horarioMap.put("aperturaMañana", horarioDia.getAperturaMañana());
                horarioMap.put("cierreMañana", horarioDia.getCierreMañana());
                horarioMap.put("aperturaTarde", horarioDia.getAperturaTarde());
                horarioMap.put("cierreTarde", horarioDia.getCierreTarde());
                horarioMap.put("activo", horarioDia.getActivo());
                horarios.add(horarioMap);
            }
        }
        
        configuracion.put("horarios", horarios);
        
        // Configuración general (si existe)
        if (horariosConfig != null) {
            configuracion.put("duracionCitaMinutos", 60); // Valor por defecto
            configuracion.put("citasPorHora", 1);
            configuracion.put("margenCierreDiasAnticipacion", 7);
        }
        
        return configuracion;
    }

    /**
     * Valida que la configuración de horarios sea correcta.
     * Verifica que:
     * - Las horas de apertura sean antes que las de cierre
     * - Haya separación entre turno mañana y tarde
     * - Todos los días estén configurados
     */
    public boolean validarConfiguracion() {
        try {
            boolean todasValidas = true;
            
            for (DiaSemana dia : DiaSemana.values()) {
                HorarioDiaSemana horarioDia = horarioDiaSemanaRepository.findByDiaSemana(dia)
                        .orElse(null);
                
                if (horarioDia == null) {
                    logger.warn("No existe configuracion para: {}", dia);
                    todasValidas = false;
                    continue;
                }
                
                // Validar que apertura < cierre en ambos turnos
                if (!horarioDia.isHorarioValido()) {
                    logger.warn("Horario inválido para {}: apertura >= cierre", dia);
                    todasValidas = false;
                }
                
                // Validar separación entre mañana y tarde
                if (!horarioDia.isSeparacionValida()) {
                    logger.warn("Falta separación entre turnos en {}", dia);
                    todasValidas = false;
                }
            }
            
            return todasValidas;
        } catch (Exception e) {
            logger.error("Error validando configuracion de horarios", e);
            return false;
        }
    }
}
package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.config.HorariosConfig;
import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.enums.TipoLavado;
import com.lavaderosepulveda.app.repository.CitaRepository;
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

@Service
public class HorarioService {

    private static final Logger logger = LoggerFactory.getLogger(HorarioService.class);

    @Autowired
    private HorariosConfig horariosConfig;

    @Autowired
    private CitaRepository citaRepository;

    // ─── HORARIOS DISPONIBLES ─────────────────────────────────────────────────

    public List<LocalTime> obtenerHorariosDisponibles(LocalDate fecha) {
        if (fecha == null) throw new IllegalArgumentException("La fecha no puede ser nula");

        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) return Collections.emptyList();

        List<LocalTime> todosLosHorarios = generarHorariosPorDia(fecha);
        Set<LocalTime> horariosOcupados = obtenerHorariosOcupados(fecha);

        return todosLosHorarios.stream()
                .filter(h -> !horariosOcupados.contains(h))
                .sorted()
                .collect(Collectors.toList());
    }

    public List<LocalTime> generarHorariosPorDia(LocalDate fecha) {
        return fecha.getDayOfWeek() == DayOfWeek.SATURDAY
                ? generarHorariosSabado()
                : generarHorariosRegulares();
    }

    private List<LocalTime> generarHorariosRegulares() {
        List<LocalTime> horarios = new ArrayList<>();
        HorariosConfig.Turno manana = horariosConfig.getManana();
        HorariosConfig.Turno tarde  = horariosConfig.getTarde();
        for (int h = manana.getInicio(); h < manana.getFin(); h++)
            if (!manana.isHoraExcluida(h)) horarios.add(LocalTime.of(h, 0));
        for (int h = tarde.getInicio(); h < tarde.getFin(); h++)
            if (!tarde.isHoraExcluida(h)) horarios.add(LocalTime.of(h, 0));
        return horarios;
    }

    private List<LocalTime> generarHorariosSabado() {
        List<LocalTime> horarios = new ArrayList<>();
        HorariosConfig.Turno sabado = horariosConfig.getSabado();
        for (int h = sabado.getInicio(); h < sabado.getFin(); h++)
            if (!sabado.isHoraExcluida(h)) horarios.add(LocalTime.of(h, 0));
        return horarios;
    }

    // ─── HORARIOS OCUPADOS (por fecha individual) ─────────────────────────────

    private Set<LocalTime> obtenerHorariosOcupados(LocalDate fecha) {
        return construirHorariosOcupados(citaRepository.findByFecha(fecha));
    }

    /**
     * Construye el set de horas ocupadas a partir de una lista de citas ya cargada.
     * Reutilizable tanto para consultas individuales como para el batch mensual.
     */
    private Set<LocalTime> construirHorariosOcupados(List<Cita> citas) {
        Set<LocalTime> ocupados = new HashSet<>();
        for (Cita cita : citas) {
            ocupados.add(cita.getHora());
            TipoLavado tipo = cita.getTipoLavado();
            if (tipo == TipoLavado.TAPICERIA_SIN_DESMONTAR || tipo == TipoLavado.TAPICERIA_DESMONTANDO) {
                ocupados.add(cita.getHora().plusHours(1));
                ocupados.add(cita.getHora().plusHours(2));
            }
        }
        return ocupados;
    }

    // ─── VERIFICACIÓN DE DISPONIBILIDAD ──────────────────────────────────────

    public boolean esHorarioDisponible(LocalDate fecha, LocalTime hora) {
        if (fecha == null || hora == null) return false;
        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) return false;
        if (!generarHorariosPorDia(fecha).contains(hora)) return false;
        return !citaRepository.existsByFechaAndHora(fecha, hora);
    }

    public Optional<LocalTime> siguienteHorarioDisponible(LocalDate fecha, LocalTime horaActual) {
        return obtenerHorariosDisponibles(fecha).stream()
                .filter(h -> h.isAfter(horaActual))
                .findFirst();
    }

    // ─── DISPONIBILIDAD MENSUAL (OPTIMIZADA) ─────────────────────────────────

    /**
     * Versión optimizada: carga TODAS las citas del mes en UNA sola query
     * en lugar de una query por cada día. Antes hacía 31+ queries; ahora hace 1.
     */
    public List<String> obtenerDiasNoDisponibles(YearMonth mes, TipoLavado tipoServicio) {
        List<String> diasNoDisponibles = new ArrayList<>();
        LocalDate fechaInicio = mes.atDay(1);
        LocalDate fechaFin    = mes.atEndOfMonth();

        // Días pasados del mes actual → ya no disponibles
        LocalDate hoy = LocalDate.now();
        if (mes.equals(YearMonth.now()) && hoy.isAfter(fechaInicio)) {
            for (LocalDate d = fechaInicio; d.isBefore(hoy); d = d.plusDays(1)) {
                diasNoDisponibles.add(d.toString());
            }
            fechaInicio = hoy;
        }

        // ── UNA SOLA QUERY para todo el mes ──────────────────────────────────
        List<Cita> citasDelMes = citaRepository.findCitasBetweenDates(fechaInicio, fechaFin);

        // Agrupar por fecha en un Map para acceso O(1)
        Map<LocalDate, List<Cita>> citasPorFecha = citasDelMes.stream()
                .collect(Collectors.groupingBy(Cita::getFecha));

        boolean esTapiceria = (tipoServicio == TipoLavado.TAPICERIA_SIN_DESMONTAR
                || tipoServicio == TipoLavado.TAPICERIA_DESMONTANDO);

        for (LocalDate fecha = fechaInicio; !fecha.isAfter(fechaFin); fecha = fecha.plusDays(1)) {
            // Domingo → cerrado siempre
            if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
                diasNoDisponibles.add(fecha.toString());
                continue;
            }

            // Tapicería solo L-J
            if (esTapiceria && (fecha.getDayOfWeek() == DayOfWeek.FRIDAY
                    || fecha.getDayOfWeek() == DayOfWeek.SATURDAY)) {
                diasNoDisponibles.add(fecha.toString());
                continue;
            }

            // Verificar disponibilidad usando las citas ya cargadas (sin nueva query)
            List<Cita> citasDia = citasPorFecha.getOrDefault(fecha, Collections.emptyList());
            Set<LocalTime> ocupados = construirHorariosOcupados(citasDia);

            if (!hayHorarioDisponibleEnMemoria(fecha, tipoServicio, ocupados)) {
                diasNoDisponibles.add(fecha.toString());
            }
        }

        return diasNoDisponibles;
    }

    /**
     * Versión sin BD — trabaja con el set de ocupados ya cargado en memoria.
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

    private boolean esHorarioValido(LocalDate fecha, LocalTime hora) {
        return generarHorariosPorDia(fecha).contains(hora);
    }

    // ─── ESTADÍSTICAS Y CONFIGURACIÓN ────────────────────────────────────────

    public Map<String, Object> obtenerEstadisticasOcupacion(LocalDate fecha) {
        List<LocalTime> todos   = generarHorariosPorDia(fecha);
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

    public Map<String, Object> obtenerConfiguracionHorarios() {
        HorariosConfig.Turno manana = horariosConfig.getManana();
        HorariosConfig.Turno tarde  = horariosConfig.getTarde();
        HorariosConfig.Turno sabado = horariosConfig.getSabado();

        Map<String, Object> config = new HashMap<>();
        config.put("horaAperturaMañana",  manana.getInicio());
        config.put("horaCierreMañana",    manana.getFin());
        config.put("horaAperturaTarde",   tarde.getInicio());
        config.put("horaCierreTarde",     tarde.getFin());
        config.put("horaAperturaSabado",  sabado.getInicio());
        config.put("horaCierreSabado",    sabado.getFin());
        config.put("intervaloMinutos",    horariosConfig.getIntervaloMinutos());
        config.put("horasExcluidasMañana", manana.getExcluir());
        config.put("horasExcluidasTarde",  tarde.getExcluir());
        return config;
    }

    public boolean validarConfiguracion() {
        if (!horariosConfig.isConfiguracionValida()) {
            logger.error("Configuración de horarios inválida: {}", horariosConfig);
            return false;
        }
        logger.info("Configuración de horarios válida: {}", horariosConfig);
        return true;
    }
}
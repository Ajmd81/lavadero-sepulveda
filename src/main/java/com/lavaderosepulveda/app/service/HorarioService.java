package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.config.HorariosConfig;
import com.lavaderosepulveda.app.repository.CitaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.lavaderosepulveda.app.model.enums.TipoLavado;

/**
 * Servicio especializado en la gestión de horarios disponibles
 * Separa la lógica de horarios de CitaService para mayor claridad y
 * testabilidad
 */
@Service
public class HorarioService {

    private static final Logger logger = LoggerFactory.getLogger(HorarioService.class);

    @Autowired
    private HorariosConfig horariosConfig;

    @Autowired
    private CitaRepository citaRepository;

    /**
     * Obtiene todos los horarios disponibles para una fecha específica
     *
     * @param fecha Fecha para la cual obtener horarios
     * @return Lista de horarios disponibles ordenados
     */
    public List<LocalTime> obtenerHorariosDisponibles(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }

        logger.debug("Obteniendo horarios disponibles para: {}", fecha);

        // Verificar si es domingo (cerrado)
        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            logger.debug("Domingo: negocio cerrado");
            return Collections.emptyList();
        }

        // Generar todos los horarios posibles para el día
        List<LocalTime> todosLosHorarios = generarHorariosPorDia(fecha);

        // Obtener horarios ya ocupados
        Set<LocalTime> horariosOcupados = obtenerHorariosOcupados(fecha);

        // Filtrar horarios disponibles
        List<LocalTime> horariosDisponibles = todosLosHorarios.stream()
                .filter(horario -> !horariosOcupados.contains(horario))
                .sorted()
                .collect(Collectors.toList());

        logger.debug("Horarios disponibles para {}: {}", fecha, horariosDisponibles);
        return horariosDisponibles;
    }

    /**
     * Genera todos los horarios posibles según el día de la semana
     */
    public List<LocalTime> generarHorariosPorDia(LocalDate fecha) {
        boolean esSabado = fecha.getDayOfWeek() == DayOfWeek.SATURDAY;

        if (esSabado) {
            return generarHorariosSabado();
        } else {
            return generarHorariosRegulares();
        }
    }

    /**
     * Genera horarios para días regulares (lunes a viernes)
     */
    private List<LocalTime> generarHorariosRegulares() {
        List<LocalTime> horarios = new ArrayList<>();

        HorariosConfig.Turno manana = horariosConfig.getManana();
        HorariosConfig.Turno tarde = horariosConfig.getTarde();

        // Turno mañana
        for (int hora = manana.getInicio(); hora < manana.getFin(); hora++) {
            if (!manana.isHoraExcluida(hora)) {
                horarios.add(LocalTime.of(hora, 0));
            }
        }

        // Turno tarde
        for (int hora = tarde.getInicio(); hora < tarde.getFin(); hora++) {
            if (!tarde.isHoraExcluida(hora)) {
                horarios.add(LocalTime.of(hora, 0));
            }
        }

        return horarios;
    }

    /**
     * Genera horarios para sábados
     */
    private List<LocalTime> generarHorariosSabado() {
        List<LocalTime> horarios = new ArrayList<>();

        HorariosConfig.Turno sabado = horariosConfig.getSabado();

        for (int hora = sabado.getInicio(); hora < sabado.getFin(); hora++) {
            if (!sabado.isHoraExcluida(hora)) {
                horarios.add(LocalTime.of(hora, 0));
            }
        }

        return horarios;
    }

    /**
     * Obtiene los horarios ya ocupados por citas existentes
     */
    /**
     * Obtiene los horarios ya ocupados por citas existentes
     * Considera la duración extendida de servicios de tapicería (3 horas)
     */
    private Set<LocalTime> obtenerHorariosOcupados(LocalDate fecha) {
        Set<LocalTime> horariosOcupados = new HashSet<>();
        List<com.lavaderosepulveda.app.model.Cita> citas = citaRepository.findByFecha(fecha);

        for (com.lavaderosepulveda.app.model.Cita cita : citas) {
            horariosOcupados.add(cita.getHora());

            // Si es tapicería, bloquear también las 2 horas siguientes (total 3 horas)
            com.lavaderosepulveda.app.model.enums.TipoLavado tipo = cita.getTipoLavado();
            if (tipo == com.lavaderosepulveda.app.model.enums.TipoLavado.TAPICERIA_SIN_DESMONTAR ||
                    tipo == com.lavaderosepulveda.app.model.enums.TipoLavado.TAPICERIA_DESMONTANDO) {

                horariosOcupados.add(cita.getHora().plusHours(1));
                horariosOcupados.add(cita.getHora().plusHours(2));
            }
        }

        return horariosOcupados;
    }

    /**
     * Verifica si un horario específico está disponible
     */
    public boolean esHorarioDisponible(LocalDate fecha, LocalTime hora) {
        if (fecha == null || hora == null) {
            return false;
        }

        // Verificar si el día está abierto
        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return false;
        }

        // Verificar si el horario está en los horarios válidos
        List<LocalTime> horariosValidos = generarHorariosPorDia(fecha);
        if (!horariosValidos.contains(hora)) {
            return false;
        }

        // Verificar si no está ocupado
        return !citaRepository.existsByFechaAndHora(fecha, hora);
    }

    /**
     * Obtiene el siguiente horario disponible después de uno dado
     */
    public Optional<LocalTime> siguienteHorarioDisponible(LocalDate fecha, LocalTime horaActual) {
        List<LocalTime> horariosDisponibles = obtenerHorariosDisponibles(fecha);

        return horariosDisponibles.stream()
                .filter(hora -> hora.isAfter(horaActual))
                .findFirst();
    }

    /**
     * Obtiene información de configuración de horarios para la API
     */
    public Map<String, Object> obtenerConfiguracionHorarios() {
        Map<String, Object> config = new HashMap<>();

        HorariosConfig.Turno manana = horariosConfig.getManana();
        HorariosConfig.Turno tarde = horariosConfig.getTarde();
        HorariosConfig.Turno sabado = horariosConfig.getSabado();

        config.put("horaAperturaMañana", manana.getInicio());
        config.put("horaCierreMañana", manana.getFin());
        config.put("horaAperturaTarde", tarde.getInicio());
        config.put("horaCierreTarde", tarde.getFin());
        config.put("horaAperturaSabado", sabado.getInicio());
        config.put("horaCierreSabado", sabado.getFin());
        config.put("intervaloMinutos", horariosConfig.getIntervaloMinutos());
        config.put("horasExcluidasMañana", manana.getExcluir());
        config.put("horasExcluidasTarde", tarde.getExcluir());

        return config;
    }

    /**
     * Valida que la configuración de horarios sea correcta
     */
    public boolean validarConfiguracion() {
        if (!horariosConfig.isConfiguracionValida()) {
            logger.error("Configuración de horarios inválida: {}", horariosConfig);
            return false;
        }

        logger.info("Configuración de horarios válida: {}", horariosConfig);
        return true;
    }

    /**
     * Obtiene estadísticas de ocupación para una fecha
     */
    public Map<String, Object> obtenerEstadisticasOcupacion(LocalDate fecha) {
        List<LocalTime> todosLosHorarios = generarHorariosPorDia(fecha);
        Set<LocalTime> horariosOcupados = obtenerHorariosOcupados(fecha);

        int totalHorarios = todosLosHorarios.size();
        int horariosLibres = totalHorarios - horariosOcupados.size();
        double porcentajeOcupacion = totalHorarios > 0 ? (double) horariosOcupados.size() / totalHorarios * 100 : 0.0;

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("fecha", fecha);
        estadisticas.put("totalHorarios", totalHorarios);
        estadisticas.put("horariosOcupados", horariosOcupados.size());
        estadisticas.put("horariosLibres", horariosLibres);
        estadisticas.put("porcentajeOcupacion", Math.round(porcentajeOcupacion * 100.0) / 100.0);

        return estadisticas;
    }

    /**
     * Obtiene la disponibilidad para todo un mes, devoviendo las fechas que NO
     * están disponibles
     * Filtra según el tipo de servicio (reglas especiales para tapicería)
     */
    public List<String> obtenerDiasNoDisponibles(YearMonth mes, TipoLavado tipoServicio) {
        List<String> diasNoDisponibles = new ArrayList<>();
        LocalDate fechaInicio = mes.atDay(1);
        LocalDate fechaFin = mes.atEndOfMonth();

        // Si el mes es el actual, empezar desde hoy
        if (mes.equals(YearMonth.now())) {
            LocalDate hoy = LocalDate.now();
            if (hoy.isAfter(fechaInicio)) {
                fechaInicio = hoy;
            }
        }

        // Reglas específicas para tapicería: Solo Lunes-Jueves
        boolean esTapiceria = (tipoServicio == TipoLavado.TAPICERIA_SIN_DESMONTAR ||
                tipoServicio == TipoLavado.TAPICERIA_DESMONTANDO);

        for (LocalDate fecha = fechaInicio; !fecha.isAfter(fechaFin); fecha = fecha.plusDays(1)) {
            // 1. Verificar si el negocio está cerrado (Domingos)
            if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
                diasNoDisponibles.add(fecha.toString());
                continue;
            }

            if (esTapiceria) {
                if (fecha.getDayOfWeek() == DayOfWeek.FRIDAY || fecha.getDayOfWeek() == DayOfWeek.SATURDAY) {
                    diasNoDisponibles.add(fecha.toString());
                    continue;
                }
            }

            // 3. Verificar disponibilidad real de horarios
            if (!hayHorarioDisponible(fecha, tipoServicio)) {
                diasNoDisponibles.add(fecha.toString());
            }
        }

        // Añadir días pasados del mes si empezamos a mitad
        LocalDate primerDiaMes = mes.atDay(1);
        for (LocalDate d = primerDiaMes; d.isBefore(fechaInicio); d = d.plusDays(1)) {
            diasNoDisponibles.add(d.toString());
        }

        return diasNoDisponibles;
    }

    /**
     * Verifica si hay AL MENOS UN horario disponible para la fecha y servicio dados
     */
    private boolean hayHorarioDisponible(LocalDate fecha, TipoLavado tipoServicio) {
        // Generar todos los horarios posibles para el día
        List<LocalTime> todosLosHorarios = generarHorariosPorDia(fecha);
        Set<LocalTime> horariosOcupados = obtenerHorariosOcupados(fecha);

        // Si es tapicería, necesitamos verificar bloques específicos (08:00 + 3 horas)
        if (tipoServicio == TipoLavado.TAPICERIA_SIN_DESMONTAR || tipoServicio == TipoLavado.TAPICERIA_DESMONTANDO) {
            LocalTime horaInicio = LocalTime.of(8, 0);

            // Solo se permite reserva a las 08:00
            if (!todosLosHorarios.contains(horaInicio)) {
                return false;
            }

            // Verificar que 08:00, 09:00 y 10:00 estén libres
            boolean horario08Libre = !horariosOcupados.contains(horaInicio);
            boolean horario09Libre = !horariosOcupados.contains(horaInicio.plusHours(1))
                    && esHorarioValido(fecha, horaInicio.plusHours(1));
            boolean horario10Libre = !horariosOcupados.contains(horaInicio.plusHours(2))
                    && esHorarioValido(fecha, horaInicio.plusHours(2));

            return horario08Libre && horario09Libre && horario10Libre;
        }

        // Para otros servicios, basta con que haya un hueco libre
        return todosLosHorarios.stream().anyMatch(h -> !horariosOcupados.contains(h));
    }

    private boolean esHorarioValido(LocalDate fecha, LocalTime hora) {
        List<LocalTime> horariosValidos = generarHorariosPorDia(fecha);
        return horariosValidos.contains(hora);
    }
}
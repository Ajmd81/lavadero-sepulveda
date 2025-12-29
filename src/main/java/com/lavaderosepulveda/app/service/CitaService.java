package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.dto.ClienteEstadisticaDTO;
import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.EstadoCita;
import com.lavaderosepulveda.app.model.TipoLavado;
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
     * Cambiar solo el estado de una cita sin validar horarios
     */
    public Cita cambiarEstado(Long id, EstadoCita nuevoEstado) {
        if (id == null || nuevoEstado == null) {
            throw new IllegalArgumentException("El ID y el estado no pueden ser nulos");
        }

        return citaRepository.findById(id)
                .map(cita -> {
                    cita.setEstado(nuevoEstado);
                    return citaRepository.save(cita);
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

    // ==================== MÉTODOS DE ESTADÍSTICAS ====================

    /**
     * Obtener los 10 mejores clientes del último año
     * Incluye servicio más frecuente por cada cliente
     */
    public List<ClienteEstadisticaDTO> obtenerTop10ClientesUltimoAnio() {
        LocalDate fechaHaceUnAnio = LocalDate.now().minusYears(1);
        
        // Obtener la lista base de clientes
        List<ClienteEstadisticaDTO> clientes = citaRepository
            .findTop10ClientesByReservasUltimoAnio(fechaHaceUnAnio)
            .stream()
            .limit(10)
            .collect(Collectors.toList());
        
        // Completar con el servicio más frecuente de cada cliente
        clientes.forEach(cliente -> {
            String servicioMasFrecuente = citaRepository
                .findServicioMasFrecuenteByTelefono(cliente.getTelefono(), fechaHaceUnAnio);
            
            if (servicioMasFrecuente != null) {
                cliente.setServicioMasFrecuente(
                    formatearNombreServicio(servicioMasFrecuente)
                );
            } else {
                cliente.setServicioMasFrecuente("Variado");
            }
        });
        
        return clientes;
    }
    
    /**
     * Obtener estadísticas generales del negocio en el último año
     */
    public Map<String, Object> obtenerEstadisticasGenerales() {
        LocalDate fechaHaceUnAnio = LocalDate.now().minusYears(1);
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Total de clientes únicos
        Long clientesUnicos = citaRepository.countClientesUnicos(fechaHaceUnAnio);
        estadisticas.put("clientesUnicos", clientesUnicos);
        
        // Total de reservas completadas
        Long reservasCompletadas = citaRepository.countReservasCompletadas(fechaHaceUnAnio);
        estadisticas.put("reservasCompletadas", reservasCompletadas);
        
        // Servicio más popular
        String servicioMasPopular = citaRepository.findServicioMasPopular(fechaHaceUnAnio);
        estadisticas.put("servicioMasPopular", 
            servicioMasPopular != null ? formatearNombreServicio(servicioMasPopular) : "N/A");
        
        // Promedio de reservas por cliente
        if (clientesUnicos != null && clientesUnicos > 0) {
            double promedioReservas = (double) reservasCompletadas / clientesUnicos;
            estadisticas.put("promedioReservasPorCliente", 
                String.format("%.1f", promedioReservas));
        } else {
            estadisticas.put("promedioReservasPorCliente", "0.0");
        }
        
        return estadisticas;
    }
    
    /**
     * Formatear el nombre del tipo de lavado para mostrar en la UI
     * Usa directamente el enum TipoLavado para obtener la descripción
     */
    private String formatearNombreServicio(String tipoLavado) {
        if (tipoLavado == null) return "N/A";
        
        // Intentar obtener directamente del enum
        try {
            TipoLavado tipo = TipoLavado.valueOf(tipoLavado);
            return tipo.getDescripcion();
        } catch (IllegalArgumentException e) {
            // Si no existe en el enum, hacer el formateo manual (fallback)
            return switch (tipoLavado) {
                case "LAVADO_COMPLETO_TURISMO" -> "Lavado Completo Turismo";
                case "LAVADO_INTERIOR_TURISMO" -> "Lavado Interior Turismo";
                case "LAVADO_EXTERIOR_TURISMO" -> "Lavado Exterior Turismo";
                case "LAVADO_COMPLETO_RANCHERA" -> "Lavado Completo Turismo Ranchera";
                case "LAVADO_INTERIOR_RANCHERA" -> "Lavado Interior Turismo Ranchera";
                case "LAVADO_EXTERIOR_RANCHERA" -> "Lavado Exterior Turismo Ranchera";
                case "LAVADO_COMPLETO_MONOVOLUMEN" -> "Lavado Completo Monovolumen/Todoterreno Pequeño";
                case "LAVADO_INTERIOR_MONOVOLUMEN" -> "Lavado Interior Monovolumen/Todoterreno Pequeño";
                case "LAVADO_EXTERIOR_MONOVOLUMEN" -> "Lavado Exterior Monovolumen/Todoterreno Pequeño";
                case "LAVADO_COMPLETO_TODOTERRENO" -> "Lavado Completo Todoterreno Grande";
                case "LAVADO_INTERIOR_TODOTERRENO" -> "Lavado Interior Todoterreno Grande";
                case "LAVADO_EXTERIOR_TODOTERRENO" -> "Lavado Exterior Todoterreno Grande";
                case "LAVADO_COMPLETO_FURGONETA_PEQUEÑA" -> "Lavado Completo Furgoneta Pequeña";
                case "LAVADO_INTERIOR_FURGONETA_PEQUEÑA" -> "Lavado Interior Furgoneta Pequeña";
                case "LAVADO_EXTERIOR_FURGONETA_PEQUEÑA" -> "Lavado Exterior Furgoneta Pequeña";
                case "LAVADO_COMPLETO_FURGONETA_GRANDE" -> "Lavado Completo Furgoneta Grande";
                case "LAVADO_INTERIOR_FURGONETA_GRANDE" -> "Lavado Interior Furgoneta Grande";
                case "LAVADO_EXTERIOR_FURGONETA_GRANDE" -> "Lavado Exterior Furgoneta Grande";
                case "TRATAMIENTO_OZONO" -> "Tratamiento de Ozono";
                case "ENCERADO" -> "Encerado de Vehículo a Mano";
                case "TAPICERIA_SIN_DESMONTAR" -> "Limpieza de tapicería sin desmontar asientos";
                case "TAPICERIA_DESMONTANDO" -> "Limpieza de tapicería desmontando asientos";
                default -> tipoLavado.replace("_", " ");
            };
        }
    }

    // ==================== VALIDACIONES PRIVADAS ====================

    /**
     * Validar disponibilidad del horario
     */
    private void validarDisponibilidadHorario(LocalDate fecha, LocalTime hora) {
        if (!horarioService.esHorarioDisponible(fecha, hora)) {
            throw new RuntimeException("El horario seleccionado no está disponible. " +
                    "Por favor, elija otro horario.");
        }
    }

    /**
     * Validar que la fecha sea futura
     */
    private void validarFechaFutura(LocalDate fecha) {
        if (fecha.isBefore(LocalDate.now())) {
            throw new RuntimeException("No se pueden crear citas en fechas pasadas");
        }
    }
}

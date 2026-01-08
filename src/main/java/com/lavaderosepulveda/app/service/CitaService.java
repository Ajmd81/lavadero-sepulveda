package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.dto.ClienteEstadisticaDTO;
import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.EstadoCita;
import com.lavaderosepulveda.app.model.TipoLavado;
import com.lavaderosepulveda.app.repository.CitaRepository;
import com.lavaderosepulveda.app.util.DateTimeFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final Logger log = LoggerFactory.getLogger(CitaService.class);

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

        // Validar disponibilidad del horario (pasamos la cita completa para validar
        // tipo)
        validarDisponibilidadHorario(cita);

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
                        validarDisponibilidadHorario(citaActualizada);
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
                                })));

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

    // ========================================
    // MÉTODOS ADICIONALES PARA CRM
    // ========================================

    /**
     * Obtener citas por estado
     */
    public List<Cita> obtenerCitasPorEstado(EstadoCita estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
        return citaRepository.findByEstadoOrderByFechaDescHoraDesc(estado);
    }

    /**
     * Obtener citas pendientes (PENDIENTE o CONFIRMADA)
     */
    public List<Cita> obtenerCitasPendientes() {
        return citaRepository.findCitasPendientes();
    }

    /**
     * Obtener citas completadas sin facturar
     */
    public List<Cita> obtenerCitasCompletadasSinFacturar() {
        return citaRepository.findCitasCompletadasSinFacturar();
    }

    /**
     * Obtener citas de hoy
     */
    public List<Cita> obtenerCitasDeHoy() {
        return citaRepository.findCitasDeHoy(LocalDate.now());
    }

    /**
     * Obtener citas por cliente ID
     */
    public List<Cita> obtenerCitasPorClienteId(Long clienteId) {
        if (clienteId == null) {
            throw new IllegalArgumentException("El ID del cliente no puede ser nulo");
        }
        return citaRepository.findByClienteIdOrderByFechaDescHoraDesc(clienteId);
    }

    /**
     * Cambiar estado de una cita
     */
    @Transactional
    public Cita cambiarEstado(Long id, EstadoCita nuevoEstado) {
        if (id == null || nuevoEstado == null) {
            throw new IllegalArgumentException("El ID y el estado no pueden ser nulos");
        }

        return citaRepository.findById(id)
                .map(cita -> {
                    EstadoCita estadoAnterior = cita.getEstado();
                    cita.setEstado(nuevoEstado);

                    // Registrar tiempos según el cambio de estado
                    LocalTime ahora = LocalTime.now();

                    switch (nuevoEstado) {
                        case EN_PROCESO:
                            if (cita.getHoraLlegada() == null) {
                                cita.setHoraLlegada(ahora);
                            }
                            cita.setHoraInicio(ahora);
                            break;
                        case COMPLETADA:
                            cita.setHoraFin(ahora);
                            break;
                        default:
                            break;
                    }

                    log.info("Cita {} cambió de estado: {} -> {}", id, estadoAnterior, nuevoEstado);
                    return citaRepository.save(cita);
                })
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
    }

    /**
     * Cancelar una cita
     */
    @Transactional
    public Cita cancelarCita(Long id, String motivo) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        return citaRepository.findById(id)
                .map(cita -> {
                    // Verificar que la cita puede ser cancelada
                    if (cita.getEstado() == EstadoCita.COMPLETADA) {
                        throw new RuntimeException("No se puede cancelar una cita ya completada");
                    }

                    cita.setEstado(EstadoCita.CANCELADA);
                    if (motivo != null && !motivo.trim().isEmpty()) {
                        String observacionesActuales = cita.getObservaciones() != null ? cita.getObservaciones() : "";
                        cita.setObservaciones(observacionesActuales + "\n[CANCELACIÓN] " + motivo);
                    }

                    log.info("Cita {} cancelada. Motivo: {}", id, motivo);
                    return citaRepository.save(cita);
                })
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
    }

    /**
     * Marcar cita como no presentado
     */
    @Transactional
    public Cita marcarNoPresentado(Long id) {
        return cambiarEstado(id, EstadoCita.NO_PRESENTADO);
    }

    /**
     * Confirmar cita
     */
    @Transactional
    public Cita confirmarCita(Long id) {
        return cambiarEstado(id, EstadoCita.CONFIRMADA);
    }

    /**
     * Iniciar servicio (en proceso)
     */
    @Transactional
    public Cita iniciarServicio(Long id) {
        return cambiarEstado(id, EstadoCita.EN_PROCESO);
    }

    /**
     * Completar cita
     */
    @Transactional
    public Cita completarCita(Long id) {
        return cambiarEstado(id, EstadoCita.COMPLETADA);
    }

    /**
     * Marcar cita como facturada
     */
    @Transactional
    public Cita marcarComoFacturada(Long citaId, Long facturaId) {
        return citaRepository.findById(citaId)
                .map(cita -> {
                    cita.setFacturada(true);
                    cita.setFacturaId(facturaId);
                    log.info("Cita {} marcada como facturada. Factura ID: {}", citaId, facturaId);
                    return citaRepository.save(cita);
                })
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + citaId));
    }

    /**
     * Registrar llegada del cliente
     */
    @Transactional
    public Cita registrarLlegada(Long id) {
        return citaRepository.findById(id)
                .map(cita -> {
                    cita.setHoraLlegada(LocalTime.now());
                    log.info("Llegada registrada para cita {}", id);
                    return citaRepository.save(cita);
                })
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
    }

    // ========================================
    // MÉTODOS DE CONTEO PARA DASHBOARD
    // ========================================

    /**
     * Contar citas de hoy
     */
    public long contarCitasHoy() {
        return citaRepository.countByFecha(LocalDate.now());
    }

    /**
     * Contar citas por estado
     */
    public long contarCitasPorEstado(EstadoCita estado) {
        return citaRepository.countByEstado(estado);
    }

    /**
     * Contar citas de hoy por estado
     */
    public long contarCitasHoyPorEstado(EstadoCita estado) {
        return citaRepository.countByEstadoAndFecha(estado, LocalDate.now());
    }

    /**
     * Obtener resumen de citas de hoy
     */
    public Map<String, Object> obtenerResumenCitasHoy() {
        LocalDate hoy = LocalDate.now();
        Map<String, Object> resumen = new HashMap<>();

        resumen.put("total", citaRepository.countByFecha(hoy));
        resumen.put("pendientes", citaRepository.countByEstadoAndFecha(EstadoCita.PENDIENTE, hoy));
        resumen.put("confirmadas", citaRepository.countByEstadoAndFecha(EstadoCita.CONFIRMADA, hoy));
        resumen.put("enProceso", citaRepository.countByEstadoAndFecha(EstadoCita.EN_PROCESO, hoy));
        resumen.put("completadas", citaRepository.countByEstadoAndFecha(EstadoCita.COMPLETADA, hoy));
        resumen.put("canceladas", citaRepository.countByEstadoAndFecha(EstadoCita.CANCELADA, hoy));
        resumen.put("noPresentados", citaRepository.countByEstadoAndFecha(EstadoCita.NO_PRESENTADO, hoy));

        return resumen;
    }

    /**
     * Obtener citas para recordatorio (mañana)
     */
    public List<Cita> obtenerCitasParaRecordatorio() {
        LocalDate manana = LocalDate.now().plusDays(1);
        return citaRepository.findCitasParaRecordatorio(manana);
    }

    /**
     * Marcar recordatorio como enviado
     */
    @Transactional
    public void marcarRecordatorioEnviado(Long citaId) {
        citaRepository.findById(citaId).ifPresent(cita -> {
            cita.setRecordatorioEnviado(true);
            citaRepository.save(cita);
            log.info("Recordatorio marcado como enviado para cita {}", citaId);
        });
    }

    /**
     * Marcar confirmación como enviada
     */
    @Transactional
    public void marcarConfirmacionEnviada(Long citaId) {
        citaRepository.findById(citaId).ifPresent(cita -> {
            cita.setConfirmacionEnviada(true);
            citaRepository.save(cita);
            log.info("Confirmación marcada como enviada para cita {}", citaId);
        });
    }

    /**
     * Obtener citas del mes
     */
    public List<Cita> obtenerCitasDelMes(int anio, int mes) {
        return citaRepository.findCitasByMes(anio, mes);
    }

    /**
     * Obtener citas en proceso
     */
    public List<Cita> obtenerCitasEnProceso() {
        return citaRepository.findCitasEnProceso();
    }

    // ==================== MÉTODOS DE ESTADÍSTICAS ====================

    /**
     * Obtener los 10 mejores clientes del último año
     * Incluye servicio más frecuente por cada cliente
     */
    public List<ClienteEstadisticaDTO> obtenerTop10ClientesUltimoAnio() {
        LocalDate fechaHaceUnAnio = LocalDate.now().minusYears(1);

        // Obtener la lista base de clientes (native query devuelve Object[])
        List<Object[]> resultados = citaRepository.findTop10ClientesRaw(fechaHaceUnAnio);

        // Convertir Object[] a ClienteEstadisticaDTO
        List<ClienteEstadisticaDTO> clientes = resultados.stream()
                .map(row -> new ClienteEstadisticaDTO(
                        (String) row[0], // nombre
                        (String) row[1], // telefono
                        (String) row[2], // email
                        ((Number) row[3]).longValue(), // totalReservas
                        ((Number) row[4]).doubleValue() // totalGastado
                ))
                .collect(Collectors.toList());

        // Completar con el servicio más frecuente de cada cliente
        clientes.forEach(cliente -> {
            String servicioMasFrecuente = citaRepository
                    .findServicioMasFrecuenteByTelefono(cliente.getTelefono(), fechaHaceUnAnio);

            if (servicioMasFrecuente != null) {
                cliente.setServicioMasFrecuente(
                        formatearNombreServicio(servicioMasFrecuente));
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
        if (tipoLavado == null)
            return "N/A";

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
    /**
     * Validar disponibilidad del horario y reglas de negocio
     */
    private void validarDisponibilidadHorario(Cita cita) {
        LocalDate fecha = cita.getFecha();
        LocalTime hora = cita.getHora();
        TipoLavado tipo = cita.getTipoLavado();

        // Reglas específicas para Tapicería
        if (tipo == TipoLavado.TAPICERIA_SIN_DESMONTAR || tipo == TipoLavado.TAPICERIA_DESMONTANDO) {
            // 1. Solo Lunes a Jueves
            DayOfWeek dia = fecha.getDayOfWeek();
            if (dia == DayOfWeek.FRIDAY || dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
                throw new RuntimeException(
                        "Las citas de Limpieza de Tapicería solo están disponibles de Lunes a Jueves.");
            }

            // 2. Solo a las 08:00
            if (!hora.equals(LocalTime.of(8, 0))) {
                throw new RuntimeException(
                        "Las citas de Limpieza de Tapicería solo se pueden reservar a las 08:00 (duración de 3 horas).");
            }

            // 3. Verificar bloque de 3 horas (08:00, 09:00, 10:00)
            // Verificar si 09:00 está libre
            if (!horarioService.esHorarioDisponible(fecha, hora.plusHours(1))) {
                throw new RuntimeException(
                        "No hay disponibilidad suficiente para las 3 horas requeridas. El horario de las 09:00 está ocupado.");
            }
            // Verificar si 10:00 está libre
            if (!horarioService.esHorarioDisponible(fecha, hora.plusHours(2))) {
                throw new RuntimeException(
                        "No hay disponibilidad suficiente para las 3 horas requeridas. El horario de las 10:00 está ocupado.");
            }
        }

        // Validación estándar de disponibilidad
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
package com.lavaderosepulveda.app.repository;

import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Métodos existentes
    List<Cita> findByFecha(LocalDate fecha);
    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);
    List<Cita> findByTelefono(String telefono);

    @Query("SELECT c FROM Cita c WHERE c.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY c.fecha, c.hora")
    List<Cita> findCitasBetweenDates(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    // ========================================
    // MÉTODOS ADICIONALES PARA CRM
    // ========================================

    /**
     * Buscar citas por estado
     */
    List<Cita> findByEstado(EstadoCita estado);

    /**
     * Buscar citas por estado ordenadas por fecha y hora
     */
    List<Cita> findByEstadoOrderByFechaDescHoraDesc(EstadoCita estado);

    /**
     * Buscar citas por cliente ID
     */
    List<Cita> findByClienteIdOrderByFechaDescHoraDesc(Long clienteId);

    /**
     * Buscar citas pendientes (no completadas ni canceladas)
     */
    @Query("SELECT c FROM Cita c WHERE c.estado IN ('PENDIENTE', 'CONFIRMADA') ORDER BY c.fecha, c.hora")
    List<Cita> findCitasPendientes();

    /**
     * Buscar citas completadas sin facturar
     */
    @Query("SELECT c FROM Cita c WHERE c.estado = 'COMPLETADA' AND (c.facturada = false OR c.facturada IS NULL) ORDER BY c.fecha DESC")
    List<Cita> findCitasCompletadasSinFacturar();

    /**
     * Contar citas por fecha
     */
    long countByFecha(LocalDate fecha);

    /**
     * Contar citas por estado
     */
    long countByEstado(EstadoCita estado);

    /**
     * Contar citas por estado y fecha
     */
    long countByEstadoAndFecha(EstadoCita estado, LocalDate fecha);

    /**
     * Buscar citas de hoy ordenadas por hora
     */
    @Query("SELECT c FROM Cita c WHERE c.fecha = :fecha ORDER BY c.hora")
    List<Cita> findCitasDeHoy(@Param("fecha") LocalDate fecha);

    /**
     * Buscar citas para recordatorio (mañana, no enviado)
     */
    @Query("SELECT c FROM Cita c WHERE c.fecha = :fechaManana AND (c.recordatorioEnviado = false OR c.recordatorioEnviado IS NULL) AND c.estado IN ('PENDIENTE', 'CONFIRMADA')")
    List<Cita> findCitasParaRecordatorio(@Param("fechaManana") LocalDate fechaManana);

    /**
     * Buscar citas por fecha y estado
     */
    List<Cita> findByFechaAndEstado(LocalDate fecha, EstadoCita estado);

    /**
     * Buscar citas en proceso
     */
    @Query("SELECT c FROM Cita c WHERE c.estado = 'EN_PROCESO' ORDER BY c.horaInicio")
    List<Cita> findCitasEnProceso();

    /**
     * Buscar citas del mes actual
     */
    @Query("SELECT c FROM Cita c WHERE YEAR(c.fecha) = :anio AND MONTH(c.fecha) = :mes ORDER BY c.fecha, c.hora")
    List<Cita> findCitasByMes(@Param("anio") int anio, @Param("mes") int mes);

    /**
     * Contar citas del mes por estado
     */
    @Query("SELECT COUNT(c) FROM Cita c WHERE YEAR(c.fecha) = :anio AND MONTH(c.fecha) = :mes AND c.estado = :estado")
    long countCitasByMesAndEstado(@Param("anio") int anio, @Param("mes") int mes, @Param("estado") EstadoCita estado);

    // ==================== MÉTODOS PARA ESTADÍSTICAS (Native Queries) ====================

    /**
     * Obtener los 10 clientes con más reservas en el último año
     * Usa native query porque tipoLavado es un enum
     */
    @Query(value = """
        SELECT 
            c.nombre as nombre,
            c.telefono as telefono,
            c.email as email,
            COUNT(*) as totalReservas,
            SUM(CASE 
                WHEN c.tipo_lavado = 'LAVADO_COMPLETO_TURISMO' THEN 23.0
                WHEN c.tipo_lavado = 'LAVADO_INTERIOR_TURISMO' THEN 16.0
                WHEN c.tipo_lavado = 'LAVADO_EXTERIOR_TURISMO' THEN 12.0
                WHEN c.tipo_lavado = 'LAVADO_COMPLETO_RANCHERA' THEN 26.0
                WHEN c.tipo_lavado = 'LAVADO_INTERIOR_RANCHERA' THEN 18.0
                WHEN c.tipo_lavado = 'LAVADO_EXTERIOR_RANCHERA' THEN 13.0
                WHEN c.tipo_lavado = 'LAVADO_COMPLETO_MONOVOLUMEN' THEN 28.0
                WHEN c.tipo_lavado = 'LAVADO_INTERIOR_MONOVOLUMEN' THEN 19.0
                WHEN c.tipo_lavado = 'LAVADO_EXTERIOR_MONOVOLUMEN' THEN 14.0
                WHEN c.tipo_lavado = 'LAVADO_COMPLETO_TODOTERRENO' THEN 31.0
                WHEN c.tipo_lavado = 'LAVADO_INTERIOR_TODOTERRENO' THEN 20.0
                WHEN c.tipo_lavado = 'LAVADO_EXTERIOR_TODOTERRENO' THEN 16.0
                WHEN c.tipo_lavado = 'LAVADO_COMPLETO_FURGONETA_PEQUEÑA' THEN 30.0
                WHEN c.tipo_lavado = 'LAVADO_INTERIOR_FURGONETA_PEQUEÑA' THEN 20.0
                WHEN c.tipo_lavado = 'LAVADO_EXTERIOR_FURGONETA_PEQUEÑA' THEN 15.0
                WHEN c.tipo_lavado = 'LAVADO_COMPLETO_FURGONETA_GRANDE' THEN 35.0
                WHEN c.tipo_lavado = 'LAVADO_INTERIOR_FURGONETA_GRANDE' THEN 25.0
                WHEN c.tipo_lavado = 'LAVADO_EXTERIOR_FURGONETA_GRANDE' THEN 20.0
                WHEN c.tipo_lavado = 'TRATAMIENTO_OZONO' THEN 15.0
                WHEN c.tipo_lavado = 'ENCERADO' THEN 25.0
                WHEN c.tipo_lavado = 'TAPICERIA_SIN_DESMONTAR' THEN 100.0
                WHEN c.tipo_lavado = 'TAPICERIA_DESMONTANDO' THEN 150.0
                ELSE 23.0
            END) as totalGastado
        FROM citas c
        WHERE c.fecha >= :fechaInicio
        AND c.estado IN ('CONFIRMADA', 'COMPLETADA')
        GROUP BY c.telefono, c.nombre, c.email
        ORDER BY COUNT(*) DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> findTop10ClientesRaw(@Param("fechaInicio") LocalDate fechaInicio);

    /**
     * Obtener el servicio más frecuente de un cliente por teléfono
     */
    @Query(value = """
        SELECT c.tipo_lavado
        FROM citas c
        WHERE c.telefono = :telefono
        AND c.fecha >= :fechaInicio
        AND c.estado IN ('CONFIRMADA', 'COMPLETADA')
        GROUP BY c.tipo_lavado
        ORDER BY COUNT(*) DESC
        LIMIT 1
        """, nativeQuery = true)
    String findServicioMasFrecuenteByTelefono(
            @Param("telefono") String telefono,
            @Param("fechaInicio") LocalDate fechaInicio
    );

    /**
     * Obtener estadísticas generales del último año
     */
    @Query(value = """
        SELECT COUNT(DISTINCT c.telefono)
        FROM citas c
        WHERE c.fecha >= :fechaInicio
        AND c.estado IN ('CONFIRMADA', 'COMPLETADA')
        """, nativeQuery = true)
    Long countClientesUnicos(@Param("fechaInicio") LocalDate fechaInicio);

    /**
     * Obtener total de reservas completadas en el último año
     */
    @Query(value = """
        SELECT COUNT(*)
        FROM citas c
        WHERE c.fecha >= :fechaInicio
        AND c.estado IN ('CONFIRMADA', 'COMPLETADA')
        """, nativeQuery = true)
    Long countReservasCompletadas(@Param("fechaInicio") LocalDate fechaInicio);

    /**
     * Obtener el servicio más popular del último año
     */
    @Query(value = """
        SELECT c.tipo_lavado
        FROM citas c
        WHERE c.fecha >= :fechaInicio
        AND c.estado IN ('CONFIRMADA', 'COMPLETADA')
        GROUP BY c.tipo_lavado
        ORDER BY COUNT(*) DESC
        LIMIT 1
        """, nativeQuery = true)
    String findServicioMasPopular(@Param("fechaInicio") LocalDate fechaInicio);
}
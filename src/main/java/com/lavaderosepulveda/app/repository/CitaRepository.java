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

    // ========================================
    // MÉTODOS BÁSICOS
    // ========================================

    List<Cita> findByFecha(LocalDate fecha);

    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);

    List<Cita> findByTelefono(String telefono);

    @Query("SELECT c FROM Cita c WHERE c.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY c.fecha, c.hora")
    List<Cita> findCitasBetweenDates(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    // ========================================
    // MÉTODOS PARA CRM
    // ========================================

    List<Cita> findByEstado(EstadoCita estado);

    List<Cita> findByEstadoOrderByFechaDescHoraDesc(EstadoCita estado);

    List<Cita> findByClienteIdOrderByFechaDescHoraDesc(Long clienteId);

    List<Cita> findByClienteIdOrderByFechaDesc(Long clienteId);

    List<Cita> findByFechaAndEstado(LocalDate fecha, EstadoCita estado);

    // ✅ CORREGIDO: enum literal en lugar de string
    @Query("SELECT c FROM Cita c WHERE c.estado IN (" +
           "com.lavaderosepulveda.app.model.enums.EstadoCita.PENDIENTE, " +
           "com.lavaderosepulveda.app.model.enums.EstadoCita.CONFIRMADA) " +
           "ORDER BY c.fecha, c.hora")
    List<Cita> findCitasPendientes();

    // ✅ CORREGIDO: enum literal en lugar de string
    @Query("SELECT c FROM Cita c WHERE c.estado = com.lavaderosepulveda.app.model.enums.EstadoCita.COMPLETADA " +
           "AND (c.facturada = false OR c.facturada IS NULL) ORDER BY c.fecha DESC")
    List<Cita> findCitasCompletadasSinFacturar();

    // ✅ CORREGIDO: enum literal en lugar de string
    @Query("SELECT c FROM Cita c WHERE c.estado = com.lavaderosepulveda.app.model.enums.EstadoCita.EN_PROCESO " +
           "ORDER BY c.horaInicio")
    List<Cita> findCitasEnProceso();

    // ✅ CORREGIDO: enum literal en lugar de string
    @Query("SELECT c FROM Cita c WHERE c.fecha = :fechaManana " +
           "AND (c.recordatorioEnviado = false OR c.recordatorioEnviado IS NULL) " +
           "AND c.estado IN (" +
           "com.lavaderosepulveda.app.model.enums.EstadoCita.PENDIENTE, " +
           "com.lavaderosepulveda.app.model.enums.EstadoCita.CONFIRMADA)")
    List<Cita> findCitasParaRecordatorio(@Param("fechaManana") LocalDate fechaManana);

    @Query("SELECT c FROM Cita c WHERE c.fecha = :fecha ORDER BY c.hora")
    List<Cita> findCitasDeHoy(@Param("fecha") LocalDate fecha);

    @Query("SELECT c FROM Cita c WHERE YEAR(c.fecha) = :anio AND MONTH(c.fecha) = :mes ORDER BY c.fecha, c.hora")
    List<Cita> findCitasByMes(@Param("anio") int anio, @Param("mes") int mes);

    // ========================================
    // CONTEOS
    // ========================================

    long countByFecha(LocalDate fecha);

    long countByEstado(EstadoCita estado);

    long countByEstadoAndFecha(EstadoCita estado, LocalDate fecha);

    @Query("SELECT COUNT(c) FROM Cita c WHERE YEAR(c.fecha) = :anio AND MONTH(c.fecha) = :mes AND c.estado = :estado")
    long countCitasByMesAndEstado(
            @Param("anio") int anio,
            @Param("mes") int mes,
            @Param("estado") EstadoCita estado
    );

    // ========================================
    // ESTADÍSTICAS (Native Queries)
    // ========================================

    @Query(value = """
            SELECT
                c.nombre     AS nombre,
                c.telefono   AS telefono,
                c.email      AS email,
                COUNT(*)     AS totalReservas,
                SUM(CASE
                    WHEN c.tipo_lavado = 'LAVADO_COMPLETO_TURISMO'           THEN 25.0
                    WHEN c.tipo_lavado = 'LAVADO_INTERIOR_TURISMO'           THEN 17.0
                    WHEN c.tipo_lavado = 'LAVADO_EXTERIOR_TURISMO'           THEN 13.0
                    WHEN c.tipo_lavado = 'LAVADO_COMPLETO_RANCHERA'          THEN 26.0
                    WHEN c.tipo_lavado = 'LAVADO_INTERIOR_RANCHERA'          THEN 18.0
                    WHEN c.tipo_lavado = 'LAVADO_EXTERIOR_RANCHERA'          THEN 13.0
                    WHEN c.tipo_lavado = 'LAVADO_COMPLETO_MONOVOLUMEN'       THEN 30.0
                    WHEN c.tipo_lavado = 'LAVADO_INTERIOR_MONOVOLUMEN'       THEN 20.0
                    WHEN c.tipo_lavado = 'LAVADO_EXTERIOR_MONOVOLUMEN'       THEN 15.0
                    WHEN c.tipo_lavado = 'LAVADO_COMPLETO_TODOTERRENO'       THEN 35.0
                    WHEN c.tipo_lavado = 'LAVADO_INTERIOR_TODOTERRENO'       THEN 22.0
                    WHEN c.tipo_lavado = 'LAVADO_EXTERIOR_TODOTERRENO'       THEN 18.0
                    WHEN c.tipo_lavado = 'LAVADO_COMPLETO_FURGONETA_PEQUEÑA' THEN 30.0
                    WHEN c.tipo_lavado = 'LAVADO_INTERIOR_FURGONETA_PEQUEÑA' THEN 20.0
                    WHEN c.tipo_lavado = 'LAVADO_EXTERIOR_FURGONETA_PEQUEÑA' THEN 15.0
                    WHEN c.tipo_lavado = 'LAVADO_COMPLETO_FURGONETA_GRANDE'  THEN 35.0
                    WHEN c.tipo_lavado = 'LAVADO_INTERIOR_FURGONETA_GRANDE'  THEN 25.0
                    WHEN c.tipo_lavado = 'LAVADO_EXTERIOR_FURGONETA_GRANDE'  THEN 20.0
                    WHEN c.tipo_lavado = 'TRATAMIENTO_OZONO'                 THEN 15.0
                    WHEN c.tipo_lavado = 'ENCERADO'                          THEN 25.0
                    WHEN c.tipo_lavado = 'TAPICERIA_SIN_DESMONTAR'           THEN 100.0
                    WHEN c.tipo_lavado = 'TAPICERIA_DESMONTANDO'             THEN 150.0
                    ELSE 25.0
                END) AS totalGastado
            FROM citas c
            WHERE c.fecha >= :fechaInicio
              AND c.estado IN ('CONFIRMADA', 'COMPLETADA')
            GROUP BY c.telefono, c.nombre, c.email
            ORDER BY COUNT(*) DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Object[]> findTop10ClientesRaw(@Param("fechaInicio") LocalDate fechaInicio);

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

    @Query(value = """
            SELECT COUNT(DISTINCT c.telefono)
            FROM citas c
            WHERE c.fecha >= :fechaInicio
              AND c.estado IN ('CONFIRMADA', 'COMPLETADA')
            """, nativeQuery = true)
    Long countClientesUnicos(@Param("fechaInicio") LocalDate fechaInicio);

    @Query(value = """
            SELECT COUNT(*)
            FROM citas c
            WHERE c.fecha >= :fechaInicio
              AND c.estado IN ('CONFIRMADA', 'COMPLETADA')
            """, nativeQuery = true)
    Long countReservasCompletadas(@Param("fechaInicio") LocalDate fechaInicio);

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
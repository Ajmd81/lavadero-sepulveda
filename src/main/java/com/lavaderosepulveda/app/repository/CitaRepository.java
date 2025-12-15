package com.lavaderosepulveda.app.repository;

import com.lavaderosepulveda.app.dto.ClienteEstadisticaDTO;
import com.lavaderosepulveda.app.model.Cita;
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

    // ==================== NUEVOS MÉTODOS PARA ESTADÍSTICAS ====================
    
    /**
     * Obtener los 10 clientes con más reservas en el último año
     * PRECIOS EXACTOS según tu enum TipoLavado
     */
    @Query("""
        SELECT new com.lavaderosepulveda.app.dto.ClienteEstadisticaDTO(
            c.nombre,
            c.telefono,
            c.email,
            COUNT(c),
            SUM(CASE 
                WHEN c.tipoLavado = 'LAVADO_COMPLETO_TURISMO' THEN 23.0
                WHEN c.tipoLavado = 'LAVADO_INTERIOR_TURISMO' THEN 16.0
                WHEN c.tipoLavado = 'LAVADO_EXTERIOR_TURISMO' THEN 12.0
                WHEN c.tipoLavado = 'LAVADO_COMPLETO_RANCHERA' THEN 26.0
                WHEN c.tipoLavado = 'LAVADO_INTERIOR_RANCHERA' THEN 18.0
                WHEN c.tipoLavado = 'LAVADO_EXTERIOR_RANCHERA' THEN 13.0
                WHEN c.tipoLavado = 'LAVADO_COMPLETO_MONOVOLUMEN' THEN 28.0
                WHEN c.tipoLavado = 'LAVADO_INTERIOR_MONOVOLUMEN' THEN 19.0
                WHEN c.tipoLavado = 'LAVADO_EXTERIOR_MONOVOLUMEN' THEN 14.0
                WHEN c.tipoLavado = 'LAVADO_COMPLETO_TODOTERRENO' THEN 31.0
                WHEN c.tipoLavado = 'LAVADO_INTERIOR_TODOTERRENO' THEN 20.0
                WHEN c.tipoLavado = 'LAVADO_EXTERIOR_TODOTERRENO' THEN 16.0
                WHEN c.tipoLavado = 'LAVADO_COMPLETO_FURGONETA_PEQUEÑA' THEN 30.0
                WHEN c.tipoLavado = 'LAVADO_INTERIOR_FURGONETA_PEQUEÑA' THEN 20.0
                WHEN c.tipoLavado = 'LAVADO_EXTERIOR_FURGONETA_PEQUEÑA' THEN 15.0
                WHEN c.tipoLavado = 'LAVADO_COMPLETO_FURGONETA_GRANDE' THEN 35.0
                WHEN c.tipoLavado = 'LAVADO_INTERIOR_FURGONETA_GRANDE' THEN 25.0
                WHEN c.tipoLavado = 'LAVADO_EXTERIOR_FURGONETA_GRANDE' THEN 20.0
                WHEN c.tipoLavado = 'TRATAMIENTO_OZONO' THEN 15.0
                WHEN c.tipoLavado = 'ENCERADO' THEN 25.0
                WHEN c.tipoLavado = 'TAPICERIA_SIN_DESMONTAR' THEN 100.0
                WHEN c.tipoLavado = 'TAPICERIA_DESMONTANDO' THEN 150.0
                ELSE 23.0
            END)
        )
        FROM Cita c
        WHERE c.fecha >= :fechaInicio
        AND c.estado IN ('CONFIRMADA', 'COMPLETADA')
        GROUP BY c.telefono, c.nombre, c.email
        ORDER BY COUNT(c) DESC
        """)
    List<ClienteEstadisticaDTO> findTop10ClientesByReservasUltimoAnio(
        @Param("fechaInicio") LocalDate fechaInicio
    );
    
    /**
     * Obtener el servicio más frecuente de un cliente por teléfono
     */
    @Query("""
        SELECT c.tipoLavado
        FROM Cita c
        WHERE c.telefono = :telefono
        AND c.fecha >= :fechaInicio
        AND c.estado IN ('CONFIRMADA', 'COMPLETADA')
        GROUP BY c.tipoLavado
        ORDER BY COUNT(c.tipoLavado) DESC
        LIMIT 1
        """)
    String findServicioMasFrecuenteByTelefono(
        @Param("telefono") String telefono,
        @Param("fechaInicio") LocalDate fechaInicio
    );
    
    /**
     * Obtener estadísticas generales del último año
     */
    @Query("""
        SELECT COUNT(DISTINCT c.telefono)
        FROM Cita c
        WHERE c.fecha >= :fechaInicio
        AND c.estado IN ('CONFIRMADA', 'COMPLETADA')
        """)
    Long countClientesUnicos(@Param("fechaInicio") LocalDate fechaInicio);
    
    /**
     * Obtener total de reservas completadas en el último año
     */
    @Query("""
        SELECT COUNT(c)
        FROM Cita c
        WHERE c.fecha >= :fechaInicio
        AND c.estado IN ('CONFIRMADA', 'COMPLETADA')
        """)
    Long countReservasCompletadas(@Param("fechaInicio") LocalDate fechaInicio);
    
    /**
     * Obtener el servicio más popular del último año
     */
    @Query("""
        SELECT c.tipoLavado
        FROM Cita c
        WHERE c.fecha >= :fechaInicio
        AND c.estado IN ('CONFIRMADA', 'COMPLETADA')
        GROUP BY c.tipoLavado
        ORDER BY COUNT(c.tipoLavado) DESC
        LIMIT 1
        """)
    String findServicioMasPopular(@Param("fechaInicio") LocalDate fechaInicio);
}

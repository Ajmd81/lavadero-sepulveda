package com.lavaderosepulveda.app.repository;

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

    // Buscar citas por fecha
    List<Cita> findByFecha(LocalDate fecha);

    // Buscar citas por fecha y hora (para verificar disponibilidad)
    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);

    // Buscar citas por teléfono (para buscar historial de un cliente)
    List<Cita> findByTelefono(String telefono);

    // Buscar citas para un rango de fechas
    @Query("SELECT c FROM Cita c WHERE c.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY c.fecha, c.hora")
    List<Cita> findCitasBetweenDates(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
}

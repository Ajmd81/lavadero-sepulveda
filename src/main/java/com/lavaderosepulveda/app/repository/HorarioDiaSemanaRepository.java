package com.lavaderosepulveda.app.repository;

import com.lavaderosepulveda.app.model.HorarioDiaSemana;
import com.lavaderosepulveda.app.model.enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para HorarioDiaSemana
 */
@Repository
public interface HorarioDiaSemanaRepository extends JpaRepository<HorarioDiaSemana, Long> {

    /**
     * Obtiene el horario de un día específico
     */
    Optional<HorarioDiaSemana> findByDiaSemana(DiaSemana diaSemana);

    /**
     * Obtiene todos los horarios ordenados por día de la semana
     */
    List<HorarioDiaSemana> findAllByOrderByDiaSemanaAsc();

    /**
     * Obtiene solo los días activos
     */
    List<HorarioDiaSemana> findByActivoTrue();
}
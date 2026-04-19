package com.lavaderosepulveda.app.repository;

import com.lavaderosepulveda.app.model.DeclaracionFiscal;
import com.lavaderosepulveda.app.model.enums.EstadoDeclaracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeclaracionFiscalRepository extends JpaRepository<DeclaracionFiscal, Long> {

    /** Todas las declaraciones, ordenadas por año y período desc (más reciente primero). */
    List<DeclaracionFiscal> findAllByOrderByEjercicioDescPeriodoDesc();

    /** Filtra por modelo: "303" o "130". */
    List<DeclaracionFiscal> findByModeloOrderByEjercicioDescPeriodoDesc(String modelo);

    /** Filtra por estado: GENERADO o PRESENTADO. */
    List<DeclaracionFiscal> findByEstadoOrderByEjercicioDescPeriodoDesc(EstadoDeclaracion estado);

    /** Busca una declaración concreta (clave natural: modelo + ejercicio + período). */
    Optional<DeclaracionFiscal> findByModeloAndEjercicioAndPeriodo(
            String modelo, Integer ejercicio, String periodo);

    /** Comprueba si ya existe una declaración para ese trimestre y modelo. */
    boolean existsByModeloAndEjercicioAndPeriodo(
            String modelo, Integer ejercicio, String periodo);
}

package com.lavaderosepulveda.app.repository;

import com.lavaderosepulveda.app.model.DeclaracionFiscal;
import com.lavaderosepulveda.app.model.enums.EstadoDeclaracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para el historial de declaraciones fiscales.
 *
 * IMPORTANTE: Los métodos que ordenan por "periodo" usan @Query explícita
 * porque "PERIOD" es palabra reservada en SQL/Hibernate 6 y Spring Data
 * falla al derivar la query automáticamente del nombre del método.
 */
@Repository
public interface DeclaracionFiscalRepository extends JpaRepository<DeclaracionFiscal, Long> {

        /** Todas las declaraciones, ordenadas por año desc y período desc. */
        @Query("SELECT d FROM DeclaracionFiscal d ORDER BY d.ejercicio DESC, d.periodo DESC")
        List<DeclaracionFiscal> findAllOrdenadas();

        /** Filtra por modelo: "303" o "130". */
        @Query("SELECT d FROM DeclaracionFiscal d WHERE d.modelo = :modelo ORDER BY d.ejercicio DESC, d.periodo DESC")
        List<DeclaracionFiscal> findByModelo(@Param("modelo") String modelo);

        /** Filtra por estado: GENERADO o PRESENTADO. */
        @Query("SELECT d FROM DeclaracionFiscal d WHERE d.estado = :estado ORDER BY d.ejercicio DESC, d.periodo DESC")
        List<DeclaracionFiscal> findByEstado(@Param("estado") EstadoDeclaracion estado);

        /**
         * Busca una declaración concreta por clave natural: modelo + ejercicio +
         * período.
         */
        @Query("SELECT d FROM DeclaracionFiscal d WHERE d.modelo = :modelo AND d.ejercicio = :ejercicio AND d.periodo = :periodo")
        Optional<DeclaracionFiscal> findByClaveNatural(
                        @Param("modelo") String modelo,
                        @Param("ejercicio") Integer ejercicio,
                        @Param("periodo") String periodo);

        /** Comprueba si ya existe una declaración para ese modelo, año y trimestre. */
        @Query("SELECT COUNT(d) > 0 FROM DeclaracionFiscal d WHERE d.modelo = :modelo AND d.ejercicio = :ejercicio AND d.periodo = :periodo")
        boolean existsByClaveNatural(
                        @Param("modelo") String modelo,
                        @Param("ejercicio") Integer ejercicio,
                        @Param("periodo") String periodo);
}
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
 * NOTA Hibernate 6: todos los métodos usan @Query explícita.
 * - "periodo" / "PERIOD" es palabra reservada en SQL — no se puede usar
 * en nombres de métodos derivados de Spring Data.
 * - COUNT(d) > 0 no es JPQL válido — se devuelve Long y se compara en Java.
 * - Ningún método usa ordenación derivada del nombre (OrderBy en el método).
 */
@Repository
public interface DeclaracionFiscalRepository extends JpaRepository<DeclaracionFiscal, Long> {

        @Query("SELECT d FROM DeclaracionFiscal d ORDER BY d.ejercicio DESC, d.periodo DESC")
        List<DeclaracionFiscal> findAllOrdenadas();

        @Query("SELECT d FROM DeclaracionFiscal d WHERE d.modelo = :modelo ORDER BY d.ejercicio DESC, d.periodo DESC")
        List<DeclaracionFiscal> findByModelo(@Param("modelo") String modelo);

        @Query("SELECT d FROM DeclaracionFiscal d WHERE d.estado = :estado ORDER BY d.ejercicio DESC, d.periodo DESC")
        List<DeclaracionFiscal> findByEstado(@Param("estado") EstadoDeclaracion estado);

        @Query("SELECT d FROM DeclaracionFiscal d WHERE d.modelo = :modelo AND d.ejercicio = :ejercicio AND d.periodo = :periodo")
        Optional<DeclaracionFiscal> findByClaveNatural(
                        @Param("modelo") String modelo,
                        @Param("ejercicio") Integer ejercicio,
                        @Param("periodo") String periodo);

        @Query("SELECT COUNT(d) FROM DeclaracionFiscal d WHERE d.modelo = :modelo AND d.ejercicio = :ejercicio AND d.periodo = :periodo")
        Long countByClaveNatural(
                        @Param("modelo") String modelo,
                        @Param("ejercicio") Integer ejercicio,
                        @Param("periodo") String periodo);
}
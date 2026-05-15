package com.lavaderosepulveda.app.repository;

import com.lavaderosepulveda.app.model.DiaCerrado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaCerradoRepository extends JpaRepository<DiaCerrado, Long> {

    Optional<DiaCerrado> findByFecha(LocalDate fecha);

    boolean existsByFecha(LocalDate fecha);

    @Query("SELECT d FROM DiaCerrado d WHERE d.fecha BETWEEN :inicio AND :fin ORDER BY d.fecha")
    List<DiaCerrado> findByRango(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    /** Solo las fechas (usado por la web pública de reservas) */
    @Query("SELECT d.fecha FROM DiaCerrado d WHERE d.fecha BETWEEN :inicio AND :fin ORDER BY d.fecha")
    List<LocalDate> findFechasByRango(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);
}
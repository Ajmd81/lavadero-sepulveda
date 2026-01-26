package com.lavaderosepulveda.app.repository;

import com.lavaderosepulveda.app.model.enums.CategoriaGasto;
import com.lavaderosepulveda.app.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface GastoRepository extends JpaRepository<Gasto, Long> {

       List<Gasto> findByOrderByFechaDesc();

       List<Gasto> findByCategoriaOrderByFechaDesc(CategoriaGasto categoria);

       List<Gasto> findByRecurrenteTrueOrderByConceptoAsc();

       // Gastos por rango de fechas
       @Query("SELECT g FROM Gasto g WHERE g.fecha BETWEEN :inicio AND :fin ORDER BY g.fecha DESC")
       List<Gasto> findByFechaBetween(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

       // Búsqueda general
       @Query("SELECT g FROM Gasto g WHERE LOWER(g.concepto) LIKE LOWER(CONCAT('%', :termino, '%'))")
       List<Gasto> buscar(@Param("termino") String termino);

       // Total por categoría en un período
       @Query("SELECT SUM(g.importe) FROM Gasto g WHERE g.categoria = :categoria AND g.fecha BETWEEN :inicio AND :fin")
       BigDecimal totalPorCategoria(@Param("categoria") CategoriaGasto categoria, @Param("inicio") LocalDate inicio,
                     @Param("fin") LocalDate fin);

       // Total por período
       @Query("SELECT SUM(g.importe) FROM Gasto g WHERE g.fecha BETWEEN :inicio AND :fin")
       BigDecimal totalPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

       // Resumen por categoría
       @Query("SELECT g.categoria, COUNT(g), SUM(g.importe) FROM Gasto g " +
                     "WHERE g.fecha BETWEEN :inicio AND :fin GROUP BY g.categoria ORDER BY SUM(g.importe) DESC")
       List<Object[]> resumenPorCategoria(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

       // Gastos del mes actual
       @Query("SELECT g FROM Gasto g WHERE YEAR(g.fecha) = :year AND MONTH(g.fecha) = :month ORDER BY g.fecha DESC")
       List<Gasto> findByMes(@Param("year") int year, @Param("month") int month);

       // Total base imponible por período (para IVA soportado)
       @Query("SELECT SUM(g.baseImponible) FROM Gasto g WHERE g.fecha BETWEEN :inicio AND :fin")
       BigDecimal totalBasePorPeriodo(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

       // Total IVA soportado por período
       @Query("SELECT SUM(g.cuotaIva) FROM Gasto g WHERE g.fecha BETWEEN :inicio AND :fin")
       BigDecimal totalIvaSoportado(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

       // Gastos sin pagar
       @Query("SELECT g FROM Gasto g WHERE g.pagado = false ORDER BY g.fecha ASC")
       List<Gasto> findPendientesPago();

       // Gastos recurrentes para un día específico
       @Query("SELECT g FROM Gasto g WHERE g.recurrente = true AND g.diaRecurrencia = :dia")
       List<Gasto> findRecurrentesPorDia(@Param("dia") Integer dia);

       // Evolución mensual
       @Query("SELECT YEAR(g.fecha), MONTH(g.fecha), SUM(g.importe) FROM Gasto g " +
                     "WHERE g.fecha BETWEEN :inicio AND :fin GROUP BY YEAR(g.fecha), MONTH(g.fecha) ORDER BY YEAR(g.fecha), MONTH(g.fecha)")
       List<Object[]> evolucionMensual(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
}

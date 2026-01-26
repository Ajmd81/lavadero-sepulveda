package com.lavaderosepulveda.app.repository;

import com.lavaderosepulveda.app.model.enums.CategoriaGasto;
import com.lavaderosepulveda.app.model.enums.EstadoFactura;
import com.lavaderosepulveda.app.model.FacturaRecibida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FacturaRecibidaRepository extends JpaRepository<FacturaRecibida, Long> {

       List<FacturaRecibida> findByOrderByFechaFacturaDesc();

       List<FacturaRecibida> findByEstadoOrderByFechaFacturaDesc(EstadoFactura estado);

       List<FacturaRecibida> findByProveedorIdOrderByFechaFacturaDesc(Long proveedorId);

       List<FacturaRecibida> findByCategoriaOrderByFechaFacturaDesc(CategoriaGasto categoria);

       // Facturas pendientes de pago
       @Query("SELECT f FROM FacturaRecibida f WHERE f.estado = 'PENDIENTE' ORDER BY f.fechaVencimiento ASC")
       List<FacturaRecibida> findPendientes();

       // Facturas vencidas sin pagar
       @Query("SELECT f FROM FacturaRecibida f WHERE f.estado = 'PENDIENTE' AND f.fechaVencimiento < :hoy ORDER BY f.fechaVencimiento ASC")
       List<FacturaRecibida> findVencidas(@Param("hoy") LocalDate hoy);

       // Facturas por rango de fechas
       @Query("SELECT f FROM FacturaRecibida f WHERE f.fechaFactura BETWEEN :inicio AND :fin ORDER BY f.fechaFactura DESC")
       List<FacturaRecibida> findByFechaFacturaBetween(@Param("inicio") LocalDate inicio,
                     @Param("fin") LocalDate fin);

       // Búsqueda general
       @Query("SELECT f FROM FacturaRecibida f WHERE " +
                     "LOWER(f.numeroFactura) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
                     "LOWER(f.proveedorNombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
                     "LOWER(f.concepto) LIKE LOWER(CONCAT('%', :termino, '%'))")
       List<FacturaRecibida> buscar(@Param("termino") String termino);

       // Total por categoría en un período
       @Query("SELECT SUM(f.total) FROM FacturaRecibida f WHERE f.categoria = :categoria AND f.fechaFactura BETWEEN :inicio AND :fin")
       BigDecimal totalPorCategoria(@Param("categoria") CategoriaGasto categoria, @Param("inicio") LocalDate inicio,
                     @Param("fin") LocalDate fin);

       // Total por período
       @Query("SELECT SUM(f.total) FROM FacturaRecibida f WHERE f.fechaFactura BETWEEN :inicio AND :fin")
       BigDecimal totalPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

       // Total IVA soportado por período
       @Query("SELECT SUM(f.cuotaIva) FROM FacturaRecibida f WHERE f.fechaFactura BETWEEN :inicio AND :fin")
       BigDecimal totalIvaSoportado(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

       // Resumen por categoría
       @Query("SELECT f.categoria, COUNT(f), SUM(f.total) FROM FacturaRecibida f " +
                     "WHERE f.fechaFactura BETWEEN :inicio AND :fin GROUP BY f.categoria ORDER BY SUM(f.total) DESC")
       List<Object[]> resumenPorCategoria(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

       // Resumen por proveedor
       @Query("SELECT f.proveedorNombre, COUNT(f), SUM(f.total) FROM FacturaRecibida f " +
                     "WHERE f.fechaFactura BETWEEN :inicio AND :fin GROUP BY f.proveedorNombre ORDER BY SUM(f.total) DESC")
       List<Object[]> resumenPorProveedor(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

       // Facturas del mes actual
       @Query("SELECT f FROM FacturaRecibida f WHERE YEAR(f.fechaFactura) = :year AND MONTH(f.fechaFactura) = :month ORDER BY f.fechaFactura DESC")
       List<FacturaRecibida> findByMes(@Param("year") int year, @Param("month") int month);

       // Próximos vencimientos
       @Query("SELECT f FROM FacturaRecibida f WHERE f.estado = 'PENDIENTE' AND f.fechaVencimiento BETWEEN :hoy AND :limite ORDER BY f.fechaVencimiento ASC")
       List<FacturaRecibida> findProximosVencimientos(@Param("hoy") LocalDate hoy, @Param("limite") LocalDate limite);
}

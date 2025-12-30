package com.lavaderosepulveda.app.repository;

import com.lavaderosepulveda.app.model.EstadoFactura;
import com.lavaderosepulveda.app.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    // Buscar por número de factura
    Optional<Factura> findByNumero(String numero);

    // Buscar por estado
    List<Factura> findByEstadoOrderByFechaDesc(EstadoFactura estado);

    // Buscar por cliente
    List<Factura> findByClienteIdOrderByFechaDesc(Long clienteId);

    // Buscar por rango de fechas
    List<Factura> findByFechaBetweenOrderByFechaDesc(LocalDate fechaInicio, LocalDate fechaFin);

    // Buscar por año
    List<Factura> findByAnioOrderByNumeroSecuencialDesc(Integer anio);

    // Obtener el último número secuencial del año
    @Query("SELECT MAX(f.numeroSecuencial) FROM Factura f WHERE f.anio = :anio")
    Optional<Integer> findMaxNumeroSecuencialByAnio(@Param("anio") Integer anio);

    // Buscar facturas pendientes
    @Query("SELECT f FROM Factura f WHERE f.estado = 'PENDIENTE' ORDER BY f.fecha")
    List<Factura> findFacturasPendientes();

    // Total facturado en un período
    @Query("SELECT COALESCE(SUM(f.total), 0) FROM Factura f WHERE f.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal sumTotalByFechaBetween(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    // Total facturado por estado en un período
    @Query("SELECT COALESCE(SUM(f.total), 0) FROM Factura f WHERE f.estado = :estado AND f.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal sumTotalByEstadoAndFechaBetween(@Param("estado") EstadoFactura estado, 
                                                @Param("fechaInicio") LocalDate fechaInicio, 
                                                @Param("fechaFin") LocalDate fechaFin);

    // Contar facturas por estado
    long countByEstado(EstadoFactura estado);

    // Contar facturas del mes actual
    @Query("SELECT COUNT(f) FROM Factura f WHERE YEAR(f.fecha) = :anio AND MONTH(f.fecha) = :mes")
    long countByMes(@Param("anio") int anio, @Param("mes") int mes);

    // Buscar facturas de hoy
    List<Factura> findByFechaOrderByCreatedAtDesc(LocalDate fecha);

    // Total pendiente de cobro
    @Query("SELECT COALESCE(SUM(f.total), 0) FROM Factura f WHERE f.estado = 'PENDIENTE'")
    BigDecimal sumTotalPendiente();

    // Búsqueda por texto (número o nombre cliente)
    @Query("SELECT f FROM Factura f WHERE f.numero LIKE %:texto% OR f.clienteNombre LIKE %:texto% ORDER BY f.fecha DESC")
    List<Factura> buscarPorTexto(@Param("texto") String texto);
}

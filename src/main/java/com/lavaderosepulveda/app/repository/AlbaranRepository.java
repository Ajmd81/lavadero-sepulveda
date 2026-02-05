package com.lavaderosepulveda.app.repository;

import com.lavaderosepulveda.app.model.Albaran;
import com.lavaderosepulveda.app.model.Albaran.EstadoAlbaran;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlbaranRepository extends JpaRepository<Albaran, Long> {
    
    @Query("SELECT DISTINCT a FROM Albaran a LEFT JOIN FETCH a.cliente LEFT JOIN FETCH a.lineas ORDER BY a.fecha DESC")
    List<Albaran> findAllWithRelations();
    
    Optional<Albaran> findByNumero(String numero);
    
    List<Albaran> findByClienteIdOrderByFechaDesc(Long clienteId);
    
    List<Albaran> findByEstado(EstadoAlbaran estado);
    
    List<Albaran> findByFechaBetweenOrderByFechaDesc(LocalDate fechaInicio, LocalDate fechaFin);
    
    List<Albaran> findByEstadoAndIdIn(EstadoAlbaran estado, List<Long> ids);
    
    @Query("SELECT a FROM Albaran a WHERE a.estado = :estado AND a.cliente.id = :clienteId ORDER BY a.fecha DESC")
    List<Albaran> findByEstadoAndClienteId(EstadoAlbaran estado, Long clienteId);
    
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(a.numero, 10) AS int)), 0) FROM Albaran a WHERE a.numero LIKE CONCAT('ALB-', :year, '-%')")
    Integer findMaxNumeroByYear(String year);
}
package com.lavaderosepulveda.app.repository;

import com.lavaderosepulveda.app.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    List<Proveedor> findByActivoTrueOrderByNombreAsc();

    Optional<Proveedor> findByNif(String nif);

    @Query("SELECT p FROM Proveedor p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(p.nif) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Proveedor> buscar(@Param("termino") String termino);

    @Query("SELECT p FROM Proveedor p WHERE p.activo = true AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(p.nif) LIKE LOWER(CONCAT('%', :termino, '%')))")
    List<Proveedor> buscarActivos(@Param("termino") String termino);

    boolean existsByNif(String nif);
}

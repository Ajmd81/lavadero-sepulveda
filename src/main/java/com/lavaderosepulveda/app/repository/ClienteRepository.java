package com.lavaderosepulveda.app.repository;

import com.lavaderosepulveda.app.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Buscar cliente por teléfono
     */
    Optional<Cliente> findByTelefono(String telefono);

    /**
     * Buscar clientes por nombre (contiene)
     */
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Buscar clientes por email
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Buscar clientes activos
     */
    List<Cliente> findByActivoTrue();

    /**
     * Buscar clientes inactivos
     */
    List<Cliente> findByActivoFalse();

    /**
     * Verificar si existe un cliente con ese teléfono
     */
    boolean existsByTelefono(String telefono);
    
    // ========================================
    // MÉTODOS ADICIONALES PARA CRM
    // ========================================
    
    /**
     * Buscar cliente por NIF
     */
    Optional<Cliente> findByNif(String nif);
    
    /**
     * Verificar si existe un cliente con ese NIF
     */
    boolean existsByNif(String nif);
    
    /**
     * Buscar clientes por matrícula
     */
    List<Cliente> findByMatriculaContainingIgnoreCase(String matricula);
    
    /**
     * Buscar clientes por ciudad
     */
    List<Cliente> findByCiudadIgnoreCase(String ciudad);
    
    /**
     * Buscar clientes por provincia
     */
    List<Cliente> findByProvinciaIgnoreCase(String provincia);
    
    /**
     * Buscar por nombre o apellidos (búsqueda combinada)
     */
    @Query("SELECT c FROM Cliente c WHERE " +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.apellidos) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    List<Cliente> buscarPorNombreOApellidos(@Param("busqueda") String busqueda);
    
    /**
     * Contar clientes activos
     */
    long countByActivoTrue();
    
    /**
     * Buscar clientes por marca de vehículo
     */
    List<Cliente> findByMarcaIgnoreCase(String marca);
}
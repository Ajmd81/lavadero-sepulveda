package com.lavaderosepulveda.app.repository;

import com.lavaderosepulveda.app.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
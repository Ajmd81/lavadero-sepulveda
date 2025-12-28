package com.lavaderosepulveda.crm.repository;

import com.lavaderosepulveda.crm.model.Cliente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class ClienteRepository extends BaseRepository<Cliente, Long> {
    
    public ClienteRepository() {
        super(Cliente.class);
    }
    
    public Optional<Cliente> findByTelefono(String telefono) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Cliente> query = em.createQuery(
                "SELECT c FROM Cliente c WHERE c.telefono = :telefono", 
                Cliente.class
            );
            query.setParameter("telefono", telefono);
            List<Cliente> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }
    
    public Optional<Cliente> findByEmail(String email) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Cliente> query = em.createQuery(
                "SELECT c FROM Cliente c WHERE c.email = :email", 
                Cliente.class
            );
            query.setParameter("email", email);
            List<Cliente> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }
    
    public List<Cliente> findByNombreContaining(String nombre) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Cliente> query = em.createQuery(
                "SELECT c FROM Cliente c WHERE LOWER(c.nombre) LIKE LOWER(:nombre) " +
                "OR LOWER(c.apellidos) LIKE LOWER(:nombre)", 
                Cliente.class
            );
            query.setParameter("nombre", "%" + nombre + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Cliente> findActivos() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Cliente> query = em.createQuery(
                "SELECT c FROM Cliente c WHERE c.activo = true ORDER BY c.nombre", 
                Cliente.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Cliente> findTopClientesByFacturacion(int limit) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Cliente> query = em.createQuery(
                "SELECT c FROM Cliente c WHERE c.activo = true " +
                "ORDER BY c.totalFacturado DESC", 
                Cliente.class
            );
            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Cliente> findClientesConMasNoPresentaciones(int limit) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Cliente> query = em.createQuery(
                "SELECT c FROM Cliente c WHERE c.activo = true AND c.citasNoPresentadas > 0 " +
                "ORDER BY c.citasNoPresentadas DESC", 
                Cliente.class
            );
            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Cliente> findClientesConBajaTasaCompletacion(double tasaMaxima, int limit) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Cliente> query = em.createQuery(
                "SELECT c FROM Cliente c WHERE c.activo = true AND c.totalCitas > 0 " +
                "AND (CAST(c.citasCompletadas AS double) / c.totalCitas * 100) < :tasa " +
                "ORDER BY (CAST(c.citasCompletadas AS double) / c.totalCitas)", 
                Cliente.class
            );
            query.setParameter("tasa", tasaMaxima);
            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}

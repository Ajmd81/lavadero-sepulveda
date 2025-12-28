package com.lavaderosepulveda.crm.repository;

import com.lavaderosepulveda.crm.model.Servicio;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class ServicioRepository extends BaseRepository<Servicio, Long> {
    
    public ServicioRepository() {
        super(Servicio.class);
    }
    
    public List<Servicio> findActivos() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Servicio> query = em.createQuery(
                "SELECT s FROM Servicio s WHERE s.activo = true ORDER BY s.nombre", 
                Servicio.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Servicio> findByCategoria(String categoria) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Servicio> query = em.createQuery(
                "SELECT s FROM Servicio s WHERE s.categoria = :categoria " +
                "AND s.activo = true ORDER BY s.precio", 
                Servicio.class
            );
            query.setParameter("categoria", categoria);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Servicio> findByNombreContaining(String nombre) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Servicio> query = em.createQuery(
                "SELECT s FROM Servicio s WHERE LOWER(s.nombre) LIKE LOWER(:nombre) " +
                "AND s.activo = true ORDER BY s.nombre", 
                Servicio.class
            );
            query.setParameter("nombre", "%" + nombre + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<String> findAllCategorias() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<String> query = em.createQuery(
                "SELECT DISTINCT s.categoria FROM Servicio s WHERE s.activo = true " +
                "ORDER BY s.categoria", 
                String.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}

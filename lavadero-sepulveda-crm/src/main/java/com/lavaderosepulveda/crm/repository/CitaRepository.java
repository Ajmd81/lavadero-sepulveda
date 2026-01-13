package com.lavaderosepulveda.crm.repository;

import com.lavaderosepulveda.crm.model.entity.Cita;
import com.lavaderosepulveda.crm.model.enums.EstadoCita;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class CitaRepository extends BaseRepository<Cita, Long> {
    
    public CitaRepository() {
        super(Cita.class);
    }
    
    public List<Cita> findByFecha(LocalDate fecha) {
        EntityManager em = getEntityManager();
        try {
            LocalDateTime startOfDay = fecha.atStartOfDay();
            LocalDateTime endOfDay = fecha.atTime(LocalTime.MAX);
            
            TypedQuery<Cita> query = em.createQuery(
                "SELECT c FROM Cita c WHERE c.fechaHora BETWEEN :start AND :end " +
                "ORDER BY c.fechaHora", 
                Cita.class
            );
            query.setParameter("start", startOfDay);
            query.setParameter("end", endOfDay);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Cita> findByClienteId(Long clienteId) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Cita> query = em.createQuery(
                "SELECT c FROM Cita c WHERE c.cliente.id = :clienteId " +
                "ORDER BY c.fechaHora DESC", 
                Cita.class
            );
            query.setParameter("clienteId", clienteId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Cita> findByEstado(EstadoCita estado) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Cita> query = em.createQuery(
                "SELECT c FROM Cita c WHERE c.estado = :estado " +
                "ORDER BY c.fechaHora", 
                Cita.class
            );
            query.setParameter("estado", estado);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Cita> findByFechaRange(LocalDate fechaInicio, LocalDate fechaFin) {
        EntityManager em = getEntityManager();
        try {
            LocalDateTime startOfDay = fechaInicio.atStartOfDay();
            LocalDateTime endOfDay = fechaFin.atTime(LocalTime.MAX);
            
            TypedQuery<Cita> query = em.createQuery(
                "SELECT c FROM Cita c WHERE c.fechaHora BETWEEN :start AND :end " +
                "ORDER BY c.fechaHora", 
                Cita.class
            );
            query.setParameter("start", startOfDay);
            query.setParameter("end", endOfDay);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Cita> findCitasPendientes() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Cita> query = em.createQuery(
                "SELECT c FROM Cita c WHERE c.estado = :estado " +
                "AND c.fechaHora >= :now ORDER BY c.fechaHora", 
                Cita.class
            );
            query.setParameter("estado", EstadoCita.PENDIENTE);
            query.setParameter("now", LocalDateTime.now());
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Cita> findCitasHoy() {
        return findByFecha(LocalDate.now());
    }
    
    public List<Cita> findCitasProximasSemana() {
        LocalDate hoy = LocalDate.now();
        LocalDate finSemana = hoy.plusDays(7);
        return findByFechaRange(hoy, finSemana);
    }
    
    public List<Cita> findCitasNoFacturadas() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Cita> query = em.createQuery(
                "SELECT c FROM Cita c WHERE c.estado = :estado " +
                "AND c.facturada = false ORDER BY c.fechaHora DESC", 
                Cita.class
            );
            query.setParameter("estado", EstadoCita.COMPLETADA);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public Long countByEstado(EstadoCita estado) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM Cita c WHERE c.estado = :estado", 
                Long.class
            );
            query.setParameter("estado", estado);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    public Long countCitasHoy() {
        EntityManager em = getEntityManager();
        try {
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
            
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM Cita c WHERE c.fechaHora BETWEEN :start AND :end", 
                Long.class
            );
            query.setParameter("start", startOfDay);
            query.setParameter("end", endOfDay);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}

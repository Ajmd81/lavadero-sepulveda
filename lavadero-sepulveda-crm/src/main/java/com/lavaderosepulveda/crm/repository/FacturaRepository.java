package com.lavaderosepulveda.crm.repository;

import com.lavaderosepulveda.crm.model.entity.Factura;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FacturaRepository extends BaseRepository<Factura, Long> {
    
    public FacturaRepository() {
        super(Factura.class);
    }
    
    public Optional<Factura> findByNumeroFactura(String numeroFactura) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Factura> query = em.createQuery(
                "SELECT f FROM Factura f WHERE f.numeroFactura = :numero", 
                Factura.class
            );
            query.setParameter("numero", numeroFactura);
            List<Factura> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }
    
    public List<Factura> findByClienteId(Long clienteId) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Factura> query = em.createQuery(
                "SELECT f FROM Factura f WHERE f.cliente.id = :clienteId " +
                "ORDER BY f.fechaFactura DESC", 
                Factura.class
            );
            query.setParameter("clienteId", clienteId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Factura> findByFechaRange(LocalDate fechaInicio, LocalDate fechaFin) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Factura> query = em.createQuery(
                "SELECT f FROM Factura f WHERE f.fechaFactura BETWEEN :inicio AND :fin " +
                "ORDER BY f.fechaFactura DESC", 
                Factura.class
            );
            query.setParameter("inicio", fechaInicio);
            query.setParameter("fin", fechaFin);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Factura> findPendientesPago() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Factura> query = em.createQuery(
                "SELECT f FROM Factura f WHERE f.pagada = false " +
                "ORDER BY f.fechaVencimiento", 
                Factura.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Factura> findVencidas() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Factura> query = em.createQuery(
                "SELECT f FROM Factura f WHERE f.pagada = false " +
                "AND f.fechaVencimiento < :hoy ORDER BY f.fechaVencimiento", 
                Factura.class
            );
            query.setParameter("hoy", LocalDate.now());
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public String getUltimoNumeroFactura(String serie) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<String> query = em.createQuery(
                "SELECT f.numeroFactura FROM Factura f WHERE f.serieFactura = :serie " +
                "ORDER BY f.id DESC", 
                String.class
            );
            query.setParameter("serie", serie);
            query.setMaxResults(1);
            List<String> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
    
    public Long getNextNumeroFactura(String serie) {
        String ultimoNumero = getUltimoNumeroFactura(serie);
        if (ultimoNumero == null) {
            return 1L;
        }
        // Extraer el número de la factura (formato: A-2024-0001)
        String[] parts = ultimoNumero.split("-");
        if (parts.length >= 3) {
            try {
                return Long.parseLong(parts[2]) + 1;
            } catch (NumberFormatException e) {
                return 1L;
            }
        }
        return 1L;
    }
    
    public Double getTotalFacturadoEnPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Double> query = em.createQuery(
                "SELECT COALESCE(SUM(f.totalFactura), 0.0) FROM Factura f " +
                "WHERE f.fechaFactura BETWEEN :inicio AND :fin", 
                Double.class
            );
            query.setParameter("inicio", fechaInicio);
            query.setParameter("fin", fechaFin);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    public Double getTotalPendienteCobro() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Double> query = em.createQuery(
                "SELECT COALESCE(SUM(f.totalFactura), 0.0) FROM Factura f " +
                "WHERE f.pagada = false", 
                Double.class
            );
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    public List<Factura> findFacturasDelMes(int mes, int anio) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Factura> query = em.createQuery(
                "SELECT f FROM Factura f WHERE YEAR(f.fechaFactura) = :anio " +
                "AND MONTH(f.fechaFactura) = :mes ORDER BY f.fechaFactura", 
                Factura.class
            );
            query.setParameter("anio", anio);
            query.setParameter("mes", mes);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}

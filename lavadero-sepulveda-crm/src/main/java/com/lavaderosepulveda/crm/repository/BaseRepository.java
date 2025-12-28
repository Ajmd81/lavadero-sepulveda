package com.lavaderosepulveda.crm.repository;

import com.lavaderosepulveda.crm.config.DatabaseConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class BaseRepository<T, ID> {
    
    protected final Class<T> entityClass;
    protected final DatabaseConfig databaseConfig;
    
    public BaseRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.databaseConfig = DatabaseConfig.getInstance();
    }
    
    public T save(T entity) {
        EntityManager em = databaseConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            T savedEntity = em.merge(entity);
            transaction.commit();
            log.debug("Entidad guardada: {}", savedEntity);
            return savedEntity;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.error("Error al guardar entidad", e);
            throw new RuntimeException("Error al guardar entidad", e);
        } finally {
            em.close();
        }
    }
    
    public Optional<T> findById(ID id) {
        EntityManager em = databaseConfig.getEntityManager();
        try {
            T entity = em.find(entityClass, id);
            return Optional.ofNullable(entity);
        } finally {
            em.close();
        }
    }
    
    public List<T> findAll() {
        EntityManager em = databaseConfig.getEntityManager();
        try {
            TypedQuery<T> query = em.createQuery(
                "SELECT e FROM " + entityClass.getSimpleName() + " e", 
                entityClass
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public void delete(T entity) {
        EntityManager em = databaseConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            T merged = em.merge(entity);
            em.remove(merged);
            transaction.commit();
            log.debug("Entidad eliminada: {}", entity);
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.error("Error al eliminar entidad", e);
            throw new RuntimeException("Error al eliminar entidad", e);
        } finally {
            em.close();
        }
    }
    
    public void deleteById(ID id) {
        findById(id).ifPresent(this::delete);
    }
    
    public long count() {
        EntityManager em = databaseConfig.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", 
                Long.class
            );
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }
    
    protected EntityManager getEntityManager() {
        return databaseConfig.getEntityManager();
    }
}

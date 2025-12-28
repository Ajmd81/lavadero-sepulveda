package com.lavaderosepulveda.crm.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatabaseConfig {
    
    private static DatabaseConfig instance;
    private final EntityManagerFactory entityManagerFactory;
    
    private DatabaseConfig() {
        log.info("Inicializando configuración de base de datos...");
        try {
            this.entityManagerFactory = Persistence.createEntityManagerFactory("lavadero-crm-pu");
            log.info("EntityManagerFactory creado exitosamente");
        } catch (Exception e) {
            log.error("Error al crear EntityManagerFactory", e);
            throw new RuntimeException("No se pudo inicializar la base de datos", e);
        }
    }
    
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }
    
    public EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }
    
    public void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            log.info("Cerrando EntityManagerFactory...");
            entityManagerFactory.close();
        }
    }
}

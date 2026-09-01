package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.model.ConfiguracionHorario;
import com.lavaderosepulveda.app.model.enums.ModoHorario;
import com.lavaderosepulveda.app.repository.ConfiguracionHorarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

/**
 * Servicio para gestionar la configuración centralizada de horarios del lavadero
 * 
 * Patrón Singleton: Solo existe una configuración con ID = 1
 * Si no existe, se crea automáticamente con valores por defecto
 */
@Service
public class ConfiguracionHorarioService {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguracionHorarioService.class);
    private static final Long CONFIG_ID = 1L;

    @Autowired
    private ConfiguracionHorarioRepository repository;

    /**
     * Obtiene la configuración actual del lavadero
     * Si no existe, la crea con valores por defecto
     * 
     * @return ConfiguracionHorario singleton
     */
    @Transactional(readOnly = true)
    public ConfiguracionHorario obtenerConfiguracion() {
        return repository.findById(CONFIG_ID)
                .orElseGet(this::crearConfiguracionPorDefecto);
    }

    /**
     * Actualiza la configuración del lavadero
     * 
     * @param config Nueva configuración
     * @return ConfiguracionHorario actualizada
     * @throws IllegalArgumentException si la configuración no es válida
     */
    @Transactional
    public ConfiguracionHorario actualizarConfiguracion(ConfiguracionHorario config) {
        logger.info("Actualizando configuración de horarios");

        // Forzar ID singleton
        config.setId(CONFIG_ID);

        // Validar configuración
        if (!config.isHorarioValido()) {
            throw new IllegalArgumentException(
                    "Horario inválido: hora de apertura debe ser menor a hora de cierre"
            );
        }

        if (!config.isConfiguracionValida()) {
            throw new IllegalArgumentException(
                    "Configuración inválida: la duración de la cita debe ser compatible con citas por hora"
            );
        }

        ConfiguracionHorario configuracionGuardada = repository.save(config);
        logger.info("Configuración actualizada: {}", configuracionGuardada);

        return configuracionGuardada;
    }

    /**
     * Crea la configuración por defecto si no existe
     * 
     * @return ConfiguracionHorario con valores por defecto
     */
    @Transactional
    private ConfiguracionHorario crearConfiguracionPorDefecto() {
        logger.info("Creando configuración por defecto");

        ConfiguracionHorario config = new ConfiguracionHorario(
                60,                              // duracionCitaMinutos
                2,                               // citasPorHora
                ModoHorario.COMPLETO,           // modoHorario
                LocalTime.of(8, 0),             // horaApertura
                LocalTime.of(20, 0)             // horaCierre
        );

        config.setId(CONFIG_ID);
        return repository.save(config);
    }

    /**
     * Obtiene solo la duración de la cita en minutos
     * Útil para HorarioService y cálculos de disponibilidad
     * 
     * @return Duración en minutos
     */
    @Transactional(readOnly = true)
    public Integer obtenerDuracionCitaMinutos() {
        return obtenerConfiguracion().getDuracionCitaMinutos();
    }

    /**
     * Obtiene solo las citas por hora
     * 
     * @return Cantidad de citas por hora
     */
    @Transactional(readOnly = true)
    public Integer obtenerCitasPorHora() {
        return obtenerConfiguracion().getCitasPorHora();
    }

    /**
     * Obtiene el modo de horario
     * 
     * @return ModoHorario (SOLO_MAÑANA, SOLO_TARDE, COMPLETO)
     */
    @Transactional(readOnly = true)
    public ModoHorario obtenerModoHorario() {
        return obtenerConfiguracion().getModoHorario();
    }

    /**
     * Obtiene la hora de apertura
     * 
     * @return LocalTime de apertura
     */
    @Transactional(readOnly = true)
    public LocalTime obtenerHoraApertura() {
        return obtenerConfiguracion().getHoraApertura();
    }

    /**
     * Obtiene la hora de cierre
     * 
     * @return LocalTime de cierre
     */
    @Transactional(readOnly = true)
    public LocalTime obtenerHoraCierre() {
        return obtenerConfiguracion().getHoraCierre();
    }
}
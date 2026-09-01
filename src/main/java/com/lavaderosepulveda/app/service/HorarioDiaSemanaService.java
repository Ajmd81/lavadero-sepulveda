package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.model.HorarioDiaSemana;
import com.lavaderosepulveda.app.model.enums.DiaSemana;
import com.lavaderosepulveda.app.repository.HorarioDiaSemanaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar los horarios por día de la semana
 */
@Service
public class HorarioDiaSemanaService {

    private static final Logger logger = LoggerFactory.getLogger(HorarioDiaSemanaService.class);

    @Autowired
    private HorarioDiaSemanaRepository repository;

    /**
     * Obtiene el horario de un día específico
     */
    @Transactional(readOnly = true)
    public Optional<HorarioDiaSemana> obtenerPorDia(DiaSemana diaSemana) {
        return repository.findByDiaSemana(diaSemana);
    }

    /**
     * Obtiene todos los horarios ordenados por día
     */
    @Transactional(readOnly = true)
    public List<HorarioDiaSemana> obtenerTodos() {
        return repository.findAllByOrderByDiaSemanaAsc();
    }

    /**
     * Obtiene todos los días activos
     */
    @Transactional(readOnly = true)
    public List<HorarioDiaSemana> obtenerDiasActivos() {
        return repository.findByActivoTrue();
    }

    /**
     * Actualiza el horario de un día específico
     */
    @Transactional
    public HorarioDiaSemana actualizarHorarioDia(DiaSemana diaSemana, HorarioDiaSemana nuevoHorario) {
        logger.info("Actualizando horario para {}", diaSemana);

        // Validar horario
        if (!nuevoHorario.isHorarioValido()) {
            throw new IllegalArgumentException(
                    "Horario inválido para " + diaSemana + ": apertura debe ser menor a cierre"
            );
        }

        if (!nuevoHorario.isSeparacionValida()) {
            throw new IllegalArgumentException(
                    "Horario inválido para " + diaSemana + ": debe haber separación entre mañana y tarde"
            );
        }

        HorarioDiaSemana horarioGuardado = repository.findByDiaSemana(diaSemana)
                .orElseThrow(() -> new IllegalArgumentException("Día no encontrado: " + diaSemana));

        horarioGuardado.setAperturaMañana(nuevoHorario.getAperturaMañana());
        horarioGuardado.setCierreMañana(nuevoHorario.getCierreMañana());
        horarioGuardado.setAperturaTarde(nuevoHorario.getAperturaTarde());
        horarioGuardado.setCierreTarde(nuevoHorario.getCierreTarde());
        horarioGuardado.setActivo(nuevoHorario.getActivo());

        return repository.save(horarioGuardado);
    }

    /**
     * Actualiza múltiples horarios a la vez
     */
    @Transactional
    public List<HorarioDiaSemana> actualizarTodos(List<HorarioDiaSemana> horarios) {
        logger.info("Actualizando {} horarios", horarios.size());

        for (HorarioDiaSemana horario : horarios) {
            if (!horario.isHorarioValido() || !horario.isSeparacionValida()) {
                throw new IllegalArgumentException(
                        "Horario inválido para " + horario.getDiaSemana()
                );
            }
        }

        return repository.saveAll(horarios);
    }

    /**
     * Inicializa los horarios por defecto (primera vez)
     */
    @Transactional
    public void inicializarHorariosPorDefecto() {
        logger.info("Inicializando horarios por defecto");

        // Verificar si ya existen
        long count = repository.count();
        if (count > 0) {
            logger.info("Horarios ya existen, no se inicializan");
            return;
        }

        // Crear horarios para todos los días
        for (DiaSemana dia : DiaSemana.values()) {
            HorarioDiaSemana horario = new HorarioDiaSemana();
            horario.setDiaSemana(dia);

            // Por defecto: 9:00-14:00 y 17:00-20:00 (lunes a viernes)
            // Sábados: 9:00-13:30 (solo mañana)
            // Domingos: cerrado

            if (dia == DiaSemana.DOMINGO) {
                horario.setActivo(false);
            } else if (dia == DiaSemana.SABADO) {
                horario.setAperturaMañana(java.time.LocalTime.of(9, 0));
                horario.setCierreMañana(java.time.LocalTime.of(13, 30));
                horario.setActivo(true);
            } else {
                horario.setAperturaMañana(java.time.LocalTime.of(9, 0));
                horario.setCierreMañana(java.time.LocalTime.of(14, 0));
                horario.setAperturaTarde(java.time.LocalTime.of(17, 0));
                horario.setCierreTarde(java.time.LocalTime.of(20, 0));
                horario.setActivo(true);
            }

            repository.save(horario);
            logger.info("Horario creado para {}", dia);
        }
    }
}
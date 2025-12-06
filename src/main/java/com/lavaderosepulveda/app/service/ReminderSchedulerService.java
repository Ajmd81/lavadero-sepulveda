package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.repository.CitaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para programar y enviar recordatorios automáticos de citas
 */
@Service
public class ReminderSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderSchedulerService.class);

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.recordatorios.enabled:true}")
    private boolean recordatoriosEnabled;

    /**
     * Tarea programada que se ejecuta todos los días a las 9:00 AM
     * para enviar recordatorios de citas del día siguiente
     */
    @Scheduled(cron = "${app.recordatorios.cron:0 0 9 * * ?}") // Todos los días a las 9:00 AM por defecto
    public void enviarRecordatoriosCitas() {
        if (!recordatoriosEnabled) {
            logger.info("El envío de recordatorios está desactivado");
            return;
        }

        logger.info("Iniciando envío de recordatorios de citas...");

        // Obtener fecha de mañana
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // Buscar todas las citas para mañana
        List<Cita> citasMañana = citaRepository.findByFecha(tomorrow);

        if (citasMañana.isEmpty()) {
            logger.info("No hay citas programadas para mañana {}", tomorrow);
            return;
        }

        // Enviar recordatorio para cada cita
        logger.info("Enviando {} recordatorios para las citas de mañana {}", citasMañana.size(), tomorrow);

        for (Cita cita : citasMañana) {
            try {
                emailService.enviarRecordatorioCita(cita);
                logger.info("Recordatorio enviado para la cita ID: {}, Cliente: {}", cita.getId(), cita.getNombre());
            } catch (Exception e) {
                logger.error("Error al enviar recordatorio para la cita ID: {}, Error: {}", cita.getId(), e.getMessage());
            }
        }

        logger.info("Proceso de envío de recordatorios completado");
    }

    /**
     * Método para enviar un recordatorio manual a una cita específica
     * Útil para pruebas o envíos manuales desde el panel de administración
     */
    public void enviarRecordatorioManual(Long citaId) {
        logger.info("Enviando recordatorio manual para la cita ID: {}", citaId);

        citaRepository.findById(citaId).ifPresent(cita -> {
            try {
                emailService.enviarRecordatorioCita(cita);
                logger.info("Recordatorio manual enviado con éxito para la cita ID: {}", citaId);
            } catch (Exception e) {
                logger.error("Error al enviar recordatorio manual para la cita ID: {}, Error: {}", citaId, e.getMessage());
                throw new RuntimeException("Error al enviar recordatorio: " + e.getMessage());
            }
        });
    }
}
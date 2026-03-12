package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.enums.TipoLavado;
import com.lavaderosepulveda.app.repository.CitaRepository;
import com.lavaderosepulveda.app.util.DateTimeFormatUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio refactorizado para envío de emails
 * Usa DateTimeFormatUtils para formateo consistente de fechas y horas
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender emailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private CitaRepository citaRepository;

    @Value("${spring.mail.username:noreply@lavaderosepulveda.com}")
    private String remitente;

    @Value("${app.empresa.nombre:Lavadero Sepúlveda}")
    private String nombreEmpresa;

    /**
     * Envía un email de confirmación con los detalles de la cita
     * Método ASINCRÓNICO - se ejecuta en background sin bloquear la solicitud
     * Usa DateTimeFormatUtils para formateo consistente
     * NOTA: Recibe citaId para evitar LazyInitializationException de Hibernate
     */
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<Void> enviarEmailConfirmacion(Long citaId) {
        logger.info("📧 INICIANDO envío ASINCRÓNICO de email de confirmación para cita ID: {}", citaId);
        
        try {
            // Obtener cita dentro de la transacción
            var citaOpt = citaRepository.findById(citaId);
            if (citaOpt.isEmpty()) {
                logger.error("❌ No se encontró cita con ID: {}", citaId);
                return CompletableFuture.failedFuture(new IllegalArgumentException("Cita no encontrada: " + citaId));
            }
            
            Cita cita = citaOpt.get();
            // Acceder a todos los campos DENTRO de la transacción para inicializarlos
            String email = cita.getEmail();
            String nombre = cita.getNombre();
            LocalDate fecha = cita.getFecha();
            LocalTime hora = cita.getHora();
            TipoLavado tipoLavado = cita.getTipoLavado();
            
            logger.info("📧 Cita obtenida: {} - Email: {}", cita.getId(), email);
            
            if (!isEmailConfigured()) {
                logger.error("❌ EmailService NO ESTÁ CONFIGURADO. JavaMailSender es null. Verificar variables de entorno SPRING_MAIL_*");
                return CompletableFuture.failedFuture(new RuntimeException("EmailService no configurado"));
            }

            if (!isEmailValido(email)) {
                logger.error("❌ Email inválido para la cita ID {}: '{}'", cita.getId(), email);
                return CompletableFuture.failedFuture(new RuntimeException("Email inválido"));
            }
            
            logger.info("✅ Validaciones pasadas. Preparando envío a: {}", email);

            MimeMessage message = crearMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Configuración del mensaje
            helper.setFrom(remitente);
            helper.setTo(email);
            helper.setSubject(String.format("Confirmación de reserva - %s", nombreEmpresa));

            // Preparar contexto usando utilities centralizadas
            Context context = crearContextoEmail(cita);

            // Procesar plantilla
            String contenido = templateEngine.process("emails/confirmacion-cita", context);
            helper.setText(contenido, true);

            // Enviar email
            logger.info("Enviando mensaje SMTP a {} para cita {}...", email, cita.getId());
            emailSender.send(message);
            logger.info("✅ OK: Email de confirmacion enviado exitosamente a: {} para cita ID {}", email, cita.getId());
            return CompletableFuture.completedFuture(null);

        } catch (MessagingException e) {
            logger.error("❌ Error MessagingException al enviar email para cita ID {}: {} | Causa: {}", 
                    citaId, e.getMessage(), e.getCause(), e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            logger.error("❌ Error inesperado al enviar email para cita ID {}: {} | Causa: {}", 
                    citaId, e.getMessage(), e.getCause(), e);
            return CompletableFuture.failedFuture(e);
        }
    }


    /**
     * Envía un recordatorio de cita un día antes
     * Usa DateTimeFormatUtils para formateo consistente
     */
    public void enviarRecordatorioCita(Cita cita) {
        if (!isEmailConfigured()) {
            logger.warn("EmailService no está configurado. No se enviará el recordatorio.");
            return;
        }

        if (!isEmailValido(cita.getEmail())) {
            logger.warn("Email inválido para recordatorio de cita ID {}: {}", cita.getId(), cita.getEmail());
            return;
        }

        try {
            MimeMessage message = crearMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(cita.getEmail());
            helper.setSubject(String.format("Recordatorio de cita - %s", nombreEmpresa));

            // Preparar contexto usando utilities centralizadas
            Context context = crearContextoEmail(cita);
            context.setVariable("esRecordatorio", true);

            // Procesar plantilla
            String contenido = templateEngine.process("emails/recordatorio-cita", context);
            helper.setText(contenido, true);

            // Enviar email
            emailSender.send(message);
            logger.info("Recordatorio enviado exitosamente a: {}", cita.getEmail());

        } catch (MessagingException e) {
            logger.error("Error al enviar recordatorio para cita ID {}: {}",
                    cita.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al enviar recordatorio: " + e.getMessage(), e);
        }
    }

    /**
     * Envía email de cancelación de cita
     */
    public void enviarEmailCancelacion(Cita cita, String motivo) {
        if (!isEmailConfigured() || !isEmailValido(cita.getEmail())) {
            return;
        }

        try {
            MimeMessage message = crearMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(cita.getEmail());
            helper.setSubject(String.format("Cancelación de cita - %s", nombreEmpresa));

            Context context = crearContextoEmail(cita);
            context.setVariable("motivoCancelacion", motivo != null ? motivo : "Sin motivo especificado");

            String contenido = templateEngine.process("emails/cancelacion-cita", context);
            helper.setText(contenido, true);

            emailSender.send(message);
            logger.info("Email de cancelación enviado a: {}", cita.getEmail());

        } catch (MessagingException e) {
            logger.error("Error al enviar email de cancelación para cita ID {}: {}",
                    cita.getId(), e.getMessage(), e);
        }
    }

    /**
     * Envía email genérico usando una plantilla
     */
    public void enviarEmailPersonalizado(String destinatario, String asunto, String plantilla, Context contexto) {
        if (!isEmailConfigured() || !isEmailValido(destinatario)) {
            return;
        }

        try {
            MimeMessage message = crearMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);

            String contenido = templateEngine.process(plantilla, contexto);
            helper.setText(contenido, true);

            emailSender.send(message);
            logger.info("Email personalizado enviado a: {}", destinatario);

        } catch (MessagingException e) {
            logger.error("Error al enviar email personalizado a {}: {}", destinatario, e.getMessage(), e);
            throw new RuntimeException("Error al enviar email: " + e.getMessage(), e);
        }
    }

    /**
     * Crea el contexto común para emails usando DateTimeFormatUtils
     */
    private Context crearContextoEmail(Cita cita) {
        Context context = new Context(new Locale("es"));

        // Variables de la cita
        context.setVariable("cita", cita);

        // Formateo consistente usando utilities centralizadas
        context.setVariable("fechaFormateada", DateTimeFormatUtils.formatearFechaCorta(cita.getFecha()));
        context.setVariable("horaFormateada", DateTimeFormatUtils.formatearHoraCorta(cita.getHora()));
        context.setVariable("fechaCompleta", DateTimeFormatUtils.formatearFechaCompleta(cita.getFecha()));

        // Variables de la empresa
        context.setVariable("nombreEmpresa", nombreEmpresa);
        context.setVariable("precio", cita.getTipoLavado().getPrecio());
        context.setVariable("tipoLavadoDescripcion", cita.getTipoLavado().getDescripcion());

        return context;
    }

    /**
     * Crea un MimeMessage básico
     */
    private MimeMessage crearMimeMessage() {
        if (emailSender == null) {
            throw new IllegalStateException("JavaMailSender no está configurado");
        }
        return emailSender.createMimeMessage();
    }

    /**
     * Verifica si el servicio de email está configurado
     */
    private boolean isEmailConfigured() {
        return emailSender != null;
    }

    /**
     * Valida si un email tiene formato correcto
     */
    private boolean isEmailValido(String email) {
        return email != null &&
                !email.trim().isEmpty() &&
                email.contains("@") &&
                email.contains(".");
    }

    /**
     * Verifica el estado de la configuración del email
     */
    public boolean isServicioDisponible() {
        return isEmailConfigured();
    }

    /**
     * Obtiene información de configuración para diagnóstico
     */
    public String obtenerEstadoConfiguracion() {
        if (isEmailConfigured()) {
            return String.format("EmailService configurado correctamente. Remitente: %s", remitente);
        } else {
            return "EmailService no configurado. Verifique la configuración de spring.mail en application.yml";
        }
    }
}
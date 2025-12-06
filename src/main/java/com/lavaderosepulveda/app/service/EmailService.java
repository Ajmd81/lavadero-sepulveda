package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.util.DateTimeFormatUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;

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

    @Value("${spring.mail.username:noreply@lavaderosepulveda.com}")
    private String remitente;

    @Value("${app.empresa.nombre:Lavadero Sepúlveda}")
    private String nombreEmpresa;

    /**
     * Envía un email de confirmación con los detalles de la cita
     * Usa DateTimeFormatUtils para formateo consistente
     */
    public void enviarEmailConfirmacion(Cita cita) {
        if (!isEmailConfigured()) {
            logger.warn("EmailService no está configurado. No se enviará el email de confirmación.");
            return;
        }

        if (!isEmailValido(cita.getEmail())) {
            logger.warn("Email inválido para la cita ID {}: {}", cita.getId(), cita.getEmail());
            return;
        }

        try {
            MimeMessage message = crearMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Configuración del mensaje
            helper.setFrom(remitente);
            helper.setTo(cita.getEmail());
            helper.setSubject(String.format("Confirmación de reserva - %s", nombreEmpresa));

            // Preparar contexto usando utilities centralizadas
            Context context = crearContextoEmail(cita);

            // Procesar plantilla
            String contenido = templateEngine.process("emails/confirmacion-cita", context);
            helper.setText(contenido, true);

            // Enviar email
            emailSender.send(message);
            logger.info("Email de confirmación enviado exitosamente a: {}", cita.getEmail());

        } catch (MessagingException e) {
            logger.error("Error al enviar email de confirmación para cita ID {}: {}",
                    cita.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al enviar email de confirmación: " + e.getMessage(), e);
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
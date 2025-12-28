package com.lavaderosepulveda.crm.service;

import com.lavaderosepulveda.crm.config.ConfigManager;
import com.lavaderosepulveda.crm.model.Factura;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

@Slf4j
public class EmailService {
    
    private static EmailService instance;
    private final ConfigManager config;
    
    // Configuración del servidor SMTP
    private String smtpHost;
    private String smtpPort;
    private String username;
    private String password;
    private String fromEmail;
    private String fromName;
    
    private EmailService() {
        this.config = ConfigManager.getInstance();
        cargarConfiguracion();
    }
    
    public static synchronized EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }
    
    private void cargarConfiguracion() {
        this.smtpHost = config.getProperty("email.smtp.host", "smtp.gmail.com");
        this.smtpPort = config.getProperty("email.smtp.port", "587");
        this.username = config.getProperty("email.smtp.username", "");
        this.password = config.getProperty("email.smtp.password", "");
        this.fromEmail = config.getProperty("email.smtp.from", "");
        this.fromName = config.getProperty("email.smtp.from_name", "Lavadero Sepúlveda");
        
        log.info("EmailService configurado - Host: {}, Port: {}, From: {}", smtpHost, smtpPort, fromEmail);
    }
    
    public void configurarSmtp(String host, String port, String user, String pass, String from) {
        this.smtpHost = host;
        this.smtpPort = port;
        this.username = user;
        this.password = pass;
        this.fromEmail = from;
    }
    
    public boolean enviarFactura(Factura factura, File pdfFile) {
        try {
            if (factura.getCliente().getEmail() == null || factura.getCliente().getEmail().isEmpty()) {
                log.warn("Cliente sin email configurado: {}", factura.getCliente().getNombreCompleto());
                return false;
            }
            
            String asunto = "Factura " + factura.getNumeroFactura() + " - Lavadero Sepúlveda";
            String cuerpo = construirCuerpoFactura(factura);
            
            enviarEmail(factura.getCliente().getEmail(), asunto, cuerpo, pdfFile);
            
            log.info("Factura {} enviada por email a {}", 
                factura.getNumeroFactura(), 
                factura.getCliente().getEmail());
            return true;
            
        } catch (Exception e) {
            log.error("Error al enviar factura por email", e);
            return false;
        }
    }
    
    public void enviarEmail(String destinatario, String asunto, String cuerpo, File adjunto) 
            throws MessagingException, UnsupportedEncodingException {
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail, fromName));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        message.setSubject(asunto);
        
        // Crear el cuerpo del mensaje
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(cuerpo, "text/html; charset=utf-8");
        
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        
        // Agregar adjunto si existe
        if (adjunto != null && adjunto.exists()) {
            MimeBodyPart attachPart = new MimeBodyPart();
            try {
                attachPart.attachFile(adjunto);
                multipart.addBodyPart(attachPart);
            } catch (Exception e) {
                log.error("Error al adjuntar archivo", e);
            }
        }
        
        message.setContent(multipart);
        
        Transport.send(message);
        log.info("Email enviado exitosamente a: {}", destinatario);
    }
    
    private String construirCuerpoFactura(Factura factura) {
        String empresaNombre = config.getProperty("app.empresa.nombre", "Lavadero Sepúlveda");
        String empresaEmail = config.getProperty("app.empresa.email", "info@lavaderosepulveda.com");
        String empresaTelefono = config.getProperty("app.empresa.telefono", "+34 XXX XXX XXX");
        
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #2c3e50;">%s</h2>
                <p>Estimado/a %s,</p>
                <p>Adjuntamos la factura número <strong>%s</strong> correspondiente a los servicios prestados.</p>
                <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                    <p><strong>Número de factura:</strong> %s</p>
                    <p><strong>Fecha:</strong> %s</p>
                    <p><strong>Importe total:</strong> %.2f €</p>
                    <p><strong>Estado:</strong> %s</p>
                </div>
                <p>Para cualquier consulta, no dude en contactar con nosotros.</p>
                <p>Gracias por confiar en nuestros servicios.</p>
                <hr style="margin: 30px 0;">
                <p style="font-size: 12px; color: #6c757d;">
                    <strong>%s</strong><br>
                    Email: %s<br>
                    Teléfono: %s
                </p>
            </body>
            </html>
            """,
            empresaNombre,
            factura.getCliente().getNombreCompleto(),
            factura.getNumeroFactura(),
            factura.getNumeroFactura(),
            factura.getFechaFactura().toString(),
            factura.getTotalFactura(),
            factura.getPagada() ? "Pagada" : "Pendiente de pago",
            empresaNombre,
            empresaEmail,
            empresaTelefono
        );
    }
    
    public boolean testConnection() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.timeout", "5000");
            props.put("mail.smtp.connectiontimeout", "5000");
            
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            
            Transport transport = session.getTransport("smtp");
            transport.connect(smtpHost, username, password);
            transport.close();
            
            log.info("Conexión SMTP exitosa");
            return true;
        } catch (Exception e) {
            log.error("Error al conectar con servidor SMTP: {}", e.getMessage());
            return false;
        }
    }
}

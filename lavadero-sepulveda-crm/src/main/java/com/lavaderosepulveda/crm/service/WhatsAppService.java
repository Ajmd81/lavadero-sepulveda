package com.lavaderosepulveda.crm.service;

import com.google.gson.JsonObject;
import com.lavaderosepulveda.crm.model.Cita;
import com.lavaderosepulveda.crm.model.Factura;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Slf4j
public class WhatsAppService {
    
    private static WhatsAppService instance;
    
    // Configuración de WhatsApp Business API (configurar según proveedor)
    private String apiUrl = "https://graph.facebook.com/v18.0"; // Meta WhatsApp API
    private String phoneNumberId = ""; // ID del número de teléfono de WhatsApp Business
    private String accessToken = ""; // Token de acceso
    
    private final OkHttpClient httpClient;
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    private WhatsAppService() {
        this.httpClient = new OkHttpClient();
    }
    
    public static synchronized WhatsAppService getInstance() {
        if (instance == null) {
            instance = new WhatsAppService();
        }
        return instance;
    }
    
    public void configurarApi(String phoneNumberId, String accessToken) {
        this.phoneNumberId = phoneNumberId;
        this.accessToken = accessToken;
    }
    
    public boolean enviarFactura(Factura factura, File pdfFile) {
        try {
            String telefono = limpiarNumeroTelefono(factura.getCliente().getTelefono());
            
            if (telefono == null || telefono.isEmpty()) {
                log.warn("Cliente sin teléfono configurado: {}", factura.getCliente().getNombreCompleto());
                return false;
            }
            
            // Primero subir el documento PDF
            String mediaId = subirDocumento(pdfFile);
            
            if (mediaId == null) {
                log.error("No se pudo subir el documento PDF");
                return false;
            }
            
            // Enviar mensaje con el documento
            String mensaje = construirMensajeFactura(factura);
            boolean enviado = enviarDocumento(telefono, mediaId, mensaje, "Factura_" + factura.getNumeroFactura() + ".pdf");
            
            if (enviado) {
                log.info("Factura {} enviada por WhatsApp a {}", factura.getNumeroFactura(), telefono);
            }
            
            return enviado;
            
        } catch (Exception e) {
            log.error("Error al enviar factura por WhatsApp", e);
            return false;
        }
    }
    
    public boolean enviarRecordatorioCita(Cita cita) {
        try {
            String telefono = limpiarNumeroTelefono(cita.getCliente().getTelefono());
            
            if (telefono == null || telefono.isEmpty()) {
                return false;
            }
            
            String mensaje = construirMensajeRecordatorio(cita);
            return enviarMensajeTexto(telefono, mensaje);
            
        } catch (Exception e) {
            log.error("Error al enviar recordatorio por WhatsApp", e);
            return false;
        }
    }
    
    public boolean enviarConfirmacionCita(Cita cita) {
        try {
            String telefono = limpiarNumeroTelefono(cita.getCliente().getTelefono());
            
            if (telefono == null || telefono.isEmpty()) {
                return false;
            }
            
            String mensaje = construirMensajeConfirmacion(cita);
            return enviarMensajeTexto(telefono, mensaje);
            
        } catch (Exception e) {
            log.error("Error al enviar confirmación por WhatsApp", e);
            return false;
        }
    }
    
    private boolean enviarMensajeTexto(String telefono, String mensaje) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("messaging_product", "whatsapp");
        json.addProperty("to", telefono);
        json.addProperty("type", "text");
        
        JsonObject text = new JsonObject();
        text.addProperty("body", mensaje);
        json.add("text", text);
        
        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
            .url(apiUrl + "/" + phoneNumberId + "/messages")
            .addHeader("Authorization", "Bearer " + accessToken)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            boolean success = response.isSuccessful();
            if (success) {
                log.info("Mensaje enviado a {}", telefono);
            } else {
                log.error("Error al enviar mensaje. Código: {}, Respuesta: {}", 
                    response.code(), response.body().string());
            }
            return success;
        }
    }
    
    private String subirDocumento(File file) throws IOException {
        // Este método varía según el proveedor de WhatsApp API
        // Aquí un ejemplo con Meta WhatsApp Business API
        
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("messaging_product", "whatsapp")
            .addFormDataPart("file", file.getName(),
                RequestBody.create(file, MediaType.parse("application/pdf")))
            .build();
        
        Request request = new Request.Builder()
            .url(apiUrl + "/" + phoneNumberId + "/media")
            .addHeader("Authorization", "Bearer " + accessToken)
            .post(requestBody)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                // Parsear el ID del media de la respuesta JSON
                // Esto depende de la estructura de la respuesta de la API
                log.info("Documento subido exitosamente");
                return responseBody; // Simplificado, debería extraer el media_id del JSON
            } else {
                log.error("Error al subir documento: {}", response.message());
                return null;
            }
        }
    }
    
    private boolean enviarDocumento(String telefono, String mediaId, String caption, String filename) 
            throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("messaging_product", "whatsapp");
        json.addProperty("to", telefono);
        json.addProperty("type", "document");
        
        JsonObject document = new JsonObject();
        document.addProperty("id", mediaId);
        document.addProperty("caption", caption);
        document.addProperty("filename", filename);
        json.add("document", document);
        
        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
            .url(apiUrl + "/" + phoneNumberId + "/messages")
            .addHeader("Authorization", "Bearer " + accessToken)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }
    
    private String limpiarNumeroTelefono(String telefono) {
        if (telefono == null) return null;
        
        // Remover espacios, guiones y otros caracteres
        String limpio = telefono.replaceAll("[\\s\\-()]", "");
        
        // Asegurar que tenga el código de país (España: +34)
        if (!limpio.startsWith("+")) {
            if (limpio.startsWith("34")) {
                limpio = "+" + limpio;
            } else if (limpio.length() == 9) {
                limpio = "+34" + limpio;
            }
        }
        
        return limpio;
    }
    
    private String construirMensajeFactura(Factura factura) {
        return String.format("""
            🧾 *Factura - Lavadero Sepúlveda*
            
            Hola %s,
            
            Adjuntamos su factura número *%s*
            
            📅 Fecha: %s
            💶 Importe: %.2f €
            📊 Estado: %s
            
            Gracias por confiar en nosotros.
            """,
            factura.getCliente().getNombre(),
            factura.getNumeroFactura(),
            factura.getFechaFactura().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            factura.getTotalFactura(),
            factura.getPagada() ? "Pagada" : "Pendiente de pago"
        );
    }
    
    private String construirMensajeRecordatorio(Cita cita) {
        return String.format("""
            🚗 *Recordatorio de Cita - Lavadero Sepúlveda*
            
            Hola %s,
            
            Te recordamos tu cita:
            📅 %s a las %s
            🔧 Servicios: %s
            
            ¡Te esperamos!
            """,
            cita.getCliente().getNombre(),
            cita.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            cita.getFechaHora().format(DateTimeFormatter.ofPattern("HH:mm")),
            cita.getServicios().stream()
                .map(s -> s.getNombre())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Sin servicios")
        );
    }
    
    private String construirMensajeConfirmacion(Cita cita) {
        return String.format("""
            ✅ *Confirmación de Cita - Lavadero Sepúlveda*
            
            Hola %s,
            
            Tu cita ha sido confirmada:
            📅 %s a las %s
            🔧 Servicios: %s
            💶 Precio estimado: %.2f €
            
            ¡Nos vemos pronto!
            """,
            cita.getCliente().getNombre(),
            cita.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            cita.getFechaHora().format(DateTimeFormatter.ofPattern("HH:mm")),
            cita.getServicios().stream()
                .map(s -> s.getNombre())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Sin servicios"),
            cita.getImporteTotal()
        );
    }
    
    public boolean testConnection() {
        try {
            // Test simple de conexión
            Request request = new Request.Builder()
                .url(apiUrl + "/" + phoneNumberId)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            log.error("Error al probar conexión WhatsApp", e);
            return false;
        }
    }
}

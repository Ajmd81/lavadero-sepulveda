package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.dto.ChatbotOption;
import com.lavaderosepulveda.app.dto.ChatbotResponse;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

@Service
public class ChatbotService {

    public ChatbotResponse process(String text, String intent) {

        if (intent == null || intent.isBlank()) {
            intent = detectIntent(text);
        }

        switch (intent) {

            case "HORARIO":
                return horario();

            case "RESERVA":
                return ChatbotResponse.builder()
                        .message("""
                        📅 **¿Cómo quieres pedir tu cita?**
                        """)
                        .options(List.of(
                                new ChatbotOption("📝 Formulario online", "RESERVA_FORM"),
                                new ChatbotOption("📞 Por teléfono", "CITA_TELEFONO"),
                                new ChatbotOption("💬 Por WhatsApp", "CITA_WHATSAPP"),
                                new ChatbotOption("🔙 Volver al menú", "MENU")
                        ))
                        .build();
            case "RESERVA_FORM":
                return ChatbotResponse.builder()
                        .message("📝 Te llevo al formulario de reserva.")
                        .formUrl("/nueva-cita")
                        .build();
            case "TARIFAS":
                return ChatbotResponse.builder()
                        .message("🔗 Te llevo a nuestra página de tarifas.")
                        .redirectUrl("/tarifas")
                        .build();

            case "UBICACION":
                return ChatbotResponse.builder()
                        .message("""
                        📍 **Dónde encontrarnos**
                
                        📌 C/ Ingeniero Ruiz de Azúa s/n, Local 8  
                        📍 Esquina con C/ Sor Ángela de la Cruz  
                        ⚽ Frente al campo de fútbol del Deportivo Córdoba  
                        🌳 Junto al Parque de la Asomadilla
                
                        ¿Cómo prefieres llegar?
                        """)
                        .options(List.of(
                                new ChatbotOption("🗺️ Ver en Google Maps", "VER_MAPA"),
                                new ChatbotOption("📅 Reservar cita", "RESERVA"),
                                new ChatbotOption("🔙 Volver al menú", "MENU")
                        ))
                        .build();
            case "VER_MAPA":
                return ChatbotResponse.builder()
                        .message("🗺️ Abriendo Google Maps…")
                        .redirectUrl(
                                "https://www.google.com/maps/search/?api=1&query=Lavadero+Sepúlveda+Córdoba"
                        )
                        .build();
            case "CITA_TELEFONO":
                return citaTelefono();

            case "CITA_WHATSAPP":
                return citaWhatsapp();

            case "MENU":
                return menuPrincipal();

            default:
                return ChatbotResponse.builder()
                        .message("🤔 No he entendido tu mensaje. ¿Qué te gustaría hacer?")
                        .options(menuOpciones())
                        .build();
        }
    }

    // ======================
    // INTENCIÓN DESDE TEXTO
    // ======================
    private String detectIntent(String text) {

        if (text == null) return "MENU";

        String normalized = normalize(text);

        if (normalized.contains("reserv") || normalized.contains("cita")) {
            return "RESERVA";
        }

        if (normalized.contains("tarifa")
                || normalized.contains("precio")
                || normalized.contains("cuesta")
                || normalized.contains("vale")) {
            return "TARIFAS";
        }

        if (normalized.contains("ubicacion") || normalized.contains("donde")) {
            return "UBICACION";
        }

        if (normalized.contains("menu") || normalized.contains("inicio")) {
            return "MENU";
        }

        if (normalized.contains("horario")
                || normalized.contains("abierto")
                || normalized.contains("abris")
                || normalized.contains("abrís")
                || normalized.contains("cierra")
                || normalized.contains("cerráis")) {
            return "HORARIO";
        }

        if (normalized.contains("telefono")
                || normalized.contains("llamar")
                || normalized.contains("llamada")) {
            return "CITA_TELEFONO";
        }

        if (normalized.contains("whatsapp")
                || normalized.contains("wasap")
                || normalized.contains("mensaje")) {
            return "CITA_WHATSAPP";
        }


        return "DESCONOCIDO";
    }

    private String normalize(String text) {
        return Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    // ======================
    // MENÚS
    // ======================

    private ChatbotResponse menuPrincipal() {
        return ChatbotResponse.builder()
                .message("""
                👋 ¡Hola! Soy tu asistente virtual de **Lavadero Sepúlveda**.
        
                ¿Qué te apetece hacer hoy?
                """)
                .options(menuOpciones())
                .build();
    }

    private List<ChatbotOption> menuOpciones() {
        return List.of(
                new ChatbotOption("🕒 Horario", "HORARIO"),
                new ChatbotOption("📅 Reservar cita", "RESERVA"),
                new ChatbotOption("📋 Ver tarifas", "TARIFAS"),
                new ChatbotOption("📍 Ubicación", "UBICACION")
        );
    }
    private ChatbotResponse horario() {
        return ChatbotResponse.builder()
                .message("""
        🕒 Te llevo al horario
        """)
                .formUrl("/horario")
                .build();
    }

    private ChatbotResponse tarifas() {
        return ChatbotResponse.builder()
                .message("""
        📋 **Nuestras tarifas**

        Puedes consultar todos los precios actualizados y servicios disponibles
        en nuestra página de tarifas.

        ¿Qué te gustaría hacer ahora?
        """)
                .options(List.of(
                        new ChatbotOption("📋 Ver tarifas completas", "VER_TARIFAS"),
                        new ChatbotOption("📅 Reservar cita", "RESERVA"),
                        new ChatbotOption("🔙 Volver al menú", "MENU")
                ))
                .build();
    }

    private static final String TELEFONO = "34658527186";

    private ChatbotResponse citaTelefono() {
        return ChatbotResponse.builder()
                .message("""
        📞 **Pedir cita por teléfono**

        Llámanos y te atenderemos encantados.
        """)
                .redirectUrl("tel:" + TELEFONO)
                .build();
    }

    private ChatbotResponse citaWhatsapp() {
        return ChatbotResponse.builder()
                .message("""
        💬 **Pedir cita por WhatsApp**

        Escríbenos y te responderemos lo antes posible.
        """)
                .redirectUrl("https://wa.me/" + TELEFONO)
                .build();
    }

}

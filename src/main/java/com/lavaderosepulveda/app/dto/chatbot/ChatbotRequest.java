package com.lavaderosepulveda.app.dto.chatbot;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChatbotRequest {

    @Size(max = 500, message = "El mensaje no puede superar 500 caracteres")
    private String message;

    @Size(max = 50)
    @Pattern(regexp = "^$|^[a-zA-Z0-9_\\-]+$", message = "Intent no válido")
    private String intent;

    @Size(max = 64)
    @Pattern(regexp = "^$|^[a-zA-Z0-9_\\-]+$", message = "sessionId no válido")
    private String sessionId;

    /** Hace falta al menos un mensaje o un intent (botón pulsado). */
    @AssertTrue(message = "Debes enviar un mensaje o elegir una opción")
    public boolean isMensajeOIntentPresente() {
        return (message != null && !message.isBlank()) || (intent != null && !intent.isBlank());
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}

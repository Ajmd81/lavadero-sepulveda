package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.ChatbotRequest;
import com.lavaderosepulveda.app.dto.ChatbotResponse;
import com.lavaderosepulveda.app.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * Controlador REST para el chatbot
 * Configurado para desarrollo y producción en Railway
 */
@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = {
        "https://lavadero-sepulveda-production.up.railway.app",
        "http://localhost:8080"
})
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    /**
     * Procesar mensaje del usuario
     */
    @PostMapping("/message")
    public ResponseEntity<ChatbotResponse> handleMessage(
            @RequestBody ChatbotRequest request) {

        ChatbotResponse response = chatbotService.process(
                request.getMessage(),
                request.getIntent()
        );

        return ResponseEntity.ok(response);
    }
}
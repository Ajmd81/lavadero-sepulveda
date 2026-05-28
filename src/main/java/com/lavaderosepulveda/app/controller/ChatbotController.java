package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.ChatbotRequest;
import com.lavaderosepulveda.app.dto.ChatbotResponse;
import com.lavaderosepulveda.app.security.ChatbotRateLimiter;
import com.lavaderosepulveda.app.service.ChatbotService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")

public class ChatbotController {

    private final ChatbotService chatbotService;
    private final ChatbotRateLimiter rateLimiter;

    public ChatbotController(ChatbotService chatbotService, ChatbotRateLimiter rateLimiter) {
        this.chatbotService = chatbotService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/message")
    public ResponseEntity<?> handleMessage(
            @RequestBody ChatbotRequest request,
            HttpServletRequest httpRequest) {

        String ip = obtenerIpReal(httpRequest);

        if (!rateLimiter.intentoPermitido(ip)) {
            long espera = rateLimiter.segundosHastaReset(ip);
            return ResponseEntity.status(429).body(Map.of(
                    "error", "Demasiados mensajes. Espera " + espera + " segundos.",
                    "retryAfter", espera
            ));
        }

        ChatbotResponse response = chatbotService.process(
                request.getMessage(),
                request.getIntent()
        );

        return ResponseEntity.ok(response);
    }

    private String obtenerIpReal(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
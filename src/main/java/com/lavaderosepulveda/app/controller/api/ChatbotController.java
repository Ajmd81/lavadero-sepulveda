package com.lavaderosepulveda.app.controller.api;

import com.lavaderosepulveda.app.dto.chatbot.ChatbotRequest;
import com.lavaderosepulveda.app.dto.chatbot.ChatbotResponse;
import com.lavaderosepulveda.app.security.ChatbotRateLimiter;
import com.lavaderosepulveda.app.security.ClientIpResolver;
import com.lavaderosepulveda.app.service.chatbot.ChatbotService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Chatbot público de la web. Antes quedaba bajo "/api/** autenticado" y el widget
 * no funcionaba para visitantes; ahora es público con rate limiting por IP y validación.
 */
@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final ChatbotRateLimiter rateLimiter;
    private final ClientIpResolver ipResolver;

    public ChatbotController(ChatbotService chatbotService, ChatbotRateLimiter rateLimiter,
                             ClientIpResolver ipResolver) {
        this.chatbotService = chatbotService;
        this.rateLimiter = rateLimiter;
        this.ipResolver = ipResolver;
    }

    @PostMapping("/message")
    public ResponseEntity<?> handleMessage(@Valid @RequestBody ChatbotRequest request,
                                           HttpServletRequest httpRequest) {

        String ip = ipResolver.resolver(httpRequest);
        if (!rateLimiter.intentoPermitido(ip)) {
            long espera = rateLimiter.segundosHastaReset(ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", String.valueOf(espera))
                    .body(Map.of(
                            "error", "Demasiados mensajes. Espera " + espera + " segundos.",
                            "retryAfter", espera));
        }

        ChatbotResponse response = chatbotService.process(request.getMessage(), request.getIntent());
        return ResponseEntity.ok(response);
    }
}

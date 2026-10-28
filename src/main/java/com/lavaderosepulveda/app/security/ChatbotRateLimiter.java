package com.lavaderosepulveda.app.security;

import com.lavaderosepulveda.app.security.RateLimitService.Politica;
import org.springframework.stereotype.Component;

/**
 * Rate limiter para el chatbot público: 10 mensajes / 5 min y 30 / hora por IP.
 */
@Component
public class ChatbotRateLimiter {

    private final RateLimitService rateLimitService;

    public ChatbotRateLimiter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    public boolean intentoPermitido(String ip) {
        return rateLimitService.permitido(Politica.CHATBOT_IP, ip);
    }

    public long segundosHastaReset(String ip) {
        return rateLimitService.segundosHastaReset(Politica.CHATBOT_IP, ip);
    }
}

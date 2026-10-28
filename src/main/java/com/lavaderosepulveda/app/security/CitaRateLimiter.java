package com.lavaderosepulveda.app.security;

import com.lavaderosepulveda.app.security.RateLimitService.Politica;
import org.springframework.stereotype.Component;

/**
 * Rate limiter específico para el formulario público de reservas (web y API).
 * 5 reservas / hora y 2 cada 5 minutos por IP.
 */
@Component
public class CitaRateLimiter {

    private final RateLimitService rateLimitService;

    public CitaRateLimiter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    public boolean intentoPermitido(String ip) {
        return rateLimitService.permitido(Politica.CITA_IP, ip);
    }

    public long segundosHastaReset(String ip) {
        return rateLimitService.segundosHastaReset(Politica.CITA_IP, ip);
    }
}

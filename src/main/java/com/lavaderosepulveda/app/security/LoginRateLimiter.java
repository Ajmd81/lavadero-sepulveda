package com.lavaderosepulveda.app.security;

import com.lavaderosepulveda.app.security.RateLimitService.Politica;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Rate limiting del login.
 *  - Por IP: 5 intentos / 15 min y 10 / hora (cualquier intento).
 *  - Por usuario: 10 intentos FALLIDOS / 30 min, venga de la IP que venga
 *    (sin esto, un ataque repartido entre muchas IPs podía probar contraseñas sin freno).
 */
@Component
public class LoginRateLimiter {

    private final RateLimitService rateLimitService;

    public LoginRateLimiter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    public boolean intentoPermitido(String ip) {
        return rateLimitService.permitido(Politica.LOGIN_IP, ip);
    }

    public long segundosHastaReset(String ip) {
        return rateLimitService.segundosHastaReset(Politica.LOGIN_IP, ip);
    }

    public boolean usuarioBloqueado(String username) {
        return rateLimitService.bloqueado(Politica.LOGIN_USUARIO, clave(username));
    }

    public long segundosHastaDesbloqueoUsuario(String username) {
        return rateLimitService.segundosHastaReset(Politica.LOGIN_USUARIO, clave(username));
    }

    public void registrarFallo(String username) {
        rateLimitService.consumir(Politica.LOGIN_USUARIO, clave(username));
    }

    public void registrarExito(String username) {
        rateLimitService.reiniciar(Politica.LOGIN_USUARIO, clave(username));
    }

    private String clave(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}

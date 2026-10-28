package com.lavaderosepulveda.app.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Lista de tokens revocados por logout (por su jti), hasta que caducan solos.
 *
 * Así "Cerrar sesión" invalida de verdad el token de ESE dispositivo sin cerrar los demás.
 * Para cerrar TODAS las sesiones (cambio de contraseña, "cerrar sesión en todos los
 * dispositivos") se usa Usuario.tokenVersion, que sí está en BD.
 *
 * Limitación conocida: vive en memoria; si el contenedor se reinicia, un token revocado
 * volvería a valer hasta su expiración (máx. jwt.expiration-minutes). Por eso los tokens
 * duran poco.
 */
@Component
public class TokenBlacklist {

    private final Cache<String, Instant> revocados = Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfter(new Expiry<String, Instant>() {
                @Override
                public long expireAfterCreate(String key, Instant expira, long currentTime) {
                    long nanos = (expira.toEpochMilli() - System.currentTimeMillis()) * 1_000_000L;
                    return Math.max(nanos, 0);
                }

                @Override
                public long expireAfterUpdate(String key, Instant expira, long currentTime, long currentDuration) {
                    return expireAfterCreate(key, expira, currentTime);
                }

                @Override
                public long expireAfterRead(String key, Instant expira, long currentTime, long currentDuration) {
                    return currentDuration;
                }
            })
            .build();

    public void revocar(String jti, Instant expiracion) {
        if (jti != null && expiracion != null && expiracion.isAfter(Instant.now())) {
            revocados.put(jti, expiracion);
        }
    }

    public boolean estaRevocado(String jti) {
        return jti != null && revocados.getIfPresent(jti) != null;
    }
}

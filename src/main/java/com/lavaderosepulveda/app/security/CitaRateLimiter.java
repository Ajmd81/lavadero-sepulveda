package com.lavaderosepulveda.app.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter específico para el formulario público de reservas.
 * Límites más permisivos que el login pero suficientes para bloquear bots.
 */
@Component
public class CitaRateLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket crearBucket() {
        return Bucket.builder()
                // Máximo 5 reservas por hora por IP — más que suficiente para un cliente real
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofHours(1))))
                // Máximo 2 reservas en 5 minutos — frena ráfagas de bots
                .addLimit(Bandwidth.classic(2, Refill.intervally(2, Duration.ofMinutes(5))))
                .build();
    }

    public boolean intentoPermitido(String ip) {
        Bucket bucket = buckets.computeIfAbsent(ip, k -> crearBucket());
        return bucket.tryConsume(1);
    }

    public long segundosHastaReset(String ip) {
        Bucket bucket = buckets.get(ip);
        if (bucket == null) return 0;
        long nanos = bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill();
        return nanos / 1_000_000_000L;
    }
}
package com.lavaderosepulveda.app.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter para el chatbot público.
 * Límites: 10 mensajes / 5 minutos por IP.
 * Más permisivo que el login pero necesario para evitar abuso de la IA.
 */
@Component
public class ChatbotRateLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket crearBucket() {
        return Bucket.builder()
                // 10 mensajes cada 5 minutos
                .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(5))))
                // Máximo 30 mensajes por hora (frena abusos sostenidos)
                .addLimit(Bandwidth.classic(30, Refill.intervally(30, Duration.ofHours(1))))
                .build();
    }

    public boolean intentoPermitido(String ip) {
        Bucket bucket = buckets.computeIfAbsent(ip, k -> crearBucket());
        return bucket.tryConsume(1);
    }

    public long segundosHastaReset(String ip) {
        Bucket bucket = buckets.get(ip);
        if (bucket == null) return 0;
        return bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill() / 1_000_000_000L;
    }
}
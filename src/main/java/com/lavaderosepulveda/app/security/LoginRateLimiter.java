package com.lavaderosepulveda.app.security;

import io.github.bucket4j.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    // Un bucket por IP — se limpia automáticamente cuando el token se recarga
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket crearBucket() {
        return Bucket.builder()
            // 5 intentos cada 15 minutos
            .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(15))))
            // Máximo 10 intentos por hora (segunda capa)
            .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofHours(1))))
            .build();
    }

    public boolean intentoPermitido(String ip) {
        Bucket bucket = buckets.computeIfAbsent(ip, k -> crearBucket());
        return bucket.tryConsume(1);
    }

    public long segundosHastaReset(String ip) {
        Bucket bucket = buckets.get(ip);
        if (bucket == null) return 0;
        return bucket.getAvailableTokens() > 0 ? 0 :
            bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill() / 1_000_000_000L;
    }
}
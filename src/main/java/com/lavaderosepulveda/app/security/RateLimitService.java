package com.lavaderosepulveda.app.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Almacén central de buckets de rate limiting (Bucket4j).
 *
 * Cada política tiene su propia caché Caffeine con expiración por inactividad y tamaño
 * máximo: antes eran ConcurrentHashMap que crecían sin límite (un bot rotando IPs podía
 * llenar la memoria del contenedor).
 *
 * Nota: el estado vive en memoria de la instancia. Con una sola réplica en Railway es
 * suficiente; si escalas a varias, habría que mover esto a Redis (bucket4j-redis).
 */
@Service
public class RateLimitService {

    public enum Politica {
        /** Toda la API, por IP, peticiones anónimas */
        API_IP_ANONIMO,
        /** Toda la API, por IP, peticiones autenticadas (techo por si roban un token) */
        API_IP_AUTENTICADO,
        /** Toda la API, por usuario autenticado (independiente de la IP) */
        API_USUARIO,
        /** Intentos de login por IP */
        LOGIN_IP,
        /** Intentos FALLIDOS de login por nombre de usuario (frena ataques distribuidos) */
        LOGIN_USUARIO,
        /** Reservas públicas por IP */
        CITA_IP,
        /** Mensajes al chatbot por IP */
        CHATBOT_IP
    }

    private final Map<Politica, Cache<String, Bucket>> caches = new EnumMap<>(Politica.class);
    private final Map<Politica, Supplier<Bucket>> fabricas = new EnumMap<>(Politica.class);

    public RateLimitService(
            @Value("${app.rate-limit.api.anonimo-por-minuto:60}") long anonimoPorMinuto,
            @Value("${app.rate-limit.api.ip-autenticado-por-minuto:300}") long ipAutenticadoPorMinuto,
            @Value("${app.rate-limit.api.usuario-por-minuto:240}") long usuarioPorMinuto,
            @Value("${app.rate-limit.api.usuario-por-hora:6000}") long usuarioPorHora) {

        registrar(Politica.API_IP_ANONIMO, () -> Bucket.builder()
                .addLimit(Bandwidth.classic(anonimoPorMinuto, Refill.greedy(anonimoPorMinuto, Duration.ofMinutes(1))))
                // ráfaga corta: como mucho 20 peticiones en 5 segundos
                .addLimit(Bandwidth.classic(20, Refill.greedy(20, Duration.ofSeconds(5))))
                .build());

        registrar(Politica.API_IP_AUTENTICADO, () -> Bucket.builder()
                .addLimit(Bandwidth.classic(ipAutenticadoPorMinuto, Refill.greedy(ipAutenticadoPorMinuto, Duration.ofMinutes(1))))
                .build());

        registrar(Politica.API_USUARIO, () -> Bucket.builder()
                .addLimit(Bandwidth.classic(usuarioPorMinuto, Refill.greedy(usuarioPorMinuto, Duration.ofMinutes(1))))
                .addLimit(Bandwidth.classic(usuarioPorHora, Refill.greedy(usuarioPorHora, Duration.ofHours(1))))
                .build());

        registrar(Politica.LOGIN_IP, () -> Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(15))))
                .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofHours(1))))
                .build());

        registrar(Politica.LOGIN_USUARIO, () -> Bucket.builder()
                .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(30))))
                .build());

        registrar(Politica.CITA_IP, () -> Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofHours(1))))
                .addLimit(Bandwidth.classic(2, Refill.intervally(2, Duration.ofMinutes(5))))
                .build());

        registrar(Politica.CHATBOT_IP, () -> Bucket.builder()
                .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(5))))
                .addLimit(Bandwidth.classic(30, Refill.intervally(30, Duration.ofHours(1))))
                .build());
    }

    private void registrar(Politica politica, Supplier<Bucket> fabrica) {
        fabricas.put(politica, fabrica);
        caches.put(politica, Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(2))
                .maximumSize(50_000)
                .build());
    }

    private Bucket bucket(Politica politica, String clave) {
        return caches.get(politica).get(clave, k -> fabricas.get(politica).get());
    }

    /** Consume 1 token y devuelve el resultado (permitido, restantes, espera). */
    public ConsumptionProbe consumir(Politica politica, String clave) {
        return bucket(politica, clave).tryConsumeAndReturnRemaining(1);
    }

    public boolean permitido(Politica politica, String clave) {
        return consumir(politica, clave).isConsumed();
    }

    /** Comprueba sin consumir (para saber si una cuenta está bloqueada antes de validar la contraseña). */
    public boolean bloqueado(Politica politica, String clave) {
        return bucket(politica, clave).getAvailableTokens() <= 0;
    }

    public long segundosHastaReset(Politica politica, String clave) {
        Bucket b = caches.get(politica).getIfPresent(clave);
        if (b == null) return 0;
        long nanos = b.estimateAbilityToConsume(1).getNanosToWaitForRefill();
        return Math.max(1, (long) Math.ceil(nanos / 1_000_000_000.0));
    }

    /** Borra el contador (p.ej. tras un login correcto se perdonan los fallos de ese usuario). */
    public void reiniciar(Politica politica, String clave) {
        caches.get(politica).invalidate(clave);
    }
}

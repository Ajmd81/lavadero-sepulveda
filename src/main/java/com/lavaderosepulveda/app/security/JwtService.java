package com.lavaderosepulveda.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMillis;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-hours:8}") long expirationHours) {

        // La clave debe tener al menos 64 bytes para HS512
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationHours * 3_600_000L;
    }

    // ─── GENERAR TOKEN ────────────────────────────────────────────────────────

    public String generarToken(String username) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(username)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    // ─── EXTRAER USERNAME ─────────────────────────────────────────────────────

    public String extraerUsername(String token) {
        return parsearClaims(token).getSubject();
    }

    // ─── VALIDAR TOKEN ────────────────────────────────────────────────────────

    /**
     * Valida firma, formato y expiración en un solo paso.
     * Lanza JwtException (o sus subclases) si algo falla.
     */
    public boolean esValido(String token) {
        try {
            parsearClaims(token); // lanza excepción si es inválido o expirado
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ─── PRIVADO ──────────────────────────────────────────────────────────────

    private Claims parsearClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
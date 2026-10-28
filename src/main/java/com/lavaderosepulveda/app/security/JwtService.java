package com.lavaderosepulveda.app.security;

import com.lavaderosepulveda.app.model.auth.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Emisión y validación de JWT.
 *
 * Cambios respecto a la versión anterior:
 *  - El "sub" es el ID del usuario (antes el username: si alguien cambiaba su username y otro
 *    usuario adoptaba el antiguo, los tokens viejos pasaban a autenticar al usuario nuevo).
 *  - Claim "ver" = Usuario.tokenVersion → cambiar contraseña o "cerrar todas las sesiones"
 *    invalida todos los tokens emitidos antes.
 *  - Claim "jti" → permite revocar un token concreto al hacer logout.
 *  - Caducidad corta (30 min por defecto) renovable con /api/auth/refresh mientras haya
 *    actividad, con un máximo absoluto de sesión (8 h) contado desde el login ("auth_time").
 *    Resultado: si el usuario deja el CRM abierto sin usarlo, la sesión muere sola.
 *  - Se exige issuer y se valida la longitud de la clave al arrancar (fail-fast).
 */
@Component
public class JwtService {

    private static final String ISSUER = "lavadero-sepulveda";
    private static final String CLAIM_USERNAME = "usr";
    private static final String CLAIM_VERSION = "ver";
    private static final String CLAIM_AUTH_TIME = "auth_time";

    private final SecretKey secretKey;
    private final long expirationMillis;
    private final long maxSessionMillis;

    public record TokenInfo(Long userId, String username, int version,
                            Instant authTime, String jti, Instant expiresAt) {}

    public record TokenEmitido(String token, Instant expiresAt) {}

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-minutes:30}") long expirationMinutes,
            @Value("${jwt.max-session-hours:8}") long maxSessionHours) {

        byte[] bytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 64) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos 64 bytes para HS512 (genera uno con: openssl rand -base64 64)");
        }
        this.secretKey = Keys.hmacShaKeyFor(bytes);
        this.expirationMillis = expirationMinutes * 60_000L;
        this.maxSessionMillis = maxSessionHours * 3_600_000L;
    }

    // ─── EMITIR ───────────────────────────────────────────────────────────────

    /** Token nuevo tras un login: la sesión empieza ahora. */
    public TokenEmitido generarToken(Usuario usuario) {
        return generarToken(usuario, Instant.now());
    }

    /**
     * Token renovado: conserva el auth_time original, así la sesión nunca supera
     * jwt.max-session-hours aunque se renueve continuamente.
     */
    public TokenEmitido generarToken(Usuario usuario, Instant authTime) {
        long ahora = System.currentTimeMillis();
        long limiteSesion = authTime.toEpochMilli() + maxSessionMillis;
        long expira = Math.min(ahora + expirationMillis, limiteSesion);

        String token = Jwts.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(usuario.getId()))
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_USERNAME, usuario.getUsername())
                .claim(CLAIM_VERSION, versionDe(usuario))
                .claim(CLAIM_AUTH_TIME, authTime.getEpochSecond())
                .issuedAt(new Date(ahora))
                .expiration(new Date(expira))
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();

        return new TokenEmitido(token, Instant.ofEpochMilli(expira));
    }

    /** ¿Se puede renovar todavía, o ya se alcanzó el máximo absoluto de sesión? */
    public boolean sesionRenovable(TokenInfo info) {
        return Instant.now().isBefore(info.authTime().plusMillis(maxSessionMillis));
    }

    public static int versionDe(Usuario usuario) {
        return usuario.getTokenVersion() == null ? 0 : usuario.getTokenVersion();
    }

    // ─── VALIDAR ──────────────────────────────────────────────────────────────

    /**
     * Valida firma, issuer, formato y expiración. Devuelve vacío si algo falla.
     * (La comprobación contra BD —usuario activo y versión— la hace JwtAuthFilter.)
     */
    public Optional<TokenInfo> validar(String token) {
        if (token == null || token.isBlank() || token.length() > 4096) return Optional.empty();
        try {
            Claims c = Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(ISSUER)
                    .clockSkewSeconds(30)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = Long.valueOf(c.getSubject());
            Number ver = c.get(CLAIM_VERSION, Number.class);
            Number authTime = c.get(CLAIM_AUTH_TIME, Number.class);
            if (ver == null || authTime == null || c.getId() == null) return Optional.empty();

            return Optional.of(new TokenInfo(
                    userId,
                    c.get(CLAIM_USERNAME, String.class),
                    ver.intValue(),
                    Instant.ofEpochSecond(authTime.longValue()),
                    c.getId(),
                    c.getExpiration().toInstant()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** Extrae el token de la cabecera "Authorization: Bearer xxx". */
    public static Optional<String> extraerBearer(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return Optional.empty();
        String t = authHeader.substring(7).trim();
        return t.isEmpty() ? Optional.empty() : Optional.of(t);
    }
}

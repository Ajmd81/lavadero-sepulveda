package com.lavaderosepulveda.app.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Obtiene la IP real del cliente de forma NO falsificable.
 *
 * Antes se usaba el PRIMER valor de X-Forwarded-For, que lo escribe el propio cliente:
 * cualquiera podía mandar "X-Forwarded-For: 1.2.3.4" distinto en cada petición y saltarse
 * todos los rate limiters.
 *
 * Orden de confianza (Railway):
 *   1. X-Real-IP  → lo sobrescribe el edge de Railway, el cliente no puede fijarlo.
 *   2. Último valor de X-Forwarded-For → lo añade el último proxy (el de confianza).
 *   3. getRemoteAddr() → conexión directa (local / tests).
 *
 * Si algún día pones Cloudflare delante, cambia app.security.client-ip-header a CF-Connecting-IP.
 */
@Component
public class ClientIpResolver {

    // IPv4 o IPv6 razonables; cualquier otra cosa se descarta (evita claves basura en los buckets)
    private static final Pattern IP_VALIDA = Pattern.compile("^[0-9a-fA-F:.]{2,45}$");

    private final String trustedHeader;

    public ClientIpResolver(@Value("${app.security.client-ip-header:X-Real-IP}") String trustedHeader) {
        this.trustedHeader = trustedHeader;
    }

    public String resolver(HttpServletRequest request) {
        String ip = limpiar(request.getHeader(trustedHeader));
        if (ip != null) return ip;

        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String[] partes = xff.split(",");
            ip = limpiar(partes[partes.length - 1]);
            if (ip != null) return ip;
        }

        String remota = limpiar(request.getRemoteAddr());
        return remota != null ? remota : "desconocida";
    }

    private String limpiar(String valor) {
        if (valor == null) return null;
        String v = valor.trim();
        return IP_VALIDA.matcher(v).matches() ? v : null;
    }
}

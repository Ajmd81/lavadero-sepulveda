package com.lavaderosepulveda.app.security;

import com.lavaderosepulveda.app.security.RateLimitService.Politica;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Rate limiting global para toda la API (y los formularios POST de la web).
 *
 * Se ejecuta DESPUÉS de JwtAuthFilter, así que ya sabe si la petición es de un usuario:
 *  - Anónima      → límite por IP (API_IP_ANONIMO).
 *  - Autenticada  → límite por usuario (API_USUARIO) + techo por IP (API_IP_AUTENTICADO).
 *
 * Los límites específicos (login, reservas, chatbot) siguen aplicándose además en sus
 * controladores, que son más estrictos.
 *
 * No se registra como @Component para que no se ejecute dos veces (servlet + cadena de seguridad):
 * se instancia en SecurityConfig.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimitService rateLimitService;
    private final ClientIpResolver ipResolver;

    public RateLimitFilter(RateLimitService rateLimitService, ClientIpResolver ipResolver) {
        this.rateLimitService = rateLimitService;
        this.ipResolver = ipResolver;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) return true; // preflight CORS
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/")) return false;
        // Formularios públicos de la web (Thymeleaf)
        return !(HttpMethod.POST.matches(request.getMethod())
                && (uri.equals("/guardar-cita") || uri.equals("/admin/login")));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        String ip = ipResolver.resolver(request);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser usuario = (auth != null && auth.getPrincipal() instanceof AuthenticatedUser u) ? u : null;

        ConsumptionProbe probe;
        Politica politica;
        String clave;

        if (usuario != null) {
            politica = Politica.API_USUARIO;
            clave = "u:" + usuario.id();
            probe = rateLimitService.consumir(politica, clave);
            if (probe.isConsumed()) {
                ConsumptionProbe probeIp = rateLimitService.consumir(Politica.API_IP_AUTENTICADO, ip);
                if (!probeIp.isConsumed()) {
                    probe = probeIp;
                    politica = Politica.API_IP_AUTENTICADO;
                    clave = ip;
                }
            }
        } else {
            politica = Politica.API_IP_ANONIMO;
            clave = ip;
            probe = rateLimitService.consumir(politica, clave);
        }

        if (!probe.isConsumed()) {
            long espera = Math.max(1, (long) Math.ceil(probe.getNanosToWaitForRefill() / 1_000_000_000.0));
            log.warn("Rate limit {} superado: clave={} ip={} {} {}", politica, clave, ip,
                    request.getMethod(), request.getRequestURI());
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(espera));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(
                    "{\"error\":\"Demasiadas peticiones. Espera " + espera + " segundos.\",\"retryAfter\":" + espera + "}");
            return;
        }

        response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
        chain.doFilter(request, response);
    }
}

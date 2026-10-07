package com.lavaderosepulveda.app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Unifica el dominio para SEO:
 *   http://lavaderosepulveda.es/...      -> https://www.lavaderosepulveda.es/...
 *   https://lavaderosepulveda.es/...     -> https://www.lavaderosepulveda.es/...
 *   http://www.lavaderosepulveda.es/...  -> https://www.lavaderosepulveda.es/...
 *
 * No toca otros hosts (dominio de Railway, localhost, app móvil contra la API).
 * Se ejecuta antes que Spring Security.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CanonicalHostFilter extends OncePerRequestFilter {

    private static final String HOST_CANONICO = "www.lavaderosepulveda.es";
    private static final String HOST_SIN_WWW = "lavaderosepulveda.es";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String host = request.getServerName() == null ? "" : request.getServerName().toLowerCase();
        String proto = request.getHeader("X-Forwarded-Proto");
        boolean esHttp = "http".equalsIgnoreCase(proto);

        boolean redirigir = host.equals(HOST_SIN_WWW) || (host.equals(HOST_CANONICO) && esHttp);

        if (redirigir) {
            String query = request.getQueryString();
            String destino = "https://" + HOST_CANONICO + request.getRequestURI()
                    + (query != null ? "?" + query : "");
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            response.setHeader("Location", destino);
            return;
        }

        chain.doFilter(request, response);
    }
}
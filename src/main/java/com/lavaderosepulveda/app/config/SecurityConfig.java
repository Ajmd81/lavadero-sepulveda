package com.lavaderosepulveda.app.config;

import com.lavaderosepulveda.app.security.ClientIpResolver;
import com.lavaderosepulveda.app.security.JwtAuthFilter;
import com.lavaderosepulveda.app.security.RateLimitFilter;
import com.lavaderosepulveda.app.security.RateLimitService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad centralizada.
 *
 * Mapa de acceso (todo lo que no esté en la lista pública de la API requiere JWT):
 *
 *  API PÚBLICA (formulario de reservas, chatbot, login) — siempre con rate limiting:
 *    POST /api/auth/login, POST /api/auth/logout
 *    POST /api/citas                       (reservar; campos internos se ignoran si no hay JWT)
 *    POST /api/chatbot/message
 *    GET  /api/citas/horarios-disponibles, /api/citas/disponibilidad-mes(nsual)
 *    GET  /api/horarios, /api/tipos-lavado, /api/enums/**
 *    GET  /api/dias-cerrados/fechas-cerradas
 *    GET  /api/vehicle/classify, /classify-by-id, /brands-models
 *
 *  API PROTEGIDA: resto de /api/** → JWT válido (401 JSON si falta o caducó).
 *  ADMIN THYMELEAF: /admin/** → autenticado.
 *  WEB PÚBLICA: páginas Thymeleaf, estáticos, SEO.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // sin esto los @PreAuthorize de los controladores se ignoraban
public class SecurityConfig {

    private static final RequestMatcher API = AntPathRequestMatcher.antMatcher("/api/**");

    private final JwtAuthFilter jwtAuthFilter;
    private final RateLimitService rateLimitService;
    private final ClientIpResolver clientIpResolver;

    @Value("${app.cors.allowed-origins}")
    private String allowedOriginsStr;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          RateLimitService rateLimitService,
                          ClientIpResolver clientIpResolver) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.rateLimitService = rateLimitService;
        this.clientIpResolver = clientIpResolver;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ─── CORS ─────────────────────────────────────────────────────────────────
    /**
     * Solo se aplica a /api/** (las páginas Thymeleaf son del mismo origen y no lo necesitan).
     *  - Orígenes exactos desde APP_CORS_ALLOWED_ORIGINS; nunca "*" ni patrones.
     *  - Sin credenciales: el JWT viaja en la cabecera Authorization, no en cookies.
     *  - Cabeceras y métodos explícitos en lugar de "*".
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origins = Arrays.stream(allowedOriginsStr.split(","))
                .map(String::trim)
                .filter(o -> !o.isEmpty())
                .toList();

        if (origins.stream().anyMatch(o -> o.contains("*"))) {
            throw new IllegalStateException("app.cors.allowed-origins no puede contener comodines: " + origins);
        }

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, "X-Requested-With"));
        configuration.setExposedHeaders(List.of(
                HttpHeaders.CONTENT_DISPOSITION, HttpHeaders.RETRY_AFTER, "X-RateLimit-Remaining",
                // Cabeceras de los ficheros BOE (Modelo 303/130)
                "X-Tipo-Declaracion", "X-Resultado", "X-Cuotas-Pendientes-Compensar"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    // ─── CADENA DE FILTROS ────────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ─── Headers de seguridad HTTP ──────────────────────────────────────
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .contentTypeOptions(ct -> {})
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://unpkg.com https://cdnjs.cloudflare.com; " +
                                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.jsdelivr.net https://unpkg.com https://cdnjs.cloudflare.com; " +
                                "font-src 'self' https://fonts.gstatic.com https://cdnjs.cloudflare.com; " +
                                "img-src 'self' data:; " +
                                "frame-src https://www.google.com https://maps.google.com; " +
                                "connect-src 'self' https://lavadero-sepulveda-production.up.railway.app; " +
                                "frame-ancestors 'none'"
                        ))
                )

                // ─── AUTORIZACIÓN ──────────────────────────────────────────────────
                .authorizeHttpRequests(authz -> authz
                        // Preflight CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()

                        // 1️⃣ API PÚBLICA — cada regla limitada al MÉTODO que necesita
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/citas").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/chatbot/message").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/citas/horarios-disponibles",
                                "/api/citas/disponibilidad-mes",
                                "/api/citas/disponibilidad-mensual",
                                "/api/horarios",
                                "/api/tipos-lavado",
                                "/api/enums/**",
                                "/api/dias-cerrados/fechas-cerradas",
                                "/api/vehicle/classify",
                                "/api/vehicle/classify-by-id",
                                "/api/vehicle/brands-models"
                        ).permitAll()

                        // 2️⃣ API PROTEGIDA — todo lo demás (incluye GET /api/citas con datos de clientes)
                        .requestMatchers("/api/**").authenticated()

                        // 3️⃣ WEB PÚBLICA — páginas Thymeleaf
                        .requestMatchers(
                                "/",
                                "/nueva-cita",
                                "/guardar-cita",
                                "/confirmacion",
                                "/horarios-disponibles",
                                "/horario",
                                "/galeria",
                                "/productos",
                                "/tarifas",
                                "/policy",
                                "/chatbot"
                        ).permitAll()

                        // 4️⃣ ESTÁTICOS y SEO
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers(
                                "/robots.txt",
                                "/sitemap.xml",
                                "/index.html",
                                "/index.php",
                                "/contacto.html",
                                "/horario.html",
                                "/horarios.html",
                                "/galeria.html",
                                "/productos.html",
                                "/tarifas.html",
                                "/precios.html",
                                "/reservas.html",
                                "/cita.html"
                        ).permitAll()

                        // 5️⃣ ADMIN Thymeleaf
                        .requestMatchers("/admin/**").authenticated()

                        // 6️⃣ Resto (páginas sueltas, /error, verificación de Google...)
                        .anyRequest().permitAll()
                )

                // ─── Respuestas JSON para la API (antes: 403 genérico o redirección al login HTML) ──
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(apiAuthenticationEntryPoint(), API)
                        .defaultAccessDeniedHandlerFor(apiAccessDeniedHandler(), API)
                )

                // ─── Form Login (para /admin/** Thymeleaf) ─────────────────────────
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .defaultSuccessUrl("/admin/citas-por-estado", true)
                        .failureUrl("/admin/login?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // ─── CSRF: la API usa JWT en cabecera (no cookies) → no aplica ─────
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

                // ─── Filtros: JWT → rate limiting (ya sabe quién es el usuario) ────
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new RateLimitFilter(rateLimitService, clientIpResolver), JwtAuthFilter.class);

        return http.build();
    }

    private AuthenticationEntryPoint apiAuthenticationEntryPoint() {
        return (request, response, authException) ->
                escribirJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "{\"error\":\"No autenticado o sesión caducada\"}");
    }

    private AccessDeniedHandler apiAccessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                escribirJson(response, HttpServletResponse.SC_FORBIDDEN, "{\"error\":\"Acceso denegado\"}");
    }

    private static void escribirJson(HttpServletResponse response, int status, String json) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(json);
    }
}

package com.lavaderosepulveda.app.config;

import com.lavaderosepulveda.app.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    // Leído de application.properties / variable de entorno Railway
    @Value("${app.cors.allowed-origins}")
    private String allowedOriginsStr;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Leer orígenes desde variable de entorno en lugar de hardcodearlos
        List<String> origins = Arrays.asList(allowedOriginsStr.split(","));
        configuration.setAllowedOrigins(origins);

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Para rutas /api/** sin autenticación válida, devolver 401 (no 403),
        // que es lo que el interceptor de axios del CRM espera para redirigir a login.
        AuthenticationEntryPoint apiEntryPoint = (request, response, authException) ->
                response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED, "No autenticado");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                apiEntryPoint,
                                request -> request.getServletPath().startsWith("/api/")))

                // ── Headers de seguridad HTTP ─────────────────────────────────
                .headers(headers -> headers

                        // Evita que la página se cargue en un iframe (clickjacking)
                        .frameOptions(frame -> frame.deny())

                        // Fuerza HTTPS durante 1 año, incluyendo subdominios
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))

                        // Evita que el navegador "adivine" el tipo de contenido
                        .contentTypeOptions(ct -> {})

                        // No enviar el referrer completo a sitios externos
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))

                        // Content Security Policy — restringe de dónde se pueden cargar recursos
                        // 'unsafe-inline' necesario para los estilos inline de Thymeleaf/Flatpickr
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://npmcdn.com https://cdnjs.cloudflare.com; " +
                                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.jsdelivr.net https://npmcdn.com https://cdnjs.cloudflare.com; " +
                                "font-src 'self' https://fonts.gstatic.com https://cdnjs.cloudflare.com; " +
                                "img-src 'self' data:; " +
                                "connect-src 'self' https://lavadero-sepulveda-production.up.railway.app; " +
                                "frame-ancestors 'none'"
                        ))
                )

                .authorizeHttpRequests(authz -> authz

                        // ════════════════════════════════════════════════════════════
                        // RUTAS PÚBLICAS — lista blanca explícita
                        // Cualquier endpoint nuevo que se cree quedará protegido por
                        // defecto a menos que se añada aquí explícitamente.
                        // ════════════════════════════════════════════════════════════

                        // Autenticación
                        .requestMatchers("/api/auth/login").permitAll()

                        // Vistas públicas Thymeleaf
                        .requestMatchers("/", "/nueva-cita", "/guardar-cita", "/confirmacion",
                                "/horario", "/galeria", "/productos", "/tarifas", "/policy",
                                "/chatbot", "/google33709fbb5cab4955.html").permitAll()

                        // Recursos estáticos
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                        // Formulario público de reserva — disponibilidad y clasificación de vehículo
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/horarios-disponibles",
                                "/api/modelos",
                                "/api/citas/horarios-disponibles",
                                "/api/citas/disponibilidad-mensual",
                                "/api/citas/verificar-disponibilidad",
                                "/api/dias-cerrados/fechas-cerradas",
                                "/api/vehicle/brands-models",
                                "/api/vehicle/classify-by-id",
                                "/api/vehicle/classify",
                                "/api/models/all",
                                "/api/models/search",
                                "/api/tipos-lavado"
                        ).permitAll()

                        // Crear cita desde el formulario público (el propio CitaApiController
                        // ya aplica honeypot + rate limiting internamente)
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/citas").permitAll()

                        // Chatbot público
                        .requestMatchers("/api/chatbot/**").permitAll()

                        // ════════════════════════════════════════════════════════════
                        // ADMIN clásico (login por formulario, no JWT)
                        // ════════════════════════════════════════════════════════════
                        .requestMatchers("/admin/login").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // ════════════════════════════════════════════════════════════
                        // TODO LO DEMÁS — incluye el resto de /api/** (CRM):
                        // citas (listado/edición/borrado), clientes, facturas, gastos,
                        // proveedores, albaranes, días cerrados (admin), configuración,
                        // migración, etc. — requiere JWT válido.
                        // ════════════════════════════════════════════════════════════
                        .anyRequest().authenticated()
                )
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
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
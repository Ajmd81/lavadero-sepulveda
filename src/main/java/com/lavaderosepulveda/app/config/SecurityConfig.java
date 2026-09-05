package com.lavaderosepulveda.app.config;

import com.lavaderosepulveda.app.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad centralizada para la aplicación.
 * 
 * Estructura de acceso:
 * 1. PÚBLICOS: /api/auth/**, /api/vehicle/**, /api/dias-cerrados/**, /api/enums/**, /api/citas (GET/POST)
 * 2. PROTEGIDOS: Todo /api/** excepto los públicos (requiere JWT)
 * 3. ADMIN: /admin/** (requiere sesión + rol ADMIN)
 * 4. ESTÁTICOS: /css/**, /js/**, etc.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

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
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

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
                                "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://npmcdn.com https://unpkg.com https://cdnjs.cloudflare.com; " +
                                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.jsdelivr.net https://npmcdn.com https://unpkg.com https://cdnjs.cloudflare.com; " +
                                "font-src 'self' https://fonts.gstatic.com https://cdnjs.cloudflare.com; " +
                                "img-src 'self' data:; " +
                                "frame-src https://www.google.com https://maps.google.com; " +
                                "connect-src 'self' https://lavadero-sepulveda-production.up.railway.app; " +
                                "frame-ancestors 'none'"
                        ))
                )

                // ─── AUTORIZACIÓN (CONSOLIDADA - Sin duplicación) ──────────────────
                .authorizeHttpRequests(authz -> authz
                        // 1️⃣ PÚBLICOS - API sin autenticación
                        // ───────────────────────────────────
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/logout").permitAll()
                        .requestMatchers("/api/vehicle/**").permitAll()
                        .requestMatchers("/api/dias-cerrados/**").permitAll()
                        .requestMatchers("/api/enums/**").permitAll()  // ✅ TIPOS DE PAGO Y LAVADO
                        .requestMatchers("/api/citas").permitAll()  // GET citas públicas
                        .requestMatchers(HttpMethod.POST, "/api/citas").permitAll()  // POST crear cita pública
                        
                        // 2️⃣ PROTEGIDOS - Todo /api/** restante requiere JWT
                        // ─────────────────────────────────────────────────
                        .requestMatchers("/api/**").authenticated()  // ✅ UNA SOLA VEZ
                        
                        // 3️⃣ PÚBLICOS - Páginas Thymeleaf
                        // ─────────────────────────────────
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
                        
                        // 4️⃣ ESTÁTICOS
                        // ──────────────
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        
                        // 5️⃣ ADMIN - Requiere sesión + rol ADMIN
                        // ───────────────────────────────────────
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        
                        // 6️⃣ LO DEMÁS
                        // ───────────
                        .anyRequest().permitAll()
                )
                
                // ─── Form Login (para /admin/**) ───────────────────────────────────
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .defaultSuccessUrl("/admin/citas-por-estado", true)
                        .failureUrl("/admin/login?error=true")
                        .permitAll()
                )
                
                // ─── Logout ───────────────────────────────────────────────────────
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                
                // ─── CSRF (Deshabilitado para API, habilitado para form login) ─────
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                
                // ─── JWT Filter (ANTES de UsernamePasswordAuthenticationFilter) ─────
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
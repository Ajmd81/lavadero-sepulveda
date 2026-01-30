package com.lavaderosepulveda.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    /**
     * ✅ Configuración CORS para Railway y desarrollo local
     * IMPORTANTE: Cuando allowCredentials es true, no se puede usar "*" en allowedOriginPatterns
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ CORREGIDO: Especificar orígenes explícitamente en lugar de usar "*"
        // Esto es requerido cuando allowCredentials es true
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:8080",           // Desarrollo local backend
                "http://127.0.0.1:8080",           // Desarrollo local alternativo
                "https://lavadero-sepulveda-production.up.railway.app", // Railway production
                "https://www.lavaderosepulveda.es", // Dominio custom
                "https://lavadero-sepulveda.vercel.app", // Frontend Vercel
                "http://localhost:5173",           // Frontend dev (Vite)
                "http://localhost:3000"            // Frontend dev alternativo
        ));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // ✅ CORREGIDO: Permitir credenciales (ahora compatible con orígenes específicos)
        configuration.setAllowCredentials(true);

        // Cache de preflight requests (1 hora)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        System.out.println("====================================");
        System.out.println("CREANDO USUARIO ADMIN:");
        System.out.println("Username: [" + adminUsername + "]");
        System.out.println("Password: [" + adminPassword + "]");
        System.out.println("====================================");

        UserDetails admin = User.builder()
                .username(adminUsername)
                .password("{noop}" + adminPassword)
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ✅ CRÍTICO: Aplicar configuración CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(authz -> authz
                        // API y chatbot públicos
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/chatbot").permitAll() // ✅ Añadido

                        // Páginas públicas
                        .requestMatchers("/", "/nueva-cita", "/guardar-cita", "/confirmacion",
                                "/horarios-disponibles", "/horario", "/galeria",
                                "/productos", "/tarifas", "/policy").permitAll()

                        // Recursos estáticos
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                        // Admin protegido
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Resto público
                        .anyRequest().permitAll()
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
                .csrf(csrf -> csrf
                        // Deshabilitar CSRF para la API REST
                        .ignoringRequestMatchers("/api/**")
                );

        return http.build();
    }
}
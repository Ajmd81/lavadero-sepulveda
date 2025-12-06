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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    // *** ELIMINADO passwordEncoder() completamente ***

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
                .authorizeHttpRequests(authz -> authz
                        // Permitir acceso sin autenticación a la API y páginas públicas
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/", "/nueva-cita", "/guardar-cita", "/confirmacion",
                                "/horarios-disponibles", "/horario", "/galeria",
                                "/productos", "/tarifas", "/policy").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        // Requerir autenticación para rutas de administración
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .defaultSuccessUrl("/admin/listado-citas", true)
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
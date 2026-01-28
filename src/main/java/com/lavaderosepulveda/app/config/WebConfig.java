package com.lavaderosepulveda.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:5173", // Desarrollo
                                "http://localhost:3000", // Desarrollo alternativo
                                "https://lavadero-sepulveda.vercel.app", // ← Tu dominio Vercel
                                "https://*.vercel.app", // Otros deploys de Vercel
                                "https://lavadero-sepulveda-production.up.railway.app" // Backend Railway
                )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
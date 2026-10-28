package com.lavaderosepulveda.app.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos personales editables. El usuario afectado SIEMPRE es el del token;
 * si el frontend manda un campo "username" se ignora.
 */
public record ActualizarPerfilRequest(
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        @Pattern(regexp = "^$|^[\\p{L}\\s'\\-\\.]+$", message = "El nombre solo puede contener letras, espacios, guiones y puntos")
        String nombreCompleto,

        @Email(message = "Formato de email no válido")
        @Size(max = 150, message = "El email no puede superar 150 caracteres")
        String email,

        @Pattern(regexp = "^$|^[0-9]{8}[A-Z]$", message = "Formato de DNI inválido. Ejemplo: 12345678A")
        String dni
) {}

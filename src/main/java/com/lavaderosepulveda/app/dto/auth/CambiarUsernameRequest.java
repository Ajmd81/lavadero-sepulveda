package com.lavaderosepulveda.app.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CambiarUsernameRequest(
        @NotBlank(message = "El nuevo username no puede estar vacío")
        @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El username solo puede contener letras, números, punto, guion y guion bajo")
        String nuevoUsername
) {}

package com.lavaderosepulveda.app.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarPasswordRequest(
        @NotBlank(message = "La contraseña actual es obligatoria")
        @Size(max = 128)
        String passwordActual,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 10, max = 128, message = "La contraseña debe tener entre 10 y 128 caracteres")
        String passwordNueva
) {}

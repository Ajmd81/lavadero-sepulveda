package com.lavaderosepulveda.app.dto.facturacion;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Body de POST /api/facturas/completa (antes un Map sin validar que daba ClassCastException/NPE). */
public record CrearFacturaCompletaRequest(
        @NotNull(message = "El cliente es obligatorio") @Positive Long clienteId,
        @NotEmpty(message = "Debe incluir al menos una cita") @Size(max = 100)
        List<@NotNull @Positive Long> citaIds,
        @Size(max = 20) @Pattern(regexp = "^$|^[A-Za-z0-9\\-]{5,20}$", message = "NIF/CIF con formato inválido")
        String clienteNif,
        @Size(max = 255) String clienteDireccion
) {}

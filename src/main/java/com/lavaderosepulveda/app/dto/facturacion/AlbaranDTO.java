package com.lavaderosepulveda.app.dto.facturacion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class AlbaranDTO {
    private Long id;
    @Size(max = 30)
    private String numero;
    @Positive
    private Long clienteId;
    @Size(max = 200)
    private String clienteNombre;
    private LocalDate fecha;
    @Valid
    @Size(max = 200)
    private List<LineaAlbaranDTO> lineas = new ArrayList<>();
    @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
    private BigDecimal baseImponible;
    @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
    private BigDecimal iva;
    @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
    private BigDecimal total;
    @Size(max = 30)
    private String estado;
    @Positive
    private Long facturaId;
}
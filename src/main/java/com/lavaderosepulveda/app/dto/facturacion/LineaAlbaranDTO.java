package com.lavaderosepulveda.app.dto.facturacion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LineaAlbaranDTO {
    private Long id;
    @Size(max = 255)
    private String concepto;
    @Min(0)
    @Max(100000)
    private Integer cantidad;
    @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
    private BigDecimal precioUnitario;
    @DecimalMin(value = "0", message = "No puede ser negativo")
    @DecimalMax(value = "100", message = "No puede superar 100")
    @Digits(integer = 3, fraction = 4)
    private BigDecimal tipoIva;
    @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
    private BigDecimal subtotal;
    @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
    private BigDecimal iva;
    @Digits(integer = 10, fraction = 4, message = "Importe con formato inválido")
    private BigDecimal total;
}

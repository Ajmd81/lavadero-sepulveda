package com.lavaderosepulveda.app.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LineaAlbaranDTO {
    private Long id;
    private String concepto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal tipoIva;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;
}

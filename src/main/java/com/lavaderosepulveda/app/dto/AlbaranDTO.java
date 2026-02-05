package com.lavaderosepulveda.app.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class AlbaranDTO {
    private Long id;
    private String numero;
    private Long clienteId;
    private String clienteNombre;
    private LocalDate fecha;
    private List<LineaAlbaranDTO> lineas = new ArrayList<>();
    private BigDecimal baseImponible;
    private BigDecimal iva;
    private BigDecimal total;
    private String estado;
    private Long facturaId;
}
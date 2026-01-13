package com.lavaderosepulveda.crm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacturaRecibidaDTO {
    private Long id;
    private String numeroFactura;
    private Long proveedorId;
    private String proveedorNombre;
    private String proveedorNif;
    private String fechaFactura;
    private String fechaVencimiento;
    private String fechaPago;
    private String categoria;
    private String concepto;
    private BigDecimal baseImponible;
    private BigDecimal tipoIva;
    private BigDecimal cuotaIva;
    private BigDecimal tipoIrpf;
    private BigDecimal cuotaIrpf;
    private BigDecimal total;
    private String estado;
    private String metodoPago;
    private String documentoAdjunto;
    private String notas;
}

package com.lavaderosepulveda.crm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GastoDTO {

    private Long id;
    private String fecha;
    private String categoria;
    private String concepto;
    private BigDecimal baseImponible;
    private BigDecimal tipoIva;
    private BigDecimal cuotaIva;
    private BigDecimal importe;  // Total con IVA
    private Boolean pagado;
    private String metodoPago;
    private String fechaPago;
    private Boolean recurrente;
    private String periodicidad;  // MENSUAL, BIMESTRAL, TRIMESTRAL, SEMESTRAL, ANUAL
    private String notas;

    @Override
    public String toString() {
        return "GastoDTO{" +
                "id=" + id +
                ", fecha='" + fecha + '\'' +
                ", concepto='" + concepto + '\'' +
                ", importe=" + importe +
                ", pagado=" + pagado +
                '}';
    }
}

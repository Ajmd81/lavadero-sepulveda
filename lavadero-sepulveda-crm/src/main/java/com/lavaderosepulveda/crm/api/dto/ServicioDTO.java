package com.lavaderosepulveda.crm.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioDTO {
    
    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer duracionEstimada; // en minutos
    private Boolean activo;
    private String categoria;
    private Double iva;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Double getPrecioConIva() {
        return precio * (1 + (iva != null ? iva : 21.0) / 100);
    }
    
    public Double getImporteIva() {
        return precio * ((iva != null ? iva : 21.0) / 100);
    }
}

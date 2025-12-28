package com.lavaderosepulveda.crm.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    
    private Long id;
    private String nombre;
    private String apellidos;
    private String telefono;
    private String email;
    private String nif;
    private String direccion;
    private String codigoPostal;
    private String ciudad;
    private String provincia;
    
    // Datos del vehículo
    private String matricula;
    private String marca;
    private String modelo;
    private String color;
    
    // Estadísticas (si la API las devuelve)
    private Integer totalCitas;
    private Integer citasCompletadas;
    private Integer citasCanceladas;
    private Integer citasNoPresentadas;
    private Double totalFacturado;
    private LocalDateTime fechaPrimeraCita;
    private LocalDateTime fechaUltimaCita;
    
    // Notas
    private String notas;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Métodos de utilidad
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }
    
    public Double getTicketMedio() {
        if (citasCompletadas == null || citasCompletadas == 0) {
            return 0.0;
        }
        return totalFacturado != null ? totalFacturado / citasCompletadas : 0.0;
    }
    
    public Double getTasaCompletacion() {
        if (totalCitas == null || totalCitas == 0) {
            return 0.0;
        }
        return citasCompletadas != null ? (citasCompletadas.doubleValue() / totalCitas) * 100 : 0.0;
    }
    
    public Double getTasaNoPresentacion() {
        if (totalCitas == null || totalCitas == 0) {
            return 0.0;
        }
        return citasNoPresentadas != null ? (citasNoPresentadas.doubleValue() / totalCitas) * 100 : 0.0;
    }
}

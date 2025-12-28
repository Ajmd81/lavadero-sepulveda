package com.lavaderosepulveda.crm.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    
    private Long id;
    private String nombre;
    private String apellidos;
    private String telefono;
    private String email;
    private Boolean activo;
    
    // Estadísticas
    private Integer totalCitas;
    private Integer citasCompletadas;
    private Integer citasCanceladas;
    private Integer citasNoPresentadas;
    private Double totalFacturado;
    
    // Vehículo habitual (el más usado)
    private String vehiculoHabitual;
    
    /**
     * Obtiene el nombre completo (nombre + apellidos)
     */
    public String getNombreCompleto() {
        StringBuilder nombreCompleto = new StringBuilder(nombre != null ? nombre : "");
        if (apellidos != null && !apellidos.isEmpty()) {
            nombreCompleto.append(" ").append(apellidos);
        }
        return nombreCompleto.toString();
    }
    
    /**
     * Calcula la tasa de no presentación
     */
    public Double getTasaNoPresentacion() {
        if (totalCitas == null || totalCitas == 0) {
            return 0.0;
        }
        return (citasNoPresentadas != null ? citasNoPresentadas : 0) * 100.0 / totalCitas;
    }
}
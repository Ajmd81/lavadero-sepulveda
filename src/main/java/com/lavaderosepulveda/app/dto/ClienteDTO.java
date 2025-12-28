package com.lavaderosepulveda.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    private Long id; // Usaremos el hash del teléfono como ID único
    private String nombre;
    private String apellidos; // Lo extraemos del nombre si tiene espacio
    private String telefono;
    private String email;
    private Boolean activo; // true si tiene citas recientes
    
    // Estadísticas
    private Integer totalCitas;
    private Integer citasCompletadas;
    private Integer citasCanceladas;
    private Integer citasNoPresentadas;
    private Double totalFacturado;
    private String vehiculoHabitual; // Modelo más usado
}
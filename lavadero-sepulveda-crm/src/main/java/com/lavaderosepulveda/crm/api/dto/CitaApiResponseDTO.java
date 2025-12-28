package com.lavaderosepulveda.crm.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que mapea exactamente el JSON que devuelve la API de Spring Boot
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaApiResponseDTO {
    
    private Long id;
    
    // Datos del cliente (vienen separados, no como objeto)
    private String nombre;
    private String email;
    private String telefono;
    
    // Datos del vehículo
    private String modeloVehiculo;
    
    // Tipo de lavado (String del enum)
    private String tipoLavado;
    
    // Fecha y hora separadas (Strings)
    private String fecha;  // Formato: "13/12/2025"
    private String hora;   // Formato: "10:00:00"
    
    // Estado (String del enum)
    private String estado;
    
    // Pago
    private Boolean pagoAdelantado;
    private String referenciaPago;
    private String numeroBizum;
    
    // Observaciones
    private String observaciones;
}
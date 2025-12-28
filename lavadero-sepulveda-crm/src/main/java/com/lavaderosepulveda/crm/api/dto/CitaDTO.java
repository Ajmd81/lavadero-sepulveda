package com.lavaderosepulveda.crm.api.dto;

import com.lavaderosepulveda.crm.model.EstadoCita;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaDTO {
    
    private Long id;
    private Long clienteId;
    private ClienteDTO cliente; // Puede venir expandido de la API
    private LocalDateTime fechaHora;
    private Integer duracionEstimada;
    private EstadoCita estado;
    private List<Long> serviciosIds = new ArrayList<>();
    private List<ServicioDTO> servicios = new ArrayList<>(); // Puede venir expandido
    private String observaciones;
    private Boolean recordatorioEnviado;
    private Boolean confirmacionEnviada;
    private String matricula;
    private String marcaModelo;
    private LocalDateTime horaLlegada;
    private LocalDateTime horaInicio;
    private LocalDateTime horaFin;
    private Boolean facturada;
    private Long facturaId;
    private String motivoCancelacion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Métodos de utilidad
    public Double getImporteTotal() {
        if (servicios == null || servicios.isEmpty()) {
            return 0.0;
        }
        return servicios.stream()
            .mapToDouble(ServicioDTO::getPrecioConIva)
            .sum();
    }
    
    public Double getImporteBase() {
        if (servicios == null || servicios.isEmpty()) {
            return 0.0;
        }
        return servicios.stream()
            .mapToDouble(ServicioDTO::getPrecio)
            .sum();
    }
    
    public Double getImporteIva() {
        return getImporteTotal() - getImporteBase();
    }
    
    public boolean isPendiente() {
        return estado == EstadoCita.PENDIENTE;
    }
    
    public boolean isCompletada() {
        return estado == EstadoCita.COMPLETADA;
    }
    
    public boolean isCancelada() {
        return estado == EstadoCita.CANCELADA;
    }
    
    public boolean isNoPresentado() {
        return estado == EstadoCita.NO_PRESENTADO;
    }
    
    public String getEstadoColor() {
        return estado != null ? estado.getColor() : "#9E9E9E";
    }
}

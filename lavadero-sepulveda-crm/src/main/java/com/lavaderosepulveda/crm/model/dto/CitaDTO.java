package com.lavaderosepulveda.crm.model.dto;

import com.lavaderosepulveda.crm.model.enums.EstadoCita;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // Constructores
    public CitaDTO() {
    }

    public CitaDTO(Long id, Long clienteId, ClienteDTO cliente, LocalDateTime fechaHora, 
                   Integer duracionEstimada, EstadoCita estado, List<Long> serviciosIds, 
                   List<ServicioDTO> servicios, String observaciones, Boolean recordatorioEnviado, 
                   Boolean confirmacionEnviada, String matricula, String marcaModelo, 
                   LocalDateTime horaLlegada, LocalDateTime horaInicio, LocalDateTime horaFin, 
                   Boolean facturada, Long facturaId, String motivoCancelacion, 
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.clienteId = clienteId;
        this.cliente = cliente;
        this.fechaHora = fechaHora;
        this.duracionEstimada = duracionEstimada;
        this.estado = estado;
        this.serviciosIds = serviciosIds;
        this.servicios = servicios;
        this.observaciones = observaciones;
        this.recordatorioEnviado = recordatorioEnviado;
        this.confirmacionEnviada = confirmacionEnviada;
        this.matricula = matricula;
        this.marcaModelo = marcaModelo;
        this.horaLlegada = horaLlegada;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.facturada = facturada;
        this.facturaId = facturaId;
        this.motivoCancelacion = motivoCancelacion;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

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

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public ClienteDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClienteDTO cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Integer getDuracionEstimada() {
        return duracionEstimada;
    }

    public void setDuracionEstimada(Integer duracionEstimada) {
        this.duracionEstimada = duracionEstimada;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public List<Long> getServiciosIds() {
        return serviciosIds;
    }

    public void setServiciosIds(List<Long> serviciosIds) {
        this.serviciosIds = serviciosIds;
    }

    public List<ServicioDTO> getServicios() {
        return servicios;
    }

    public void setServicios(List<ServicioDTO> servicios) {
        this.servicios = servicios;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Boolean getRecordatorioEnviado() {
        return recordatorioEnviado;
    }

    public void setRecordatorioEnviado(Boolean recordatorioEnviado) {
        this.recordatorioEnviado = recordatorioEnviado;
    }

    public Boolean getConfirmacionEnviada() {
        return confirmacionEnviada;
    }

    public void setConfirmacionEnviada(Boolean confirmacionEnviada) {
        this.confirmacionEnviada = confirmacionEnviada;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getMarcaModelo() {
        return marcaModelo;
    }

    public void setMarcaModelo(String marcaModelo) {
        this.marcaModelo = marcaModelo;
    }

    public LocalDateTime getHoraLlegada() {
        return horaLlegada;
    }

    public void setHoraLlegada(LocalDateTime horaLlegada) {
        this.horaLlegada = horaLlegada;
    }

    public LocalDateTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalDateTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalDateTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalDateTime horaFin) {
        this.horaFin = horaFin;
    }

    public Boolean getFacturada() {
        return facturada;
    }

    public void setFacturada(Boolean facturada) {
        this.facturada = facturada;
    }

    public Long getFacturaId() {
        return facturaId;
    }

    public void setFacturaId(Long facturaId) {
        this.facturaId = facturaId;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "CitaDTO{" +
                "id=" + id +
                ", clienteId=" + clienteId +
                ", cliente=" + cliente +
                ", fechaHora=" + fechaHora +
                ", duracionEstimada=" + duracionEstimada +
                ", estado=" + estado +
                ", serviciosIds=" + serviciosIds +
                ", servicios=" + servicios +
                ", observaciones='" + observaciones + '\'' +
                ", recordatorioEnviado=" + recordatorioEnviado +
                ", confirmacionEnviada=" + confirmacionEnviada +
                ", matricula='" + matricula + '\'' +
                ", marcaModelo='" + marcaModelo + '\'' +
                ", horaLlegada=" + horaLlegada +
                ", horaInicio=" + horaInicio +
                ", horaFin=" + horaFin +
                ", facturada=" + facturada +
                ", facturaId=" + facturaId +
                ", motivoCancelacion='" + motivoCancelacion + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
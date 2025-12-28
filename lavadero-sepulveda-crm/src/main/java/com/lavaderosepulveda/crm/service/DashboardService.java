package com.lavaderosepulveda.crm.service;

import com.lavaderosepulveda.crm.api.dto.CitaDTO;
import com.lavaderosepulveda.crm.api.dto.ClienteDTO;
import com.lavaderosepulveda.crm.api.service.CitaApiService;
import com.lavaderosepulveda.crm.api.service.ClienteApiService;
import com.lavaderosepulveda.crm.model.EstadoCita;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class DashboardService {
    
    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
    
    private static DashboardService instance;
    private final CitaApiService citaApiService;
    private final ClienteApiService clienteApiService;
    
    private DashboardService() {
        this.citaApiService = CitaApiService.getInstance();
        this.clienteApiService = ClienteApiService.getInstance();
    }
    
    public static synchronized DashboardService getInstance() {
        if (instance == null) {
            instance = new DashboardService();
        }
        return instance;
    }
    
    /**
     * Obtener métricas del dashboard para hoy
     */
    public DashboardMetrics obtenerMetricsHoy() {
        log.info("Obteniendo métricas del dashboard...");
        
        DashboardMetrics metrics = new DashboardMetrics();
        
        try {
            // Obtener todas las citas
            List<CitaDTO> todasLasCitas = citaApiService.findAll();
            LocalDate hoy = LocalDate.now();
            YearMonth mesActual = YearMonth.now();
            
            // Citas de hoy
            long citasHoy = todasLasCitas.stream()
                .filter(cita -> cita.getFechaHora() != null && 
                               cita.getFechaHora().toLocalDate().equals(hoy))
                .count();
            metrics.setCitasHoy((int) citasHoy);
            
            // Citas pendientes (futuras y con estado PENDIENTE o CONFIRMADA)
            long citasPendientes = todasLasCitas.stream()
                .filter(cita -> cita.getFechaHora() != null && 
                               !cita.getFechaHora().toLocalDate().isBefore(hoy))
                .filter(cita -> cita.getEstado() == EstadoCita.PENDIENTE || 
                               cita.getEstado() == EstadoCita.CONFIRMADA)
                .count();
            metrics.setCitasPendientes((int) citasPendientes);
            
            // Facturado hoy
            double facturadoHoy = todasLasCitas.stream()
                .filter(cita -> cita.getFechaHora() != null && 
                               cita.getFechaHora().toLocalDate().equals(hoy))
                .filter(cita -> cita.getEstado() == EstadoCita.COMPLETADA)
                .mapToDouble(CitaDTO::getImporteTotal)
                .sum();
            metrics.setFacturadoHoy(facturadoHoy);
            
            // Facturado este mes
            double facturadoMes = todasLasCitas.stream()
                .filter(cita -> {
                    if (cita.getFechaHora() == null) return false;
                    YearMonth citaMes = YearMonth.from(cita.getFechaHora());
                    return citaMes.equals(mesActual);
                })
                .filter(cita -> cita.getEstado() == EstadoCita.COMPLETADA)
                .mapToDouble(CitaDTO::getImporteTotal)
                .sum();
            metrics.setFacturadoMes(facturadoMes);
            
            // Pendiente de cobro (citas completadas pero no facturadas)
            double pendienteCobro = todasLasCitas.stream()
                .filter(cita -> cita.getEstado() == EstadoCita.COMPLETADA)
                .filter(cita -> cita.getFacturada() == null || !cita.getFacturada())
                .mapToDouble(CitaDTO::getImporteTotal)
                .sum();
            metrics.setPendienteCobro(pendienteCobro);
            
            // Clientes activos
            long clientesActivos = clienteApiService.contarClientesActivos();
            metrics.setClientesActivos((int) clientesActivos);
            
            log.info("Métricas obtenidas: {} citas hoy, {} pendientes", citasHoy, citasPendientes);
            
        } catch (Exception e) {
            log.error("Error al obtener métricas", e);
        }
        
        return metrics;
    }
    
    /**
     * Obtener top clientes por facturación
     */
    public List<ClienteDTO> obtenerTopClientesPorFacturacion(int limite) {
        log.info("Obteniendo top {} clientes por facturación", limite);
        return clienteApiService.obtenerTopClientesPorFacturacion(limite);
    }
    
    /**
     * Obtener clientes con más no presentaciones
     */
    public List<ClienteDTO> obtenerClientesConMasNoPresentaciones(int limite) {
        log.info("Obteniendo top {} clientes con más no presentaciones", limite);
        return clienteApiService.obtenerClientesConMasNoPresentaciones(limite);
    }
    
    /**
     * Clase interna para métricas del dashboard
     */
    public static class DashboardMetrics {
        private Integer citasHoy = 0;
        private Integer citasPendientes = 0;
        private Double facturadoHoy = 0.0;
        private Double facturadoMes = 0.0;
        private Double pendienteCobro = 0.0;
        private Integer clientesActivos = 0;

        public DashboardMetrics() {
        }

        public Integer getCitasHoy() {
            return citasHoy;
        }

        public void setCitasHoy(Integer citasHoy) {
            this.citasHoy = citasHoy;
        }

        public Integer getCitasPendientes() {
            return citasPendientes;
        }

        public void setCitasPendientes(Integer citasPendientes) {
            this.citasPendientes = citasPendientes;
        }

        public Double getFacturadoHoy() {
            return facturadoHoy;
        }

        public void setFacturadoHoy(Double facturadoHoy) {
            this.facturadoHoy = facturadoHoy;
        }

        public Double getFacturadoMes() {
            return facturadoMes;
        }

        public void setFacturadoMes(Double facturadoMes) {
            this.facturadoMes = facturadoMes;
        }

        public Double getPendienteCobro() {
            return pendienteCobro;
        }

        public void setPendienteCobro(Double pendienteCobro) {
            this.pendienteCobro = pendienteCobro;
        }

        public Integer getClientesActivos() {
            return clientesActivos;
        }

        public void setClientesActivos(Integer clientesActivos) {
            this.clientesActivos = clientesActivos;
        }
    }
}
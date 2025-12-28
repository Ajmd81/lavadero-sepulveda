package com.lavaderosepulveda.crm.service;

import com.lavaderosepulveda.crm.api.dto.ClienteDTO;
import com.lavaderosepulveda.crm.api.service.CitaApiService;
import com.lavaderosepulveda.crm.api.service.ClienteApiService;
import com.lavaderosepulveda.crm.model.EstadoCita;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DashboardService {
    
    private static DashboardService instance;
    
    private final ClienteApiService clienteApiService;
    private final CitaApiService citaApiService;
    
    private DashboardService() {
        this.clienteApiService = ClienteApiService.getInstance();
        this.citaApiService = CitaApiService.getInstance();
    }
    
    public static synchronized DashboardService getInstance() {
        if (instance == null) {
            instance = new DashboardService();
        }
        return instance;
    }
    
    // Métricas generales
    public DashboardMetrics obtenerMetricsHoy() {
        DashboardMetrics metrics = new DashboardMetrics();
        
        metrics.setCitasHoy(citaApiService.countCitasHoy());
        metrics.setCitasPendientes(citaApiService.countByEstado(EstadoCita.PENDIENTE));
        metrics.setCitasCompletadas(citaApiService.countByEstado(EstadoCita.COMPLETADA));
        metrics.setClientesActivos(clienteApiService.findActivos().size());
        
        // Las métricas de facturación se pueden obtener de la API si tiene esos endpoints
        // Por ahora dejamos valores en 0 o implementar endpoints adicionales
        metrics.setFacturadoHoy(0.0);
        metrics.setFacturadoMes(0.0);
        metrics.setPendienteCobro(0.0);
        metrics.setFacturasVencidas(0);
        
        return metrics;
    }
    
    public DashboardMetrics obtenerMetricsPeriodo(LocalDate inicio, LocalDate fin) {
        DashboardMetrics metrics = new DashboardMetrics();
        
        metrics.setCitasPeriodo(citaApiService.findByFechaRange(inicio, fin).size());
        metrics.setFacturadoPeriodo(0.0); // Implementar endpoint de facturación si existe
        
        return metrics;
    }
    
    // Top clientes por facturación
    public List<ClienteDTO> obtenerTopClientesPorFacturacion(int limit) {
        return clienteApiService.findTopClientesPorFacturacion(limit);
    }
    
    // Clientes con más no presentaciones
    public List<ClienteDTO> obtenerClientesConMasNoPresentaciones(int limit) {
        return clienteApiService.findClientesConMasNoPresentaciones(limit);
    }
    
    // Clientes con baja tasa de completación
    public List<ClienteDTO> obtenerClientesBajaTasaCompletacion(int limit) {
        // Implementar si la API tiene este endpoint
        return clienteApiService.findActivos();
    }
    
    // Estadísticas por estado de citas
    public Map<EstadoCita, Long> obtenerEstadisticasPorEstado() {
        Map<EstadoCita, Long> stats = new HashMap<>();
        for (EstadoCita estado : EstadoCita.values()) {
            stats.put(estado, citaApiService.countByEstado(estado));
        }
        return stats;
    }
    
    // Facturación mensual del año actual
    public Map<Integer, Double> obtenerFacturacionMensualAnioActual() {
        Map<Integer, Double> facturacionMensual = new HashMap<>();
        // Implementar si la API tiene endpoints de facturación
        for (int mes = 1; mes <= 12; mes++) {
            facturacionMensual.put(mes, 0.0);
        }
        return facturacionMensual;
    }
    
    // Citas próximas (próximos 7 días)
    public int obtenerCitasProximaSemana() {
        return citaApiService.findCitasProximaSemana().size();
    }
    
    // Clase interna para métricas del dashboard
    @Data
    public static class DashboardMetrics {
        private Long citasHoy;
        private Long citasPendientes;
        private Long citasCompletadas;
        private Integer clientesActivos;
        private Double facturadoHoy;
        private Double facturadoMes;
        private Double pendienteCobro;
        private Integer facturasVencidas;
        private Integer citasPeriodo;
        private Double facturadoPeriodo;
    }
}

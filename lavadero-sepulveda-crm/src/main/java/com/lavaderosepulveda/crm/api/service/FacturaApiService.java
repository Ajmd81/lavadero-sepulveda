package com.lavaderosepulveda.crm.api.service;

import com.google.gson.reflect.TypeToken;
import com.lavaderosepulveda.crm.api.ApiClient;
import com.lavaderosepulveda.crm.api.dto.FacturaDTO;
import com.lavaderosepulveda.crm.config.ConfigManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class FacturaApiService {

    private static FacturaApiService instance;
    private final ApiClient apiClient;
    private final String baseUrl;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private FacturaApiService() {
        this.apiClient = ApiClient.getInstance();
        ConfigManager config = ConfigManager.getInstance();
        this.baseUrl = config.getApiBaseUrl() + "/api/facturas";
    }

    public static synchronized FacturaApiService getInstance() {
        if (instance == null) {
            instance = new FacturaApiService();
        }
        return instance;
    }

    // ========================================
    // CRUD BÁSICO
    // ========================================

    /**
     * Obtener todas las facturas
     */
    public List<FacturaDTO> findAll() {
        try {
            String response = apiClient.getRaw(baseUrl);
            Type listType = new TypeToken<ArrayList<FacturaDTO>>() {
            }.getType();
            List<FacturaDTO> facturas = apiClient.getGson().fromJson(response, listType);
            log.info("Obtenidas {} facturas", facturas.size());
            return facturas;
        } catch (IOException e) {
            log.error("Error al obtener facturas", e);
            return new ArrayList<>();
        }
    }

    /**
     * Obtener factura por ID
     */
    public FacturaDTO findById(Long id) throws IOException {
        String url = baseUrl + "/" + id;
        String response = apiClient.getRaw(url);
        return apiClient.getGson().fromJson(response, FacturaDTO.class);
    }

    /**
     * Eliminar factura
     */
    public void delete(Long id) throws IOException {
        String url = baseUrl + "/" + id;
        apiClient.delete(url);
        log.info("Factura {} eliminada", id);
    }

    // ========================================
    // CREACIÓN DE FACTURAS
    // ========================================

    /**
     * Crear factura simplificada desde una cita
     */
    public FacturaDTO crearSimplificadaDesdeCita(Long citaId) throws IOException {
        String url = baseUrl + "/simplificada/cita/" + citaId;
        String response = apiClient.postRaw(url, new HashMap<>());
        FacturaDTO factura = apiClient.getGson().fromJson(response, FacturaDTO.class);
        log.info("Factura simplificada {} creada desde cita {}", factura.getNumero(), citaId);
        return factura;
    }

    /**
     * Crear factura completa
     */
    public FacturaDTO crearCompleta(Long clienteId, List<Long> citaIds, String clienteNif, String clienteDireccion)
            throws IOException {
        String url = baseUrl + "/completa";

        Map<String, Object> request = new HashMap<>();
        request.put("clienteId", clienteId);
        request.put("citaIds", citaIds);
        request.put("clienteNif", clienteNif);
        request.put("clienteDireccion", clienteDireccion);

        String response = apiClient.postRaw(url, request);
        FacturaDTO factura = apiClient.getGson().fromJson(response, FacturaDTO.class);
        log.info("Factura completa {} creada", factura.getNumero());
        return factura;
    }

    /**
     * Crear factura manual
     */
    public FacturaDTO crearManual(FacturaDTO facturaDTO) throws IOException {
        String url = baseUrl + "/manual";
        String response = apiClient.postRaw(url, facturaDTO);
        FacturaDTO factura = apiClient.getGson().fromJson(response, FacturaDTO.class);
        log.info("Factura manual {} creada", factura.getNumero());
        return factura;
    }

    // ========================================
    // GESTIÓN DE PAGOS
    // ========================================

    /**
     * Marcar factura como pagada
     */
    public FacturaDTO marcarComoPagada(Long id, String metodoPago) throws IOException {
        String url = baseUrl + "/" + id + "/pagar";

        Map<String, String> request = new HashMap<>();
        request.put("metodoPago", metodoPago);

        String response = apiClient.putRaw(url, request);
        FacturaDTO factura = apiClient.getGson().fromJson(response, FacturaDTO.class);
        log.info("Factura {} marcada como pagada con {}", factura.getNumero(), metodoPago);
        return factura;
    }

    // ========================================
    // CONSULTAS
    // ========================================

    /**
     * Obtener facturas por estado
     */
    public List<FacturaDTO> findByEstado(String estado) {
        try {
            String url = baseUrl + "/estado/" + estado;
            String response = apiClient.getRaw(url);
            Type listType = new TypeToken<ArrayList<FacturaDTO>>() {
            }.getType();
            return apiClient.getGson().fromJson(response, listType);
        } catch (IOException e) {
            log.error("Error al obtener facturas por estado", e);
            return new ArrayList<>();
        }
    }

    /**
     * Obtener facturas pendientes
     */
    public List<FacturaDTO> findPendientes() {
        try {
            String url = baseUrl + "/pendientes";
            String response = apiClient.getRaw(url);
            Type listType = new TypeToken<ArrayList<FacturaDTO>>() {
            }.getType();
            return apiClient.getGson().fromJson(response, listType);
        } catch (IOException e) {
            log.error("Error al obtener facturas pendientes", e);
            return new ArrayList<>();
        }
    }

    /**
     * Obtener facturas de un cliente
     */
    public List<FacturaDTO> findByCliente(Long clienteId) {
        try {
            String url = baseUrl + "/cliente/" + clienteId;
            String response = apiClient.getRaw(url);
            Type listType = new TypeToken<ArrayList<FacturaDTO>>() {
            }.getType();
            return apiClient.getGson().fromJson(response, listType);
        } catch (IOException e) {
            log.error("Error al obtener facturas del cliente", e);
            return new ArrayList<>();
        }
    }

    /**
     * Obtener facturas por rango de fechas
     */
    public List<FacturaDTO> findByFechas(LocalDate desde, LocalDate hasta) {
        try {
            String desdeStr = desde.format(DATE_FORMATTER);
            String hastaStr = hasta.format(DATE_FORMATTER);
            String url = baseUrl + "/fecha?desde=" + desdeStr + "&hasta=" + hastaStr;
            String response = apiClient.getRaw(url);
            Type listType = new TypeToken<ArrayList<FacturaDTO>>() {
            }.getType();
            return apiClient.getGson().fromJson(response, listType);
        } catch (IOException e) {
            log.error("Error al obtener facturas por fecha", e);
            return new ArrayList<>();
        }
    }

    /**
     * Buscar facturas
     */
    public List<FacturaDTO> buscar(String texto) {
        try {
            String url = baseUrl + "/buscar?q=" + texto;
            String response = apiClient.getRaw(url);
            Type listType = new TypeToken<ArrayList<FacturaDTO>>() {
            }.getType();
            return apiClient.getGson().fromJson(response, listType);
        } catch (IOException e) {
            log.error("Error al buscar facturas", e);
            return new ArrayList<>();
        }
    }

    /**
     * Obtener facturas de hoy
     */
    public List<FacturaDTO> findDeHoy() {
        try {
            String url = baseUrl + "/hoy";
            String response = apiClient.getRaw(url);
            Type listType = new TypeToken<ArrayList<FacturaDTO>>() {
            }.getType();
            return apiClient.getGson().fromJson(response, listType);
        } catch (IOException e) {
            log.error("Error al obtener facturas de hoy", e);
            return new ArrayList<>();
        }
    }

    // ========================================
    // ESTADÍSTICAS
    // ========================================

    /**
     * Obtener resumen de facturación
     */
    public Map<String, Object> getResumen() {
        try {
            String url = baseUrl + "/resumen";
            String response = apiClient.getRaw(url);
            Type mapType = new TypeToken<HashMap<String, Object>>() {
            }.getType();
            return apiClient.getGson().fromJson(response, mapType);
        } catch (IOException e) {
            log.error("Error al obtener resumen de facturación", e);
            return new HashMap<>();
        }
    }

    /**
     * Obtener datos del emisor
     */
    public Map<String, String> getDatosEmisor() {
        try {
            String url = baseUrl + "/emisor";
            String response = apiClient.getRaw(url);
            Type mapType = new TypeToken<HashMap<String, String>>() {
            }.getType();
            return apiClient.getGson().fromJson(response, mapType);
        } catch (IOException e) {
            log.error("Error al obtener datos del emisor", e);
            return new HashMap<>();
        }
    }

    // ========================================
    // PDF
    // ========================================

    /**
     * Obtener URL para descargar PDF
     */
    public String getUrlPdf(Long facturaId) {
        return baseUrl + "/" + facturaId + "/pdf";
    }

    /**
     * Obtener URL para previsualizar PDF
     */
    public String getUrlPdfPreview(Long facturaId) {
        return baseUrl + "/" + facturaId + "/pdf/preview";
    }

    /**
     * Descargar PDF como bytes
     */
    public byte[] descargarPdf(Long facturaId) throws IOException {
        String url = baseUrl + "/" + facturaId + "/pdf";
        return apiClient.getBytes(url);
    }
}

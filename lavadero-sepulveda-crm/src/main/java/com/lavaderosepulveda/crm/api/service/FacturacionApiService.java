package com.lavaderosepulveda.crm.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lavaderosepulveda.crm.model.dto.*;
import com.lavaderosepulveda.crm.model.entity.*;
import com.lavaderosepulveda.crm.model.enums.*;
import com.lavaderosepulveda.crm.model.dto.ClienteDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class FacturacionApiService {

        private static FacturacionApiService instance;
        private final ObjectMapper objectMapper;
        private String baseUrl;

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        private static final int TIMEOUT = 15000; // 15 segundos

        private FacturacionApiService() {
                this.objectMapper = new ObjectMapper();
                this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                this.baseUrl = "https://lavadero-sepulveda-production.up.railway.app";
        }

        public static synchronized FacturacionApiService getInstance() {
                if (instance == null) {
                        instance = new FacturacionApiService();
                }
                return instance;
        }

        public void setBaseUrl(String baseUrl) {
                this.baseUrl = baseUrl;
        }

        // ==================== MÉTODOS HTTP BASE ====================

        private String doGet(String endpoint) throws IOException {
                URL url = new URL(baseUrl + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(TIMEOUT);
                conn.setReadTimeout(TIMEOUT);
                conn.setRequestProperty("Accept", "application/json");

                return readResponse(conn);
        }

        private String doPost(String endpoint, String jsonBody) throws IOException {
                URL url = new URL(baseUrl + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(TIMEOUT);
                conn.setReadTimeout(TIMEOUT);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                if (jsonBody != null && !jsonBody.isEmpty()) {
                        try (OutputStream os = conn.getOutputStream()) {
                                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                        }
                }

                return readResponse(conn);
        }

        private String doPut(String endpoint, String jsonBody) throws IOException {
                URL url = new URL(baseUrl + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setConnectTimeout(TIMEOUT);
                conn.setReadTimeout(TIMEOUT);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                if (jsonBody != null && !jsonBody.isEmpty()) {
                        try (OutputStream os = conn.getOutputStream()) {
                                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                        }
                }

                return readResponse(conn);
        }

        private void doDelete(String endpoint) throws IOException {
                URL url = new URL(baseUrl + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setConnectTimeout(TIMEOUT);
                conn.setReadTimeout(TIMEOUT);

                int responseCode = conn.getResponseCode();
                if (responseCode >= 400) {
                        throw new IOException("Error en la API: " + responseCode);
                }
        }

        private byte[] doGetBytes(String endpoint) throws IOException {
                URL url = new URL(baseUrl + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(TIMEOUT);
                conn.setReadTimeout(TIMEOUT);

                int responseCode = conn.getResponseCode();
                if (responseCode >= 400) {
                        throw new IOException("Error al descargar: " + responseCode);
                }

                return conn.getInputStream().readAllBytes();
        }

        private String readResponse(HttpURLConnection conn) throws IOException {
                int responseCode = conn.getResponseCode();

                BufferedReader reader;
                if (responseCode >= 400) {
                        reader = new BufferedReader(
                                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                } else {
                        reader = new BufferedReader(
                                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                }

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                        response.append(line);
                }
                reader.close();

                if (responseCode >= 400) {
                        throw new IOException("Error en la API (" + responseCode + "): " + response);
                }

                return response.toString();
        }

        private String encode(String value) {
                return URLEncoder.encode(value, StandardCharsets.UTF_8);
        }
        
        // ==================== CLIENTES ====================
        
        public List<ClienteDTO> obtenerClientes() throws IOException {
        String response = doGet("/api/clientes");
        return objectMapper.readValue(response, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, ClienteDTO.class));
        }
        
        public List<ClienteDTO> buscarClientes(String termino) throws IOException {
        String response = doGet("/api/clientes/buscar?termino=" + 
                java.net.URLEncoder.encode(termino, java.nio.charset.StandardCharsets.UTF_8));
        return objectMapper.readValue(response, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, ClienteDTO.class));
        }
        
        public ClienteDTO obtenerClientePorId(Long id) throws IOException {
        String response = doGet("/api/clientes/" + id);
        return objectMapper.readValue(response, ClienteDTO.class);
        }
        // ==================== PROVEEDORES ====================

        public List<ProveedorDTO> obtenerProveedores() throws IOException {
                String response = doGet("/api/proveedores/todos");
                return objectMapper.readValue(response, new TypeReference<List<ProveedorDTO>>() {
                });
        }

        public List<ProveedorDTO> obtenerProveedoresActivos() throws IOException {
                String response = doGet("/api/proveedores");
                return objectMapper.readValue(response, new TypeReference<List<ProveedorDTO>>() {
                });
        }

        public ProveedorDTO obtenerProveedor(Long id) throws IOException {
                String response = doGet("/api/proveedores/" + id);
                return objectMapper.readValue(response, ProveedorDTO.class);
        }

        public List<ProveedorDTO> buscarProveedores(String termino) throws IOException {
                String response = doGet("/api/proveedores/buscar?termino=" + encode(termino));
                return objectMapper.readValue(response, new TypeReference<List<ProveedorDTO>>() {
                });
        }

        public ProveedorDTO crearProveedor(ProveedorDTO proveedor) throws IOException {
                String json = objectMapper.writeValueAsString(proveedor);
                String response = doPost("/api/proveedores", json);
                return objectMapper.readValue(response, ProveedorDTO.class);
        }

        public ProveedorDTO actualizarProveedor(Long id, ProveedorDTO proveedor) throws IOException {
                String json = objectMapper.writeValueAsString(proveedor);
                String response = doPut("/api/proveedores/" + id, json);
                return objectMapper.readValue(response, ProveedorDTO.class);
        }

        public void desactivarProveedor(Long id) throws IOException {
                doPut("/api/proveedores/" + id + "/desactivar", null);
        }

        public void activarProveedor(Long id) throws IOException {
                doPut("/api/proveedores/" + id + "/activar", null);
        }

        public void eliminarProveedor(Long id) throws IOException {
                doDelete("/api/proveedores/" + id);
        }

        // ==================== FACTURAS EMITIDAS ====================

        public List<FacturaEmitidaDTO> obtenerFacturasEmitidas() throws IOException {
                String response = doGet("/api/facturas");
                return objectMapper.readValue(response, new TypeReference<List<FacturaEmitidaDTO>>() {
                });
        }

        public List<FacturaEmitidaDTO> obtenerFacturasEmitidasPorEstado(String estado) throws IOException {
                String response = doGet("/api/facturas/estado/" + estado);
                return objectMapper.readValue(response, new TypeReference<List<FacturaEmitidaDTO>>() {
                });
        }

        public List<FacturaEmitidaDTO> obtenerFacturasEmitidasPorPeriodo(LocalDate desde, LocalDate hasta)
                        throws IOException {
                String desdeStr = desde.format(DATE_FORMATTER);
                String hastaStr = hasta.format(DATE_FORMATTER);
                String response = doGet(
                                "/api/facturas?desde=" + encode(desdeStr) + "&hasta=" + encode(hastaStr));
                return objectMapper.readValue(response, new TypeReference<List<FacturaEmitidaDTO>>() {
                });
        }

        public FacturaEmitidaDTO obtenerFacturaEmitida(Long id) throws IOException {
                String response = doGet("/api/facturas/" + id);
                return objectMapper.readValue(response, FacturaEmitidaDTO.class);
        }

        public byte[] descargarPdfFacturaEmitida(Long id) throws IOException {
                return doGetBytes("/api/facturas/" + id + "/pdf");
        }

        public FacturaEmitidaDTO marcarFacturaEmitidaPagada(Long id, String metodoPago) throws IOException {
                String json = "{\"metodoPago\":\"" + metodoPago + "\"}";
                String response = doPut("/api/facturas/" + id + "/pagar", json);  
                return objectMapper.readValue(response, FacturaEmitidaDTO.class);
        }

        public List<FacturaEmitidaDTO> buscarFacturasEmitidas(String termino) throws IOException {
                String response = doGet("/api/facturas/buscar?termino=" + encode(termino));
                return objectMapper.readValue(response, new TypeReference<List<FacturaEmitidaDTO>>() {
                });
        }

        // ==================== FACTURAS RECIBIDAS ====================

        public List<FacturaRecibidaDTO> obtenerFacturasRecibidas() throws IOException {
                String response = doGet("/api/facturas-recibidas");
                return objectMapper.readValue(response, new TypeReference<List<FacturaRecibidaDTO>>() {
                });
        }

        public List<FacturaRecibidaDTO> obtenerFacturasRecibidasPorEstado(String estado) throws IOException {
                String response = doGet("/api/facturas-recibidas/estado/" + estado);
                return objectMapper.readValue(response, new TypeReference<List<FacturaRecibidaDTO>>() {
                });
        }

        public List<FacturaRecibidaDTO> obtenerFacturasRecibidasPendientes() throws IOException {
                String response = doGet("/api/facturas-recibidas/pendientes");
                return objectMapper.readValue(response, new TypeReference<List<FacturaRecibidaDTO>>() {
                });
        }

        public List<FacturaRecibidaDTO> obtenerFacturasRecibidasVencidas() throws IOException {
                String response = doGet("/api/facturas-recibidas/vencidas");
                return objectMapper.readValue(response, new TypeReference<List<FacturaRecibidaDTO>>() {
                });
        }

        public List<FacturaRecibidaDTO> obtenerFacturasRecibidasPorProveedor(Long proveedorId) throws IOException {
                String response = doGet("/api/facturas-recibidas/proveedor/" + proveedorId);
                return objectMapper.readValue(response, new TypeReference<List<FacturaRecibidaDTO>>() {
                });
        }

        public List<FacturaRecibidaDTO> obtenerFacturasRecibidasPorCategoria(String categoria) throws IOException {
                String response = doGet("/api/facturas-recibidas/categoria/" + categoria);
                return objectMapper.readValue(response, new TypeReference<List<FacturaRecibidaDTO>>() {
                });
        }

        public List<FacturaRecibidaDTO> obtenerFacturasRecibidasPorPeriodo(LocalDate desde, LocalDate hasta)
                        throws IOException {
                String desdeStr = desde.format(DATE_FORMATTER);
                String hastaStr = hasta.format(DATE_FORMATTER);
                String response = doGet("/api/facturas-recibidas?desde=" + encode(desdeStr) + "&hasta="
                                + encode(hastaStr));
                return objectMapper.readValue(response, new TypeReference<List<FacturaRecibidaDTO>>() {
                });
        }

        public List<FacturaRecibidaDTO> obtenerFacturasRecibidasPorMes(int year, int month) throws IOException {
                String response = doGet("/api/facturas-recibidas/mes/" + year + "/" + month);
                return objectMapper.readValue(response, new TypeReference<List<FacturaRecibidaDTO>>() {
                });
        }

        public FacturaRecibidaDTO obtenerFacturaRecibida(Long id) throws IOException {
                String response = doGet("/api/facturas-recibidas/" + id);
                return objectMapper.readValue(response, FacturaRecibidaDTO.class);
        }

        public List<FacturaRecibidaDTO> buscarFacturasRecibidas(String termino) throws IOException {
                String response = doGet("/api/facturas-recibidas/buscar?termino=" + encode(termino));
                return objectMapper.readValue(response, new TypeReference<List<FacturaRecibidaDTO>>() {
                });
        }

        public FacturaRecibidaDTO crearFacturaRecibida(FacturaRecibidaDTO factura) throws IOException {
                String json = objectMapper.writeValueAsString(factura);
                String response = doPost("/api/facturas-recibidas", json);
                return objectMapper.readValue(response, FacturaRecibidaDTO.class);
        }

        public FacturaRecibidaDTO actualizarFacturaRecibida(Long id, FacturaRecibidaDTO factura) throws IOException {
                String json = objectMapper.writeValueAsString(factura);
                String response = doPut("/api/facturas-recibidas/" + id, json);
                return objectMapper.readValue(response, FacturaRecibidaDTO.class);
        }

        public FacturaRecibidaDTO marcarFacturaRecibidaPagada(Long id, String metodoPago) throws IOException {
                String response = doPut("/api/facturas-recibidas/" + id + "/pagar?metodoPago=" + metodoPago, null);
                return objectMapper.readValue(response, FacturaRecibidaDTO.class);
        }

        public void eliminarFacturaRecibida(Long id) throws IOException {
                doDelete("/api/facturas-recibidas/" + id);
        }

        // Resúmenes de facturas recibidas
        public Map<String, Object> obtenerResumenFacturasRecibidas(LocalDate desde, LocalDate hasta)
                        throws IOException {
                String desdeStr = desde.format(DATE_FORMATTER);
                String hastaStr = hasta.format(DATE_FORMATTER);
                String response = doGet("/api/facturas-recibidas/resumen/total?desde=" + encode(desdeStr) + "&hasta="
                                + encode(hastaStr));
                return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {
                });
        }

        public List<Map<String, Object>> obtenerResumenPorCategoriaFacturasRecibidas(LocalDate desde, LocalDate hasta)
                        throws IOException {
                String desdeStr = desde.format(DATE_FORMATTER);
                String hastaStr = hasta.format(DATE_FORMATTER);
                String response = doGet("/api/facturas-recibidas/resumen/categorias?desde=" + encode(desdeStr)
                                + "&hasta=" + encode(hastaStr));
                return objectMapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {
                });
        }

        // ==================== GASTOS ====================

        public List<GastoDTO> obtenerGastos() throws IOException {
                String response = doGet("/api/gastos");
                return objectMapper.readValue(response, new TypeReference<List<GastoDTO>>() {
                });
        }

        public List<GastoDTO> obtenerGastosPorCategoria(String categoria) throws IOException {
                String response = doGet("/api/gastos/categoria/" + categoria);
                return objectMapper.readValue(response, new TypeReference<List<GastoDTO>>() {
                });
        }

        public List<GastoDTO> obtenerGastosRecurrentes() throws IOException {
                String response = doGet("/api/gastos/recurrentes");
                return objectMapper.readValue(response, new TypeReference<List<GastoDTO>>() {
                });
        }

        public List<GastoDTO> obtenerGastosPendientes() throws IOException {
                String response = doGet("/api/gastos/pendientes");
                return objectMapper.readValue(response, new TypeReference<List<GastoDTO>>() {
                });
        }

        public List<GastoDTO> obtenerGastosPorPeriodo(LocalDate desde, LocalDate hasta) throws IOException {
                String desdeStr = desde.format(DATE_FORMATTER);
                String hastaStr = hasta.format(DATE_FORMATTER);
                String response = doGet("/api/gastos?desde=" + encode(desdeStr) + "&hasta=" + encode(hastaStr));
                return objectMapper.readValue(response, new TypeReference<List<GastoDTO>>() {
                });
        }

        public List<GastoDTO> obtenerGastosPorMes(int year, int month) throws IOException {
                String response = doGet("/api/gastos/mes/" + year + "/" + month);
                return objectMapper.readValue(response, new TypeReference<List<GastoDTO>>() {
                });
        }

        public GastoDTO obtenerGasto(Long id) throws IOException {
                String response = doGet("/api/gastos/" + id);
                return objectMapper.readValue(response, GastoDTO.class);
        }

        public List<GastoDTO> buscarGastos(String termino) throws IOException {
                String response = doGet("/api/gastos/buscar?termino=" + encode(termino));
                return objectMapper.readValue(response, new TypeReference<List<GastoDTO>>() {
                });
        }

        public GastoDTO crearGasto(GastoDTO gasto) throws IOException {
                String json = objectMapper.writeValueAsString(gasto);
                String response = doPost("/api/gastos", json);
                return objectMapper.readValue(response, GastoDTO.class);
        }

        public GastoDTO actualizarGasto(Long id, GastoDTO gasto) throws IOException {
                String json = objectMapper.writeValueAsString(gasto);
                String response = doPut("/api/gastos/" + id, json);
                return objectMapper.readValue(response, GastoDTO.class);
        }

        public GastoDTO marcarGastoPagado(Long id, String metodoPago) throws IOException {
                String response = doPut("/api/gastos/" + id + "/pagar?metodoPago=" + metodoPago, null);
                return objectMapper.readValue(response, GastoDTO.class);
        }

        public void generarGastosRecurrentes(int year, int month) throws IOException {
                doPost("/api/gastos/generar-recurrentes?year=" + year + "&month=" + month, null);
        }

        public void eliminarGasto(Long id) throws IOException {
                doDelete("/api/gastos/" + id);
        }

        // Resúmenes de gastos
        public Map<String, Object> obtenerResumenGastos(LocalDate desde, LocalDate hasta) throws IOException {
                String desdeStr = desde.format(DATE_FORMATTER);
                String hastaStr = hasta.format(DATE_FORMATTER);
                String response = doGet(
                                "/api/gastos/resumen/total?desde=" + encode(desdeStr) + "&hasta=" + encode(hastaStr));
                return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {
                });
        }

        public List<Map<String, Object>> obtenerResumenPorCategoriaGastos(LocalDate desde, LocalDate hasta)
                        throws IOException {
                String desdeStr = desde.format(DATE_FORMATTER);
                String hastaStr = hasta.format(DATE_FORMATTER);
                String response = doGet("/api/gastos/resumen/categorias?desde=" + encode(desdeStr) + "&hasta="
                                + encode(hastaStr));
                return objectMapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {
                });
        }

        public List<Map<String, Object>> obtenerEvolucionMensualGastos(int year) throws IOException {
                String response = doGet("/api/gastos/resumen/evolucion?year=" + year);
                return objectMapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {
                });
        }

        // ==================== RESUMEN FINANCIERO GLOBAL ====================

        public ResumenFinancieroDTO obtenerResumenFinanciero(LocalDate desde, LocalDate hasta) throws IOException {
                String desdeStr = desde.format(DATE_FORMATTER);
                String hastaStr = hasta.format(DATE_FORMATTER);
                String response = doGet(
                                "/api/finanzas/resumen?desde=" + encode(desdeStr) + "&hasta=" + encode(hastaStr));
                return objectMapper.readValue(response, ResumenFinancieroDTO.class);
        }

        // ==================== MÉTODOS DE FILTRADO COMBINADO ====================

        /**
         * Filtra facturas emitidas por estado y/o período
         */
        public List<FacturaEmitidaDTO> filtrarFacturasEmitidas(String estado, LocalDate desde, LocalDate hasta)
                        throws IOException {
                // Si hay período definido, usar filtro por período
                if (desde != null && hasta != null) {
                        List<FacturaEmitidaDTO> facturas = obtenerFacturasEmitidasPorPeriodo(desde, hasta);
                        // Filtrar por estado si no es "Todos"
                        if (estado != null && !estado.equals("Todos") && !estado.isEmpty()) {
                                final String estadoFinal = estado;
                                return facturas.stream()
                                                .filter(f -> estadoFinal.equals(f.getEstado()))
                                                .collect(java.util.stream.Collectors.toList());
                        }
                        return facturas;
                }

                // Si solo hay estado
                if (estado != null && !estado.equals("Todos") && !estado.isEmpty()) {
                        return obtenerFacturasEmitidasPorEstado(estado);
                }

                // Sin filtros, devolver todas
                return obtenerFacturasEmitidas();
        }

        /**
         * Filtra facturas recibidas por categoría, estado y/o período
         */
        public List<FacturaRecibidaDTO> filtrarFacturasRecibidas(String categoria, String estado, LocalDate desde,
                        LocalDate hasta) throws IOException {
                List<FacturaRecibidaDTO> facturas;

                // Obtener base según período o todas
                if (desde != null && hasta != null) {
                        facturas = obtenerFacturasRecibidasPorPeriodo(desde, hasta);
                } else {
                        facturas = obtenerFacturasRecibidas();
                }

                // Aplicar filtros adicionales
                return facturas.stream()
                                .filter(f -> categoria == null || categoria.equals("Todas") || categoria.isEmpty()
                                                || categoria.equals(f.getCategoria()))
                                .filter(f -> estado == null || estado.equals("Todos") || estado.isEmpty()
                                                || estado.equals(f.getEstado()))
                                .collect(java.util.stream.Collectors.toList());
        }

        /**
         * Filtra gastos por categoría, período y/o recurrencia
         */
        public List<GastoDTO> filtrarGastos(String categoria, LocalDate desde, LocalDate hasta, boolean soloRecurrentes)
                        throws IOException {
                List<GastoDTO> gastos;

                // Si solo recurrentes
                if (soloRecurrentes) {
                        gastos = obtenerGastosRecurrentes();
                } else if (desde != null && hasta != null) {
                        gastos = obtenerGastosPorPeriodo(desde, hasta);
                } else {
                        gastos = obtenerGastos();
                }

                // Aplicar filtro de categoría
                if (categoria != null && !categoria.equals("Todas") && !categoria.isEmpty()) {
                        final String categoriaFinal = categoria;
                        return gastos.stream()
                                        .filter(g -> categoriaFinal.equals(g.getCategoria()))
                                        .collect(java.util.stream.Collectors.toList());
                }

                return gastos;
        }
}
package com.lavaderosepulveda.crm.api.service;

import com.google.gson.reflect.TypeToken;
import com.lavaderosepulveda.crm.api.client.ApiClient;
import com.lavaderosepulveda.crm.model.dto.ClienteDTO;
import com.lavaderosepulveda.crm.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClienteApiService {
    
    private static final Logger log = LoggerFactory.getLogger(ClienteApiService.class);
    
    private static ClienteApiService instance;
    private final ApiClient apiClient;
    private final ConfigManager config;
    private final String baseUrl;
    
    private ClienteApiService() {
        this.apiClient = ApiClient.getInstance();
        this.config = ConfigManager.getInstance();
        this.baseUrl = config.getClientesEndpoint();
    }
    
    public static synchronized ClienteApiService getInstance() {
        if (instance == null) {
            instance = new ClienteApiService();
        }
        return instance;
    }
    
    /**
     * Obtener todos los clientes
     */
    public List<ClienteDTO> obtenerTodosLosClientes() {
        try {
            String response = apiClient.getRaw(baseUrl);
            
            // Parsear JSON a List<ClienteDTO>
            Type listType = new TypeToken<ArrayList<ClienteDTO>>(){}.getType();
            List<ClienteDTO> clientes = apiClient.getGson().fromJson(response, listType);
            
            log.info("Obtenidos {} clientes de la API", clientes != null ? clientes.size() : 0);
            return clientes != null ? clientes : new ArrayList<>();
        } catch (IOException e) {
            log.error("Error al obtener clientes", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener cliente por ID
     */
    public ClienteDTO obtenerClientePorId(Long id) {
        try {
            String response = apiClient.getRaw(baseUrl + "/" + id);
            ClienteDTO cliente = apiClient.getGson().fromJson(response, ClienteDTO.class);
            log.info("Cliente obtenido: {}", id);
            return cliente;
        } catch (IOException e) {
            log.error("Error al obtener cliente: " + id, e);
            return null;
        }
    }
    
    /**
     * Obtener cliente por teléfono
     */
    public ClienteDTO obtenerClientePorTelefono(String telefono) {
        try {
            String response = apiClient.getRaw(baseUrl + "/telefono/" + telefono);
            ClienteDTO cliente = apiClient.getGson().fromJson(response, ClienteDTO.class);
            log.info("Cliente obtenido por teléfono: {}", telefono);
            return cliente;
        } catch (IOException e) {
            log.error("Error al obtener cliente por teléfono: " + telefono, e);
            return null;
        }
    }
    
    /**
     * Filtrar clientes por nombre (filtrado local)
     */
    public List<ClienteDTO> buscarPorNombre(String nombre) {
        try {
            List<ClienteDTO> todosLosClientes = obtenerTodosLosClientes();
            String nombreBusqueda = nombre.toLowerCase();
            
            return todosLosClientes.stream()
                .filter(cliente -> {
                    String nombreCompleto = cliente.getNombre();
                    if (cliente.getApellidos() != null) {
                        nombreCompleto += " " + cliente.getApellidos();
                    }
                    return nombreCompleto.toLowerCase().contains(nombreBusqueda);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al buscar clientes por nombre", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener clientes activos (filtrado local)
     */
    public List<ClienteDTO> obtenerClientesActivos() {
        try {
            List<ClienteDTO> todosLosClientes = obtenerTodosLosClientes();
            return todosLosClientes.stream()
                .filter(cliente -> cliente.getActivo() != null && cliente.getActivo())
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener clientes activos", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener clientes inactivos (filtrado local)
     */
    public List<ClienteDTO> obtenerClientesInactivos() {
        try {
            List<ClienteDTO> todosLosClientes = obtenerTodosLosClientes();
            return todosLosClientes.stream()
                .filter(cliente -> cliente.getActivo() == null || !cliente.getActivo())
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener clientes inactivos", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener clientes con más citas (filtrado local)
     */
    public List<ClienteDTO> obtenerTopClientesPorCitas(int limite) {
        try {
            List<ClienteDTO> todosLosClientes = obtenerTodosLosClientes();
            return todosLosClientes.stream()
                .sorted((c1, c2) -> {
                    int citas1 = c1.getTotalCitas() != null ? c1.getTotalCitas() : 0;
                    int citas2 = c2.getTotalCitas() != null ? c2.getTotalCitas() : 0;
                    return Integer.compare(citas2, citas1);
                })
                .limit(limite)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al obtener top clientes por citas", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener clientes con más facturación - USANDO ENDPOINT DEL BACKEND
     */
    public List<ClienteDTO> obtenerTopClientesPorFacturacion(int limite) {
        try {
            String response = apiClient.getRaw(baseUrl + "/top-facturacion?limit=" + limite);
            
            Type listType = new TypeToken<ArrayList<ClienteDTO>>(){}.getType();
            List<ClienteDTO> clientes = apiClient.getGson().fromJson(response, listType);
            
            log.info("Obtenidos {} top clientes por facturación desde API", clientes != null ? clientes.size() : 0);
            return clientes != null ? clientes : new ArrayList<>();
        } catch (IOException e) {
            log.error("Error al obtener top clientes por facturación", e);
            // Fallback: filtrado local
            return obtenerTopClientesPorFacturacionLocal(limite);
        }
    }
    
    /**
     * Fallback: Obtener top clientes por facturación (filtrado local)
     */
    private List<ClienteDTO> obtenerTopClientesPorFacturacionLocal(int limite) {
        try {
            List<ClienteDTO> todosLosClientes = obtenerTodosLosClientes();
            return todosLosClientes.stream()
                .sorted((c1, c2) -> {
                    double fact1 = c1.getTotalFacturado() != null ? c1.getTotalFacturado() : 0.0;
                    double fact2 = c2.getTotalFacturado() != null ? c2.getTotalFacturado() : 0.0;
                    return Double.compare(fact2, fact1);
                })
                .limit(limite)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error en fallback de top clientes por facturación", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener clientes con más no presentaciones - USANDO ENDPOINT DEL BACKEND
     */
    public List<ClienteDTO> obtenerClientesConMasNoPresentaciones(int limite) {
        try {
            String response = apiClient.getRaw(baseUrl + "/no-presentaciones?limit=" + limite);
            
            Type listType = new TypeToken<ArrayList<ClienteDTO>>(){}.getType();
            List<ClienteDTO> clientes = apiClient.getGson().fromJson(response, listType);
            
            log.info("Obtenidos {} clientes con más no presentaciones desde API", clientes != null ? clientes.size() : 0);
            return clientes != null ? clientes : new ArrayList<>();
        } catch (IOException e) {
            log.error("Error al obtener clientes con más no presentaciones", e);
            // Fallback: filtrado local
            return obtenerClientesConMasNoPresentacionesLocal(limite);
        }
    }
    
    /**
     * Fallback: Obtener clientes con más no presentaciones (filtrado local)
     */
    private List<ClienteDTO> obtenerClientesConMasNoPresentacionesLocal(int limite) {
        try {
            List<ClienteDTO> todosLosClientes = obtenerTodosLosClientes();
            return todosLosClientes.stream()
                .filter(cliente -> cliente.getCitasNoPresentadas() != null && cliente.getCitasNoPresentadas() > 0)
                .sorted((c1, c2) -> {
                    int noP1 = c1.getCitasNoPresentadas() != null ? c1.getCitasNoPresentadas() : 0;
                    int noP2 = c2.getCitasNoPresentadas() != null ? c2.getCitasNoPresentadas() : 0;
                    return Integer.compare(noP2, noP1);
                })
                .limit(limite)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error en fallback de clientes con más no presentaciones", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Crear nuevo cliente (cuando esté disponible en la API)
     */
    public ClienteDTO crear(ClienteDTO cliente) {
        try {
            ClienteDTO nuevoCliente = apiClient.post(baseUrl, cliente, ClienteDTO.class);
            log.info("Cliente creado: {}", nuevoCliente.getId());
            return nuevoCliente;
        } catch (IOException e) {
            log.error("Error al crear cliente", e);
            return null;
        }
    }
    
    /**
     * Actualizar cliente (cuando esté disponible en la API)
     */
    public ClienteDTO actualizar(Long id, ClienteDTO cliente) {
        try {
            ClienteDTO clienteActualizado = apiClient.put(baseUrl + "/" + id, cliente, ClienteDTO.class);
            log.info("Cliente actualizado: {}", id);
            return clienteActualizado;
        } catch (IOException e) {
            log.error("Error al actualizar cliente: " + id, e);
            return null;
        }
    }
    
    /**
     * Eliminar cliente (cuando esté disponible en la API)
     */
    public boolean eliminar(Long id) {
        try {
            apiClient.delete(baseUrl + "/" + id);
            log.info("Cliente eliminado: {}", id);
            return true;
        } catch (IOException e) {
            log.error("Error al eliminar cliente: " + id, e);
            return false;
        }
    }
    
    /**
     * Contar total de clientes
     */
    public long contarTotalClientes() {
        try {
            List<ClienteDTO> clientes = obtenerTodosLosClientes();
            return clientes.size();
        } catch (Exception e) {
            log.error("Error al contar clientes", e);
            return 0;
        }
    }
    
    /**
     * Contar clientes activos
     */
    public long contarClientesActivos() {
        try {
            List<ClienteDTO> activos = obtenerClientesActivos();
            return activos.size();
        } catch (Exception e) {
            log.error("Error al contar clientes activos", e);
            return 0;
        }
    }
}
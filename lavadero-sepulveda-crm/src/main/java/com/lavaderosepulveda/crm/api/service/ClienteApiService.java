package com.lavaderosepulveda.crm.api.service;

import com.google.gson.reflect.TypeToken;
import com.lavaderosepulveda.crm.api.ApiClient;
import com.lavaderosepulveda.crm.api.dto.ClienteDTO;
import com.lavaderosepulveda.crm.config.ConfigManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ClienteApiService {
    
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
    public List<ClienteDTO> findAll() {
        try {
            String response = apiClient.getRaw(baseUrl);
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
    public ClienteDTO findById(Long id) {
        try {
            ClienteDTO cliente = apiClient.get(baseUrl + "/" + id, ClienteDTO.class);
            log.info("Cliente obtenido: {}", id);
            return cliente;
        } catch (IOException e) {
            log.error("Error al obtener cliente: " + id, e);
            return null;
        }
    }
    
    /**
     * Buscar clientes por nombre (filtrado local)
     */
    public List<ClienteDTO> buscarPorNombre(String nombre) {
        try {
            List<ClienteDTO> todosLosClientes = findAll();
            String nombreLower = nombre.toLowerCase();
            return todosLosClientes.stream()
                .filter(cliente -> {
                    String nombreCompleto = (cliente.getNombre() + " " + cliente.getApellidos()).toLowerCase();
                    return nombreCompleto.contains(nombreLower);
                })
                .toList();
        } catch (Exception e) {
            log.error("Error al buscar clientes por nombre", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Buscar cliente por teléfono (filtrado local)
     */
    public ClienteDTO buscarPorTelefono(String telefono) {
        try {
            List<ClienteDTO> todosLosClientes = findAll();
            return todosLosClientes.stream()
                .filter(cliente -> telefono.equals(cliente.getTelefono()))
                .findFirst()
                .orElse(null);
        } catch (Exception e) {
            log.error("Error al buscar cliente por teléfono", e);
            return null;
        }
    }
    
    /**
     * Obtener clientes activos (filtrado local)
     */
    public List<ClienteDTO> findActivos() {
        try {
            List<ClienteDTO> todosLosClientes = findAll();
            return todosLosClientes.stream()
                .filter(cliente -> cliente.getActivo() != null && cliente.getActivo())
                .toList();
        } catch (Exception e) {
            log.error("Error al obtener clientes activos", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener top clientes por facturación (calculado localmente)
     */
    public List<ClienteDTO> findTopClientesPorFacturacion(int limit) {
        try {
            List<ClienteDTO> todosLosClientes = findAll();
            return todosLosClientes.stream()
                .sorted((c1, c2) -> {
                    Double total1 = c1.getTotalFacturado() != null ? c1.getTotalFacturado() : 0.0;
                    Double total2 = c2.getTotalFacturado() != null ? c2.getTotalFacturado() : 0.0;
                    return total2.compareTo(total1); // Descendente
                })
                .limit(limit)
                .toList();
        } catch (Exception e) {
            log.error("Error al obtener top clientes", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener clientes con más no presentaciones (calculado localmente)
     */
    public List<ClienteDTO> findClientesConMasNoPresentaciones(int limit) {
        try {
            List<ClienteDTO> todosLosClientes = findAll();
            return todosLosClientes.stream()
                .filter(cliente -> cliente.getCitasNoPresentadas() != null && cliente.getCitasNoPresentadas() > 0)
                .sorted((c1, c2) -> {
                    Integer np1 = c1.getCitasNoPresentadas() != null ? c1.getCitasNoPresentadas() : 0;
                    Integer np2 = c2.getCitasNoPresentadas() != null ? c2.getCitasNoPresentadas() : 0;
                    return np2.compareTo(np1); // Descendente
                })
                .limit(limit)
                .toList();
        } catch (Exception e) {
            log.error("Error al obtener clientes con no presentaciones", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Crear nuevo cliente
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
     * Actualizar cliente
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
     * Eliminar cliente
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
     * Contar clientes
     */
    public long count() {
        try {
            String response = apiClient.getRaw(baseUrl + "/count");
            return Long.parseLong(response);
        } catch (Exception e) {
            log.error("Error al contar clientes", e);
            return 0;
        }
    }
}

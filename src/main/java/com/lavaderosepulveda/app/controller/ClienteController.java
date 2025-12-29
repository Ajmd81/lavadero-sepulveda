package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.ClienteDTO;
import com.lavaderosepulveda.app.service.ClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    private static final Logger log = LoggerFactory.getLogger(ClienteController.class);

    @Autowired
    private ClienteService clienteService;

    /**
     * GET /api/clientes
     * Obtener todos los clientes
     */
    @GetMapping
    public ResponseEntity<List<ClienteDTO>> obtenerTodosLosClientes() {
        try {
            List<ClienteDTO> clientes = clienteService.obtenerTodosLosClientes();
            log.info("Obtenidos {} clientes", clientes.size());
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            log.error("Error al obtener clientes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/clientes/{id}
     * Obtener cliente por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> obtenerClientePorId(@PathVariable Long id) {
        try {
            return clienteService.obtenerClientePorId(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error al obtener cliente con ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/clientes/telefono/{telefono}
     * Obtener cliente por teléfono
     */
    @GetMapping("/telefono/{telefono}")
    public ResponseEntity<ClienteDTO> obtenerClientePorTelefono(@PathVariable String telefono) {
        try {
            return clienteService.obtenerClientePorTelefono(telefono)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error al obtener cliente con teléfono: {}", telefono, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/clientes
     * Crear nuevo cliente
     */
    @PostMapping
    public ResponseEntity<?> crearCliente(@RequestBody ClienteDTO clienteDTO) {
        try {
            // Validaciones
            if (clienteDTO.getNombre() == null || clienteDTO.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El nombre es obligatorio"));
            }
            if (clienteDTO.getTelefono() == null || clienteDTO.getTelefono().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El teléfono es obligatorio"));
            }

            ClienteDTO clienteCreado = clienteService.crearCliente(clienteDTO);
            log.info("Cliente creado: {} - {}", clienteCreado.getId(), clienteCreado.getNombre());

            return ResponseEntity.status(HttpStatus.CREATED).body(clienteCreado);

        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al crear cliente: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al crear cliente", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    /**
     * PUT /api/clientes/{id}
     * Actualizar cliente existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCliente(
            @PathVariable Long id,
            @RequestBody ClienteDTO clienteDTO) {
        try {
            // Validaciones
            if (clienteDTO.getNombre() == null || clienteDTO.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El nombre es obligatorio"));
            }
            if (clienteDTO.getTelefono() == null || clienteDTO.getTelefono().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El teléfono es obligatorio"));
            }

            ClienteDTO clienteActualizado = clienteService.actualizarCliente(id, clienteDTO);
            log.info("Cliente actualizado: {} - {}", id, clienteActualizado.getNombre());

            return ResponseEntity.ok(clienteActualizado);

        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al actualizar cliente {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al actualizar cliente {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    /**
     * DELETE /api/clientes/{id}
     * Eliminar cliente
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCliente(@PathVariable Long id) {
        try {
            clienteService.eliminarCliente(id);
            log.info("Cliente eliminado: {}", id);

            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("Error al eliminar cliente {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al eliminar cliente {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    /**
     * POST /api/clientes/migrar
     * Migrar clientes desde citas (ejecutar una sola vez)
     */
    @PostMapping("/migrar")
    public ResponseEntity<?> migrarClientesDesdeCitas() {
        try {
            int clientesMigrados = clienteService.migrarClientesDesdeCitas();
            log.info("Migración completada: {} clientes", clientesMigrados);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Migración completada",
                    "clientesMigrados", clientesMigrados
            ));

        } catch (Exception e) {
            log.error("Error al migrar clientes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al migrar clientes: " + e.getMessage()));
        }
    }

    // ========================================
    // NUEVOS ENDPOINTS PARA INTEGRACIÓN CRM
    // ========================================

    /**
     * GET /api/clientes/activos
     * Obtener solo clientes activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<ClienteDTO>> obtenerClientesActivos() {
        try {
            List<ClienteDTO> clientes = clienteService.obtenerClientesActivos();
            log.info("Obtenidos {} clientes activos", clientes.size());
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            log.error("Error al obtener clientes activos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/clientes/buscar?nombre=X
     * Buscar clientes por nombre
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteDTO>> buscarClientesPorNombre(
            @RequestParam(value = "nombre", required = false) String nombre) {
        try {
            if (nombre == null || nombre.trim().isEmpty()) {
                return ResponseEntity.ok(clienteService.obtenerTodosLosClientes());
            }
            
            List<ClienteDTO> clientes = clienteService.buscarPorNombre(nombre);
            log.info("Encontrados {} clientes con nombre que contiene '{}'", clientes.size(), nombre);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            log.error("Error al buscar clientes por nombre: {}", nombre, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/clientes/count
     * Contar total de clientes
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> contarClientes() {
        try {
            long total = clienteService.contarClientes();
            long activos = clienteService.contarClientesActivos();
            
            Map<String, Long> counts = Map.of(
                    "total", total,
                    "activos", activos,
                    "inactivos", total - activos
            );
            
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            log.error("Error al contar clientes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/clientes/top-facturacion?limit=10
     * Obtener top clientes por facturación
     */
    @GetMapping("/top-facturacion")
    public ResponseEntity<List<ClienteDTO>> obtenerTopClientesPorFacturacion(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        try {
            List<ClienteDTO> topClientes = clienteService.obtenerTopClientesPorFacturacion(limit);
            log.info("Obtenidos top {} clientes por facturación", topClientes.size());
            return ResponseEntity.ok(topClientes);
        } catch (Exception e) {
            log.error("Error al obtener top clientes por facturación", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/clientes/no-presentaciones?limit=10
     * Obtener clientes con más no presentaciones
     */
    @GetMapping("/no-presentaciones")
    public ResponseEntity<List<ClienteDTO>> obtenerClientesConMasNoPresentaciones(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        try {
            List<ClienteDTO> clientesProblematicos = clienteService.obtenerClientesConMasNoPresentaciones(limit);
            log.info("Obtenidos {} clientes con más no presentaciones", clientesProblematicos.size());
            return ResponseEntity.ok(clientesProblematicos);
        } catch (Exception e) {
            log.error("Error al obtener clientes con no presentaciones", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/clientes/estadisticas
     * Obtener estadísticas generales de clientes
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasClientes() {
        try {
            Map<String, Object> stats = clienteService.obtenerEstadisticasClientes();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas de clientes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
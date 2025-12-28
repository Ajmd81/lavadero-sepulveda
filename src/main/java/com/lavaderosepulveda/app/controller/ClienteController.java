package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.ClienteDTO;
import com.lavaderosepulveda.app.service.ClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);

    @Autowired
    private ClienteService clienteService;

    /**
     * GET /api/clientes
     * Obtener todos los clientes
     */
    @GetMapping
    public ResponseEntity<List<ClienteDTO>> obtenerTodos() {
        try {
            List<ClienteDTO> clientes = clienteService.obtenerTodosLosClientes();
            logger.info("Obtenidos {} clientes", clientes.size());
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            logger.error("Error al obtener clientes", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/clientes/{id}
     * Obtener cliente por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> obtenerPorId(@PathVariable Long id) {
        try {
            return clienteService.obtenerClientePorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error al obtener cliente {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/clientes/telefono/{telefono}
     * Buscar cliente por teléfono
     */
    @GetMapping("/telefono/{telefono}")
    public ResponseEntity<ClienteDTO> obtenerPorTelefono(@PathVariable String telefono) {
        try {
            return clienteService.obtenerClientePorTelefono(telefono)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error al buscar cliente por teléfono {}", telefono, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
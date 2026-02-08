package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.ProveedorDTO;
import com.lavaderosepulveda.app.service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorApiController {

    @Autowired
    private ProveedorService proveedorService;

    /**
     * GET /api/proveedores?page=0&size=10&sortBy=nombre&sortDir=asc
     * Obtener proveedores activos con paginación
     * Si NO se envían parámetros de paginación, devuelve la lista completa (compatibilidad)
     */
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        // Si NO se proporcionan parámetros de paginación, usar el método original
        if (page == null || size == null) {
            return ResponseEntity.ok(proveedorService.listarActivos());
        }
        
        // Si SÍ se proporcionan, usar paginación
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProveedorDTO> pageProveedores = proveedorService.listarActivosPaginados(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", pageProveedores.getContent());
        response.put("currentPage", pageProveedores.getNumber());
        response.put("totalItems", pageProveedores.getTotalElements());
        response.put("totalPages", pageProveedores.getTotalPages());
        response.put("size", pageProveedores.getSize());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/todos")
    public ResponseEntity<List<ProveedorDTO>> listarTodos() {
        return ResponseEntity.ok(proveedorService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorDTO> obtener(@PathVariable Long id) {
        ProveedorDTO proveedor = proveedorService.obtenerPorId(id);
        if (proveedor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(proveedor);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProveedorDTO>> buscar(@RequestParam String termino) {
        return ResponseEntity.ok(proveedorService.buscar(termino));
    }

    @PostMapping
    public ResponseEntity<ProveedorDTO> crear(@RequestBody ProveedorDTO dto) {
        return ResponseEntity.ok(proveedorService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorDTO> actualizar(@PathVariable Long id, @RequestBody ProveedorDTO dto) {
        return ResponseEntity.ok(proveedorService.actualizar(id, dto));
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Map<String, String>> desactivar(@PathVariable Long id) {
        proveedorService.desactivar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Proveedor desactivado correctamente"));
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<Map<String, String>> activar(@PathVariable Long id) {
        proveedorService.activar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Proveedor activado correctamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        proveedorService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Proveedor eliminado correctamente"));
    }
}
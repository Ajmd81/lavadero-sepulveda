package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.ProveedorDTO;
import com.lavaderosepulveda.app.service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin(origins = "*")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    @GetMapping
    public ResponseEntity<List<ProveedorDTO>> listar() {
        return ResponseEntity.ok(proveedorService.listarActivos());
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

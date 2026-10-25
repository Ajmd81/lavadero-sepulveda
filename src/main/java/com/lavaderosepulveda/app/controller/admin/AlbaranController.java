package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.dto.AlbaranDTO;
import com.lavaderosepulveda.app.model.Albaran;
import com.lavaderosepulveda.app.service.AlbaranService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/albaranes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AlbaranController {
    
    private final AlbaranService albaranService;
    
    @GetMapping
    public ResponseEntity<List<AlbaranDTO>> getAllAlbaranes() {
        return ResponseEntity.ok(albaranService.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AlbaranDTO> getAlbaranById(@PathVariable Long id) {
        return ResponseEntity.ok(albaranService.findById(id));
    }
    
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<AlbaranDTO>> getAlbaranesByCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(albaranService.findByCliente(clienteId));
    }
    
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<AlbaranDTO>> getAlbaranesByEstado(@PathVariable String estado) {
        Albaran.EstadoAlbaran estadoEnum = Albaran.EstadoAlbaran.valueOf(estado.toUpperCase());
        return ResponseEntity.ok(albaranService.findByEstado(estadoEnum));
    }
    
    @GetMapping("/pendientes-facturar/{clienteId}")
    public ResponseEntity<List<AlbaranDTO>> getAlbaranesPendientesFacturar(@PathVariable Long clienteId) {
        return ResponseEntity.ok(albaranService.findPendientesParaFacturar(clienteId));
    }
    
    @PostMapping
    public ResponseEntity<AlbaranDTO> createAlbaran(@RequestBody AlbaranDTO albaranDTO) {
        AlbaranDTO created = albaranService.create(albaranDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AlbaranDTO> updateAlbaran(
            @PathVariable Long id,
            @RequestBody AlbaranDTO albaranDTO) {
        return ResponseEntity.ok(albaranService.update(id, albaranDTO));
    }
    
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {
        Albaran.EstadoAlbaran estadoEnum = Albaran.EstadoAlbaran.valueOf(estado.toUpperCase());
        albaranService.cambiarEstado(id, estadoEnum);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlbaran(@PathVariable Long id) {
        albaranService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
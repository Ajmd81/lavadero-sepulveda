package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.model.enums.CategoriaGasto;
import com.lavaderosepulveda.app.model.enums.MetodoPago;
import com.lavaderosepulveda.app.model.enums.EstadoFactura;
import com.lavaderosepulveda.app.model.enums.TipoLavado;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enums")
@CrossOrigin(origins = {"https://lavadero-sepulveda.vercel.app", "http://localhost:3000"})
public class EnumsApiController {

    @GetMapping("/categorias-gasto")
    public ResponseEntity<List<Map<String, String>>> obtenerCategoriasGasto() {
        List<Map<String, String>> categorias = Arrays.stream(CategoriaGasto.values())
                .map(cat -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("valor", cat.name());
                    map.put("descripcion", cat.getDescripcion());
                    return map;
                })
                .toList();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/metodos-pago")
    public ResponseEntity<List<Map<String, String>>> obtenerMetodosPago() {
        List<Map<String, String>> metodos = Arrays.stream(MetodoPago.values())
                .map(metodo -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("valor", metodo.name());
                    map.put("descripcion", metodo.getDescripcion());
                    return map;
                })
                .toList();
        return ResponseEntity.ok(metodos);
    }

    @GetMapping("/estados-factura")
    public ResponseEntity<List<Map<String, String>>> obtenerEstadosFactura() {
        List<Map<String, String>> estados = Arrays.stream(EstadoFactura.values())
                .map(estado -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("valor", estado.name());
                    map.put("descripcion", estado.getDescripcion());
                    return map;
                })
                .toList();
        return ResponseEntity.ok(estados);
    }

    @GetMapping("/tipos-lavado")
    public ResponseEntity<List<Map<String, Object>>> obtenerTiposLavado() {
        List<Map<String, Object>> tipos = Arrays.stream(TipoLavado.values())
                .map(tipo -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("valor", tipo.name());
                    map.put("descripcion", tipo.getDescripcion());
                    map.put("precio", tipo.getPrecio());
                    return map;
                })
                .toList();
        return ResponseEntity.ok(tipos);
    }
}

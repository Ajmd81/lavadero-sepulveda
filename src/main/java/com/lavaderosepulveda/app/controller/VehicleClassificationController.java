package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.model.TipoLavado;
import com.lavaderosepulveda.app.model.VehicleModel;
import com.lavaderosepulveda.app.repository.VehicleModelRepository;
import com.lavaderosepulveda.app.service.VehicleClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class VehicleClassificationController {

    @Autowired
    private VehicleClassificationService vehicleClassificationService;

    @Autowired
    private VehicleModelRepository modelRepository;

    @GetMapping("/vehicle/classify")
    public ResponseEntity<Map<String, Object>> classifyVehicle(@RequestParam("model") String vehicleModel) {
        try {
            String category = vehicleClassificationService.classifyVehicle(vehicleModel);
            List<TipoLavado> availableServices = vehicleClassificationService.getAvailableServices(category);
            String categoryDescription = vehicleClassificationService.getCategoryDescription(category);

            Map<String, Object> response = new HashMap<>();
            response.put("category", category);
            response.put("categoryDescription", categoryDescription);
            response.put("availableServices", availableServices);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/models/all")
    public ResponseEntity<Map<String, List<String>>> getAllModels() {
        List<VehicleModel> allModels = modelRepository.findAll();

        Map<String, List<String>> modelsByCategory = allModels.stream()
                .collect(Collectors.groupingBy(
                        vm -> vm.getCategory().getName(),
                        Collectors.mapping(VehicleModel::getName, Collectors.toList())
                ));

        return ResponseEntity.ok(modelsByCategory);
    }

    @GetMapping("/debug/buscar-modelo")
    public ResponseEntity<Map<String, Object>> buscarModelo(@RequestParam("modelo") String modelo) {
        String normalizado = modelo.toLowerCase().trim();

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("modeloOriginal", modelo);
        resultado.put("modeloNormalizado", normalizado);

        // Buscar con diferentes variaciones
        List<VehicleModel> allModels = modelRepository.findAll();

        // Buscar coincidencias exactas
        List<VehicleModel> exact = allModels.stream()
                .filter(vm -> vm.getName().equalsIgnoreCase(normalizado))
                .collect(Collectors.toList());

        // Buscar coincidencias que contengan el texto
        List<VehicleModel> contains = allModels.stream()
                .filter(vm -> vm.getName().toLowerCase().contains(normalizado))
                .collect(Collectors.toList());

        // Buscar coincidencias sin la primera palabra (marca)
        String[] parts = normalizado.split("\\s+");
        String sinMarca = parts.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length)) : normalizado;

        List<VehicleModel> sinMarcaMatches = allModels.stream()
                .filter(vm -> vm.getName().toLowerCase().contains(sinMarca))
                .collect(Collectors.toList());

        resultado.put("coincidenciaExacta", exact);
        resultado.put("coincidenciaContiene", contains);
        resultado.put("modeloSinMarca", sinMarca);
        resultado.put("coincidenciaSinMarca", sinMarcaMatches);

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/models/search")
    public ResponseEntity<List<VehicleModel>> searchModels(@RequestParam("query") String query) {
        List<VehicleModel> allModels = modelRepository.findAll();

        String normalizedQuery = query.toLowerCase().trim();

        List<VehicleModel> results = allModels.stream()
                .filter(vm -> vm.getName().toLowerCase().contains(normalizedQuery))
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }
}
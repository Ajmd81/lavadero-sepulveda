package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.model.enums.TipoLavado;
import com.lavaderosepulveda.app.model.VehicleCategory;
import com.lavaderosepulveda.app.model.VehicleModel;
import com.lavaderosepulveda.app.repository.VehicleCategoryRepository;
import com.lavaderosepulveda.app.repository.VehicleModelRepository;
import com.lavaderosepulveda.app.service.VehicleClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @Autowired
    private VehicleCategoryRepository categoryRepository;

    /**
     * Clasifica un vehículo por nombre del modelo
     */
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

    /**
     * Clasifica un vehículo por ID (más fiable que por nombre)
     */
    @GetMapping("/vehicle/classify-by-id")
    public ResponseEntity<Map<String, Object>> classifyVehicleById(@RequestParam("id") Long modelId) {
        try {
            return modelRepository.findById(modelId).map(vehicleModel -> {
                String category = vehicleModel.getCategory().getName();
                List<TipoLavado> availableServices = vehicleClassificationService.getAvailableServices(category);
                String categoryDescription = vehicleClassificationService.getCategoryDescription(category);

                Map<String, Object> response = new HashMap<>();
                response.put("category", category);
                response.put("categoryDescription", categoryDescription);
                response.put("availableServices", availableServices);
                response.put("modelName", vehicleModel.getName());

                return ResponseEntity.ok(response);
            }).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene todos los modelos agrupados por categoría
     */
    @GetMapping("/models/all")
    public ResponseEntity<Map<String, List<String>>> getAllModels() {
        List<VehicleModel> allModels = modelRepository.findAll();

        Map<String, List<String>> modelsByCategory = allModels.stream()
                .collect(Collectors.groupingBy(
                        vm -> vm.getCategory().getName(),
                        Collectors.mapping(VehicleModel::getName, Collectors.toList())));

        return ResponseEntity.ok(modelsByCategory);
    }

    /**
     * Devuelve todas las marcas con sus modelos en estructura de cascada
     * Formato: {Marca: [{id, name}, ...], ...}
     */
    @GetMapping("/vehicle/brands-models")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getBrandsWithModels() {
        List<String> brands = modelRepository.findAllDistinctBrands();
        Map<String, List<Map<String, Object>>> result = new java.util.TreeMap<>();

        for (String brand : brands) {
            List<Map<String, Object>> models = modelRepository.findByBrandIgnoreCase(brand)
                    .stream()
                    .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                    .map(vm -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", vm.getId());
                        m.put("name", vm.getName());
                        return m;
                    })
                    .collect(Collectors.toList());
            result.put(brand, models);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Busca modelos por query (búsqueda por nombre)
     */
    @GetMapping("/models/search")
    public ResponseEntity<List<VehicleModel>> searchModels(@RequestParam("query") String query) {
        List<VehicleModel> allModels = modelRepository.findAll();
        String normalizedQuery = query.toLowerCase().trim();

        List<VehicleModel> results = allModels.stream()
                .filter(vm -> vm.getName().toLowerCase().contains(normalizedQuery))
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    /**
     * Debug: buscar modelo con múltiples estrategias de búsqueda
     */
    @GetMapping("/debug/buscar-modelo")
    public ResponseEntity<Map<String, Object>> buscarModelo(@RequestParam("modelo") String modelo) {
        String normalizado = modelo.toLowerCase().trim();
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("modeloOriginal", modelo);
        resultado.put("modeloNormalizado", normalizado);

        List<VehicleModel> allModels = modelRepository.findAll();

        List<VehicleModel> exact = allModels.stream()
                .filter(vm -> vm.getName().equalsIgnoreCase(normalizado))
                .collect(Collectors.toList());

        List<VehicleModel> contains = allModels.stream()
                .filter(vm -> vm.getName().toLowerCase().contains(normalizado))
                .collect(Collectors.toList());

        String[] parts = normalizado.split("\\s+");
        String sinMarca = parts.length > 1 ? 
            String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length)) : normalizado;

        List<VehicleModel> sinMarcaMatches = allModels.stream()
                .filter(vm -> vm.getName().toLowerCase().contains(sinMarca))
                .collect(Collectors.toList());

        resultado.put("coincidenciaExacta", exact);
        resultado.put("coincidenciaContiene", contains);
        resultado.put("modeloSinMarca", sinMarca);
        resultado.put("coincidenciaSinMarca", sinMarcaMatches);

        return ResponseEntity.ok(resultado);
    }

    /**
     * POST: Crear nuevo modelo de vehículo con validación de duplicados
     */
    @PostMapping("/vehicle-models")
    public ResponseEntity<?> createVehicleModel(@RequestBody VehicleModelDTO dto) {
        try {
            // Validar que la categoría existe
            VehicleCategory category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

            // Validar que no haya duplicados (normaliza brand + name)
            vehicleClassificationService.validateNoDuplicate(dto.getBrand(), dto.getName());

            // Crear modelo
            VehicleModel model = new VehicleModel();
            model.setName(dto.getName().toLowerCase().trim());
            model.setBrand(dto.getBrand().toLowerCase().trim());
            model.setCategory(category);

            VehicleModel saved = modelRepository.save(model);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(convertToDTO(saved));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("error_validacion", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("error_interno", e.getMessage()));
        }
    }

    /**
     * PUT: Actualizar modelo de vehículo con validación de duplicados
     */
    @PutMapping("/vehicle-models/{id}")
    public ResponseEntity<?> updateVehicleModel(
            @PathVariable Long id,
            @RequestBody VehicleModelDTO dto) {
        try {
            VehicleModel model = modelRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Modelo no encontrado"));

            VehicleCategory category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

            // Validar duplicados solo si cambió el brand o name
            if (!model.getBrand().equalsIgnoreCase(dto.getBrand()) || 
                !model.getName().equalsIgnoreCase(dto.getName())) {
                vehicleClassificationService.validateNoDuplicate(dto.getBrand(), dto.getName());
            }

            model.setName(dto.getName().toLowerCase().trim());
            model.setBrand(dto.getBrand().toLowerCase().trim());
            model.setCategory(category);

            VehicleModel updated = modelRepository.save(model);

            return ResponseEntity.ok(convertToDTO(updated));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("error_validacion", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("error_interno", e.getMessage()));
        }
    }

    /**
     * DELETE: Eliminar modelo de vehículo
     */
    @DeleteMapping("/vehicle-models/{id}")
    public ResponseEntity<?> deleteVehicleModel(@PathVariable Long id) {
        try {
            if (modelRepository.existsById(id)) {
                modelRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("error_interno", e.getMessage()));
        }
    }

    /**
     * GET: Obtener modelo por ID
     */
    @GetMapping("/vehicle-models/{id}")
    public ResponseEntity<?> getVehicleModelById(@PathVariable Long id) {
        return modelRepository.findById(id)
                .map(model -> ResponseEntity.ok(convertToDTO(model)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Convierte entidad VehicleModel a DTO
     */
    private VehicleModelDTO convertToDTO(VehicleModel model) {
        VehicleModelDTO dto = new VehicleModelDTO();
        dto.setId(model.getId());
        dto.setName(model.getName());
        dto.setBrand(model.getBrand());
        dto.setCategoryId(model.getCategory().getId());
        dto.setCategoryName(model.getCategory().getName());
        return dto;
    }

    /**
     * DTO para transferencia de datos
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VehicleModelDTO {
        private Long id;
        private String name;
        private String brand;
        private Long categoryId;
        private String categoryName;
    }

    /**
     * Respuesta de error estandarizada
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private String codigo;
        private String mensaje;
    }
}
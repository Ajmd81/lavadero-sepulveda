package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.model.TipoLavado;
import com.lavaderosepulveda.app.model.VehicleCategory;
import com.lavaderosepulveda.app.model.VehicleModel;
import com.lavaderosepulveda.app.repository.VehicleCategoryRepository;
import com.lavaderosepulveda.app.repository.VehicleModelRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VehicleClassificationService {

    private final VehicleModelRepository modelRepository;
    private final VehicleCategoryRepository categoryRepository;

    
    public VehicleClassificationService(VehicleModelRepository modelRepository, VehicleCategoryRepository categoryRepository) {
        this.modelRepository = modelRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Clasifica un vehículo basándose en su modelo, consultando la base de datos.
     */
    public String classifyVehicle(String vehicleModel) {
        if (vehicleModel == null || vehicleModel.trim().isEmpty()) {
            return "turismo"; // Categoría por defecto
        }

        String normalizedModel = normalizeVehicleModel(vehicleModel);

        // Buscar el modelo en la base de datos
        Optional<VehicleModel> modelOpt = modelRepository.findFirstByNameContainingNormalized(normalizedModel);

        if (modelOpt.isPresent()) {
            return modelOpt.get().getCategory().getName();
        }

        // Si no se encuentra, intentar detección por palabras clave como fallback
        return detectByKeywords(normalizedModel);
    }

    /**
     * Normaliza el modelo del vehículo para la búsqueda.
     */
    private String normalizeVehicleModel(String vehicleModel) {
        return vehicleModel.toLowerCase()
                .trim()
                .replaceAll("[^a-zA-Z0-9]", ""); // Quita espacios y caracteres especiales
    }

    /**
     * Detecta el tipo por palabras clave si no se encuentra en la base de datos.
     */
    private String detectByKeywords(String vehicleModel) {
        // Palabras clave para SUV/Todoterrenos
        if (vehicleModel.contains("suv") || vehicleModel.contains("4x4") || vehicleModel.contains("todoterreno")) {
            return "todoterreno";
        }
        // Palabras clave para furgonetas
        if (vehicleModel.contains("furgon") || vehicleModel.contains("van") || vehicleModel.contains("cargo")) {
            return "furgoneta_pequena";
        }
        // Palabras clave para monovolúmenes
        if (vehicleModel.contains("monovolumen") || vehicleModel.contains("mpv")) {
            return "monovolumen";
        }
        return "turismo"; // Por defecto
    }

    /**
     * Obtiene los tipos de lavado disponibles para una categoría de vehículo.
     */
    public List<TipoLavado> getAvailableServices(String vehicleCategoryName) {
        return Arrays.stream(TipoLavado.values())
                .filter(tipo -> isServiceAvailableForCategory(tipo, vehicleCategoryName))
                .collect(Collectors.toList());
    }

    /**
     * Verifica si un servicio está disponible para una categoría de vehículo.
     */
    private boolean isServiceAvailableForCategory(TipoLavado tipoLavado, String categoryName) {
        String serviceName = tipoLavado.name().toLowerCase();

        // Mapear nombres de categorías a los sufijos del enum
        String enumSuffix = mapCategoryToEnumSuffix(categoryName);

        // Verificar si el servicio corresponde a esta categoría específica
        if (serviceName.contains(enumSuffix)) {
            return true;
        }

        // Servicios genéricos disponibles para todas las categorías
        return serviceName.equals("tratamiento_ozono") ||
                serviceName.equals("encerado") ||
                serviceName.startsWith("tapiceria");
    }

    /**
     * Mapea los nombres de categorías a los sufijos usados en el enum TipoLavado.
     */
    private String mapCategoryToEnumSuffix(String categoryName) {
        switch (categoryName.toLowerCase()) {
            case "turismo":
                return "turismo";
            case "ranchera":
                return "ranchera";
            case "monovolumen":
                return "monovolumen";
            case "todoterreno":
                return "todoterreno";
            case "furgoneta_pequena":
                return "furgoneta_pequeña";
            case "furgoneta_grande":
                return "furgoneta_grande";
            default:
                return "turismo"; // Por defecto
        }
    }

    /**
     * Obtiene la descripción legible de una categoría a partir de su nombre.
     */
    public String getCategoryDescription(String categoryName) {
        return categoryRepository.findByName(categoryName)
                .map(VehicleCategory::getDescription)
                .orElse("Turismo"); // Devuelve 'Turismo' si la categoría no se encuentra
    }
}
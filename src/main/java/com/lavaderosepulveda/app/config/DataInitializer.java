package com.lavaderosepulveda.app.config;

import com.lavaderosepulveda.app.model.VehicleCategory;
import com.lavaderosepulveda.app.model.VehicleModel;
import com.lavaderosepulveda.app.repository.VehicleCategoryRepository;
import com.lavaderosepulveda.app.repository.VehicleModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private VehicleCategoryRepository categoryRepository;

    @Autowired
    private VehicleModelRepository modelRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try {
            if (categoryRepository.count() == 0) {
                logger.info("Iniciando población de base de datos con categorías y modelos de vehículos...");
                populateDatabase();
                logger.info("Base de datos poblada exitosamente. Total categorías: {}, Total modelos: {}",
                        categoryRepository.count(), modelRepository.count());
            } else {
                logger.info("Base de datos ya contiene datos. Categorías existentes: {}, Modelos existentes: {}",
                        categoryRepository.count(), modelRepository.count());
            }
        } catch (Exception e) {
            logger.error("Error al poblar la base de datos: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void populateDatabase() {
        // Definición de categorías con orden específico
        List<CategoryDefinition> categoryDefinitions = Arrays.asList(
                new CategoryDefinition("turismo", "Turismo", 1),
                new CategoryDefinition("ranchera", "Turismo Ranchera", 2),
                new CategoryDefinition("monovolumen", "Monovolumen/Todoterreno Pequeño", 3),
                new CategoryDefinition("todoterreno", "Todoterreno Grande", 4),
                new CategoryDefinition("furgoneta_pequena", "Furgoneta Pequeña", 5),
                new CategoryDefinition("furgoneta_grande", "Furgoneta Grande", 6)
        );

        Map<String, Set<String>> vehicleData = getVehicleData();
        Map<String, VehicleCategory> categories = new HashMap<>();

        // Crear y guardar categorías
        logger.info("Creando categorías de vehículos...");
        for (CategoryDefinition categoryDef : categoryDefinitions) {
            VehicleCategory category = new VehicleCategory();
            category.setName(categoryDef.getName());
            category.setDescription(categoryDef.getDescription());

            VehicleCategory savedCategory = categoryRepository.save(category);
            categories.put(categoryDef.getName(), savedCategory);
            logger.debug("Categoría creada: {} - {}", categoryDef.getName(), categoryDef.getDescription());
        }

        // Crear y guardar modelos en lotes para mejor rendimiento
        logger.info("Creando modelos de vehículos...");
        List<VehicleModel> allModels = new ArrayList<>();

        for (Map.Entry<String, Set<String>> entry : vehicleData.entrySet()) {
            VehicleCategory category = categories.get(entry.getKey());
            if (category != null) {
                Set<String> modelNames = entry.getValue();
                logger.debug("Procesando {} modelos para categoría: {}", modelNames.size(), entry.getKey());

                for (String modelName : modelNames) {
                    VehicleModel model = new VehicleModel();
                    model.setName(modelName.toLowerCase().trim());
                    model.setCategory(category);
                    allModels.add(model);
                }
            } else {
                logger.warn("Categoría no encontrada: {}", entry.getKey());
            }
        }

        // Guardar todos los modelos en lote
        if (!allModels.isEmpty()) {
            modelRepository.saveAll(allModels);
            logger.info("Guardados {} modelos de vehículos en total", allModels.size());
        }
    }

    private Map<String, Set<String>> getVehicleData() {
        Map<String, Set<String>> database = new HashMap<>();

        // Turismos - Vehículos compactos y sedanes
        Set<String> turismos = new LinkedHashSet<>(Arrays.asList(
                // Volkswagen
                "golf", "polo", "passat", "jetta", "arteon", "vento", "bora",
                // Seat
                "ibiza", "leon", "toledo", "cordoba", "exeo",
                // Ford
                "fiesta", "focus", "mondeo", "fusion", "escort", "sierra",
                // Opel
                "corsa", "astra", "insignia", "vectra", "calibra", "omega",
                // Renault
                "clio", "megane", "laguna", "fluence", "talisman", "symbol",
                // Peugeot
                "105", "205", "106", "206", "207", "208", "306", "307", "308", "406", "407", "408", "508",
                // Citroën
                "c1", "c2", "c3", "c4", "c5", "xsara", "saxo", "ax", "zx",
                // BMW
                "serie 1", "serie 2", "serie 3", "serie 4", "serie 5", "serie 6", "serie 7", "serie 8",
                "118", "120", "316", "318", "320", "325", "330", "520", "525", "530",
                // Mercedes
                "clase a", "clase b", "clase c", "clase e", "clase s", "cla", "cls",
                "a180", "a200", "c180", "c200", "c220", "e200", "e220", "e300",
                // Audi
                "a1", "a3", "a4", "a5", "a6", "a7", "a8", "tt",
                // Toyota
                "yaris", "corolla", "auris", "avensis", "camry", "prius",
                // Nissan
                "micra", "almera", "primera", "maxima", "sentra",
                // Honda
                "civic", "accord", "jazz", "insight",
                // Hyundai
                "i10", "i20", "i30", "i40", "elantra", "sonata", "ioniq", "accent",
                // Kia
                "rio", "ceed", "cerato", "optima",
                // Mazda
                "2", "3", "6", "mx5", "mx-5",
                // Skoda
                "fabia", "octavia", "superb", "rapid",
                // Fiat
                "punto", "bravo", "tipo", "linea"
        ));

        // Rancheras/Familiares
        Set<String> rancheras = new LinkedHashSet<>(Arrays.asList(
                "golf variant", "golf sportsvan", "passat variant", "passat alltrack",
                "leon st", "leon x-perience", "focus sw", "focus sportbreak",
                "astra sports tourer", "astra caravan", "insignia sports tourer",
                "megane estate", "megane grandtour", "laguna estate", "talisman estate",
                "308 sw", "407 sw", "508 sw", "c5 tourer",
                "serie 3 touring", "serie 5 touring",
                "clase c estate", "clase e estate",
                "a4 avant", "a6 avant", "a3 sportback",
                "corolla touring", "corolla estate",
                "octavia combi", "superb combi", "fabia combi"
        ));

        // Monovolúmenes y SUV pequeños
        Set<String> monovolumenes = new LinkedHashSet<>(Arrays.asList(
                // Monovolúmenes tradicionales
                "golf sportsvan", "touran", "sharan", "altea xl", "alhambra",
                "b-max", "c-max", "s-max", "galaxy", "zafira", "meriva",
                "scenic", "grand scenic", "espace", "modus",
                "1007", "807", "5008", "c3 picasso", "c4 picasso", "grand c4 picasso", "c8", "xsara picasso",
                "serie 2 active tourer", "serie 2 gran tourer", "i3",
                "clase b", "clase r", "clase v", "viano", "vaneo", "a2",
                "verso", "previa", "picnic", "yaris verso", "corolla verso", "avensis verso", "proace verso", "iq",
                "note", "cube", "tiida", "almera tino", "primera traveller", "serena",
                "fr-v", "stream", "odyssey", "shuttle", "matrix", "trajet", "h1", "ix20",
                "carens", "carnival", "sedona", "venga", "soul",
                "premacy", "mpv", "5", "biante", "roomster",
                "multipla", "ulysse", "idea", "doblo", "qubo", "500l", "phedra",

                // SUV compactos y crossovers
                "t-cross", "t-roc", "tiguan", "arona", "ateca", "tarraco",
                "kuga", "ecosport", "puma", "mokka", "crossland", "grandland",
                "captur", "kadjar", "arkana", "koleos", "3008", "2008",
                "c3 aircross", "c4 aircross", "c5 aircross",
                "x1", "x2", "x3", "gla", "glb", "glc", "q2", "q3", "q5",
                "c-hr", "rav4", "juke", "qashqai", "x-trail", "murano",
                "cr-v", "hr-v", "pilot", "tucson", "santa fe", "kona", "nexo",
                "sportage", "sorento", "niro", "stonic", "xceed",
                "cx-3", "cx-5", "cx-7", "cx-9", "cx-30",
                "yeti", "kamiq", "karoq", "kodiaq",
                "500x", "stelvio", "tonale"
        ));

        // Todoterrenos grandes
        Set<String> todoterrenos = new LinkedHashSet<>(Arrays.asList(
                "touareg", "cayenne", "macan", "tarraco",
                "edge", "explorer", "antara", "grandland",
                "koleos", "5008", "4008", "c5 aircross", "ds7",
                "x3", "x4", "x5", "x6", "x7",
                "gle", "gls", "q7", "q8",
                "rav4", "highlander", "land cruiser",
                "x-trail", "pathfinder", "patrol",
                "cr-v", "crv", "hr-v", "hrv",
                "santa fe", "sorento",
                "cx-5", "cx-7", "cx-9",
                "kodiaq", "karoq", "xc90",
                "range rover", "discovery", "defender", "freelander",
                "range rover sport", "range rover evoque", "range rover velar",
                "discovery sport", "f-pace", "e-pace", "i-pace"
        ));

        // Furgonetas pequeñas
        Set<String> furgonetasPequenas = new LinkedHashSet<>(Arrays.asList(
                "caddy", "combo", "berlingo", "partner", "kangoo",
                "doblo", "bipper", "nemo", "citan", "connect", "courier"
        ));

        // Furgonetas grandes
        Set<String> furgonetasGrandes = new LinkedHashSet<>(Arrays.asList(
                "crafter", "sprinter", "transit", "movano", "master",
                "ducato", "expert", "boxer", "jumper", "daily", "lt",
                "trafic", "vivaro", "primastar", "vito", "viano",
                "marco polo", "california"
        ));

        database.put("turismo", turismos);
        database.put("ranchera", rancheras);
        database.put("monovolumen", monovolumenes);
        database.put("todoterreno", todoterrenos);
        database.put("furgoneta_pequena", furgonetasPequenas);
        database.put("furgoneta_grande", furgonetasGrandes);

        return database;
    }

    // Clase interna para definir categorías con orden
    private static class CategoryDefinition {
        private final String name;
        private final String description;
        private final int order;

        public CategoryDefinition(String name, String description, int order) {
            this.name = name;
            this.description = description;
            this.order = order;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getOrder() { return order; }
    }
}
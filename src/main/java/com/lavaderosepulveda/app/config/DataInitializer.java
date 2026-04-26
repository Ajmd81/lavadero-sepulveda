package com.lavaderosepulveda.app.config;

import com.lavaderosepulveda.app.model.Usuario;
import com.lavaderosepulveda.app.repository.UsuarioRepository;
import com.lavaderosepulveda.app.model.VehicleCategory;
import com.lavaderosepulveda.app.model.VehicleModel;
import com.lavaderosepulveda.app.repository.VehicleCategoryRepository;
import com.lavaderosepulveda.app.repository.VehicleModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try {
            // ✅ MIGRAR USUARIO
            if (usuarioRepository.count() == 0) {
                logger.info("🔄 Migrando usuario inicial a base de datos...");
                Usuario admin = new Usuario();
                admin.setUsername(adminUsername);
                admin.setNombreCompleto("Administrador Principal");
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setEmail("admin@lavaderosepulveda.com");
                admin.setActivo(true);
                usuarioRepository.save(admin);
                logger.info("✅ Usuario migrado: {}", adminUsername);
                logger.warn("⚠️  IMPORTANTE: Cambia tu usuario y contraseña desde la interfaz web");
                logger.warn("⚠️  Luego elimina app.admin.* del application.properties");
            }

            if (categoryRepository.count() == 0) {
                // Primera vez: poblar desde cero
                logger.info("Iniciando población de base de datos con categorías y modelos de vehículos...");
                populateDatabase();
                logger.info("Base de datos poblada exitosamente. Total categorías: {}, Total modelos: {}",
                        categoryRepository.count(), modelRepository.count());
            } else {
                logger.info("Base de datos ya contiene datos. Categorías existentes: {}, Modelos existentes: {}",
                        categoryRepository.count(), modelRepository.count());

                // ✅ MIGRACIÓN: si los modelos existentes no tienen brand, asignarla
                long sinMarca = modelRepository.findAll().stream()
                        .filter(vm -> vm.getBrand() == null || vm.getBrand().isBlank())
                        .count();
                if (sinMarca > 0) {
                    logger.info("🔄 Detectados {} modelos sin marca. Iniciando migración de datos de marca...",
                            sinMarca);
                    migrateBrands();
                    logger.info("✅ Migración de marcas completada.");
                }

                // ✅ SINCRONIZACIÓN: añadir modelos nuevos que falten en la BD
                logger.info("🔄 Comprobando modelos nuevos para sincronizar...");
                syncMissingModels();
            }
        } catch (Exception e) {
            logger.error("Error al poblar la base de datos: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Compara el catálogo definido en getVehicleData() con lo que hay en BD
     * e inserta únicamente los modelos que falten, sin tocar los existentes.
     * La clave de unicidad es: nombre (en minúsculas) + categoría.
     */
    private void syncMissingModels() {
        Map<String, Map<String, List<String>>> vehicleData = getVehicleData();

        // Cargar categorías existentes en un mapa nombre → entidad
        Map<String, VehicleCategory> categoryMap = new HashMap<>();
        categoryRepository.findAll().forEach(c -> categoryMap.put(c.getName(), c));

        // Construir un Set con las claves de modelos ya existentes:
        // "categoryName::modelName"
        Set<String> existingKeys = new HashSet<>();
        modelRepository.findAll().forEach(vm -> {
            String catName = vm.getCategory() != null ? vm.getCategory().getName() : "";
            existingKeys.add(catName + "::" + vm.getName().toLowerCase().trim());
        });

        List<VehicleModel> toInsert = new ArrayList<>();

        for (Map.Entry<String, Map<String, List<String>>> catEntry : vehicleData.entrySet()) {
            String categoryName = catEntry.getKey();
            VehicleCategory category = categoryMap.get(categoryName);

            if (category == null) {
                logger.warn("⚠️  Categoría '{}' no encontrada en BD, se omite.", categoryName);
                continue;
            }

            for (Map.Entry<String, List<String>> brandEntry : catEntry.getValue().entrySet()) {
                String brand = brandEntry.getKey();
                for (String modelName : brandEntry.getValue()) {
                    String normalizedName = modelName.toLowerCase().trim();
                    String key = categoryName + "::" + normalizedName;

                    if (!existingKeys.contains(key)) {
                        VehicleModel model = new VehicleModel();
                        model.setName(normalizedName);
                        model.setBrand(brand);
                        model.setCategory(category);
                        toInsert.add(model);
                        existingKeys.add(key); // evitar duplicados dentro del propio batch
                    }
                }
            }
        }

        if (!toInsert.isEmpty()) {
            modelRepository.saveAll(toInsert);
            logger.info("✅ Sincronización completada: {} modelos nuevos añadidos.", toInsert.size());
        } else {
            logger.info("✅ Catálogo ya actualizado, no hay modelos nuevos que añadir.");
        }
    }

    /**
     * Pobla la BD desde cero con categorías, marcas y modelos.
     */
    private void populateDatabase() {
        List<CategoryDefinition> categoryDefinitions = Arrays.asList(
                new CategoryDefinition("turismo", "Turismo", 1),
                new CategoryDefinition("ranchera", "Turismo Ranchera", 2),
                new CategoryDefinition("monovolumen", "Monovolumen/Todoterreno Pequeño", 3),
                new CategoryDefinition("todoterreno", "Todoterreno Grande", 4),
                new CategoryDefinition("furgoneta_pequena", "Furgoneta Pequeña", 5),
                new CategoryDefinition("furgoneta_grande", "Furgoneta Grande", 6));

        Map<String, Map<String, List<String>>> vehicleData = getVehicleData();
        Map<String, VehicleCategory> categories = new HashMap<>();

        logger.info("Creando categorías de vehículos...");
        for (CategoryDefinition categoryDef : categoryDefinitions) {
            VehicleCategory category = new VehicleCategory();
            category.setName(categoryDef.getName());
            category.setDescription(categoryDef.getDescription());
            VehicleCategory savedCategory = categoryRepository.save(category);
            categories.put(categoryDef.getName(), savedCategory);
            logger.debug("Categoría creada: {} - {}", categoryDef.getName(), categoryDef.getDescription());
        }

        logger.info("Creando modelos de vehículos con marca...");
        List<VehicleModel> allModels = new ArrayList<>();

        for (Map.Entry<String, Map<String, List<String>>> catEntry : vehicleData.entrySet()) {
            VehicleCategory category = categories.get(catEntry.getKey());
            if (category == null) {
                logger.warn("Categoría no encontrada: {}", catEntry.getKey());
                continue;
            }
            for (Map.Entry<String, List<String>> brandEntry : catEntry.getValue().entrySet()) {
                String brand = brandEntry.getKey();
                for (String modelName : brandEntry.getValue()) {
                    VehicleModel model = new VehicleModel();
                    model.setName(modelName.toLowerCase().trim());
                    model.setBrand(brand);
                    model.setCategory(category);
                    allModels.add(model);
                }
            }
        }

        if (!allModels.isEmpty()) {
            modelRepository.saveAll(allModels);
            logger.info("Guardados {} modelos de vehículos en total", allModels.size());
        }
    }

    /**
     * Migra los modelos existentes en BD asignándoles la marca según los datos
     * definidos.
     */
    private void migrateBrands() {
        Map<String, Map<String, List<String>>> vehicleData = getVehicleData();

        Map<String, String> modelToBrand = new HashMap<>();
        for (Map<String, List<String>> brandMap : vehicleData.values()) {
            for (Map.Entry<String, List<String>> brandEntry : brandMap.entrySet()) {
                String brand = brandEntry.getKey();
                for (String modelName : brandEntry.getValue()) {
                    modelToBrand.put(modelName.toLowerCase().trim(), brand);
                }
            }
        }

        List<VehicleModel> allModels = modelRepository.findAll();
        List<VehicleModel> toUpdate = new ArrayList<>();

        for (VehicleModel vm : allModels) {
            if (vm.getBrand() == null || vm.getBrand().isBlank()) {
                String brand = modelToBrand.get(vm.getName());
                if (brand != null) {
                    vm.setBrand(brand);
                    toUpdate.add(vm);
                }
            }
        }

        if (!toUpdate.isEmpty()) {
            modelRepository.saveAll(toUpdate);
            logger.info("✅ Actualizados {} modelos con su marca.", toUpdate.size());
        }
    }

    /**
     * Catálogo completo de vehículos: categoría → (marca → lista de modelos).
     * Para añadir modelos en el futuro, simplemente agrégalos aquí y
     * syncMissingModels() los insertará automáticamente en el siguiente arranque.
     */
    private Map<String, Map<String, List<String>>> getVehicleData() {
        Map<String, Map<String, List<String>>> database = new LinkedHashMap<>();

        // ─────────────────────────────────────────────────────────
        // TURISMOS
        // ─────────────────────────────────────────────────────────
        Map<String, List<String>> turismos = new LinkedHashMap<>();
        turismos.put("Volkswagen",
                Arrays.asList("golf", "polo", "passat", "jetta", "arteon", "vento", "bora", "id.3", "scirocco", "eos"));
        turismos.put("Seat", Arrays.asList("ibiza", "leon", "toledo", "cordoba", "exeo"));
        turismos.put("Cupra", Arrays.asList("cupra born", "cupra leon"));
        turismos.put("Ford",
                Arrays.asList("fiesta", "focus", "mondeo", "fusion", "escort", "sierra", "mustang", "mustang mach-e"));
        turismos.put("Opel", Arrays.asList("corsa", "astra", "insignia", "vectra", "calibra", "omega", "corsa-e"));
        turismos.put("Renault",
                Arrays.asList("clio", "megane", "laguna", "fluence", "talisman", "symbol", "zoe", "megane e-tech"));
        turismos.put("Peugeot", Arrays.asList("105", "205", "106", "206", "207", "208", "306", "307", "308", "406",
                "407", "408", "508", "e-208", "e-308"));
        turismos.put("Citroën", Arrays.asList("c1", "c2", "c3", "c4", "c5", "xsara", "saxo", "ax", "zx", "c5 x"));
        turismos.put("BMW",
                Arrays.asList("serie 1", "serie 2", "serie 3", "serie 4", "serie 5", "serie 6", "serie 7", "serie 8",
                        "serie 1 hatchback", "serie 2 coupe", "serie 2 gran coupe",
                        "serie 4 coupe", "serie 4 gran coupe", "serie 8 coupe",
                        "118", "120", "316", "318", "320", "325", "330", "520", "525", "530",
                        "i4", "i5", "i7", "m2", "m3", "m4", "m5", "m8", "z4"));
        turismos.put("Mercedes", Arrays.asList("clase a", "clase b", "clase c", "clase e", "clase s",
                "clase a sedan", "clase c coupe",
                "cla", "cls", "a180", "a200", "c180", "c200", "c220", "e200", "e220", "e300",
                "eqe", "eqs", "amg a45", "amg c63", "amg e63", "amg gt"));
        turismos.put("Audi", Arrays.asList("a1", "a3", "a4", "a5", "a6", "a7", "a8", "tt",
                "a3 sedan", "a4 sedan", "e-tron gt",
                "rs3", "rs4", "rs5", "rs6", "rs7",
                "s3", "s4", "s5", "s7", "s8"));
        turismos.put("Alfa Romeo", Arrays.asList("giulia", "147", "156", "159", "mito", "giulietta"));
        turismos.put("Toyota", Arrays.asList("yaris", "corolla", "auris", "avensis", "camry", "prius", "bz4x", "gr86",
                "gr supra", "c-hr hybrid"));
        turismos.put("Nissan", Arrays.asList("micra", "almera", "primera", "maxima", "sentra", "leaf"));
        turismos.put("Honda", Arrays.asList("civic", "accord", "jazz", "insight", "e"));
        turismos.put("Hyundai",
                Arrays.asList("i10", "i20", "i30", "i40", "elantra", "sonata", "ioniq", "accent", "ioniq 6", "i20n"));
        turismos.put("Kia", Arrays.asList("rio", "ceed", "cerato", "optima", "ev6", "stinger"));
        turismos.put("Mazda", Arrays.asList("mazda 2", "mazda 3", "mazda 6", "mx5", "mx-5"));
        turismos.put("Skoda", Arrays.asList("fabia", "octavia", "superb", "rapid", "enyaq"));
        turismos.put("Fiat", Arrays.asList("punto", "bravo", "tipo", "linea", "500", "500e"));
        turismos.put("Tesla", Arrays.asList("model 3", "model s"));
        turismos.put("Volvo", Arrays.asList("s40", "s60", "s90", "c40"));
        turismos.put("Porsche", Arrays.asList("panamera", "taycan", "911", "boxster", "cayman"));
        turismos.put("Jaguar", Arrays.asList("xe", "xf", "xj"));
        turismos.put("Lexus", Arrays.asList("is", "es", "gs", "ls", "lc"));
        turismos.put("Mini", Arrays.asList("mini cooper", "mini one", "mini clubman"));
        turismos.put("Dacia", Arrays.asList("sandero", "logan"));
        turismos.put("Suzuki", Arrays.asList("swift", "baleno"));
        turismos.put("DS", Arrays.asList("ds 3", "ds 4", "ds 9"));
        // ── Marcas chinas ──
        turismos.put("MG", Arrays.asList("mg3", "mg4", "mg5", "mg7", "cyberster"));
        turismos.put("BYD", Arrays.asList("seal", "dolphin", "han", "atto 3", "seagull", "sea lion 6"));
        turismos.put("Xpeng", Arrays.asList("p5", "p7"));
        turismos.put("Nio", Arrays.asList("et5", "et7", "et9"));
        turismos.put("Lynk & Co", Arrays.asList("01", "02", "03", "05", "06", "09"));
        database.put("turismo", turismos);

        // ─────────────────────────────────────────────────────────
        // RANCHERAS / FAMILIARES
        // ─────────────────────────────────────────────────────────
        Map<String, List<String>> rancheras = new LinkedHashMap<>();
        rancheras.put("Volkswagen",
                Arrays.asList("golf variant", "golf sportsvan", "passat variant", "passat alltrack"));
        rancheras.put("Seat", Arrays.asList("leon st", "leon x-perience"));
        rancheras.put("Ford", Arrays.asList("focus sw", "focus sportbreak"));
        rancheras.put("Opel", Arrays.asList("astra sports tourer", "astra caravan", "insignia sports tourer"));
        rancheras.put("Renault",
                Arrays.asList("megane estate", "megane grandtour", "laguna estate", "talisman estate"));
        rancheras.put("Peugeot", Arrays.asList("308 sw", "407 sw", "508 sw", "e-308 sw"));
        rancheras.put("Citroën", Arrays.asList("c5 tourer"));
        rancheras.put("BMW", Arrays.asList("serie 3 touring", "serie 5 touring", "serie 3 gran turismo"));
        rancheras.put("Mercedes", Arrays.asList("clase c estate", "clase e estate"));
        rancheras.put("Audi", Arrays.asList("a4 avant", "a6 avant", "a3 sportback", "rs4 avant", "rs6 avant"));
        rancheras.put("Alfa Romeo", Arrays.asList("156 sportwagon", "159 sportwagon"));
        rancheras.put("Toyota", Arrays.asList("corolla touring", "corolla estate"));
        rancheras.put("Skoda", Arrays.asList("octavia combi", "superb combi", "fabia combi"));
        rancheras.put("Volvo", Arrays.asList("v40", "v60", "v90"));
        rancheras.put("Jaguar", Arrays.asList("xf sportbrake"));
        rancheras.put("Mini", Arrays.asList("mini countryman"));
        database.put("ranchera", rancheras);

        // ─────────────────────────────────────────────────────────
        // MONOVOLÚMENES Y SUV COMPACTOS
        // ─────────────────────────────────────────────────────────
        Map<String, List<String>> monovolumenes = new LinkedHashMap<>();
        monovolumenes.put("Volkswagen",
                Arrays.asList("golf sportsvan", "touran", "sharan", "t-cross", "t-roc", "tiguan", "id.4"));
        monovolumenes.put("Seat", Arrays.asList("altea xl", "alhambra", "arona", "ateca", "tarraco"));
        monovolumenes.put("Cupra", Arrays.asList("cupra ateca", "cupra formentor", "cupra terramar"));
        monovolumenes.put("Ford", Arrays.asList("b-max", "c-max", "s-max", "galaxy", "kuga", "ecosport", "puma"));
        monovolumenes.put("Opel", Arrays.asList("zafira", "meriva", "mokka", "crossland", "grandland"));
        monovolumenes.put("Renault",
                Arrays.asList("scenic", "grand scenic", "espace", "modus", "captur", "kadjar", "arkana", "koleos"));
        monovolumenes.put("Peugeot", Arrays.asList("1007", "807", "5008", "c3 picasso", "c4 picasso",
                "grand c4 picasso", "c8", "xsara picasso", "3008", "2008", "3008 hybrid", "e-3008"));
        monovolumenes.put("Citroën", Arrays.asList("c3 aircross", "c4 aircross", "c5 aircross"));
        monovolumenes.put("BMW",
                Arrays.asList("serie 2 active tourer", "serie 2 gran tourer", "i3", "x1", "x2", "x3", "ix1", "ix3"));
        monovolumenes.put("Mercedes", Arrays.asList("clase b", "clase r", "clase v", "viano", "vaneo", "gla", "glb",
                "glc", "glc coupe", "eqa", "eqb", "eqc"));
        monovolumenes.put("Audi",
                Arrays.asList("a2", "q2", "q3", "q5", "q3 sportback", "q4 e-tron", "q5 sportback", "sq2", "sq5"));
        monovolumenes.put("Alfa Romeo", Arrays.asList("stelvio", "tonale"));
        monovolumenes.put("Toyota", Arrays.asList("verso", "previa", "yaris verso", "corolla verso", "avensis verso",
                "proace verso", "iq", "c-hr", "rav4"));
        monovolumenes.put("Nissan", Arrays.asList("note", "cube", "tiida", "almera tino", "primera traveller", "serena",
                "juke", "qashqai", "x-trail", "murano", "ariya"));
        monovolumenes.put("Honda", Arrays.asList("fr-v", "stream", "odyssey", "shuttle", "cr-v", "hr-v"));
        monovolumenes.put("Hyundai",
                Arrays.asList("matrix", "trajet", "h1", "ix20", "tucson", "kona", "nexo", "ioniq 5", "bayon"));
        monovolumenes.put("Kia", Arrays.asList("carens", "carnival", "sedona", "venga", "soul", "sportage", "sorento",
                "niro", "stonic", "xceed", "ev3", "seltos"));
        monovolumenes.put("Mazda", Arrays.asList("premacy", "mpv", "biante", "cx-3", "cx-5", "cx-7", "cx-30", "cx-60"));
        monovolumenes.put("Skoda", Arrays.asList("roomster", "yeti", "kamiq", "karoq", "kodiaq"));
        monovolumenes.put("Fiat",
                Arrays.asList("multipla", "ulysse", "idea", "doblo", "qubo", "500l", "phedra", "500x", "600"));
        monovolumenes.put("Tesla", Arrays.asList("model y"));
        monovolumenes.put("Volvo", Arrays.asList("xc40", "xc60", "ec40"));
        monovolumenes.put("Jeep", Arrays.asList("renegade", "compass"));
        monovolumenes.put("Suzuki", Arrays.asList("vitara", "s-cross", "across"));
        monovolumenes.put("Dacia", Arrays.asList("duster", "spring", "jogger"));
        monovolumenes.put("Mini", Arrays.asList("mini paceman", "mini aceman"));
        monovolumenes.put("Lexus", Arrays.asList("ux", "nx"));
        monovolumenes.put("DS", Arrays.asList("ds 3 crossback", "ds 4 crossback", "ds 7 crossback"));
        // ── Marcas chinas ──
        monovolumenes.put("MG", Arrays.asList("mg zs", "mg hs", "mg one", "ehs", "marvel r"));
        monovolumenes.put("BYD", Arrays.asList("yuan plus", "song plus", "tang", "atto 3"));
        monovolumenes.put("Jaecoo", Arrays.asList("jaecoo 7", "jaecoo 8"));
        monovolumenes.put("Omoda", Arrays.asList("omoda 5", "omoda 5 ev"));
        monovolumenes.put("Chery", Arrays.asList("tiggo 4", "tiggo 7", "tiggo 8"));
        monovolumenes.put("Geely", Arrays.asList("coolray", "okavango"));
        monovolumenes.put("Maxus", Arrays.asList("mifa 9", "euniq 5", "euniq 6"));
        monovolumenes.put("Xpeng", Arrays.asList("g3", "g6", "g9"));
        monovolumenes.put("Nio", Arrays.asList("es6", "es8", "el6", "el8"));
        monovolumenes.put("Deepal", Arrays.asList("s07"));
        monovolumenes.put("Leapmotor", Arrays.asList("c10", "t03"));
        database.put("monovolumen", monovolumenes);

        // ─────────────────────────────────────────────────────────
        // TODOTERRENOS GRANDES
        // ─────────────────────────────────────────────────────────
        Map<String, List<String>> todoterrenos = new LinkedHashMap<>();
        todoterrenos.put("Volkswagen", Arrays.asList("touareg", "id.6"));
        todoterrenos.put("Porsche", Arrays.asList("cayenne", "cayenne coupe", "macan"));
        todoterrenos.put("Seat", Arrays.asList("tarraco"));
        todoterrenos.put("Ford", Arrays.asList("edge", "explorer", "bronco"));
        todoterrenos.put("Opel", Arrays.asList("antara", "grandland"));
        todoterrenos.put("Renault", Arrays.asList("koleos"));
        todoterrenos.put("Peugeot", Arrays.asList("4008", "5008"));
        todoterrenos.put("Citroën", Arrays.asList("c5 aircross"));
        todoterrenos.put("BMW", Arrays.asList("x3", "x4", "x5", "x6", "x7", "ix", "xm"));
        todoterrenos.put("Mercedes", Arrays.asList("gle", "gls", "gle coupe", "g class", "eqe suv", "eqs suv"));
        todoterrenos.put("Audi", Arrays.asList("q7", "q8", "e-tron", "sq7", "sq8", "rsq8"));
        todoterrenos.put("Toyota", Arrays.asList("rav4", "highlander", "land cruiser", "sequoia"));
        todoterrenos.put("Nissan", Arrays.asList("x-trail", "pathfinder", "patrol"));
        todoterrenos.put("Honda", Arrays.asList("cr-v", "crv", "hr-v", "hrv", "pilot"));
        todoterrenos.put("Hyundai", Arrays.asList("santa fe", "ioniq 7"));
        todoterrenos.put("Kia", Arrays.asList("sorento", "ev9"));
        todoterrenos.put("Mazda", Arrays.asList("cx-5", "cx-7", "cx-9", "cx-90"));
        todoterrenos.put("Skoda", Arrays.asList("kodiaq", "karoq"));
        todoterrenos.put("Land Rover", Arrays.asList("range rover", "discovery", "defender", "freelander",
                "range rover sport", "range rover evoque", "range rover velar", "discovery sport"));
        todoterrenos.put("Jaguar", Arrays.asList("f-pace", "e-pace", "i-pace"));
        todoterrenos.put("Volvo", Arrays.asList("xc90", "ex90"));
        todoterrenos.put("Tesla", Arrays.asList("model x"));
        todoterrenos.put("Jeep", Arrays.asList("grand cherokee", "wrangler", "gladiator", "commander"));
        todoterrenos.put("Lexus", Arrays.asList("rx", "lx", "tx", "gx"));
        // ── Marcas chinas ──
        todoterrenos.put("MG", Arrays.asList("mg gloster", "mg rh8"));
        todoterrenos.put("BYD", Arrays.asList("yangwang u8", "tang ev", "sea lion 9"));
        todoterrenos.put("Jaecoo", Arrays.asList("jaecoo 9"));
        todoterrenos.put("Chery", Arrays.asList("tiggo 9"));
        todoterrenos.put("Xpeng", Arrays.asList("g9"));
        database.put("todoterreno", todoterrenos);

        // ─────────────────────────────────────────────────────────
        // FURGONETAS PEQUEÑAS
        // ─────────────────────────────────────────────────────────
        Map<String, List<String>> furgonetasPequenas = new LinkedHashMap<>();
        furgonetasPequenas.put("Volkswagen", Arrays.asList("caddy"));
        furgonetasPequenas.put("Opel", Arrays.asList("combo"));
        furgonetasPequenas.put("Citroën", Arrays.asList("berlingo", "nemo", "bipper"));
        furgonetasPequenas.put("Peugeot", Arrays.asList("partner", "bipper", "rifter"));
        furgonetasPequenas.put("Renault", Arrays.asList("kangoo", "kangoo e-tech"));
        furgonetasPequenas.put("Fiat", Arrays.asList("doblo", "fiorino"));
        furgonetasPequenas.put("Mercedes", Arrays.asList("citan", "t-class"));
        furgonetasPequenas.put("Ford", Arrays.asList("connect", "courier"));
        furgonetasPequenas.put("Nissan", Arrays.asList("nv200"));
        furgonetasPequenas.put("Toyota", Arrays.asList("proace city"));
        database.put("furgoneta_pequena", furgonetasPequenas);

        // ─────────────────────────────────────────────────────────
        // FURGONETAS GRANDES
        // ─────────────────────────────────────────────────────────
        Map<String, List<String>> furgonetasGrandes = new LinkedHashMap<>();
        furgonetasGrandes.put("Volkswagen", Arrays.asList("crafter", "lt", "transporter"));
        furgonetasGrandes.put("Mercedes", Arrays.asList("sprinter", "vito", "viano", "marco polo", "california"));
        furgonetasGrandes.put("Ford", Arrays.asList("transit", "transit custom"));
        furgonetasGrandes.put("Opel", Arrays.asList("movano", "vivaro"));
        furgonetasGrandes.put("Renault", Arrays.asList("master", "trafic"));
        furgonetasGrandes.put("Fiat", Arrays.asList("ducato", "scudo"));
        furgonetasGrandes.put("Peugeot", Arrays.asList("expert", "boxer", "jumper"));
        furgonetasGrandes.put("Nissan", Arrays.asList("primastar", "nv400"));
        furgonetasGrandes.put("Iveco", Arrays.asList("daily"));
        furgonetasGrandes.put("Toyota", Arrays.asList("proace", "hiace"));
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

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public int getOrder() {
            return order;
        }
    }
}
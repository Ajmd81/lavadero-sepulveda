package com.lavaderosepulveda.app.repository;

import com.lavaderosepulveda.app.model.PlantillaFacturaConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlantillaFacturaConfigRepository extends JpaRepository<PlantillaFacturaConfig, Long> {
    // Solo habrá una configuración, siempre con ID = 1
}

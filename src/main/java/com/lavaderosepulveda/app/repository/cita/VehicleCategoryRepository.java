package com.lavaderosepulveda.app.repository.cita;

import com.lavaderosepulveda.app.model.cita.VehicleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory, Long> {
    Optional<VehicleCategory> findByName(String name);
}

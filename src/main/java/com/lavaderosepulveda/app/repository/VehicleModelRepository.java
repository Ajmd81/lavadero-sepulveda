package com.lavaderosepulveda.app.repository;

import com.lavaderosepulveda.app.model.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {

    /**
     * Busca un modelo de vehículo cuyo nombre coincida o contenga el término de búsqueda normalizado.
     * La normalización en la base de datos se hace reemplazando caracteres no alfanuméricos y espacios.
     * Ejemplo: "Serie 3" se buscaría como "serie3".
     */
    @Query("SELECT vm FROM VehicleModel vm WHERE LOWER(REPLACE(REPLACE(vm.name, ' ', ''), '-', '')) LIKE LOWER(CONCAT('%', :name, '%'))")
    Optional<VehicleModel> findFirstByNameContainingNormalized(@Param("name") String name);

}

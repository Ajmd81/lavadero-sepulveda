package com.lavaderosepulveda.app.repository;

import com.lavaderosepulveda.app.model.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /**
     * Devuelve todas las marcas distintas almacenadas, ordenadas alfabéticamente.
     * Excluye nulos para retrocompatibilidad.
     */
    @Query("SELECT DISTINCT vm.brand FROM VehicleModel vm WHERE vm.brand IS NOT NULL ORDER BY vm.brand ASC")
    List<String> findAllDistinctBrands();

    /**
     * Devuelve todos los modelos de una marca concreta, ordenados alfabéticamente.
     */
    @Query("SELECT vm FROM VehicleModel vm WHERE LOWER(vm.brand) = LOWER(:brand) ORDER BY vm.name ASC")
    List<VehicleModel> findByBrandIgnoreCase(@Param("brand") String brand);

}


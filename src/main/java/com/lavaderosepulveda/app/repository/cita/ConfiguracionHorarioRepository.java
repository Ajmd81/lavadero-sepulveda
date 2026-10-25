package com.lavaderosepulveda.app.repository.cita;

import com.lavaderosepulveda.app.model.cita.ConfiguracionHorario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para ConfiguracionHorario
 * Patrón singleton: siempre hay un único registro con ID = 1
 */
@Repository
public interface ConfiguracionHorarioRepository extends JpaRepository<ConfiguracionHorario, Long> {

    /**
     * Obtiene la configuración única (ID = 1)
     */
    Optional<ConfiguracionHorario> findById(Long id);
}
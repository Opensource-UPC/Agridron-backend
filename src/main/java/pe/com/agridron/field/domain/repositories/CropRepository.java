package pe.com.agridron.field.domain.repositories;

import pe.com.agridron.field.domain.model.aggregates.Crop;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for the read-only {@link Crop} catalog.
 */
public interface CropRepository {

    Optional<Crop> findById(Long id);

    List<Crop> findAll();

    boolean existsById(Long id);
}

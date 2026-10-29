package pe.com.agridron.field.domain.repositories;

import pe.com.agridron.field.domain.model.aggregates.FumigationArea;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for the {@link FumigationArea} aggregate.
 */
public interface FumigationAreaRepository {

    FumigationArea save(FumigationArea area);

    Optional<FumigationArea> findById(Long id);

    List<FumigationArea> findByParcelId(Long parcelId);

    boolean existsById(Long id);
}

package pe.com.agridron.field.domain.repositories;

import pe.com.agridron.field.domain.model.aggregates.Crop;
import pe.com.agridron.field.domain.model.queries.PageResult;
import pe.com.agridron.field.domain.model.queries.Pagination;
import pe.com.agridron.field.domain.model.queries.SortOrder;

import java.util.Optional;

/**
 * Repository port for the read-only {@link Crop} catalog.
 */
public interface CropRepository {

    Optional<Crop> findById(Long id);

    PageResult<Crop> findAll(Pagination pagination, SortOrder sortOrder);

    boolean existsById(Long id);
}

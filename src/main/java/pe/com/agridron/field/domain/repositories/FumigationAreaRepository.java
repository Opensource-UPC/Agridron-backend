package pe.com.agridron.field.domain.repositories;

import pe.com.agridron.field.domain.model.aggregates.FumigationArea;
import pe.com.agridron.field.domain.model.queries.PageResult;
import pe.com.agridron.field.domain.model.queries.Pagination;
import pe.com.agridron.field.domain.model.queries.SortOrder;

import java.util.Optional;

/**
 * Repository port for the {@link FumigationArea} aggregate.
 */
public interface FumigationAreaRepository {

    FumigationArea save(FumigationArea area);

    Optional<FumigationArea> findById(Long id);

    PageResult<FumigationArea> findByParcelId(
            Long parcelId, Pagination pagination, SortOrder sortOrder);

    boolean existsById(Long id);
}

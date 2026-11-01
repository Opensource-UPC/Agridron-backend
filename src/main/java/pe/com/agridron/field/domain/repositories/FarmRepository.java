package pe.com.agridron.field.domain.repositories;

import pe.com.agridron.field.domain.model.aggregates.Farm;
import pe.com.agridron.field.domain.model.queries.PageResult;
import pe.com.agridron.field.domain.model.queries.Pagination;
import pe.com.agridron.field.domain.model.queries.SortOrder;

import java.util.Optional;

/**
 * Repository port for the {@link Farm} aggregate.
 */
public interface FarmRepository {

    Farm save(Farm farm);

    Optional<Farm> findById(Long id);

    PageResult<Farm> findAllByOwnerId(Long ownerId, Pagination pagination, SortOrder sortOrder);

    boolean existsByOwnerIdAndName(Long ownerId, String name);

    boolean existsByOwnerIdAndNameAndIdNot(Long ownerId, String name, Long id);
}

package pe.com.agridron.field.application.queryservices;

import pe.com.agridron.field.domain.model.aggregates.Farm;
import pe.com.agridron.field.domain.model.queries.GetFarmByIdQuery;
import pe.com.agridron.field.domain.model.queries.GetFarmsByOwnerQuery;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

/**
 * Application service that resolves farm read queries.
 */
public interface FarmQueryService {

    /**
     * Retrieves a farm by its identifier.
     *
     * @param query the query
     * @return the farm or a {@code FARM_NOT_FOUND} error
     */
    Result<Farm, ApplicationError> handle(GetFarmByIdQuery query);

    /**
     * Lists the farms owned by a user, ordered by name.
     *
     * @param query the query
     * @return the matching farms, possibly empty
     */
    List<Farm> handle(GetFarmsByOwnerQuery query);
}

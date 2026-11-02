package pe.com.agridron.field.application.queryservices;

import pe.com.agridron.field.domain.model.aggregates.FumigationArea;
import pe.com.agridron.field.domain.model.queries.GetFumigationAreaByIdQuery;
import pe.com.agridron.field.domain.model.queries.GetFumigationAreasByParcelQuery;
import pe.com.agridron.field.domain.model.queries.PageResult;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

/**
 * Application service that resolves fumigation area read queries.
 */
public interface FumigationAreaQueryService {

    /**
     * Retrieves a fumigation area by its identifier.
     *
     * @param query the query
     * @return the area or a {@code FUMIGATION_AREA_NOT_FOUND} error
     */
    Result<FumigationArea, ApplicationError> handle(GetFumigationAreaByIdQuery query);

    /**
     * Lists the fumigation areas of a parcel.
     *
     * @param query the query
     * @return the matching areas and pagination metadata,
     *         or a {@code PARCEL_NOT_FOUND} error
     */
    Result<PageResult<FumigationArea>, ApplicationError> handle(
            GetFumigationAreasByParcelQuery query);
}

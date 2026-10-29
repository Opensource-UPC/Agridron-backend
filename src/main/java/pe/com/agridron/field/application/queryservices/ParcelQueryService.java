package pe.com.agridron.field.application.queryservices;

import pe.com.agridron.field.domain.model.aggregates.Parcel;
import pe.com.agridron.field.domain.model.queries.GetParcelByIdQuery;
import pe.com.agridron.field.domain.model.queries.ParcelSearchCriteria;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

/**
 * Application service that resolves parcel read queries.
 */
public interface ParcelQueryService {

    /**
     * Retrieves a parcel by its identifier.
     *
     * @param query the query
     * @return the parcel or a {@code PARCEL_NOT_FOUND} error
     */
    Result<Parcel, ApplicationError> handle(GetParcelByIdQuery query);

    /**
     * Searches parcels combining the non-null criteria with AND.
     *
     * @param criteria the search filters
     * @return the matching parcels ordered by name and id, possibly empty
     */
    List<Parcel> handle(ParcelSearchCriteria criteria);
}

package pe.com.agridron.field.application.queryservices;

import pe.com.agridron.field.domain.model.aggregates.Crop;
import pe.com.agridron.field.domain.model.queries.GetAllCropsQuery;
import pe.com.agridron.field.domain.model.queries.GetCropByIdQuery;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

/**
 * Application service that resolves read queries against the crop catalog.
 */
public interface CropQueryService {

    /**
     * Retrieves a crop by its identifier.
     *
     * @param query the query
     * @return the crop or a {@code CROP_NOT_FOUND} error
     */
    Result<Crop, ApplicationError> handle(GetCropByIdQuery query);

    /**
     * Lists every crop of the catalog.
     *
     * @param query the query
     * @return the catalog, possibly empty
     */
    List<Crop> handle(GetAllCropsQuery query);
}

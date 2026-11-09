package pe.com.agridron.inventory.application.queryservices;

import pe.com.agridron.inventory.domain.model.aggregates.Nozzle;
import pe.com.agridron.inventory.domain.model.queries.GetAllNozzlesQuery;
import pe.com.agridron.inventory.domain.model.queries.GetNozzleByIdQuery;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

/**
 * Consultas sobre boquillas.
 */
public interface NozzleQueryService {

    /** Devuelve la boquilla, o {@code NOZZLE_NOT_FOUND} si no existe. */
    Result<Nozzle, ApplicationError> handle(GetNozzleByIdQuery query);

    List<Nozzle> handle(GetAllNozzlesQuery query);
}
package pe.com.agridron.inventory.domain.model.queries;


/**
 * Consulta para obtener una boquilla por su id.
 *
 * @param nozzleId identificador, siempre positivo
 */
public record GetNozzleByIdQuery(Long nozzleId) {

    public GetNozzleByIdQuery {
        if (nozzleId == null || nozzleId <= 0) {
            throw new IllegalArgumentException("nozzleId must be positive");
        }
    }
}
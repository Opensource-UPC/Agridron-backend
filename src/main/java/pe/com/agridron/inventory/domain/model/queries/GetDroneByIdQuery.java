package pe.com.agridron.inventory.domain.model.queries;

/**
 * Consulta para obtener un dron por su id.
 *
 * @param droneId identificador, siempre positivo
 */
public record GetDroneByIdQuery(Long droneId) {

    public GetDroneByIdQuery {
        if (droneId == null || droneId <= 0) {
            throw new IllegalArgumentException("droneId must be positive");
        }
    }
}
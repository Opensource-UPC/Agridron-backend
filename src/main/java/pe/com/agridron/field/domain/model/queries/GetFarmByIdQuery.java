package pe.com.agridron.field.domain.model.queries;

/**
 * Query that requests a farm by its identifier.
 *
 * @param farmId identifier of the farm
 */
public record GetFarmByIdQuery(Long farmId) {
}

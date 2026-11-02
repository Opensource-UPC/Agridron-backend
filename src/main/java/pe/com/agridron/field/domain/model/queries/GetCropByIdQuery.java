package pe.com.agridron.field.domain.model.queries;

/**
 * Query that requests a crop from the read-only catalog.
 *
 * @param cropId identifier of the crop
 */
public record GetCropByIdQuery(Long cropId) {
}

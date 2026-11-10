package pe.com.agridron.field.domain.model.queries;

/**
 * Query that requests a fumigation area by its identifier.
 *
 * @param fumigationAreaId identifier of the fumigation area
 */
public record GetFumigationAreaByIdQuery(Long fumigationAreaId) {
}

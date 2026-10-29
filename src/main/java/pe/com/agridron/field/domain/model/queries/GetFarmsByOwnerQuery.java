package pe.com.agridron.field.domain.model.queries;

/**
 * Query that requests every farm owned by a user.
 *
 * @param ownerId identifier of the owner in the IAM bounded context
 */
public record GetFarmsByOwnerQuery(Long ownerId) {
}

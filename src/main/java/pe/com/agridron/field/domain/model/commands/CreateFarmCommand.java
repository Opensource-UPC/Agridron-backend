package pe.com.agridron.field.domain.model.commands;

/**
 * Command that requests the registration of a new farm.
 *
 * @param ownerId    identifier of the owner in the IAM bounded context
 * @param name       farm name, unique per owner
 * @param location   human-readable location
 * @param department Peruvian department where the farm is located
 */
public record CreateFarmCommand(
        Long ownerId,
        String name,
        String location,
        String department) {
}

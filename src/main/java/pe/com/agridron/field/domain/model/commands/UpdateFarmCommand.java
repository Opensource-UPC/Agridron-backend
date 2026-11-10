package pe.com.agridron.field.domain.model.commands;

/**
 * Command that requests an update of the descriptive data of a farm.
 *
 * <p>The owner cannot be changed; it is taken from the persisted aggregate.</p>
 *
 * @param farmId     identifier of the farm
 * @param name       new farm name, unique per owner
 * @param location   new human-readable location
 * @param department new Peruvian department
 */
public record UpdateFarmCommand(
        Long farmId,
        String name,
        String location,
        String department) {
}


package pe.com.agridron.inventory.domain.model.commands;

/**
 * Comando para registrar un nuevo tipo de boquilla en el catálogo (DR-004).
 *
 * @param type         tipo de boquilla (por ejemplo, flat-fan o hollow-cone)
 * @param diameterMm   diámetro en milímetros
 * @param goodUnits    unidades iniciales en buen estado
 * @param wornUnits    unidades iniciales desgastadas
 * @param damagedUnits unidades iniciales dañadas
 */
public record CreateNozzleCommand(
        String type,
        Double diameterMm,
        int goodUnits,
        int wornUnits,
        int damagedUnits) {

    public CreateNozzleCommand {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type is required");
        }
        if (diameterMm == null || diameterMm <= 0) {
            throw new IllegalArgumentException("diameterMm must be positive");
        }
        if (goodUnits < 0 || wornUnits < 0 || damagedUnits < 0) {
            throw new IllegalArgumentException("units must not be negative");
        }
    }
}
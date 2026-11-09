

package pe.com.agridron.inventory.domain.model.valueobjects;

/**
 * Referencia a la boquilla instalada en un dron.
 *
 * <p>El dron no guarda la boquilla completa (es otro agregado), solo su identificador.</p>
 *
 * @param id identificador de la boquilla, siempre positivo
 */
public record NozzleId(Long id) {

    public NozzleId {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("nozzleId must be positive");
        }
    }
}
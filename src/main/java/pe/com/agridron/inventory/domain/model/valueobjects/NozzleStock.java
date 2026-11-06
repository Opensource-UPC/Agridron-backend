package pe.com.agridron.inventory.domain.model.valueobjects;

/**
 * Stock en bodega de un tipo de boquilla, separado por estado de las unidades.
 *
 * <p>Es inmutable: cada operación devuelve un nuevo {@code NozzleStock}. Ningún contador
 * puede quedar negativo.</p>
 *
 * @param goodUnits    unidades en buen estado (las únicas que se pueden instalar)
 * @param wornUnits    unidades desgastadas
 * @param damagedUnits unidades dañadas
 */
public record NozzleStock(int goodUnits, int wornUnits, int damagedUnits) {

    public NozzleStock {
        if (goodUnits < 0 || wornUnits < 0 || damagedUnits < 0) {
            throw new IllegalArgumentException("Nozzle stock units must not be negative");
        }
    }

    /**
     * Stock vacío (0 unidades en todos los estados).
     */
    public static NozzleStock empty() {
        return new NozzleStock(0, 0, 0);
    }

    /**
     * Total de unidades en bodega, sin importar su estado.
     */
    public int total() {
        return goodUnits + wornUnits + damagedUnits;
    }

    /**
     * Indica si hay al menos una unidad buena para instalar en un dron.
     */
    public boolean hasInstallableUnits() {
        return goodUnits > 0;
    }

    /**
     * Agrega unidades nuevas (en buen estado).
     */
    public NozzleStock restock(int units) {
        requirePositive(units);
        return new NozzleStock(goodUnits + units, wornUnits, damagedUnits);
    }

    /**
     * Pasa unidades buenas a desgastadas.
     */
    public NozzleStock markAsWorn(int units) {
        requirePositive(units);
        requireEnoughGoodUnits(units);
        return new NozzleStock(goodUnits - units, wornUnits + units, damagedUnits);
    }

    /**
     * Pasa unidades buenas a dañadas.
     */
    public NozzleStock markAsDamaged(int units) {
        requirePositive(units);
        requireEnoughGoodUnits(units);
        return new NozzleStock(goodUnits - units, wornUnits, damagedUnits + units);
    }

    /**
     * Retira una unidad buena de bodega para instalarla en un dron.
     */
    public NozzleStock takeOne() {
        requireEnoughGoodUnits(1);
        return new NozzleStock(goodUnits - 1, wornUnits, damagedUnits);
    }

    private static void requirePositive(int units) {
        if (units <= 0) {
            throw new IllegalArgumentException("units must be positive");
        }
    }

    private void requireEnoughGoodUnits(int units) {
        if (units > goodUnits) {
            throw new IllegalStateException(
                    "Not enough good units: requested %d, available %d".formatted(units, goodUnits));
        }
    }
}

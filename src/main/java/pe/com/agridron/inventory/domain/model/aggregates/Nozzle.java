package pe.com.agridron.inventory.domain.model.aggregates;

import lombok.Getter;
import pe.com.agridron.inventory.domain.model.valueobjects.NozzleStock;
import pe.com.agridron.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

/**
 * Tipo de boquilla del catálogo de inventario, con su stock en bodega.
 *
 * <p>Agregado de dominio sin anotaciones JPA (ver ADR-003). Las reglas de negocio se
 * comprueban en el command service antes de llamar a estos métodos; las validaciones de aquí
 * son la última barrera para no dejar el agregado en un estado inválido.</p>
 */
@Getter
public class Nozzle extends AbstractDomainAggregateRoot<Nozzle> {

    private static final int TYPE_MAX_LENGTH = 60;

    private Long id;
    private final String type;
    private final Double diameterMm;
    private NozzleStock stock;

    private Nozzle(String type, Double diameterMm, NozzleStock stock) {
        this.type = requireType(type);
        this.diameterMm = requirePositiveDiameter(diameterMm);
        this.stock = stock != null ? stock : NozzleStock.empty();
    }

    /**
     * Registra un nuevo tipo de boquilla en el catálogo.
     */
    public static Nozzle register(String type, Double diameterMm, NozzleStock initialStock) {
        return new Nozzle(type, diameterMm, initialStock);
    }

    /**
     * Reconstruye una boquilla existente a partir de lo guardado en base de datos.
     */
    public static Nozzle reconstitute(Long id, String type, Double diameterMm, NozzleStock stock) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        var nozzle = new Nozzle(type, diameterMm, stock);
        nozzle.id = id;
        return nozzle;
    }

    /** Agrega unidades nuevas en buen estado. */
    public void restock(int units) {
        this.stock = stock.restock(units);
    }

    /** Pasa unidades buenas a desgastadas. */
    public void markAsWorn(int units) {
        this.stock = stock.markAsWorn(units);
    }

    /** Pasa unidades buenas a dañadas. */
    public void markAsDamaged(int units) {
        this.stock = stock.markAsDamaged(units);
    }

    /** Indica si hay al menos una unidad buena para instalar en un dron. */
    public boolean hasInstallableUnits() {
        return stock.hasInstallableUnits();
    }

    /** Retira una unidad buena para instalarla en un dron. */
    public void takeOneUnit() {
        this.stock = stock.takeOne();
    }

    private static String requireType(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type is required");
        }
        var normalized = type.trim();
        if (normalized.length() > TYPE_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "type must have at most %d characters".formatted(TYPE_MAX_LENGTH));
        }
        return normalized;
    }

    private static Double requirePositiveDiameter(Double diameterMm) {
        if (diameterMm == null || diameterMm <= 0) {
            throw new IllegalArgumentException("diameterMm must be positive");
        }
        return diameterMm;
    }
}
package pe.com.agridron.field.domain.model.aggregates;

import lombok.Getter;
import pe.com.agridron.field.domain.model.services.DomainValidation;
import pe.com.agridron.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

/**
 * Crop aggregate root of the catalog.
 *
 * <p>The catalog is seeded by Flyway and exposed as read-only through the API.</p>
 */
@Getter
public class Crop extends AbstractDomainAggregateRoot<Crop> {

    private static final int TEXT_MAX_LENGTH = 120;

    private Long id;
    private final String name;
    private final String variety;

    private Crop(String name, String variety) {
        this.name = DomainValidation.requireText(name, "name", TEXT_MAX_LENGTH);
        this.variety = DomainValidation.requireText(variety, "variety", TEXT_MAX_LENGTH);
    }

    /**
     * Creates a new crop catalog entry.
     */
    public static Crop register(String name, String variety) {
        return new Crop(name, variety);
    }

    /**
     * Reconstitutes an existing crop from persistence state.
     */
    public static Crop reconstitute(Long id, String name, String variety) {
        var crop = new Crop(name, variety);
        crop.id = DomainValidation.requirePositive(id, "id");
        return crop;
    }
}

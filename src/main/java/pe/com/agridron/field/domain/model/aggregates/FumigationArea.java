package pe.com.agridron.field.domain.model.aggregates;

import lombok.Getter;
import pe.com.agridron.field.domain.events.FumigationAreaDelimitedEvent;
import pe.com.agridron.field.domain.exceptions.FumigationAreaOutsideParcelException;
import pe.com.agridron.field.domain.model.services.DomainValidation;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.util.Objects;

/**
 * Fumigation area aggregate root.
 *
 * <p>Represents a polygon inside a parcel where a spraying mission is planned. The area
 * cannot exceed the parcel boundary.</p>
 */
@Getter
public class FumigationArea extends AbstractDomainAggregateRoot<FumigationArea> {

    private Long id;
    private final Long parcelId;
    private FieldGeometry geometry;

    private FumigationArea(Long parcelId, FieldGeometry geometry) {
        this.parcelId = DomainValidation.requirePositive(parcelId, "parcelId");
        this.geometry = Objects.requireNonNull(geometry, "geometry is required");
    }

    /**
     * Delimits a new fumigation area inside the given parcel geometry.
     *
     * @param parcelId       the owning parcel id
     * @param parcelGeometry the parcel boundary
     * @param geometry       the fumigation area boundary
     * @return the new area
     * @throws FumigationAreaOutsideParcelException when the area is outside or larger than the parcel
     */
    public static FumigationArea delimit(
            Long parcelId,
            FieldGeometry parcelGeometry,
            FieldGeometry geometry) {
        validateContainment(parcelGeometry, geometry);
        return new FumigationArea(parcelId, geometry);
    }

    /**
     * Reconstitutes an existing fumigation area from persistence state.
     */
    public static FumigationArea reconstitute(Long id, Long parcelId, FieldGeometry geometry) {
        var area = new FumigationArea(parcelId, geometry);
        area.id = DomainValidation.requirePositive(id, "id");
        return area;
    }

    /**
     * Registers the creation event. Called by the repository adapter after the identity is assigned.
     */
    public void onCreated() {
        registerDomainEvent(FumigationAreaDelimitedEvent.from(this));
    }

    /**
     * Redefines the fumigation area boundary and registers the delimited event again.
     */
    public void redefine(FieldGeometry parcelGeometry, FieldGeometry geometry) {
        validateContainment(parcelGeometry, geometry);
        this.geometry = Objects.requireNonNull(geometry, "geometry is required");
        registerDomainEvent(FumigationAreaDelimitedEvent.from(this));
    }

    private static void validateContainment(FieldGeometry parcelGeometry, FieldGeometry geometry) {
        Objects.requireNonNull(parcelGeometry, "parcelGeometry is required");
        Objects.requireNonNull(geometry, "geometry is required");
        if (!parcelGeometry.covers(geometry)) {
            throw new FumigationAreaOutsideParcelException(
                    "fumigation area must be fully contained in its parcel");
        }
        if (geometry.areaHectares().compareTo(parcelGeometry.areaHectares()) > 0) {
            throw new FumigationAreaOutsideParcelException(
                    "fumigation area must not exceed the parcel area");
        }
    }
}

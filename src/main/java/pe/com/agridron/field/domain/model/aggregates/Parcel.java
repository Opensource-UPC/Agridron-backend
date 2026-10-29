package pe.com.agridron.field.domain.model.aggregates;

import lombok.Getter;
import pe.com.agridron.field.domain.events.ParcelCropInformationRegisteredEvent;
import pe.com.agridron.field.domain.events.ParcelGeometryUpdatedEvent;
import pe.com.agridron.field.domain.events.ParcelRegisteredEvent;
import pe.com.agridron.field.domain.events.ParcelStatusChangedEvent;
import pe.com.agridron.field.domain.model.services.DomainValidation;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.domain.model.valueobjects.PhenologicalStage;
import pe.com.agridron.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.util.Objects;

/**
 * Parcel aggregate root.
 *
 * <p>Owns the boundary and area of a parcel, its operational status and the optional
 * crop information. The farm and crop are referenced by id; they are separate aggregates.</p>
 */
@Getter
public class Parcel extends AbstractDomainAggregateRoot<Parcel> {

    private static final int NAME_MAX_LENGTH = 120;

    private Long id;
    private final Long farmId;
    private Long cropId;
    private String name;
    private FieldGeometry geometry;
    private ParcelStatus status;
    private PhenologicalStage phenologicalStage;

    private Parcel(
            Long farmId,
            String name,
            FieldGeometry geometry,
            Long cropId,
            PhenologicalStage phenologicalStage) {
        this.farmId = DomainValidation.requirePositive(farmId, "farmId");
        this.name = DomainValidation.requireText(name, "name", NAME_MAX_LENGTH);
        this.geometry = Objects.requireNonNull(geometry, "geometry is required");
        this.status = ParcelStatus.ACTIVE;
        applyCropInformation(cropId, phenologicalStage);
    }

    /**
     * Creates a new active parcel. Creation events are registered by {@link #onCreated()}
     * once the persistence adapter has assigned the identity.
     */
    public static Parcel create(
            Long farmId,
            String name,
            FieldGeometry geometry,
            Long cropId,
            PhenologicalStage phenologicalStage) {
        return new Parcel(farmId, name, geometry, cropId, phenologicalStage);
    }

    /**
     * Reconstitutes an existing parcel from persistence state.
     */
    public static Parcel reconstitute(
            Long id,
            Long farmId,
            Long cropId,
            String name,
            FieldGeometry geometry,
            ParcelStatus status,
            PhenologicalStage phenologicalStage) {
        var parcel = new Parcel(farmId, name, geometry, cropId, phenologicalStage);
        parcel.id = DomainValidation.requirePositive(id, "id");
        parcel.status = Objects.requireNonNull(status, "status is required");
        return parcel;
    }

    /**
     * Registers the creation events. Called by the repository adapter after the identity is assigned.
     */
    public void onCreated() {
        registerDomainEvent(ParcelRegisteredEvent.from(this));
        if (cropId != null) {
            registerDomainEvent(ParcelCropInformationRegisteredEvent.from(this));
        }
    }

    /**
     * Updates the descriptive data and, optionally, the crop information.
     * A crop information event is registered only when the crop actually changes.
     */
    public void updateDetails(String name, Long cropId, PhenologicalStage phenologicalStage) {
        this.name = DomainValidation.requireText(name, "name", NAME_MAX_LENGTH);
        var cropChanged = !Objects.equals(this.cropId, cropId) || this.phenologicalStage != phenologicalStage;
        if (cropChanged) {
            applyCropInformation(cropId, phenologicalStage);
            if (cropId != null) {
                registerDomainEvent(ParcelCropInformationRegisteredEvent.from(this));
            }
        }
    }

    /**
     * Redefines the boundary, recalculates the area and registers the geometry event.
     */
    public void redefineGeometry(FieldGeometry geometry) {
        this.geometry = Objects.requireNonNull(geometry, "geometry is required");
        registerDomainEvent(ParcelGeometryUpdatedEvent.from(this));
    }

    /**
     * Marks the parcel as inactive. Deactivating an inactive parcel is idempotent.
     */
    public void deactivate() {
        if (status == ParcelStatus.INACTIVE) {
            return;
        }
        this.status = ParcelStatus.INACTIVE;
        registerDomainEvent(new ParcelStatusChangedEvent(id, status));
    }

    /**
     * Marks the parcel as active. Activating an active parcel is idempotent.
     */
    public void activate() {
        if (status == ParcelStatus.ACTIVE) {
            return;
        }
        this.status = ParcelStatus.ACTIVE;
        registerDomainEvent(new ParcelStatusChangedEvent(id, status));
    }

    private void applyCropInformation(Long cropId, PhenologicalStage phenologicalStage) {
        if (cropId == null) {
            if (phenologicalStage != null) {
                throw new IllegalArgumentException("phenologicalStage must be null when cropId is null");
            }
            this.cropId = null;
            this.phenologicalStage = null;
            return;
        }

        this.cropId = DomainValidation.requirePositive(cropId, "cropId");
        this.phenologicalStage = Objects.requireNonNull(
                phenologicalStage,
                "phenologicalStage is required when cropId is present");
    }
}

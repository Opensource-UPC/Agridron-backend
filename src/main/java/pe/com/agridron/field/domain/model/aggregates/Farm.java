package pe.com.agridron.field.domain.model.aggregates;

import lombok.Getter;
import pe.com.agridron.field.domain.events.FarmDetailsUpdatedEvent;
import pe.com.agridron.field.domain.events.FarmRegisteredEvent;
import pe.com.agridron.field.domain.events.FarmSupervisorAssignedEvent;
import pe.com.agridron.field.domain.events.FarmSupervisorRemovedEvent;
import pe.com.agridron.field.domain.model.services.DomainValidation;
import pe.com.agridron.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Farm aggregate root.
 *
 * <p>Owns the descriptive data of a farm and the set of supervisors assigned to it.
 * Parcels are a separate aggregate and reference the farm by id.</p>
 */
@Getter
public class Farm extends AbstractDomainAggregateRoot<Farm> {

    private static final int NAME_MAX_LENGTH = 120;
    private static final int LOCATION_MAX_LENGTH = 255;
    private static final int DEPARTMENT_MAX_LENGTH = 100;
    private static final int IMAGE_MAX_LENGTH = 500;

    private Long id;
    private final Long ownerId;
    private String name;
    private String location;
    private String department;
    private String image;
    private final Set<Long> supervisorIds = new LinkedHashSet<>();

    private Farm(Long ownerId, String name, String location, String department, String image) {
        this.ownerId = DomainValidation.requirePositive(ownerId, "ownerId");
        this.name = DomainValidation.requireText(name, "name", NAME_MAX_LENGTH);
        this.location = DomainValidation.requireText(location, "location", LOCATION_MAX_LENGTH);
        this.department = DomainValidation.requireText(department, "department", DEPARTMENT_MAX_LENGTH);
        this.image = normalizeImage(image);
    }

    /**
     * Creates a new farm. The {@code FarmRegisteredEvent} is registered by {@link #onCreated()}
     * once the persistence adapter has assigned the identity.
     */
    public static Farm register(
            Long ownerId,
            String name,
            String location,
            String department,
            String image) {
        return new Farm(ownerId, name, location, department, image);
    }

    /**
     * Reconstitutes an existing farm from persistence state.
     */
    public static Farm reconstitute(
            Long id,
            Long ownerId,
            String name,
            String location,
            String department,
            Collection<Long> supervisorIds,
            String image) {
        var farm = new Farm(ownerId, name, location, department, image);
        farm.id = DomainValidation.requirePositive(id, "id");
        if (supervisorIds != null) {
            supervisorIds.forEach(farm::addSupervisor);
        }
        return farm;
    }

    /**
     * Registers the creation event. Called by the repository adapter after the identity is assigned.
     */
    public void onCreated() {
        registerDomainEvent(FarmRegisteredEvent.from(this));
    }

    public void updateDetails(String name, String location, String department, String image) {
        var newName = DomainValidation.requireText(name, "name", NAME_MAX_LENGTH);
        var newLocation = DomainValidation.requireText(location, "location", LOCATION_MAX_LENGTH);
        var newDepartment = DomainValidation.requireText(department, "department", DEPARTMENT_MAX_LENGTH);
        var newImage = normalizeImage(image);

        if (Objects.equals(this.name, newName)
                && Objects.equals(this.location, newLocation)
                && Objects.equals(this.department, newDepartment)
                && Objects.equals(this.image, newImage)) {
            return;
        }

        this.name = newName;
        this.location = newLocation;
        this.department = newDepartment;
        this.image = newImage;
        registerDomainEvent(FarmDetailsUpdatedEvent.from(this));
    }

    /**
     * Assigns a supervisor. Assigning the same supervisor twice is idempotent.
     */
    public void assignSupervisor(Long supervisorId) {
        if (addSupervisor(supervisorId)) {
            registerDomainEvent(new FarmSupervisorAssignedEvent(id, supervisorId));
        }
    }

    /**
     * Removes a supervisor. Removing an absent supervisor is idempotent.
     */
    public void removeSupervisor(Long supervisorId) {
        var validatedId = DomainValidation.requirePositive(supervisorId, "supervisorId");
        if (supervisorIds.remove(validatedId)) {
            registerDomainEvent(new FarmSupervisorRemovedEvent(id, validatedId));
        }
    }

    /**
     * Returns an unmodifiable view of the assigned supervisors.
     */
    public Set<Long> getSupervisorIds() {
        return Collections.unmodifiableSet(supervisorIds);
    }

    private boolean addSupervisor(Long supervisorId) {
        return supervisorIds.add(DomainValidation.requirePositive(supervisorId, "supervisorId"));
    }

    private static String normalizeImage(String image) {
        if (image == null || image.isBlank()) {
            return null;
        }
        return DomainValidation.requireText(image, "image", IMAGE_MAX_LENGTH);
    }
}

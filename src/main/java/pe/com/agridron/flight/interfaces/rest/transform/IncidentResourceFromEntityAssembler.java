package pe.com.agridron.flight.interfaces.rest.transform;

import pe.com.agridron.flight.domain.model.entities.Incident;
import pe.com.agridron.flight.interfaces.rest.resources.IncidentResource;

/**
 * Assembler that converts an {@link Incident} entity into an {@link IncidentResource}.
 */
public final class IncidentResourceFromEntityAssembler {

    private IncidentResourceFromEntityAssembler() {
    }

    /**
     * Converts the incident into its REST representation.
     *
     * @param missionId the owning mission; incidents do not hold a back reference
     * @param incident  the incident entity
     */
    public static IncidentResource toResourceFromEntity(Long missionId, Incident incident) {
        return new IncidentResource(
                incident.getId(),
                missionId,
                incident.getType(),
                incident.getDescription(),
                incident.getOccurredAt());
    }
}

package pe.com.agridron.flight.domain.model.entities;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Incident reported while a mission is being executed.
 *
 * <p>It is part of the {@code Mission} aggregate and is only created through
 * {@code Mission#reportIncident}. Incidents are append-only.</p>
 */
@Getter
public class Incident {

    public static final int TYPE_MAX_LENGTH = 50;
    public static final int DESCRIPTION_MAX_LENGTH = 1000;

    private Long id;
    private final String type;
    private final String description;
    private final Instant occurredAt;

    private Incident(String type, String description, Instant occurredAt) {
        this.type = requireText(type, "type", TYPE_MAX_LENGTH);
        this.description = requireText(description, "description", DESCRIPTION_MAX_LENGTH);
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt is required");
    }

    /**
     * Registers a new incident.
     */
    public static Incident register(String type, String description, Instant occurredAt) {
        return new Incident(type, description, occurredAt);
    }

    /**
     * Reconstitutes an existing incident from persistence state.
     */
    public static Incident reconstitute(Long id, String type, String description, Instant occurredAt) {
        var incident = new Incident(type, description, occurredAt);
        incident.id = id;
        return incident;
    }

    private static String requireText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("%s is required".formatted(field));
        }
        var trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(
                    "%s must have at most %d characters".formatted(field, maxLength));
        }
        return trimmed;
    }
}

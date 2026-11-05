package pe.com.agridron.flight.domain.model.entities;

import lombok.Getter;

/**
 * Result registered when a mission is completed.
 *
 * <p>It is part of the {@code Mission} aggregate and is only created through
 * {@code Mission#complete}.</p>
 */
@Getter
public class MissionResult {

    public static final int OBSERVATIONS_MAX_LENGTH = 1000;

    private Long id;
    private final Double treatedArea;
    private final Double appliedVolume;
    private final String observations;

    private MissionResult(Double treatedArea, Double appliedVolume, String observations) {
        this.treatedArea = requireNonNegative(treatedArea, "treatedArea");
        this.appliedVolume = requireNonNegative(appliedVolume, "appliedVolume");
        this.observations = normalize(observations);
    }

    /**
     * Registers a new mission result.
     *
     * @param treatedArea   treated area, in hectares
     * @param appliedVolume applied agrochemical volume, in liters
     * @param observations  optional free-text observations
     */
    public static MissionResult register(Double treatedArea, Double appliedVolume, String observations) {
        return new MissionResult(treatedArea, appliedVolume, observations);
    }

    /**
     * Reconstitutes an existing result from persistence state.
     */
    public static MissionResult reconstitute(
            Long id, Double treatedArea, Double appliedVolume, String observations) {
        var result = new MissionResult(treatedArea, appliedVolume, observations);
        result.id = id;
        return result;
    }

    private static Double requireNonNegative(Double value, String field) {
        if (value == null || !Double.isFinite(value) || value < 0) {
            throw new IllegalArgumentException("%s must be zero or positive".formatted(field));
        }
        return value;
    }

    private static String normalize(String observations) {
        if (observations == null || observations.isBlank()) {
            return null;
        }
        var trimmed = observations.trim();
        if (trimmed.length() > OBSERVATIONS_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "observations must have at most %d characters".formatted(OBSERVATIONS_MAX_LENGTH));
        }
        return trimmed;
    }
}

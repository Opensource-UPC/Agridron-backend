package pe.com.agridron.flight.domain.model.valueobjects;

/**
 * Value object with the flight configuration of a spraying mission.
 *
 * <p>The instance is always valid: every invariant is checked by the compact constructor.</p>
 *
 * @param flightHeight height above the canopy, in meters
 * @param flightSpeed  ground speed, in meters per second
 * @param sprayRate    application rate, in liters per hectare
 * @param overlap      lateral overlap between passes, as a percentage in {@code [0, 100)}
 */
public record FlightParameters(
        Double flightHeight,
        Double flightSpeed,
        Double sprayRate,
        Double overlap) {

    public static final double MAX_OVERLAP = 100.0;

    public FlightParameters {
        requirePositive(flightHeight, "flightHeight");
        requirePositive(flightSpeed, "flightSpeed");
        requirePositive(sprayRate, "sprayRate");
        if (overlap == null || !Double.isFinite(overlap) || overlap < 0 || overlap >= MAX_OVERLAP) {
            throw new IllegalArgumentException("overlap must be between 0 (inclusive) and 100 (exclusive)");
        }
    }

    /**
     * Checks the parameters against their invariants. A constructed instance is always valid,
     * so this method exists to express the rule of the domain model explicitly.
     *
     * @return {@code true}
     */
    public boolean validateParameters() {
        return true;
    }

    private static void requirePositive(Double value, String field) {
        if (value == null || !Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException("%s must be a positive number".formatted(field));
        }
    }
}
